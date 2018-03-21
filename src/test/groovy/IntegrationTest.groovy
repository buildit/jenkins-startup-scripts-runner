import org.junit.*
import org.junit.rules.TemporaryFolder
import org.jvnet.hudson.test.JenkinsRule
import org.jvnet.hudson.test.recipes.LocalData
import utilities.AddScriptToLocalDataZip
import utilities.FreePort

import static org.hamcrest.Matchers.hasItems
import static org.junit.Assert.assertThat
import static utilities.HttpServer.startServer
import static utilities.ResourcePath.resourcePath

class IntegrationTest {

    private static final List SCRIPTS = ["runner.groovy", "startupscript/executor.groovy"]
    private static final String SCRIPT_PATH = "scripts"
    private static final String SCRIPT_TARGET = "init.groovy.d"

    private static final String SERVER_PORT = FreePort.nextFreePort(8000, 9000)
    private static final String SERVER_ADDRESS = "http://localhost:${SERVER_PORT}"

    static server = null

    @ClassRule
    public static TemporaryFolder folder = new TemporaryFolder()

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule()

    @BeforeClass
    public static void setUp(){
        File configFile = folder.newFile("jenkins.config")
        def jenkinsHome = configFile.parent
        System.metaClass.static.getenv = { String key ->
            return [JENKINS_HOME: jenkinsHome].get(key)
        }
        configFile.withWriter {  writer ->
            writer.write(
                    """
                    startupScripts = [
                        local: [
                            artifactPattern: '${SERVER_ADDRESS}/[organisation]/[module]/[revision]/[artifact]-[revision].[ext]',
                            artifacts: ['com.buildit:jenkins-startup-scripts:2.0.0@zip']
                        ]
                    ]
                    
                    env {
                        variables {
                            url = 'http://www.bbc.co.uk\'
                        }
                    }
                    """)
        }
        def serverPath = resourcePath("artifacts", "") as String
        server = startServer(serverPath, SERVER_PORT)
        AddScriptToLocalDataZip.addScriptToLocalDataZip(IntegrationTest.class, SCRIPTS, SCRIPT_PATH, SCRIPT_TARGET)
    }

    @Test
    @LocalData
    void shouldConfigureEnvVariablesFromConfig(){
        def envVarsNodePropertyList = jenkins.model.Jenkins.instance.globalNodeProperties.getAll(hudson.slaves.EnvironmentVariablesNodeProperty.class)
        def envVars = envVarsNodePropertyList[0].envVars
        assertThat(envVars.keySet(), hasItems("url"))
        assertThat(envVars.values(), hasItems("http://www.bbc.co.uk"))
    }

    @AfterClass
    public static void tearDown(){
        def serverPath = resourcePath("", "artifacts") as String
        server.stop()
    }
}

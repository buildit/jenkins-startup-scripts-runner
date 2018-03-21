import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.ClassRule
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.jvnet.hudson.test.JenkinsRule
import org.jvnet.hudson.test.recipes.LocalData
import utilities.AddScriptToLocalDataZip
import utilities.FreePort
import utilities.ZipTestFiles

import static org.hamcrest.Matchers.hasItems
import static org.junit.Assert.assertThat
import static utilities.HttpServer.startServer
import static utilities.ResourcePath.resourcePath

class LocalIntegrationTest {

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
        configFile.withWriter {  writer ->
            writer.write(
                    """
                    startupScripts = [
                        local: [
                            artifactPattern: '${SERVER_ADDRESS}/[organisation]/[module]/[revision]/[artifact]-[revision].[ext]',
                            artifacts: ["com.buildit:jenkins-startup-scripts:2.0.0@zip"]
                        ]
                    ]
                    
                    env {
                        variables {
                            url = 'http://www.bbc.co.uk\'
                        }
                    }
                    """)
        }
        AddScriptToLocalDataZip.addScriptToLocalDataZip(LocalIntegrationTest.class, SCRIPTS, SCRIPT_PATH, SCRIPT_TARGET)
        def serverPath = resourcePath("artifacts", "") as String
        server = startServer(serverPath, SERVER_PORT)
        def directory = folder.newFolder().absolutePath
        def zip = resourcePath("jenkins-startup-scripts-2.0.0.zip", "artifacts/com/buildit/jenkins-startup-scripts/2.0.0") as String
        new AntBuilder().unzip(src: zip, dest:directory)
        System.metaClass.static.getenv = { String secret ->
            return [JENKINS_STARTUP_SCRIPTS: directory, JENKINS_HOME: jenkinsHome].get(secret)
        }
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

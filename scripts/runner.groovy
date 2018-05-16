import org.codehaus.groovy.control.CompilerConfiguration

import static groovy.io.FileType.FILES

def baseDirectory = baseDirectory()
def classLoader = getAugmentedClassLoader("${baseDirectory}/lib")

def runner = load("${baseDirectory}/startupscript/executor.groovy", [:], classLoader)
runner.main()

def load(script, args=[:], classLoader=this.class.classLoader){
    CompilerConfiguration compilerConfiguration = new CompilerConfiguration()
    def shell = new GroovyShell(classLoader, new Binding(), compilerConfiguration)
    def file = new File("${script}")
    try{
        if(file.exists()){
            println("Loading script : ${file.absolutePath}")
            shell.getClassLoader().addURL(file.parentFile.toURI().toURL())
            def result = shell.evaluate("new " + file.name.split("\\.", 2)[0] + "()")
            args.each{ k, v -> result."${k}" = v}
            return result
        }
    }catch(Exception e){
        println("Error loading script : ${e.getMessage()}")
        e.printStackTrace()
    }
}

def jenkinsHome(){
    if(System.getenv("JENKINS_HOME")){
        return System.getenv("JENKINS_HOME")
    }
    if(new File('/etc/default/jenkins').exists()){
        return readProperties('/etc/default/jenkins').getProperty('JENKINS_HOME')
    }
    if(new File('/etc/sysconfig/jenkins').exists()){
        return readProperties('/etc/sysconfig/jenkins').getProperty('JENKINS_HOME')
    }
    throw new IllegalStateException("JENKINS_HOME value cannot be found")
}

def baseDirectory(){
    def jenkinsHomeInitDir = "${jenkinsHome()}/init.groovy.d"
    if(new File("${jenkinsHomeInitDir}/runner.groovy").exists()){
        return jenkinsHomeInitDir
    }
    File thisScript = new File(getClass().protectionDomain.codeSource.location.path)
    return thisScript.getParent()
}

def getAugmentedClassLoader(String libDirectory){
    def additionalJars = []
    def lib = new File(libDirectory)
    if(lib.exists()){
        lib.traverse(type: FILES, maxDepth: 0) {
            additionalJars.add(it.toURI().toURL())
        }
    }
    return new URLClassLoader(additionalJars as URL[], this.class.classLoader)
}

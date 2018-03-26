
# Jenkins Startup Scripts Runner

A framework to enable configuration of jenkins at startup. 

## Using the Runner

The simplest way to use the runner is via the Buildit [Jenkins Image](https://github.com/buildit/jenkins-image).

To use the runner outside of the image, unzip the build artifact to $JENKINS_HOME/init.groovy.d and provide the configuration as described in the README.md of the [Jenkins Config Fetcher](https://github.com/buildit/jenkins-config-fetcher).

## Startup Scripts

An archive of startup scripts can be pulled from a remote location at runtime. The following configuration block in a .config file will initiate a download of jenkins-startup-scripts-X-X-X.zip from bintray during startup.

```groovy
startupScripts=[
        bintray: [
                artifactPattern: 'https://dl.bintray.com/buildit/maven/com/buildit/[module]/[revision]/[module]-[revision].[ext]',
                artifacts: [":jenkins-startup-scripts:X.X.X@zip"]
        ]
]
```

The runner will then proceed to unzip the archive and look for a file called main.groovy in the archive's root directory before binding the jenkinsConfig value (with the entire parsed config) and running the main.main() method. 



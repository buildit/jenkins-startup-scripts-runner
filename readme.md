
# Jenkins Startup Scripts Runner

A small framework to enable configuration of jenkins at startup

## Configuration File

The runner takes a groovy [ConfigSlurper](http://docs.groovy-lang.org/latest/html/gapi/groovy/util/ConfigSlurper.html) file as input. By default, the scripts will look for a file called JENKINS_HOME/jenkins.config, however, the name and location of this configuration file can be configured via the JENKINS_CONFIG_FILE env variable.

The JENKINS_CONFIG_FILE can be made up of a list of config files like so

```
export JENKINS_CONFIG_FILE=jenkins_common.config,jenkins_specific.config
```

These files will be loaded in the order in which they are listed - allowing values in jenkins_specific.config to override values in jenkins_common.config. It is also possible to to set the value to a list of urls pointing to single files, a zip archive or a git repository.

```
export JENKINS_CONFIG_FILE=http://some_server/file.config,http://some_server/some.zip,http://some_git_repo_server/some_project.git,http://some_git_repo_server/some_other_project.git#some_tag_or_branch
```

All configuration files must end with the a .config suffix. Zip files and git repositories will be pulled down at runtime and scanned for any file with the .config suffix.

Note. multiple files in zip archives or git repositories will be loaded in a random order. If you wish to override values then please ensure that specific values are located in a repository or archive specified last in the list.

Note. The following environment variables are available to enable cloning from from secure git repostories; JENKINS_CONFIG_GIT_USERNAME, JENKINS_CONFIG_GIT_PASSWORD and JENKINS_CONFIG_GIT_PASSWORD_FILE (use JENKINS_CONFIG_GIT_PASSWORD_FILE if the password is stored in a file).

Note. The JENKINS_CONFIG_GIT_VERIFY_SSL environment variable should be set to 'false' if your git repository is secured using a self signed certificate.

## Passwords and Sensitive Information

All passwords and sensitive information stored in configuration should be encrypted. Any encrypted values in the configuration file must be wrapped using ENC(...). These values will then be decrypted at runtime.

To encrypt a password run the following docker image

```
docker run -p 4567:4567 builditdigital/encryptor-api:1.10.9
```

And navigate to http://localhost:4567/. Enter the value you wish encrypt in the "password" field and a secret key in the "secret" field.

Copy the output to the config file ensuring any multline values are wrapped in triple quotes like so

```
'''ENC(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)
'''
```

Note. It is necessary to wrap single line encrypted values in triple quotes if the decryped value will span more than one line.

In order to decrypt these values, the key must be provided using the JENKINS_STARTUP_SECRET or JENKINS_STARTUP_SECRET_FILE environment variable (the location of a file that holds the key) on the Jenkins master when the scripts are executed.

## Configuring Startup Scripts

An archive of startup scripts can be pulled from a remote location at runtime. The following configuration block in a .config file will initiate a download of jenkins-startup-scripts-X-X-X.zip from bintray during startup.

```groovy
startupScripts=[
        bintray: [
                artifactPattern: 'https://dl.bintray.com/buildit/maven/com/buildit/[module]/[revision]/[module]-[revision].[ext]',
                artifacts: [":jenkins-startup-scripts:X.X.X@zip"]
        ]
]
```

The runner will then proceed to unzip the archive and look for a file called main.groovy in the archives root directory before binding the jenkinsConfig value (with the entire parsed config) and running the main() method. 



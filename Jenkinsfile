pipeline {
    agent any

    stages {

        stage ('Build') {
            steps {
                echo 'This is dsadsfsdgsf very minimal pipeline.'
                withMaven(
                        // Maven installation declared in the Jenkins "Global Tool Configuration"
                        maven: 'jenkins-maven',
                        // Maven settings.xml file defined with the Jenkins Config File Provider Plugin
                        // Maven settings and global settings can also be defined in Jenkins Global Tools Configuration
                        mavenSettingsConfig: '60f738ac-43d1-47e2-b989-fef3e4065709',
                        mavenLocalRepo: '.repository') {

                      // Run the maven build
                      sh "mvn --version"
                      sh "mvn clean install"
                 }

            }
        }
    }
}

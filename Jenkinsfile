pipeline {
    agent any

    stages {


        stage ('Build') {
            steps {
                echo 'This is a minimal pipeline.'
                withMaven(
                        // Maven installation declared in the Jenkins "Global Tool Configuration"
                        maven: 'jenkins-maven',
                        // Maven settings.xml file defined with the Jenkins Config File Provider Plugin
                        // Maven settings and global settings can also be defined in Jenkins Global Tools Configuration
                        mavenSettingsConfig: 'MavenGlobalSettings',
                        mavenLocalRepo: '.repository') {

                      // Run the maven build
                      sh "mvn clean install"
                      }
            }
        }
    }
}
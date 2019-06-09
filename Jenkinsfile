pipeline {
    agent any
    tools {
        // Note: this should match with the tool name configured in your jenkins instance (JENKINS_URL/configureTools/)
        maven "jenkins-maven"
    }
    environment {
        
        NEXUS_VERSION = "nexus3"
        NEXUS_PROTOCOL = "http"
        NEXUS_URL = "35.202.156.106:8081"
        NEXUS_REPOSITORY = "${(env.BRANCH_NAME).matches('release/(.*)') ? 'dash-maven-releases' : 'dash-maven-snapshots' }"
        // Jenkins credential id to authenticate to Nexus 
        NEXUS_CREDENTIAL_ID = "nexus-cred"
        // DOCKER registry 
        DOCKER_REGISTRY = "knights007/spring-boot-cd"
        //registryCredential 
        DOCKER_REGISTRY_CREDENTIALS = 'dockerhub-credentials'
        dockerImage = ''
        helm_home = tool name: 'helm-jenkins', type: 'com.cloudbees.jenkins.plugins.customtools.CustomTool'
    }
    stages {

        stage("Helm install")
        {
            steps 
            {
                script 
                {    
                git branch: 'master' , url:'https://github.com/knightz007/dash-helm.git';
                sh "ls -ltr ${WORKSPACE}"
                sh """
                ${helm_home}/linux-amd64/helm version
                ${helm_home}/linux-amd64/helm install --debug ./dash-helm --name=release-1 --set namespace.name=dev --namespace dev

                """
                }
            }
        }

        stage("Clone code") {
            steps {
                script {                    
                    sh 'printenv'
                    git branch: env.BRANCH_NAME , url:'https://github.com/knightz007/dash.git';
                }
            }
        }
        stage("Maven build") {
            steps {
                script {
                    sh "mvn package -DskipTests=true"
                }
            }
        }
        stage("Upload artifacts to Nexus") {
            steps {
                script {

                    // Read POM xml file using 'readMavenPom' step , this step 'readMavenPom' is included in: https://plugins.jenkins.io/pipeline-utility-steps
                    pom = readMavenPom file: "pom.xml";
                    // Find built artifact under target folder
                    filesByGlob = findFiles(glob: "target/*.${pom.packaging}");
                    // Print some info from the artifact found
                    echo "${filesByGlob[0].name} ${filesByGlob[0].path} ${filesByGlob[0].directory} ${filesByGlob[0].length} ${filesByGlob[0].lastModified}"
                    // Extract the path from the File found
                    artifactPath = filesByGlob[0].path;
                    // Assign to a boolean response verifying If the artifact name exists
                    artifactExists = fileExists artifactPath;
                    if(artifactExists) {
                        echo "*** File: ${artifactPath}, group: ${pom.groupId}, packaging: ${pom.packaging}, version ${pom.version}";
                        nexusArtifactUploader(
                            nexusVersion: NEXUS_VERSION,
                            protocol: NEXUS_PROTOCOL,
                            nexusUrl: NEXUS_URL,
                            groupId: pom.groupId,
                            version: pom.version,
                            repository: NEXUS_REPOSITORY,
                            credentialsId: NEXUS_CREDENTIAL_ID,
                            artifacts: [
                                // Artifact generated such as .jar, .ear and .war files.
                                [artifactId: pom.artifactId,
                                classifier: '',
                                file: artifactPath,
                                type: pom.packaging],
                                [artifactId: pom.artifactId,
                                classifier: '',
                                file: "pom.xml",
                                type: "pom"]
                            ]
                        );
                    } else {
                        error "*** File: ${artifactPath}, could not be found";
                    }
                }
            }
        }

        stage('Build image') {
          steps{
            script {
                //dockerImage = docker.build registry + ":$BUILD_NUMBER"
                def dockerfile = 'Dockerfile'
                // Get artifact details from pom
                pom = readMavenPom file: "pom.xml";
                artifact = findFiles(glob: "target/*.${pom.packaging}");
                artifactPath = artifact[0].path;
                //def artifactName = artifact[0].name;
                pomVersion = pom.version;

                sh "ls -ltr ${WORKSPACE}"
                
                //create tag and build image
                tag = "${pomVersion}_${BUILD_NUMBER}" 

                dir(WORKSPACE)
                {
                dockerImage = docker.build("${DOCKER_REGISTRY}:${tag}", "--build-arg JAR_FILE=${artifactPath} -f Dockerfile ./")
                }
            }
          }
        }

        stage('Deploy Image') {
          steps{
             script 
                {
                    docker.withRegistry( '', DOCKER_REGISTRY_CREDENTIALS ) 
                    {
                        dockerImage.push()
                    }
                }   
            }        
        }



    }
}
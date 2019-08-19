pipeline {
    agent any
    tools {
        maven "jenkins-maven"
    }
    environment {
        
        NEXUS_VERSION = "nexus3"
        NEXUS_PROTOCOL = "http"
        NEXUS_URL = "nexus-web.hopto.org:8080"
        NEXUS_REPOSITORY = "${(env.BRANCH_NAME).matches('release/(.*)') ? 'dash-maven-releases' : 'dash-maven-snapshots' }"
        // Jenkins credential id to authenticate to Nexus 
        NEXUS_CREDENTIAL_ID = "nexus-cred"
        // DOCKER registry 
        DOCKER_REGISTRY = "knights007/spring-boot-cd"
        //registryCredential 
        DOCKER_REGISTRY_CREDENTIALS = 'dockerhub-credentials'
        DOCKER_IMAGE = ''
        DOCKER_IMAGE_TAG = ''
        HELM_HOME = tool name: 'helm-jenkins', type: 'com.cloudbees.jenkins.plugins.customtools.CustomTool'
        NAMESPACE = "${env.BRANCH_NAME.matches('release/(.*)') ? 'prod' : 'dev'}"
        ANCHORE_ENGINE_URL='http://anchore-web.hopto.org:8228/v1'
    }
    stages {

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
                    // Read POM xml file using 'readMavenPom'
                    pom = readMavenPom file: "pom.xml";
                    // Find the artifact in target folder
                    filesByGlob = findFiles(glob: "target/*.${pom.packaging}");
                    // Print artifact info
                    echo "${filesByGlob[0].name} ${filesByGlob[0].path} ${filesByGlob[0].directory} ${filesByGlob[0].length} ${filesByGlob[0].lastModified}"
                    // Extract artifact path
                    artifactPath = filesByGlob[0].path;
                    // Check if the artifact name exists
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

        stage('Build Docker image') {
          steps{
            script {                
                dockerfile = 'Dockerfile'
                // Get artifact details from pom
                pom = readMavenPom file: "pom.xml";
                pomVersion = pom.version;
                artifact = findFiles(glob: "target/*.${pom.packaging}");
                artifactPath = artifact[0].path;  
                // Pring workspace items
                sh "ls -ltr ${WORKSPACE}"
                
                //create tag 
                DOCKER_IMAGE_TAG = "${pomVersion}_${BUILD_NUMBER}" 
                // Build image
                dir(WORKSPACE)
                {
                DOCKER_IMAGE = docker.build("${DOCKER_REGISTRY}:${DOCKER_IMAGE_TAG}", "--build-arg JAR_FILE=${artifactPath} -f Dockerfile ./")
                }
            }
          }
        }

        stage('Push docker image to DockerHub') {
          steps{
             script 
                {
                    docker.withRegistry( '', DOCKER_REGISTRY_CREDENTIALS ) 
                    {
                        DOCKER_IMAGE.push()
                    }
                }   
            }        
        }

        stage("Perform security scan on docker image") 
        {
            steps 
            {
                script 
                {
                    sh """
                       echo "${DOCKER_REGISTRY}:${DOCKER_IMAGE_TAG}" > anchore_images
                    """
                    anchore autoSubscribeTagUpdates: false, engineCredentialsId: 'anchore-cred', engineurl: ANCHORE_ENGINE_URL, name: 'anchore_images', bailOnFail: false
                }
            }
        }

        stage("Helm: Deploy to Kubernetes")
        {
            steps 
            {
                script 
                { 
                // Checkout the helm chart
                sh 'mkdir -p dash-helm'
                dir('dash-helm')
                {
                git branch: 'master' , url:'https://github.com/knightz007/dash-helm.git';
                }

                // Install the helm chart
                sh """
                ${HELM_HOME}/linux-amd64/helm version
                ${HELM_HOME}/linux-amd64/helm ls --all --namespace ${NAMESPACE} --short | xargs -L1 ${HELM_HOME}/linux-amd64/helm delete --purge || true
                sleep 10
                ${HELM_HOME}/linux-amd64/helm install --debug ./dash-helm --name=${NAMESPACE}-${env.BUILD_NUMBER} --set namespace.name=${NAMESPACE} --set persistentVolume.pdName=mysql-pd-${NAMESPACE} --set deployment.web.image=${DOCKER_REGISTRY} --set deployment.web.tag=${DOCKER_IMAGE_TAG} --namespace ${NAMESPACE}
                """

                    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId:"root_dbcred_${NAMESPACE}", usernameVariable: 'ROOT_USERNAME', passwordVariable: 'ROOT_PASSWORD']]) {
                    
                    sh """
                    kubectl create secret generic dash-secret1 --from-literal=root_password=${ROOT_PASSWORD} --namespace ${NAMESPACE}
                    """
                    }


                }
            }
        }

        stage("QA Test:- Access url")
        {
            steps {
                script 
                {   
                    // Form the service name for this build and corresponding namespace
                    def dashSvcName = "${NAMESPACE}-${env.BUILD_NUMBER}-dash-chart-web-service"
                    
                    // Wait for the load balancer to be ready
                    sh """
                    #!/bin/bash
                    loadBalancer_ip=''
                    while [ -z \$loadBalancer_ip ]; do
                      echo "Waiting for end point..."
                      loadBalancer_ip="`kubectl get svc ${dashSvcName} --namespace=${NAMESPACE} --template='{{range .status.loadBalancer.ingress}}{{.ip}}{{end}}'`"
                      [ -z "\$external_ip" ] && sleep 10
                    done
                    echo 'Load Balancer ip:' && echo \$loadBalancer_ip
                    """
                    // Print out the load balancer service url
                    sh("echo `kubectl --namespace=${NAMESPACE} get service/${dashSvcName} --output=json | jq -r '.status.loadBalancer.ingress[0].ip'` > ${dashSvcName}")
                    sh("echo ACCESS_URL: http://`cat ${dashSvcName}`:8080/color.html")
                }
            }
        }  
    }
}

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

    }
    stages {
        stage("Install Docker") {
              agent {
                kubernetes {
                        yaml """
                        apiVersion: v1
                        kind: Pod
                        metadata:
                          labels:
                            name: docker
                        spec:
                          containers:
                          - name: docker
                            image: docker:latest
                            command:
                            - cat
                            tty: true
                        """
                        }
                    }
                 steps {
                    script
                    {
                        container("docker")
                        {
                            sh 'docker --version' 
                        } 
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
                dockerfile = 'Dockerfile'
                // Get artifact details from pom
                pom = readMavenPom file: "pom.xml";
                pomVersion = pom.version;
                artifact = findFiles(glob: "target/*.${pom.packaging}");
                artifactPath = artifact[0].path;

                sh "ls -ltr ${WORKSPACE}"

                //create tag and build image
                DOCKER_IMAGE_TAG = "${pomVersion}_${BUILD_NUMBER}"
                container("docker")
                {
                    dir(WORKSPACE)
                    {
                    DOCKER_IMAGE = docker.build("${DOCKER_REGISTRY}:${DOCKER_IMAGE_TAG}", "--build-arg JAR_FILE=${artifactPath} -f Dockerfile ./")
                    }
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
                        DOCKER_IMAGE.push()
                    }
                }
            }
        }

        stage("Helm: Deploy to Kubernetes")
        {
            steps
            {
                script
                {
                sh 'mkdir dash-helm'
                dir('dash-helm')
                {
                git branch: 'master' , url:'https://github.com/knightz007/dash-helm.git';
                }

                sh """
                ${HELM_HOME}/linux-amd64/helm version
                ${HELM_HOME}/linux-amd64/helm ls --all --namespace ${NAMESPACE} --short | xargs -L1 ${HELM_HOME}/linux-amd64/helm delete --purge || true
                sleep 10
                ${HELM_HOME}/linux-amd64/helm install --debug ./dash-helm --name=${NAMESPACE}-${env.BUILD_NUMBER} --set namespace.name=${NAMESPACE} --set persistentVolume.pdName=mysql-pd-${NAMESPACE} --set deployment.web.image=${DOCKER_REGISTRY} --set deployment.web.tag=${DOCKER_IMAGE_TAG} --namespace ${NAMESPACE}
                """
                }
            }
        }

        stage("QA Test:- Access url")
        {
            steps {
                script
                {
                    def dashSvcName = "${NAMESPACE}-${env.BUILD_NUMBER}-dash-chart-web-service"

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

                    sh("echo `kubectl --namespace=${NAMESPACE} get service/${dashSvcName} --output=json | jq -r '.status.loadBalancer.ingress[0].ip'` > ${dashSvcName}")
                    sh("echo ACCESS_URL: http://`cat ${dashSvcName}`:8080/Color.html")
                }
            }
        }

    }
}
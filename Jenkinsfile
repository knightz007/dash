def label = "worker-${UUID.randomUUID().toString()}"

podTemplate(label: label, containers: [
  containerTemplate(name: 'gradle', image: 'gradle:4.5.1-jdk9', command: 'cat', ttyEnabled: true),
  containerTemplate(name: 'maven', image: 'maven:3.3.9-jdk-8-alpine', ttyEnabled: true, command: 'cat'),
  containerTemplate(name: 'docker', image: 'docker', command: 'cat', ttyEnabled: true),
  containerTemplate(name: 'kubectl', image: 'lachlanevenson/k8s-kubectl:v1.8.8', command: 'cat', ttyEnabled: true),
  containerTemplate(name: 'helm', image: 'lachlanevenson/k8s-helm:latest', command: 'cat', ttyEnabled: true),
  containerTemplate(name: 'anchore', image: 'anchore/engine-cli', command: 'cat', ttyEnabled: true)
],
volumes: [
  hostPathVolume(mountPath: '/home/gradle/.gradle', hostPath: '/tmp/jenkins/.gradle'),
  hostPathVolume(mountPath: '/var/run/docker.sock', hostPath: '/var/run/docker.sock')
]) {
  node(label) {
    // def myRepo = checkout scm
    // def gitCommit = myRepo.GIT_COMMIT
    // def gitBranch = myRepo.GIT_BRANCH
    // def shortGitCommit = "${gitCommit[0..10]}"
    // def previousGitCommit = sh(script: "git rev-parse ${gitCommit}~", returnStdout: true)

    def NEXUS_VERSION = "nexus3"
    def NEXUS_PROTOCOL = "http"
    def NEXUS_URL = "nexus-web.hopto.org:8080"
    // def NEXUS_REPOSITORY = "${(${env.BRANCH_NAME}).matches('release/(.*)') ? 'dash-maven-releases' : 'dash-maven-snapshots' }"
    def NEXUS_REPOSITORY = "${env.BRANCH_NAME}".matches('release/(.*)') ? 'dash-maven-releases' : 'dash-maven-snapshots'
    // Jenkins credential id to authenticate to Nexus
    def NEXUS_CREDENTIAL_ID = "nexus-cred"
    // DOCKER registry
    def DOCKER_REGISTRY = "knights007/spring-boot-cd"
    //registryCredential
    def DOCKER_REGISTRY_CREDENTIALS = 'dockerhub-credentials'
    def DOCKER_IMAGE = ''
    def DOCKER_IMAGE_TAG = ''
    def HELM_HOME = tool name: 'helm-jenkins', type: 'com.cloudbees.jenkins.plugins.customtools.CustomTool'
    def NAMESPACE = "${env.BRANCH_NAME.matches('release/(.*)') ? 'prod' : 'dev'}"
    def ANCHORE_ENGINE_URL='http://anchore-web.hopto.org:8228/v1'

    stage('Print Nexus repo') {
        sh "echo ${NEXUS_REPOSITORY}"
        // container('maven') {
        //     stage('Maven Build') {
        //         sh 'mvn package -DskipTests=true'
        //     }
        // }
    }

    stage('Get a Maven project') {
        git branch: env.BRANCH_NAME , url:'https://github.com/knightz007/dash.git';
        container('maven') {
            stage('Maven Build') {
                sh 'mvn package -DskipTests=true'
            }
        }
    }

    stage("Upload artifacts to Nexus") {
        container('maven') {

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
                            nexusVersion: "${NEXUS_VERSION}",
                            protocol: "${NEXUS_PROTOCOL}",
                            nexusUrl: "${NEXUS_URL}",
                            groupId: pom.groupId,
                            version: pom.version,
                            repository: "${NEXUS_REPOSITORY}",
                            credentialsId: "${NEXUS_CREDENTIAL_ID}",
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

        stage('Build image') {
          // steps
          // {
          //   script {
                // container('maven')
                // {
                    dockerfile = 'Dockerfile'
                    // Get artifact details from pom
                    pom = readMavenPom file: "pom.xml";
                    pomVersion = pom.version;
                    artifact = findFiles(glob: "target/*.${pom.packaging}");
                    artifactPath = artifact[0].path;

                    sh "ls -ltr ${WORKSPACE}"

                    //create tag and build image
                    DOCKER_IMAGE_TAG = "${pomVersion}_${BUILD_NUMBER}"
                // }

                container('docker')
                {
                    dir(WORKSPACE)
                    {
                        DOCKER_IMAGE = docker.build("${DOCKER_REGISTRY}:${DOCKER_IMAGE_TAG}", "--build-arg JAR_FILE=${artifactPath} -f Dockerfile ./")
                    }
                }

          //   }
          // }
        }

        stage('Deploy Image') {
            container('docker')
                {
                    docker.withRegistry( '', DOCKER_REGISTRY_CREDENTIALS )
                    {
                        DOCKER_IMAGE.push()
                    }
                }            
        }

        stage('Perform security scan on docker image')
        {

//              container('anchore')
//                 {
                    sh """
                       echo "${DOCKER_REGISTRY}:${DOCKER_IMAGE_TAG}" > anchore_images
                    """
                    anchore autoSubscribeTagUpdates: false, engineCredentialsId: 'anchore-cred', engineurl: ANCHORE_ENGINE_URL, name: 'anchore_images', bailOnFail: false

//                        anchore-cli image add ${DOCKER_REGISTRY}:${DOCKER_IMAGE_TAG}
//                        anchore-cli evaluate check ${DOCKER_REGISTRY}:${DOCKER_IMAGE_TAG}
//                 }
        }


        stage("Helm: Deploy to Kubernetes")
        {

                sh 'mkdir dash-helm'
                dir('dash-helm')
                {
                git branch: 'master' , url:'https://github.com/knightz007/dash-helm.git';
                }
                container('helm')
                {
                sh """
                ${HELM_HOME}/linux-amd64/helm version
                ${HELM_HOME}/linux-amd64/helm ls --all --namespace ${NAMESPACE} --short | xargs -L1 ${HELM_HOME}/linux-amd64/helm delete --purge || true
                sleep 10
                ${HELM_HOME}/linux-amd64/helm install --debug ./dash-helm --name=${NAMESPACE}-${env.BUILD_NUMBER} --set namespace.name=${NAMESPACE} --set persistentVolume.pdName=mysql-pd-${NAMESPACE} --set deployment.web.image=${DOCKER_REGISTRY} --set deployment.web.tag=${DOCKER_IMAGE_TAG} --namespace ${NAMESPACE}
                """
            }            
        }

        stage("QA Test:- Access url")
        {
            container('kubectl')
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


    // stage('Create Docker images') {
    //   container('docker') {
    //     sh "docker images"
    //     sh "echo ${DOCKER_REGISTRY}"
    //   }
    // }
    // stage('Run kubectl') {
    //   container('kubectl') {
    //     sh "kubectl get pods"
    //   }
    // }
    // stage('Run helm') {
    //   container('helm') {
    //     sh "helm list"
    //   }
    // }


  }
}
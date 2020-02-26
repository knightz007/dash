def label = "worker-${UUID.randomUUID().toString()}"

podTemplate(label: label, containers: [
  containerTemplate(name: 'gradle', image: 'gradle:4.5.1-jdk9', command: 'cat', ttyEnabled: true),
  containerTemplate(name: 'docker', image: 'docker', command: 'cat', ttyEnabled: true),
  containerTemplate(name: 'kubectl', image: 'lachlanevenson/k8s-kubectl:v1.8.8', command: 'cat', ttyEnabled: true),
  containerTemplate(name: 'helm', image: 'lachlanevenson/k8s-helm:latest', command: 'cat', ttyEnabled: true)
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
    def NEXUS_REPOSITORY = "${(env.BRANCH_NAME).matches('release/(.*)') ? 'dash-maven-releases' : 'dash-maven-snapshots' }"
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

    stage('Create Docker images') {
      container('docker') {
        sh "docker images"
        sh "echo ${DOCKER_REGISTRY}"
      }
    }
    stage('Run kubectl') {
      container('kubectl') {
        sh "kubectl get pods"
      }
    }
    stage('Run helm') {
      container('helm') {
        sh "helm list"
      }
    }
  }
}
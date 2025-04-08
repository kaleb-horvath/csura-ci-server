pipeline {
    agent any
    tools {
        jdk 'JDK 11' // defined in your Groovy init
        maven 'Maven 3.9.6' // if also defined in Groovy init
    }

    // job parameters
    environment {
        PROJECT_NAME = 'WordCount'
        PROJECT_TARGET_JDK = '11'

        SCM_USERNAME = 'kaleb-horvath'
        SCM_REMOTE_NAME = 'wordcount-hadoop-job-source.git'
        SCM_CREDENTIALS_ID = 'jenkins-scm-identity'

        BRANCHES = 'main'
    }

    stages {
        stage('Verify Environment') {
            steps {
                script {
                    def mvnVersionInfo = sh(script: "mvn -version", returnStdout: true).trim()
                    echo "${mvnVersionInfo}"
                    if (!mvnVersionInfo.contains("Java version: ${PROJECT_TARGET_JDK}")) {
                        error "JDK version should be ${PROJECT_TARGET_JDK} for this build. Check your environment"
                    }
                }
                sh 'echo $JAVA_HOME'
            }
        }
        

        stage('Checkout Source') {
            steps {
                script {
                    def branches = BRANCHES.split(',')
                    branches.each { branch -> 
                        checkout([
                            $class: 'GitSCM',
                            branches: [[name: "*/${branch.trim()}"]],
                            userRemoteConfigs: [[
                                url: "git@github.com:${SCM_USERNAME}/${SCM_REMOTE_NAME}",
                                credentialsId: "${SCM_CREDENTIALS_ID}"
                            ]]
                        ])}
                }
            }
        }



        stage('Build') {
            steps {
                sh 'mvn dependency:resolve'
                sh 'mvn clean package -DskipTests'
            }
        }

        // deliberate separate unit test stage
        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }

        stage('Archive Artifacts') {
            steps {
                script {
                    def artifactName = sh(script: 'basename $(ls target/*.jar)', returnStdout: true).trim()
                    archiveArtifacts artifacts: "target/${artifactName}", fingerprint: true, allowEmptyArchive: true
                    sh "cp target/${artifactName} /var/jenkins_home/artifacts"
                }
            }
        }
    }
    
    post {
        success {
            echo 'Build completed successfully!'
            // sh '''find target -type f ! -name '*.jar' -exec rm -f {} +'''
            sh 'rm -rf target/*'

        }
        failure {
            echo 'Build failed. Check the logs for details.'
        }        
    }
}
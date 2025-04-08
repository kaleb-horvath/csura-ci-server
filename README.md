#### Deployment
Launch Jenkins server with persistent storage.
```
$ docker volume create jenkins_home
$ docker build --no-cache -t jenkins:v1 .
$ docker run -d \
    -v jenkins_home:/var/jenkins_home \
    --name jenkins-server \
    --env-file env.list
    -p 8080:8080 jenkins:v1 

```
**Bootstrap SSH Credentials for SCM**<br>
Copy keypair files and launch a shell on the server.
```
$ docker cp /path/to/ssh/creds/* jenkins-server:/var/jenkins_home/scm_credentials
$ docker exec -it jenkins-server bash
```
Verify permissions and configure SSH agent, then test connection to VCS provider.
```
jenkins@containerId:/$ ls -l /var/jenkins_home/scm_credentials/*
jenkins@containerId:/$ eval "$(ssh-agent -s)"
jenkins@containerId:/$ ssh-add /var/jenkins_home/scm_credentials/id_rsa
jenkins@containerId:/$ ssh -T git@github.com
```
Configure `jenkins-scm-identity` in the credentials manager with the script that comes with this image.
```
jenkins@containerId:/$ java -jar /var/jenkins_home/jenkins-cli.jar -s http://localhost:8080 -auth admin:admin groovy = < /var/jenkins_home/init.groovy.d/configure-ssh-credentials.groovy
```
**SCM in Declarative Pipeline**<br>
You can use the included SCM plugins directly in a declarative pipeline:
```groovy
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
```
**Environment Variables for Current Setup**<br>
```
#env.list
JENKINS_SERVER_URL=http://localhost
JENKINS_SERVER_PORT=8080
JENKINS_SERVER_ARTIFACT_BACKUP_DIR=/var/jenkins_home/artifacts
JENKINS_SERVER_JOB_XML_DIR=/var/jenkins_home/job_descriptions
JENKINS_SERVER_SCM_CREDENTIALS_PRIVATE_KEY=/var/jenkins_home/scm_credentials/id_rsa
JENKINS_SERVER_SCM_USERNAME=kaleb-horvath
JENKINS_SERVER_SCM_CREDENTIALS_ID=jenkins-scm-identity 
```
*NOTE: `setup.sh` assumes the above environment, and makes other assumptions*
*NOTE: `Dockerfile` assumes the above environment, changing the environment will not change the behavior of the docker build, but only what the Jenkins server uses*
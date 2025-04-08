#### Deployment
Launch Jenkins server with persistent storage.
```
$ docker volume create jenkins_home
$ docker build --no-cache -t jenkins:v1 .
$ docker run -d \
    -v jenkins_home:/var/jenkins_home \
    --name jenkins-server \
    -p 8080:8080 jenkins:v1 

```
**Bootstrap SSH Credentials for SCM**
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
**SCM in Declarative Pipeline**
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
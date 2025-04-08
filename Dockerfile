FROM jenkins/jenkins:2.480-jdk17

USER root

# tooling
RUN apt-get update && apt-get install -y \
    apt-transport-https \ 
    ca-certificates \
    curl \
    software-properties-common \
    net-tools iputils-ping \
    && apt-get clean 

# Install required packages and SDKMAN + JDKs
RUN apt-get update && apt-get install -y curl unzip zip && \
    curl -s "https://get.sdkman.io" | bash && \
    bash -c "source /root/.sdkman/bin/sdkman-init.sh && \
        sdk install java 11.0.21-tem && \
        sdk install java 19.0.2-tem"

# Copy SDKMAN to jenkins user home
RUN mkdir -p /var/jenkins_home/.sdkman && \
    cp -r /root/.sdkman/* /var/jenkins_home/.sdkman/ && \
    chown -R jenkins:jenkins /var/jenkins_home/.sdkman

# Install Maven
RUN mkdir -p /opt/maven && \
    curl -fsSL https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz | tar -xz -C /opt/maven && \
    chown -R jenkins:jenkins /opt/maven


# Go back to jenkins user before Jenkins starts
USER jenkins

RUN mkdir -p /var/jenkins_home/artifacts 
RUN mkdir -p /var/jenkins_home/scm_credentials
RUN mkdir -p /var/jenkins_home/job_descriptions


COPY ./assets/jenkins-cli.jar /var/jenkins_home/

# Copy Jenkins plugins
COPY --chown=jenkins:jenkins ./plugins.txt /usr/share/jenkins/plugins.txt
RUN jenkins-plugin-cli -f /usr/share/jenkins/plugins.txt  

# Disable setup wizard and set credentials (for automation/dev)
ENV JENKINS_USER=admin
ENV JENKINS_PASS=admin
ENV JENKINS_URL=http://csura.jenkins.com
ENV JAVA_OPTS=-Djenkins.install.runSetupWizard=false

# Copy init scripts and job descriptions
COPY scripts/*.groovy /usr/share/jenkins/ref/init.groovy.d/
COPY jobs/*.xml /var/jenkins_home/job_descriptions/

# Set SDKMAN and Maven in PATH
ENV SDKMAN_DIR=/var/jenkins_home/.sdkman
ENV MAVEN_HOME=/opt/maven/apache-maven-3.9.6
ENV PATH="$MAVEN_HOME/bin:$PATH"



ENTRYPOINT ["/usr/local/bin/jenkins.sh"]

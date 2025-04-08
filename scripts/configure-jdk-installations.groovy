import jenkins.model.*
import hudson.model.*
import hudson.tools.*
import hudson.tasks.Maven

def descriptor = Jenkins.instance.getDescriptor("hudson.model.JDK")
def installations = []

def jdks = [
    [name: "JDK 19", home: "/var/jenkins_home/.sdkman/candidates/java/19.0.2-tem"],
    [name: "JDK 11", home: "/var/jenkins_home/.sdkman/candidates/java/11.0.21-tem"],
]

jdks.each { jdk ->
    def installer = new JDK(jdk.name, jdk.home)
    installations << installer
}

descriptor.setInstallations(installations.toArray(new JDK[0]))
descriptor.save()

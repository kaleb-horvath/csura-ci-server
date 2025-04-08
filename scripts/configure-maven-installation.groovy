import jenkins.model.*
import hudson.tasks.Maven


def desc = Jenkins.instance.getDescriptorByType(Maven.DescriptorImpl)

def mavenInstall = new Maven.MavenInstallation(
    "Maven 3.9.6",
    "/opt/maven/apache-maven-3.9.6",
    []
)

desc.setInstallations(mavenInstall)
desc.save()

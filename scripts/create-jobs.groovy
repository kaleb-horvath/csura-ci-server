import jenkins.model.*
import hudson.model.*
import java.io.*


def jobsDirectory = '/var/jenkins_home/job_descriptions/'
def jenkinsInstance = Jenkins.instance 

new File(jobsDirectory).eachFile { file -> 
    if (file.name.endsWith('xml')) {
        def jobName = file.name.replace('.xml', '')
        println("Processing job description: $jobName")

        def xmlInputStream = new FileInputStream(file)
        def existingJob = jenkinsInstance.getItem(jobName)
        if (existingJob != null) {
            println("Job '$jobName' already exists, skipping configuration.")
        } else {
            try {
                jenkinsInstance.createProjectFromXML(jobName, xmlInputStream)
                println("Job '$jobName' created successfully.")
            } catch (IOException e) {
                println("Failed to create job '$jobName': ${e.message}")
            } finally {
                xmlInputStream.close()
            }
        }
    }
}


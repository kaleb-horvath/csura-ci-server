import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths

def jenkinsUrl = System.getenv('JENKINS_SERVER_URL')
def jenkinsPort = System.getenv('JENKINS_SERVER_PORT')
def jenkinsEndpoint = "${jenkinsUrl}:${jenkinsPort}"

def downloadPath = "/var/jenkins_home"

if (new File("${downloadPath}/jenkins-cli.jar").exists()) {
    println "jenkins-cli.jar already exists at ${downloadPath}, skipping download."
} else {
    try {
        def url = new URL("${jenkinsEndpoint}/jnlpJars/jenkins-cli.jar")
        def connection = url.openConnection()
        connection.setRequestMethod("HEAD")
        if (connection.responseCode == 200) {
            println "Downloading jenkins-cli.jar from ${jenkinsEndpoint} to ${downloadPath}..."
            
            def inputStream = url.openStream()
            def outputPath = Paths.get("${downloadPath}/jenkins-cli.jar")
            Files.copy(inputStream, outputPath)
            inputStream.close()
        } else {
            println "Failed to reach Jenkins service at ${jenkinsEndpoint}, response code: ${connection.responseCode}."
        }
    } catch (Exception e) {
        println "Error while downloading jenkins-cli.jar: ${e.message}"
    }
}


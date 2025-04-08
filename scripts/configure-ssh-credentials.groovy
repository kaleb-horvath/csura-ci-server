import jenkins.model.Jenkins
import com.cloudbees.plugins.credentials.domains.Domain
import com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey
import com.cloudbees.plugins.credentials.CredentialsScope
import com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey.DirectEntryPrivateKeySource
import hudson.plugins.sshslaves.*

// Path to the SSH private key file
def privateKeyPath = '/var/jenkins_home/scm_credentials/id_rsa'

// Read the private key content
def privateKeyContent = new File(privateKeyPath).text

// Define the credentials ID and description
def credentialId = 'jenkins-scm-identity'
def description = 'SSH Key for GitHub Access'

// Create the SSH key credential
def privateKeySource = new DirectEntryPrivateKeySource(privateKeyContent) // Use the correct source type for private key content

def sshCredential = new BasicSSHUserPrivateKey(
    CredentialsScope.GLOBAL,
    credentialId,
    'kaleb-horvath', // Username for the SSH connection
    privateKeySource,  // Pass the private key wrapped as a PrivateKeySource
    null,              // Passphrase if any (null if none)
    description
)

// Get Jenkins credentials provider and add the credential
def credentialsProvider = Jenkins.instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0]
credentialsProvider.store.addCredentials(
    com.cloudbees.plugins.credentials.domains.Domain.global(), sshCredential
)

// Print confirmation
println "SSH Key Credential with ID '${credentialId}' has been added to Jenkins."

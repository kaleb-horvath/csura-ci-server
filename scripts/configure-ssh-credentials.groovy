import jenkins.model.Jenkins
import com.cloudbees.plugins.credentials.domains.Domain
import com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey
import com.cloudbees.plugins.credentials.CredentialsScope
import com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey.DirectEntryPrivateKeySource
import hudson.plugins.sshslaves.*

def privateKeyPath = System.getenv('JENKINS_SERVER_SCM_CREDENTIALS_PRIVATE_KEY')
def privateKeyContent = new File(privateKeyPath).text

def credentialId = System.getenv('JENKINS_SERVER_SCM_CREDENTIALS_ID')
def description = 'SSH Key for remote VCS provider access'

def username = System.getenv('JENKINS_SERVER_SCM_USERNAME')


def privateKeySource = new DirectEntryPrivateKeySource(privateKeyContent)

def sshCredential = new BasicSSHUserPrivateKey(
    CredentialsScope.GLOBAL,
    credentialId,
    username, 
    privateKeySource,  
    null,              // passphrase (none for now)
    description
)

// Get Jenkins credentials provider and add the credential
def credentialsProvider = Jenkins.instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0]
credentialsProvider.store.addCredentials(
    com.cloudbees.plugins.credentials.domains.Domain.global(), sshCredential
)

// Print confirmation
println "SSH Key Credential with ID '${credentialId}' has been added to Jenkins."

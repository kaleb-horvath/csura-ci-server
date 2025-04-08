#!/bin/bash


if [ "$#" -lt 5 ]; then
  echo "Usage: $0 <volume_name> <image_tag> <container_name> <env_file> <scm_rsa_creds_path>"
  echo "Example: $0 my_volume my_image_tag my_container_name /path/to/env_file /path/to/ssh/creds"
  exit 1
fi


VOLUME_NAME="$1"            # Docker volume name
IMAGE_TAG="$2"              # Docker image tag to use
CONTAINER_NAME="$3"         # Docker container name
ENV_FILE="$4"               # Path to the environment file
SCM_CREDENTIALS="$5"        # Path to SSH credentials for SCM


if docker volume ls --format "{{.Name}}" | grep -q "^${VOLUME_NAME}$"; then
  echo "Docker volume '${VOLUME_NAME}' exists, skipping creation."
else
  echo "Docker volume '${VOLUME_NAME}' does not exist. Creating it now..."
  docker volume create "${VOLUME_NAME}"
fi


echo "Building Docker image '${IMAGE_TAG}'..."
cd /tmp && docker build --no-cache -t "${IMAGE_TAG}" .


echo "Running Docker container '${CONTAINER_NAME}'..."
docker run -d \
  -v "${VOLUME_NAME}:/var/jenkins_home" \
  --name "${CONTAINER_NAME}" \
  --env-file "${ENV_FILE}" \
  "${IMAGE_TAG}"

docker cp ${SCM_CREDENTIALS}/id_rsa ${CONTAINER_NAME}:/var/jenkins_home/scm_credentials/id_rsa
docker cp ${SCM_CREDENTIALS}/id_rsa.pub ${CONTAINER_NAME}:/var/jenkins_home/scm_credentials/id_rsa.pub

# run SSH credentials configuration manually
docker exec -it ${CONTAINER_NAME} \
    java -jar /var/jenkins_home/jenkins-cli.jar -s http://localhost:8080 -auth admin:admin \
        groovy = < /var/jenkins_home/init.groovy.d/configure-ssh-credentials.groovy

# borrowed from popularowl@github.com
echo "Installing UFW (Uncomplicated Firewall)..."
apt -y install ufw 


echo "Configuring UFW firewall..."
ufw status verbose 
ufw default deny incoming 
ufw default allow outgoing 
ufw allow ssh 
ufw allow 22 
ufw allow 80 
yes | ufw enable 

# (Optional) Nginx configuration (commented out)
# echo "Configuring Nginx..."
# rm -f /etc/nginx/sites-enabled/default
# cp -f /tmp/jenkins-proxy /etc/nginx/sites-enabled
# service nginx restart

echo "Script completed successfully!"

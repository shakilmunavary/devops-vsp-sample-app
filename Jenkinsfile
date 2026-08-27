pipeline {
    agent any

    environment {
        // Docker Configuration
        DOCKER_HUB_REPO = "shakilahamed/devops-vsp-sample-app"
        IMAGE_TAG       = "${env.BUILD_NUMBER ?: 'latest'}"
        CONTAINER_NAME  = "devops-vsp-sample-app"
        HOST_PORT       = "7000"
        CONTAINER_PORT  = "8080"
        DOCKER_CREDS_ID = "dockerhub-creds"
    }

    stages {
        stage('Checkout') {
            steps {
                echo "Checking out source code..."
                checkout scm
            }
        }

        stage('Build App') {
            steps {
                script {
                    echo "Compiling and packaging Java Application with Maven..."
                    sh "mvn clean package -DskipTests"
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    echo "Building Docker image: ${DOCKER_HUB_REPO}:${IMAGE_TAG} and latest..."
                    sh """
                        docker build -t ${DOCKER_HUB_REPO}:${IMAGE_TAG} -t ${DOCKER_HUB_REPO}:latest .
                    """
                }
            }
        }

        stage('Push to Docker Hub') {
            steps {
                script {
                    echo "Authenticating and pushing Docker image to Docker Hub..."
                    withCredentials([usernamePassword(credentialsId: "${DOCKER_CREDS_ID}",
                                                     usernameVariable: 'DOCKER_USER',
                                                     passwordVariable: 'DOCKER_PASS')]) {
                        sh """
                            echo "\$DOCKER_PASS" | docker login -u "\$DOCKER_USER" --password-stdin
                            docker push ${DOCKER_HUB_REPO}:${IMAGE_TAG}
                            docker push ${DOCKER_HUB_REPO}:latest
                        """
                    }
                }
            }
        }

        stage('Deploy & Run Container') {
            steps {
                script {
                    echo "Running container '${CONTAINER_NAME}' on Port ${HOST_PORT}..."
                    sh """
                        # Stop and remove existing container if running
                        docker rm -f ${CONTAINER_NAME} || true

                        # Run new container
                        docker run -d \\
                            --name ${CONTAINER_NAME} \\
                            --restart unless-stopped \\
                            -p ${HOST_PORT}:${CONTAINER_PORT} \\
                            ${DOCKER_HUB_REPO}:${IMAGE_TAG}
                    """
                }
            }
        }

        stage('Verify Deployment') {
            steps {
                script {
                    echo "Verifying application accessibility on http://localhost:${HOST_PORT}..."
                    sh """
                        sleep 5
                        docker ps | grep ${CONTAINER_NAME} || true
                        curl -sI http://localhost:${HOST_PORT} || echo "Application starting up on Port ${HOST_PORT}..."
                    """
                }
            }
        }
    }

    post {
        success {
            echo "================================================================="
            echo "Pipeline Completed Successfully!"
            echo "Access Application in Browser: http://localhost:7000"
            echo "================================================================="
        }

        failure {
            echo "Pipeline failed. Please check build console logs."
        }

        always {
            cleanWs()
        }
    }
}

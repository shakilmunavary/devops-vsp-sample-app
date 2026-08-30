pipeline {
    agent any

    environment {
        // Docker Configuration
        DOCKER_HUB_REPO = "shakilahamed/devops-vsp-sample-app"
        IMAGE_TAG       = "${env.BUILD_NUMBER ?: 'latest'}"
        CONTAINER_NAME  = "devops-vsp-sample-app"
        HOST_PORT       = "7000"
        DOCKER_CREDS_ID = "dockerhub-creds"
        GITHUB_CREDS_ID = "github-token"
    }

    stages {
        stage('Checkout') {
            steps {
                echo "📥 Checking out source code..."
                checkout scm
            }
        }

        stage('Ensure Docker Service') {
            steps {
                script {
                    echo "🐳 Verifying Docker daemon is active..."
                    sh """
                        if ! docker info >/dev/null 2>&1; then
                            echo "⚠️ Docker daemon is not running. Starting Docker service..."
                            sudo service docker start || true
                            sleep 3
                        fi
                        docker info >/dev/null 2>&1 || (echo "❌ Could not start Docker daemon"; exit 1)
                        echo "✅ Docker daemon is active and ready."
                    """
                }
            }
        }

        stage('Build App') {
            steps {
                script {
                    echo "⚙️ Compiling and packaging Java Application with Maven..."
                    sh "mvn clean package -DskipTests"
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    echo "🔨 Building Docker image: ${DOCKER_HUB_REPO}:${IMAGE_TAG} and latest..."
                    sh """
                        docker build -t ${DOCKER_HUB_REPO}:${IMAGE_TAG} -t ${DOCKER_HUB_REPO}:latest .
                    """
                }
            }
        }

        stage('Push to Docker Hub') {
            steps {
                script {
                    echo "📦 Authenticating and pushing Docker image to Docker Hub..."
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
                    echo "🚀 Running container '${CONTAINER_NAME}' with latest image on Port ${HOST_PORT}..."
                    withCredentials([string(credentialsId: "${GITHUB_CREDS_ID}", variable: 'APP_GITHUB_TOKEN')]) {
                        sh """
                            # Stop and remove existing container if running
                            docker rm -f ${CONTAINER_NAME} || true

                            # Run container with host networking and inject GitHub token
                            docker run -d \\
                                --name ${CONTAINER_NAME} \\
                                --restart unless-stopped \\
                                --net=host \\
                                -e GITHUB_TOKEN="\$APP_GITHUB_TOKEN" \\
                                ${DOCKER_HUB_REPO}:latest
                        """
                    }
                }
            }
        }

        stage('Purge Old & Intermediate Images') {
            steps {
                script {
                    echo "🧹 Cleaning up local images to retain ONLY '${DOCKER_HUB_REPO}:latest'..."
                    sh """
                        # Untag the build-number tag so only :latest remains
                        if [ "${IMAGE_TAG}" != "latest" ]; then
                            docker rmi ${DOCKER_HUB_REPO}:${IMAGE_TAG} 2>/dev/null || true
                        fi

                        # Remove base builder image (eclipse-temurin)
                        docker rmi eclipse-temurin:17-jdk-alpine 2>/dev/null || true

                        # Prune any dangling and untagged intermediate layers
                        docker image prune -f || true
                    """
                }
            }
        }

        stage('Verify Deployment') {
            steps {
                script {
                    echo "🔍 Verifying application accessibility on http://localhost:${HOST_PORT}..."
                    sh """
                        sleep 8
                        docker ps | grep ${CONTAINER_NAME} || true
                        curl -sI http://localhost:${HOST_PORT}/ping || echo "Application ping endpoint ready..."
                        curl -sI http://localhost:${HOST_PORT}/dashboard || echo "Application dashboard ready..."
                    """
                }
            }
        }
    }

    post {
        success {
            echo "================================================================="
            echo "🎉 Pipeline Completed Successfully!"
            echo "🌐 Access Application in Browser: http://localhost:7000/dashboard"
            echo "================================================================="
        }

        failure {
            echo "❌ Pipeline failed. Please check build console logs."
        }

        always {
            sh "docker image prune -f || true"
            cleanWs()
        }
    }
}

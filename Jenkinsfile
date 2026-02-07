pipeline {
    agent any

    tools {
        maven 'M3'
        jdk 'JDK21'
    }

    environment {
        // Name of the Docker image we will build
        IMAGE_NAME = "budget-buddy:latest"
        // The container name to run
        CONTAINER_NAME = "budget-backend"
    }

    options {
        disableConcurrentBuilds()
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build JAR') {
            steps {
                echo '🚀 Compiling Application...'
                // Skip tests here if you want faster deploys, or keep them for safety
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Build Image') {
            steps {
                echo '🐳 Building Docker Image...'
                // Uses the Dockerfile we created (copying the jar)
                sh "docker build -t ${IMAGE_NAME} ."
            }
        }

        stage('Deploy') {
            steps {
                echo '🚀 Deploying to Production...'
                script {
                    // 1. Stop old container (ignore failure if it doesn't exist)
                    try {
                        sh "docker stop ${CONTAINER_NAME}"
                        sh "docker rm ${CONTAINER_NAME}"
                    } catch (Exception e) {
                        echo 'No existing container to stop.'
                    }

                    // 2. Run new container
                    // We map port 8080 on Host to 8080 in Container
                    // We inject the production secrets from your VPS file
                    sh """
                        docker run -d \
                        --name ${CONTAINER_NAME} \
                        --restart always \
                        -p 8080:8080 \
                        --env-file /opt/budget-buddy/prod.env \
                        ${IMAGE_NAME}
                    """
                }
            }
        }
    }
}
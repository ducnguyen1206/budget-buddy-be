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
                        withCredentials([file(credentialsId: 'budget-prod-env', variable: 'SECRET_ENV')]) {
                            script {
                                try {
                                    sh "docker stop ${CONTAINER_NAME}"
                                    sh "docker rm ${CONTAINER_NAME}"
                                } catch (Exception e) {
                                    echo 'No existing container to stop.'
                                }

                                // Updated for HOST NETWORKING
                                // 1. --network="host" lets us talk to localhost DB
                                // 2. We OVERRIDE the DB_URL from the file using -e flags
                                sh """
                                    docker run -d \
                                    --name ${CONTAINER_NAME} \
                                    --restart always \
                                    --network="host" \
                                    -e DB_URL=jdbc:postgresql://localhost:5432/budgetbuddy_db \
                                    -e REDIS_HOST=localhost \
                                    --env-file '${SECRET_ENV}' \
                                    ${IMAGE_NAME}
                                """
                            }
                        }
                    }
                }
    }
}
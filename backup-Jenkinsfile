pipeline {
    agent any

    tools {
        // We don't need to install tools here because we use Docker containers below
        jdk 'jdk-21' // Just a placeholder, we use the image below
    }

    environment {
        // Name of the image we will build
        IMAGE_NAME = "budget-backend"
        // The port your Spring Boot app runs on INSIDE the container
        CONTAINER_PORT = "8080"
        // The port you want to expose to the world (VPS Port)
        HOST_PORT = "8081"
    }

    stages {
        stage('Build & Test') {
                    steps {
                        script {
                            // ADDED: ("-u root") - This forces the container to run as root
                            // so it has permission to create the .m2 folder.
                            docker.image('maven:3.9.6-eclipse-temurin-21').inside("-u root") {
                                sh 'mvn clean package -DskipTests=false'
                            }
                        }
                    }
                }

        stage('Build Docker Image') {
            steps {
                script {
                    sh "docker build -t ${IMAGE_NAME} ."
                }
            }
        }

        stage('Deploy (Option B)') {
            steps {
                script {
                    // 1. Stop the old container (if running)
                    sh "docker stop ${IMAGE_NAME} || true"

                    // 2. Remove the old container
                    sh "docker rm ${IMAGE_NAME} || true"

                    // 3. Run the new one
                    // --network="host" lets the app talk to Postgres on localhost:5432
//                  sh "docker run -d --name ${IMAGE_NAME} --restart unless-stopped --network=\"host\" ${IMAGE_NAME}"
                    // We add '--server.port=8081' so it doesn't clash with your Bare Metal app on 8080
                    sh "docker run -d --name ${IMAGE_NAME} --restart unless-stopped --network=\"host\" -e SERVER_PORT=8081 ${IMAGE_NAME}"
                }
            }
        }
    }
}
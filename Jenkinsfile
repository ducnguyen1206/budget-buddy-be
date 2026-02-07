pipeline {
    agent any
    tools {
        maven 'M3'
        jdk 'JDK21'
    }
    environment {
        IMAGE_NAME = "budget-buddy:latest"
        CONTAINER_NAME = "budget-backend"
    }
    options { disableConcurrentBuilds() }

    stages {
        stage('Checkout') {
            steps { checkout scm }
        }

        // ✅ ALWAYS RUN: This verifies your PR code is good
        stage('Build & Test') {
            steps {
                echo '🚀 Compiling and Testing...'
                // Remove -DskipTests if you want to run unit tests on PRs (Recommended)
                sh 'mvn clean package'
            }
        }

        // 🛑 ONLY RUN ON MAIN: Docker Build
        stage('Build Image') {
            when {
                branch 'main' // <--- THE MAGIC LINE
            }
            steps {
                echo '🐳 Building Docker Image (Main Branch Only)...'
                sh "docker build -t ${IMAGE_NAME} ."
            }
        }

        // 🛑 ONLY RUN ON MAIN: Deploy
        stage('Deploy') {
            when {
                branch 'main' // <--- THE MAGIC LINE
            }
            steps {
                echo '🚀 Deploying to Production...'
                withCredentials([file(credentialsId: 'budget-prod-env', variable: 'SECRET_ENV')]) {
                    script {
                        try {
                            sh "docker stop ${CONTAINER_NAME}"
                            sh "docker rm ${CONTAINER_NAME}"
                        } catch (Exception e) { echo 'No container to stop.' }

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
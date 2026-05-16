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
                // Tests use H2 in-memory DB — no external services required
                sh 'mvn clean package'
            }
        }

        // 🛑 ONLY RUN ON MAIN: Docker Build
        stage('Build Image') {
            when {
                // BRANCH_NAME  → set by Multibranch Pipeline jobs
                // GIT_BRANCH   → set by regular Pipeline jobs (value is "origin/main")
                anyOf {
                    branch 'main'
                    expression { env.GIT_BRANCH == 'main' }
                    expression { env.GIT_BRANCH == 'origin/main' }
                }
            }
            steps {
                echo '🐳 Building Docker Image (Main Branch Only)...'
                sh "docker build -t ${IMAGE_NAME} ."
            }
        }

        // 🛑 ONLY RUN ON MAIN: Deploy
        stage('Deploy') {
            when {
                anyOf {
                    branch 'main'
                    expression { env.GIT_BRANCH == 'main' }
                    expression { env.GIT_BRANCH == 'origin/main' }
                }
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
                            --restart unless-stopped \
                            --network="host" \
                            -e SPRING_PROFILES_ACTIVE=prod \
                            --env-file '${SECRET_ENV}' \
                            ${IMAGE_NAME}
                        """
                    }
                }
            }
        }
    }
}
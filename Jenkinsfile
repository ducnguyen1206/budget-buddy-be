pipeline {
    agent any

    // Use the Maven tool we named 'M3' earlier
    tools {
        maven 'M3'
        // jdk 'Java21' // Uncomment this line if you named your JDK tool 'Java21'
        jdk 'JDK21'
    }

    options {
        // CRITICAL: Prevents multiple builds from killing your CPU
        disableConcurrentBuilds()
    }

    stages {
        stage('Checkout') {
            steps {
                // Gets code from the branch you configured (main)
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                echo '🚀 Starting Build and Unit Tests...'
                // -Xmx512m limits Maven to 512MB RAM so Jenkins doesn't crash
                // 'clean package' runs unit tests automatically
                sh 'export MAVEN_OPTS="-Xmx512m" && mvn clean package'
            }
        }
    }

    post {
        always {
            // Tells Jenkins to record the test results
            junit '**/target/surefire-reports/*.xml'
        }
    }
}
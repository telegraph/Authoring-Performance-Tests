pipeline {
    agent any

    tools {
        jdk 'JDK17'
    }

    environment {
        MVNW_ALIAS = "./mvnw --no-transfer-progress"
    }

    stages {
        stage("Build Maven") {
            steps {
                sh '$MVNW_ALIAS clean'
            }
        }
        stage("Run Gatling") {
            steps {
                sh '$MVNW_ALIAS gatling:test'
            }
            post {
                always {
                    gatlingArchive()
                }
            }
        }
    }
}
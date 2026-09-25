pipeline {
    agent any

    triggers {
            pollSCM('* * * * *')
    }

    environment {
        APPLICATION_NAME = 'mfscreener'
    }

    stages {
        stage('Build') {
            steps {
                sh './gradlew clean build'
            }
        }
    }
}

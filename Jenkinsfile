pipeline { 
    agent any
    tools { 
        maven 'Maven 3.3.9' 
        jdk 'jdk8' 
    }
    stages { 
        stage('Build') { 
            steps {
                git(url: "https://github.com/epics-base/epicsCoreJava.git")
                sh 'mvn clean install' 
            }
            post {
                always {
                    junit 'target/surefire-reports/**/*.xml' 
                }
            }
        }
    }
}

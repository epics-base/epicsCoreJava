pipeline { 
    agent any

    environment{
        MAVEN_OPTS="-Xmx2048m -Xms1024M -Xss128M -XX:-UseGCOverheadLimit"
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

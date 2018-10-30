pipeline { 
    agent any

    environment{
        MAVEN_OPTS="-Xmx2048m -Xms1024M -Xss128M -XX:-UseGCOverheadLimit"
    }
    
    stages { 
        stage('Build') { 
            steps {
                git(url: "https://github.com/epics-base/epicsCoreJava.git")
                sh 'git submodule update --recursive --remote'
                sh 'mvn clean install' 
            }
        }
    }
    post {
        always {
            archiveArtifacts artifacts: '**/*.jar', fingerprint: true
        }
    }
}

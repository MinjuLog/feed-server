pipeline {
    agent any

    environment {
        IMAGE = "jeongseho1/minjulog-feed:latest"
        REMOTE = "ubuntu@155.248.211.226"
        DEPLOY_DIR = "/home/uroi/minjulog"
    }

    stages {
        stage('도커 이미지 빌드') {
            steps {
                sh 'docker build -t $IMAGE .'
            }
        }

        stage('도커 이미지 푸시') {
            steps {
                withCredentials([
                  string(credentialsId: 'UROI_DOCKER_USERNAME', variable: 'DOCKER_ID'),
                  string(credentialsId: 'UROI_DOCKER_PASSWORD', variable: 'DOCKER_PW')
                ]) {
                  sh '''
                    echo "$DOCKER_PW" | docker login -u "$DOCKER_ID" --password-stdin
                    docker push jeongseho1/minjulog-feed:latest
                    docker logout
                  '''
                }
            }
        }

        stage('원격 서버 배포') {
            steps {
                sshagent(credentials: ['407c0ea1-1194-4188-9c3d-d7f6196497dd']) {
                    sh '''
                        ssh -o StrictHostKeyChecking=no $REMOTE "
                            set -e
                            cd $DEPLOY_DIR
                            sudo docker compose --env-file .env down
                            sudo docker compose --env-file .env pull
                            sudo docker compose --env-file .env up -d
                        "
                    '''
                }
            }
        }
    }
}
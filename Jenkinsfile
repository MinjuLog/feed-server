pipeline {
    agent any

    environment {
        IMAGE = "jeongseho1/minjulog-feed:latest"
        REMOTE = "uroi@155.248.211.226"
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
                  usernamePassword(
                    credentialsId: 'UROI_DOCKER',
                    usernameVariable: 'DOCKER_ID',
                    passwordVariable: 'DOCKER_PW'
                  )
                ]) {
                  sh '''
                    echo "$DOCKER_PW" | docker login -u "$DOCKER_ID" --password-stdin
                    docker push $IMAGE
                    docker logout
                  '''
                }
            }
        }

        stage('Feed 블루그린 배포') {
            steps {
                sshagent(credentials: ['UROI_SSH_KEY']) {
                    sh '''
                      ssh -o StrictHostKeyChecking=no uroi@155.248.211.226 <<'EOF'
                      set -e
                      cd /home/uroi/minjulog

                      # 최신 이미지 pull (inactive 쪽에서 사용)
                      docker compose pull feed-blue feed-green

                      # feed 블루그린 스위치
                      ./deploy-feed-bluegreen.sh
                      EOF
                    '''
                }
            }
        }
    }
}
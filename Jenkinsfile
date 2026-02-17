pipeline {
  agent any

  environment {
    REPO = "jeongseho1/minjulog-feed"
    REMOTE = "uroi@155.248.211.226"
    DEPLOY_DIR = "/home/uroi/minjulog"
  }

  stages {
    stage('Build tag 설정') {
      steps {
        script {
          def shortCommit = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
          env.BUILD_TAG = "build-${env.BUILD_NUMBER}-${shortCommit}"
          env.IMAGE = "${env.REPO}:${env.BUILD_TAG}"
        }
      }
    }

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

    stage('원격 서버 블루그린 배포') {
      steps {
        sshagent(credentials: ['UROI_SSH_KEY']) {
          sh """
            ssh -o StrictHostKeyChecking=no ${REMOTE} <<'EOF'
            set -e
            cd ${DEPLOY_DIR}
            ./deploy-feed-bluegreen.sh ${BUILD_TAG}
            EOF
          """
        }
      }
    }
  }
}
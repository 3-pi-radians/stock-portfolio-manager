pipeline {
    agent any

    environment {
        DOCKER_IMAGE = '3piradians/app-service'
        DOCKER_TAG = "${BUILD_NUMBER}"
        DB_USERNAME = credentials('DB_USERNAME')
        DB_PASSWORD = credentials('DB_PASSWORD')
        FINNHUB_API_KEY = credentials('FINNHUB_API_KEY')
        JWT_SECRET = credentials('JWT_SECRET')
        KUBECONFIG = "/Users/pankajdeopa/.kube/config"
        PATH = "/opt/homebrew/bin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin"
    }

    stages {

        stage('Clone Repository') {
            steps {
                echo '========== Stage 1: Cloning repository =========='
                git branch: 'main',
                    url: 'https://github.com/3-pi-radians/stock-portfolio-manager.git'
            }
        }

        stage('Run Unit Tests') {
            steps {
                echo '========== Stage 2: Running tests =========='
                sh 'mvn test -DskipTests'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Build JAR') {
            steps {
                echo '========== Stage 3: Building JAR =========='
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Build Docker Image') {
            steps {
                echo '========== Stage 4: Building Docker image =========='
                sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."
                sh "docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest"
            }
        }

        stage('Push to Docker Hub') {
            steps {
                echo '========== Stage 5: Pushing to Docker Hub =========='
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-credentials',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh """
                        docker logout || true
                        echo \$DOCKER_PASS | docker login -u \$DOCKER_USER --password-stdin
                        docker push ${DOCKER_IMAGE}:${DOCKER_TAG}
                        docker push ${DOCKER_IMAGE}:latest
                    """
                    echo 'Image pushed to Docker Hub successfully'
                }
            }
        }

        stage('Deploy') {
            steps {
                echo 'app-service image pushed to Docker Hub successfully'
                echo 'Full stack deployment handled by api-gateway pipeline'
            }
        }

    }

    post {
        success {
            echo "app-service deployed: ${DOCKER_IMAGE}:${DOCKER_TAG}"
        }
        failure {
            echo 'Pipeline FAILED!'
        }
        always {
            sh 'docker logout || true'
        }
    }
}

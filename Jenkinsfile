pipeline {
    agent any

    environment {
        DOCKER_IMAGE = '3piradians/stock-portfolio-manager'
        DOCKER_TAG = "${BUILD_NUMBER}"
        DB_USERNAME = credentials('DB_USERNAME')
        DB_PASSWORD = credentials('DB_PASSWORD')
        FINNHUB_API_KEY = credentials('FINNHUB_API_KEY')
        JWT_SECRET = credentials('JWT_SECRET')
        KUBECONFIG = "/Users/pankajdeopa/.kube/config"
    }

    stages {

        stage('Clone Repository') {
            steps {
                echo '========== Stage 1: Cloning repository =========='
                git branch: 'main',
                    url: 'https://github.com/3-pi-radians/stock-portfolio-manager.git'
                echo 'Repository cloned successfully'
            }
        }

        stage('Run Unit Tests') {
            steps {
                echo '========== Stage 2: Running JUnit tests =========='
                sh 'mvn test'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
                success {
                    echo 'All tests passed!'
                }
                failure {
                    echo 'Tests failed! Stopping pipeline.'
                }
            }
        }

        stage('Build JAR') {
            steps {
                echo '========== Stage 3: Building JAR with Maven =========='
                sh 'mvn clean package -DskipTests'
                echo 'JAR built successfully'
            }
        }

        stage('Build Docker Image') {
            steps {
                echo '========== Stage 4: Building Docker image =========='
                sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."
                sh "docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest"
                echo "Docker image built: ${DOCKER_IMAGE}:${DOCKER_TAG}"
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
                    sh "echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin"
                    sh "docker push ${DOCKER_IMAGE}:${DOCKER_TAG}"
                    sh "docker push ${DOCKER_IMAGE}:latest"
                    echo 'Image pushed to Docker Hub successfully'
                }
            }
        }

        stage('Deploy with Ansible') {
            steps {
                echo '========== Stage 6: Deploying via Ansible =========='
                sh """
                    ansible-playbook -i ansible/inventory/hosts.ini \
                    ansible/playbooks/deploy.yml \
                    --extra-vars "image_tag=${DOCKER_TAG} \
                                 db_username=${DB_USERNAME} \
                                 db_password=${DB_PASSWORD} \
                                 jwt_secret=${JWT_SECRET} \
                                 finnhub_api_key=${FINNHUB_API_KEY}"
                """
            }
        }

    }

    post {
        success {
            echo '========================================='
            echo 'Pipeline completed successfully!'
            echo "Image deployed: ${DOCKER_IMAGE}:${DOCKER_TAG}"
            echo '========================================='
        }
        failure {
            echo '========================================='
            echo 'Pipeline FAILED! Check logs above.'
            echo '========================================='
        }
        always {
            sh 'docker logout'
        }
    }
}

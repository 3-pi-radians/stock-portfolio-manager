pipeline {
    agent any

    environment {
        DOCKER_IMAGE = '3piradians/api-gateway'
        DOCKER_TAG = "${BUILD_NUMBER}"
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
                dir('api-gateway') {
                    sh 'mvn test -DskipTests'
                }
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/api-gateway/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Build JAR') {
            steps {
                echo '========== Stage 3: Building JAR =========='
                dir('api-gateway') {
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                echo '========== Stage 4: Building Docker image =========='
                sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} api-gateway/"
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
                        mkdir -p /tmp/docker-config-${BUILD_NUMBER}
                        echo '{"auths":{"https://index.docker.io/v1/":{"auth":"'"$(echo -n '${DOCKER_USER}:${DOCKER_PASS}' | base64)"'"}}}' > /tmp/docker-config-${BUILD_NUMBER}/config.json
                        docker --config /tmp/docker-config-${BUILD_NUMBER} push ${DOCKER_IMAGE}:${DOCKER_TAG}
                        docker --config /tmp/docker-config-${BUILD_NUMBER} push ${DOCKER_IMAGE}:latest
                        rm -rf /tmp/docker-config-${BUILD_NUMBER}
                    """
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
                                 deploy_service=api-gateway"
                """
            }
        }

    }

    post {
        success {
            echo "api-gateway deployed: ${DOCKER_IMAGE}:${DOCKER_TAG}"
        }
        failure {
            echo 'Pipeline FAILED!'
        }
        always {
            sh 'docker logout || true'
        }
    }
}

pipeline {
    agent any

    tools {
        maven 'Maven3'
    }

    environment {
        SONAR_TOKEN = credentials('sonar-token')
        DOCKER_HUB_USER = 'yassmine0806'
        DOCKER_IMAGE_BACKEND = "${DOCKER_HUB_USER}/admin-backend"
        DOCKER_TAG = "${BUILD_NUMBER}"
        MONGODB_URI   = credentials('backend-mongodb-uri')
        JWT_SECRET    = credentials('backend-jwt-secret')
        MAIL_USERNAME = credentials('backend-mail-username')
        MAIL_PASSWORD = credentials('backend-mail-password')
    }

    stages {

        stage('Checkout') {
            steps {
                echo '=== Récupération du code source ==='
                checkout scm
            }
        }

        stage('Préparer la configuration') {
            steps {
                echo '=== Injection de application.properties depuis Jenkins Credentials ==='
                withCredentials([file(credentialsId: 'backend-application-properties', variable: 'APP_PROPS_FILE')]) {
                    sh 'cp "$APP_PROPS_FILE" src/main/resources/application.properties'
                }
            }
        }

        stage('Installation des dépendances') {
            steps {
                echo '=== Installation Maven ==='
                sh 'mvn dependency:resolve'
            }
        }

        stage('Préparer la config de test') {
            steps {
                echo '=== Injection de application-test.properties depuis Jenkins Credentials ==='
                withCredentials([file(credentialsId: 'backend-application-test-properties', variable: 'TEST_PROPS_FILE')]) {
                    sh 'cp "$TEST_PROPS_FILE" src/test/resources/application-test.properties'
                }
            }
        }

        stage('Tests unitaires') {
            steps {
                echo '=== Lancement des tests ==='
                sh 'mvn test -Dspring.profiles.active=test'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Analyse SonarQube') {
            steps {
                echo '=== Analyse qualité du code ==='
                withSonarQubeEnv('SonarQube') {
                    sh '''
                        export MAVEN_OPTS="-Xmx512m -XX:MaxMetaspaceSize=256m"
                        mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Dsonar.projectKey=Admin-Backend -Dsonar.host.url=http://sonarqube:9000 -Dsonar.token=$SONAR_TOKEN
                    '''
                }
            }
        }

        stage('Build') {
            steps {
                echo '=== Build Maven (génération du .jar) ==='
                sh 'mvn clean package -DskipTests'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('Build & Push Docker Image') {
            steps {
                echo '=== Construction et Push vers Docker Hub ==='
                withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials', passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
                    sh """
                        echo "\${DOCKER_PASSWORD}" | docker login -u "\${DOCKER_USERNAME}" --password-stdin
                        docker build -t ${DOCKER_IMAGE_BACKEND}:${DOCKER_TAG} .
                        docker tag ${DOCKER_IMAGE_BACKEND}:${DOCKER_TAG} ${DOCKER_IMAGE_BACKEND}:latest
                        docker push ${DOCKER_IMAGE_BACKEND}:${DOCKER_TAG}
                        docker push ${DOCKER_IMAGE_BACKEND}:latest
                    """
                }
            }
        }

        stage('Déploiement (Dev local)') {
            when { branch 'develop' }
            steps {
                echo '=== Déploiement automatique sur environnement Dev local ==='
                sh """
                    docker stop admin_backend || true
                    docker rm admin_backend || true
                    docker run -d \
                        --name admin_backend \
                        --network administration_default \
                        -p 8083:8083 \
                        -v admin_uploads:/app/uploads \
                        -e SPRING_DATA_MONGODB_URI="${MONGODB_URI}" \
                        -e JWT_SECRET="${JWT_SECRET}" \
                        -e JWT_EXPIRATION="86400000" \
                        -e SERVER_PORT="8083" \
                        -e FILE_UPLOAD_DIR="/app/uploads" \
                        -e APP_BASE_URL="http://localhost:8083" \
                        -e APP_FRONTEND_URL="http://localhost:4200" \
                        -e SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE="50MB" \
                        -e SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE="50MB" \
                        -e SERVER_TOMCAT_MAX_SWALLOW_SIZE="50MB" \
                        -e MAIL_USERNAME="${MAIL_USERNAME}" \
                        -e MAIL_PASSWORD="${MAIL_PASSWORD}" \
                        --restart unless-stopped \
                        ${DOCKER_IMAGE_BACKEND}:latest
                """
            }
        }

        stage('Déployer sur Staging') {
            when { branch 'staging' }
            steps {
                echo '=== Déploiement automatique sur rocky-staging (192.168.72.20) ==='
                withCredentials([sshUserPrivateKey(credentialsId: 'ssh-staging-prod-key', keyFileVariable: 'SSH_KEY', usernameVariable: 'SSH_USER')]) {
                    sh """
                        ssh -i \$SSH_KEY -o StrictHostKeyChecking=no -p 2222 \$SSH_USER@192.168.72.20 'cd ~/staging-app && docker compose pull backend && docker compose up -d backend'
                    """
                }
            }
        }



        stage('Déployer sur Production') {
            when { branch 'main' }
            steps {
                echo '=== Déploiement automatique sur prod-server (192.168.72.40) ==='
                withCredentials([sshUserPrivateKey(credentialsId: 'ssh-staging-prod-key', keyFileVariable: 'SSH_KEY', usernameVariable: 'SSH_USER')]) {
                    sh """
                        ssh -i \$SSH_KEY -o StrictHostKeyChecking=no \$SSH_USER@192.168.72.40 'cd ~/prod-app && docker compose pull backend && docker compose up -d backend'
                    """
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline terminé avec succès !'
        }
        failure {
            echo 'Pipeline échoué !'
        }
        always {
            echo 'Nettoyage...'
            cleanWs()
        }
    }
}
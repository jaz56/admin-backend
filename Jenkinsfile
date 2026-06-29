pipeline {
    agent any

    tools {
        maven 'Maven3'
    }

    environment {
        SONAR_TOKEN = credentials('sonar-token')
        DOCKER_IMAGE_BACKEND = 'admin-backend'
        DOCKER_TAG = "${BUILD_NUMBER}"
    }

    stages {

        stage('📥 Checkout') {
            steps {
                echo '=== Récupération du code source ==='
                checkout scm
            }
        }

        stage('📦 Installation des dépendances') {
            steps {
                echo '=== Installation Maven ==='
                sh 'mvn dependency:resolve'
            }
        }

       stage('🧪 Tests unitaires') {
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

       stage('🔍 Analyse SonarQube') {
           steps {
               echo '=== Analyse qualité du code ==='
               withSonarQubeEnv('SonarQube') {
                   sh """
                       mvn sonar:sonar \
                           -Dsonar.projectKey=Admin-Backend \
                           -Dsonar.host.url=http://sonarqube:9000 \
                           -Dsonar.token=${SONAR_TOKEN}
                   """
               }
           }
       }



        stage('🔨 Build') {
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

        stage('🐳 Build Docker Image') {
            steps {
                echo '=== Construction de l image Docker ==='
                sh """
                    docker build -t ${DOCKER_IMAGE_BACKEND}:${DOCKER_TAG} .
                    docker tag ${DOCKER_IMAGE_BACKEND}:${DOCKER_TAG} ${DOCKER_IMAGE_BACKEND}:latest
                """
            }
        }

       stage('🚀 Déploiement') {
           steps {
               echo '=== Déploiement avec Docker ==='
               sh '''
                   docker stop admin_backend || true
                   docker rm admin_backend || true
                   docker run -d \
                       --name admin_backend \
                       --network administration_default \
                       -p 8083:8083 \
                       -e SPRING_DATA_MONGODB_URI="mongodb://adminUser:08062003@admin_mongodb:27017/admin_db?authSource=admin" \
                       -e JWT_SECRET="Y2Q4OWI0M2VlZjZkNTk3YjRlZGNhYzc2MjhkYjhmZTU0ZDVhMzE0ZTJiMjc4NmViNDFiYWVjODNiNmEyZWY5YQ==" \
                       -e JWT_EXPIRATION="86400000" \
                       -e SERVER_PORT="8083" \
                       -e FILE_UPLOAD_DIR="/app/uploads" \
                       -e APP_BASE_URL="http://localhost:8083" \
                       --restart unless-stopped \
                       admin-backend:latest
               '''
           }
       }
    }

    post {
        success {
            echo '✅ Pipeline terminé avec succès !'
        }
        failure {
            echo '❌ Pipeline échoué !'
        }
        always {
            echo '🧹 Nettoyage...'
            cleanWs()
        }
    }
}
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

        stage('⏳ Quality Gate') {
            steps {
                echo '=== Vérification du Quality Gate SonarQube ==='
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
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
                echo '=== Déploiement avec Docker Compose ==='
                sh """
                    docker-compose down --remove-orphans || true
                    docker-compose up -d --build
                """
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
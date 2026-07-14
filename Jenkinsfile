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
                          // 1. On utilise les simples guillemets (''') pour éviter l'avertissement de sécurité de Jenkins
                          // 2. On limite la mémoire allouée au scanner SonarQube avec MAVEN_OPTS
                          sh '''
                              export MAVEN_OPTS="-Xmx512m -XX:MaxMetaspaceSize=256m"
                              mvn sonar:sonar \
                                  -Dsonar.projectKey=Admin-Backend \
                                  -Dsonar.host.url=http://sonarqube:9000 \
                                  -Dsonar.token=$SONAR_TOKEN
                          '''
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
        stage('🐳 Build & Push Docker Image') {
                    steps {
                        echo '=== Construction et Push vers Docker Hub ==='

                        // On récupère les identifiants Docker Hub configurés dans Jenkins
                        withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials', passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
                            sh """
                                # 1. Connexion sécurisée à Docker Hub
                                echo "\${DOCKER_PASSWORD}" | docker login -u "\${DOCKER_USERNAME}" --password-stdin

                                # 2. Construction de l'image avec votre préfixe Docker Hub
                                docker build -t ${DOCKER_IMAGE_BACKEND}:${DOCKER_TAG} .
                                docker tag ${DOCKER_IMAGE_BACKEND}:${DOCKER_TAG} ${DOCKER_IMAGE_BACKEND}:latest

                                # 3. Envoi (Push) vers Docker Hub
                                docker push ${DOCKER_IMAGE_BACKEND}:${DOCKER_TAG}
                                docker push ${DOCKER_IMAGE_BACKEND}:latest
                            """
                        }
                    }
                }

      stage('🚀 Déploiement') {
                  steps {
                      echo '=== Déploiement avec Docker ==='
                      // Remarquez que l'on utilise ${DOCKER_IMAGE_BACKEND} à la fin au lieu de "admin-backend:latest"
                      sh """
                          docker stop admin_backend || true
                          docker rm admin_backend || true
                          docker run -d \
                              --name admin_backend \
                              --network administration_default \
                              -p 8083:8083 \
                              -v admin_uploads:/app/uploads \
                              -e SPRING_DATA_MONGODB_URI="mongodb://adminUser:08062003@admin_mongodb:27017/admin_db?authSource=admin" \
                              -e JWT_SECRET="Y2Q4OWI0M2VlZjZkNTk3YjRlZGNhYzc2MjhkYjhmZTU0ZDVhMzE0ZTJiMjc4NmViNDFiYWVjODNiNmEyZWY5YQ==" \
                              -e JWT_EXPIRATION="86400000" \
                              -e SERVER_PORT="8083" \
                              -e FILE_UPLOAD_DIR="/app/uploads" \
                              -e APP_BASE_URL="http://localhost:8083" \
                              -e SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE="50MB" \
                              -e SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE="50MB" \
                              -e SERVER_TOMCAT_MAX_SWALLOW_SIZE="50MB" \
                              --restart unless-stopped \
                              ${DOCKER_IMAGE_BACKEND}:latest
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
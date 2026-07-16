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
        // 🟢 NOUVEAU : secrets de déploiement, stockés dans Jenkins Credentials — plus jamais en clair ici
        MONGODB_URI  = credentials('backend-mongodb-uri')      // Secret text : mongodb://adminUser:***@admin_mongodb:27017/admin_db?authSource=admin
        JWT_SECRET   = credentials('backend-jwt-secret')       // Secret text
        MAIL_USERNAME = credentials('backend-mail-username')   // Secret text
        MAIL_PASSWORD = credentials('backend-mail-password')   // Secret text
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

        // 🟢 NOUVEAU : injecte le fichier de config de test depuis les Jenkins Credentials
        // (Secret file), puisque application-test.properties n'est plus versionné dans Git
        // (il contient des identifiants MongoDB — voir .gitignore).
        stage('🔐 Préparer la config de test') {
            steps {
                echo '=== Injection de application-test.properties depuis Jenkins Credentials ==='
                withCredentials([file(credentialsId: 'backend-application-test-properties', variable: 'TEST_PROPS_FILE')]) {
                    sh 'cp "$TEST_PROPS_FILE" src/test/resources/application-test.properties'
                }
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
                      // 🟢 CORRIGÉ : plus aucune valeur secrète en clair — tout vient de environment{}
                      // ci-dessus, lui-même alimenté par les Jenkins Credentials.
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

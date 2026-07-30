# Alumni Platform - User Service

Genere a partir de `user-service.yaml` avec OpenAPI Generator 7.24.0 (generator `spring`, mode `interfaceOnly`).

## Structure

```
user-service-parent/          <- pom parent (reactor)
├── user-service-spi/         <- INTERFACES generees (SPI) - NE PAS MODIFIER A LA MAIN
│   └── src/main/java/com/uasz/alumni/userservice/
│       ├── api/UsersApi.java          <- interface a implementer
│       └── model/*.java               <- User, CreateUserRequest, UpdateUserRequest, UserPage, Address, ErrorResponse, UserType, UserStatus, Gender
└── user-service-app/         <- appli Spring Boot qui implemente le SPI
    └── src/main/java/com/uasz/alumni/userservice/app/
        ├── UserServiceApplication.java
        ├── controller/UsersController.java       <- implements UsersApi (les 7 endpoints)
        ├── controller/GlobalExceptionHandler.java
        └── repository/UserRepository.java        <- stockage en memoire (a remplacer par JPA/PostgreSQL)
```

## Etapes deja faites

1. **Mock server** : le yaml contient deja un serveur `SwaggerHub API Auto Mocking`
   (`https://virtserver.swaggerhub.com/uasz-77e/user-service/1.0.0`). C'est le mock
   deploye. Si vous en voulez un autre (Prism, ou genere par openapi-generator +
   deploye sur Render/Railway), voir section "Alternative mock" plus bas.
2. **SPI genere** : `user-service-spi` (commande ci-dessous).
3. **Projet Spring Boot initialise** avec le SPI comme dependance Maven (reactor).
4. **Endpoints implementes** dans `UsersController` en respectant les signatures du SPI.
5. **SDK client Java genere** dans `../sdk-java` (a cote de ce dossier).

## Commandes utilisees (a reproduire / adapter)

```bash
# SPI (interfaces Spring, pas d'implementation)
openapi-generator-cli generate \
  -i user-service.yaml -g spring -o ./user-service-spi \
  --api-package com.uasz.alumni.userservice.api \
  --model-package com.uasz.alumni.userservice.model \
  --group-id com.uasz.alumni --artifact-id user-service-spi --artifact-version 1.0.0 \
  --additional-properties=interfaceOnly=true,useTags=true,useSpringBoot3=true,skipDefaultInterface=true,dateLibrary=java8

# SDK client (ici en Java ; remplacer -g java par typescript-axios, python, etc. si besoin)
openapi-generator-cli generate \
  -i user-service.yaml -g java -o ./sdk-java \
  --api-package com.uasz.alumni.userservice.client.api \
  --model-package com.uasz.alumni.userservice.client.model \
  --invoker-package com.uasz.alumni.userservice.client \
  --group-id com.uasz.alumni --artifact-id user-service-sdk --artifact-version 1.0.0 \
  --additional-properties=library=resttemplate,dateLibrary=java8,useJakartaEe=true
```

## Lancer le projet

```bash
cd user-service-parent
mvn clean install          # construit le SPI puis l'app (ordre gere par le reactor)
cd user-service-app
mvn spring-boot:run
# API disponible sur http://localhost:8080/users
```

## Alternative mock server (si le prof veut un mock genere par vous, pas SwaggerHub)

```bash
# Genere un serveur mock Node/Express qui repond avec les exemples du yaml
openapi-generator-cli generate -i user-service.yaml -g nodejs-express-server -o ./mock-server
cd mock-server && npm install && npm start   # ecoute sur http://localhost:3000
```
Deployer ensuite ce dossier sur Render/Railway/Fly.io (gratuit), puis ajouter
l'URL publique dans `servers:` du yaml.

## Point SonarQube (note du prof)

SonarQube s'execute **apres le push** (analyse statique en CI, ex: GitHub Actions /
GitLab CI), pas a l'execution de l'application. Rien a coder pour ca : juste
s'assurer que le pipeline CI appelle `mvn sonar:sonar` (ou l'action GitHub
correspondante) apres le build.

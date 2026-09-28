# Build image
FROM maven:3.8.5-jdk-11@sha256:4e4b4e0a8bc2d07f5fef86d2b55b19fa0e8c8aa54540b34be27c711904a8014d AS MAVEN_BUILD

WORKDIR /pn-logextractor-build/

COPY . .

RUN  mvn clean package -Dmaven.test.skip=true

# Runtime image
FROM eclipse-temurin:11-jre-alpine@sha256:ea8a026083aaf0ad939774bec895ba24f67bd73cafde44c5eff6cf306870b878

WORKDIR /app

COPY --from=MAVEN_BUILD /pn-logextractor-build/target/pn-logextractor-*.jar /app/pn-logextractor.jar

HEALTHCHECK CMD curl -f http://localhost:8080/status || exit 1

RUN apk add curl

ENTRYPOINT ["java", "-jar", "pn-logextractor.jar"]
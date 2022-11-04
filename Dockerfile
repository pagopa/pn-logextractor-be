FROM maven:3.8.5-jdk-11 AS MAVEN_BUILD

WORKDIR /pn-logextractor-build/

COPY . .

RUN  mvn clean package -Dmaven.test.skip=true

FROM eclipse-temurin:11-jre-alpine

WORKDIR /app

COPY --from=MAVEN_BUILD /pn-logextractor-build/target/pn-logextractor-*.jar /app/pn-logextractor.jar

HEALTHCHECK CMD curl -f http://localhost:8080/health-check/status || exit 1

RUN apk add curl

ENTRYPOINT ["java", "-jar", "pn-logextractor.jar"]
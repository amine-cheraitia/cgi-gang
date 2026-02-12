FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /workspace

COPY pom.xml ./
COPY marketplace-domain/pom.xml marketplace-domain/pom.xml
COPY marketplace-application/pom.xml marketplace-application/pom.xml
COPY marketplace-infrastructure/pom.xml marketplace-infrastructure/pom.xml

COPY marketplace-domain/src marketplace-domain/src
COPY marketplace-application/src marketplace-application/src
COPY marketplace-infrastructure/src marketplace-infrastructure/src

RUN mvn -q -pl marketplace-infrastructure -am clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /workspace/marketplace-infrastructure/target/marketplace-infrastructure-*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

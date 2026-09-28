FROM maven:3.9.9-eclipse-temurin-21-jammy AS builder

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests


FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=builder /app/target/employee-service-0.0.1-SNAPSHOT.jar employee-service.jar

ENTRYPOINT ["java", "-jar", "employee-service.jar"]
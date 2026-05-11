FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml ./pom.xml
COPY todo-service/ ./todo-service/
RUN cd todo-service && mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/todo-service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
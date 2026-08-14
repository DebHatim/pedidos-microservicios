# 1 - Compile the project
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# 2 - First copy the pom.xml file and download the dependencies
# By separating it from 'src', Docker will cache this layer. If only the source code changes in the future,
# it won't have to download the dependencies again.
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline -B

# 3 - Copy the source code and package the application
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests

# 4 - Create the final image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 5 - Copy the JAR file generated from the compilation stage
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
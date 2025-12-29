# Stage 1: Build the application
  FROM maven:3.9-eclipse-temurin-21-alpine AS build
  WORKDIR /app

  # Copy pom.xml and download dependencies (layer caching)
  COPY pom.xml .
  RUN mvn dependency:go-offline -B

  # Copy source code and build
  COPY src ./src
  COPY api ./api
  COPY pmd-ruleset.xml .
  COPY spotbugs-exclude.xml .
  COPY checkstyle.xml .
  RUN mvn clean package -DskipTests -Dpmd.skip=true -Dspotbugs.skip=true -Dcheckstyle.skip=true
  # Stage 2: Runtime image
  FROM eclipse-temurin:21-jre-alpine
  WORKDIR /app

  # Copy the built JAR from build stage
  COPY --from=build /app/target/*.jar app.jar

  # Expose the port (Railway/Fly.io will override with PORT env var)
  EXPOSE 8080

  # Run the application
  ENTRYPOINT ["java", "-jar", "app.jar"]
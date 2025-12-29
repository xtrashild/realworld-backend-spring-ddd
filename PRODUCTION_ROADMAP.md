# Backend Production Readiness Roadmap

This document outlines the steps to make the RealWorld backend production-ready with PostgreSQL, Docker, CI/CD, and deployment.

## Current Status

✅ **Completed:**
- Lombok migration (removed 451 lines of boilerplate)
- Dependency updates to latest versions
- Static analysis (PMD, SpotBugs, Checkstyle) re-enabled
- JaCoCo configured with exclusions for generated code
- All tests passing
- Application running successfully with H2

## Next Steps

### 1. PostgreSQL Migration

**Goal:** Migrate from H2 (in-memory) to PostgreSQL for production persistence.

**Steps:**

1. **Add PostgreSQL dependency to `pom.xml`**
   ```xml
   <!-- Keep H2 for dev -->
   <dependency>
       <groupId>com.h2database</groupId>
       <artifactId>h2</artifactId>
       <scope>runtime</scope>
   </dependency>

   <!-- Add PostgreSQL for prod -->
   <dependency>
       <groupId>org.postgresql</groupId>
       <artifactId>postgresql</artifactId>
       <scope>runtime</scope>
   </dependency>
   ```

2. **Create profile-specific configurations**

   **`src/main/resources/application.properties`** (base):
   ```properties
   # Default profile
   spring.profiles.active=${SPRING_PROFILES_ACTIVE:dev}

   # Server
   server.port=${PORT:8080}

   # SpringDoc OpenAPI
   springdoc.api-docs.path=/api-docs
   springdoc.swagger-ui.path=/swagger-ui.html
   ```

   **`src/main/resources/application-dev.properties`** (H2 for local dev):
   ```properties
   # H2 Database (in-memory)
   spring.datasource.url=jdbc:h2:mem:testdb
   spring.datasource.driverClassName=org.h2.Driver
   spring.h2.console.enabled=true
   spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
   spring.jpa.hibernate.ddl-auto=create-drop
   spring.jpa.show-sql=true

   # JWT
   jwt.secret=${JWT_SECRET:caa3e0593e2b968efb6278433206c1a8e3c19a92d57868fd86100aa95cf38771}
   jwt.sessionTime=${JWT_SESSION_TIME:86400}
   ```

   **`src/main/resources/application-prod.properties`** (PostgreSQL):
   ```properties
   # PostgreSQL Database (Supabase)
   spring.datasource.url=${DATABASE_URL}
   spring.datasource.username=${DB_USERNAME}
   spring.datasource.password=${DB_PASSWORD}
   spring.datasource.driver-class-name=org.postgresql.Driver
   spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.show-sql=false

   # JWT (must be from environment variables)
   jwt.secret=${JWT_SECRET}
   jwt.sessionTime=${JWT_SESSION_TIME:86400}
   ```

3. **Create `.env` file for local testing** (add to `.gitignore`!)
   ```env
   SPRING_PROFILES_ACTIVE=prod
   DATABASE_URL=postgresql://postgres.xxxxx.supabase.co:5432/postgres
   DB_USERNAME=postgres
   DB_PASSWORD=your-supabase-password
   JWT_SECRET=your-production-jwt-secret
   ```

4. **Update `.gitignore`**
   ```
   .env
   *.env
   application-local.properties
   ```

5. **Test locally**
   ```bash
   # Dev mode (H2)
   mvn spring-boot:run

   # Prod mode (PostgreSQL)
   source .env && mvn spring-boot:run
   ```

---

### 2. Security: Externalize Secrets

**Goal:** Remove hardcoded secrets from properties files.

**Steps:**

1. **Generate new JWT secret**
   ```bash
   openssl rand -hex 64
   ```

2. **Move all secrets to environment variables**
   - Already done in application-prod.properties
   - Update application-dev.properties to use defaults with `${VAR:default}` syntax

3. **Document required environment variables**
   - Create `ENV_VARIABLES.md` listing all required vars
   - Include in deployment documentation

---

### 3. Docker Setup

**Goal:** Containerize the application for consistent deployment.

**Steps:**

1. **Create `Dockerfile`** (multi-stage build)
   ```dockerfile
   # Build stage
   FROM maven:3.9-eclipse-temurin-21-alpine AS build
   WORKDIR /app
   COPY pom.xml .
   COPY api ./api
   COPY src ./src
   RUN mvn clean package -DskipTests

   # Run stage
   FROM eclipse-temurin:21-jre-alpine
   WORKDIR /app
   COPY --from=build /app/target/*.jar app.jar
   EXPOSE 8080
   ENTRYPOINT ["java", "-jar", "app.jar"]
   ```

2. **Create `docker-compose.yml`** (for local testing with PostgreSQL)
   ```yaml
   version: '3.8'
   services:
     postgres:
       image: postgres:16-alpine
       environment:
         POSTGRES_DB: realworld
         POSTGRES_USER: postgres
         POSTGRES_PASSWORD: postgres
       ports:
         - "5432:5432"
       volumes:
         - postgres_data:/var/lib/postgresql/data

     backend:
       build: .
       ports:
         - "8080:8080"
       environment:
         SPRING_PROFILES_ACTIVE: prod
         DATABASE_URL: jdbc:postgresql://postgres:5432/realworld
         DB_USERNAME: postgres
         DB_PASSWORD: postgres
         JWT_SECRET: your-secret-here
       depends_on:
         - postgres

   volumes:
     postgres_data:
   ```

3. **Create `.dockerignore`**
   ```
   target/
   .git/
   .env
   *.md
   .gitignore
   ```

4. **Test Docker locally**
   ```bash
   # Build image
   docker build -t realworld-backend .

   # Run with docker-compose
   docker-compose up

   # Test API
   curl http://localhost:8080/api/tags
   ```

---

### 4. CI/CD Pipeline (GitHub Actions)

**Goal:** Automate testing and deployment.

**Steps:**

1. **Create `.github/workflows/ci.yml`**
   ```yaml
   name: CI

   on:
     push:
       branches: [ main ]
     pull_request:
       branches: [ main ]

   jobs:
     build:
       runs-on: ubuntu-latest

       steps:
       - uses: actions/checkout@v4

       - name: Set up JDK 21
         uses: actions/setup-java@v4
         with:
           java-version: '21'
           distribution: 'temurin'
           cache: maven

       - name: Build with Maven
         run: mvn -B clean verify

       - name: Upload coverage to Codecov
         uses: codecov/codecov-action@v4
         with:
           file: ./target/site/jacoco/jacoco.xml
   ```

2. **Create `.github/workflows/docker.yml`** (optional - for Docker image publishing)
   ```yaml
   name: Docker

   on:
     push:
       branches: [ main ]
       tags: [ 'v*' ]

   jobs:
     docker:
       runs-on: ubuntu-latest
       steps:
       - uses: actions/checkout@v4

       - name: Build Docker image
         run: docker build -t realworld-backend .

       - name: Log in to Docker Hub
         if: github.event_name != 'pull_request'
         uses: docker/login-action@v3
         with:
           username: ${{ secrets.DOCKER_USERNAME }}
           password: ${{ secrets.DOCKER_PASSWORD }}

       - name: Push to Docker Hub
         if: github.event_name != 'pull_request'
         run: |
           docker tag realworld-backend ${{ secrets.DOCKER_USERNAME }}/realworld-backend:latest
           docker push ${{ secrets.DOCKER_USERNAME }}/realworld-backend:latest
   ```

---

### 5. Deployment to Free Tier Platform

**Goal:** Deploy to production on a free tier service.

**Options:**

#### **Option A: Railway.app** (Recommended)
- ✅ Free tier: 500 hours/month, $5 credit
- ✅ PostgreSQL included
- ✅ Easy GitHub integration
- ✅ Automatic HTTPS

**Steps:**
1. Sign up at railway.app
2. Create new project → "Deploy from GitHub repo"
3. Select your repository
4. Add PostgreSQL service
5. Set environment variables:
   - `SPRING_PROFILES_ACTIVE=prod`
   - `DATABASE_URL` (auto-provided by Railway)
   - `JWT_SECRET` (generate new one)
6. Deploy!

#### **Option B: Render.com**
- ✅ Free tier: 750 hours/month
- ✅ PostgreSQL included (90 days)
- ✅ Dockerfile support

**Steps:**
1. Sign up at render.com
2. New Web Service → Connect GitHub repo
3. Select "Docker" runtime
4. Add PostgreSQL database
5. Set environment variables
6. Deploy!

#### **Option C: Fly.io**
- ✅ Free tier: 3 shared VMs
- ✅ PostgreSQL included (3GB)
- ✅ CLI-based deployment

**Steps:**
1. Install flyctl: `brew install flyctl` or download
2. `fly auth login`
3. `fly launch` (generates fly.toml)
4. `fly postgres create`
5. `fly secrets set JWT_SECRET=xxx`
6. `fly deploy`

---

## Testing Checklist

Before going to production:

- [ ] All tests passing (`mvn clean verify`)
- [ ] Application runs with dev profile (H2)
- [ ] Application runs with prod profile (PostgreSQL/Supabase)
- [ ] Docker image builds successfully
- [ ] Docker container runs and connects to database
- [ ] API endpoints work (test with Postman/curl)
- [ ] Frontend connects to deployed backend
- [ ] Environment variables properly configured
- [ ] No secrets in git repository
- [ ] CI/CD pipeline runs successfully

---

## Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Railway.app Docs](https://docs.railway.app/)
- [Render.com Docs](https://render.com/docs)
- [Fly.io Docs](https://fly.io/docs/)
- [Supabase PostgreSQL](https://supabase.com/docs/guides/database)
- [GitHub Actions](https://docs.github.com/en/actions)

---

## Notes

- **Database Migration:** For production, consider using Flyway or Liquibase for schema versioning
- **Monitoring:** Add Spring Boot Actuator for health checks and metrics
- **Logging:** Configure proper logging levels for production
- **CORS:** Update CORS settings if frontend is on different domain
- **Rate Limiting:** Consider adding rate limiting for API endpoints

---

**Last Updated:** 2025-12-27
**Status:** Ready to start PostgreSQL migration

# ---- Frontend build ----
FROM node:22-alpine AS frontend
WORKDIR /app/frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# ---- Backend build ----
FROM maven:3.9.9-eclipse-temurin-21 AS backend
WORKDIR /app
COPY backend/pom.xml ./
RUN mvn -q -B dependency:go-offline
COPY backend/src ./src
COPY --from=frontend /app/frontend/dist ./src/main/resources/static
RUN mvn -q -B -DskipTests package

# ---- Runtime ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
ENV JAVA_OPTS=""
ENV SPRING_DATASOURCE_URL="jdbc:mysql://mysql:3306/ecommerce?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
ENV SPRING_DATASOURCE_USERNAME="ecommerce"
ENV SPRING_DATASOURCE_PASSWORD="ecommerce"
ENV SERVER_PORT=8080
COPY --from=backend /app/target/store-api-1.0.0.jar /app/app.jar
EXPOSE 8080
HEALTHCHECK --interval=15s --timeout=5s --retries=10 CMD wget -qO- http://127.0.0.1:8080/api/health || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

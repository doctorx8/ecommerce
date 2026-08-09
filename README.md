# NORTHLINE Store

OpenCart-inspired ecommerce app with:

- **MySQL** database
- **Java Spring Boot** API
- **React** storefront UI
- **One deployable package** (fat JAR or Docker Compose)

## Quick deploy (anywhere with Docker)

```bash
docker compose up -d
```

Open: **http://localhost:8080**

That starts:
- MySQL
- App (API + UI in one container)

### Hostinger Docker Manager

Hostinger only pulls public Docker Hub images reliably (GHCR often stays private).

1. Wait for GitHub Action **Release app JAR** to finish on `main`
2. Confirm the file exists: https://github.com/doctorx8/ecommerce/releases/latest
3. In Hostinger Docker Manager, redeploy this repo’s `docker-compose.yml`

Compose pulls `mysql:8.4` + `eclipse-temurin:21-jre-jammy`, then downloads `northline-store.jar` from GitHub Releases.

## Local package (single JAR)

Requires Java 21, Maven, Node.js, and a running MySQL.

```bash
# MySQL (Homebrew example)
brew services start mysql
mysql -u root -e "CREATE DATABASE IF NOT EXISTS ecommerce; CREATE USER IF NOT EXISTS 'ecommerce'@'localhost' IDENTIFIED BY 'ecommerce'; GRANT ALL PRIVILEGES ON ecommerce.* TO 'ecommerce'@'localhost'; FLUSH PRIVILEGES;"

# Build UI + backend into one jar
./scripts/package.sh

# Run
java -jar dist/northline-store.jar
```

Open: **http://localhost:8080**

## Development mode

Terminal 1 — API:

```bash
export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"
export JAVA_HOME="/opt/homebrew/opt/openjdk@21"
cd backend && mvn spring-boot:run
```

Terminal 2 — UI (hot reload, proxies `/api` → `:8080`):

```bash
cd frontend && npm run dev
```

UI: http://localhost:5173  
API: http://localhost:8080/api

## Demo accounts

| Role     | Email                 | Password     |
|----------|-----------------------|--------------|
| Customer | customer@store.local  | password123  |
| Admin    | admin@store.local     | password123  |

Coupon: `WELCOME10`

## Project layout

```
frontend/                 React storefront (NORTHLINE)
backend/                  Spring Boot + JPA + JWT
scripts/package.sh        Build fat JAR with UI embedded
Dockerfile                Multi-stage production image
docker-compose.yml        App + MySQL
dist/northline-store.jar  Deployable artifact (after package)
```

## Useful URLs

- Store UI: http://localhost:8080/
- API index: http://localhost:8080/api
- Products: http://localhost:8080/api/products
- Health: http://localhost:8080/api/health

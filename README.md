# Karwan Store

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

1. Application / project name: **karwan**
2. Wait for GitHub Action **Release app JAR** to finish on `main`
3. Confirm: https://github.com/doctorx8/ecommerce/releases/latest
4. Deploy this repo’s `docker-compose.yml`

Compose pulls `mysql:8.4` + `eclipse-temurin:21-jre-jammy`, then downloads `karwan-store.jar` from GitHub Releases.

## Local package (single JAR)

Requires Java 21, Maven, Node.js, and a running MySQL.

```bash
brew services start mysql
./scripts/package.sh
java -jar dist/karwan-store.jar
```

## Development mode

```bash
# API
cd backend && mvn spring-boot:run

# UI
cd frontend && npm run dev
```

## Demo accounts

| Role     | Email                 | Password     |
|----------|-----------------------|--------------|
| Customer | customer@store.local  | password123  |
| Admin    | admin@store.local     | password123  |

Coupon: `WELCOME10`

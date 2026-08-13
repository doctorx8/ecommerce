# Karwan Store

OpenCart-inspired ecommerce app with:

- **MySQL** database
- **Java Spring Boot** API
- **React** storefront UI
- **One deployable package** (fat JAR or Docker Compose)

## Quick deploy (anywhere with Docker)

**Hostinger / VPS (uses published release JAR):**
```bash
docker compose up -d
```

**Local build (no GitHub release needed):**
```bash
docker compose -f docker-compose.local.yml up -d --build
```

Open: **http://localhost:8080**

### Hostinger Docker Manager

The GitHub repo must be **public** so Hostinger can download the compose file and JAR.

1. Confirm release exists: https://github.com/doctorx8/ecommerce/releases/latest
2. In Docker Manager, create project from URL (must be the **raw** file):
   ```
   https://raw.githubusercontent.com/doctorx8/ecommerce/main/docker-compose.yml
   ```
3. Application / project name: **karwan**
4. After deploy, open `http://YOUR_VPS_IP:8080`

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

## Admin console

Open **http://localhost:8080/admin** (or `/admin` on your deployed host).

| Role     | Email                 | Password     |
|----------|-----------------------|--------------|
| Customer | customer@store.local  | password123  |
| Admin    | admin@store.local     | password123  |

Admin features: store overview, inventory/stock edits, order & payment status, customers, coupons, review moderation, audit log, sales chart.

Also includes: mock checkout payment, SMTP emails (Mailhog locally on `:8025`), shipping/tax quote, wishlist, moderated reviews, password reset.

See `.env.example` for mail/shipping/tax/JWT settings.

## Postman

Import `postman/Karwan-API.postman_collection.json` and `postman/Karwan-Local.postman_environment.json`.

1. Run **01 Token Generators → Customer Login** (or Admin Login) — JWT is saved automatically  
2. Follow the numbered workflow folders  

Details: `postman/README.md`

Coupon: `WELCOME10`

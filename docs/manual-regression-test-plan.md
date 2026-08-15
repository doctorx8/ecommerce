# Karwan — Manual Regression Test Plan

**Purpose:** Full-stack manual regression after deploy or major changes (API, DB, storefront, admin, mail, payments).  
**Base URL:** `http://localhost:8080` (or your Hostinger host)  
**Date / build:** _______________ **Tester:** _______________ **Env:** ☐ Local ☐ Staging ☐ Prod

### Demo accounts

| Role | Email | Password |
|------|--------|----------|
| Customer | `customer@store.local` | `password123` |
| Admin | `admin@store.local` | `password123` |
| Coupon | `WELCOME10` | — |

**Mark:** ☐ Pass ☐ Fail ☐ Blocked ☐ N/A  
**Severity if fail:** S1 blocker · S2 major · S3 minor · S4 cosmetic

---

## 0. Preflight (infra / deploy layer)

| ID | Step | Expected | Result | Notes |
|----|------|----------|--------|-------|
| INF-01 | Open base URL | Storefront loads (home + header) | ☐ | |
| INF-02 | `GET /api/health` | Healthy / 200 JSON | ☐ | |
| INF-03 | `GET /api` | API docs/root JSON | ☐ | |
| INF-04 | Docker: `mysql` + app containers up | Both healthy; app reaches DB | ☐ | |
| INF-05 | Mailhog UI `:8025` (local) or SMTP configured | Reachable if testing mail | ☐ | |
| INF-06 | After new JAR release, recreate app container | New features present (header cats / products) | ☐ | |
| INF-07 | DB login (`ecommerce` / `ecommerce`) | Can `USE ecommerce; SHOW TABLES;` | ☐ | |

---

## 1. Catalog / data layer (seed + API)

| ID | Step | Expected | Result | Notes |
|----|------|----------|--------|-------|
| CAT-01 | `GET /api/categories?tree=true` | Top cats: notebook, pc-accessories, gaming, smartphone-tablet, tv-audio, smart-home | ☐ | |
| CAT-02 | `GET /api/manufacturers` | Apple, Sony, Samsung, Dell, Logitech, Nintendo, Google, Bose, ASUS, Philips | ☐ | |
| CAT-03 | `GET /api/products?limit=48` | ≥ ~15–20 products; `total` correct | ☐ | |
| CAT-04 | `GET /api/products?category=notebook` | MacBook / XPS / Zenbook (or similar) | ☐ | |
| CAT-05 | `GET /api/products?manufacturer=apple` | Apple-only list | ☐ | |
| CAT-06 | `GET /api/products?sort=price_asc` | Prices ascending | ☐ | |
| CAT-07 | `GET /api/products?sort=popular` | 200; ordered by views | ☐ | |
| CAT-08 | `GET /api/products?featured=true` | Featured only | ☐ | |
| CAT-09 | `GET /api/products?inStock=true` | In-stock only | ☐ | |
| CAT-10 | `GET /api/products?onSale=true` | Items with compare-at > price | ☐ | |
| CAT-11 | `GET /api/products?minPrice=100&maxPrice=500` | Prices in range | ☐ | |
| CAT-12 | `GET /api/products?search=iphone` | Relevant hits | ☐ | |
| CAT-13 | `GET /api/products/{slug}` e.g. `iphone-15` | Detail + images + categories | ☐ | |
| CAT-14 | `GET /api/coupons/validate/WELCOME10` | Valid discount payload | ☐ | |

---

## 2. Storefront — header & navigation (UI)

| ID | Step | Expected | Result | Notes |
|----|------|----------|--------|-------|
| NAV-01 | Header top: brand + search + account/wishlist/cart icons | Matches Cyberport-style layout | ☐ | |
| NAV-02 | Search “sony” → submit | `/shop?q=sony` with matching products | ☐ | |
| NAV-03 | Category: Apple | `/shop?manufacturer=apple` | ☐ | |
| NAV-04 | Category: Notebook | `/shop?category=notebook` | ☐ | |
| NAV-05 | Gaming / Smartphone & Tablet / TV & Audio / Smart Home / PC & Accessories | Correct filtered shop | ☐ | |
| NAV-06 | Offers | `/shop?onSale=1` | ☐ | |
| NAV-07 | Outlet | Sale + price asc | ☐ | |
| NAV-08 | All (hamburger) mega menu | Categories, brands, deals; links work; backdrop closes | ☐ | |
| NAV-09 | Shop all | Full catalog | ☐ | |
| NAV-10 | Mobile (~375px): search wraps; cats scroll; icons usable | No broken overflow | ☐ | |

---

## 3. Storefront — home & shop (UI)

| ID | Step | Expected | Result | Notes |
|----|------|----------|--------|-------|
| SHP-01 | Home hero shows KARWAN + CTAs | Brand-first hero | ☐ | |
| SHP-02 | Featured products render with images | Cards clickable to PDP | ☐ | |
| SHP-03 | Shop toolbar: search, category, brand, sort, min/max $ | All update URL + results | ☐ | |
| SHP-04 | Sort: newest, oldest, popular, featured, price ↑↓, name A–Z/Z–A, catalog | List order changes | ☐ | |
| SHP-05 | Toggles: Featured / In stock / On sale | Filters apply; count updates | ☐ | |
| SHP-06 | Clear filters | Resets URL + full list | ☐ | |
| SHP-07 | Empty filters | Friendly empty + clear | ☐ | |
| SHP-08 | Result count matches API `total` | e.g. “N products” | ☐ | |

---

## 4. Product detail, reviews, wishlist (UI + API)

| ID | Step | Expected | Result | Notes |
|----|------|----------|--------|-------|
| PDP-01 | Open product with options (iPhone if present) | Name, price, stock, media | ☐ | |
| PDP-02 | Add to cart (qty 2) | Success; cart badge increments | ☐ | |
| PDP-03 | Guest: wishlist | Prompt / blocked sensibly | ☐ | |
| PDP-04 | Logged-in: add wishlist | Appears on `/wishlist` | ☐ | |
| PDP-05 | Remove from wishlist | Removed | ☐ | |
| PDP-06 | Submit review (logged-in) | “Pending admin approval” message | ☐ | |
| PDP-07 | Public reviews list | Only **approved** reviews visible | ☐ | |
| PDP-08 | Out-of-stock / low stock (if set in admin) | UI reflects status | ☐ | |

---

## 5. Cart & checkout (UI + pricing + mock pay)

| ID | Step | Expected | Result | Notes |
|----|------|----------|--------|-------|
| CART-01 | Cart: change qty | Subtotal updates | ☐ | |
| CART-02 | Cart: remove line / clear | Cart updates | ☐ | |
| CART-03 | Guest cart survives refresh (session) | Same items | ☐ | |
| CHK-01 | Checkout with empty cart | Empty state + link to shop | ☐ | |
| CHK-02 | Quote without coupon | Subtotal + shipping + tax + total | ☐ | |
| CHK-03 | Subtotal &lt; $100 | Shipping **$9.99** (default) | ☐ | |
| CHK-04 | Subtotal ≥ $100 | Free shipping | ☐ | |
| CHK-05 | Coupon `WELCOME10` | Discount applied; tax on (subtotal − discount) | ☐ | |
| CHK-06 | Invalid coupon | Error / no discount | ☐ | |
| CHK-07 | Mock pay **SUCCESS** | Order created; payment status paid/success | ☐ | |
| CHK-08 | Mock pay **FAIL** | Order fails or unpaid; clear error | ☐ | |
| CHK-09 | Mock pay **PENDING** | Order pending payment status | ☐ | |
| CHK-10 | Order confirmation `/order/{id}` | Number, lines, totals, status | ☐ | |
| CHK-11 | Account → Orders | New order listed | ☐ | |

---

## 6. Customer auth & account (UI + API)

| ID | Step | Expected | Result | Notes |
|----|------|----------|--------|-------|
| AUTH-01 | Register new email | Token + redirect / logged in | ☐ | |
| AUTH-02 | Login customer | Access account | ☐ | |
| AUTH-03 | Bad password | Error; no token | ☐ | |
| AUTH-04 | Forgot password | Success message; Mailhog shows reset mail (local) | ☐ | |
| AUTH-05 | Reset via token | Password changes; can login | ☐ | |
| AUTH-06 | Update profile | Name/email/phone saved | ☐ | |
| AUTH-07 | Change password | Old rejected; new works | ☐ | |
| AUTH-08 | Addresses CRUD + default | Create/edit/delete/default | ☐ | |
| AUTH-09 | Delete account (danger) | Account gone; cannot login | ☐ | Use disposable user |
| AUTH-10 | Protected routes without login | Redirect / auth required | ☐ | `/account`, `/wishlist` |

---

## 7. Admin console (UI + RBAC)

Login: `/admin` → `admin@store.local` / `password123`

| ID | Step | Expected | Result | Notes |
|----|------|----------|--------|-------|
| ADM-01 | Admin login | Dashboard | ☐ | |
| ADM-02 | Customer JWT cannot open `/admin` APIs | 401/403 | ☐ | |
| ADM-03 | Overview: counts + sales chart (14d) | Renders; no crash | ☐ | |
| ADM-04 | Inventory: search / low stock | List loads | ☐ | |
| ADM-05 | Patch inventory qty / stock status | Product updates; storefront reflects | ☐ | |
| ADM-06 | Orders: filter + open detail | Shipping, payment, lines visible | ☐ | |
| ADM-07 | Update order status (allowed transition) | Status changes | ☐ | |
| ADM-08 | Illegal status jump | Rejected with error | ☐ | |
| ADM-09 | Cancel order (admin) | Cancelled; stock/rules OK | ☐ | |
| ADM-10 | Refund order (admin) | Refunded when allowed | ☐ | |
| ADM-11 | Customers list | Shows seeded + registered | ☐ | |
| ADM-12 | Coupons CRUD | Create/edit/delete; validate on storefront | ☐ | |
| ADM-13 | Reviews: approve | Appears on PDP | ☐ | |
| ADM-14 | Reviews: reject / delete | Not public | ☐ | |
| ADM-15 | Audit log | Recent admin actions listed | ☐ | |
| ADM-16 | Logout / session expiry | Back to admin login | ☐ | |

### STAFF vs ADMIN (if STAFF user exists)

| ID | Step | Expected | Result | Notes |
|----|------|----------|--------|-------|
| RBAC-01 | STAFF: inventory + orders | Allowed | ☐ | |
| RBAC-02 | STAFF: coupons / cancel / refund / reviews / audit | Denied | ☐ | |
| RBAC-03 | ADMIN: all of the above | Allowed | ☐ | |

---

## 8. Cross-cutting: email, security, API auth

| ID | Step | Expected | Result | Notes |
|----|------|----------|--------|-------|
| X-01 | Order placed → confirmation email (Mailhog) | Mail received with order # | ☐ | |
| X-02 | Password reset email | Link/token usable once | ☐ | |
| X-03 | Reuse spent reset token | Rejected | ☐ | |
| X-04 | Cart/checkout APIs without session where required | Sensible errors | ☐ | |
| X-05 | Admin endpoints without Bearer | 401 | ☐ | |
| X-06 | CORS / same-origin UI→API | Shop works from deployed host | ☐ | |

---

## 9. Regression smoke (15-minute path)

Run this after every deploy if short on time:

1. ☐ Health OK  
2. ☐ Home + category nav (Notebook + Apple)  
3. ☐ Search + sort price  
4. ☐ Add to cart → checkout SUCCESS + `WELCOME10`  
5. ☐ Order visible in account  
6. ☐ Admin login → see order → bump status  
7. ☐ Submit review → admin approve → visible on PDP  
8. ☐ Wishlist add/remove  

---

## 10. Sign-off

| Area | Pass? | Blockers |
|------|-------|----------|
| Infra / deploy | ☐ | |
| Catalog API | ☐ | |
| Storefront UI | ☐ | |
| Cart / checkout / pay | ☐ | |
| Customer account | ☐ | |
| Admin + RBAC | ☐ | |
| Mail | ☐ | |

**Overall:** ☐ Ready to ship ☐ Ready with known issues ☐ Not ready  

**Known issues / waivers:**  
_ _

**Tester signature / date:** _______________

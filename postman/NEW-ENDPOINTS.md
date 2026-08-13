# Postman — new endpoints (v2 features)

Import the main collection, then add these requests (or re-import after regenerating).

## Token generators (unchanged)
- `POST {{baseUrl}}/auth/login` → `customer_token`
- `POST {{baseUrl}}/auth/admin/login` → `admin_token`

## Checkout / payment / pricing
| Method | Path | Auth | Notes |
|--------|------|------|-------|
| POST | `/orders/quote` | optional | Body: `{ sessionId, couponCode }` |
| POST | `/orders/checkout` | optional | Add `paymentOutcome`: `SUCCESS` \| `FAIL` \| `PENDING` |
| GET | `/payments/mock` | public | Describes mock provider |

## Auth / email
| Method | Path | Body |
|--------|------|------|
| POST | `/auth/forgot-password` | `{ "email": "..." }` |
| POST | `/auth/reset-password` | `{ "token": "...", "newPassword": "password123" }` |

## Reviews
| Method | Path | Auth |
|--------|------|------|
| GET | `/reviews/product/{{productId}}` | public |
| POST | `/reviews` | customer | `{ productId, rating, text }` |
| GET | `/admin/reviews/pending` | ADMIN |
| PATCH | `/admin/reviews/{{id}}/approve` | ADMIN |
| PATCH | `/admin/reviews/{{id}}/reject` | ADMIN |
| DELETE | `/admin/reviews/{{id}}` | ADMIN |

## Wishlist
| Method | Path | Auth |
|--------|------|------|
| GET | `/wishlist` | customer |
| POST | `/wishlist/{{productId}}` | customer |
| DELETE | `/wishlist/{{productId}}` | customer |

## Admin cancel / refund / audit
| Method | Path | Auth |
|--------|------|------|
| POST | `/admin/orders/{{orderId}}/cancel` | ADMIN | `{ "comment": "..." }` |
| POST | `/admin/orders/{{orderId}}/refund` | ADMIN | `{ "comment": "..." }` |
| GET | `/admin/audit-logs` | ADMIN |

## Env vars (see `.env.example`)
`APP_SHIPPING_*`, `APP_TAX_RATE`, `SPRING_MAIL_*`, `APP_MAIL_*`, `APP_PUBLIC_URL`, `APP_JWT_SECRET`

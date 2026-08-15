# Karwan UI selectors (test automation)

Prefer **`data-testid`** for automation. `id` mirrors the same names for Selenium `By.id`. Visible **link text** / **button text** also work.

## Locator cheat sheet

| Strategy | Example |
|----------|---------|
| CSS test id | `[data-testid="auth-email"]` |
| ID | `#auth-email` |
| Link text | `Shop`, `Sign in`, `Checkout` |
| Partial link text | `Continue shopping` |
| Button text | `Add to cart`, `Place order`, `Sign in` |
| Name | `name="email"` on auth/checkout forms |

### Playwright
```ts
await page.getByTestId('auth-email').fill('customer@store.local')
await page.getByTestId('auth-submit').click()
await page.getByRole('link', { name: 'Shop' }).click()
```

### Selenium (Java)
```java
driver.findElement(By.cssSelector("[data-testid='auth-email']")).sendKeys("customer@store.local");
driver.findElement(By.id("auth-submit")).click();
driver.findElement(By.linkText("Shop")).click();
```

### Cypress
```js
cy.get('[data-testid="auth-email"]').type('customer@store.local')
cy.get('[data-testid="auth-submit"]').click()
cy.contains('a', 'Shop').click()
```

---

## Navigation

| Element | data-testid / id | Link text |
|---------|------------------|-----------|
| Brand / home | `nav-brand` | KARWAN |
| Shop | `nav-shop` | Shop |
| Cart | `nav-cart` | Cart |
| Cart badge | `cart-count` | — |
| Sign in | `nav-signin` | Sign in |
| Account | `nav-account` | Account |
| Sign out | `nav-signout` | Sign out |

## Home

| Element | data-testid / id | Link / button text |
|---------|------------------|--------------------|
| Page | `home-page` | — |
| CTA shop | `home-cta-shop` | Shop the lineup |
| CTA MacBook | `home-cta-macbook` | View MacBook Air |
| Featured grid | `featured-product-grid` | — |

## Shop

| Element | data-testid / id |
|---------|------------------|
| Page | `shop-page` |
| Search | `shop-search` |
| Category | `shop-category` |
| Brand | `shop-manufacturer` |
| Sort | `shop-sort` |
| Min / max price | `shop-min-price`, `shop-max-price` |
| Featured / in stock / on sale | `shop-featured`, `shop-in-stock`, `shop-on-sale` |
| Clear filters | `shop-clear-filters` |
| Result count | `shop-result-count` |
| Product grid | `shop-product-grid` |
| Product card | `product-card-{id}` (+ `data-product-slug`) |

## Product detail (PDP)

| Element | data-testid / id | Button text |
|---------|------------------|-------------|
| Page | `pdp-page` | — |
| Name | `pdp-name` | — |
| Price | `pdp-price` | — |
| Qty | `pdp-qty` | — |
| Add | `pdp-add-to-cart` | Add to cart |
| Success | `pdp-success` | View cart (link) |

## Auth (customer login / register)

| Element | data-testid / id | Button / tab text |
|---------|------------------|-------------------|
| Form | `auth-form` | — |
| Login tab | `auth-tab-login` | Sign in |
| Register tab | `auth-tab-register` | Register |
| First name | `auth-first-name` | — |
| Last name | `auth-last-name` | — |
| Email | `auth-email` | — |
| Password | `auth-password` | — |
| Submit | `auth-submit` | Sign in / Create account |
| Error | `auth-error` | — |

## Cart

| Element | data-testid / id | Link / button text |
|---------|------------------|--------------------|
| Page | `cart-page` | — |
| Empty | `cart-empty` | Continue shopping |
| Row | `cart-item-{id}` | — |
| Qty | `cart-qty-{id}` | — |
| Remove | `cart-remove-{id}` | Remove |
| Subtotal | `cart-subtotal` | — |
| Checkout | `cart-checkout` | Checkout |

## Checkout

| Element | data-testid / id | Button text |
|---------|------------------|-------------|
| Form | `checkout-form` | — |
| Email | `checkout-email` | — |
| First / last | `checkout-first-name`, `checkout-last-name` | — |
| Phone | `checkout-phone` | — |
| Address | `checkout-address1` | — |
| City / postcode | `checkout-city`, `checkout-postcode` | — |
| Country / zone | `checkout-country`, `checkout-zone` | — |
| Coupon | `checkout-coupon` | — |
| Submit | `checkout-submit` | Place order |
| Error | `checkout-error` | — |

## Order confirmation

| Element | data-testid / id | Link text |
|---------|------------------|-----------|
| Success | `order-success` | — |
| Number | `order-number` | — |
| Status | `order-status` | — |
| Total | `order-total` | — |
| Continue | `order-continue-shopping` | Continue shopping |

## Account

| Element | data-testid / id | Button text |
|---------|------------------|-------------|
| Tabs | `account-tab-profile`, `account-tab-password`, `account-tab-addresses`, `account-tab-orders`, `account-tab-danger` | Account info, Change password, … |
| Profile fields | `account-first-name`, `account-email`, … | — |
| Save profile | `account-save-profile` | Save changes |
| Password fields | `account-current-password`, `account-new-password`, `account-confirm-password` | — |
| Address form | `address-line1`, `address-city`, `address-save`, … | Add address |
| Orders | `account-order-{id}`, `account-order-link-{id}` | order number link text |

## Admin

| Element | data-testid / id | Link / button text |
|---------|------------------|--------------------|
| Login email/password | `admin-email`, `admin-password` | — |
| Login submit | `admin-login-submit` | Sign in |
| Nav | `admin-nav-overview`, `admin-nav-inventory`, `admin-nav-orders`, `admin-nav-customers`, `admin-nav-coupons` | Overview, Inventory, … |
| Sign out | `admin-signout` | Sign out |
| Inventory qty | `admin-qty-{id}` | — |
| Stock status | `admin-stock-status-{id}` | — |
| Save product | `admin-product-save-{id}` | Save |
| Order row | `admin-order-row-{id}` | — |
| Order status | `admin-order-status` | — |
| Payment status | `admin-payment-status` | — |
| Update order | `admin-order-update` | Update order |

---

## Checkout extras
| Element | data-testid |
|---------|-------------|
| Payment SUCCESS/PENDING/FAIL | `checkout-pay-success`, `checkout-pay-pending`, `checkout-pay-fail` |
| Shipping / tax / total | `checkout-shipping`, `checkout-tax`, `checkout-total` |

## PDP reviews / wishlist
| Element | data-testid |
|---------|-------------|
| Wishlist button | `pdp-wishlist` |
| Review form | `pdp-review-form`, `review-rating`, `review-text`, `review-submit` |
| Nav wishlist | `nav-wishlist` |

## Admin extras
| Element | data-testid |
|---------|-------------|
| Sales chart | `admin-sales-chart` |
| Cancel / refund | `admin-order-cancel`, `admin-order-refund` |
| Reviews / audit nav | `admin-nav-reviews`, `admin-nav-audit` |

## Suggested smoke flow (customer)

1. `[data-testid="nav-signin"]` → fill `auth-email` / `auth-password` → `auth-submit`
2. `nav-shop` → optional `shop-search` → click `product-card-{id}`
3. set `pdp-qty` → `pdp-add-to-cart` → `nav-cart` → `cart-checkout`
4. choose `checkout-pay-success`, fill address → `checkout-submit` → assert `order-success`

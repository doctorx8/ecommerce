# Karwan Postman collection

## Import
1. Open Postman → **Import**
2. Add:
   - `Karwan-API.postman_collection.json`
   - `Karwan-Local.postman_environment.json` (and/or Hostinger env)
3. Select environment **Karwan Local** (top-right)

For Hostinger, set `baseUrl` to `http://YOUR_VPS_IP:8080/api` (or your domain + `/api`).

## Token generators
In folder **01 Token Generators**:

| Request | Saves |
|---------|--------|
| Customer Login | `customer_token` |
| Admin Login | `admin_token` |
| Register Customer | `customer_token` (+ new email) |

Run login once; later folders auto-send `Authorization: Bearer …`.

## Suggested run order
1. **00 Health**
2. **01 Token Generators** → Customer Login + Admin Login
3. **02 Catalog**
4. **03 Guest Shop Workflow** (guest cart → checkout)
5. **05 Customer Shop Workflow** (logged-in cart → orders)
6. **04 Customer Account Workflow**
7. **06 Admin Workflow** (inventory, order status, coupons)

## Demo credentials
- Customer: `customer@store.local` / `password123`
- Admin: `admin@store.local` / `password123`
- Coupon: `WELCOME10`

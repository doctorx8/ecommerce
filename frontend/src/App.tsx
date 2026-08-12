import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { Layout } from './components/Layout'
import { StoreProvider } from './state/StoreContext'
import { HomePage } from './pages/HomePage'
import { ShopPage } from './pages/ShopPage'
import { ProductPage } from './pages/ProductPage'
import { CartPage } from './pages/CartPage'
import { CheckoutPage } from './pages/CheckoutPage'
import { AuthPage } from './pages/AuthPage'
import { AccountPage } from './pages/AccountPage'
import { OrderPage } from './pages/OrderPage'
import { AdminProvider } from './admin/AdminContext'
import { AdminLayout } from './admin/AdminLayout'
import { AdminLoginPage } from './admin/AdminLoginPage'
import { AdminDashboardPage } from './admin/AdminDashboardPage'
import { AdminProductsPage } from './admin/AdminProductsPage'
import { AdminOrdersPage } from './admin/AdminOrdersPage'
import { AdminCustomersPage } from './admin/AdminCustomersPage'
import { AdminCouponsPage } from './admin/AdminCouponsPage'

export default function App() {
  return (
    <StoreProvider>
      <AdminProvider>
        <BrowserRouter>
          <Routes>
            <Route path="/admin/login" element={<AdminLoginPage />} />
            <Route path="/admin" element={<AdminLayout />}>
              <Route index element={<AdminDashboardPage />} />
              <Route path="products" element={<AdminProductsPage />} />
              <Route path="orders" element={<AdminOrdersPage />} />
              <Route path="customers" element={<AdminCustomersPage />} />
              <Route path="coupons" element={<AdminCouponsPage />} />
            </Route>

            <Route element={<Layout />}>
              <Route index element={<HomePage />} />
              <Route path="shop" element={<ShopPage />} />
              <Route path="product/:slug" element={<ProductPage />} />
              <Route path="cart" element={<CartPage />} />
              <Route path="checkout" element={<CheckoutPage />} />
              <Route path="auth" element={<AuthPage />} />
              <Route path="account" element={<AccountPage />} />
              <Route path="order/:id" element={<OrderPage />} />
              <Route path="*" element={<Navigate to="/" replace />} />
            </Route>
          </Routes>
        </BrowserRouter>
      </AdminProvider>
    </StoreProvider>
  )
}

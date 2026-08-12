import { Navigate, NavLink, Outlet } from 'react-router-dom'
import { useAdmin } from './AdminContext'
import './admin.css'

export function AdminLayout() {
  const { admin, logout } = useAdmin()

  if (!admin) {
    return <Navigate to="/admin/login" replace />
  }

  return (
    <div className="admin-shell" data-testid="admin-shell" id="admin-shell">
      <aside className="admin-sidebar" data-testid="admin-sidebar" id="admin-sidebar">
        <div className="admin-brand" data-testid="admin-brand">
          KAR<span>WAN</span>
          <small>Admin</small>
        </div>
        <nav className="admin-nav" data-testid="admin-nav" id="admin-nav" aria-label="Admin">
          <NavLink to="/admin" end data-testid="admin-nav-overview" id="admin-nav-overview">
            Overview
          </NavLink>
          <NavLink to="/admin/products" data-testid="admin-nav-inventory" id="admin-nav-inventory">
            Inventory
          </NavLink>
          <NavLink to="/admin/orders" data-testid="admin-nav-orders" id="admin-nav-orders">
            Orders
          </NavLink>
          <NavLink to="/admin/customers" data-testid="admin-nav-customers" id="admin-nav-customers">
            Customers
          </NavLink>
          <NavLink to="/admin/coupons" data-testid="admin-nav-coupons" id="admin-nav-coupons">
            Coupons
          </NavLink>
        </nav>
        <div className="admin-sidebar-foot">
          <div className="admin-user" data-testid="admin-user">
            <strong>
              {admin.firstName} {admin.lastName}
            </strong>
            <span>{admin.email}</span>
          </div>
          <button
            type="button"
            className="admin-ghost"
            data-testid="admin-signout"
            id="admin-signout"
            onClick={logout}
          >
            Sign out
          </button>
          <a
            className="admin-store-link"
            href="/"
            target="_blank"
            rel="noreferrer"
            data-testid="admin-open-storefront"
          >
            Open storefront
          </a>
        </div>
      </aside>
      <div className="admin-main" data-testid="admin-main" id="admin-main">
        <Outlet />
      </div>
    </div>
  )
}

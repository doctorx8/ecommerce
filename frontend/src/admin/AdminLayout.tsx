import { Navigate, NavLink, Outlet } from 'react-router-dom'
import { useAdmin } from './AdminContext'
import './admin.css'

export function AdminLayout() {
  const { admin, logout } = useAdmin()

  if (!admin) {
    return <Navigate to="/admin/login" replace />
  }

  return (
    <div className="admin-shell">
      <aside className="admin-sidebar">
        <div className="admin-brand">
          KAR<span>WAN</span>
          <small>Admin</small>
        </div>
        <nav className="admin-nav">
          <NavLink to="/admin" end>
            Overview
          </NavLink>
          <NavLink to="/admin/products">Inventory</NavLink>
          <NavLink to="/admin/orders">Orders</NavLink>
          <NavLink to="/admin/customers">Customers</NavLink>
          <NavLink to="/admin/coupons">Coupons</NavLink>
        </nav>
        <div className="admin-sidebar-foot">
          <div className="admin-user">
            <strong>
              {admin.firstName} {admin.lastName}
            </strong>
            <span>{admin.email}</span>
          </div>
          <button type="button" className="admin-ghost" onClick={logout}>
            Sign out
          </button>
          <a className="admin-store-link" href="/" target="_blank" rel="noreferrer">
            Open storefront
          </a>
        </div>
      </aside>
      <div className="admin-main">
        <Outlet />
      </div>
    </div>
  )
}

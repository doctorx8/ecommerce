import { Link, NavLink, Outlet } from 'react-router-dom'
import { useStore } from '../state/StoreContext'

export function Layout() {
  const { cart, user, logout } = useStore()

  return (
    <div className="app-shell">
      <header className="site-header">
        <div className="container nav">
          <Link to="/" className="brand">
            NORTH<span>LINE</span>
          </Link>
          <nav className="nav-links">
            <NavLink to="/shop">Shop</NavLink>
            <NavLink to="/cart" className="cart-pill">
              Cart
              <span className="cart-count">{cart?.itemCount ?? 0}</span>
            </NavLink>
            {user ? (
              <>
                <NavLink to="/account">Account</NavLink>
                <button type="button" onClick={logout}>
                  Sign out
                </button>
              </>
            ) : (
              <NavLink to="/auth">Sign in</NavLink>
            )}
          </nav>
        </div>
      </header>

      <main className="main">
        <Outlet />
      </main>

      <footer className="site-footer">
        <div className="container footer-row">
          <div>
            <strong className="brand">NORTH<span>LINE</span></strong>
            <p className="muted" style={{ margin: '0.4rem 0 0' }}>
              Precision electronics, shipped with care.
            </p>
          </div>
          <div>© {new Date().getFullYear()} Northline Store</div>
        </div>
      </footer>
    </div>
  )
}

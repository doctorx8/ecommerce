import { Link, NavLink, Outlet } from 'react-router-dom'
import { useStore } from '../state/StoreContext'

export function Layout() {
  const { cart, user, logout } = useStore()

  return (
    <div className="app-shell" data-testid="storefront-shell" id="storefront-shell">
      <header className="site-header" data-testid="site-header" id="site-header">
        <div className="container nav">
          <Link to="/" className="brand" data-testid="nav-brand" id="nav-brand" aria-label="Karwan home">
            KAR<span>WAN</span>
          </Link>
          <nav className="nav-links" data-testid="nav-links" id="nav-links" aria-label="Main">
            <NavLink to="/shop" data-testid="nav-shop" id="nav-shop">
              Shop
            </NavLink>
            <NavLink to="/cart" className="cart-pill" data-testid="nav-cart" id="nav-cart">
              Cart
              <span className="cart-count" data-testid="cart-count" id="cart-count">
                {cart?.itemCount ?? 0}
              </span>
            </NavLink>
            {user ? (
              <>
                <NavLink to="/account" data-testid="nav-account" id="nav-account">
                  Account
                </NavLink>
                <button type="button" data-testid="nav-signout" id="nav-signout" onClick={logout}>
                  Sign out
                </button>
              </>
            ) : (
              <NavLink to="/auth" data-testid="nav-signin" id="nav-signin">
                Sign in
              </NavLink>
            )}
          </nav>
        </div>
      </header>

      <main className="main" data-testid="main-content" id="main-content">
        <Outlet />
      </main>

      <footer className="site-footer" data-testid="site-footer" id="site-footer">
        <div className="container footer-row">
          <div>
            <strong className="brand">
              KAR<span>WAN</span>
            </strong>
            <p className="muted" style={{ margin: '0.4rem 0 0' }}>
              Precision electronics, shipped with care.
            </p>
          </div>
          <div>© {new Date().getFullYear()} Karwan Store</div>
        </div>
      </footer>
    </div>
  )
}

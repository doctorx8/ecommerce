import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { Link, NavLink, Outlet, useLocation, useNavigate, useSearchParams } from 'react-router-dom'
import { api, type Category } from '../api/client'
import { useStore } from '../state/StoreContext'

/** Cyberport-style primary electronics nav (Karwan labels). */
const PRIMARY_NAV: { label: string; to: string; special?: boolean; testId: string }[] = [
  { label: 'Apple', to: '/shop?manufacturer=apple', testId: 'cat-nav-apple' },
  { label: 'Notebook', to: '/shop?category=notebook', testId: 'cat-nav-notebook' },
  { label: 'PC & Accessories', to: '/shop?category=pc-accessories', testId: 'cat-nav-pc' },
  { label: 'Gaming', to: '/shop?category=gaming', testId: 'cat-nav-gaming' },
  { label: 'Smartphone & Tablet', to: '/shop?category=smartphone-tablet', testId: 'cat-nav-phone-tablet' },
  { label: 'TV & Audio', to: '/shop?category=tv-audio', testId: 'cat-nav-tv-audio' },
  { label: 'Smart Home', to: '/shop?category=smart-home', testId: 'cat-nav-smart-home' },
  { label: 'Offers', to: '/shop?onSale=1', special: true, testId: 'cat-nav-offers' },
  { label: 'Outlet', to: '/shop?onSale=1&sort=price_asc', special: true, testId: 'cat-nav-outlet' },
]

function isShopNavActive(to: string, pathname: string, search: string) {
  if (pathname !== '/shop') return false
  const target = new URL(to, 'http://local').searchParams
  const current = new URLSearchParams(search)
  if ([...target.keys()].length === 0) return [...current.keys()].length === 0
  for (const [key, value] of target.entries()) {
    if (current.get(key) !== value) return false
  }
  return true
}

function IconUser() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <circle cx="12" cy="8" r="3.25" stroke="currentColor" strokeWidth="1.6" />
      <path
        d="M5.5 19.2c1.7-3.1 4-4.7 6.5-4.7s4.8 1.6 6.5 4.7"
        stroke="currentColor"
        strokeWidth="1.6"
        strokeLinecap="round"
      />
    </svg>
  )
}

function IconHeart() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path
        d="M12 19.4s-6.8-4.2-8.7-8.1C1.7 8.2 3.4 5 6.7 5c1.9 0 3.2 1.1 3.9 2.1C11.3 6.1 12.6 5 14.5 5c3.3 0 5 3.2 3.4 6.3-1.9 3.9-8.9 8.1-8.9 8.1Z"
        stroke="currentColor"
        strokeWidth="1.6"
        strokeLinejoin="round"
      />
    </svg>
  )
}

function IconCart() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path
        d="M3.5 5.5h1.7l1.4 10.2a1.5 1.5 0 0 0 1.5 1.3h9.2a1.5 1.5 0 0 0 1.5-1.2l1.2-6.3H7"
        stroke="currentColor"
        strokeWidth="1.6"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <circle cx="10" cy="19.5" r="1.2" fill="currentColor" />
      <circle cx="17" cy="19.5" r="1.2" fill="currentColor" />
    </svg>
  )
}

function IconSearch() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <circle cx="11" cy="11" r="6.5" stroke="currentColor" strokeWidth="1.6" />
      <path d="M16.5 16.5 20 20" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" />
    </svg>
  )
}

function IconMenu() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M4 7h16M4 12h16M4 17h16" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" />
    </svg>
  )
}

export function Layout() {
  const { cart, user } = useStore()
  const navigate = useNavigate()
  const location = useLocation()
  const [searchParams] = useSearchParams()
  const [query, setQuery] = useState(searchParams.get('q') || '')
  const [menuOpen, setMenuOpen] = useState(false)
  const [categories, setCategories] = useState<Category[]>([])
  const [brands, setBrands] = useState<{ id: number; name: string; slug: string }[]>([])

  useEffect(() => {
    setQuery(searchParams.get('q') || '')
  }, [searchParams])

  useEffect(() => {
    api.getCategories().then(setCategories).catch(() => setCategories([]))
    api.getManufacturers().then(setBrands).catch(() => setBrands([]))
  }, [])

  function onSearch(e: FormEvent) {
    e.preventDefault()
    const q = query.trim()
    navigate(q ? `/shop?q=${encodeURIComponent(q)}` : '/shop')
    setMenuOpen(false)
  }

  const flatCategories = categories.flatMap((c) => [c, ...(c.children || [])])

  return (
    <div className="app-shell" data-testid="storefront-shell" id="storefront-shell">
      <header className="site-header" data-testid="site-header" id="site-header">
        <div className="header-top">
          <div className="container header-top-inner">
            <Link to="/" className="brand" data-testid="nav-brand" id="nav-brand" aria-label="Karwan home">
              KAR<span>WAN</span>
            </Link>

            <form
              className="header-search"
              data-testid="header-search"
              id="header-search"
              role="search"
              onSubmit={onSearch}
            >
              <span className="header-search-icon" aria-hidden="true">
                <IconSearch />
              </span>
              <input
                id="nav-search"
                name="q"
                type="search"
                data-testid="nav-search"
                placeholder="What are you looking for?"
                aria-label="Search products"
                value={query}
                onChange={(e) => setQuery(e.target.value)}
              />
              <button type="submit" className="header-search-btn" data-testid="nav-search-submit" id="nav-search-submit">
                Search
              </button>
            </form>

            <nav className="header-icons" data-testid="nav-links" id="nav-links" aria-label="Account">
              <NavLink
                to={user ? '/account' : '/auth'}
                className="icon-link"
                data-testid={user ? 'nav-account' : 'nav-signin'}
                id={user ? 'nav-account' : 'nav-signin'}
                aria-label={user ? 'Account' : 'Sign in'}
                title={user ? 'Account' : 'Sign in'}
              >
                <IconUser />
                <span className="icon-label">{user ? 'Account' : 'Sign in'}</span>
              </NavLink>
              <NavLink
                to="/wishlist"
                className="icon-link"
                data-testid="nav-wishlist"
                id="nav-wishlist"
                aria-label="Wishlist"
                title="Wishlist"
              >
                <IconHeart />
                <span className="icon-label">Wishlist</span>
              </NavLink>
              <NavLink
                to="/cart"
                className="icon-link cart-pill"
                data-testid="nav-cart"
                id="nav-cart"
                aria-label="Cart"
                title="Cart"
              >
                <IconCart />
                <span className="icon-label">Cart</span>
                <span className="cart-count" data-testid="cart-count" id="cart-count">
                  {cart?.itemCount ?? 0}
                </span>
              </NavLink>
            </nav>
          </div>
        </div>

        <div className="header-cats" data-testid="category-nav" id="category-nav">
          <div className="container header-cats-inner">
            <button
              type="button"
              className="cat-menu-btn"
              data-testid="nav-menu-toggle"
              id="nav-menu-toggle"
              aria-expanded={menuOpen}
              aria-controls="all-categories-panel"
              onClick={() => setMenuOpen((o) => !o)}
            >
              <IconMenu />
              <span>All</span>
            </button>

            <div className="cat-scroll" data-testid="cat-nav-links" id="cat-nav-links">
              {PRIMARY_NAV.map((item) => {
                const active = isShopNavActive(item.to, location.pathname, location.search)
                return (
                  <Link
                    key={item.testId}
                    to={item.to}
                    className={`cat-link${item.special ? ' cat-link-special' : ''}${active ? ' is-active' : ''}`}
                    data-testid={item.testId}
                    id={item.testId}
                    onClick={() => setMenuOpen(false)}
                  >
                    {item.label}
                  </Link>
                )
              })}
            </div>

            <NavLink
              to="/shop"
              className="cat-shop-all"
              data-testid="nav-shop"
              id="nav-shop"
              onClick={() => setMenuOpen(false)}
            >
              Shop all
            </NavLink>
          </div>
        </div>

        {menuOpen ? (
          <div
            className="mega-panel"
            id="all-categories-panel"
            data-testid="all-categories-panel"
            role="dialog"
            aria-label="All categories"
          >
            <div className="container mega-grid">
              <div>
                <h3>Categories</h3>
                <ul>
                  {flatCategories.length === 0 ? (
                    <li className="muted">Loading…</li>
                  ) : (
                    flatCategories.map((c) => (
                      <li key={c.id}>
                        <Link
                          to={`/shop?category=${encodeURIComponent(c.slug)}`}
                          data-testid={`mega-cat-${c.slug}`}
                          onClick={() => setMenuOpen(false)}
                        >
                          {c.name}
                        </Link>
                      </li>
                    ))
                  )}
                </ul>
              </div>
              <div>
                <h3>Brands</h3>
                <ul>
                  {brands.map((b) => (
                    <li key={b.id}>
                      <Link
                        to={`/shop?manufacturer=${encodeURIComponent(b.slug)}`}
                        data-testid={`mega-brand-${b.slug}`}
                        onClick={() => setMenuOpen(false)}
                      >
                        {b.name}
                      </Link>
                    </li>
                  ))}
                </ul>
              </div>
              <div>
                <h3>Deals</h3>
                <ul>
                  <li>
                    <Link to="/shop?onSale=1" onClick={() => setMenuOpen(false)}>
                      Offers
                    </Link>
                  </li>
                  <li>
                    <Link to="/shop?featured=1" onClick={() => setMenuOpen(false)}>
                      Featured
                    </Link>
                  </li>
                  <li>
                    <Link to="/shop?inStock=1" onClick={() => setMenuOpen(false)}>
                      In stock
                    </Link>
                  </li>
                </ul>
              </div>
            </div>
          </div>
        ) : null}
      </header>

      {menuOpen ? (
        <button
          type="button"
          className="mega-backdrop"
          aria-label="Close menu"
          data-testid="nav-menu-backdrop"
          onClick={() => setMenuOpen(false)}
        />
      ) : null}

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

import { useState, type FormEvent } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { adminLogin, useAdmin } from './AdminContext'
import './admin.css'

export function AdminLoginPage() {
  const { admin, setAdmin } = useAdmin()
  const navigate = useNavigate()
  const [email, setEmail] = useState('admin@store.local')
  const [password, setPassword] = useState('password123')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  if (admin) return <Navigate to="/admin" replace />

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setBusy(true)
    setError('')
    try {
      await adminLogin(email, password, setAdmin)
      navigate('/admin', { replace: true })
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Login failed')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="admin-login" data-testid="admin-login-page" id="admin-login-page">
      <form
        className="admin-login-card"
        onSubmit={onSubmit}
        data-testid="admin-login-form"
        id="admin-login-form"
      >
        <div className="admin-brand">
          KAR<span>WAN</span>
          <small>Admin console</small>
        </div>
        <p className="admin-muted">Sign in to manage inventory, orders, and store data.</p>
        {error ? (
          <div className="admin-error" data-testid="admin-login-error" id="admin-login-error">
            {error}
          </div>
        ) : null}
        <label htmlFor="admin-email">
          Email
          <input
            id="admin-email"
            name="email"
            data-testid="admin-email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            type="email"
            required
          />
        </label>
        <label htmlFor="admin-password">
          Password
          <input
            id="admin-password"
            name="password"
            data-testid="admin-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            type="password"
            required
          />
        </label>
        <button
          type="submit"
          className="admin-btn"
          disabled={busy}
          data-testid="admin-login-submit"
          id="admin-login-submit"
        >
          {busy ? 'Signing in…' : 'Sign in'}
        </button>
      </form>
    </div>
  )
}

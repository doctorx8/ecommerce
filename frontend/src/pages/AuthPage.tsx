import { useState } from 'react'
import type { FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../api/client'
import { useStore } from '../state/StoreContext'

export function AuthPage() {
  const { setUser, refreshCart } = useStore()
  const navigate = useNavigate()
  const [mode, setMode] = useState<'login' | 'register'>('login')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const [form, setForm] = useState({
    email: 'customer@store.local',
    password: 'password123',
    firstName: 'Jane',
    lastName: 'Doe',
  })

  function setField(key: string, value: string) {
    setForm((prev) => ({ ...prev, [key]: value }))
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setBusy(true)
    setError('')
    try {
      const res =
        mode === 'login'
          ? await api.login(form.email, form.password)
          : await api.register({
              email: form.email,
              password: form.password,
              firstName: form.firstName,
              lastName: form.lastName,
            })
      setUser(res.customer, res.token)
      await refreshCart()
      navigate('/account')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Auth failed')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="page" data-testid="auth-page" id="auth-page">
      <div className="container">
        <h1 className="page-title" style={{ textAlign: 'center' }} data-testid="auth-title">
          {mode === 'login' ? 'Welcome back' : 'Create account'}
        </h1>
        <form className="auth-box" onSubmit={onSubmit} data-testid="auth-form" id="auth-form">
          <div className="tabs" data-testid="auth-tabs">
            <button
              type="button"
              className={mode === 'login' ? 'active' : ''}
              data-testid="auth-tab-login"
              id="auth-tab-login"
              onClick={() => setMode('login')}
            >
              Sign in
            </button>
            <button
              type="button"
              className={mode === 'register' ? 'active' : ''}
              data-testid="auth-tab-register"
              id="auth-tab-register"
              onClick={() => setMode('register')}
            >
              Register
            </button>
          </div>

          {mode === 'register' ? (
            <>
              <label htmlFor="auth-first-name">
                First name
                <input
                  id="auth-first-name"
                  name="firstName"
                  data-testid="auth-first-name"
                  required
                  value={form.firstName}
                  onChange={(e) => setField('firstName', e.target.value)}
                />
              </label>
              <label htmlFor="auth-last-name">
                Last name
                <input
                  id="auth-last-name"
                  name="lastName"
                  data-testid="auth-last-name"
                  required
                  value={form.lastName}
                  onChange={(e) => setField('lastName', e.target.value)}
                />
              </label>
            </>
          ) : null}

          <label htmlFor="auth-email">
            Email
            <input
              id="auth-email"
              name="email"
              data-testid="auth-email"
              required
              type="email"
              autoComplete="email"
              value={form.email}
              onChange={(e) => setField('email', e.target.value)}
            />
          </label>
          <label htmlFor="auth-password">
            Password
            <input
              id="auth-password"
              name="password"
              data-testid="auth-password"
              required
              type="password"
              minLength={8}
              autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
              value={form.password}
              onChange={(e) => setField('password', e.target.value)}
            />
          </label>

          {error ? (
            <div className="alert" data-testid="auth-error" id="auth-error">
              {error}
            </div>
          ) : null}
          <button
            className="btn btn-primary"
            type="submit"
            disabled={busy}
            data-testid="auth-submit"
            id="auth-submit"
          >
            {busy ? 'Please wait…' : mode === 'login' ? 'Sign in' : 'Create account'}
          </button>
        </form>
      </div>
    </div>
  )
}

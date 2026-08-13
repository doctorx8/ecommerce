import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { api } from '../api/client'
import { useStore } from '../state/StoreContext'

export function AuthPage() {
  const { setUser, refreshCart } = useStore()
  const navigate = useNavigate()
  const [params] = useSearchParams()
  const [mode, setMode] = useState<'login' | 'register' | 'forgot' | 'reset'>('login')
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [busy, setBusy] = useState(false)
  const [form, setForm] = useState({
    email: 'customer@store.local',
    password: 'password123',
    firstName: 'Jane',
    lastName: 'Doe',
    newPassword: '',
  })
  const [resetToken, setResetToken] = useState('')

  useEffect(() => {
    const token = params.get('resetToken')
    if (token) {
      setResetToken(token)
      setMode('reset')
    }
  }, [params])

  function setField(key: string, value: string) {
    setForm((prev) => ({ ...prev, [key]: value }))
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setBusy(true)
    setError('')
    setMessage('')
    try {
      if (mode === 'forgot') {
        const res = await api.forgotPassword(form.email)
        setMessage(res.message)
        return
      }
      if (mode === 'reset') {
        await api.resetPassword(resetToken, form.newPassword)
        setMessage('Password updated. You can sign in.')
        setMode('login')
        return
      }
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
          {mode === 'login'
            ? 'Welcome back'
            : mode === 'register'
              ? 'Create account'
              : mode === 'forgot'
                ? 'Reset password'
                : 'Choose a new password'}
        </h1>
        <form className="auth-box" onSubmit={onSubmit} data-testid="auth-form" id="auth-form">
          {mode === 'login' || mode === 'register' ? (
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
          ) : null}

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

          {mode !== 'reset' ? (
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
          ) : null}

          {mode === 'login' || mode === 'register' ? (
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
          ) : null}

          {mode === 'reset' ? (
            <label htmlFor="auth-new-password">
              New password
              <input
                id="auth-new-password"
                name="newPassword"
                data-testid="auth-new-password"
                required
                type="password"
                minLength={8}
                value={form.newPassword}
                onChange={(e) => setField('newPassword', e.target.value)}
              />
            </label>
          ) : null}

          {error ? (
            <div className="alert" data-testid="auth-error" id="auth-error">
              {error}
            </div>
          ) : null}
          {message ? (
            <div className="success" data-testid="auth-message" id="auth-message">
              {message}
            </div>
          ) : null}

          <button
            className="btn btn-primary"
            type="submit"
            disabled={busy}
            data-testid="auth-submit"
            id="auth-submit"
          >
            {busy
              ? 'Please wait…'
              : mode === 'login'
                ? 'Sign in'
                : mode === 'register'
                  ? 'Create account'
                  : mode === 'forgot'
                    ? 'Send reset link'
                    : 'Update password'}
          </button>

          {mode === 'login' ? (
            <button
              type="button"
              className="linkish"
              data-testid="auth-forgot-link"
              id="auth-forgot-link"
              onClick={() => setMode('forgot')}
            >
              Forgot password?
            </button>
          ) : null}
          {mode === 'forgot' || mode === 'reset' ? (
            <button type="button" className="linkish" data-testid="auth-back-login" onClick={() => setMode('login')}>
              Back to sign in
            </button>
          ) : null}
        </form>
      </div>
    </div>
  )
}

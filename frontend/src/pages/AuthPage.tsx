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
    <div className="page">
      <div className="container">
        <h1 className="page-title" style={{ textAlign: 'center' }}>
          {mode === 'login' ? 'Welcome back' : 'Create account'}
        </h1>
        <form className="auth-box" onSubmit={onSubmit}>
          <div className="tabs">
            <button type="button" className={mode === 'login' ? 'active' : ''} onClick={() => setMode('login')}>
              Sign in
            </button>
            <button
              type="button"
              className={mode === 'register' ? 'active' : ''}
              onClick={() => setMode('register')}
            >
              Register
            </button>
          </div>

          {mode === 'register' ? (
            <>
              <label>
                First name
                <input required value={form.firstName} onChange={(e) => setField('firstName', e.target.value)} />
              </label>
              <label>
                Last name
                <input required value={form.lastName} onChange={(e) => setField('lastName', e.target.value)} />
              </label>
            </>
          ) : null}

          <label>
            Email
            <input required type="email" value={form.email} onChange={(e) => setField('email', e.target.value)} />
          </label>
          <label>
            Password
            <input
              required
              type="password"
              minLength={8}
              value={form.password}
              onChange={(e) => setField('password', e.target.value)}
            />
          </label>

          {error ? <div className="alert">{error}</div> : null}
          <button className="btn btn-primary" type="submit" disabled={busy}>
            {busy ? 'Please wait…' : mode === 'login' ? 'Sign in' : 'Create account'}
          </button>
        </form>
      </div>
    </div>
  )
}

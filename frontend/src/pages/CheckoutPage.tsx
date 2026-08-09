import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { api, money } from '../api/client'
import { useStore } from '../state/StoreContext'

export function CheckoutPage() {
  const { cart, user, refreshCart } = useStore()
  const navigate = useNavigate()
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const [form, setForm] = useState({
    email: user?.email ?? '',
    firstName: user?.firstName ?? '',
    lastName: user?.lastName ?? '',
    telephone: '',
    address1: '',
    city: '',
    postcode: '',
    country: 'US',
    zone: '',
    couponCode: 'WELCOME10',
  })

  if (!cart || cart.items.length === 0) {
    return (
      <div className="page container">
        <h1 className="page-title">Checkout</h1>
        <div className="empty">
          Nothing to checkout. <Link to="/shop">Shop products</Link>
        </div>
      </div>
    )
  }

  function setField(key: string, value: string) {
    setForm((prev) => ({ ...prev, [key]: value }))
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setBusy(true)
    setError('')
    try {
      const order = await api.checkout({
        email: form.email,
        firstName: form.firstName,
        lastName: form.lastName,
        telephone: form.telephone,
        couponCode: form.couponCode || undefined,
        shipping: {
          firstName: form.firstName,
          lastName: form.lastName,
          address1: form.address1,
          city: form.city,
          postcode: form.postcode,
          country: form.country,
          zone: form.zone,
        },
      })
      await refreshCart()
      navigate(`/order/${order.id}`, { state: { order } })
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Checkout failed')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="page">
      <div className="container">
        <h1 className="page-title">Checkout</h1>
        <form className="checkout-grid" onSubmit={onSubmit}>
          <div className="form-grid">
            <label className="full">
              Email
              <input required type="email" value={form.email} onChange={(e) => setField('email', e.target.value)} />
            </label>
            <label>
              First name
              <input required value={form.firstName} onChange={(e) => setField('firstName', e.target.value)} />
            </label>
            <label>
              Last name
              <input required value={form.lastName} onChange={(e) => setField('lastName', e.target.value)} />
            </label>
            <label className="full">
              Phone
              <input value={form.telephone} onChange={(e) => setField('telephone', e.target.value)} />
            </label>
            <label className="full">
              Address
              <input required value={form.address1} onChange={(e) => setField('address1', e.target.value)} />
            </label>
            <label>
              City
              <input required value={form.city} onChange={(e) => setField('city', e.target.value)} />
            </label>
            <label>
              Postcode
              <input required value={form.postcode} onChange={(e) => setField('postcode', e.target.value)} />
            </label>
            <label>
              Country
              <input required value={form.country} onChange={(e) => setField('country', e.target.value)} />
            </label>
            <label>
              State / Zone
              <input value={form.zone} onChange={(e) => setField('zone', e.target.value)} />
            </label>
            <label className="full">
              Coupon
              <input value={form.couponCode} onChange={(e) => setField('couponCode', e.target.value)} />
            </label>
            {error ? <div className="alert full">{error}</div> : null}
            <div className="full">
              <button className="btn btn-primary" type="submit" disabled={busy}>
                {busy ? 'Placing order…' : 'Place order'}
              </button>
            </div>
          </div>

          <aside className="summary">
            <h3 style={{ marginTop: 0, fontFamily: 'var(--font-display)' }}>Order summary</h3>
            {cart.items.map((item) => (
              <div className="summary-row" key={item.id}>
                <span>
                  {item.product.name} × {item.quantity}
                </span>
                <span>{money(Number(item.product.price) * item.quantity)}</span>
              </div>
            ))}
            <div className="summary-row total">
              <span>Subtotal</span>
              <span>{money(cart.subtotal)}</span>
            </div>
          </aside>
        </form>
      </div>
    </div>
  )
}

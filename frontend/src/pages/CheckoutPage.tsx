import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { api, money } from '../api/client'
import { useStore } from '../state/StoreContext'

type Quote = {
  subtotal: number | string
  discountTotal: number | string
  shippingCost: number | string
  taxTotal: number | string
  total: number | string
}

export function CheckoutPage() {
  const { cart, user, refreshCart } = useStore()
  const navigate = useNavigate()
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const [quote, setQuote] = useState<Quote | null>(null)
  const [paymentOutcome, setPaymentOutcome] = useState<'SUCCESS' | 'FAIL' | 'PENDING'>('SUCCESS')
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

  useEffect(() => {
    if (!cart || cart.items.length === 0) return
    api
      .quote(form.couponCode || undefined)
      .then(setQuote)
      .catch(() => setQuote(null))
  }, [cart, form.couponCode])

  if (!cart || cart.items.length === 0) {
    return (
      <div className="page container" data-testid="checkout-page" id="checkout-page">
        <h1 className="page-title">Checkout</h1>
        <div className="empty" data-testid="checkout-empty">
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
        paymentMethod: 'mock_card',
        paymentOutcome,
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
    <div className="page" data-testid="checkout-page" id="checkout-page">
      <div className="container">
        <h1 className="page-title" data-testid="checkout-title">
          Checkout
        </h1>
        <form className="checkout-grid" onSubmit={onSubmit} data-testid="checkout-form" id="checkout-form">
          <div className="form-grid">
            <label className="full" htmlFor="checkout-email">
              Email
              <input
                id="checkout-email"
                name="email"
                data-testid="checkout-email"
                required
                type="email"
                value={form.email}
                onChange={(e) => setField('email', e.target.value)}
              />
            </label>
            <label htmlFor="checkout-first-name">
              First name
              <input
                id="checkout-first-name"
                name="firstName"
                data-testid="checkout-first-name"
                required
                value={form.firstName}
                onChange={(e) => setField('firstName', e.target.value)}
              />
            </label>
            <label htmlFor="checkout-last-name">
              Last name
              <input
                id="checkout-last-name"
                name="lastName"
                data-testid="checkout-last-name"
                required
                value={form.lastName}
                onChange={(e) => setField('lastName', e.target.value)}
              />
            </label>
            <label className="full" htmlFor="checkout-phone">
              Phone
              <input
                id="checkout-phone"
                name="telephone"
                data-testid="checkout-phone"
                value={form.telephone}
                onChange={(e) => setField('telephone', e.target.value)}
              />
            </label>
            <label className="full" htmlFor="checkout-address1">
              Address
              <input
                id="checkout-address1"
                name="address1"
                data-testid="checkout-address1"
                required
                value={form.address1}
                onChange={(e) => setField('address1', e.target.value)}
              />
            </label>
            <label htmlFor="checkout-city">
              City
              <input
                id="checkout-city"
                name="city"
                data-testid="checkout-city"
                required
                value={form.city}
                onChange={(e) => setField('city', e.target.value)}
              />
            </label>
            <label htmlFor="checkout-postcode">
              Postcode
              <input
                id="checkout-postcode"
                name="postcode"
                data-testid="checkout-postcode"
                required
                value={form.postcode}
                onChange={(e) => setField('postcode', e.target.value)}
              />
            </label>
            <label htmlFor="checkout-country">
              Country
              <input
                id="checkout-country"
                name="country"
                data-testid="checkout-country"
                required
                value={form.country}
                onChange={(e) => setField('country', e.target.value)}
              />
            </label>
            <label htmlFor="checkout-zone">
              State / Zone
              <input
                id="checkout-zone"
                name="zone"
                data-testid="checkout-zone"
                value={form.zone}
                onChange={(e) => setField('zone', e.target.value)}
              />
            </label>
            <label className="full" htmlFor="checkout-coupon">
              Coupon
              <input
                id="checkout-coupon"
                name="couponCode"
                data-testid="checkout-coupon"
                value={form.couponCode}
                onChange={(e) => setField('couponCode', e.target.value)}
              />
            </label>

            <fieldset className="full" data-testid="checkout-payment" id="checkout-payment">
              <legend>Mock payment</legend>
              <p className="muted" style={{ marginTop: 0 }}>
                Sandbox only — no real card charge.
              </p>
              {(['SUCCESS', 'PENDING', 'FAIL'] as const).map((outcome) => (
                <label key={outcome} className="checkbox-row" htmlFor={`pay-${outcome}`}>
                  <input
                    id={`pay-${outcome}`}
                    data-testid={`checkout-pay-${outcome.toLowerCase()}`}
                    type="radio"
                    name="paymentOutcome"
                    checked={paymentOutcome === outcome}
                    onChange={() => setPaymentOutcome(outcome)}
                  />
                  {outcome}
                </label>
              ))}
            </fieldset>

            {error ? (
              <div className="alert full" data-testid="checkout-error" id="checkout-error">
                {error}
              </div>
            ) : null}
            <div className="full">
              <button
                className="btn btn-primary"
                type="submit"
                disabled={busy}
                data-testid="checkout-submit"
                id="checkout-submit"
              >
                {busy ? 'Placing order…' : 'Place order'}
              </button>
            </div>
          </div>

          <aside className="summary" data-testid="checkout-summary" id="checkout-summary">
            <h3 style={{ marginTop: 0, fontFamily: 'var(--font-display)' }}>Order summary</h3>
            {cart.items.map((item) => (
              <div className="summary-row" key={item.id} data-testid={`checkout-summary-item-${item.id}`}>
                <span>
                  {item.product.name} × {item.quantity}
                </span>
                <span>{money(Number(item.product.price) * item.quantity)}</span>
              </div>
            ))}
            <div className="summary-row">
              <span>Subtotal</span>
              <span data-testid="checkout-subtotal">{money(quote?.subtotal ?? cart.subtotal)}</span>
            </div>
            <div className="summary-row">
              <span>Discount</span>
              <span data-testid="checkout-discount">-{money(quote?.discountTotal ?? 0)}</span>
            </div>
            <div className="summary-row">
              <span>Shipping</span>
              <span data-testid="checkout-shipping">{money(quote?.shippingCost ?? 0)}</span>
            </div>
            <div className="summary-row">
              <span>Tax</span>
              <span data-testid="checkout-tax">{money(quote?.taxTotal ?? 0)}</span>
            </div>
            <div className="summary-row total">
              <span>Total</span>
              <span data-testid="checkout-total">{money(quote?.total ?? cart.subtotal)}</span>
            </div>
          </aside>
        </form>
      </div>
    </div>
  )
}

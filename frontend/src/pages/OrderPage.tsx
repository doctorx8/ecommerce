import { useEffect, useState } from 'react'
import { Link, useLocation, useParams } from 'react-router-dom'
import { api, money, type Order } from '../api/client'

export function OrderPage() {
  const { id } = useParams()
  const location = useLocation()
  const [order, setOrder] = useState<Order | null>((location.state as { order?: Order })?.order ?? null)
  const [error, setError] = useState('')

  useEffect(() => {
    if (order || !id) return
    // Guest checkout returns order in navigation state; authenticated users can refetch later.
    api
      .myOrders()
      .then((orders) => {
        const found = orders.find((o) => String(o.id) === id)
        if (found) setOrder(found)
        else setError('Order details unavailable. Sign in if this was your account order.')
      })
      .catch(() => setError('Order details unavailable for this session.'))
  }, [id, order])

  if (error && !order) {
    return (
      <div className="page container">
        <div className="alert">{error}</div>
        <Link to="/shop">Back to shop</Link>
      </div>
    )
  }

  if (!order) {
    return (
      <div className="page container">
        <p className="muted">Loading order…</p>
      </div>
    )
  }

  return (
    <div className="page">
      <div className="container" style={{ maxWidth: 720 }}>
        <div className="success">Order placed successfully.</div>
        <h1 className="page-title" style={{ marginTop: '1rem' }}>
          {order.orderNumber}
        </h1>
        <p className="muted">Status: {order.status}</p>

        <div className="panel" style={{ marginTop: '1.5rem' }}>
          {order.items.map((item) => (
            <div className="summary-row" key={item.id}>
              <span>
                {item.name} × {item.quantity}
              </span>
              <span>{money(item.total)}</span>
            </div>
          ))}
          <div className="summary-row">
            <span>Subtotal</span>
            <span>{money(order.subtotal)}</span>
          </div>
          <div className="summary-row">
            <span>Discount</span>
            <span>-{money(order.discountTotal)}</span>
          </div>
          <div className="summary-row total">
            <span>Total</span>
            <span>{money(order.total)}</span>
          </div>
        </div>

        <div style={{ marginTop: '1.5rem' }}>
          <Link className="btn btn-primary" to="/shop">
            Continue shopping
          </Link>
        </div>
      </div>
    </div>
  )
}

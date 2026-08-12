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
      <div className="page container" data-testid="order-page" id="order-page">
        <div className="alert" data-testid="order-error" id="order-error">
          {error}
        </div>
        <Link to="/shop" data-testid="order-back-shop">
          Back to shop
        </Link>
      </div>
    )
  }

  if (!order) {
    return (
      <div className="page container" data-testid="order-loading">
        <p className="muted">Loading order…</p>
      </div>
    )
  }

  return (
    <div className="page" data-testid="order-page" id="order-page" data-order-id={order.id}>
      <div className="container" style={{ maxWidth: 720 }}>
        <div className="success" data-testid="order-success" id="order-success">
          Order placed successfully.
        </div>
        <h1 className="page-title" style={{ marginTop: '1rem' }} data-testid="order-number" id="order-number">
          {order.orderNumber}
        </h1>
        <p className="muted" data-testid="order-status" id="order-status">
          Status: {order.status}
        </p>

        <div className="panel" style={{ marginTop: '1.5rem' }} data-testid="order-items" id="order-items">
          {order.items.map((item) => (
            <div className="summary-row" key={item.id} data-testid={`order-item-${item.id}`}>
              <span>
                {item.name} × {item.quantity}
              </span>
              <span>{money(item.total)}</span>
            </div>
          ))}
          <div className="summary-row">
            <span>Subtotal</span>
            <span data-testid="order-subtotal">{money(order.subtotal)}</span>
          </div>
          <div className="summary-row">
            <span>Discount</span>
            <span data-testid="order-discount">-{money(order.discountTotal)}</span>
          </div>
          <div className="summary-row total">
            <span>Total</span>
            <span data-testid="order-total" id="order-total">
              {money(order.total)}
            </span>
          </div>
        </div>

        <div style={{ marginTop: '1.5rem' }}>
          <Link className="btn btn-primary" to="/shop" data-testid="order-continue-shopping" id="order-continue-shopping">
            Continue shopping
          </Link>
        </div>
      </div>
    </div>
  )
}

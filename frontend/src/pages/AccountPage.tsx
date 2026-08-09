import { useEffect, useState } from 'react'
import { Link, Navigate } from 'react-router-dom'
import { api, money, type Order } from '../api/client'
import { useStore } from '../state/StoreContext'

export function AccountPage() {
  const { user } = useStore()
  const [orders, setOrders] = useState<Order[]>([])
  const [error, setError] = useState('')

  useEffect(() => {
    if (!user) return
    api
      .myOrders()
      .then(setOrders)
      .catch((err: Error) => setError(err.message))
  }, [user])

  if (!user) return <Navigate to="/auth" replace />

  return (
    <div className="page">
      <div className="container">
        <h1 className="page-title">Account</h1>
        <p className="muted">
          Signed in as {user.firstName} {user.lastName} ({user.email})
        </p>

        <h2 style={{ fontFamily: 'var(--font-display)', marginTop: '2rem' }}>Orders</h2>
        {error ? <div className="alert">{error}</div> : null}
        {orders.length === 0 ? (
          <div className="empty">No orders yet. <Link to="/shop">Start shopping</Link></div>
        ) : (
          <div className="order-list panel">
            {orders.map((order) => (
              <div className="order-row" key={order.id} style={{ gridTemplateColumns: '1fr auto' }}>
                <div>
                  <Link to={`/order/${order.id}`}>
                    <strong>{order.orderNumber}</strong>
                  </Link>
                  <div className="muted">{order.status}</div>
                </div>
                <div>{money(order.total)}</div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

import { useEffect, useMemo, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import {
  ORDER_STATUSES,
  PAYMENT_STATUSES,
  adminApi,
  formatDate,
  money,
  type AdminOrder,
} from './adminApi'

export function AdminOrdersPage() {
  const [params, setParams] = useSearchParams()
  const [items, setItems] = useState<AdminOrder[]>([])
  const [selected, setSelected] = useState<AdminOrder | null>(null)
  const [error, setError] = useState('')
  const [comment, setComment] = useState('')
  const [status, setStatus] = useState('')
  const [paymentStatus, setPaymentStatus] = useState('')
  const [busy, setBusy] = useState(false)

  const statusFilter = params.get('status') ?? ''
  const focusId = params.get('focus')

  const query = useMemo(
    () => ({
      status: statusFilter || undefined,
      limit: 50,
    }),
    [statusFilter],
  )

  async function load() {
    try {
      const res = await adminApi.orders(query)
      setItems(res.items)
      setError('')
      if (focusId) {
        const found = res.items.find((o) => String(o.id) === focusId)
        if (found) selectOrder(found)
        else {
          const detail = await adminApi.order(Number(focusId))
          selectOrder(detail)
        }
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load orders')
    }
  }

  useEffect(() => {
    load()
  }, [query])

  function selectOrder(order: AdminOrder) {
    setSelected(order)
    setStatus(order.status)
    setPaymentStatus(order.paymentStatus)
    setComment('')
  }

  async function save() {
    if (!selected) return
    setBusy(true)
    try {
      const updated = await adminApi.updateOrder(selected.id, {
        status: status !== selected.status ? status : undefined,
        paymentStatus: paymentStatus !== selected.paymentStatus ? paymentStatus : undefined,
        comment: comment || undefined,
      })
      setSelected(updated)
      setItems((prev) => prev.map((o) => (o.id === updated.id ? updated : o)))
      setComment('')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Update failed')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="admin-page" data-testid="admin-orders-page" id="admin-orders-page">
      <header className="admin-page-head">
        <div>
          <h1>Orders</h1>
          <p className="admin-muted">Track packages and update fulfillment status.</p>
        </div>
      </header>

      <div className="admin-filters">
        <button
          type="button"
          className={`admin-chip ${!statusFilter ? 'on' : ''}`}
          onClick={() => setParams({})}
        >
          All
        </button>
        {ORDER_STATUSES.map((s) => (
          <button
            key={s}
            type="button"
            className={`admin-chip ${statusFilter === s ? 'on' : ''}`}
            onClick={() => setParams({ status: s })}
          >
            {s}
          </button>
        ))}
      </div>

      {error ? <div className="admin-error">{error}</div> : null}

      <div className="admin-split wide">
        <section className="admin-panel">
          <table className="admin-table">
            <thead>
              <tr>
                <th>Order</th>
                <th>Customer</th>
                <th>Status</th>
                <th>Total</th>
                <th>Date</th>
              </tr>
            </thead>
            <tbody>
              {items.map((o) => (
                <tr
                  key={o.id}
                  className={selected?.id === o.id ? 'selected' : ''}
                  data-testid={`admin-order-row-${o.id}`}
                  data-order-id={o.id}
                  onClick={() => selectOrder(o)}
                >
                  <td>{o.orderNumber}</td>
                  <td>
                    {o.firstName} {o.lastName}
                    <div className="admin-muted">{o.email}</div>
                  </td>
                  <td>
                    <span className={`badge status-${o.status.toLowerCase()}`}>{o.status}</span>
                  </td>
                  <td>{money(o.total)}</td>
                  <td>{formatDate(o.createdAt)}</td>
                </tr>
              ))}
            </tbody>
          </table>
          {items.length === 0 ? <p className="admin-muted">No orders yet.</p> : null}
        </section>

        <section className="admin-panel">
          {!selected ? (
            <p className="admin-muted">Select an order to update package status.</p>
          ) : (
            <>
              <div className="admin-panel-head">
                <h2>{selected.orderNumber}</h2>
                <span className="admin-muted">{formatDate(selected.createdAt)}</span>
              </div>

              <div className="admin-detail-grid">
                <div>
                  <h3>Customer</h3>
                  <p>
                    {selected.firstName} {selected.lastName}
                    <br />
                    {selected.email}
                    <br />
                    {selected.telephone || 'No phone'}
                  </p>
                </div>
                <div>
                  <h3>Shipping</h3>
                  {selected.shipping ? (
                    <p>
                      {selected.shipping.firstName} {selected.shipping.lastName}
                      <br />
                      {selected.shipping.address1}
                      <br />
                      {selected.shipping.city}, {selected.shipping.postcode}
                      <br />
                      {selected.shipping.country}
                    </p>
                  ) : (
                    <p className="admin-muted">No shipping data</p>
                  )}
                </div>
              </div>

              <h3>Items</h3>
              <table className="admin-table compact">
                <thead>
                  <tr>
                    <th>Item</th>
                    <th>Qty</th>
                    <th>Total</th>
                  </tr>
                </thead>
                <tbody>
                  {selected.items.map((item) => (
                    <tr key={item.id}>
                      <td>
                        {item.name}
                        <div className="admin-muted">{item.sku}</div>
                      </td>
                      <td>{item.quantity}</td>
                      <td>{money(item.total)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>

              <div className="admin-totals">
                <div>
                  <span>Subtotal</span>
                  <strong>{money(selected.subtotal)}</strong>
                </div>
                <div>
                  <span>Shipping</span>
                  <strong>{money(selected.shippingCost)}</strong>
                </div>
                <div>
                  <span>Discount</span>
                  <strong>{money(selected.discountTotal)}</strong>
                </div>
                <div>
                  <span>Total</span>
                  <strong>{money(selected.total)}</strong>
                </div>
              </div>

              <div className="admin-form-row">
                <label htmlFor="admin-order-status">
                  Package status
                  <select
                    id="admin-order-status"
                    data-testid="admin-order-status"
                    value={status}
                    onChange={(e) => setStatus(e.target.value)}
                  >
                    {ORDER_STATUSES.map((s) => (
                      <option key={s} value={s}>
                        {s}
                      </option>
                    ))}
                  </select>
                </label>
                <label htmlFor="admin-payment-status">
                  Payment status
                  <select
                    id="admin-payment-status"
                    data-testid="admin-payment-status"
                    value={paymentStatus}
                    onChange={(e) => setPaymentStatus(e.target.value)}
                  >
                    {PAYMENT_STATUSES.map((s) => (
                      <option key={s} value={s}>
                        {s}
                      </option>
                    ))}
                  </select>
                </label>
              </div>

              <label htmlFor="admin-order-comment">
                Status comment
                <textarea
                  id="admin-order-comment"
                  data-testid="admin-order-comment"
                  rows={3}
                  value={comment}
                  onChange={(e) => setComment(e.target.value)}
                  placeholder="Optional note for order history"
                />
              </label>

              <div className="admin-actions">
                <button
                  type="button"
                  className="admin-btn"
                  data-testid="admin-order-update"
                  id="admin-order-update"
                  disabled={busy}
                  onClick={save}
                >
                  {busy ? 'Saving…' : 'Update order'}
                </button>
                <button
                  type="button"
                  className="admin-ghost"
                  data-testid="admin-order-cancel"
                  id="admin-order-cancel"
                  disabled={busy}
                  onClick={async () => {
                    if (!selected || !confirm('Cancel this order?')) return
                    setBusy(true)
                    try {
                      const updated = await adminApi.cancelOrder(selected.id, comment || 'Cancelled by admin')
                      setSelected(updated)
                      setItems((prev) => prev.map((o) => (o.id === updated.id ? updated : o)))
                    } catch (err) {
                      setError(err instanceof Error ? err.message : 'Cancel failed')
                    } finally {
                      setBusy(false)
                    }
                  }}
                >
                  Cancel order
                </button>
                <button
                  type="button"
                  className="admin-ghost danger"
                  data-testid="admin-order-refund"
                  id="admin-order-refund"
                  disabled={busy}
                  onClick={async () => {
                    if (!selected || !confirm('Refund this order?')) return
                    setBusy(true)
                    try {
                      const updated = await adminApi.refundOrder(selected.id, comment || 'Refunded by admin')
                      setSelected(updated)
                      setItems((prev) => prev.map((o) => (o.id === updated.id ? updated : o)))
                    } catch (err) {
                      setError(err instanceof Error ? err.message : 'Refund failed')
                    } finally {
                      setBusy(false)
                    }
                  }}
                >
                  Refund
                </button>
              </div>

              {selected.history && selected.history.length > 0 ? (
                <>
                  <h3>History</h3>
                  <ul className="admin-history">
                    {selected.history.map((h) => (
                      <li key={h.id}>
                        <strong>{h.status}</strong>
                        <span>{formatDate(h.createdAt)}</span>
                        {h.comment ? <p>{h.comment}</p> : null}
                      </li>
                    ))}
                  </ul>
                </>
              ) : null}
            </>
          )}
        </section>
      </div>
    </div>
  )
}

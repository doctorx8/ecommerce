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
    <div className="admin-page">
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
                <label>
                  Package status
                  <select value={status} onChange={(e) => setStatus(e.target.value)}>
                    {ORDER_STATUSES.map((s) => (
                      <option key={s} value={s}>
                        {s}
                      </option>
                    ))}
                  </select>
                </label>
                <label>
                  Payment status
                  <select value={paymentStatus} onChange={(e) => setPaymentStatus(e.target.value)}>
                    {PAYMENT_STATUSES.map((s) => (
                      <option key={s} value={s}>
                        {s}
                      </option>
                    ))}
                  </select>
                </label>
              </div>

              <label>
                Status comment
                <textarea
                  rows={3}
                  value={comment}
                  onChange={(e) => setComment(e.target.value)}
                  placeholder="Optional note for order history"
                />
              </label>

              <button type="button" className="admin-btn" disabled={busy} onClick={save}>
                {busy ? 'Saving…' : 'Update order'}
              </button>

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

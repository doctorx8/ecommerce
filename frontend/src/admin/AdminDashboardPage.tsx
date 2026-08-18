import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { adminApi, formatDate, money, type AdminOverview } from './adminApi'

export function AdminDashboardPage() {
  const [data, setData] = useState<AdminOverview | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    adminApi
      .overview()
      .then(setData)
      .catch((err) => setError(err instanceof Error ? err.message : 'Failed to load'))
  }, [])

  if (error) return <div className="admin-error">{error}</div>
  if (!data) return <div className="admin-muted">Loading overview…</div>

  const stats = [
    { label: 'Visitors today', value: String(data.visitorsToday ?? 0), testId: 'admin-stat-visitors-today' },
    { label: 'Page views today', value: String(data.pageViewsToday ?? 0), testId: 'admin-stat-pageviews-today' },
    { label: 'Visitors total', value: String(data.visitorsTotal ?? 0), testId: 'admin-stat-visitors-total' },
    { label: 'Revenue', value: money(data.revenue) },
    { label: 'Orders', value: String(data.orders) },
    { label: 'Products', value: `${data.activeProducts}/${data.products}` },
    { label: 'Low stock', value: String(data.lowStock) },
    { label: 'Customers', value: String(data.customers) },
    { label: 'Coupons', value: String(data.coupons) },
  ]

  return (
    <div className="admin-page" data-testid="admin-overview" id="admin-overview">
      <header className="admin-page-head">
        <div>
          <h1>Overview</h1>
          <p className="admin-muted">Store health, website visitors, stock alerts, and recent packages.</p>
        </div>
      </header>

      <div className="admin-stat-grid" data-testid="admin-visitor-stats" id="admin-visitor-stats">
        {stats.map((s) => (
          <div key={s.label} className="admin-stat" data-testid={s.testId}>
            <span>{s.label}</span>
            <strong>{s.value}</strong>
          </div>
        ))}
      </div>

      <div className="admin-split">
        <section className="admin-panel" data-testid="admin-sales-chart" id="admin-sales-chart">
          <div className="admin-panel-head">
            <h2>Sales (14 days)</h2>
          </div>
          {(data.salesOverTime?.length ?? 0) === 0 ? (
            <p className="admin-muted">No sales in the last 14 days.</p>
          ) : (
            <div className="admin-chart">
              {data.salesOverTime!.map((point) => {
                const max = Math.max(
                  ...data.salesOverTime!.map((p) => Number(p.revenue) || 0),
                  1,
                )
                const height = Math.max((Number(point.revenue) / max) * 120, 4)
                return (
                  <div key={point.date} className="admin-chart-bar" title={`${point.date}: ${money(point.revenue)}`}>
                    <div className="admin-chart-fill" style={{ height }} data-testid={`sales-bar-${point.date}`} />
                    <span>{String(point.date).slice(5)}</span>
                  </div>
                )
              })}
            </div>
          )}
        </section>

        <section className="admin-panel" data-testid="admin-visitors-chart" id="admin-visitors-chart">
          <div className="admin-panel-head">
            <h2>Website visitors (14 days)</h2>
          </div>
          {(data.visitorsOverTime?.length ?? 0) === 0 ? (
            <p className="admin-muted">No visitor data yet. Browse the storefront to start counting.</p>
          ) : (
            <div className="admin-chart">
              {data.visitorsOverTime!.map((point) => {
                const max = Math.max(
                  ...data.visitorsOverTime!.map((p) => Number(p.visitors) || 0),
                  1,
                )
                const height = Math.max((Number(point.visitors) / max) * 120, 4)
                return (
                  <div
                    key={point.date}
                    className="admin-chart-bar"
                    title={`${point.date}: ${point.visitors} visitors / ${point.pageViews} views`}
                  >
                    <div
                      className="admin-chart-fill admin-chart-fill-visitors"
                      style={{ height }}
                      data-testid={`visitors-bar-${point.date}`}
                    />
                    <span>{String(point.date).slice(5)}</span>
                  </div>
                )
              })}
            </div>
          )}
          <p className="admin-muted" style={{ marginTop: '0.75rem' }}>
            Unique visitors use the browser session key. Page views count each storefront route visit.
          </p>
        </section>
      </div>

      <div className="admin-split">
        <section className="admin-panel">
          <div className="admin-panel-head">
            <h2>Orders by status</h2>
          </div>
          <div className="admin-status-bars">
            {Object.entries(data.ordersByStatus).map(([status, count]) => (
              <div key={status} className="admin-status-row">
                <span>{status}</span>
                <div className="admin-bar-track">
                  <div
                    className="admin-bar-fill"
                    style={{
                      width: `${data.orders ? Math.max((count / data.orders) * 100, count ? 6 : 0) : 0}%`,
                    }}
                  />
                </div>
                <strong>{count}</strong>
              </div>
            ))}
          </div>
        </section>

        <section className="admin-panel">
          <div className="admin-panel-head">
            <h2>Low stock</h2>
            <Link to="/admin/products?lowStock=1">Manage</Link>
          </div>
          {data.lowStockProducts.length === 0 ? (
            <p className="admin-muted">All active products look healthy.</p>
          ) : (
            <table className="admin-table">
              <thead>
                <tr>
                  <th>Product</th>
                  <th>SKU</th>
                  <th>Qty</th>
                </tr>
              </thead>
              <tbody>
                {data.lowStockProducts.map((p) => (
                  <tr key={p.id}>
                    <td>{p.name}</td>
                    <td>{p.sku}</td>
                    <td>
                      <span className={p.quantity <= 0 ? 'badge bad' : 'badge warn'}>{p.quantity}</span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </section>
      </div>

      <section className="admin-panel">
        <div className="admin-panel-head">
          <h2>Recent orders</h2>
          <Link to="/admin/orders">View all</Link>
        </div>
        <table className="admin-table">
          <thead>
            <tr>
              <th>Order</th>
              <th>Customer</th>
              <th>Status</th>
              <th>Payment</th>
              <th>Total</th>
              <th>Date</th>
            </tr>
          </thead>
          <tbody>
            {data.recentOrders.map((o) => (
              <tr key={o.id}>
                <td>
                  <Link to={`/admin/orders?focus=${o.id}`}>{o.orderNumber}</Link>
                </td>
                <td>
                  {o.firstName} {o.lastName}
                  <div className="admin-muted">{o.email}</div>
                </td>
                <td>
                  <span className={`badge status-${o.status.toLowerCase()}`}>{o.status}</span>
                </td>
                <td>{o.paymentStatus}</td>
                <td>{money(o.total)}</td>
                <td>{formatDate(o.createdAt)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </div>
  )
}

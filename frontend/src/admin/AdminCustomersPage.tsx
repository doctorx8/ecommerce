import { useEffect, useState, type FormEvent } from 'react'
import { adminApi, formatDate, type AdminCustomer } from './adminApi'

export function AdminCustomersPage() {
  const [items, setItems] = useState<AdminCustomer[]>([])
  const [total, setTotal] = useState(0)
  const [search, setSearch] = useState('')
  const [error, setError] = useState('')

  async function load(q = search) {
    try {
      const res = await adminApi.customers({ search: q || undefined, limit: 100 })
      setItems(res.items)
      setTotal(res.total)
      setError('')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load customers')
    }
  }

  useEffect(() => {
    load()
  }, [])

  function onSearch(e: FormEvent) {
    e.preventDefault()
    load()
  }

  return (
    <div className="admin-page">
      <header className="admin-page-head">
        <div>
          <h1>Customers</h1>
          <p className="admin-muted">{total} accounts in the database</p>
        </div>
      </header>

      <form className="admin-search" onSubmit={onSearch}>
        <input
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search name or email…"
        />
        <button type="submit" className="admin-btn">
          Search
        </button>
      </form>

      {error ? <div className="admin-error">{error}</div> : null}

      <section className="admin-panel">
        <table className="admin-table">
          <thead>
            <tr>
              <th>Customer</th>
              <th>Email</th>
              <th>Phone</th>
              <th>Orders</th>
              <th>Status</th>
              <th>Joined</th>
            </tr>
          </thead>
          <tbody>
            {items.map((c) => (
              <tr key={c.id}>
                <td>
                  {c.firstName} {c.lastName}
                </td>
                <td>{c.email}</td>
                <td>{c.telephone || '—'}</td>
                <td>{c.orderCount}</td>
                <td>
                  <span className={c.isActive ? 'badge ok' : 'badge bad'}>
                    {c.isActive ? 'Active' : 'Disabled'}
                  </span>
                </td>
                <td>{formatDate(c.createdAt)}</td>
              </tr>
            ))}
          </tbody>
        </table>
        {items.length === 0 ? <p className="admin-muted">No customers found.</p> : null}
      </section>
    </div>
  )
}

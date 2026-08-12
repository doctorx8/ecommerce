import { useEffect, useMemo, useState, type FormEvent } from 'react'
import { useSearchParams } from 'react-router-dom'
import {
  STOCK_STATUSES,
  adminApi,
  money,
  type AdminProduct,
} from './adminApi'

export function AdminProductsPage() {
  const [params, setParams] = useSearchParams()
  const [items, setItems] = useState<AdminProduct[]>([])
  const [total, setTotal] = useState(0)
  const [error, setError] = useState('')
  const [busyId, setBusyId] = useState<number | null>(null)
  const [drafts, setDrafts] = useState<Record<number, Partial<AdminProduct>>>({})

  const search = params.get('search') ?? ''
  const lowStock = params.get('lowStock') === '1'
  const activeFilter = params.get('active')

  const query = useMemo(
    () => ({
      search: search || undefined,
      lowStock: lowStock || undefined,
      active: activeFilter === '1' ? true : activeFilter === '0' ? false : undefined,
      limit: 100,
    }),
    [search, lowStock, activeFilter],
  )

  async function load() {
    try {
      const res = await adminApi.products(query)
      setItems(res.items)
      setTotal(res.total)
      setDrafts({})
      setError('')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load products')
    }
  }

  useEffect(() => {
    load()
  }, [query])

  function draft(id: number) {
    return drafts[id] ?? {}
  }

  function setDraft(id: number, patch: Partial<AdminProduct>) {
    setDrafts((prev) => ({ ...prev, [id]: { ...prev[id], ...patch } }))
  }

  async function save(product: AdminProduct) {
    const d = draft(product.id)
    setBusyId(product.id)
    try {
      const updated = await adminApi.updateInventory(product.id, {
        quantity: d.quantity !== undefined ? Number(d.quantity) : product.quantity,
        stockStatus: d.stockStatus ?? product.stockStatus,
        active: d.isActive !== undefined ? Boolean(d.isActive) : product.isActive,
      })
      setItems((prev) => prev.map((p) => (p.id === updated.id ? { ...p, ...updated } : p)))
      setDrafts((prev) => {
        const next = { ...prev }
        delete next[product.id]
        return next
      })
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Save failed')
    } finally {
      setBusyId(null)
    }
  }

  function onSearch(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    const fd = new FormData(e.currentTarget)
    const next = new URLSearchParams(params)
    const q = String(fd.get('search') || '')
    if (q) next.set('search', q)
    else next.delete('search')
    setParams(next)
  }

  return (
    <div className="admin-page" data-testid="admin-products-page" id="admin-products-page">
      <header className="admin-page-head">
        <div>
          <h1>Inventory</h1>
          <p className="admin-muted">{total} products · edit stock, status, and visibility</p>
        </div>
      </header>

      <div className="admin-toolbar">
        <form onSubmit={onSearch} className="admin-search" data-testid="admin-product-search-form">
          <input
            id="admin-product-search"
            name="search"
            data-testid="admin-product-search"
            defaultValue={search}
            placeholder="Search name, SKU, model…"
          />
          <button type="submit" className="admin-btn" data-testid="admin-product-search-btn">
            Search
          </button>
        </form>
        <div className="admin-filters">
          <button
            type="button"
            className={`admin-chip ${!lowStock && !activeFilter ? 'on' : ''}`}
            onClick={() => setParams({})}
          >
            All
          </button>
          <button
            type="button"
            className={`admin-chip ${lowStock ? 'on' : ''}`}
            onClick={() => setParams({ lowStock: '1' })}
          >
            Low stock
          </button>
          <button
            type="button"
            className={`admin-chip ${activeFilter === '1' ? 'on' : ''}`}
            onClick={() => setParams({ active: '1' })}
          >
            Active
          </button>
          <button
            type="button"
            className={`admin-chip ${activeFilter === '0' ? 'on' : ''}`}
            onClick={() => setParams({ active: '0' })}
          >
            Inactive
          </button>
        </div>
      </div>

      {error ? <div className="admin-error">{error}</div> : null}

      <section className="admin-panel">
        <table className="admin-table" data-testid="admin-inventory-table" id="admin-inventory-table">
          <thead>
            <tr>
              <th>Product</th>
              <th>SKU</th>
              <th>Price</th>
              <th>Qty</th>
              <th>Stock status</th>
              <th>Active</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {items.map((p) => {
              const d = draft(p.id)
              const qty = d.quantity !== undefined ? d.quantity : p.quantity
              const stockStatus = d.stockStatus ?? p.stockStatus
              const isActive = d.isActive !== undefined ? d.isActive : p.isActive
              const dirty =
                d.quantity !== undefined ||
                d.stockStatus !== undefined ||
                d.isActive !== undefined
              return (
                <tr key={p.id} data-testid={`admin-product-row-${p.id}`} data-product-id={p.id}>
                  <td>
                    <strong>{p.name}</strong>
                    <div className="admin-muted">{p.model || p.slug}</div>
                  </td>
                  <td>{p.sku}</td>
                  <td>{money(p.price)}</td>
                  <td>
                    <input
                      className="admin-qty"
                      id={`admin-qty-${p.id}`}
                      data-testid={`admin-qty-${p.id}`}
                      type="number"
                      min={0}
                      value={qty}
                      onChange={(e) => setDraft(p.id, { quantity: Number(e.target.value) })}
                    />
                  </td>
                  <td>
                    <select
                      id={`admin-stock-status-${p.id}`}
                      data-testid={`admin-stock-status-${p.id}`}
                      value={stockStatus}
                      onChange={(e) => setDraft(p.id, { stockStatus: e.target.value })}
                    >
                      {STOCK_STATUSES.map((s) => (
                        <option key={s} value={s}>
                          {s}
                        </option>
                      ))}
                    </select>
                  </td>
                  <td>
                    <label className="admin-check">
                      <input
                        id={`admin-active-${p.id}`}
                        data-testid={`admin-active-${p.id}`}
                        type="checkbox"
                        checked={isActive}
                        onChange={(e) => setDraft(p.id, { isActive: e.target.checked })}
                      />
                      Live
                    </label>
                  </td>
                  <td>
                    <button
                      type="button"
                      className="admin-btn compact"
                      data-testid={`admin-product-save-${p.id}`}
                      disabled={!dirty || busyId === p.id}
                      onClick={() => save(p)}
                    >
                      {busyId === p.id ? 'Saving…' : 'Save'}
                    </button>
                  </td>
                </tr>
              )
            })}
          </tbody>
        </table>
        {items.length === 0 ? <p className="admin-muted">No products match these filters.</p> : null}
      </section>
    </div>
  )
}

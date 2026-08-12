import { useEffect, useState, type FormEvent } from 'react'
import { adminApi, money, type AdminCoupon } from './adminApi'

const emptyForm = {
  code: '',
  name: '',
  type: 'PERCENT',
  discount: '10',
  minOrderTotal: '',
  maxUses: '',
  active: true,
}

export function AdminCouponsPage() {
  const [items, setItems] = useState<AdminCoupon[]>([])
  const [form, setForm] = useState(emptyForm)
  const [editingId, setEditingId] = useState<number | null>(null)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  async function load() {
    try {
      setItems(await adminApi.coupons())
      setError('')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load coupons')
    }
  }

  useEffect(() => {
    load()
  }, [])

  function startEdit(coupon: AdminCoupon) {
    setEditingId(coupon.id)
    setForm({
      code: coupon.code,
      name: coupon.name,
      type: coupon.type,
      discount: String(coupon.discount),
      minOrderTotal: coupon.minOrderTotal != null ? String(coupon.minOrderTotal) : '',
      maxUses: coupon.maxUses != null ? String(coupon.maxUses) : '',
      active: coupon.isActive,
    })
  }

  function reset() {
    setEditingId(null)
    setForm(emptyForm)
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setBusy(true)
    try {
      const payload = {
        code: form.code,
        name: form.name,
        type: form.type,
        discount: Number(form.discount),
        minOrderTotal: form.minOrderTotal ? Number(form.minOrderTotal) : null,
        maxUses: form.maxUses ? Number(form.maxUses) : null,
        active: form.active,
      }
      if (editingId) await adminApi.updateCoupon(editingId, payload)
      else await adminApi.createCoupon(payload)
      reset()
      await load()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Save failed')
    } finally {
      setBusy(false)
    }
  }

  async function remove(id: number) {
    if (!confirm('Delete this coupon?')) return
    try {
      await adminApi.deleteCoupon(id)
      await load()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Delete failed')
    }
  }

  return (
    <div className="admin-page">
      <header className="admin-page-head">
        <div>
          <h1>Coupons</h1>
          <p className="admin-muted">Create and manage discount codes.</p>
        </div>
      </header>

      {error ? <div className="admin-error">{error}</div> : null}

      <div className="admin-split">
        <section className="admin-panel">
          <div className="admin-panel-head">
            <h2>{editingId ? 'Edit coupon' : 'New coupon'}</h2>
            {editingId ? (
              <button type="button" className="admin-ghost" onClick={reset}>
                Cancel
              </button>
            ) : null}
          </div>
          <form className="admin-form" onSubmit={onSubmit}>
            <label>
              Code
              <input
                value={form.code}
                onChange={(e) => setForm({ ...form, code: e.target.value.toUpperCase() })}
                required
              />
            </label>
            <label>
              Name
              <input
                value={form.name}
                onChange={(e) => setForm({ ...form, name: e.target.value })}
                required
              />
            </label>
            <div className="admin-form-row">
              <label>
                Type
                <select value={form.type} onChange={(e) => setForm({ ...form, type: e.target.value })}>
                  <option value="PERCENT">Percent</option>
                  <option value="FIXED">Fixed</option>
                </select>
              </label>
              <label>
                Discount
                <input
                  type="number"
                  min={0}
                  step="0.01"
                  value={form.discount}
                  onChange={(e) => setForm({ ...form, discount: e.target.value })}
                  required
                />
              </label>
            </div>
            <div className="admin-form-row">
              <label>
                Min order
                <input
                  type="number"
                  min={0}
                  step="0.01"
                  value={form.minOrderTotal}
                  onChange={(e) => setForm({ ...form, minOrderTotal: e.target.value })}
                />
              </label>
              <label>
                Max uses
                <input
                  type="number"
                  min={0}
                  value={form.maxUses}
                  onChange={(e) => setForm({ ...form, maxUses: e.target.value })}
                />
              </label>
            </div>
            <label className="admin-check">
              <input
                type="checkbox"
                checked={form.active}
                onChange={(e) => setForm({ ...form, active: e.target.checked })}
              />
              Active
            </label>
            <button type="submit" className="admin-btn" disabled={busy}>
              {busy ? 'Saving…' : editingId ? 'Update coupon' : 'Create coupon'}
            </button>
          </form>
        </section>

        <section className="admin-panel">
          <div className="admin-panel-head">
            <h2>All coupons</h2>
          </div>
          <table className="admin-table">
            <thead>
              <tr>
                <th>Code</th>
                <th>Discount</th>
                <th>Uses</th>
                <th>Status</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {items.map((c) => (
                <tr key={c.id}>
                  <td>
                    <strong>{c.code}</strong>
                    <div className="admin-muted">{c.name}</div>
                  </td>
                  <td>
                    {c.type === 'PERCENT' ? `${c.discount}%` : money(c.discount)}
                    {c.minOrderTotal != null ? (
                      <div className="admin-muted">min {money(c.minOrderTotal)}</div>
                    ) : null}
                  </td>
                  <td>
                    {c.usedCount}
                    {c.maxUses != null ? ` / ${c.maxUses}` : ''}
                  </td>
                  <td>
                    <span className={c.isActive ? 'badge ok' : 'badge bad'}>
                      {c.isActive ? 'Active' : 'Off'}
                    </span>
                  </td>
                  <td className="admin-actions">
                    <button type="button" className="admin-ghost" onClick={() => startEdit(c)}>
                      Edit
                    </button>
                    <button type="button" className="admin-ghost danger" onClick={() => remove(c.id)}>
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {items.length === 0 ? <p className="admin-muted">No coupons yet.</p> : null}
        </section>
      </div>
    </div>
  )
}

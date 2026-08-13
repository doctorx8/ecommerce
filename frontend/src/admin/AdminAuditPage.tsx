import { useEffect, useState } from 'react'
import { adminApi, formatDate } from './adminApi'

export function AdminAuditPage() {
  const [items, setItems] = useState<
    {
      id: number
      actorEmail?: string
      actorRole?: string
      action: string
      entityType?: string
      entityId?: string
      details?: string
      createdAt?: string
    }[]
  >([])
  const [error, setError] = useState('')

  useEffect(() => {
    adminApi
      .auditLogs()
      .then((res) => setItems(res.items))
      .catch((err) => setError(err instanceof Error ? err.message : 'Failed to load audit log'))
  }, [])

  return (
    <div className="admin-page" data-testid="admin-audit-page" id="admin-audit-page">
      <header className="admin-page-head">
        <div>
          <h1>Audit log</h1>
          <p className="admin-muted">Who changed what, and when.</p>
        </div>
      </header>
      {error ? <div className="admin-error">{error}</div> : null}
      <section className="admin-panel">
        <table className="admin-table" data-testid="admin-audit-table">
          <thead>
            <tr>
              <th>When</th>
              <th>Actor</th>
              <th>Action</th>
              <th>Entity</th>
              <th>Details</th>
            </tr>
          </thead>
          <tbody>
            {items.map((row) => (
              <tr key={row.id} data-testid={`admin-audit-row-${row.id}`}>
                <td>{formatDate(row.createdAt)}</td>
                <td>
                  {row.actorEmail || '—'}
                  <div className="admin-muted">{row.actorRole}</div>
                </td>
                <td>{row.action}</td>
                <td>
                  {row.entityType} {row.entityId}
                </td>
                <td>{row.details || '—'}</td>
              </tr>
            ))}
          </tbody>
        </table>
        {items.length === 0 ? <p className="admin-muted">No audit events yet.</p> : null}
      </section>
    </div>
  )
}

import { useEffect, useState } from 'react'
import { adminApi, formatDate } from './adminApi'

type PendingReview = {
  id: number
  productId: number
  author: string
  rating: number
  text: string
  createdAt?: string
}

export function AdminReviewsPage() {
  const [items, setItems] = useState<PendingReview[]>([])
  const [error, setError] = useState('')

  async function load() {
    try {
      setItems(await adminApi.pendingReviews())
      setError('')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load reviews')
    }
  }

  useEffect(() => {
    load()
  }, [])

  return (
    <div className="admin-page" data-testid="admin-reviews-page" id="admin-reviews-page">
      <header className="admin-page-head">
        <div>
          <h1>Review moderation</h1>
          <p className="admin-muted">Approve or reject customer product reviews.</p>
        </div>
      </header>
      {error ? <div className="admin-error">{error}</div> : null}
      <section className="admin-panel">
        <table className="admin-table" data-testid="admin-reviews-table">
          <thead>
            <tr>
              <th>Product</th>
              <th>Author</th>
              <th>Rating</th>
              <th>Text</th>
              <th>Date</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {items.map((r) => (
              <tr key={r.id} data-testid={`admin-review-row-${r.id}`}>
                <td>#{r.productId}</td>
                <td>{r.author}</td>
                <td>{r.rating}/5</td>
                <td>{r.text}</td>
                <td>{formatDate(r.createdAt)}</td>
                <td className="admin-actions">
                  <button
                    type="button"
                    className="admin-btn compact"
                    data-testid={`admin-review-approve-${r.id}`}
                    onClick={async () => {
                      await adminApi.approveReview(r.id)
                      await load()
                    }}
                  >
                    Approve
                  </button>
                  <button
                    type="button"
                    className="admin-ghost"
                    data-testid={`admin-review-reject-${r.id}`}
                    onClick={async () => {
                      await adminApi.rejectReview(r.id)
                      await load()
                    }}
                  >
                    Keep pending
                  </button>
                  <button
                    type="button"
                    className="admin-ghost danger"
                    data-testid={`admin-review-delete-${r.id}`}
                    onClick={async () => {
                      await adminApi.deleteReview(r.id)
                      await load()
                    }}
                  >
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {items.length === 0 ? <p className="admin-muted">No pending reviews.</p> : null}
      </section>
    </div>
  )
}

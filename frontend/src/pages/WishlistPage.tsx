import { useEffect, useState } from 'react'
import { Link, Navigate } from 'react-router-dom'
import { api, money, type Product } from '../api/client'
import { useStore } from '../state/StoreContext'

type WishItem = { id: number; product: Product }

export function WishlistPage() {
  const { user } = useStore()
  const [items, setItems] = useState<WishItem[]>([])
  const [error, setError] = useState('')

  async function load() {
    try {
      setItems(await api.getWishlist())
      setError('')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load wishlist')
    }
  }

  useEffect(() => {
    if (user) load()
  }, [user])

  if (!user) return <Navigate to="/auth" replace />

  async function remove(productId: number) {
    await api.removeWishlist(productId)
    await load()
  }

  return (
    <div className="page" data-testid="wishlist-page" id="wishlist-page">
      <div className="container">
        <h1 className="page-title">Saved for later</h1>
        <p className="muted">Your wishlist</p>
        {error ? (
          <div className="alert" data-testid="wishlist-error">
            {error}
          </div>
        ) : null}
        {items.length === 0 ? (
          <div className="empty" data-testid="wishlist-empty">
            Nothing saved yet. <Link to="/shop">Browse shop</Link>
          </div>
        ) : (
          <div className="panel" data-testid="wishlist-list">
            {items.map((item) => (
              <div
                key={item.id}
                className="cart-row"
                data-testid={`wishlist-item-${item.product.id}`}
                style={{ alignItems: 'center' }}
              >
                <div>
                  <Link to={`/product/${item.product.slug}`}>
                    <strong>{item.product.name}</strong>
                  </Link>
                  <div className="muted">{money(item.product.price)}</div>
                </div>
                <div className="cart-actions">
                  <Link className="btn btn-ghost" to={`/product/${item.product.slug}`}>
                    View
                  </Link>
                  <button
                    type="button"
                    className="linkish"
                    data-testid={`wishlist-remove-${item.product.id}`}
                    onClick={() => remove(item.product.id)}
                  >
                    Remove
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

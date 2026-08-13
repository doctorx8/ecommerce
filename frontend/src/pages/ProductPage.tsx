import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useParams } from 'react-router-dom'
import { api, money, type Product } from '../api/client'
import { ProductMedia } from '../components/ProductMedia'
import { useStore } from '../state/StoreContext'

type Review = { id: number; author: string; rating: number; text: string; createdAt?: string }

export function ProductPage() {
  const { slug = '' } = useParams()
  const { addToCart, user } = useStore()
  const [product, setProduct] = useState<Product | null>(null)
  const [quantity, setQuantity] = useState(1)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const [reviews, setReviews] = useState<Review[]>([])
  const [wishBusy, setWishBusy] = useState(false)
  const [reviewForm, setReviewForm] = useState({ rating: 5, text: '' })
  const [reviewMsg, setReviewMsg] = useState('')

  useEffect(() => {
    api
      .getProduct(slug)
      .then(async (p) => {
        setProduct(p)
        setReviews(await api.getReviews(p.id))
      })
      .catch((err: Error) => setError(err.message))
  }, [slug])

  async function onAdd(e: FormEvent) {
    e.preventDefault()
    if (!product) return
    setBusy(true)
    setMessage('')
    setError('')
    try {
      await addToCart(product.id, quantity)
      setMessage('Added to cart')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not add to cart')
    } finally {
      setBusy(false)
    }
  }

  async function toggleWishlist() {
    if (!product || !user) return
    setWishBusy(true)
    try {
      await api.addWishlist(product.id)
      setMessage('Saved to wishlist')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Wishlist failed')
    } finally {
      setWishBusy(false)
    }
  }

  async function submitReview(e: FormEvent) {
    e.preventDefault()
    if (!product || !user) return
    setReviewMsg('')
    try {
      await api.createReview({
        productId: product.id,
        rating: reviewForm.rating,
        text: reviewForm.text,
      })
      setReviewForm({ rating: 5, text: '' })
      setReviewMsg('Review submitted for admin approval.')
    } catch (err) {
      setReviewMsg(err instanceof Error ? err.message : 'Could not submit review')
    }
  }

  if (error && !product) {
    return (
      <div className="page container" data-testid="pdp-error-page">
        <div className="alert" data-testid="pdp-error" id="pdp-error">
          {error}
        </div>
      </div>
    )
  }

  if (!product) {
    return (
      <div className="page container" data-testid="pdp-loading">
        <p className="muted">Loading product…</p>
      </div>
    )
  }

  return (
    <div className="page" data-testid="pdp-page" id="pdp-page" data-product-id={product.id}>
      <div className="container pdp">
        <ProductMedia product={product} className="pdp-media" />
        <div className="pdp-info" data-testid="pdp-info">
          <div className="brand-line muted">{product.manufacturer?.name ?? 'Karwan'}</div>
          <h1 data-testid="pdp-name" id="pdp-name">
            {product.name}
          </h1>
          <div className="price-row" style={{ marginBottom: '1rem' }}>
            <span className="price" style={{ fontSize: '1.4rem' }} data-testid="pdp-price" id="pdp-price">
              {money(product.price)}
            </span>
            {product.compareAtPrice ? (
              <span className="compare">{money(product.compareAtPrice)}</span>
            ) : null}
          </div>
          <p data-testid="pdp-description">{product.description || product.shortDesc}</p>

          <form onSubmit={onAdd} data-testid="pdp-add-form" id="pdp-add-form">
            <div className="qty-row">
              <label htmlFor="pdp-qty">
                Qty
                <input
                  id="pdp-qty"
                  name="quantity"
                  data-testid="pdp-qty"
                  type="number"
                  min={1}
                  max={Math.max(product.quantity, 1)}
                  value={quantity}
                  onChange={(e) => setQuantity(Number(e.target.value))}
                />
              </label>
              <button
                className="btn btn-primary"
                type="submit"
                disabled={busy}
                data-testid="pdp-add-to-cart"
                id="pdp-add-to-cart"
              >
                {busy ? 'Adding…' : 'Add to cart'}
              </button>
              {user ? (
                <button
                  className="btn btn-ghost"
                  type="button"
                  disabled={wishBusy}
                  data-testid="pdp-wishlist"
                  id="pdp-wishlist"
                  onClick={toggleWishlist}
                >
                  Save for later
                </button>
              ) : null}
            </div>
          </form>

          {message ? (
            <div className="success" data-testid="pdp-success" id="pdp-success">
              {message} —{' '}
              <Link to="/cart" data-testid="pdp-view-cart">
                View cart
              </Link>
            </div>
          ) : null}
          {error ? (
            <div className="alert" data-testid="pdp-error" id="pdp-alert">
              {error}
            </div>
          ) : null}
        </div>
      </div>

      <div className="container" style={{ marginTop: '2rem' }} data-testid="pdp-reviews" id="pdp-reviews">
        <h2>Reviews</h2>
        {reviews.length === 0 ? (
          <p className="muted" data-testid="pdp-reviews-empty">
            No approved reviews yet.
          </p>
        ) : (
          <div className="panel" style={{ display: 'grid', gap: '1rem' }}>
            {reviews.map((r) => (
              <div key={r.id} data-testid={`review-${r.id}`}>
                <strong>
                  {r.author} · {'★'.repeat(r.rating)}
                  {'☆'.repeat(5 - r.rating)}
                </strong>
                <p style={{ margin: '0.35rem 0 0' }}>{r.text}</p>
              </div>
            ))}
          </div>
        )}

        {user ? (
          <form
            className="form-grid"
            style={{ marginTop: '1.25rem', maxWidth: 560 }}
            onSubmit={submitReview}
            data-testid="pdp-review-form"
            id="pdp-review-form"
          >
            <h3 style={{ gridColumn: '1 / -1', margin: 0 }}>Write a review</h3>
            <label htmlFor="review-rating">
              Rating
              <select
                id="review-rating"
                data-testid="review-rating"
                value={reviewForm.rating}
                onChange={(e) => setReviewForm({ ...reviewForm, rating: Number(e.target.value) })}
              >
                {[5, 4, 3, 2, 1].map((n) => (
                  <option key={n} value={n}>
                    {n} stars
                  </option>
                ))}
              </select>
            </label>
            <label className="full" htmlFor="review-text">
              Review
              <textarea
                id="review-text"
                data-testid="review-text"
                required
                rows={4}
                value={reviewForm.text}
                onChange={(e) => setReviewForm({ ...reviewForm, text: e.target.value })}
              />
            </label>
            {reviewMsg ? (
              <div className="muted full" data-testid="review-message">
                {reviewMsg}
              </div>
            ) : null}
            <div className="full">
              <button className="btn btn-primary" type="submit" data-testid="review-submit" id="review-submit">
                Submit for approval
              </button>
            </div>
          </form>
        ) : (
          <p className="muted">
            <Link to="/auth">Sign in</Link> to leave a review.
          </p>
        )}
      </div>
    </div>
  )
}

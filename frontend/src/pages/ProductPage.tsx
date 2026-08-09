import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useParams } from 'react-router-dom'
import { api, money, type Product } from '../api/client'
import { ProductMedia } from '../components/ProductMedia'
import { useStore } from '../state/StoreContext'

export function ProductPage() {
  const { slug = '' } = useParams()
  const { addToCart } = useStore()
  const [product, setProduct] = useState<Product | null>(null)
  const [quantity, setQuantity] = useState(1)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    api
      .getProduct(slug)
      .then(setProduct)
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

  if (error && !product) {
    return (
      <div className="page container">
        <div className="alert">{error}</div>
      </div>
    )
  }

  if (!product) {
    return (
      <div className="page container">
        <p className="muted">Loading product…</p>
      </div>
    )
  }

  return (
    <div className="page">
      <div className="container pdp">
        <ProductMedia product={product} className="pdp-media" />
        <div className="pdp-info">
          <div className="brand-line muted">{product.manufacturer?.name ?? 'Karwan'}</div>
          <h1>{product.name}</h1>
          <div className="price-row" style={{ marginBottom: '1rem' }}>
            <span className="price" style={{ fontSize: '1.4rem' }}>
              {money(product.price)}
            </span>
            {product.compareAtPrice ? (
              <span className="compare">{money(product.compareAtPrice)}</span>
            ) : null}
          </div>
          <p>{product.description || product.shortDesc}</p>

          <form onSubmit={onAdd}>
            <div className="qty-row">
              <label>
                Qty
                <input
                  type="number"
                  min={1}
                  max={Math.max(product.quantity, 1)}
                  value={quantity}
                  onChange={(e) => setQuantity(Number(e.target.value))}
                />
              </label>
              <button className="btn btn-primary" type="submit" disabled={busy}>
                {busy ? 'Adding…' : 'Add to cart'}
              </button>
            </div>
          </form>

          {message ? <div className="success">{message} — <Link to="/cart">View cart</Link></div> : null}
          {error ? <div className="alert">{error}</div> : null}
        </div>
      </div>
    </div>
  )
}

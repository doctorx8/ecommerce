import { Link } from 'react-router-dom'
import { api, money } from '../api/client'
import { useStore } from '../state/StoreContext'
import { ProductMedia } from '../components/ProductMedia'

export function CartPage() {
  const { cart, refreshCart } = useStore()

  async function updateQty(id: number, quantity: number) {
    await api.updateCartItem(id, Math.max(1, quantity))
    await refreshCart()
  }

  async function remove(id: number) {
    await api.removeCartItem(id)
    await refreshCart()
  }

  if (!cart || cart.items.length === 0) {
    return (
      <div className="page container">
        <h1 className="page-title">Cart</h1>
        <div className="empty">
          Your cart is empty. <Link to="/shop">Continue shopping</Link>
        </div>
      </div>
    )
  }

  return (
    <div className="page">
      <div className="container">
        <h1 className="page-title">Cart</h1>
        <p className="muted">{cart.itemCount} item(s)</p>

        <div className="panel cart-list">
          {cart.items.map((item) => (
            <div className="cart-row" key={item.id}>
              <div className="cart-thumb">
                <ProductMedia product={item.product} />
              </div>
              <div>
                <Link to={`/product/${item.product.slug}`}>
                  <strong>{item.product.name}</strong>
                </Link>
                <div className="muted">{money(item.product.price)}</div>
              </div>
              <div className="cart-actions">
                <input
                  type="number"
                  min={1}
                  value={item.quantity}
                  onChange={(e) => updateQty(item.id, Number(e.target.value))}
                />
                <div>{money(Number(item.product.price) * item.quantity)}</div>
                <button className="linkish" type="button" onClick={() => remove(item.id)}>
                  Remove
                </button>
              </div>
            </div>
          ))}
        </div>

        <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '1.5rem', gap: '1rem', flexWrap: 'wrap' }}>
          <div>
            <div className="muted">Subtotal</div>
            <strong style={{ fontSize: '1.4rem' }}>{money(cart.subtotal)}</strong>
          </div>
          <Link className="btn btn-primary" to="/checkout">
            Checkout
          </Link>
        </div>
      </div>
    </div>
  )
}

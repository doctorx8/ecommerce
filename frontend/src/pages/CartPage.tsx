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
      <div className="page container" data-testid="cart-page" id="cart-page">
        <h1 className="page-title" data-testid="cart-title">
          Cart
        </h1>
        <div className="empty" data-testid="cart-empty" id="cart-empty">
          Your cart is empty.{' '}
          <Link to="/shop" data-testid="cart-continue-shopping">
            Continue shopping
          </Link>
        </div>
      </div>
    )
  }

  return (
    <div className="page" data-testid="cart-page" id="cart-page">
      <div className="container">
        <h1 className="page-title" data-testid="cart-title">
          Cart
        </h1>
        <p className="muted" data-testid="cart-item-count">
          {cart.itemCount} item(s)
        </p>

        <div className="panel cart-list" data-testid="cart-list" id="cart-list">
          {cart.items.map((item) => (
            <div
              className="cart-row"
              key={item.id}
              data-testid={`cart-item-${item.id}`}
              id={`cart-item-${item.id}`}
              data-cart-item-id={item.id}
            >
              <div className="cart-thumb">
                <ProductMedia product={item.product} />
              </div>
              <div>
                <Link to={`/product/${item.product.slug}`} data-testid={`cart-item-name-${item.id}`}>
                  <strong>{item.product.name}</strong>
                </Link>
                <div className="muted">{money(item.product.price)}</div>
              </div>
              <div className="cart-actions">
                <input
                  id={`cart-qty-${item.id}`}
                  name={`quantity-${item.id}`}
                  data-testid={`cart-qty-${item.id}`}
                  type="number"
                  min={1}
                  aria-label={`Quantity for ${item.product.name}`}
                  value={item.quantity}
                  onChange={(e) => updateQty(item.id, Number(e.target.value))}
                />
                <div data-testid={`cart-line-total-${item.id}`}>
                  {money(Number(item.product.price) * item.quantity)}
                </div>
                <button
                  className="linkish"
                  type="button"
                  data-testid={`cart-remove-${item.id}`}
                  id={`cart-remove-${item.id}`}
                  onClick={() => remove(item.id)}
                >
                  Remove
                </button>
              </div>
            </div>
          ))}
        </div>

        <div
          style={{
            display: 'flex',
            justifyContent: 'space-between',
            marginTop: '1.5rem',
            gap: '1rem',
            flexWrap: 'wrap',
          }}
        >
          <div>
            <div className="muted">Subtotal</div>
            <strong style={{ fontSize: '1.4rem' }} data-testid="cart-subtotal" id="cart-subtotal">
              {money(cart.subtotal)}
            </strong>
          </div>
          <Link className="btn btn-primary" to="/checkout" data-testid="cart-checkout" id="cart-checkout">
            Checkout
          </Link>
        </div>
      </div>
    </div>
  )
}

import { Link } from 'react-router-dom'
import { money, type Product } from '../api/client'
import { ProductMedia } from './ProductMedia'

export function ProductCard({ product, index = 0 }: { product: Product; index?: number }) {
  return (
    <Link
      to={`/product/${product.slug}`}
      className="product-tile"
      style={{ animationDelay: `${index * 80}ms` }}
    >
      <ProductMedia product={product} />
      <div className="product-meta">
        <div className="brand-line">{product.manufacturer?.name ?? 'Northline'}</div>
        <h3>{product.name}</h3>
        <div className="price-row">
          <span className="price">{money(product.price)}</span>
          {product.compareAtPrice ? (
            <span className="compare">{money(product.compareAtPrice)}</span>
          ) : null}
        </div>
      </div>
    </Link>
  )
}

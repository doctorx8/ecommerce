import { Link } from 'react-router-dom'
import { money, type Product } from '../api/client'
import { ProductMedia } from './ProductMedia'

export function ProductCard({ product, index = 0 }: { product: Product; index?: number }) {
  return (
    <Link
      to={`/product/${product.slug}`}
      className="product-tile"
      style={{ animationDelay: `${index * 80}ms` }}
      data-testid={`product-card-${product.id}`}
      id={`product-card-${product.id}`}
      data-product-id={product.id}
      data-product-slug={product.slug}
      aria-label={product.name}
    >
      <ProductMedia product={product} />
      <div className="product-meta">
        <div className="brand-line">{product.manufacturer?.name ?? 'Karwan'}</div>
        <h3 data-testid={`product-name-${product.id}`}>{product.name}</h3>
        <div className="price-row">
          <span className="price" data-testid={`product-price-${product.id}`}>
            {money(product.price)}
          </span>
          {product.compareAtPrice ? (
            <span className="compare">{money(product.compareAtPrice)}</span>
          ) : null}
        </div>
      </div>
    </Link>
  )
}

import { useEffect, useState } from 'react'
import { api, type Category, type Product } from '../api/client'
import { ProductCard } from '../components/ProductCard'

export function ShopPage() {
  const [products, setProducts] = useState<Product[]>([])
  const [categories, setCategories] = useState<Category[]>([])
  const [search, setSearch] = useState('')
  const [category, setCategory] = useState('')
  const [sort, setSort] = useState('newest')
  const [error, setError] = useState('')

  useEffect(() => {
    api.getCategories().then(setCategories).catch(() => setCategories([]))
  }, [])

  useEffect(() => {
    const timer = setTimeout(() => {
      api
        .getProducts({ search, category: category || undefined, sort, limit: 24 })
        .then((res) => {
          setProducts(res.items)
          setError('')
        })
        .catch((err: Error) => setError(err.message))
    }, 200)
    return () => clearTimeout(timer)
  }, [search, category, sort])

  const flatCategories = categories.flatMap((c) => [c, ...(c.children || [])])

  return (
    <div className="page">
      <div className="container">
        <h1 className="page-title">Shop</h1>
        <p className="muted">Browse the full Karwan catalog.</p>

        <div className="toolbar">
          <input
            placeholder="Search products"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
          <select value={category} onChange={(e) => setCategory(e.target.value)}>
            <option value="">All categories</option>
            {flatCategories.map((c) => (
              <option key={c.id} value={c.slug}>
                {c.name}
              </option>
            ))}
          </select>
          <select value={sort} onChange={(e) => setSort(e.target.value)}>
            <option value="newest">Newest</option>
            <option value="price_asc">Price ↑</option>
            <option value="price_desc">Price ↓</option>
            <option value="name">Name</option>
          </select>
        </div>

        {error ? <div className="alert">{error}</div> : null}

        {products.length === 0 ? (
          <div className="empty">No products match your filters.</div>
        ) : (
          <div className="product-grid">
            {products.map((product, index) => (
              <ProductCard key={product.id} product={product} index={index} />
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

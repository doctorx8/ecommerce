import { useEffect, useMemo, useState } from 'react'
import { api, type Category, type Product } from '../api/client'
import { ProductCard } from '../components/ProductCard'

type Manufacturer = { id: number; name: string; slug: string }

const SORT_OPTIONS = [
  { value: 'newest', label: 'Newest first' },
  { value: 'oldest', label: 'Oldest first' },
  { value: 'popular', label: 'Most popular' },
  { value: 'featured', label: 'Featured first' },
  { value: 'price_asc', label: 'Price: low to high' },
  { value: 'price_desc', label: 'Price: high to low' },
  { value: 'name_asc', label: 'Name: A–Z' },
  { value: 'name_desc', label: 'Name: Z–A' },
  { value: 'catalog', label: 'Catalog order' },
] as const

export function ShopPage() {
  const [products, setProducts] = useState<Product[]>([])
  const [total, setTotal] = useState(0)
  const [categories, setCategories] = useState<Category[]>([])
  const [manufacturers, setManufacturers] = useState<Manufacturer[]>([])
  const [search, setSearch] = useState('')
  const [category, setCategory] = useState('')
  const [manufacturer, setManufacturer] = useState('')
  const [sort, setSort] = useState('newest')
  const [minPrice, setMinPrice] = useState('')
  const [maxPrice, setMaxPrice] = useState('')
  const [featuredOnly, setFeaturedOnly] = useState(false)
  const [inStockOnly, setInStockOnly] = useState(false)
  const [onSaleOnly, setOnSaleOnly] = useState(false)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.getCategories().then(setCategories).catch(() => setCategories([]))
    api.getManufacturers().then(setManufacturers).catch(() => setManufacturers([]))
  }, [])

  useEffect(() => {
    const timer = setTimeout(() => {
      setLoading(true)
      api
        .getProducts({
          search: search || undefined,
          category: category || undefined,
          manufacturer: manufacturer || undefined,
          sort,
          limit: 48,
          minPrice: minPrice !== '' ? Number(minPrice) : undefined,
          maxPrice: maxPrice !== '' ? Number(maxPrice) : undefined,
          featured: featuredOnly || undefined,
          inStock: inStockOnly || undefined,
          onSale: onSaleOnly || undefined,
        })
        .then((res) => {
          setProducts(res.items)
          setTotal(res.total ?? res.items.length)
          setError('')
        })
        .catch((err: Error) => setError(err.message))
        .finally(() => setLoading(false))
    }, 200)
    return () => clearTimeout(timer)
  }, [search, category, manufacturer, sort, minPrice, maxPrice, featuredOnly, inStockOnly, onSaleOnly])

  const flatCategories = useMemo(
    () =>
      categories.flatMap((c) => {
        const kids = (c.children || []).map((child) => ({
          ...child,
          name: `${c.name} › ${child.name}`,
        }))
        return [c, ...kids]
      }),
    [categories],
  )

  const hasFilters =
    Boolean(search) ||
    Boolean(category) ||
    Boolean(manufacturer) ||
    Boolean(minPrice) ||
    Boolean(maxPrice) ||
    featuredOnly ||
    inStockOnly ||
    onSaleOnly ||
    sort !== 'newest'

  function clearFilters() {
    setSearch('')
    setCategory('')
    setManufacturer('')
    setSort('newest')
    setMinPrice('')
    setMaxPrice('')
    setFeaturedOnly(false)
    setInStockOnly(false)
    setOnSaleOnly(false)
  }

  return (
    <div className="page" data-testid="shop-page" id="shop-page">
      <div className="container">
        <h1 className="page-title" data-testid="shop-title">
          Shop
        </h1>
        <p className="muted">Browse phones, laptops, audio, and the full Karwan electronics catalog.</p>

        <div className="toolbar shop-toolbar" data-testid="shop-toolbar" id="shop-toolbar">
          <input
            id="shop-search"
            name="search"
            data-testid="shop-search"
            placeholder="Search products, SKU…"
            aria-label="Search products"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
          <select
            id="shop-category"
            name="category"
            data-testid="shop-category"
            aria-label="Filter by category"
            value={category}
            onChange={(e) => setCategory(e.target.value)}
          >
            <option value="">All categories</option>
            {flatCategories.map((c) => (
              <option key={c.id} value={c.slug}>
                {c.name}
              </option>
            ))}
          </select>
          <select
            id="shop-manufacturer"
            name="manufacturer"
            data-testid="shop-manufacturer"
            aria-label="Filter by brand"
            value={manufacturer}
            onChange={(e) => setManufacturer(e.target.value)}
          >
            <option value="">All brands</option>
            {manufacturers.map((m) => (
              <option key={m.id} value={m.slug}>
                {m.name}
              </option>
            ))}
          </select>
          <select
            id="shop-sort"
            name="sort"
            data-testid="shop-sort"
            aria-label="Sort products"
            value={sort}
            onChange={(e) => setSort(e.target.value)}
          >
            {SORT_OPTIONS.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>
          <input
            id="shop-min-price"
            name="minPrice"
            type="number"
            min="0"
            step="1"
            inputMode="decimal"
            data-testid="shop-min-price"
            placeholder="Min $"
            aria-label="Minimum price"
            value={minPrice}
            onChange={(e) => setMinPrice(e.target.value)}
          />
          <input
            id="shop-max-price"
            name="maxPrice"
            type="number"
            min="0"
            step="1"
            inputMode="decimal"
            data-testid="shop-max-price"
            placeholder="Max $"
            aria-label="Maximum price"
            value={maxPrice}
            onChange={(e) => setMaxPrice(e.target.value)}
          />
        </div>

        <div className="shop-filter-row" data-testid="shop-filter-row" id="shop-filter-row">
          <label className="checkbox-row shop-check" htmlFor="shop-featured">
            <input
              id="shop-featured"
              name="featured"
              type="checkbox"
              data-testid="shop-featured"
              checked={featuredOnly}
              onChange={(e) => setFeaturedOnly(e.target.checked)}
            />
            Featured only
          </label>
          <label className="checkbox-row shop-check" htmlFor="shop-in-stock">
            <input
              id="shop-in-stock"
              name="inStock"
              type="checkbox"
              data-testid="shop-in-stock"
              checked={inStockOnly}
              onChange={(e) => setInStockOnly(e.target.checked)}
            />
            In stock
          </label>
          <label className="checkbox-row shop-check" htmlFor="shop-on-sale">
            <input
              id="shop-on-sale"
              name="onSale"
              type="checkbox"
              data-testid="shop-on-sale"
              checked={onSaleOnly}
              onChange={(e) => setOnSaleOnly(e.target.checked)}
            />
            On sale
          </label>
          {hasFilters ? (
            <button
              type="button"
              className="linkish"
              data-testid="shop-clear-filters"
              id="shop-clear-filters"
              onClick={clearFilters}
            >
              Clear filters
            </button>
          ) : null}
          <span className="shop-result-count muted" data-testid="shop-result-count" id="shop-result-count">
            {loading ? 'Loading…' : `${total} product${total === 1 ? '' : 's'}`}
          </span>
        </div>

        {error ? (
          <div className="alert" data-testid="shop-error" id="shop-error">
            {error}
          </div>
        ) : null}

        {!loading && products.length === 0 ? (
          <div className="empty" data-testid="shop-empty" id="shop-empty">
            No products match your filters.
            {hasFilters ? (
              <>
                {' '}
                <button type="button" className="linkish" onClick={clearFilters}>
                  Clear filters
                </button>
              </>
            ) : null}
          </div>
        ) : (
          <div className="product-grid" data-testid="shop-product-grid" id="shop-product-grid">
            {products.map((product, index) => (
              <ProductCard key={product.id} product={product} index={index} />
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

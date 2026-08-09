import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api, type Product } from '../api/client'
import { ProductCard } from '../components/ProductCard'

export function HomePage() {
  const [featured, setFeatured] = useState<Product[]>([])

  useEffect(() => {
    api.getProducts({ featured: true, limit: 3 }).then((res) => setFeatured(res.items)).catch(() => setFeatured([]))
  }, [])

  return (
    <>
      <section className="hero">
        <div className="hero-media" aria-hidden="true" />
        <div className="hero-veil" aria-hidden="true" />
        <div className="container hero-copy">
          <p className="brand-mark">NORTHLINE</p>
          <h1>Tools that disappear into the work.</h1>
          <p>
            A focused collection of phones, laptops, and sound — chosen for clarity, build, and
            everyday speed.
          </p>
          <div className="cta-row">
            <Link className="btn btn-primary" to="/shop">
              Shop the lineup
            </Link>
            <Link className="btn btn-ghost" to="/product/macbook-air-m3">
              View MacBook Air
            </Link>
          </div>
        </div>
      </section>

      <section className="section">
        <div className="container">
          <div className="section-head">
            <div>
              <h2>Featured now</h2>
              <p>Current picks from the Northline catalog.</p>
            </div>
            <Link className="btn btn-ghost" to="/shop">
              View all
            </Link>
          </div>
          <div className="product-grid">
            {featured.map((product, index) => (
              <ProductCard key={product.id} product={product} index={index} />
            ))}
          </div>
        </div>
      </section>
    </>
  )
}

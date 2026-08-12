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
    <div data-testid="home-page" id="home-page">
      <section className="hero" data-testid="home-hero" id="home-hero">
        <div className="hero-media" aria-hidden="true" />
        <div className="hero-veil" aria-hidden="true" />
        <div className="container hero-copy">
          <p className="brand-mark" data-testid="home-brand">
            KARWAN
          </p>
          <h1 data-testid="home-headline">Tools that disappear into the work.</h1>
          <p>
            A focused collection of phones, laptops, and sound — chosen for clarity, build, and
            everyday speed.
          </p>
          <div className="cta-row">
            <Link className="btn btn-primary" to="/shop" data-testid="home-cta-shop" id="home-cta-shop">
              Shop the lineup
            </Link>
            <Link
              className="btn btn-ghost"
              to="/product/macbook-air-m3"
              data-testid="home-cta-macbook"
              id="home-cta-macbook"
            >
              View MacBook Air
            </Link>
          </div>
        </div>
      </section>

      <section className="section" data-testid="home-featured" id="home-featured">
        <div className="container">
          <div className="section-head">
            <div>
              <h2>Featured now</h2>
              <p>Current picks from the Karwan catalog.</p>
            </div>
            <Link className="btn btn-ghost" to="/shop" data-testid="home-view-all" id="home-view-all">
              View all
            </Link>
          </div>
          <div className="product-grid" data-testid="featured-product-grid" id="featured-product-grid">
            {featured.map((product, index) => (
              <ProductCard key={product.id} product={product} index={index} />
            ))}
          </div>
        </div>
      </section>
    </div>
  )
}

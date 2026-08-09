import { useState } from 'react'
import type { Product } from '../api/client'

const FALLBACKS: Record<string, string> = {
  'iphone-15':
    'https://images.unsplash.com/photo-1695048133142-1a20484d2569?auto=format&fit=crop&w=1200&q=80',
  'macbook-air-m3':
    'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=1200&q=80',
  'sony-wh-1000xm5':
    'https://images.unsplash.com/photo-1546435770-a3e426bf472b?auto=format&fit=crop&w=1200&q=80',
}

export function ProductMedia({
  product,
  className = 'product-media',
}: {
  product: Product
  className?: string
}) {
  const primary = product.images?.[0]?.image
  const remote = FALLBACKS[product.slug]
  const [src, setSrc] = useState(remote || primary || '')
  const [failed, setFailed] = useState(!src)

  return (
    <div className={className}>
      {!failed && src ? (
        <img
          src={src}
          alt={product.images?.[0]?.alt || product.name}
          onError={() => {
            if (src !== remote && remote) {
              setSrc(remote)
              return
            }
            setFailed(true)
          }}
        />
      ) : (
        <div className="fallback">{product.name.slice(0, 2).toUpperCase()}</div>
      )}
    </div>
  )
}

import { useState } from 'react'
import type { Product } from '../api/client'

const FALLBACKS: Record<string, string> = {
  'iphone-15':
    'https://images.unsplash.com/photo-1695048133142-1a20484d2569?auto=format&fit=crop&w=1200&q=80',
  'macbook-air-m3':
    'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=1200&q=80',
  'sony-wh-1000xm5':
    'https://images.unsplash.com/photo-1546435770-a3e426bf472b?auto=format&fit=crop&w=1200&q=80',
  'dell-xps-13':
    'https://images.unsplash.com/photo-1593642632823-8f785ba67e45?auto=format&fit=crop&w=1200&q=80',
  'asus-zenbook-14':
    'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&w=1200&q=80',
  'logitech-mx-master-3s':
    'https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?auto=format&fit=crop&w=1200&q=80',
  'dell-ultrasharp-27':
    'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?auto=format&fit=crop&w=1200&q=80',
  'samsung-t7-1tb':
    'https://images.unsplash.com/photo-1597872200969-2b65d56bd16b?auto=format&fit=crop&w=1200&q=80',
  'playstation-5':
    'https://images.unsplash.com/photo-1606813907291-d86efa9b94db?auto=format&fit=crop&w=1200&q=80',
  'nintendo-switch-oled':
    'https://images.unsplash.com/photo-1578303512597-81e6d8c88863?auto=format&fit=crop&w=1200&q=80',
  'asus-rog-ally':
    'https://images.unsplash.com/photo-1612287230202-1ff1d85d1bdf?auto=format&fit=crop&w=1200&q=80',
  'logitech-g-pro-x':
    'https://images.unsplash.com/photo-1599669454699-248893623440?auto=format&fit=crop&w=1200&q=80',
  'samsung-galaxy-s24':
    'https://images.unsplash.com/photo-1610945415295-d9bbf067e59c?auto=format&fit=crop&w=1200&q=80',
  'google-pixel-8':
    'https://images.unsplash.com/photo-1598327105666-5b89351aff97?auto=format&fit=crop&w=1200&q=80',
  'ipad-air-m2':
    'https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?auto=format&fit=crop&w=1200&q=80',
  'bose-qc-ultra':
    'https://images.unsplash.com/photo-1484704849700-f032a568e944?auto=format&fit=crop&w=1200&q=80',
  'airpods-pro-2':
    'https://images.unsplash.com/photo-1600294037681-c80b4cb5b434?auto=format&fit=crop&w=1200&q=80',
  'sony-bravia-55':
    'https://images.unsplash.com/photo-1593359677879-a4bb92f829d1?auto=format&fit=crop&w=1200&q=80',
  'google-nest-hub':
    'https://images.unsplash.com/photo-1558089687-f282ffcbc0d4?auto=format&fit=crop&w=1200&q=80',
  'homepod-mini':
    'https://images.unsplash.com/photo-1589492477829-5e65395b66cc?auto=format&fit=crop&w=1200&q=80',
  'philips-hue-starter':
    'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?auto=format&fit=crop&w=1200&q=80',
  'samsung-smart-monitor-m8':
    'https://images.unsplash.com/photo-1585792187664-6b1c9f2c2e2f?auto=format&fit=crop&w=1200&q=80',
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

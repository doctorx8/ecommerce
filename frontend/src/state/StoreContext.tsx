import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { api, type Cart } from '../api/client'

type User = { id: number; email: string; firstName: string; lastName: string }

type StoreContextValue = {
  cart: Cart | null
  user: User | null
  loading: boolean
  refreshCart: () => Promise<void>
  addToCart: (productId: number, quantity?: number) => Promise<void>
  setUser: (user: User | null, token?: string | null) => void
  logout: () => void
}

const StoreContext = createContext<StoreContextValue | null>(null)

export function StoreProvider({ children }: { children: ReactNode }) {
  const [cart, setCart] = useState<Cart | null>(null)
  const [user, setUserState] = useState<User | null>(() => {
    const raw = localStorage.getItem('karwan_user')
    return raw ? (JSON.parse(raw) as User) : null
  })
  const [loading, setLoading] = useState(true)

  const refreshCart = useCallback(async () => {
    try {
      const next = await api.getCart()
      setCart(next)
    } catch {
      setCart({ items: [], itemCount: 0, subtotal: 0 })
    }
  }, [])

  useEffect(() => {
    refreshCart().finally(() => setLoading(false))
  }, [refreshCart])

  const addToCart = useCallback(
    async (productId: number, quantity = 1) => {
      const next = await api.addToCart(productId, quantity)
      setCart(next)
    },
    [],
  )

  const setUser = useCallback((next: User | null, token?: string | null) => {
    setUserState(next)
    if (next) localStorage.setItem('karwan_user', JSON.stringify(next))
    else localStorage.removeItem('karwan_user')
    if (token) localStorage.setItem('karwan_token', token)
    if (token === null) localStorage.removeItem('karwan_token')
  }, [])

  const logout = useCallback(() => {
    setUser(null, null)
  }, [setUser])

  const value = useMemo(
    () => ({ cart, user, loading, refreshCart, addToCart, setUser, logout }),
    [cart, user, loading, refreshCart, addToCart, setUser, logout],
  )

  return <StoreContext.Provider value={value}>{children}</StoreContext.Provider>
}

export function useStore() {
  const ctx = useContext(StoreContext)
  if (!ctx) throw new Error('useStore must be used within StoreProvider')
  return ctx
}

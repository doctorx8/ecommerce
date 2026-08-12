import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { adminApi, type AdminUser } from './adminApi'

type AdminContextValue = {
  admin: AdminUser | null
  setAdmin: (admin: AdminUser | null, token?: string | null) => void
  logout: () => void
}

const AdminContext = createContext<AdminContextValue | null>(null)

export function AdminProvider({ children }: { children: ReactNode }) {
  const [admin, setAdminState] = useState<AdminUser | null>(() => {
    const raw = localStorage.getItem('karwan_admin')
    return raw ? (JSON.parse(raw) as AdminUser) : null
  })

  const setAdmin = useCallback((next: AdminUser | null, token?: string | null) => {
    setAdminState(next)
    if (next) localStorage.setItem('karwan_admin', JSON.stringify(next))
    else localStorage.removeItem('karwan_admin')
    if (token) localStorage.setItem('karwan_admin_token', token)
    if (token === null) localStorage.removeItem('karwan_admin_token')
  }, [])

  const logout = useCallback(() => {
    setAdmin(null, null)
  }, [setAdmin])

  const value = useMemo(() => ({ admin, setAdmin, logout }), [admin, setAdmin, logout])

  return <AdminContext.Provider value={value}>{children}</AdminContext.Provider>
}

export function useAdmin() {
  const ctx = useContext(AdminContext)
  if (!ctx) throw new Error('useAdmin must be used within AdminProvider')
  return ctx
}

export async function adminLogin(email: string, password: string, setAdmin: AdminContextValue['setAdmin']) {
  const res = await adminApi.login(email, password)
  setAdmin(res.admin, res.token)
  return res.admin
}

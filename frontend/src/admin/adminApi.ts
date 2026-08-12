import { money } from '../api/client'

const API_BASE = import.meta.env.VITE_API_BASE ?? '/api'

export type AdminUser = {
  id: number
  email: string
  firstName: string
  lastName: string
  role: string
}

export type AdminProduct = {
  id: number
  name: string
  slug: string
  sku: string
  model?: string
  price: number | string
  quantity: number
  stockStatus: string
  isActive: boolean
  isFeatured?: boolean
  subtractStock?: boolean
  updatedAt?: string
}

export type AdminOrder = {
  id: number
  orderNumber: string
  status: string
  paymentStatus: string
  paymentMethod?: string
  shippingMethod?: string
  email: string
  telephone?: string
  firstName: string
  lastName: string
  subtotal: number | string
  shippingCost: number | string
  discountTotal: number | string
  taxTotal?: number | string
  total: number | string
  comment?: string
  createdAt?: string
  customerId?: number | null
  items: {
    id: number
    name: string
    sku?: string
    quantity: number
    price: number | string
    total: number | string
  }[]
  history?: { id: number; status: string; comment?: string; createdAt?: string }[]
  shipping?: Record<string, string>
  billing?: Record<string, string>
}

export type AdminCustomer = {
  id: number
  email: string
  firstName: string
  lastName: string
  telephone?: string | null
  isActive: boolean
  newsletter?: boolean
  createdAt?: string
  orderCount: number
}

export type AdminCoupon = {
  id: number
  code: string
  name: string
  type: string
  discount: number | string
  minOrderTotal?: number | string | null
  maxUses?: number | null
  usedCount: number
  isActive: boolean
}

export type AdminOverview = {
  products: number
  activeProducts: number
  lowStock: number
  outOfStock: number
  customers: number
  coupons: number
  orders: number
  revenue: number | string
  categories: number
  ordersByStatus: Record<string, number>
  recentOrders: AdminOrder[]
  lowStockProducts: AdminProduct[]
}

export const ORDER_STATUSES = [
  'PENDING',
  'PROCESSING',
  'SHIPPED',
  'DELIVERED',
  'CANCELLED',
  'REFUNDED',
] as const

export const PAYMENT_STATUSES = ['PENDING', 'PAID', 'FAILED', 'REFUNDED'] as const
export const STOCK_STATUSES = ['IN_STOCK', 'OUT_OF_STOCK', 'PREORDER', 'BACKORDER'] as const

function adminHeaders(): HeadersInit {
  const token = localStorage.getItem('karwan_admin_token')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

async function adminRequest<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers: HeadersInit = {
    'Content-Type': 'application/json',
    ...adminHeaders(),
    ...(init.headers || {}),
  }
  const res = await fetch(`${API_BASE}${path}`, { ...init, headers })
  if (res.status === 204) return undefined as T
  const data = await res.json().catch(() => ({}))
  if (!res.ok) {
    throw new Error(data.error || `Request failed (${res.status})`)
  }
  return data as T
}

export const adminApi = {
  login: (email: string, password: string) =>
    adminRequest<{ token: string; admin: AdminUser }>('/auth/admin/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    }),
  overview: () => adminRequest<AdminOverview>('/admin/overview'),
  products: (params: Record<string, string | number | boolean | undefined> = {}) => {
    const qs = new URLSearchParams()
    Object.entries(params).forEach(([k, v]) => {
      if (v !== undefined && v !== '') qs.set(k, String(v))
    })
    const q = qs.toString()
    return adminRequest<{ items: AdminProduct[]; total: number; page: number; totalPages: number }>(
      `/admin/products${q ? `?${q}` : ''}`,
    )
  },
  updateInventory: (
    id: number,
    payload: { quantity?: number; stockStatus?: string; active?: boolean },
  ) =>
    adminRequest<AdminProduct>(`/admin/products/${id}/inventory`, {
      method: 'PATCH',
      body: JSON.stringify(payload),
    }),
  orders: (params: Record<string, string | number | undefined> = {}) => {
    const qs = new URLSearchParams()
    Object.entries(params).forEach(([k, v]) => {
      if (v !== undefined && v !== '') qs.set(k, String(v))
    })
    const q = qs.toString()
    return adminRequest<{ items: AdminOrder[]; total: number; page: number; totalPages: number }>(
      `/admin/orders${q ? `?${q}` : ''}`,
    )
  },
  order: (id: number) => adminRequest<AdminOrder>(`/admin/orders/${id}`),
  updateOrder: (
    id: number,
    payload: {
      status?: string
      paymentStatus?: string
      comment?: string
      notifyCustomer?: boolean
    },
  ) =>
    adminRequest<AdminOrder>(`/admin/orders/${id}`, {
      method: 'PATCH',
      body: JSON.stringify(payload),
    }),
  customers: (params: Record<string, string | number | undefined> = {}) => {
    const qs = new URLSearchParams()
    Object.entries(params).forEach(([k, v]) => {
      if (v !== undefined && v !== '') qs.set(k, String(v))
    })
    const q = qs.toString()
    return adminRequest<{ items: AdminCustomer[]; total: number; page: number; totalPages: number }>(
      `/admin/customers${q ? `?${q}` : ''}`,
    )
  },
  coupons: () => adminRequest<AdminCoupon[]>('/admin/coupons'),
  createCoupon: (payload: Record<string, unknown>) =>
    adminRequest<AdminCoupon>('/admin/coupons', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),
  updateCoupon: (id: number, payload: Record<string, unknown>) =>
    adminRequest<AdminCoupon>(`/admin/coupons/${id}`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    }),
  deleteCoupon: (id: number) =>
    adminRequest<void>(`/admin/coupons/${id}`, { method: 'DELETE' }),
}

export { money }

export function formatDate(value?: string) {
  if (!value) return '—'
  return new Intl.DateTimeFormat('en-US', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value))
}

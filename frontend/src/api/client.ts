const API_BASE = import.meta.env.VITE_API_BASE ?? '/api'

export type Product = {
  id: number
  name: string
  slug: string
  sku: string
  model?: string
  description?: string
  shortDesc?: string
  price: number | string
  compareAtPrice?: number | string | null
  quantity: number
  stockStatus: string
  isFeatured?: boolean
  manufacturer?: { id: number; name: string; slug: string } | null
  images?: { id: number; image: string; alt?: string }[]
  categories?: { id: number; name: string; slug: string }[]
  options?: {
    id: number
    name: string
    required: boolean
    values: { id: number; name: string; priceModifier: number | string }[]
  }[]
}

export type Category = {
  id: number
  name: string
  slug: string
  description?: string
  children?: Category[]
}

export type Cart = {
  items: {
    id: number
    quantity: number
    product: Product
  }[]
  itemCount: number
  subtotal: number | string
}

export type Order = {
  id: number
  orderNumber: string
  status: string
  total: number | string
  subtotal: number | string
  discountTotal: number | string
  shippingCost: number | string
  items: { id: number; name: string; quantity: number; price: number | string; total: number | string }[]
  createdAt?: string
}

export type Address = {
  id: number
  firstName: string
  lastName: string
  company?: string | null
  address1: string
  address2?: string | null
  city: string
  postcode: string
  country: string
  zone?: string | null
  isDefault: boolean
}

export type CustomerProfile = {
  id: number
  email: string
  firstName: string
  lastName: string
  telephone?: string | null
  newsletter?: boolean
  createdAt?: string
  addresses?: Address[]
}

function createId(): string {
  // crypto.randomUUID() is missing on non-HTTPS origins (e.g. http://VPS_IP).
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID()
  }
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0
    const v = c === 'x' ? r : (r & 0x3) | 0x8
    return v.toString(16)
  })
}

function sessionId(): string {
  const key = 'karwan_session'
  let id = localStorage.getItem(key)
  if (!id) {
    id = `guest-${createId()}`
    localStorage.setItem(key, id)
  }
  return id
}

function authHeaders(): HeadersInit {
  const token = localStorage.getItem('karwan_token')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers: HeadersInit = {
    'Content-Type': 'application/json',
    ...authHeaders(),
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

export const api = {
  sessionId,
  getProducts: (params: Record<string, string | number | boolean | undefined> = {}) => {
    const qs = new URLSearchParams()
    Object.entries(params).forEach(([k, v]) => {
      if (v !== undefined && v !== '') qs.set(k, String(v))
    })
    const q = qs.toString()
    return request<{ items: Product[]; total: number; page: number; totalPages: number }>(
      `/products${q ? `?${q}` : ''}`,
    )
  },
  getProduct: (idOrSlug: string) => request<Product>(`/products/${idOrSlug}`),
  getCategories: () => request<Category[]>('/categories?tree=true'),
  getManufacturers: () =>
    request<{ id: number; name: string; slug: string }[]>('/manufacturers'),
  login: (email: string, password: string) =>
    request<{ token: string; customer: { id: number; email: string; firstName: string; lastName: string } }>(
      '/auth/login',
      { method: 'POST', body: JSON.stringify({ email, password }) },
    ),
  register: (payload: {
    email: string
    password: string
    firstName: string
    lastName: string
    telephone?: string
  }) =>
    request<{ token: string; customer: { id: number; email: string; firstName: string; lastName: string } }>(
      '/auth/register',
      { method: 'POST', body: JSON.stringify(payload) },
    ),
  me: () => request<CustomerProfile>('/auth/me'),
  updateProfile: (payload: {
    email: string
    firstName: string
    lastName: string
    telephone?: string
    newsletter?: boolean
  }) =>
    request<CustomerProfile>('/auth/me', {
      method: 'PUT',
      body: JSON.stringify(payload),
    }),
  changePassword: (currentPassword: string, newPassword: string) =>
    request<void>('/auth/password', {
      method: 'PUT',
      body: JSON.stringify({ currentPassword, newPassword }),
    }),
  deleteAccount: (password: string) =>
    request<void>('/auth/me', {
      method: 'DELETE',
      body: JSON.stringify({ password }),
    }),
  getAddresses: () => request<Address[]>('/addresses'),
  createAddress: (payload: Omit<Address, 'id'>) =>
    request<Address>('/addresses', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),
  updateAddress: (id: number, payload: Omit<Address, 'id'>) =>
    request<Address>(`/addresses/${id}`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    }),
  deleteAddress: (id: number) =>
    request<void>(`/addresses/${id}`, { method: 'DELETE' }),
  getCart: () => request<Cart>(`/cart?sessionId=${sessionId()}`),
  addToCart: (productId: number, quantity = 1) =>
    request<Cart>('/cart', {
      method: 'POST',
      body: JSON.stringify({ productId, quantity, sessionId: sessionId() }),
    }),
  updateCartItem: (id: number, quantity: number) =>
    request<Cart>(`/cart/${id}?sessionId=${sessionId()}`, {
      method: 'PUT',
      body: JSON.stringify({ quantity }),
    }),
  removeCartItem: (id: number) =>
    request<Cart>(`/cart/${id}?sessionId=${sessionId()}`, { method: 'DELETE' }),
  quote: (couponCode?: string) =>
    request<{
      subtotal: number | string
      discountTotal: number | string
      shippingCost: number | string
      taxTotal: number | string
      total: number | string
      taxRate: number | string
      shippingFreeThreshold: number | string
    }>('/orders/quote', {
      method: 'POST',
      body: JSON.stringify({ sessionId: sessionId(), couponCode }),
    }),
  checkout: (payload: Record<string, unknown>) =>
    request<Order>('/orders/checkout', {
      method: 'POST',
      body: JSON.stringify({ ...payload, sessionId: sessionId() }),
    }),
  myOrders: () => request<Order[]>('/orders/mine'),
  forgotPassword: (email: string) =>
    request<{ ok: boolean; message: string }>('/auth/forgot-password', {
      method: 'POST',
      body: JSON.stringify({ email }),
    }),
  resetPassword: (token: string, newPassword: string) =>
    request<void>('/auth/reset-password', {
      method: 'POST',
      body: JSON.stringify({ token, newPassword }),
    }),
  getReviews: (productId: number) =>
    request<{ id: number; author: string; rating: number; text: string; createdAt?: string }[]>(
      `/reviews/product/${productId}`,
    ),
  createReview: (payload: { productId: number; rating: number; text: string; author?: string }) =>
    request('/reviews', { method: 'POST', body: JSON.stringify(payload) }),
  getWishlist: () =>
    request<{ id: number; product: Product; createdAt?: string }[]>('/wishlist'),
  addWishlist: (productId: number) =>
    request(`/wishlist/${productId}`, { method: 'POST' }),
  removeWishlist: (productId: number) =>
    request<void>(`/wishlist/${productId}`, { method: 'DELETE' }),
}

export function money(value: number | string | undefined | null): string {
  const n = Number(value ?? 0)
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(n)
}

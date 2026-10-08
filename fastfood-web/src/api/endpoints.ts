import { api } from './client'
import type {
  AuthResponse,
  Challenge,
  CompleteOrderResponse,
  CustomerLookup,
  MyLoyalty,
  Order,
  OrderStatus,
  Product,
  RedeemResponse,
  Reward,
  ServiceMode,
  Settings,
  User,
} from './types'

export const authApi = {
  register: (email: string, password: string, firstName: string) =>
    api<Challenge>('POST', '/auth/register', { email, password, firstName }),
  login: (email: string, password: string) =>
    api<Challenge>('POST', '/auth/login', { email, password }),
  verify: (challengeId: string, code: string) =>
    api<AuthResponse>('POST', '/auth/verify', { challengeId, code }),
  resend: (challengeId: string) =>
    api<Challenge>('POST', '/auth/resend', { challengeId }),
  me: () => api<User>('GET', '/auth/me'),
}

export const menuApi = {
  list: () => api<Product[]>('GET', '/products'),
  setAvailable: (id: number, available: boolean) =>
    api<Product>('PATCH', `/admin/products/${id}/availability?available=${available}`),
}

export const settingsApi = {
  get: () => api<Settings>('GET', '/settings'),
}

export const orderApi = {
  place: (serviceMode: ServiceMode, items: { productId: number; quantity: number }[]) =>
    api<Order>('POST', '/orders', { serviceMode, items }),
  mine: () => api<Order[]>('GET', '/orders/mine'),
  cancel: (id: number) => api<Order>('POST', `/orders/${id}/cancel`),

  active: () => api<Order[]>('GET', '/admin/orders'),
  updateStatus: (id: number, status: OrderStatus) =>
    api<Order>('PATCH', `/admin/orders/${id}/status`, { status }),
  complete: (id: number, loyaltyCode: string) =>
    api<CompleteOrderResponse>('POST', `/admin/orders/${id}/complete`, { loyaltyCode }),
}

export const loyaltyApi = {
  rewards: () => api<Reward[]>('GET', '/rewards'),
  me: () => api<MyLoyalty>('GET', '/loyalty/me'),
  lookup: (code: string) => api<CustomerLookup>('GET', `/admin/loyalty/customers/${encodeURIComponent(code)}`),
  redeem: (loyaltyCode: string, rewardId: number) =>
    api<RedeemResponse>('POST', '/admin/loyalty/redeem', { loyaltyCode, rewardId }),
}

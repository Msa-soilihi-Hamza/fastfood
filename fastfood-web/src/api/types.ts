// Types alignés sur les DTO de l'API Spring Boot

export type Role = 'CUSTOMER' | 'RESTAURANT'
export type ServiceMode = 'TAKEAWAY' | 'DINE_IN'
export type OrderStatus = 'RECEIVED' | 'PREPARING' | 'READY' | 'COMPLETED' | 'CANCELLED'

export interface User {
  id: number
  email: string
  firstName: string
  role: Role
  loyaltyCode: string
  points: number
}

export interface AuthResponse {
  token: string
  user: User
}

export type ChallengePurpose = 'VERIFY_EMAIL' | 'LOGIN'

/** Un code vient d'être envoyé par e-mail : il faut le saisir pour terminer. */
export interface Challenge {
  challengeId: string
  purpose: ChallengePurpose
  maskedEmail: string
  resendAvailableInSeconds: number
}

export interface Product {
  id: number
  name: string
  description: string | null
  price: number
  category: string
  available: boolean
}

export interface Settings {
  takeawayEnabled: boolean
  dineInEnabled: boolean
  pointsPerEuro: number
}

export interface OrderItem {
  productId: number
  productName: string
  unitPrice: number
  quantity: number
}

export interface Order {
  id: number
  customerFirstName: string
  serviceMode: ServiceMode
  status: OrderStatus
  total: number
  createdAt: string
  items: OrderItem[]
}

export interface CompleteOrderResponse {
  order: Order
  pointsEarned: number
  customerPoints: number
}

export interface Reward {
  id: number
  name: string
  costPoints: number
  active: boolean
}

export interface PointTransaction {
  delta: number
  reason: string
  createdAt: string
}

export interface MyLoyalty {
  loyaltyCode: string
  points: number
  history: PointTransaction[]
}

export interface CustomerLookup {
  firstName: string
  loyaltyCode: string
  points: number
}

export interface RedeemResponse {
  rewardName: string
  pointsSpent: number
  customerPoints: number
}

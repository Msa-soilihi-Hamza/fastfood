import type { OrderStatus, ServiceMode } from '../api/types'

const euroFormatter = new Intl.NumberFormat('fr-FR', { style: 'currency', currency: 'EUR' })

export const euros = (amount: number) => euroFormatter.format(amount)

export const time = (iso: string) =>
  new Date(iso).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' })

export const dateTime = (iso: string) =>
  new Date(iso).toLocaleString('fr-FR', { day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' })

export const statusLabel: Record<OrderStatus, string> = {
  RECEIVED: 'Reçue',
  PREPARING: 'En préparation',
  READY: 'Prête',
  COMPLETED: 'Remise',
  CANCELLED: 'Annulée',
}

export const modeLabel: Record<ServiceMode, string> = {
  TAKEAWAY: 'À emporter',
  DINE_IN: 'Sur place',
}

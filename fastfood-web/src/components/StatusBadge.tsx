import type { OrderStatus } from '../api/types'
import { statusLabel } from '../lib/format'

const styles: Record<OrderStatus, string> = {
  RECEIVED: 'bg-surface text-ink',
  PREPARING: 'bg-amber-100 text-amber-900',
  READY: 'bg-brand text-white',
  COMPLETED: 'bg-brand-soft text-brand',
  CANCELLED: 'bg-red-50 text-danger',
}

export function StatusBadge({ status }: { status: OrderStatus }) {
  return (
    <span className={`inline-flex rounded-full px-3 py-1 text-xs font-medium ${styles[status]}`}>
      {statusLabel[status]}
    </span>
  )
}

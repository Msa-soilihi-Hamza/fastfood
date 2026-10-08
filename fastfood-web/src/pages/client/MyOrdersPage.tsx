import { useCallback, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { ApiError } from '../../api/client'
import { orderApi } from '../../api/endpoints'
import type { Order, OrderStatus } from '../../api/types'
import { StatusBadge } from '../../components/StatusBadge'
import { Button, ErrorBanner, PageTitle } from '../../components/ui'
import { useAuth } from '../../context/AuthContext'
import { dateTime, euros, modeLabel } from '../../lib/format'
import { usePolling } from '../../lib/usePolling'

const STEPS: OrderStatus[] = ['RECEIVED', 'PREPARING', 'READY']
const STEP_LABELS = ['Reçue', 'En cuisine', 'Prête']

function Progress({ status }: { status: OrderStatus }) {
  const current = status === 'COMPLETED' ? STEPS.length : STEPS.indexOf(status)
  return (
    <ol className="grid grid-cols-3 gap-2" aria-label="Avancement de la commande">
      {STEP_LABELS.map((label, i) => (
        <li key={label} className="flex flex-col gap-1.5">
          <span className={`h-1.5 rounded-full ${i <= current ? 'bg-brand' : 'bg-line'}`} />
          <span className={`text-xs ${i <= current ? 'font-medium' : 'text-muted'}`}>{label}</span>
        </li>
      ))}
    </ol>
  )
}

export function MyOrdersPage() {
  const { user } = useAuth()
  const placedId = (useLocation().state as { placedId?: number } | null)?.placedId
  const load = useCallback(() => orderApi.mine(), [])
  const { data: orders, error, reload } = usePolling(load, 8000)
  const [actionError, setActionError] = useState<string | null>(null)

  const cancel = async (order: Order) => {
    setActionError(null)
    try {
      await orderApi.cancel(order.id)
      await reload()
    } catch (e) {
      setActionError(e instanceof ApiError ? e.message : "L'annulation a échoué")
    }
  }

  const active = orders?.filter((o) => ['RECEIVED', 'PREPARING', 'READY'].includes(o.status)) ?? []
  const past = orders?.filter((o) => !active.includes(o)) ?? []

  return (
    <div className="mx-auto max-w-2xl">
      <PageTitle subtitle="L'avancement se met à jour automatiquement.">Mes commandes</PageTitle>

      {placedId && (
        <p className="mb-6 rounded-xl bg-brand-soft px-5 py-4 text-sm text-brand">
          <strong>Commande n°{placedId} envoyée.</strong> Le restaurant la prépare, vous pouvez suivre son avancement ici.
        </p>
      )}
      {error && <ErrorBanner>{error}</ErrorBanner>}
      {actionError && <div className="mb-4"><ErrorBanner>{actionError}</ErrorBanner></div>}

      {orders && orders.length === 0 && (
        <div className="rounded-2xl bg-surface p-8 text-center">
          <p className="font-medium">Aucune commande pour l'instant.</p>
          <Link to="/" className="mt-4 inline-flex min-h-11 items-center rounded-full bg-ink px-5 text-sm font-medium text-white">
            Voir le menu
          </Link>
        </div>
      )}

      <div className="flex flex-col gap-4">
        {active.map((order) => (
          <article key={order.id} className="flex flex-col gap-5 rounded-2xl border border-line p-6">
            <header className="flex items-start justify-between gap-4">
              <div>
                <h2 className="text-lg font-bold">Commande n°{order.id}</h2>
                <p className="text-sm text-muted">{modeLabel[order.serviceMode]} · {dateTime(order.createdAt)}</p>
              </div>
              <StatusBadge status={order.status} />
            </header>

            <Progress status={order.status} />

            {order.status === 'READY' && user && (
              <div className="rounded-xl bg-ink p-5 text-white">
                <p className="text-sm text-neutral-300">Votre commande est prête. Au comptoir, donnez ce code :</p>
                <p className="mt-1 font-mono text-3xl font-bold tracking-[0.3em]">{user.loyaltyCode}</p>
              </div>
            )}

            <ul className="text-sm">
              {order.items.map((item) => (
                <li key={item.productId} className="flex justify-between py-1">
                  <span>{item.quantity} × {item.productName}</span>
                  <span className="text-muted">{euros(item.unitPrice * item.quantity)}</span>
                </li>
              ))}
            </ul>

            <footer className="flex items-center justify-between border-t border-line pt-4">
              <span className="font-bold">{euros(order.total)} <span className="font-normal text-muted">· à payer sur place</span></span>
              {order.status === 'RECEIVED' && (
                <Button variant="secondary" onClick={() => cancel(order)}>Annuler</Button>
              )}
            </footer>
          </article>
        ))}
      </div>

      {past.length > 0 && (
        <section className="mt-12">
          <h2 className="mb-4 text-xl font-bold">Historique</h2>
          <ul className="divide-y divide-line">
            {past.map((order) => (
              <li key={order.id} className="flex items-center justify-between gap-4 py-4">
                <div>
                  <p className="font-medium">Commande n°{order.id} · {euros(order.total)}</p>
                  <p className="text-sm text-muted">{dateTime(order.createdAt)}</p>
                </div>
                <StatusBadge status={order.status} />
              </li>
            ))}
          </ul>
        </section>
      )}
    </div>
  )
}

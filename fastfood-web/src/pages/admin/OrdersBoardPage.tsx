import { useCallback, useState, type FormEvent } from 'react'
import { ApiError } from '../../api/client'
import { orderApi } from '../../api/endpoints'
import type { Order, OrderStatus } from '../../api/types'
import { Button, ErrorBanner, PageTitle } from '../../components/ui'
import { euros, modeLabel, time } from '../../lib/format'
import { usePolling } from '../../lib/usePolling'

const COLUMNS: { status: OrderStatus; title: string }[] = [
  { status: 'RECEIVED', title: 'Reçues' },
  { status: 'PREPARING', title: 'En préparation' },
  { status: 'READY', title: 'Prêtes' },
]

/** Remise au comptoir : le client donne son code, les points sont crédités. */
function HandOverForm({ order, onDone }: { order: Order; onDone: (message: string) => void }) {
  const [code, setCode] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const submit = async (event: FormEvent) => {
    event.preventDefault()
    setSubmitting(true)
    setError(null)
    try {
      const result = await orderApi.complete(order.id, code)
      onDone(`Commande n°${order.id} remise à ${order.customerFirstName} · +${result.pointsEarned} points (total ${result.customerPoints})`)
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'La remise a échoué')
      setSubmitting(false)
    }
  }

  return (
    <form onSubmit={submit} className="flex flex-col gap-2">
      <label className="flex flex-col gap-1">
        <span className="text-xs font-medium text-muted">Code du client</span>
        <input
          value={code}
          onChange={(e) => setCode(e.target.value.toUpperCase())}
          maxLength={6}
          required
          placeholder="ABC123"
          autoComplete="off"
          className="min-h-11 rounded-lg border-2 border-transparent bg-surface px-3 font-mono text-lg tracking-widest outline-none focus:border-ink focus:bg-white"
        />
      </label>
      {error && <p className="text-sm text-danger">{error}</p>}
      <Button type="submit" disabled={submitting || code.length < 6}>Payée et remise</Button>
    </form>
  )
}

export function OrdersBoardPage() {
  const load = useCallback(() => orderApi.active(), [])
  const { data: orders, error, reload } = usePolling(load, 5000)
  const [notice, setNotice] = useState<string | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)

  const move = async (order: Order, status: OrderStatus) => {
    setActionError(null)
    try {
      await orderApi.updateStatus(order.id, status)
      await reload()
    } catch (e) {
      setActionError(e instanceof ApiError ? e.message : 'Action impossible')
    }
  }

  const handedOver = async (message: string) => {
    setNotice(message)
    await reload()
  }

  return (
    <div>
      <PageTitle subtitle="Mise à jour automatique toutes les 5 secondes.">Commandes en cours</PageTitle>

      {notice && (
        <p className="mb-6 flex items-center justify-between gap-4 rounded-xl bg-brand-soft px-5 py-4 text-sm text-brand">
          <span>{notice}</span>
          <button type="button" onClick={() => setNotice(null)} className="font-medium underline">OK</button>
        </p>
      )}
      {error && <div className="mb-4"><ErrorBanner>{error}</ErrorBanner></div>}
      {actionError && <div className="mb-4"><ErrorBanner>{actionError}</ErrorBanner></div>}

      <div className="grid gap-6 md:grid-cols-3">
        {COLUMNS.map(({ status, title }) => {
          const column = orders?.filter((o) => o.status === status) ?? []
          return (
            <section key={status} className="flex flex-col gap-3 rounded-2xl bg-surface p-4">
              <h2 className="flex items-center justify-between px-1 font-bold">
                {title}
                <span className="rounded-full bg-white px-2.5 py-0.5 text-sm">{column.length}</span>
              </h2>

              {column.length === 0 && <p className="px-1 py-6 text-center text-sm text-muted">Aucune commande</p>}

              {column.map((order) => (
                <article key={order.id} className="flex flex-col gap-3 rounded-xl bg-white p-4 shadow-sm">
                  <header className="flex items-baseline justify-between">
                    <h3 className="text-lg font-bold">n°{order.id} · {order.customerFirstName}</h3>
                    <span className="text-sm text-muted">{time(order.createdAt)}</span>
                  </header>
                  <span className={`self-start rounded-full px-3 py-1 text-xs font-medium ${order.serviceMode === 'DINE_IN' ? 'bg-ink text-white' : 'bg-surface'}`}>
                    {modeLabel[order.serviceMode]}
                  </span>
                  <ul className="text-sm">
                    {order.items.map((item) => (
                      <li key={item.productId}><strong>{item.quantity}×</strong> {item.productName}</li>
                    ))}
                  </ul>
                  <p className="text-sm text-muted">À encaisser : <strong className="text-ink">{euros(order.total)}</strong></p>

                  {status === 'RECEIVED' && (
                    <div className="flex gap-2">
                      <Button className="flex-1" onClick={() => move(order, 'PREPARING')}>Préparer</Button>
                      <Button variant="secondary" onClick={() => move(order, 'CANCELLED')}>Refuser</Button>
                    </div>
                  )}
                  {status === 'PREPARING' && (
                    <Button onClick={() => move(order, 'READY')}>Marquer prête</Button>
                  )}
                  {status === 'READY' && <HandOverForm order={order} onDone={handedOver} />}
                </article>
              ))}
            </section>
          )
        })}
      </div>
    </div>
  )
}

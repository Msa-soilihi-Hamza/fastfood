import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { ApiError } from '../api/client'
import { orderApi } from '../api/endpoints'
import type { ServiceMode, Settings } from '../api/types'
import { useAuth } from '../context/AuthContext'
import { useCart } from '../context/CartContext'
import { euros, modeLabel } from '../lib/format'
import { Button, ErrorBanner, QuantityStepper } from './ui'

/** Le panier : quantités, choix du mode (selon les réglages du restaurant) et validation. */
export function CartPanel({ settings }: { settings: Settings | null }) {
  const { lines, total, setQuantity, clear } = useCart()
  const { user } = useAuth()
  const navigate = useNavigate()

  const modes: ServiceMode[] = settings
    ? (['TAKEAWAY', 'DINE_IN'] as const).filter((m) => (m === 'TAKEAWAY' ? settings.takeawayEnabled : settings.dineInEnabled))
    : []
  const [chosenMode, setChosenMode] = useState<ServiceMode | null>(null)
  const mode = chosenMode && modes.includes(chosenMode) ? chosenMode : modes[0]

  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const placeOrder = async () => {
    if (!user) {
      navigate('/login', { state: { from: '/' } })
      return
    }
    if (!mode) return
    setSubmitting(true)
    setError(null)
    try {
      const order = await orderApi.place(mode, lines.map((l) => ({ productId: l.product.id, quantity: l.quantity })))
      clear()
      navigate('/orders', { state: { placedId: order.id } })
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'La commande a échoué')
    } finally {
      setSubmitting(false)
    }
  }

  if (lines.length === 0) {
    return (
      <div className="rounded-2xl border border-line p-6">
        <h2 className="text-xl font-bold">Votre panier</h2>
        <p className="mt-2 text-sm text-muted">Ajoutez des plats pour commencer votre commande.</p>
      </div>
    )
  }

  return (
    <div className="flex flex-col gap-5 rounded-2xl border border-line p-6">
      <h2 className="text-xl font-bold">Votre panier</h2>

      <ul className="flex flex-col divide-y divide-line">
        {lines.map(({ product, quantity }) => (
          <li key={product.id} className="flex items-center gap-3 py-3">
            <div className="min-w-0 flex-1">
              <p className="truncate font-medium">{product.name}</p>
              <p className="text-sm text-muted">{euros(product.price * quantity)}</p>
            </div>
            <QuantityStepper value={quantity} onChange={(q) => setQuantity(product.id, q)} label={product.name} />
          </li>
        ))}
      </ul>

      {modes.length > 0 && (
        <fieldset>
          <legend className="mb-2 text-sm font-medium">Comment souhaitez-vous manger ?</legend>
          <div className="grid grid-cols-2 gap-1 rounded-full bg-surface p-1">
            {modes.map((m) => (
              <label
                key={m}
                className={`flex min-h-10 cursor-pointer items-center justify-center rounded-full text-sm font-medium transition-colors has-[:focus-visible]:ring-2 has-[:focus-visible]:ring-ink ${mode === m ? 'bg-white shadow-sm' : 'text-muted'} ${modes.length === 1 ? 'col-span-2' : ''}`}
              >
                <input type="radio" name="mode" value={m} checked={mode === m} onChange={() => setChosenMode(m)} className="sr-only" />
                {modeLabel[m]}
              </label>
            ))}
          </div>
        </fieldset>
      )}

      <div className="flex items-baseline justify-between border-t border-line pt-4">
        <span className="font-medium">Total</span>
        <span className="text-xl font-bold">{euros(total)}</span>
      </div>
      <p className="-mt-3 text-xs text-muted">
        Paiement sur place.{settings && settings.pointsPerEuro > 0 && ` Vous gagnerez ${Math.floor(total * settings.pointsPerEuro)} points.`}
      </p>

      {error && <ErrorBanner>{error}</ErrorBanner>}

      <Button onClick={placeOrder} disabled={submitting || !mode} className="min-h-12 text-base">
        {submitting ? 'Envoi…' : user ? `Commander · ${euros(total)}` : 'Se connecter pour commander'}
      </Button>
    </div>
  )
}

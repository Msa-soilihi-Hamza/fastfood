import { useEffect, useState, type FormEvent } from 'react'
import { ApiError } from '../../api/client'
import { loyaltyApi } from '../../api/endpoints'
import type { CustomerLookup, Reward } from '../../api/types'
import { Button, ErrorBanner, PageTitle } from '../../components/ui'

/** Comptoir : retrouver un client par son code et lui offrir un cadeau. */
export function CounterPage() {
  const [code, setCode] = useState('')
  const [customer, setCustomer] = useState<CustomerLookup | null>(null)
  const [rewards, setRewards] = useState<Reward[]>([])
  const [error, setError] = useState<string | null>(null)
  const [notice, setNotice] = useState<string | null>(null)

  useEffect(() => {
    loyaltyApi.rewards().then(setRewards).catch(() => setError('Impossible de charger les cadeaux.'))
  }, [])

  const lookup = async (event: FormEvent) => {
    event.preventDefault()
    setError(null)
    setNotice(null)
    setCustomer(null)
    try {
      setCustomer(await loyaltyApi.lookup(code))
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'Recherche impossible')
    }
  }

  const redeem = async (reward: Reward) => {
    if (!customer) return
    setError(null)
    try {
      const result = await loyaltyApi.redeem(customer.loyaltyCode, reward.id)
      setCustomer({ ...customer, points: result.customerPoints })
      setNotice(`Cadeau remis à ${customer.firstName} : ${result.rewardName} · −${result.pointsSpent} points`)
    } catch (e) {
      setError(e instanceof ApiError ? e.message : "L'échange a échoué")
    }
  }

  return (
    <div className="mx-auto max-w-xl">
      <PageTitle subtitle="Saisissez le code que le client vous donne.">Comptoir fidélité</PageTitle>

      <form onSubmit={lookup} className="flex gap-2">
        <label className="sr-only" htmlFor="code">Code du client</label>
        <input
          id="code"
          value={code}
          onChange={(e) => setCode(e.target.value.toUpperCase())}
          maxLength={6}
          required
          placeholder="ABC123"
          autoComplete="off"
          className="min-h-12 flex-1 rounded-full border-2 border-transparent bg-surface px-5 font-mono text-xl tracking-widest outline-none focus:border-ink focus:bg-white"
        />
        <Button type="submit" disabled={code.length < 6} className="min-h-12">Rechercher</Button>
      </form>

      <div className="mt-6 flex flex-col gap-4">
        {error && <ErrorBanner>{error}</ErrorBanner>}
        {notice && <p className="rounded-xl bg-brand-soft px-5 py-4 text-sm text-brand">{notice}</p>}
      </div>

      {customer && (
        <section className="mt-6 rounded-2xl border border-line p-6">
          <div className="flex items-baseline justify-between">
            <h2 className="text-2xl font-bold">{customer.firstName}</h2>
            <p><span className="text-3xl font-bold">{customer.points}</span> <span className="text-muted">points</span></p>
          </div>

          <ul className="mt-6 divide-y divide-line">
            {rewards.map((reward) => {
              const affordable = customer.points >= reward.costPoints
              return (
                <li key={reward.id} className="flex items-center justify-between gap-4 py-3">
                  <div>
                    <p className="font-medium">{reward.name}</p>
                    <p className="text-sm text-muted">{reward.costPoints} points</p>
                  </div>
                  <Button variant={affordable ? 'primary' : 'secondary'} disabled={!affordable} onClick={() => redeem(reward)}>
                    Offrir
                  </Button>
                </li>
              )
            })}
          </ul>
        </section>
      )}
    </div>
  )
}

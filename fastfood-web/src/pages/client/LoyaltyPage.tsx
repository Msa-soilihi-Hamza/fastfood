import { useEffect, useState } from 'react'
import { useLocation } from 'react-router-dom'
import { loyaltyApi } from '../../api/endpoints'
import type { MyLoyalty, Reward } from '../../api/types'
import { ErrorBanner, PageTitle } from '../../components/ui'
import { dateTime } from '../../lib/format'

export function LoyaltyPage() {
  const welcome = (useLocation().state as { welcome?: boolean } | null)?.welcome
  const [loyalty, setLoyalty] = useState<MyLoyalty | null>(null)
  const [rewards, setRewards] = useState<Reward[]>([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    Promise.all([loyaltyApi.me(), loyaltyApi.rewards()])
      .then(([l, r]) => {
        setLoyalty(l)
        setRewards(r)
      })
      .catch(() => setError('Impossible de charger vos points.'))
  }, [])

  const next = loyalty ? rewards.find((r) => r.costPoints > loyalty.points) : undefined

  return (
    <div className="mx-auto max-w-2xl">
      <PageTitle subtitle="1 € dépensé = des points. Échangez-les contre des cadeaux au comptoir.">Fidélité</PageTitle>

      {welcome && (
        <p className="mb-6 rounded-xl bg-brand-soft px-5 py-4 text-sm text-brand">
          <strong>Bienvenue !</strong> Voici votre code personnel : donnez-le au comptoir pour cumuler des points.
        </p>
      )}
      {error && <ErrorBanner>{error}</ErrorBanner>}

      {loyalty && (
        <>
          <section className="grid gap-4 sm:grid-cols-2">
            <div className="rounded-2xl bg-ink p-6 text-white">
              <p className="text-sm text-neutral-300">Votre code</p>
              <p className="mt-2 font-mono text-4xl font-bold tracking-[0.25em]">{loyalty.loyaltyCode}</p>
              <p className="mt-3 text-xs text-neutral-400">À donner au comptoir à chaque commande.</p>
            </div>
            <div className="rounded-2xl bg-surface p-6">
              <p className="text-sm text-muted">Vos points</p>
              <p className="mt-2 text-4xl font-bold">{loyalty.points}</p>
              {next ? (
                <>
                  <div className="mt-4 h-2 overflow-hidden rounded-full bg-line">
                    <div className="h-full rounded-full bg-brand" style={{ width: `${Math.min(100, (loyalty.points / next.costPoints) * 100)}%` }} />
                  </div>
                  <p className="mt-2 text-xs text-muted">Encore {next.costPoints - loyalty.points} points pour : {next.name}</p>
                </>
              ) : (
                rewards.length > 0 && <p className="mt-3 text-xs text-brand">Tous les cadeaux sont à votre portée.</p>
              )}
            </div>
          </section>

          <section className="mt-12">
            <h2 className="mb-4 text-xl font-bold">Cadeaux</h2>
            <ul className="divide-y divide-line">
              {rewards.map((reward) => {
                const unlocked = loyalty.points >= reward.costPoints
                return (
                  <li key={reward.id} className="flex items-center justify-between py-4">
                    <span className="font-medium">{reward.name}</span>
                    <span className={`rounded-full px-3 py-1 text-sm font-medium ${unlocked ? 'bg-brand text-white' : 'bg-surface text-muted'}`}>
                      {reward.costPoints} pts
                    </span>
                  </li>
                )
              })}
            </ul>
          </section>

          <section className="mt-12">
            <h2 className="mb-4 text-xl font-bold">Historique</h2>
            {loyalty.history.length === 0 ? (
              <p className="text-sm text-muted">Aucun mouvement pour l'instant.</p>
            ) : (
              <ul className="divide-y divide-line">
                {loyalty.history.map((t, i) => (
                  <li key={i} className="flex items-center justify-between py-3">
                    <div>
                      <p className="text-sm font-medium">{t.reason}</p>
                      <p className="text-xs text-muted">{dateTime(t.createdAt)}</p>
                    </div>
                    <span className={`font-bold ${t.delta > 0 ? 'text-brand' : ''}`}>{t.delta > 0 ? '+' : ''}{t.delta}</span>
                  </li>
                ))}
              </ul>
            )}
          </section>
        </>
      )}
    </div>
  )
}

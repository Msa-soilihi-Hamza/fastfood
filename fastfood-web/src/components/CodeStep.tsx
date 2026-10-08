import { useEffect, useState, type FormEvent } from 'react'
import { ApiError } from '../api/client'
import { authApi } from '../api/endpoints'
import type { Challenge, User } from '../api/types'
import { useAuth } from '../context/AuthContext'
import { Button, ErrorBanner } from './ui'

/** Deuxième étape : saisir le code à 6 chiffres reçu par e-mail. */
export function CodeStep({
  challenge: initial,
  onVerified,
  onBack,
}: {
  challenge: Challenge
  onVerified: (user: User) => void
  onBack: () => void
}) {
  const { verify } = useAuth()
  const [challenge, setChallenge] = useState(initial)
  const [code, setCode] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [info, setInfo] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [cooldown, setCooldown] = useState(initial.resendAvailableInSeconds)

  // Compte à rebours avant de pouvoir redemander un code
  useEffect(() => {
    if (cooldown <= 0) return
    const id = setTimeout(() => setCooldown((s) => s - 1), 1000)
    return () => clearTimeout(id)
  }, [cooldown])

  const submit = async (event: FormEvent) => {
    event.preventDefault()
    setSubmitting(true)
    setError(null)
    setInfo(null)
    try {
      onVerified(await verify(challenge.challengeId, code))
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'Vérification impossible')
      setCode('')
      setSubmitting(false)
    }
  }

  const resend = async () => {
    setError(null)
    setInfo(null)
    try {
      const next = await authApi.resend(challenge.challengeId)
      setChallenge(next)
      setCooldown(next.resendAvailableInSeconds)
      setInfo('Un nouveau code vient de vous être envoyé.')
    } catch (e) {
      setError(e instanceof ApiError ? e.message : "Le code n'a pas pu être renvoyé")
    }
  }

  const isSignup = challenge.purpose === 'VERIFY_EMAIL'

  return (
    <div>
      <h1 className="text-3xl font-bold tracking-tight">{isSignup ? 'Confirmez votre e-mail' : 'Vérification'}</h1>
      <p className="mb-8 mt-2 text-muted">
        Nous avons envoyé un code à 6 chiffres à <strong className="text-ink">{challenge.maskedEmail}</strong>.
        Il est valable 10 minutes.
      </p>

      <form onSubmit={submit} className="flex flex-col gap-4">
        <label className="flex flex-col gap-1.5">
          <span className="text-sm font-medium">Code reçu par e-mail</span>
          <input
            value={code}
            onChange={(e) => setCode(e.target.value.replace(/\D/g, '').slice(0, 6))}
            inputMode="numeric"
            autoComplete="one-time-code"
            autoFocus
            required
            placeholder="000000"
            className="min-h-14 rounded-lg border-2 border-transparent bg-surface px-4 text-center font-mono text-3xl tracking-[0.5em] outline-none focus:border-ink focus:bg-white"
          />
        </label>
        {error && <ErrorBanner>{error}</ErrorBanner>}
        {info && <p className="rounded-lg bg-brand-soft px-4 py-3 text-sm text-brand">{info}</p>}
        <Button type="submit" disabled={submitting || code.length !== 6} className="mt-2 min-h-12 text-base">
          {submitting ? 'Vérification…' : isSignup ? 'Activer mon compte' : 'Se connecter'}
        </Button>
      </form>

      <div className="mt-6 flex flex-wrap items-center justify-between gap-2 text-sm">
        <button type="button" onClick={onBack} className="min-h-11 font-medium text-muted underline-offset-4 hover:underline">
          ← Retour
        </button>
        <button
          type="button"
          onClick={resend}
          disabled={cooldown > 0}
          className="min-h-11 font-medium underline underline-offset-4 disabled:text-muted disabled:no-underline"
        >
          {cooldown > 0 ? `Renvoyer le code (${cooldown} s)` : 'Renvoyer le code'}
        </button>
      </div>
      <p className="mt-4 text-xs text-muted">Pensez à vérifier vos courriers indésirables.</p>
    </div>
  )
}

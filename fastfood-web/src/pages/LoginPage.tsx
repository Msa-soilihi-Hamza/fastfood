import { useState, type FormEvent } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { ApiError } from '../api/client'
import type { Challenge, User } from '../api/types'
import { CodeStep } from '../components/CodeStep'
import { Button, ErrorBanner, Field } from '../components/ui'
import { useAuth } from '../context/AuthContext'

export function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const from = (useLocation().state as { from?: string } | null)?.from

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [challenge, setChallenge] = useState<Challenge | null>(null)

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault()
    setSubmitting(true)
    setError(null)
    try {
      setChallenge(await login(email, password))
      setPassword('')
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'Connexion impossible')
    } finally {
      setSubmitting(false)
    }
  }

  const done = (user: User) => navigate(user.role === 'RESTAURANT' ? '/admin' : from ?? '/', { replace: true })

  if (challenge) {
    return (
      <div className="mx-auto max-w-sm">
        <CodeStep challenge={challenge} onVerified={done} onBack={() => setChallenge(null)} />
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-sm">
      <h1 className="text-3xl font-bold tracking-tight">Connexion</h1>
      <p className="mb-8 mt-2 text-muted">Un code de vérification vous sera envoyé par e-mail.</p>
      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
        <Field label="E-mail" type="email" autoComplete="email" required value={email} onChange={(e) => setEmail(e.target.value)} />
        <Field label="Mot de passe" type="password" autoComplete="current-password" required value={password} onChange={(e) => setPassword(e.target.value)} />
        {error && <ErrorBanner>{error}</ErrorBanner>}
        <Button type="submit" disabled={submitting} className="mt-2 min-h-12 text-base">
          {submitting ? 'Envoi du code…' : 'Continuer'}
        </Button>
      </form>
      <p className="mt-6 text-center text-sm text-muted">
        Pas encore de compte ?{' '}
        <Link to="/register" className="font-medium text-ink underline underline-offset-4">Créer un compte</Link>
      </p>
    </div>
  )
}

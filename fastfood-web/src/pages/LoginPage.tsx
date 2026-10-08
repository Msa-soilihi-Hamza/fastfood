import { useState, type FormEvent } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { ApiError } from '../api/client'
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

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault()
    setSubmitting(true)
    setError(null)
    try {
      const user = await login(email, password)
      navigate(user.role === 'RESTAURANT' ? '/admin' : from ?? '/', { replace: true })
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'Connexion impossible')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="mx-auto max-w-sm">
      <h1 className="mb-8 text-3xl font-bold tracking-tight">Connexion</h1>
      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
        <Field label="E-mail" type="email" autoComplete="email" required value={email} onChange={(e) => setEmail(e.target.value)} />
        <Field label="Mot de passe" type="password" autoComplete="current-password" required value={password} onChange={(e) => setPassword(e.target.value)} />
        {error && <ErrorBanner>{error}</ErrorBanner>}
        <Button type="submit" disabled={submitting} className="mt-2 min-h-12 text-base">
          {submitting ? 'Connexion…' : 'Se connecter'}
        </Button>
      </form>
      <p className="mt-6 text-center text-sm text-muted">
        Pas encore de compte ?{' '}
        <Link to="/register" className="font-medium text-ink underline underline-offset-4">Créer un compte</Link>
      </p>
    </div>
  )
}

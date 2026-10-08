import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { ApiError } from '../api/client'
import type { Challenge } from '../api/types'
import { CodeStep } from '../components/CodeStep'
import { isStrongPassword, PasswordRules } from '../components/PasswordRules'
import { Button, ErrorBanner, Field } from '../components/ui'
import { useAuth } from '../context/AuthContext'

export function RegisterPage() {
  const { register } = useAuth()
  const navigate = useNavigate()

  const [firstName, setFirstName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [submitting, setSubmitting] = useState(false)
  const [challenge, setChallenge] = useState<Challenge | null>(null)

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault()
    setSubmitting(true)
    setError(null)
    setFieldErrors({})
    try {
      setChallenge(await register(email, password, firstName))
      setPassword('')
    } catch (e) {
      if (e instanceof ApiError) {
        setError(e.message)
        setFieldErrors(e.fieldErrors)
      } else {
        setError('Inscription impossible')
      }
    } finally {
      setSubmitting(false)
    }
  }

  if (challenge) {
    return (
      <div className="mx-auto max-w-sm">
        <CodeStep
          challenge={challenge}
          onVerified={() => navigate('/loyalty', { replace: true, state: { welcome: true } })}
          onBack={() => setChallenge(null)}
        />
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-sm">
      <h1 className="text-3xl font-bold tracking-tight">Créer un compte</h1>
      <p className="mb-8 mt-2 text-muted">Commandez à l'avance et gagnez des points à chaque passage.</p>
      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
        <Field label="Prénom" autoComplete="given-name" required value={firstName} onChange={(e) => setFirstName(e.target.value)} error={fieldErrors.firstName} />
        <Field label="E-mail" type="email" autoComplete="email" required value={email} onChange={(e) => setEmail(e.target.value)} error={fieldErrors.email} />
        <Field label="Mot de passe" type="password" autoComplete="new-password" required value={password} onChange={(e) => setPassword(e.target.value)} error={fieldErrors.password} />
        <PasswordRules password={password} />
        {error && Object.keys(fieldErrors).length === 0 && <ErrorBanner>{error}</ErrorBanner>}
        <Button type="submit" disabled={submitting || !isStrongPassword(password)} className="mt-2 min-h-12 text-base">
          {submitting ? 'Création…' : 'Créer mon compte'}
        </Button>
      </form>
      <p className="mt-6 text-center text-sm text-muted">
        Déjà inscrit ?{' '}
        <Link to="/login" className="font-medium text-ink underline underline-offset-4">Se connecter</Link>
      </p>
    </div>
  )
}

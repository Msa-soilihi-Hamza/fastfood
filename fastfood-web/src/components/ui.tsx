import type { ButtonHTMLAttributes, InputHTMLAttributes, ReactNode } from 'react'

/** Petits composants d'interface partagés, pour garder un style cohérent partout. */

type Variant = 'primary' | 'secondary' | 'ghost'

const variants: Record<Variant, string> = {
  primary: 'bg-ink text-white hover:bg-neutral-800 disabled:bg-neutral-300',
  secondary: 'bg-surface text-ink hover:bg-line disabled:text-neutral-400',
  ghost: 'text-ink hover:bg-surface disabled:text-neutral-400',
}

export function Button({
  variant = 'primary',
  className = '',
  ...props
}: ButtonHTMLAttributes<HTMLButtonElement> & { variant?: Variant }) {
  return (
    <button
      type="button"
      className={`inline-flex min-h-11 items-center justify-center gap-2 rounded-full px-5 text-sm font-medium transition-colors disabled:cursor-not-allowed ${variants[variant]} ${className}`}
      {...props}
    />
  )
}

export function Field({
  label,
  error,
  hint,
  ...props
}: InputHTMLAttributes<HTMLInputElement> & { label: string; error?: string; hint?: string }) {
  return (
    <label className="flex flex-col gap-1.5">
      <span className="text-sm font-medium">{label}</span>
      <input
        className="min-h-12 rounded-lg border-2 border-transparent bg-surface px-4 text-base outline-none transition-colors focus:border-ink focus:bg-white"
        {...props}
      />
      {error ? (
        <span className="text-sm text-danger">{error}</span>
      ) : (
        hint && <span className="text-sm text-muted">{hint}</span>
      )}
    </label>
  )
}

export function ErrorBanner({ children }: { children: ReactNode }) {
  return (
    <p role="alert" className="rounded-lg bg-red-50 px-4 py-3 text-sm text-danger">
      {children}
    </p>
  )
}

export function QuantityStepper({
  value,
  onChange,
  label,
}: {
  value: number
  onChange: (value: number) => void
  label: string
}) {
  return (
    <div className="inline-flex items-center rounded-full bg-surface">
      <button
        type="button"
        aria-label={`Retirer un ${label}`}
        onClick={() => onChange(value - 1)}
        className="grid size-9 place-items-center rounded-full text-lg hover:bg-line"
      >
        −
      </button>
      <span className="min-w-6 text-center text-sm font-medium" aria-live="polite">{value}</span>
      <button
        type="button"
        aria-label={`Ajouter un ${label}`}
        onClick={() => onChange(value + 1)}
        className="grid size-9 place-items-center rounded-full text-lg hover:bg-line"
      >
        +
      </button>
    </div>
  )
}

export function PageTitle({ children, subtitle }: { children: ReactNode; subtitle?: ReactNode }) {
  return (
    <div className="mb-8">
      <h1 className="text-3xl font-bold tracking-tight sm:text-4xl">{children}</h1>
      {subtitle && <p className="mt-2 text-muted">{subtitle}</p>}
    </div>
  )
}

/** Mêmes règles que le serveur (StrongPasswordValidator) : 12 caractères dont un symbole. */
export const passwordRules = [
  { label: '12 caractères minimum', test: (p: string) => [...p].length >= 12 },
  { label: 'Un symbole (! ? @ # …)', test: (p: string) => /[^\p{L}\p{N}\s]/u.test(p) },
]

export const isStrongPassword = (password: string) => passwordRules.every((rule) => rule.test(password))

export function PasswordRules({ password }: { password: string }) {
  return (
    <ul className="flex flex-col gap-1 text-sm" aria-label="Règles du mot de passe">
      {passwordRules.map((rule) => {
        const ok = rule.test(password)
        return (
          <li key={rule.label} className={`flex items-center gap-2 ${ok ? 'text-brand' : 'text-muted'}`}>
            <span aria-hidden="true" className={`grid size-4 place-items-center rounded-full text-[10px] ${ok ? 'bg-brand text-white' : 'border border-line'}`}>
              {ok ? '✓' : ''}
            </span>
            {rule.label}
            <span className="sr-only">{ok ? ' : respecté' : ' : non respecté'}</span>
          </li>
        )
      })}
    </ul>
  )
}

const TOKEN_KEY = 'fastfood.token'

export const tokenStore = {
  get: (): string | null => {
    try {
      return localStorage.getItem(TOKEN_KEY)
    } catch {
      return null
    }
  },
  set: (token: string | null) => {
    try {
      if (token) localStorage.setItem(TOKEN_KEY, token)
      else localStorage.removeItem(TOKEN_KEY)
    } catch {
      // Stockage indisponible (navigation privée) : la session ne survivra pas au rechargement
    }
  },
}

/** Erreur renvoyée par l'API au format ProblemDetail (RFC 9457). */
export class ApiError extends Error {
  readonly status: number
  readonly fieldErrors: Record<string, string>

  constructor(status: number, message: string, fieldErrors: Record<string, string> = {}) {
    super(message)
    this.status = status
    this.fieldErrors = fieldErrors
  }
}

type Method = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'

export async function api<T>(method: Method, path: string, body?: unknown): Promise<T> {
  const headers: Record<string, string> = {}
  const token = tokenStore.get()
  if (token) headers.Authorization = `Bearer ${token}`
  if (body !== undefined) headers['Content-Type'] = 'application/json'

  const response = await fetch(`/api${path}`, {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
  })

  if (!response.ok) {
    const problem = await response.json().catch(() => null)
    throw new ApiError(
      response.status,
      problem?.detail ?? defaultMessage(response.status),
      problem?.errors ?? {},
    )
  }

  return response.status === 204 ? (undefined as T) : response.json()
}

function defaultMessage(status: number): string {
  if (status === 401) return 'Vous devez être connecté.'
  if (status === 403) return "Vous n'avez pas accès à cette page."
  if (status >= 500) return 'Le serveur ne répond pas, réessayez dans un instant.'
  return 'Une erreur est survenue.'
}

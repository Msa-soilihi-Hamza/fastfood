import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { tokenStore } from '../api/client'
import { authApi } from '../api/endpoints'
import type { AuthResponse, Challenge, User } from '../api/types'

interface AuthState {
  user: User | null
  /** Vrai tant qu'on vérifie le jeton enregistré au chargement de la page. */
  loading: boolean
  /** Étape 1 : vérifie le mot de passe, un code est envoyé par e-mail. */
  login: (email: string, password: string) => Promise<Challenge>
  /** Étape 1 : crée le compte, un code de confirmation est envoyé par e-mail. */
  register: (email: string, password: string, firstName: string) => Promise<Challenge>
  /** Étape 2 : le bon code connecte l'utilisateur. */
  verify: (challengeId: string, code: string) => Promise<User>
  logout: () => void
  refresh: () => Promise<void>
}

const AuthContext = createContext<AuthState | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [loading, setLoading] = useState(() => tokenStore.get() !== null)

  const refresh = useCallback(async () => {
    try {
      setUser(await authApi.me())
    } catch {
      // Jeton expiré ou invalide : on repart déconnecté
      tokenStore.set(null)
      setUser(null)
    }
  }, [])

  useEffect(() => {
    if (tokenStore.get()) refresh().finally(() => setLoading(false))
  }, [refresh])

  const accept = (response: AuthResponse) => {
    tokenStore.set(response.token)
    setUser(response.user)
    return response.user
  }

  const value = useMemo<AuthState>(() => ({
    user,
    loading,
    login: (email, password) => authApi.login(email, password),
    register: (email, password, firstName) => authApi.register(email, password, firstName),
    verify: async (challengeId, code) => accept(await authApi.verify(challengeId, code)),
    logout: () => {
      tokenStore.set(null)
      setUser(null)
    },
    refresh,
  }), [user, loading, refresh])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthState {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth doit être utilisé dans <AuthProvider>')
  return context
}

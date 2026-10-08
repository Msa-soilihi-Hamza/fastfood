import { useCallback, useEffect, useState } from 'react'

/**
 * Charge une donnée puis la recharge à intervalle régulier.
 * Suffisant pour suivre l'avancement d'une commande sans WebSocket.
 */
export function usePolling<T>(load: () => Promise<T>, intervalMs: number) {
  const [data, setData] = useState<T | null>(null)
  const [error, setError] = useState<string | null>(null)

  const reload = useCallback(async () => {
    try {
      setData(await load())
      setError(null)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Erreur de chargement')
    }
  }, [load])

  useEffect(() => {
    reload()
    const id = setInterval(reload, intervalMs)
    return () => clearInterval(id)
  }, [reload, intervalMs])

  return { data, error, reload }
}

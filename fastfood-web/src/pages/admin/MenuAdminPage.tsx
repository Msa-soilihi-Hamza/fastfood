import { useEffect, useState } from 'react'
import { ApiError } from '../../api/client'
import { menuApi } from '../../api/endpoints'
import type { Product } from '../../api/types'
import { ErrorBanner, PageTitle } from '../../components/ui'
import { euros } from '../../lib/format'

/** Marquer un plat comme épuisé ou de nouveau disponible. */
export function MenuAdminPage() {
  const [products, setProducts] = useState<Product[]>([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    menuApi.list().then(setProducts).catch(() => setError('Impossible de charger le menu.'))
  }, [])

  const toggle = async (product: Product) => {
    setError(null)
    try {
      const updated = await menuApi.setAvailable(product.id, !product.available)
      setProducts((current) => current.map((p) => (p.id === updated.id ? updated : p)))
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'Modification impossible')
    }
  }

  return (
    <div className="mx-auto max-w-2xl">
      <PageTitle subtitle="Un plat épuisé reste visible au menu mais ne peut plus être commandé.">Menu</PageTitle>
      {error && <ErrorBanner>{error}</ErrorBanner>}

      <ul className="divide-y divide-line">
        {products.map((product) => (
          <li key={product.id} className="flex items-center justify-between gap-4 py-4">
            <div>
              <p className={`font-medium ${product.available ? '' : 'text-muted line-through'}`}>{product.name}</p>
              <p className="text-sm text-muted">{product.category} · {euros(product.price)}</p>
            </div>
            <label className="flex min-h-11 cursor-pointer items-center gap-3">
              <span className="text-sm text-muted">{product.available ? 'Disponible' : 'Épuisé'}</span>
              <input
                type="checkbox"
                role="switch"
                checked={product.available}
                onChange={() => toggle(product)}
                className="peer sr-only"
              />
              <span className="relative h-7 w-12 rounded-full bg-line transition-colors after:absolute after:left-1 after:top-1 after:size-5 after:rounded-full after:bg-white after:shadow after:transition-transform peer-checked:bg-brand peer-checked:after:translate-x-5 peer-focus-visible:ring-2 peer-focus-visible:ring-ink" />
            </label>
          </li>
        ))}
      </ul>
    </div>
  )
}

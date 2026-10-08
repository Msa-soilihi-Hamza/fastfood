import { useEffect, useMemo, useState } from 'react'
import { menuApi, settingsApi } from '../../api/endpoints'
import type { Product, Settings } from '../../api/types'
import { CartPanel } from '../../components/CartPanel'
import { ProductCard } from '../../components/ProductCard'
import { ErrorBanner } from '../../components/ui'
import { useCart } from '../../context/CartContext'
import { euros } from '../../lib/format'

const CATEGORY_ORDER = ['Burgers', 'Accompagnements', 'Boissons', 'Desserts']

export function MenuPage() {
  const { add, count, total } = useCart()
  const [products, setProducts] = useState<Product[] | null>(null)
  const [settings, setSettings] = useState<Settings | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [cartOpen, setCartOpen] = useState(false)

  useEffect(() => {
    Promise.all([menuApi.list(), settingsApi.get()])
      .then(([p, s]) => {
        setProducts(p)
        setSettings(s)
      })
      .catch(() => setError("Le menu n'a pas pu être chargé. L'API est-elle démarrée ?"))
  }, [])

  // Regroupe les plats par catégorie, dans un ordre logique de repas
  const sections = useMemo(() => {
    const groups = new Map<string, Product[]>()
    for (const product of products ?? []) {
      groups.set(product.category, [...(groups.get(product.category) ?? []), product])
    }
    const rank = (c: string) => (CATEGORY_ORDER.indexOf(c) + 1 || CATEGORY_ORDER.length + 1)
    return [...groups.entries()].sort(([a], [b]) => rank(a) - rank(b))
  }, [products])

  return (
    <div className="grid gap-10 lg:grid-cols-[1fr_360px]">
      <div>
        <section className="mb-10">
          <h1 className="text-4xl font-bold tracking-tight sm:text-5xl">Commandez, on prépare.</h1>
          <p className="mt-3 max-w-lg text-lg text-muted">
            Choisissez vos plats, venez les chercher sans attendre. Paiement au comptoir, points de fidélité à chaque commande.
          </p>
        </section>

        {error && <ErrorBanner>{error}</ErrorBanner>}

        {sections.length > 0 && (
          <nav aria-label="Catégories" className="mb-8 flex gap-2 overflow-x-auto pb-1">
            {sections.map(([category]) => (
              <a key={category} href={`#${category}`} className="whitespace-nowrap rounded-full bg-surface px-4 py-2 text-sm font-medium hover:bg-line">
                {category}
              </a>
            ))}
          </nav>
        )}

        <div className="flex flex-col gap-12">
          {sections.map(([category, items]) => (
            <section key={category} id={category} className="scroll-mt-32">
              <h2 className="mb-5 text-2xl font-bold tracking-tight">{category}</h2>
              <div className="grid grid-cols-2 gap-x-4 gap-y-8 sm:grid-cols-3">
                {items.map((product) => (
                  <ProductCard key={product.id} product={product} onAdd={() => add(product)} />
                ))}
              </div>
            </section>
          ))}
        </div>
      </div>

      {/* Ordinateur : panier fixe sur le côté */}
      <aside className="hidden lg:block">
        <div className="sticky top-24">
          <CartPanel settings={settings} />
        </div>
      </aside>

      {/* Mobile : barre en bas qui ouvre le panier */}
      {count > 0 && !cartOpen && (
        <div className="fixed inset-x-0 bottom-0 z-30 p-4 lg:hidden">
          <button
            type="button"
            onClick={() => setCartOpen(true)}
            className="flex min-h-14 w-full items-center justify-between rounded-full bg-ink px-6 text-white shadow-lg"
          >
            <span className="font-medium">Voir le panier · {count}</span>
            <span className="font-bold">{euros(total)}</span>
          </button>
        </div>
      )}
      {cartOpen && (
        <div className="fixed inset-0 z-40 flex items-end bg-black/40 lg:hidden" onClick={() => setCartOpen(false)}>
          <div className="max-h-[85dvh] w-full overflow-y-auto rounded-t-3xl bg-white p-2" onClick={(e) => e.stopPropagation()}>
            <div className="flex justify-end">
              <button type="button" onClick={() => setCartOpen(false)} className="min-h-11 rounded-full px-4 text-sm font-medium hover:bg-surface">
                Fermer
              </button>
            </div>
            <CartPanel settings={settings} />
          </div>
        </div>
      )}
    </div>
  )
}

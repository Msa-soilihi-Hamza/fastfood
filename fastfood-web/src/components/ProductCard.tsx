import type { Product } from '../api/types'
import { euros } from '../lib/format'
import { CategoryIcon } from './CategoryIcon'

export function ProductCard({ product, onAdd }: { product: Product; onAdd: () => void }) {
  const soldOut = !product.available

  return (
    <article className="group flex flex-col">
      <div className="relative mb-3 grid aspect-[4/3] place-items-center overflow-hidden rounded-xl bg-surface">
        <CategoryIcon category={product.category} className={`size-16 ${soldOut ? 'text-neutral-300' : 'text-neutral-400'}`} />
        {soldOut ? (
          <span className="absolute left-3 top-3 rounded-full bg-white px-3 py-1 text-xs font-medium">Épuisé</span>
        ) : (
          <button
            type="button"
            onClick={onAdd}
            aria-label={`Ajouter ${product.name} au panier`}
            className="absolute bottom-3 right-3 grid size-11 place-items-center rounded-full bg-white text-2xl shadow-md transition-transform hover:scale-105 active:scale-95"
          >
            +
          </button>
        )}
      </div>
      <h3 className={`font-medium ${soldOut ? 'text-muted' : ''}`}>{product.name}</h3>
      <p className="text-sm text-muted">{euros(product.price)}</p>
      {product.description && <p className="mt-1 line-clamp-2 text-sm text-muted">{product.description}</p>}
    </article>
  )
}

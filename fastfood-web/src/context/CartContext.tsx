import { createContext, useContext, useMemo, useState, type ReactNode } from 'react'
import type { Product } from '../api/types'

export interface CartLine {
  product: Product
  quantity: number
}

interface CartState {
  lines: CartLine[]
  count: number
  total: number
  add: (product: Product) => void
  setQuantity: (productId: number, quantity: number) => void
  clear: () => void
}

const MAX_QUANTITY = 20

const CartContext = createContext<CartState | null>(null)

export function CartProvider({ children }: { children: ReactNode }) {
  const [lines, setLines] = useState<CartLine[]>([])

  const value = useMemo<CartState>(() => ({
    lines,
    count: lines.reduce((sum, line) => sum + line.quantity, 0),
    total: lines.reduce((sum, line) => sum + line.product.price * line.quantity, 0),
    add: (product) =>
      setLines((current) => {
        const existing = current.find((line) => line.product.id === product.id)
        if (!existing) return [...current, { product, quantity: 1 }]
        return current.map((line) =>
          line === existing ? { ...line, quantity: Math.min(line.quantity + 1, MAX_QUANTITY) } : line,
        )
      }),
    setQuantity: (productId, quantity) =>
      setLines((current) =>
        quantity <= 0
          ? current.filter((line) => line.product.id !== productId)
          : current.map((line) =>
              line.product.id === productId ? { ...line, quantity: Math.min(quantity, MAX_QUANTITY) } : line,
            ),
      ),
    clear: () => setLines([]),
  }), [lines])

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>
}

export function useCart(): CartState {
  const context = useContext(CartContext)
  if (!context) throw new Error('useCart doit être utilisé dans <CartProvider>')
  return context
}

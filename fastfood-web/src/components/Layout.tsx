import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

const navClass = ({ isActive }: { isActive: boolean }) =>
  `rounded-full px-4 py-2 text-sm font-medium transition-colors ${isActive ? 'bg-ink text-white' : 'hover:bg-surface'}`

export function Layout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const isRestaurant = user?.role === 'RESTAURANT'

  const handleLogout = () => {
    logout()
    navigate('/')
  }

  return (
    <div className="min-h-dvh">
      <header className="sticky top-0 z-20 border-b border-line bg-white/90 backdrop-blur">
        <div className="mx-auto flex max-w-6xl flex-wrap items-center gap-x-6 gap-y-2 px-4 py-3 sm:px-6">
          <Link to={isRestaurant ? '/admin' : '/'} className="text-xl font-bold tracking-tight">
            Fastfood<span className="text-brand">.</span>
          </Link>

          <nav className="order-3 flex w-full gap-1 overflow-x-auto sm:order-none sm:w-auto" aria-label="Navigation principale">
            {isRestaurant ? (
              <>
                <NavLink to="/admin" end className={navClass}>Commandes</NavLink>
                <NavLink to="/admin/counter" className={navClass}>Comptoir</NavLink>
                <NavLink to="/admin/menu" className={navClass}>Menu</NavLink>
              </>
            ) : (
              <>
                <NavLink to="/" end className={navClass}>Menu</NavLink>
                {user && <NavLink to="/orders" className={navClass}>Mes commandes</NavLink>}
                {user && <NavLink to="/loyalty" className={navClass}>Fidélité</NavLink>}
              </>
            )}
          </nav>

          <div className="ml-auto flex items-center gap-3">
            {user ? (
              <>
                <span className="hidden text-sm text-muted sm:inline">Bonjour, {user.firstName}</span>
                <button type="button" onClick={handleLogout} className="min-h-11 rounded-full px-4 text-sm font-medium hover:bg-surface">
                  Se déconnecter
                </button>
              </>
            ) : (
              <>
                <Link to="/login" className="flex min-h-11 items-center rounded-full px-4 text-sm font-medium hover:bg-surface">
                  Connexion
                </Link>
                <Link to="/register" className="flex min-h-11 items-center rounded-full bg-ink px-4 text-sm font-medium text-white hover:bg-neutral-800">
                  Inscription
                </Link>
              </>
            )}
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-6xl px-4 py-8 sm:px-6 sm:py-12">
        <Outlet />
      </main>
    </div>
  )
}

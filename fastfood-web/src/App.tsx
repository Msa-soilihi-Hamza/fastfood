import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { Layout } from './components/Layout'
import { ProtectedRoute } from './components/ProtectedRoute'
import { AuthProvider, useAuth } from './context/AuthContext'
import { CartProvider } from './context/CartContext'
import { CounterPage } from './pages/admin/CounterPage'
import { MenuAdminPage } from './pages/admin/MenuAdminPage'
import { OrdersBoardPage } from './pages/admin/OrdersBoardPage'
import { LoyaltyPage } from './pages/client/LoyaltyPage'
import { MenuPage } from './pages/client/MenuPage'
import { MyOrdersPage } from './pages/client/MyOrdersPage'
import { LoginPage } from './pages/LoginPage'
import { RegisterPage } from './pages/RegisterPage'

/** Le restaurateur n'a rien à faire sur le menu client : on l'envoie vers ses commandes. */
function HomeRoute() {
  const { user, loading } = useAuth()
  if (loading) return null
  return user?.role === 'RESTAURANT' ? <Navigate to="/admin" replace /> : <MenuPage />
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <CartProvider>
          <Routes>
            <Route element={<Layout />}>
              <Route index element={<HomeRoute />} />
              <Route path="login" element={<LoginPage />} />
              <Route path="register" element={<RegisterPage />} />

              <Route path="orders" element={<ProtectedRoute role="CUSTOMER"><MyOrdersPage /></ProtectedRoute>} />
              <Route path="loyalty" element={<ProtectedRoute role="CUSTOMER"><LoyaltyPage /></ProtectedRoute>} />

              <Route path="admin" element={<ProtectedRoute role="RESTAURANT"><OrdersBoardPage /></ProtectedRoute>} />
              <Route path="admin/counter" element={<ProtectedRoute role="RESTAURANT"><CounterPage /></ProtectedRoute>} />
              <Route path="admin/menu" element={<ProtectedRoute role="RESTAURANT"><MenuAdminPage /></ProtectedRoute>} />

              <Route path="*" element={<Navigate to="/" replace />} />
            </Route>
          </Routes>
        </CartProvider>
      </AuthProvider>
    </BrowserRouter>
  )
}

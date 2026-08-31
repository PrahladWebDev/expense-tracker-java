import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '@/app/providers/AuthProvider'

// CONCEPT: Route guard
// Wraps a set of routes and redirects to /login if there's no authenticated
// user, instead of every page needing to check this itself. <Outlet />
// renders whichever child route matched (React Router's composition model).
export default function ProtectedRoute() {
  const { isAuthenticated } = useAuth()
  if (!isAuthenticated) return <Navigate to="/login" replace />
  return <Outlet />
}

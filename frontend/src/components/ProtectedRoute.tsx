import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '@/app/providers/AuthProvider'

// CONCEPT: Route guard
// Wraps a set of routes and redirects to the landing page if there's no
// authenticated user (including right after logout), instead of dropping
// people straight onto a bare login form. <Outlet /> renders whichever
// child route matched (React Router's composition model).
export default function ProtectedRoute() {
  const { isAuthenticated } = useAuth()
  if (!isAuthenticated) return <Navigate to="/" replace />
  return <Outlet />
}

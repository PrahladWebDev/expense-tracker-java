import { createBrowserRouter, Navigate, Outlet } from 'react-router-dom'
import { AuthProvider } from '@/app/providers/AuthProvider'
import ProtectedRoute from '@/components/ProtectedRoute'
import Layout from '@/components/Layout'
import LoginPage from '@/features/auth/pages/LoginPage'
import RegisterPage from '@/features/auth/pages/RegisterPage'
import ProfilePage from '@/features/auth/pages/ProfilePage'
import DashboardPage from '@/features/dashboard/pages/DashboardPage'
import ExpensesListPage from '@/features/expenses/pages/ExpensesListPage'
import ExpenseFormPage from '@/features/expenses/pages/ExpenseFormPage'
import CategoriesPage from '@/features/categories/pages/CategoriesPage'
import BudgetsPage from '@/features/budgets/pages/BudgetsPage'

// CONCEPT: React Router (data router API)
// createBrowserRouter builds a route tree matched against the URL, using
// the browser's History API (no full page reload on navigation, unlike
// clicking a plain <a> tag). Nested routes (root > ProtectedRoute > Layout
// > page) let a parent route render shared UI once, with children rendered
// into its <Outlet />.
//
// AuthProvider must wrap the ENTIRE route tree (including ProtectedRoute,
// which calls useAuth() to decide whether to redirect to /login), so it's
// the element on our top-level route, with <Outlet /> rendering whichever
// child route actually matched the URL.
function RootLayout() {
  return (
    <AuthProvider>
      <Outlet />
    </AuthProvider>
  )
}

export const router = createBrowserRouter([
  {
    element: <RootLayout />,
    children: [
      { path: '/', element: <Navigate to="/dashboard" replace /> },
      { path: '/login', element: <LoginPage /> },
      { path: '/register', element: <RegisterPage /> },
      {
        element: <ProtectedRoute />,
        children: [
          {
            element: <Layout />,
            children: [
              { path: '/dashboard', element: <DashboardPage /> },
              { path: '/expenses', element: <ExpensesListPage /> },
              { path: '/expenses/new', element: <ExpenseFormPage /> },
              { path: '/expenses/:id/edit', element: <ExpenseFormPage /> },
              { path: '/categories', element: <CategoriesPage /> },
              { path: '/budgets', element: <BudgetsPage /> },
              { path: '/profile', element: <ProfilePage /> },
            ],
          },
        ],
      },
      { path: '*', element: <Navigate to="/dashboard" replace /> },
    ],
  },
])

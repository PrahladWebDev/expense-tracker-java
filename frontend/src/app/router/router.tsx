import { createBrowserRouter, Navigate, Outlet } from 'react-router-dom'
import { AuthProvider, useAuth } from '@/app/providers/AuthProvider'
import { ThemeProvider } from '@/app/providers/ThemeProvider'
import ProtectedRoute from '@/components/ProtectedRoute'
import Layout from '@/components/Layout'
import LandingPage from '@/features/marketing/pages/LandingPage'
import LoginPage from '@/features/auth/pages/LoginPage'
import RegisterPage from '@/features/auth/pages/RegisterPage'
import ProfilePage from '@/features/auth/pages/ProfilePage'
import DashboardPage from '@/features/dashboard/pages/DashboardPage'
import ExpensesListPage from '@/features/expenses/pages/ExpensesListPage'
import ExpenseFormPage from '@/features/expenses/pages/ExpenseFormPage'
import CategoriesPage from '@/features/categories/pages/CategoriesPage'
import BudgetsPage from '@/features/budgets/pages/BudgetsPage'
import GroupsListPage from '@/features/groups/pages/GroupsListPage'
import GroupDetailPage from '@/features/groups/pages/GroupDetailPage'
import JoinGroupPage from '@/features/groups/pages/JoinGroupPage'

// CONCEPT: React Router (data router API)
// createBrowserRouter builds a route tree matched against the URL, using
// the browser's History API (no full page reload on navigation, unlike
// clicking a plain <a> tag). Nested routes (root > ProtectedRoute > Layout
// > page) let a parent route render shared UI once, with children rendered
// into its <Outlet />.
//
// AuthProvider must wrap the ENTIRE route tree (including ProtectedRoute,
// which calls useAuth() to decide whether to redirect to the landing page),
// so it's the element on our top-level route, with <Outlet /> rendering
// whichever child route actually matched the URL. ThemeProvider sits alongside it
// for the same reason: both the logged-out LandingPage and the logged-in
// Layout need the dark-mode toggle.
function RootLayout() {
  return (
    <ThemeProvider>
      <AuthProvider>
        <Outlet />
      </AuthProvider>
    </ThemeProvider>
  )
}

// "/" shows the marketing landing page to signed-out visitors, and sends
// signed-in users straight to their dashboard instead of making them look
// at a sign-up pitch for a product they already use.
function RootRoute() {
  const { isAuthenticated } = useAuth()
  return isAuthenticated ? <Navigate to="/dashboard" replace /> : <LandingPage />
}

export const router = createBrowserRouter([
  {
    element: <RootLayout />,
    children: [
      { path: '/', element: <RootRoute /> },
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
              { path: '/groups', element: <GroupsListPage /> },
              { path: '/groups/:id', element: <GroupDetailPage /> },
              { path: '/groups/join/:code', element: <JoinGroupPage /> },
              { path: '/profile', element: <ProfilePage /> },
            ],
          },
        ],
      },
      { path: '*', element: <Navigate to="/" replace /> },
    ],
  },
])
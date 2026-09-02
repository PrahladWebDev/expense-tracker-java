import { useState } from 'react'
import { NavLink, Outlet, Link, useLocation } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { useAuth } from '@/app/providers/AuthProvider'
import { useTheme, ACCENT_OPTIONS } from '@/app/providers/ThemeProvider'

function useNavItems() {
  const { t } = useTranslation()
  return [
    { to: '/dashboard', label: t('nav.dashboard'), icon: '🏠' },
    { to: '/expenses', label: t('nav.expenses'), icon: '🧾' },
    { to: '/categories', label: t('nav.categories'), icon: '🏷️' },
    { to: '/budgets', label: t('nav.budgets'), icon: '🎯' },
    { to: '/groups', label: t('nav.groups'), icon: '👥' },
  ]
}

// CONCEPT: Responsive layout, one component
// Rather than separate "mobile" and "desktop" components, we render the
// same nav items three ways and let Tailwind's responsive prefixes
// (hidden / md:flex / md:hidden) decide which markup is visible at which
// breakpoint. This keeps the active-link logic (NavLink) in one place.
export default function Layout() {
  const { user, logout } = useAuth()
  const { theme, toggleTheme, accent, setAccent } = useTheme()
  const { t, i18n } = useTranslation()
  const navItems = useNavItems()
  const [menuOpen, setMenuOpen] = useState(false)
  const location = useLocation()
  // The groups pages have their own "Add expense" / "Add member" flows in
  // context, and this generic FAB always links to the personal expense
  // form - showing it on top of a group screen is confusing, not useful.
  const isGroupsSection = location.pathname.startsWith('/groups')

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-950 text-gray-900 dark:text-gray-100">
      <header className="bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-800 sticky top-0 z-20">
        <div className="max-w-6xl mx-auto px-4 flex items-center justify-between h-14">
          <div className="flex items-center gap-8">
            <Link to="/dashboard" className="font-semibold text-gray-900 dark:text-white flex items-center gap-1.5">
              <span aria-hidden>💰</span>
              <span className="hidden sm:inline">{t('nav.appName')}</span>
            </Link>
            {/* Desktop nav */}
            <nav className="hidden md:flex gap-1">
              {navItems.map((item) => (
                <NavLink
                  key={item.to}
                  to={item.to}
                  className={({ isActive }) =>
                    `px-3 py-1.5 rounded-md text-sm font-medium transition ${
                      isActive
                        ? 'bg-brand-50 dark:bg-brand-700/30 text-brand-700 dark:text-brand-100'
                        : 'text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800'
                    }`
                  }
                >
                  {item.label}
                </NavLink>
              ))}
            </nav>
          </div>

          <div className="flex items-center gap-1.5 sm:gap-3">
            <select
              value={i18n.language}
              onChange={(e) => i18n.changeLanguage(e.target.value)}
              aria-label={t('common.language')}
              className="text-xs rounded-md border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-900 text-gray-600 dark:text-gray-300 px-2 py-1.5 focus:outline-none focus:ring-2 focus:ring-brand-500"
            >
              <option value="en">EN</option>
              <option value="hi">हिंदी</option>
            </select>
            <select
              value={accent}
              onChange={(e) => setAccent(e.target.value as typeof accent)}
              aria-label="Color theme"
              className="text-xs rounded-md border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-900 text-gray-600 dark:text-gray-300 px-2 py-1.5 focus:outline-none focus:ring-2 focus:ring-brand-500"
            >
              {ACCENT_OPTIONS.map((opt) => (
                <option key={opt.value} value={opt.value}>{opt.label}</option>
              ))}
            </select>
            <button
              onClick={toggleTheme}
              aria-label="Toggle dark mode"
              className="w-9 h-9 grid place-items-center rounded-full text-gray-500 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 transition"
            >
              {theme === 'dark' ? '☀️' : '🌙'}
            </button>
            <NavLink
              to="/profile"
              className="hidden sm:block text-sm text-gray-500 dark:text-gray-400 hover:text-gray-800 dark:hover:text-gray-100"
            >
              {user?.fullName}
            </NavLink>
            <button
              onClick={() => logout()}
              className="hidden sm:block text-sm text-gray-500 dark:text-gray-400 hover:text-gray-800 dark:hover:text-gray-100 font-medium"
            >
              {t('nav.logout')}
            </button>
            {/* Mobile hamburger */}
            <button
              onClick={() => setMenuOpen((o) => !o)}
              aria-label="Open menu"
              aria-expanded={menuOpen}
              className="md:hidden w-9 h-9 grid place-items-center rounded-md text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800"
            >
              {menuOpen ? '✕' : '☰'}
            </button>
          </div>
        </div>

        {/* Mobile dropdown menu */}
        {menuOpen && (
          <nav className="md:hidden border-t border-gray-200 dark:border-gray-800 px-4 py-3 space-y-1 bg-white dark:bg-gray-900">
            {navItems.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                onClick={() => setMenuOpen(false)}
                className={({ isActive }) =>
                  `flex items-center gap-2 px-3 py-2 rounded-md text-sm font-medium transition ${
                    isActive
                      ? 'bg-brand-50 dark:bg-brand-700/30 text-brand-700 dark:text-brand-100'
                      : 'text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800'
                  }`
                }
              >
                <span aria-hidden>{item.icon}</span> {item.label}
              </NavLink>
            ))}
            <div className="border-t border-gray-200 dark:border-gray-800 my-2" />
            <NavLink
              to="/profile"
              onClick={() => setMenuOpen(false)}
              className="block px-3 py-2 rounded-md text-sm text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800"
            >
              {user?.fullName || t('nav.profile')}
            </NavLink>
            <button
              onClick={() => {
                setMenuOpen(false)
                logout()
              }}
              className="w-full text-left px-3 py-2 rounded-md text-sm text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800"
            >
              {t('nav.logout')}
            </button>
          </nav>
        )}
      </header>

      <main className="max-w-6xl mx-auto px-4 py-6 sm:py-8 pb-24 md:pb-8">
        <Outlet />
      </main>

      {/* Mobile bottom tab bar */}
      <nav className="md:hidden fixed bottom-0 inset-x-0 z-20 bg-white dark:bg-gray-900 border-t border-gray-200 dark:border-gray-800 grid grid-cols-5">
        {navItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) =>
              `flex flex-col items-center justify-center gap-0.5 py-2 text-xs font-medium ${
                isActive ? 'text-brand-600 dark:text-brand-100' : 'text-gray-500 dark:text-gray-400'
              }`
            }
          >
            <span aria-hidden className="text-base">{item.icon}</span>
            {item.label}
          </NavLink>
        ))}
      </nav>

      {/* Mobile quick-add floating action button */}
      {!isGroupsSection && (
        <Link
          to="/expenses/new"
          aria-label="Add expense"
          className="md:hidden fixed right-4 bottom-20 z-20 w-14 h-14 rounded-full bg-brand-600 hover:bg-brand-700 text-white text-2xl font-light grid place-items-center shadow-lg"
        >
          +
        </Link>
      )}
    </div>
  )
}
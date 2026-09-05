import { Link } from 'react-router-dom'
import { useTheme } from '@/app/providers/ThemeProvider'

const features = [
  {
    icon: '📊',
    title: 'Visual dashboards',
    body: 'See monthly trends and category breakdowns at a glance with interactive charts.',
  },
  {
    icon: '🏷️',
    title: 'Custom categories',
    body: 'Organize spending your way with color-coded categories you control.',
  },
  {
    icon: '🎯',
    title: 'Budgets that warn you',
    body: 'Set monthly budgets per category and know before you overspend.',
  },
  {
    icon: '🔎',
    title: 'Powerful filters',
    body: 'Search, filter by date range or category, and sort in a click.',
  },
  {
    icon: '🧾',
    title: 'Scan a receipt',
    body: 'Snap a photo and let OCR pre-fill the amount, category, and description for you.',
  },
  {
    icon: '👥',
    title: 'Split with a group',
    body: 'Create a group, invite friends with a link, and split expenses equally, by exact amount, or by percentage.',
  },
  {
    icon: '🤝',
    title: 'Settle up in one tap',
    body: 'We work out the minimum set of payments to clear every balance, then track who has actually paid.',
  },
  {
    icon: '💬',
    title: 'Comments & activity feed',
    body: 'Discuss a group expense and see a running log of everything that happens in the group.',
  },
  {
    icon: '📄',
    title: 'PDF & CSV reports',
    body: 'Download a full group report or a single member statement as a PDF, or export to CSV.',
  },
  {
    icon: '🌐',
    title: 'Multi-language',
    body: 'Use the app in English or Hindi, with a light/dark theme to match your taste.',
  },
  {
    icon: '📤',
    title: 'Export anytime',
    body: 'Download your personal expenses as CSV whenever you need them elsewhere.',
  },
  {
    icon: '🔒',
    title: 'Your data, secured',
    body: 'Token-based auth keeps your account and numbers private — group data is visible only to group members.',
  },
]

const steps = [
  { n: '1', title: 'Create your account', body: 'Sign up in seconds — no credit card required.' },
  { n: '2', title: 'Log your expenses', body: 'Add expenses as they happen, or import a backlog.' },
  { n: '3', title: 'Track & improve', body: 'Watch your dashboard and stay inside your budgets.' },
]

export default function LandingPage() {
  const { theme, toggleTheme } = useTheme()

  return (
    <div className="min-h-screen bg-white dark:bg-gray-950 text-gray-900 dark:text-gray-100">
      {/* Nav */}
      <header className="sticky top-0 z-20 bg-white/80 dark:bg-gray-950/80 backdrop-blur border-b border-gray-200 dark:border-gray-800">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 h-16 flex items-center justify-between">
          <span className="font-semibold text-lg flex items-center gap-2">
            <span aria-hidden>💰</span> Expense Tracker
          </span>
          <div className="flex items-center gap-2 sm:gap-3">
            <button
              onClick={toggleTheme}
              aria-label="Toggle dark mode"
              className="w-9 h-9 grid place-items-center rounded-full border border-gray-300 dark:border-gray-700 text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 transition"
            >
              {theme === 'dark' ? '☀️' : '🌙'}
            </button>
            <Link
              to="/login"
              className="hidden sm:inline-block text-sm font-medium text-gray-600 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white px-3 py-2"
            >
              Log in
            </Link>
            <Link
              to="/register"
              className="text-sm font-medium bg-brand-600 hover:bg-brand-700 text-white rounded-md px-3 sm:px-4 py-2 transition"
            >
              Get started
            </Link>
          </div>
        </div>
      </header>

      {/* Hero */}
      <section className="max-w-6xl mx-auto px-4 sm:px-6 pt-14 pb-16 sm:pt-20 sm:pb-24 text-center">
        <span className="inline-block text-xs font-semibold tracking-wide uppercase text-brand-700 dark:text-brand-100 bg-brand-50 dark:bg-brand-700/30 rounded-full px-3 py-1 mb-5">
          Free to use
        </span>
        <h1 className="text-3xl sm:text-5xl font-bold tracking-tight leading-tight max-w-3xl mx-auto">
          Know exactly where your money goes, every month
        </h1>
        <p className="mt-4 sm:mt-5 text-base sm:text-lg text-gray-500 dark:text-gray-400 max-w-xl mx-auto">
          Log expenses in seconds, set budgets per category, and watch clear dashboards do the
          math for you — or split a trip or bill with friends and settle up with one tap.
        </p>
        <div className="mt-8 flex flex-col sm:flex-row items-center justify-center gap-3">
          <Link
            to="/register"
            className="w-full sm:w-auto text-center bg-brand-600 hover:bg-brand-700 text-white font-medium rounded-md px-6 py-3 transition"
          >
            Create free account
          </Link>
          <Link
            to="/login"
            className="w-full sm:w-auto text-center border border-gray-300 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-800 font-medium rounded-md px-6 py-3 transition"
          >
            I already have an account
          </Link>
        </div>

        {/* Dashboard preview mock */}
        <div className="mt-14 sm:mt-16 mx-auto max-w-4xl rounded-xl border border-gray-200 dark:border-gray-800 bg-gray-50 dark:bg-gray-900 shadow-sm p-3 sm:p-6">
          <div className="grid grid-cols-2 sm:grid-cols-3 gap-3 sm:gap-4 text-left">
            {[
              { label: 'This month', value: '$1,284' },
              { label: 'Last month', value: '$1,510' },
              { label: 'Budgets on track', value: '4 / 5' },
            ].map((stat) => (
              <div
                key={stat.label}
                className="bg-white dark:bg-gray-950 border border-gray-200 dark:border-gray-800 rounded-lg p-3 sm:p-4"
              >
                <p className="text-xs text-gray-500 dark:text-gray-400">{stat.label}</p>
                <p className="text-lg sm:text-xl font-semibold mt-1">{stat.value}</p>
              </div>
            ))}
          </div>
          <div className="mt-3 sm:mt-4 h-28 sm:h-36 rounded-lg bg-gradient-to-t from-brand-100 dark:from-brand-700/30 to-transparent border border-gray-200 dark:border-gray-800 flex items-end gap-1.5 sm:gap-2 p-3 sm:p-4">
            {[40, 65, 50, 80, 55, 90, 70].map((h, i) => (
              <div
                key={i}
                className="flex-1 rounded-t bg-brand-500/70 dark:bg-brand-500/60"
                style={{ height: `${h}%` }}
              />
            ))}
          </div>
        </div>
      </section>

      {/* Features */}
      <section className="max-w-6xl mx-auto px-4 sm:px-6 py-14 sm:py-20">
        <div className="text-center max-w-xl mx-auto mb-10 sm:mb-14">
          <h2 className="text-2xl sm:text-3xl font-bold">Everything you need, nothing you don't</h2>
          <p className="mt-3 text-gray-500 dark:text-gray-400">
            A focused set of tools built for staying on top of everyday spending.
          </p>
        </div>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 sm:gap-6">
          {features.map((f) => (
            <div
              key={f.title}
              className="rounded-xl border border-gray-200 dark:border-gray-800 p-5 sm:p-6 hover:shadow-sm transition bg-white dark:bg-gray-900"
            >
              <span className="text-2xl" aria-hidden>{f.icon}</span>
              <h3 className="font-semibold mt-3">{f.title}</h3>
              <p className="text-sm text-gray-500 dark:text-gray-400 mt-1.5">{f.body}</p>
            </div>
          ))}
        </div>
      </section>

      {/* How it works */}
      <section className="bg-gray-50 dark:bg-gray-900/60 border-y border-gray-200 dark:border-gray-800">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 py-14 sm:py-20">
          <div className="text-center max-w-xl mx-auto mb-10 sm:mb-14">
            <h2 className="text-2xl sm:text-3xl font-bold">Up and running in three steps</h2>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-8 sm:gap-6">
            {steps.map((s) => (
              <div key={s.n} className="text-center sm:text-left">
                <div className="w-10 h-10 rounded-full bg-brand-600 text-white font-semibold grid place-items-center mx-auto sm:mx-0">
                  {s.n}
                </div>
                <h3 className="font-semibold mt-4">{s.title}</h3>
                <p className="text-sm text-gray-500 dark:text-gray-400 mt-1.5">{s.body}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="max-w-4xl mx-auto px-4 sm:px-6 py-16 sm:py-24 text-center">
        <h2 className="text-2xl sm:text-3xl font-bold">Start tracking your spending today</h2>
        <p className="mt-3 text-gray-500 dark:text-gray-400">It takes less than a minute to sign up.</p>
        <Link
          to="/register"
          className="inline-block mt-7 bg-brand-600 hover:bg-brand-700 text-white font-medium rounded-md px-7 py-3 transition"
        >
          Create free account
        </Link>
      </section>

      <footer className="border-t border-gray-200 dark:border-gray-800">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 py-8 flex flex-col sm:flex-row items-center justify-between gap-3 text-sm text-gray-500 dark:text-gray-400">
          <span>© {new Date().getFullYear()} Expense Tracker</span>
          <div className="flex gap-4">
            <Link to="/login" className="hover:text-gray-900 dark:hover:text-white">Log in</Link>
            <Link to="/register" className="hover:text-gray-900 dark:hover:text-white">Sign up</Link>
          </div>
        </div>
      </footer>
    </div>
  )
}

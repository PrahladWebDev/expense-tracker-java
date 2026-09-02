import { createContext, useContext, useEffect, useState, type ReactNode } from 'react'

// CONCEPT: two independent theme axes
// 1. `mode` ('light' | 'dark') - the existing background/text toggle, via
//    Tailwind's darkMode:'class' strategy (the `dark` class on <html>).
// 2. `accent` - which brand color palette is active (default/nord/dracula/
//    synthwave/forest), via a `data-accent` attribute on <html>. index.css
//    defines the CSS variables each value resolves to; tailwind.config.js
//    makes brand-50..700 read those variables. Because brand-* is already
//    used for buttons/links/active-nav/badges throughout the app, changing
//    just this attribute re-themes the whole app's accent color instantly.
// The two axes are independent: any accent can be viewed in light or dark
// background mode.
export type Mode = 'light' | 'dark'
export type Accent = 'default' | 'nord' | 'dracula' | 'synthwave' | 'forest'

export const ACCENT_OPTIONS: { value: Accent; label: string }[] = [
  { value: 'default', label: 'Default' },
  { value: 'nord', label: 'Nord' },
  { value: 'dracula', label: 'Dracula' },
  { value: 'synthwave', label: 'Synthwave' },
  { value: 'forest', label: 'Forest' },
]

interface ThemeContextValue {
  theme: Mode
  toggleTheme: () => void
  accent: Accent
  setAccent: (accent: Accent) => void
}

const ThemeContext = createContext<ThemeContextValue | undefined>(undefined)
const MODE_STORAGE_KEY = 'expense_tracker_theme'
const ACCENT_STORAGE_KEY = 'expense_tracker_accent'

function getInitialMode(): Mode {
  const stored = localStorage.getItem(MODE_STORAGE_KEY)
  if (stored === 'light' || stored === 'dark') return stored
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
}

function getInitialAccent(): Accent {
  const stored = localStorage.getItem(ACCENT_STORAGE_KEY)
  if (stored === 'default' || stored === 'nord' || stored === 'dracula' || stored === 'synthwave' || stored === 'forest') {
    return stored
  }
  return 'default'
}

export function ThemeProvider({ children }: { children: ReactNode }) {
  const [theme, setTheme] = useState<Mode>(getInitialMode)
  const [accent, setAccent] = useState<Accent>(getInitialAccent)

  useEffect(() => {
    document.documentElement.classList.toggle('dark', theme === 'dark')
    localStorage.setItem(MODE_STORAGE_KEY, theme)
  }, [theme])

  useEffect(() => {
    document.documentElement.setAttribute('data-accent', accent)
    localStorage.setItem(ACCENT_STORAGE_KEY, accent)
  }, [accent])

  function toggleTheme() {
    setTheme((t) => (t === 'dark' ? 'light' : 'dark'))
  }

  return (
    <ThemeContext.Provider value={{ theme, toggleTheme, accent, setAccent }}>
      {children}
    </ThemeContext.Provider>
  )
}

export function useTheme() {
  const context = useContext(ThemeContext)
  if (!context) throw new Error('useTheme must be used within a ThemeProvider')
  return context
}

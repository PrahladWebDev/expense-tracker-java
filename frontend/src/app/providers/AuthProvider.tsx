import { createContext, useContext, useState, type ReactNode } from 'react'
import { tokenStorage } from '@/lib/axios'
import { authApi } from '@/features/auth/api/authApi'
import type { LoginPayload, RegisterPayload, UserSummary } from '@/features/auth/types/auth.types'

// CONCEPT: React Context
// Context lets us share state (the logged-in user, login/logout functions)
// across the entire component tree WITHOUT manually passing props down
// through every intermediate component ("prop drilling"). Any component
// wrapped by <AuthProvider> can call useAuth() to read/update auth state.
interface AuthContextValue {
  user: UserSummary | null
  isAuthenticated: boolean
  isLoading: boolean
  login: (payload: LoginPayload) => Promise<void>
  register: (payload: RegisterPayload) => Promise<void>
  logout: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

function loadStoredUser(): UserSummary | null {
  const raw = localStorage.getItem('expense_tracker_user')
  return raw ? JSON.parse(raw) : null
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserSummary | null>(loadStoredUser())
  const [isLoading, setIsLoading] = useState(false)

  function persistSession(accessToken: string, refreshToken: string, user: UserSummary) {
    tokenStorage.setTokens(accessToken, refreshToken)
    localStorage.setItem('expense_tracker_user', JSON.stringify(user))
    setUser(user)
  }

  async function login(payload: LoginPayload) {
    setIsLoading(true)
    try {
      const response = await authApi.login(payload)
      persistSession(response.accessToken, response.refreshToken, response.user)
    } finally {
      setIsLoading(false)
    }
  }

  async function register(payload: RegisterPayload) {
    setIsLoading(true)
    try {
      const response = await authApi.register(payload)
      persistSession(response.accessToken, response.refreshToken, response.user)
    } finally {
      setIsLoading(false)
    }
  }

  async function logout() {
    const refreshToken = tokenStorage.getRefreshToken()
    try {
      if (refreshToken) await authApi.logout(refreshToken)
    } finally {
      tokenStorage.clear()
      localStorage.removeItem('expense_tracker_user')
      setUser(null)
    }
  }

  return (
    <AuthContext.Provider value={{ user, isAuthenticated: !!user, isLoading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

// CONCEPT: Custom hook
// A function starting with "use" that can call other hooks. This wraps
// useContext(AuthContext) with a runtime check, giving components a simple
// `const { user, login } = useAuth()` API and a clear error if someone
// forgets to wrap their tree in <AuthProvider>.
export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth must be used within an AuthProvider')
  return context
}

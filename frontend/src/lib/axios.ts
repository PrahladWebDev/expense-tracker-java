import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios'

// CONCEPT: Axios
// Axios is an HTTP client - a wrapper around the browser's fetch API that
// gives us request/response interceptors, automatic JSON parsing, and a
// nicer error-handling model (rejects the promise on non-2xx by default).
//
// CONCEPT: Centralized API client
// Instead of every component calling axios.get(...) with a hardcoded URL
// and manually attaching headers, we create ONE configured instance here
// and import it everywhere. Benefits:
//   1. Base URL and auth logic live in exactly one place.
//   2. Interceptors (below) apply automatically to every request/response,
//      so components never think about tokens directly.
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1'

export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
})

const ACCESS_TOKEN_KEY = 'expense_tracker_access_token'
const REFRESH_TOKEN_KEY = 'expense_tracker_refresh_token'

export const tokenStorage = {
  getAccessToken: () => localStorage.getItem(ACCESS_TOKEN_KEY),
  getRefreshToken: () => localStorage.getItem(REFRESH_TOKEN_KEY),
  setTokens: (accessToken: string, refreshToken: string) => {
    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken)
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
  },
  clear: () => {
    localStorage.removeItem(ACCESS_TOKEN_KEY)
    localStorage.removeItem(REFRESH_TOKEN_KEY)
  },
}

// CONCEPT: Request interceptor
// Runs BEFORE every outgoing request. We attach the current access token
// as a Bearer header here so no individual API call needs to remember to.
api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = tokenStorage.getAccessToken()
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// CONCEPT: Response interceptor + automatic token refresh
// Runs AFTER every response. If a request fails with 401 (access token
// expired) and we haven't already retried it, we:
//   1. Call POST /auth/refresh with the stored refresh token
//   2. Save the new access/refresh token pair
//   3. Retry the ORIGINAL failed request with the new access token
// This makes token expiry invisible to the rest of the app - components
// never see a 401 for an expired-but-refreshable session.
let isRefreshing = false
let pendingQueue: Array<(token: string) => void> = []

function onRefreshed(newToken: string) {
  pendingQueue.forEach((callback) => callback(newToken))
  pendingQueue = []
}

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean }

    if (error.response?.status !== 401 || originalRequest._retry || originalRequest.url?.includes('/auth/')) {
      return Promise.reject(error)
    }

    if (isRefreshing) {
      // Another request already triggered a refresh; wait for it instead of
      // firing a second concurrent refresh call.
      return new Promise((resolve) => {
        pendingQueue.push((token: string) => {
          originalRequest.headers.Authorization = `Bearer ${token}`
          resolve(api(originalRequest))
        })
      })
    }

    originalRequest._retry = true
    isRefreshing = true

    const refreshToken = tokenStorage.getRefreshToken()
    if (!refreshToken) {
      tokenStorage.clear()
      window.location.href = '/login'
      return Promise.reject(error)
    }

    try {
      const { data } = await axios.post(`${API_BASE_URL}/auth/refresh`, { refreshToken })
      const { accessToken, refreshToken: newRefreshToken } = data.data
      tokenStorage.setTokens(accessToken, newRefreshToken)
      onRefreshed(accessToken)
      originalRequest.headers.Authorization = `Bearer ${accessToken}`
      return api(originalRequest)
    } catch (refreshError) {
      tokenStorage.clear()
      window.location.href = '/login'
      return Promise.reject(refreshError)
    } finally {
      isRefreshing = false
    }
  }
)

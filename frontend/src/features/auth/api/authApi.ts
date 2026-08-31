import { api } from '@/lib/axios'
import type { AuthResponse, LoginPayload, RegisterPayload } from '../types/auth.types'

interface ApiEnvelope<T> {
  success: boolean
  message: string
  data: T
}

export const authApi = {
  login: async (payload: LoginPayload) => {
    const { data } = await api.post<ApiEnvelope<AuthResponse>>('/auth/login', payload)
    return data.data
  },
  register: async (payload: RegisterPayload) => {
    const { data } = await api.post<ApiEnvelope<AuthResponse>>('/auth/register', payload)
    return data.data
  },
  logout: async (refreshToken: string) => {
    await api.post('/auth/logout', { refreshToken })
  },
}

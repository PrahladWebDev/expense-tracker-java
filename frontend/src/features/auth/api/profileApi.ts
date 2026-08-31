import { api } from '@/lib/axios'
import type { Profile, UpdateProfilePayload } from '../types/profile.types'

interface ApiEnvelope<T> {
  success: boolean
  message: string
  data: T
}

export const profileApi = {
  getMe: async () => {
    const { data } = await api.get<ApiEnvelope<Profile>>('/users/me')
    return data.data
  },
  updateMe: async (payload: UpdateProfilePayload) => {
    const { data } = await api.put<ApiEnvelope<Profile>>('/users/me', payload)
    return data.data
  },
}

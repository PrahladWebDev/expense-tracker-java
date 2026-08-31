import { api } from '@/lib/axios'
import type { Category, CategoryPayload } from '../types/category.types'

interface ApiEnvelope<T> {
  success: boolean
  message: string
  data: T
}

export const categoryApi = {
  getAll: async () => {
    const { data } = await api.get<ApiEnvelope<Category[]>>('/categories')
    return data.data
  },
  create: async (payload: CategoryPayload) => {
    const { data } = await api.post<ApiEnvelope<Category>>('/categories', payload)
    return data.data
  },
  update: async (id: number, payload: CategoryPayload) => {
    const { data } = await api.put<ApiEnvelope<Category>>(`/categories/${id}`, payload)
    return data.data
  },
  remove: async (id: number) => {
    await api.delete(`/categories/${id}`)
  },
}

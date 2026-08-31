import { api } from '@/lib/axios'
import type { Budget, BudgetPayload } from '../types/budget.types'

interface ApiEnvelope<T> {
  success: boolean
  message: string
  data: T
}

export const budgetApi = {
  getAll: async () => {
    const { data } = await api.get<ApiEnvelope<Budget[]>>('/budgets')
    return data.data
  },
  create: async (payload: BudgetPayload) => {
    const { data } = await api.post<ApiEnvelope<Budget>>('/budgets', payload)
    return data.data
  },
  update: async (id: number, payload: BudgetPayload) => {
    const { data } = await api.put<ApiEnvelope<Budget>>(`/budgets/${id}`, payload)
    return data.data
  },
  remove: async (id: number) => {
    await api.delete(`/budgets/${id}`)
  },
}

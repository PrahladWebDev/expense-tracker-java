import { api } from '@/lib/axios'
import type { Expense, ExpenseFilters, ExpensePayload, PageResponse } from '../types/expense.types'

interface ApiEnvelope<T> {
  success: boolean
  message: string
  data: T
}

export const expenseApi = {
  search: async (filters: ExpenseFilters) => {
    const { data } = await api.get<ApiEnvelope<PageResponse<Expense>>>('/expenses', { params: filters })
    return data.data
  },
  getById: async (id: number) => {
    const { data } = await api.get<ApiEnvelope<Expense>>(`/expenses/${id}`)
    return data.data
  },
  create: async (payload: ExpensePayload) => {
    const { data } = await api.post<ApiEnvelope<Expense>>('/expenses', payload)
    return data.data
  },
  update: async (id: number, payload: ExpensePayload) => {
    const { data } = await api.put<ApiEnvelope<Expense>>(`/expenses/${id}`, payload)
    return data.data
  },
  remove: async (id: number) => {
    await api.delete(`/expenses/${id}`)
  },
}

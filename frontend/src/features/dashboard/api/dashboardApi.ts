import { api } from '@/lib/axios'
import type { CategorySpending, MonthlySpending, Summary } from '../types/dashboard.types'

interface ApiEnvelope<T> {
  success: boolean
  message: string
  data: T
}

export const dashboardApi = {
  getSummary: async () => {
    const { data } = await api.get<ApiEnvelope<Summary>>('/dashboard/summary')
    return data.data
  },
  getMonthly: async (monthsBack = 6) => {
    const { data } = await api.get<ApiEnvelope<MonthlySpending[]>>('/dashboard/monthly', { params: { monthsBack } })
    return data.data
  },
  getCategories: async () => {
    const { data } = await api.get<ApiEnvelope<CategorySpending[]>>('/dashboard/categories')
    return data.data
  },
}

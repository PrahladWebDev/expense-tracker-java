import { useQuery } from '@tanstack/react-query'
import { dashboardApi } from '../api/dashboardApi'

export function useSummary() {
  return useQuery({ queryKey: ['dashboard', 'summary'], queryFn: dashboardApi.getSummary })
}

export function useMonthlySpending(monthsBack = 6) {
  return useQuery({ queryKey: ['dashboard', 'monthly', monthsBack], queryFn: () => dashboardApi.getMonthly(monthsBack) })
}

export function useCategoryBreakdown() {
  return useQuery({ queryKey: ['dashboard', 'categories'], queryFn: dashboardApi.getCategories })
}

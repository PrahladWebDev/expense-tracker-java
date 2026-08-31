import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { budgetApi } from '../api/budgetApi'
import type { BudgetPayload } from '../types/budget.types'

export function useBudgets() {
  return useQuery({ queryKey: ['budgets'], queryFn: budgetApi.getAll })
}

export function useCreateBudget() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: BudgetPayload) => budgetApi.create(payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['budgets'] }),
  })
}

export function useDeleteBudget() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => budgetApi.remove(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['budgets'] }),
  })
}

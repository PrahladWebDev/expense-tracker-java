import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { expenseApi } from '../api/expenseApi'
import type { ExpenseFilters, ExpensePayload } from '../types/expense.types'

// CONCEPT: useQuery (reading server state)
// The `queryKey` array uniquely identifies this cached request. When
// `filters` changes (a different page, a new search term...), the key
// changes too, so TanStack Query automatically fires a new request and
// caches its result separately - no manual useEffect/dependency array needed.
export function useExpenses(filters: ExpenseFilters) {
  return useQuery({
    queryKey: ['expenses', filters],
    queryFn: () => expenseApi.search(filters),
    placeholderData: (previous) => previous, // keep showing old page while the new one loads (no flash of empty state)
  })
}

export function useExpense(id: number | undefined) {
  return useQuery({
    queryKey: ['expenses', id],
    queryFn: () => expenseApi.getById(id as number),
    enabled: !!id, // don't run the query at all until we have a real id (e.g. "new" vs "edit" forms)
  })
}

// CONCEPT: useMutation (writing server state) + cache invalidation
// A mutation performs a create/update/delete. `onSuccess` calls
// `invalidateQueries` to tell TanStack Query "the ['expenses'] cache is now
// stale" - it will automatically refetch any currently-mounted expense list
// so the UI reflects the change without a manual page reload.
//
// FIX: a budget's `spent`/`percentUsed` (see BudgetService.withUsage on the
// backend) is DERIVED from expenses in that month/category - it isn't its
// own independent piece of data. Adding, editing, or deleting an expense
// changes what every relevant budget should show, but we were only ever
// invalidating ['expenses']. The ['budgets'] cache (staleTime: 30s,
// refetchOnWindowFocus: false - see main.tsx) had no way to know it was
// stale, so the Budgets page / Dashboard budget widget could silently show
// an outdated percentUsed for up to 30s, or until something else happened
// to refetch it. Invalidating ['budgets'] alongside ['expenses'] here keeps
// them in sync immediately, and also means callers (like ExpenseFormPage)
// can safely fetch fresh budget data right after a mutation resolves.
export function useCreateExpense() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: ExpensePayload) => expenseApi.create(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['expenses'] })
      queryClient.invalidateQueries({ queryKey: ['budgets'] })
    },
  })
}

export function useUpdateExpense() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }: { id: number; payload: ExpensePayload }) => expenseApi.update(id, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['expenses'] })
      queryClient.invalidateQueries({ queryKey: ['budgets'] })
    },
  })
}

export function useDeleteExpense() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => expenseApi.remove(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['expenses'] })
      queryClient.invalidateQueries({ queryKey: ['budgets'] })
    },
  })
}
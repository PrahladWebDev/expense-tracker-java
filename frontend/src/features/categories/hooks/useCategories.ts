import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { categoryApi } from '../api/categoryApi'
import type { CategoryPayload } from '../types/category.types'

export function useCategories() {
  return useQuery({ queryKey: ['categories'], queryFn: categoryApi.getAll })
}

export function useCreateCategory() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: CategoryPayload) => categoryApi.create(payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['categories'] }),
  })
}

export function useUpdateCategory() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }: { id: number; payload: CategoryPayload }) => categoryApi.update(id, payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['categories'] }),
  })
}

export function useDeleteCategory() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => categoryApi.remove(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['categories'] }),
  })
}

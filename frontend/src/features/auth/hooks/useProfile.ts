import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { profileApi } from '../api/profileApi'
import type { UpdateProfilePayload } from '../types/profile.types'

export function useProfile() {
  return useQuery({ queryKey: ['profile'], queryFn: profileApi.getMe })
}

export function useUpdateProfile() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: UpdateProfilePayload) => profileApi.updateMe(payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['profile'] }),
  })
}

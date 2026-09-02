import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { groupApi } from '../api/groupApi'
import type {
  AddMemberPayload,
  GroupExpensePayload,
  GroupPayload,
  SettlementPayload,
} from '../types/group.types'

export function useGroups() {
  return useQuery({ queryKey: ['groups'], queryFn: groupApi.getAll })
}

export function useGroup(groupId: number) {
  return useQuery({ queryKey: ['groups', groupId], queryFn: () => groupApi.getOne(groupId), enabled: !!groupId })
}

export function useCreateGroup() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: GroupPayload) => groupApi.create(payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['groups'] }),
  })
}

export function useDeleteGroup() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (groupId: number) => groupApi.remove(groupId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['groups'] }),
  })
}

export function useCloseGroup(groupId: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: () => groupApi.close(groupId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['groups', groupId] })
      queryClient.invalidateQueries({ queryKey: ['groups'] })
    },
  })
}

export function useReopenGroup(groupId: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: () => groupApi.reopen(groupId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['groups', groupId] })
      queryClient.invalidateQueries({ queryKey: ['groups'] })
    },
  })
}

export function useAddMember(groupId: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: AddMemberPayload) => groupApi.addMember(groupId, payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['groups', groupId] }),
  })
}

export function useRemoveMember(groupId: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (memberUserId: number) => groupApi.removeMember(groupId, memberUserId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['groups', groupId] })
      queryClient.invalidateQueries({ queryKey: ['groups', groupId, 'balances'] })
    },
  })
}

export function useGroupBalances(groupId: number) {
  return useQuery({
    queryKey: ['groups', groupId, 'balances'],
    queryFn: () => groupApi.getBalances(groupId),
    enabled: !!groupId,
  })
}

export function useGroupExpenses(groupId: number) {
  return useQuery({
    queryKey: ['groups', groupId, 'expenses'],
    queryFn: () => groupApi.getExpenses(groupId),
    enabled: !!groupId,
  })
}

export function useAddGroupExpense(groupId: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: GroupExpensePayload) => groupApi.addExpense(groupId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['groups', groupId, 'expenses'] })
      queryClient.invalidateQueries({ queryKey: ['groups', groupId, 'balances'] })
      queryClient.invalidateQueries({ queryKey: ['groups', groupId, 'settlements', 'suggestions'] })
    },
  })
}

export function useRemoveGroupExpense(groupId: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (expenseId: number) => groupApi.removeExpense(groupId, expenseId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['groups', groupId, 'expenses'] })
      queryClient.invalidateQueries({ queryKey: ['groups', groupId, 'balances'] })
      queryClient.invalidateQueries({ queryKey: ['groups', groupId, 'settlements', 'suggestions'] })
    },
  })
}

export function useSettlements(groupId: number) {
  return useQuery({
    queryKey: ['groups', groupId, 'settlements'],
    queryFn: () => groupApi.getSettlements(groupId),
    enabled: !!groupId,
  })
}

export function useSettlementSuggestions(groupId: number) {
  return useQuery({
    queryKey: ['groups', groupId, 'settlements', 'suggestions'],
    queryFn: () => groupApi.getSuggestions(groupId),
    enabled: !!groupId,
  })
}

export function useRecordSettlement(groupId: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: SettlementPayload) => groupApi.recordSettlement(groupId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['groups', groupId, 'settlements'] })
      queryClient.invalidateQueries({ queryKey: ['groups', groupId, 'balances'] })
    },
  })
}

// --- Activity feed ---
export function useGroupActivity(groupId: number) {
  return useQuery({
    queryKey: ['groups', groupId, 'activity'],
    queryFn: () => groupApi.getActivity(groupId),
    enabled: !!groupId,
  })
}

// --- Invite link ---
export function useRegenerateInviteCode(groupId: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: () => groupApi.regenerateInviteCode(groupId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['groups', groupId] }),
  })
}

export function useJoinByInviteCode() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (inviteCode: string) => groupApi.joinByInviteCode(inviteCode),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['groups'] }),
  })
}

// --- Comments on an expense ---
export function useComments(groupId: number, expenseId: number | null) {
  return useQuery({
    queryKey: ['groups', groupId, 'expenses', expenseId, 'comments'],
    queryFn: () => groupApi.getComments(groupId, expenseId as number),
    enabled: !!groupId && !!expenseId,
  })
}

export function useAddComment(groupId: number, expenseId: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (text: string) => groupApi.addComment(groupId, expenseId, text),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['groups', groupId, 'expenses', expenseId, 'comments'] })
      queryClient.invalidateQueries({ queryKey: ['groups', groupId, 'activity'] })
    },
  })
}

export function useRemoveComment(groupId: number, expenseId: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (commentId: number) => groupApi.removeComment(groupId, expenseId, commentId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['groups', groupId, 'expenses', expenseId, 'comments'] }),
  })
}

// --- Receipt photo on an expense ---
export function useUploadReceipt(groupId: number, expenseId: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (file: File) => groupApi.uploadReceipt(groupId, expenseId, file),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['groups', groupId, 'expenses'] }),
  })
}

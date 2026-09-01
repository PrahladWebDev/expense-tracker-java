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

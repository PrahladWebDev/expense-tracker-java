import { api } from '@/lib/axios'
import type {
  Activity,
  AddMemberPayload,
  Comment,
  Group,
  GroupExpense,
  GroupExpensePayload,
  GroupPayload,
  MemberBalance,
  OcrResult,
  Settlement,
  SettlementPayload,
  SettlementSuggestion,
} from '../types/group.types'

interface ApiEnvelope<T> {
  success: boolean
  message: string
  data: T
}

export const groupApi = {
  getAll: async () => {
    const { data } = await api.get<ApiEnvelope<Group[]>>('/groups')
    return data.data
  },
  getOne: async (groupId: number) => {
    const { data } = await api.get<ApiEnvelope<Group>>(`/groups/${groupId}`)
    return data.data
  },
  create: async (payload: GroupPayload) => {
    const { data } = await api.post<ApiEnvelope<Group>>('/groups', payload)
    return data.data
  },
  remove: async (groupId: number) => {
    await api.delete(`/groups/${groupId}`)
  },
  close: async (groupId: number) => {
    const { data } = await api.post<ApiEnvelope<Group>>(`/groups/${groupId}/close`)
    return data.data
  },
  reopen: async (groupId: number) => {
    const { data } = await api.post<ApiEnvelope<Group>>(`/groups/${groupId}/reopen`)
    return data.data
  },
  addMember: async (groupId: number, payload: AddMemberPayload) => {
    const { data } = await api.post<ApiEnvelope<Group>>(`/groups/${groupId}/members`, payload)
    return data.data
  },
  removeMember: async (groupId: number, memberUserId: number) => {
    const { data } = await api.delete<ApiEnvelope<Group>>(`/groups/${groupId}/members/${memberUserId}`)
    return data.data
  },
  getBalances: async (groupId: number) => {
    const { data } = await api.get<ApiEnvelope<MemberBalance[]>>(`/groups/${groupId}/balances`)
    return data.data
  },

  getExpenses: async (groupId: number) => {
    const { data } = await api.get<ApiEnvelope<GroupExpense[]>>(`/groups/${groupId}/expenses`)
    return data.data
  },
  addExpense: async (groupId: number, payload: GroupExpensePayload) => {
    const { data } = await api.post<ApiEnvelope<GroupExpense>>(`/groups/${groupId}/expenses`, payload)
    return data.data
  },
  removeExpense: async (groupId: number, expenseId: number) => {
    await api.delete(`/groups/${groupId}/expenses/${expenseId}`)
  },

  getSettlements: async (groupId: number) => {
    const { data } = await api.get<ApiEnvelope<Settlement[]>>(`/groups/${groupId}/settlements`)
    return data.data
  },
  getSuggestions: async (groupId: number) => {
    const { data } = await api.get<ApiEnvelope<SettlementSuggestion[]>>(`/groups/${groupId}/settlements/suggestions`)
    return data.data
  },
  recordSettlement: async (groupId: number, payload: SettlementPayload) => {
    const { data } = await api.post<ApiEnvelope<Settlement>>(`/groups/${groupId}/settlements`, payload)
    return data.data
  },

  // Both endpoints return a raw PDF (application/pdf), so we ask axios for a
  // blob instead of trying to JSON-parse the response, then hand the caller
  // a ready-to-use object URL for download/preview.
  downloadGroupReportPdf: async (groupId: number) => {
    const response = await api.get(`/groups/${groupId}/report/pdf`, { responseType: 'blob' })
    return URL.createObjectURL(response.data as Blob)
  },
  downloadMemberStatementPdf: async (groupId: number, memberUserId: number) => {
    const response = await api.get(`/groups/${groupId}/members/${memberUserId}/statement/pdf`, {
      responseType: 'blob',
    })
    return URL.createObjectURL(response.data as Blob)
  },
  downloadGroupReportCsv: async (groupId: number) => {
    const response = await api.get(`/groups/${groupId}/report/csv`, { responseType: 'blob' })
    return URL.createObjectURL(response.data as Blob)
  },

  // Invite link
  regenerateInviteCode: async (groupId: number) => {
    const { data } = await api.post<ApiEnvelope<Group>>(`/groups/${groupId}/invite-code/regenerate`)
    return data.data
  },
  joinByInviteCode: async (inviteCode: string) => {
    const { data } = await api.post<ApiEnvelope<Group>>(`/groups/join/${inviteCode}`)
    return data.data
  },

  // Activity feed
  getActivity: async (groupId: number) => {
    const { data } = await api.get<ApiEnvelope<Activity[]>>(`/groups/${groupId}/activity`)
    return data.data
  },

  // Comments on an expense
  getComments: async (groupId: number, expenseId: number) => {
    const { data } = await api.get<ApiEnvelope<Comment[]>>(`/groups/${groupId}/expenses/${expenseId}/comments`)
    return data.data
  },
  addComment: async (groupId: number, expenseId: number, text: string) => {
    const { data } = await api.post<ApiEnvelope<Comment>>(`/groups/${groupId}/expenses/${expenseId}/comments`, { text })
    return data.data
  },
  removeComment: async (groupId: number, expenseId: number, commentId: number) => {
    await api.delete(`/groups/${groupId}/expenses/${expenseId}/comments/${commentId}`)
  },

  // Receipt photo on an expense
  uploadReceipt: async (groupId: number, expenseId: number, file: File) => {
    const form = new FormData()
    form.append('file', file)
    await api.post(`/groups/${groupId}/expenses/${expenseId}/receipt`, form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
  downloadReceipt: async (groupId: number, expenseId: number) => {
    const response = await api.get(`/groups/${groupId}/expenses/${expenseId}/receipt`, { responseType: 'blob' })
    return URL.createObjectURL(response.data as Blob)
  },

  // Receipt OCR (scan-to-prefill) - not scoped to a group, just returns suggestions
  scanReceipt: async (file: File) => {
    const form = new FormData()
    form.append('file', file)
    const { data } = await api.post<ApiEnvelope<OcrResult>>('/ocr/receipt', form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    return data.data
  },
}

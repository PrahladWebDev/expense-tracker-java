export interface GroupMember {
  userId: number
  fullName: string
  email: string
  role: 'OWNER' | 'MEMBER'
  joinedAt: string
}

export type GroupStatus = 'OPEN' | 'CLOSED'

export interface Group {
  id: number
  name: string
  description: string | null
  createdByUserId: number
  createdByName: string
  createdAt: string
  status: GroupStatus
  closedAt: string | null
  inviteCode: string
  members: GroupMember[]
}

export interface GroupPayload {
  name: string
  description?: string
}

export interface AddMemberPayload {
  email: string
}

export type SplitType = 'EQUAL' | 'EXACT' | 'PERCENTAGE'

export interface ExpenseShareInput {
  userId: number
  value?: number | null
}

export interface ExpenseShare {
  userId: number
  fullName: string
  shareAmount: number
}

export interface GroupExpense {
  id: number
  amount: number
  description: string | null
  expenseDate: string
  paidByUserId: number
  paidByName: string
  splitType: SplitType
  shares: ExpenseShare[]
  hasReceipt: boolean
  receiptOriginalName: string | null
  createdAt: string
  deleted: boolean
  deletedByName: string | null
  deletedAt: string | null
}

export interface GroupExpensePayload {
  amount: number
  description?: string
  expenseDate: string
  paidByUserId: number
  splitType: SplitType
  shares: ExpenseShareInput[]
}

export interface MemberBalance {
  userId: number
  fullName: string
  totalPaid: number
  totalShare: number
  netBalance: number
}

export interface SettlementSuggestion {
  fromUserId: number
  fromName: string
  toUserId: number
  toName: string
  amount: number
}

export interface Settlement {
  id: number
  fromUserId: number
  fromName: string
  toUserId: number
  toName: string
  amount: number
  note: string | null
  settledAt: string
}

export interface SettlementPayload {
  fromUserId: number
  toUserId: number
  amount: number
  note?: string
}

export interface Comment {
  id: number
  userId: number
  userName: string
  text: string
  createdAt: string
}

export interface Activity {
  id: number
  type: string
  message: string
  actorUserId: number | null
  actorName: string
  createdAt: string
}

export interface OcrResult {
  rawText: string
  suggestedAmount: number | null
  suggestedCategory: string | null
  suggestedDescription: string | null
}

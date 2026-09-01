export interface GroupMember {
  userId: number
  fullName: string
  email: string
  role: 'OWNER' | 'MEMBER'
  joinedAt: string
}

export interface Group {
  id: number
  name: string
  description: string | null
  createdByUserId: number
  createdByName: string
  createdAt: string
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
  createdAt: string
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

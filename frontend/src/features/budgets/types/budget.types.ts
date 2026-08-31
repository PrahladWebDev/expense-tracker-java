export interface Budget {
  id: number
  amount: number
  month: string
  categoryId: number | null
  categoryName: string
  spent: number
  remaining: number
  percentUsed: number
}

export interface BudgetPayload {
  amount: number
  month: string
  categoryId?: number
}

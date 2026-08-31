export interface Category {
  id: number
  name: string
  color: string
}

export interface Expense {
  id: number
  amount: number
  description: string | null
  expenseDate: string
  categoryId: number
  categoryName: string
  categoryColor: string
  createdAt: string
  updatedAt: string
}

export interface ExpensePayload {
  amount: number
  description?: string
  categoryId: number
  expenseDate: string
}

export interface PageResponse<T> {
  items: T[]
  page: number
  size: number
  totalItems: number
  totalPages: number
  last: boolean
}

export interface ExpenseFilters {
  search?: string
  categoryId?: number
  from?: string
  to?: string
  minAmount?: number
  maxAmount?: number
  page: number
  size: number
  sortBy: string
  direction: 'asc' | 'desc'
}

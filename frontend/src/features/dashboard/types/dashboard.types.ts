export interface Summary {
  totalAllTime: number
  currentMonth: number
  previousMonth: number
  changePercent: number
}

export interface MonthlySpending {
  month: string
  total: number
}

export interface CategorySpending {
  categoryId: number
  categoryName: string
  color: string
  total: number
}

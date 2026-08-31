import { useState } from 'react'
import { useCategories } from '@/features/categories/hooks/useCategories'
import { useBudgets, useCreateBudget, useDeleteBudget } from '../hooks/useBudgets'
import { formatCurrency } from '@/utils/format'

function currentMonth() {
  return new Date().toISOString().slice(0, 7)
}

export default function BudgetsPage() {
  const { data: budgets, isLoading } = useBudgets()
  const { data: categories } = useCategories()
  const createBudget = useCreateBudget()
  const deleteBudget = useDeleteBudget()

  const [amount, setAmount] = useState('')
  const [month, setMonth] = useState(currentMonth())
  const [categoryId, setCategoryId] = useState('')
  const [error, setError] = useState<string | null>(null)

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError(null)
    try {
      await createBudget.mutateAsync({
        amount: Number(amount),
        month,
        categoryId: categoryId ? Number(categoryId) : undefined,
      })
      setAmount('')
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Something went wrong')
    }
  }

  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
      <div className="md:col-span-1">
        <div className="bg-white border border-gray-200 rounded-xl p-5">
          <h2 className="font-semibold text-gray-900 mb-4">New budget</h2>
          {error && <p className="text-sm text-red-600 mb-3">{error}</p>}
          <form onSubmit={onSubmit} className="space-y-3">
            <div>
              <label className="block text-sm text-gray-700 mb-1">Amount</label>
              <input
                type="number"
                step="0.01"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                required
                className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
              />
            </div>
            <div>
              <label className="block text-sm text-gray-700 mb-1">Month</label>
              <input
                type="month"
                value={month}
                onChange={(e) => setMonth(e.target.value)}
                required
                className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
              />
            </div>
            <div>
              <label className="block text-sm text-gray-700 mb-1">Category (optional)</label>
              <select
                value={categoryId}
                onChange={(e) => setCategoryId(e.target.value)}
                className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
              >
                <option value="">Overall (all categories)</option>
                {categories?.map((c) => (
                  <option key={c.id} value={c.id}>{c.name}</option>
                ))}
              </select>
            </div>
            <button
              type="submit"
              className="w-full bg-brand-600 hover:bg-brand-700 text-white text-sm font-medium rounded-md py-2"
            >
              Add budget
            </button>
          </form>
        </div>
      </div>

      <div className="md:col-span-2 space-y-3">
        {isLoading ? (
          <p className="text-sm text-gray-500">Loading budgets…</p>
        ) : budgets && budgets.length > 0 ? (
          budgets.map((budget) => {
            const overBudget = budget.percentUsed > 100
            const barColor = overBudget ? 'bg-red-500' : budget.percentUsed > 80 ? 'bg-amber-500' : 'bg-brand-500'
            return (
              <div key={budget.id} className="bg-white border border-gray-200 rounded-xl p-4">
                <div className="flex items-center justify-between mb-2">
                  <div>
                    <p className="font-medium text-gray-900">{budget.categoryName}</p>
                    <p className="text-xs text-gray-500">{budget.month}</p>
                  </div>
                  <div className="text-right">
                    <p className="text-sm font-medium text-gray-900">
                      {formatCurrency(budget.spent)} / {formatCurrency(budget.amount)}
                    </p>
                    <button onClick={() => deleteBudget.mutate(budget.id)} className="text-xs text-red-600 hover:underline">
                      Delete
                    </button>
                  </div>
                </div>
                <div className="h-2 bg-gray-100 rounded-full overflow-hidden">
                  <div className={`h-full ${barColor}`} style={{ width: `${Math.min(budget.percentUsed, 100)}%` }} />
                </div>
                <p className="text-xs text-gray-500 mt-1">{budget.percentUsed}% used</p>
              </div>
            )
          })
        ) : (
          <p className="text-sm text-gray-500">No budgets set yet.</p>
        )}
      </div>
    </div>
  )
}

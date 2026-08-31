import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useDeleteExpense, useExpenses } from '../hooks/useExpenses'
import ExpenseFiltersBar from '../components/ExpenseFiltersBar'
import type { ExpenseFilters } from '../types/expense.types'
import { formatCurrency } from '@/utils/format'

const DEFAULT_FILTERS: ExpenseFilters = {
  page: 0,
  size: 10,
  sortBy: 'expenseDate',
  direction: 'desc',
}

export default function ExpensesListPage() {
  const [filters, setFilters] = useState<ExpenseFilters>(DEFAULT_FILTERS)
  const { data, isLoading } = useExpenses(filters)
  const deleteExpense = useDeleteExpense()

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-gray-900">Expenses</h1>
        <Link
          to="/expenses/new"
          className="bg-brand-600 hover:bg-brand-700 text-white text-sm font-medium rounded-md px-4 py-2"
        >
          + Add expense
        </Link>
      </div>

      <ExpenseFiltersBar filters={filters} onChange={setFilters} />

      <div className="bg-white border border-gray-200 rounded-xl overflow-hidden">
        {isLoading ? (
          <p className="p-6 text-sm text-gray-500">Loading expenses…</p>
        ) : data && data.items.length > 0 ? (
          <>
            <table className="w-full text-sm">
              <thead className="bg-gray-50 text-gray-500 text-left">
                <tr>
                  <th className="px-5 py-3 font-medium">Date</th>
                  <th className="px-5 py-3 font-medium">Description</th>
                  <th className="px-5 py-3 font-medium">Category</th>
                  <th className="px-5 py-3 font-medium text-right">Amount</th>
                  <th className="px-5 py-3"></th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {data.items.map((expense) => (
                  <tr key={expense.id} className="hover:bg-gray-50">
                    <td className="px-5 py-3 text-gray-600">{expense.expenseDate}</td>
                    <td className="px-5 py-3 text-gray-800">{expense.description || '—'}</td>
                    <td className="px-5 py-3">
                      <span
                        className="inline-flex items-center gap-1.5 text-xs font-medium px-2 py-0.5 rounded-full"
                        style={{ backgroundColor: `${expense.categoryColor}20`, color: expense.categoryColor }}
                      >
                        {expense.categoryName}
                      </span>
                    </td>
                    <td className="px-5 py-3 text-right font-medium text-gray-900">{formatCurrency(expense.amount)}</td>
                    <td className="px-5 py-3 text-right whitespace-nowrap">
                      <Link to={`/expenses/${expense.id}/edit`} className="text-brand-600 hover:underline text-xs mr-3">
                        Edit
                      </Link>
                      <button
                        onClick={() => deleteExpense.mutate(expense.id)}
                        className="text-red-600 hover:underline text-xs"
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>

            <div className="flex items-center justify-between px-5 py-3 border-t border-gray-100 text-sm text-gray-500">
              <span>
                Page {data.page + 1} of {data.totalPages} · {data.totalItems} total
              </span>
              <div className="flex gap-2">
                <button
                  disabled={data.page === 0}
                  onClick={() => setFilters((f) => ({ ...f, page: f.page - 1 }))}
                  className="px-3 py-1 rounded-md border border-gray-300 disabled:opacity-40"
                >
                  Previous
                </button>
                <button
                  disabled={data.last}
                  onClick={() => setFilters((f) => ({ ...f, page: f.page + 1 }))}
                  className="px-3 py-1 rounded-md border border-gray-300 disabled:opacity-40"
                >
                  Next
                </button>
              </div>
            </div>
          </>
        ) : (
          <p className="p-6 text-sm text-gray-500">No expenses match these filters.</p>
        )}
      </div>
    </div>
  )
}

import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useDeleteExpense, useExpenses } from '../hooks/useExpenses'
import ExpenseFiltersBar from '../components/ExpenseFiltersBar'
import type { ExpenseFilters } from '../types/expense.types'
import { formatCurrency } from '@/utils/format'
import { downloadCsv } from '@/utils/csv'

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

  function handleExport() {
    if (!data || data.items.length === 0) return
    downloadCsv(
      `expenses-${new Date().toISOString().slice(0, 10)}.csv`,
      ['Date', 'Description', 'Category', 'Amount'],
      data.items.map((e) => [e.expenseDate, e.description || '', e.categoryName, e.amount]),
    )
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between gap-3">
        <h1 className="text-xl font-semibold text-gray-900 dark:text-white">Expenses</h1>
        <div className="flex items-center gap-2">
          <button
            onClick={handleExport}
            disabled={!data || data.items.length === 0}
            className="hidden sm:inline-flex items-center gap-1.5 border border-gray-300 dark:border-gray-700 text-gray-700 dark:text-gray-200 hover:bg-gray-50 dark:hover:bg-gray-800 text-sm font-medium rounded-md px-3 py-2 disabled:opacity-40 disabled:cursor-not-allowed transition"
          >
            ⬇ Export CSV
          </button>
          <Link
            to="/expenses/new"
            className="hidden sm:inline-flex bg-brand-600 hover:bg-brand-700 text-white text-sm font-medium rounded-md px-4 py-2 transition"
          >
            + Add expense
          </Link>
        </div>
      </div>

      <ExpenseFiltersBar filters={filters} onChange={setFilters} />

      {data && data.items.length > 0 && (
        <button
          onClick={handleExport}
          className="sm:hidden w-full flex items-center justify-center gap-1.5 border border-gray-300 dark:border-gray-700 text-gray-700 dark:text-gray-200 text-sm font-medium rounded-md px-3 py-2"
        >
          ⬇ Export CSV
        </button>
      )}

      <div className="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl overflow-hidden">
        {isLoading ? (
          <p className="p-6 text-sm text-gray-500 dark:text-gray-400">Loading expenses…</p>
        ) : data && data.items.length > 0 ? (
          <>
            {/* Desktop table (hidden on small screens) */}
            <table className="w-full text-sm hidden sm:table">
              <thead className="bg-gray-50 dark:bg-gray-800/60 text-gray-500 dark:text-gray-400 text-left">
                <tr>
                  <th className="px-5 py-3 font-medium">Date</th>
                  <th className="px-5 py-3 font-medium">Description</th>
                  <th className="px-5 py-3 font-medium">Category</th>
                  <th className="px-5 py-3 font-medium text-right">Amount</th>
                  <th className="px-5 py-3"></th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100 dark:divide-gray-800">
                {data.items.map((expense) => (
                  <tr key={expense.id} className="hover:bg-gray-50 dark:hover:bg-gray-800/50">
                    <td className="px-5 py-3 text-gray-600 dark:text-gray-300">{expense.expenseDate}</td>
                    <td className="px-5 py-3 text-gray-800 dark:text-gray-100">{expense.description || '—'}</td>
                    <td className="px-5 py-3">
                      <span
                        className="inline-flex items-center gap-1.5 text-xs font-medium px-2 py-0.5 rounded-full"
                        style={{ backgroundColor: `${expense.categoryColor}20`, color: expense.categoryColor }}
                      >
                        {expense.categoryName}
                      </span>
                    </td>
                    <td className="px-5 py-3 text-right font-medium text-gray-900 dark:text-white">
                      {formatCurrency(expense.amount)}
                    </td>
                    <td className="px-5 py-3 text-right whitespace-nowrap">
                      <Link to={`/expenses/${expense.id}/edit`} className="text-brand-600 dark:text-brand-100 hover:underline text-xs mr-3">
                        Edit
                      </Link>
                      <button
                        onClick={() => deleteExpense.mutate(expense.id)}
                        className="text-red-600 dark:text-red-400 hover:underline text-xs"
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>

            {/* Mobile card list (hidden on sm+) */}
            <ul className="sm:hidden divide-y divide-gray-100 dark:divide-gray-800">
              {data.items.map((expense) => (
                <li key={expense.id} className="p-4">
                  <div className="flex items-start justify-between gap-3">
                    <div className="min-w-0">
                      <p className="text-gray-900 dark:text-white font-medium truncate">
                        {expense.description || 'Untitled expense'}
                      </p>
                      <p className="text-xs text-gray-500 dark:text-gray-400 mt-0.5">{expense.expenseDate}</p>
                      <span
                        className="inline-flex items-center gap-1.5 text-xs font-medium px-2 py-0.5 rounded-full mt-2"
                        style={{ backgroundColor: `${expense.categoryColor}20`, color: expense.categoryColor }}
                      >
                        {expense.categoryName}
                      </span>
                    </div>
                    <p className="font-semibold text-gray-900 dark:text-white whitespace-nowrap">
                      {formatCurrency(expense.amount)}
                    </p>
                  </div>
                  <div className="flex gap-4 mt-3">
                    <Link to={`/expenses/${expense.id}/edit`} className="text-brand-600 dark:text-brand-100 text-xs font-medium">
                      Edit
                    </Link>
                    <button
                      onClick={() => deleteExpense.mutate(expense.id)}
                      className="text-red-600 dark:text-red-400 text-xs font-medium"
                    >
                      Delete
                    </button>
                  </div>
                </li>
              ))}
            </ul>

            <div className="flex items-center justify-between px-5 py-3 border-t border-gray-100 dark:border-gray-800 text-sm text-gray-500 dark:text-gray-400">
              <span>
                Page {data.page + 1} of {data.totalPages} · {data.totalItems} total
              </span>
              <div className="flex gap-2">
                <button
                  disabled={data.page === 0}
                  onClick={() => setFilters((f) => ({ ...f, page: f.page - 1 }))}
                  className="px-3 py-1 rounded-md border border-gray-300 dark:border-gray-700 disabled:opacity-40"
                >
                  Previous
                </button>
                <button
                  disabled={data.last}
                  onClick={() => setFilters((f) => ({ ...f, page: f.page + 1 }))}
                  className="px-3 py-1 rounded-md border border-gray-300 dark:border-gray-700 disabled:opacity-40"
                >
                  Next
                </button>
              </div>
            </div>
          </>
        ) : (
          <p className="p-6 text-sm text-gray-500 dark:text-gray-400">No expenses match these filters.</p>
        )}
      </div>
    </div>
  )
}
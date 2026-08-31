import { useState } from 'react'
import { useCategories } from '@/features/categories/hooks/useCategories'
import type { ExpenseFilters } from '../types/expense.types'

interface Props {
  filters: ExpenseFilters
  onChange: (filters: ExpenseFilters) => void
}

const inputClass =
  'rounded-md border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-900 text-gray-900 dark:text-gray-100 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500'

export default function ExpenseFiltersBar({ filters, onChange }: Props) {
  const { data: categories } = useCategories()
  // Filters take real estate on small screens, so they start collapsed
  // there and open on demand instead of pushing the expense list down.
  const [expanded, setExpanded] = useState(false)

  function update(patch: Partial<ExpenseFilters>) {
    onChange({ ...filters, ...patch, page: 0 })
  }

  const activeCount = [filters.search, filters.categoryId, filters.from, filters.to].filter(Boolean).length

  return (
    <div className="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-4">
      <button
        onClick={() => setExpanded((e) => !e)}
        className="sm:hidden w-full flex items-center justify-between text-sm font-medium text-gray-700 dark:text-gray-200"
      >
        <span>
          Filters {activeCount > 0 && <span className="text-brand-600 dark:text-brand-100">({activeCount})</span>}
        </span>
        <span aria-hidden>{expanded ? '▲' : '▼'}</span>
      </button>

      <div className={`grid grid-cols-2 md:grid-cols-6 gap-3 ${expanded ? 'mt-3' : 'hidden'} sm:mt-0 sm:grid`}>
        <input
          placeholder="Search description…"
          value={filters.search || ''}
          onChange={(e) => update({ search: e.target.value })}
          className={`col-span-2 ${inputClass}`}
        />
        <select
          value={filters.categoryId ?? ''}
          onChange={(e) => update({ categoryId: e.target.value ? Number(e.target.value) : undefined })}
          className={inputClass}
        >
          <option value="">All categories</option>
          {categories?.map((c) => (
            <option key={c.id} value={c.id}>{c.name}</option>
          ))}
        </select>
        <input
          type="date"
          value={filters.from || ''}
          onChange={(e) => update({ from: e.target.value || undefined })}
          className={inputClass}
        />
        <input
          type="date"
          value={filters.to || ''}
          onChange={(e) => update({ to: e.target.value || undefined })}
          className={inputClass}
        />
        <select
          value={`${filters.sortBy}:${filters.direction}`}
          onChange={(e) => {
            const [sortBy, direction] = e.target.value.split(':')
            update({ sortBy, direction: direction as 'asc' | 'desc' })
          }}
          className={inputClass}
        >
          <option value="expenseDate:desc">Date (newest)</option>
          <option value="expenseDate:asc">Date (oldest)</option>
          <option value="amount:desc">Amount (high-low)</option>
          <option value="amount:asc">Amount (low-high)</option>
        </select>

        {activeCount > 0 && (
          <button
            onClick={() => onChange({ ...filters, search: undefined, categoryId: undefined, from: undefined, to: undefined, page: 0 })}
            className="col-span-2 md:col-span-1 text-sm text-gray-500 dark:text-gray-400 hover:text-gray-800 dark:hover:text-gray-100 underline text-left md:text-center"
          >
            Clear filters
          </button>
        )}
      </div>
    </div>
  )
}
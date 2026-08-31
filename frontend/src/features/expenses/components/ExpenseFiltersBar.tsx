import { useCategories } from '@/features/categories/hooks/useCategories'
import type { ExpenseFilters } from '../types/expense.types'

interface Props {
  filters: ExpenseFilters
  onChange: (filters: ExpenseFilters) => void
}

export default function ExpenseFiltersBar({ filters, onChange }: Props) {
  const { data: categories } = useCategories()

  function update(patch: Partial<ExpenseFilters>) {
    onChange({ ...filters, ...patch, page: 0 })
  }

  return (
    <div className="bg-white border border-gray-200 rounded-xl p-4 grid grid-cols-2 md:grid-cols-6 gap-3">
      <input
        placeholder="Search description…"
        value={filters.search || ''}
        onChange={(e) => update({ search: e.target.value })}
        className="col-span-2 rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
      />
      <select
        value={filters.categoryId ?? ''}
        onChange={(e) => update({ categoryId: e.target.value ? Number(e.target.value) : undefined })}
        className="rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
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
        className="rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
      />
      <input
        type="date"
        value={filters.to || ''}
        onChange={(e) => update({ to: e.target.value || undefined })}
        className="rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
      />
      <select
        value={`${filters.sortBy}:${filters.direction}`}
        onChange={(e) => {
          const [sortBy, direction] = e.target.value.split(':')
          update({ sortBy, direction: direction as 'asc' | 'desc' })
        }}
        className="rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
      >
        <option value="expenseDate:desc">Date (newest)</option>
        <option value="expenseDate:asc">Date (oldest)</option>
        <option value="amount:desc">Amount (high-low)</option>
        <option value="amount:asc">Amount (low-high)</option>
      </select>
    </div>
  )
}

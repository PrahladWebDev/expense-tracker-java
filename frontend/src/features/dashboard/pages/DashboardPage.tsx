import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, Legend } from 'recharts'
import { useSummary, useMonthlySpending, useCategoryBreakdown } from '../hooks/useDashboard'
import { useBudgets } from '@/features/budgets/hooks/useBudgets'
import { formatCurrencyCompact as formatCurrency, formatPercent } from '@/utils/format'

function StatCard({ label, value, sub }: { label: string; value: string; sub?: string }) {
  return (
    <div className="bg-white border border-gray-200 rounded-xl p-5">
      <p className="text-sm text-gray-500">{label}</p>
      <p className="text-2xl font-semibold text-gray-900 mt-1">{value}</p>
      {sub && <p className="text-xs text-gray-500 mt-1">{sub}</p>}
    </div>
  )
}

export default function DashboardPage() {
  const { data: summary } = useSummary()
  const { data: monthly } = useMonthlySpending(6)
  const { data: categories } = useCategoryBreakdown()
  const { data: budgets } = useBudgets()

  const changeLabel = summary ? `${formatPercent(summary.changePercent)} vs last month` : undefined

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-gray-900">Dashboard</h1>

      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <StatCard label="Total spending (all time)" value={summary ? formatCurrency(summary.totalAllTime) : '…'} />
        <StatCard
          label="This month"
          value={summary ? formatCurrency(summary.currentMonth) : '…'}
          sub={changeLabel}
        />
        <StatCard label="Last month" value={summary ? formatCurrency(summary.previousMonth) : '…'} />
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <StatCard
          label="Your share of group expenses (all time)"
          value={summary ? formatCurrency(summary.groupSpendingAllTime) : '…'}
          sub="Your split only, not what you paid up front"
        />
        <StatCard
          label="Your share of group expenses (this month)"
          value={summary ? formatCurrency(summary.groupSpendingCurrentMonth) : '…'}
        />
        <StatCard
          label="Combined spending (this month)"
          value={summary ? formatCurrency(summary.combinedCurrentMonth) : '…'}
          sub="Personal + your group share"
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white border border-gray-200 rounded-xl p-5">
          <h2 className="font-semibold text-gray-900 mb-4">Monthly spending</h2>
          <ResponsiveContainer width="100%" height={260}>
            <BarChart data={monthly || []}>
              <XAxis dataKey="month" tick={{ fontSize: 12 }} />
              <YAxis tick={{ fontSize: 12 }} />
              <Tooltip formatter={(value: number) => formatCurrency(value)} />
              <Bar dataKey="total" fill="#6366f1" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>

        <div className="bg-white border border-gray-200 rounded-xl p-5">
          <h2 className="font-semibold text-gray-900 mb-4">Spending by category (this month)</h2>
          {categories && categories.length > 0 ? (
            <ResponsiveContainer width="100%" height={260}>
              <PieChart>
                <Pie data={categories} dataKey="total" nameKey="categoryName" innerRadius={50} outerRadius={90}>
                  {categories.map((entry) => (
                    <Cell key={entry.categoryId} fill={entry.color || '#6366f1'} />
                  ))}
                </Pie>
                <Legend />
                <Tooltip formatter={(value: number) => formatCurrency(value)} />
              </PieChart>
            </ResponsiveContainer>
          ) : (
            <p className="text-sm text-gray-500 py-16 text-center">No spending recorded this month yet.</p>
          )}
        </div>
      </div>

      <div className="bg-white border border-gray-200 rounded-xl p-5">
        <h2 className="font-semibold text-gray-900 mb-4">Budget usage</h2>
        {budgets && budgets.length > 0 ? (
          <div className="space-y-3">
            {budgets.slice(0, 5).map((budget) => (
              <div key={budget.id}>
                <div className="flex justify-between text-sm mb-1">
                  <span className="text-gray-700">{budget.categoryName} · {budget.month}</span>
                  <span className="text-gray-500">{budget.percentUsed}%</span>
                </div>
                <div className="h-2 bg-gray-100 rounded-full overflow-hidden">
                  <div
                    className={`h-full ${budget.percentUsed > 100 ? 'bg-red-500' : budget.percentUsed > 80 ? 'bg-amber-500' : 'bg-brand-500'}`}
                    style={{ width: `${Math.min(budget.percentUsed, 100)}%` }}
                  />
                </div>
              </div>
            ))}
          </div>
        ) : (
          <p className="text-sm text-gray-500">No budgets set yet. Head to the Budgets page to create one.</p>
        )}
      </div>
    </div>
  )
}

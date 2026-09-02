import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useNavigate, useParams } from 'react-router-dom'
import { useEffect } from 'react'
import { useTranslation } from 'react-i18next'
import { useCategories } from '@/features/categories/hooks/useCategories'
import { useCreateExpense, useExpense, useUpdateExpense } from '../hooks/useExpenses'
import { budgetApi } from '@/features/budgets/api/budgetApi'

const expenseSchema = z.object({
  amount: z.coerce.number().positive('Amount must be positive'),
  description: z.string().max(255).optional(),
  categoryId: z.coerce.number({ invalid_type_error: 'Select a category' }).positive('Select a category'),
  expenseDate: z.string().min(1, 'Date is required'),
})

type ExpenseFormValues = z.infer<typeof expenseSchema>

export default function ExpenseFormPage() {
  const { t } = useTranslation()
  const { id } = useParams()
  const isEdit = !!id
  const navigate = useNavigate()
  const { data: categories } = useCategories()
  const { data: existing } = useExpense(isEdit ? Number(id) : undefined)
  const createExpense = useCreateExpense()
  const updateExpense = useUpdateExpense()

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<ExpenseFormValues>({
    resolver: zodResolver(expenseSchema),
    defaultValues: { expenseDate: new Date().toISOString().slice(0, 10) },
  })

  useEffect(() => {
    if (existing) {
      reset({
        amount: existing.amount,
        description: existing.description || '',
        categoryId: existing.categoryId,
        expenseDate: existing.expenseDate,
      })
    }
  }, [existing, reset])

  // CONCEPT: Post-save budget check (non-blocking warning)
  // We deliberately do NOT stop the expense from saving - a budget here is
  // a target to track against, not a hard limit (see BudgetService: nothing
  // on the backend rejects an over-budget expense). After the expense is
  // safely saved, we fetch the user's budgets fresh (bypassing the cache
  // with budgetApi.getAll() directly, since useCreateExpense/useUpdateExpense
  // just invalidated ['budgets'] - the numbers now include this expense) and
  // see if it pushed anything over 100%. A budget can be scoped to one
  // category OR left "Overall" (categoryId === null, i.e. it covers every
  // category that month) - both need checking.
  async function checkBudgetOverage(categoryId: number, expenseDate: string): Promise<string[]> {
    const month = expenseDate.slice(0, 7) // "YYYY-MM-DD" -> "YYYY-MM"
    try {
      const budgets = await budgetApi.getAll()
      return budgets
        .filter((b) => b.month === month && (b.categoryId === categoryId || b.categoryId === null))
        .filter((b) => b.percentUsed > 100)
        .map((b) => `${b.categoryName} budget for ${b.month} is now ${b.percentUsed}% used (₹${b.spent.toFixed(2)} of ₹${b.amount.toFixed(2)}).`)
    } catch {
      // A failed budget check should never block navigation after a
      // successful expense save - just skip the warning silently.
      return []
    }
  }

  async function onSubmit(values: ExpenseFormValues) {
    const payload = {
      amount: values.amount,
      description: values.description,
      categoryId: values.categoryId,
      expenseDate: values.expenseDate,
    }
    if (isEdit) {
      await updateExpense.mutateAsync({ id: Number(id), payload })
    } else {
      await createExpense.mutateAsync(payload)
    }

    const budgetWarnings = await checkBudgetOverage(values.categoryId, values.expenseDate)
    navigate('/expenses', { state: budgetWarnings.length > 0 ? { budgetWarnings } : undefined })
  }

  return (
    <div className="max-w-md">
      <h1 className="text-xl font-semibold text-gray-900 dark:text-gray-100 mb-6">{isEdit ? t('expenses.editExpense') : t('expenses.addExpense')}</h1>

      <form onSubmit={handleSubmit(onSubmit)} className="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-6 space-y-4">
        <div>
          <label className="block text-sm text-gray-700 dark:text-gray-300 mb-1">{t('expenses.amount')}</label>
          <input
            type="number"
            step="0.01"
            {...register('amount')}
            className="w-full rounded-md border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-900 text-gray-900 dark:text-gray-100 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
            placeholder="0.00"
          />
          {errors.amount && <p className="text-xs text-red-600 mt-1">{errors.amount.message}</p>}
        </div>

        <div>
          <label className="block text-sm text-gray-700 dark:text-gray-300 mb-1">{t('expenses.category')}</label>
          <select
            {...register('categoryId')}
            className="w-full rounded-md border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-900 text-gray-900 dark:text-gray-100 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
          >
            <option value="">{t('expenses.selectCategory')}</option>
            {categories?.map((c) => (
              <option key={c.id} value={c.id}>{c.name}</option>
            ))}
          </select>
          {errors.categoryId && <p className="text-xs text-red-600 mt-1">{errors.categoryId.message}</p>}
        </div>

        <div>
          <label className="block text-sm text-gray-700 dark:text-gray-300 mb-1">{t('expenses.date')}</label>
          <input
            type="date"
            {...register('expenseDate')}
            max={new Date().toISOString().slice(0, 10)}
            className="w-full rounded-md border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-900 text-gray-900 dark:text-gray-100 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
          />
          {errors.expenseDate && <p className="text-xs text-red-600 mt-1">{errors.expenseDate.message}</p>}
        </div>

        <div>
          <label className="block text-sm text-gray-700 dark:text-gray-300 mb-1">{t('expenses.descriptionOptional')}</label>
          <input
            {...register('description')}
            maxLength={255}
            className="w-full rounded-md border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-900 text-gray-900 dark:text-gray-100 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
            placeholder={t('expenses.descriptionPlaceholder')}
          />
        </div>

        <div className="flex gap-2 pt-2">
          <button
            type="submit"
            disabled={isSubmitting}
            className="flex-1 bg-brand-600 hover:bg-brand-700 text-white text-sm font-medium rounded-md py-2 disabled:opacity-60"
          >
            {isEdit ? t('expenses.saveChanges') : t('expenses.addExpense')}
          </button>
          <button
            type="button"
            onClick={() => navigate('/expenses')}
            className="px-4 text-sm text-gray-500 dark:text-gray-400 hover:text-gray-800 dark:hover:text-gray-100"
          >
            {t('common.cancel')}
          </button>
        </div>
      </form>
    </div>
  )
}

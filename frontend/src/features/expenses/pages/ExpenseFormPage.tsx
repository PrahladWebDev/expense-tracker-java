import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useNavigate, useParams } from 'react-router-dom'
import { useEffect } from 'react'
import { useCategories } from '@/features/categories/hooks/useCategories'
import { useCreateExpense, useExpense, useUpdateExpense } from '../hooks/useExpenses'

const expenseSchema = z.object({
  amount: z.coerce.number().positive('Amount must be positive'),
  description: z.string().max(255).optional(),
  categoryId: z.coerce.number({ invalid_type_error: 'Select a category' }).positive('Select a category'),
  expenseDate: z.string().min(1, 'Date is required'),
})

type ExpenseFormValues = z.infer<typeof expenseSchema>

export default function ExpenseFormPage() {
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
    navigate('/expenses')
  }

  return (
    <div className="max-w-md">
      <h1 className="text-xl font-semibold text-gray-900 mb-6">{isEdit ? 'Edit expense' : 'Add expense'}</h1>

      <form onSubmit={handleSubmit(onSubmit)} className="bg-white border border-gray-200 rounded-xl p-6 space-y-4">
        <div>
          <label className="block text-sm text-gray-700 mb-1">Amount</label>
          <input
            type="number"
            step="0.01"
            {...register('amount')}
            className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
            placeholder="0.00"
          />
          {errors.amount && <p className="text-xs text-red-600 mt-1">{errors.amount.message}</p>}
        </div>

        <div>
          <label className="block text-sm text-gray-700 mb-1">Category</label>
          <select
            {...register('categoryId')}
            className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
          >
            <option value="">Select a category</option>
            {categories?.map((c) => (
              <option key={c.id} value={c.id}>{c.name}</option>
            ))}
          </select>
          {errors.categoryId && <p className="text-xs text-red-600 mt-1">{errors.categoryId.message}</p>}
        </div>

        <div>
          <label className="block text-sm text-gray-700 mb-1">Date</label>
          <input
            type="date"
            {...register('expenseDate')}
            max={new Date().toISOString().slice(0, 10)}
            className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
          />
          {errors.expenseDate && <p className="text-xs text-red-600 mt-1">{errors.expenseDate.message}</p>}
        </div>

        <div>
          <label className="block text-sm text-gray-700 mb-1">Description (optional)</label>
          <input
            {...register('description')}
            maxLength={255}
            className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
            placeholder="e.g. Weekly groceries"
          />
        </div>

        <div className="flex gap-2 pt-2">
          <button
            type="submit"
            disabled={isSubmitting}
            className="flex-1 bg-brand-600 hover:bg-brand-700 text-white text-sm font-medium rounded-md py-2 disabled:opacity-60"
          >
            {isEdit ? 'Save changes' : 'Add expense'}
          </button>
          <button
            type="button"
            onClick={() => navigate('/expenses')}
            className="px-4 text-sm text-gray-500 hover:text-gray-800"
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  )
}

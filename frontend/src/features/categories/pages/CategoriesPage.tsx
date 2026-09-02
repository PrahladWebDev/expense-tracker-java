import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useCategories, useCreateCategory, useDeleteCategory, useUpdateCategory } from '../hooks/useCategories'
import type { Category } from '../types/category.types'

const DEFAULT_COLOR = '#6366f1'

export default function CategoriesPage() {
  const { t } = useTranslation()
  const { data: categories, isLoading } = useCategories()
  const createCategory = useCreateCategory()
  const updateCategory = useUpdateCategory()
  const deleteCategory = useDeleteCategory()

  const [name, setName] = useState('')
  const [color, setColor] = useState(DEFAULT_COLOR)
  const [editingId, setEditingId] = useState<number | null>(null)
  const [error, setError] = useState<string | null>(null)

  function startEdit(category: Category) {
    setEditingId(category.id)
    setName(category.name)
    setColor(category.color)
  }

  function resetForm() {
    setEditingId(null)
    setName('')
    setColor(DEFAULT_COLOR)
    setError(null)
  }

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError(null)
    try {
      if (editingId) {
        await updateCategory.mutateAsync({ id: editingId, payload: { name, color } })
      } else {
        await createCategory.mutateAsync({ name, color })
      }
      resetForm()
    } catch (err: any) {
      setError(err?.response?.data?.message || t('common.somethingWrong'))
    }
  }

  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
      <div className="md:col-span-1">
        <div className="bg-white border border-gray-200 rounded-xl p-5">
          <h2 className="font-semibold text-gray-900 mb-4">{editingId ? t('categories.editCategory') : t('categories.newCategory')}</h2>
          {error && <p className="text-sm text-red-600 mb-3">{error}</p>}
          <form onSubmit={onSubmit} className="space-y-3">
            <div>
              <label className="block text-sm text-gray-700 mb-1">{t('categories.name')}</label>
              <input
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
                maxLength={60}
                className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
                placeholder={t('categories.namePlaceholder')}
              />
            </div>
            <div>
              <label className="block text-sm text-gray-700 mb-1">{t('categories.color')}</label>
              <div className="flex items-center gap-2">
                <input type="color" value={color} onChange={(e) => setColor(e.target.value)} className="h-9 w-12 rounded border border-gray-300" />
                <input
                  value={color}
                  onChange={(e) => setColor(e.target.value)}
                  className="flex-1 rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
                />
              </div>
            </div>
            <div className="flex gap-2">
              <button
                type="submit"
                className="flex-1 bg-brand-600 hover:bg-brand-700 text-white text-sm font-medium rounded-md py-2 transition"
              >
                {editingId ? t('expenses.saveChanges') : t('categories.addCategory')}
              </button>
              {editingId && (
                <button type="button" onClick={resetForm} className="px-3 text-sm text-gray-500 hover:text-gray-800">
                  {t('common.cancel')}
                </button>
              )}
            </div>
          </form>
        </div>
      </div>

      <div className="md:col-span-2">
        <div className="bg-white border border-gray-200 rounded-xl overflow-hidden">
          {isLoading ? (
            <p className="p-6 text-sm text-gray-500">{t('categories.loading')}</p>
          ) : categories && categories.length > 0 ? (
            <ul className="divide-y divide-gray-100">
              {categories.map((category) => (
                <li key={category.id} className="flex items-center justify-between px-5 py-3">
                  <div className="flex items-center gap-3">
                    <span className="h-3 w-3 rounded-full" style={{ backgroundColor: category.color || '#ccc' }} />
                    <span className="text-sm text-gray-800">{category.name}</span>
                  </div>
                  <div className="flex gap-3">
                    <button onClick={() => startEdit(category)} className="text-sm text-brand-600 hover:underline">
                      {t('common.edit')}
                    </button>
                    <button
                      onClick={() => deleteCategory.mutate(category.id)}
                      className="text-sm text-red-600 hover:underline"
                    >
                      {t('common.delete')}
                    </button>
                  </div>
                </li>
              ))}
            </ul>
          ) : (
            <p className="p-6 text-sm text-gray-500">{t('categories.noCategories')}</p>
          )}
        </div>
      </div>
    </div>
  )
}

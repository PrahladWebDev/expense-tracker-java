import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '@/app/providers/AuthProvider'
import { useCreateGroup, useGroups } from '../hooks/useGroups'

export default function GroupsListPage() {
  const { user } = useAuth()
  const { data: groups, isLoading } = useGroups()
  const createGroup = useCreateGroup()

  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [error, setError] = useState<string | null>(null)

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError(null)
    try {
      await createGroup.mutateAsync({ name, description: description || undefined })
      setName('')
      setDescription('')
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Something went wrong')
    }
  }

  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
      <div className="md:col-span-1">
        <div className="bg-white border border-gray-200 rounded-xl p-5">
          <h2 className="font-semibold text-gray-900 mb-4">New group</h2>
          {error && <p className="text-sm text-red-600 mb-3">{error}</p>}
          <form onSubmit={onSubmit} className="space-y-3">
            <div>
              <label className="block text-sm text-gray-700 mb-1">Group name</label>
              <input
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
                maxLength={120}
                placeholder="e.g. Goa Trip, Flatmates"
                className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
              />
            </div>
            <div>
              <label className="block text-sm text-gray-700 mb-1">Description (optional)</label>
              <textarea
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                maxLength={500}
                rows={3}
                className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
              />
            </div>
            <button
              type="submit"
              disabled={createGroup.isPending}
              className="w-full bg-brand-600 hover:bg-brand-700 text-white text-sm font-medium rounded-md py-2 disabled:opacity-60"
            >
              Create group
            </button>
          </form>
        </div>
      </div>

      <div className="md:col-span-2 space-y-3">
        {isLoading ? (
          <p className="text-sm text-gray-500">Loading groups…</p>
        ) : groups && groups.length > 0 ? (
          groups.map((group) => (
            <Link
              key={group.id}
              to={`/groups/${group.id}`}
              className="block bg-white border border-gray-200 rounded-xl p-4 hover:border-brand-300 transition"
            >
              <div className="flex items-center justify-between">
                <div>
                  <p className="font-medium text-gray-900">{group.name}</p>
                  {group.description && <p className="text-xs text-gray-500 mt-0.5">{group.description}</p>}
                </div>
                <div className="text-right">
                  <p className="text-xs text-gray-500">{group.members.length} member{group.members.length === 1 ? '' : 's'}</p>
                  {group.createdByUserId === user?.id && (
                    <span className="text-xs text-brand-600 font-medium">Owner</span>
                  )}
                </div>
              </div>
            </Link>
          ))
        ) : (
          <p className="text-sm text-gray-500">
            No groups yet. Create one to start splitting expenses with friends, roommates, or a trip crew.
          </p>
        )}
      </div>
    </div>
  )
}

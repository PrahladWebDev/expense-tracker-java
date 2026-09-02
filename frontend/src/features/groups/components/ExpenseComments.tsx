import { useState } from 'react'
import type { GroupExpense } from '../types/group.types'
import { useAddComment, useComments, useRemoveComment } from '../hooks/useGroups'
import { groupApi } from '../api/groupApi'
import { formatDateTime } from '@/utils/format'

interface Props {
  groupId: number
  expense: GroupExpense
  currentUserId: number
  isClosed: boolean
}

// Shown inline under an expense row once the user expands it - lists notes
// left by group members and (if one was attached) a link to view the
// receipt photo. Kept as its own component so the comment thread only
// fetches once the user actually opens it, not for every expense on the page.
export default function ExpenseComments({ groupId, expense, currentUserId, isClosed }: Props) {
  const { data: comments, isLoading } = useComments(groupId, expense.id)
  const addComment = useAddComment(groupId, expense.id)
  const removeComment = useRemoveComment(groupId, expense.id)
  const [text, setText] = useState('')
  const [receiptUrl, setReceiptUrl] = useState<string | null>(null)
  const [loadingReceipt, setLoadingReceipt] = useState(false)

  async function onViewReceipt() {
    if (receiptUrl) {
      setReceiptUrl(null) // toggle closed
      return
    }
    setLoadingReceipt(true)
    try {
      const url = await groupApi.downloadReceipt(groupId, expense.id)
      setReceiptUrl(url)
    } finally {
      setLoadingReceipt(false)
    }
  }

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (!text.trim()) return
    await addComment.mutateAsync(text.trim())
    setText('')
  }

  return (
    <div className="mt-2 pl-3 border-l-2 border-gray-100 space-y-2">
      {expense.hasReceipt && (
        <div>
          <button onClick={onViewReceipt} disabled={loadingReceipt} className="text-xs text-brand-600 hover:underline">
            {loadingReceipt ? 'Loading…' : receiptUrl ? 'Hide receipt' : `📎 View receipt${expense.receiptOriginalName ? ` (${expense.receiptOriginalName})` : ''}`}
          </button>
          {receiptUrl && (
            <img src={receiptUrl} alt="Receipt" className="mt-2 max-h-64 rounded-md border border-gray-200" />
          )}
        </div>
      )}

      {isLoading ? (
        <p className="text-xs text-gray-400">Loading comments…</p>
      ) : (
        <div className="space-y-1.5">
          {(comments || []).map((c) => (
            <div key={c.id} className="text-xs">
              <span className="font-medium text-gray-700">{c.userName}</span>{' '}
              <span className="text-gray-400">{formatDateTime(c.createdAt)}</span>
              <div className="flex items-start justify-between gap-2">
                <p className="text-gray-600">{c.text}</p>
                {c.userId === currentUserId && !isClosed && (
                  <button onClick={() => removeComment.mutate(c.id)} className="text-red-500 hover:underline shrink-0">
                    Delete
                  </button>
                )}
              </div>
            </div>
          ))}
          {(comments || []).length === 0 && <p className="text-xs text-gray-400">No notes yet.</p>}
        </div>
      )}

      {!isClosed && (
        <form onSubmit={onSubmit} className="flex gap-2">
          <input
            value={text}
            onChange={(e) => setText(e.target.value)}
            placeholder="Add a note…"
            maxLength={1000}
            className="flex-1 rounded-md border border-gray-300 px-2 py-1 text-xs focus:outline-none focus:ring-2 focus:ring-brand-500"
          />
          <button
            type="submit"
            disabled={addComment.isPending || !text.trim()}
            className="text-xs bg-brand-600 hover:bg-brand-700 text-white font-medium rounded-md px-2.5 disabled:opacity-60"
          >
            Post
          </button>
        </form>
      )}
    </div>
  )
}

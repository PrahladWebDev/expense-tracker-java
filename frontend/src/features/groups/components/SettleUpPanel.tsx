import { useState } from 'react'
import { formatCurrency } from '@/utils/format'
import type { GroupMember } from '../types/group.types'
import { useRecordSettlement, useSettlementSuggestions } from '../hooks/useGroups'

interface Props {
  groupId: number
  members: GroupMember[]
}

/**
 * Shows the minimal set of payments that would zero out every balance
 * (computed server-side via debt simplification), and lets any member
 * record that a suggested - or any manual - payment actually happened.
 */
export default function SettleUpPanel({ groupId, members }: Props) {
  const { data: suggestions, isLoading } = useSettlementSuggestions(groupId)
  const recordSettlement = useRecordSettlement(groupId)

  const [manualOpen, setManualOpen] = useState(false)
  const [fromUserId, setFromUserId] = useState(members[0]?.userId)
  const [toUserId, setToUserId] = useState(members[1]?.userId ?? members[0]?.userId)
  const [amount, setAmount] = useState('')
  const [note, setNote] = useState('')
  const [error, setError] = useState<string | null>(null)

  async function recordSuggestion(fromId: number, toId: number, amt: number) {
    setError(null)
    try {
      await recordSettlement.mutateAsync({ fromUserId: fromId, toUserId: toId, amount: amt, note: 'Settled up' })
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Something went wrong')
    }
  }

  async function onManualSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError(null)
    try {
      await recordSettlement.mutateAsync({
        fromUserId: Number(fromUserId),
        toUserId: Number(toUserId),
        amount: Number(amount),
        note: note || undefined,
      })
      setAmount('')
      setNote('')
      setManualOpen(false)
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Something went wrong')
    }
  }

  return (
    <div className="bg-white border border-gray-200 rounded-xl p-5">
      <div className="flex items-center justify-between mb-3">
        <h2 className="font-semibold text-gray-900">Settle up</h2>
        <button onClick={() => setManualOpen((o) => !o)} className="text-xs text-brand-600 hover:underline">
          {manualOpen ? 'Cancel' : 'Record a payment'}
        </button>
      </div>

      {error && <p className="text-sm text-red-600 mb-3">{error}</p>}

      {manualOpen && (
        <form onSubmit={onManualSubmit} className="space-y-2 mb-4 border border-gray-200 rounded-lg p-3">
          <div className="grid grid-cols-2 gap-2">
            <div>
              <label className="block text-xs text-gray-600 mb-1">From</label>
              <select
                value={fromUserId}
                onChange={(e) => setFromUserId(Number(e.target.value))}
                className="w-full rounded-md border border-gray-300 px-2 py-1.5 text-sm"
              >
                {members.map((m) => <option key={m.userId} value={m.userId}>{m.fullName}</option>)}
              </select>
            </div>
            <div>
              <label className="block text-xs text-gray-600 mb-1">To</label>
              <select
                value={toUserId}
                onChange={(e) => setToUserId(Number(e.target.value))}
                className="w-full rounded-md border border-gray-300 px-2 py-1.5 text-sm"
              >
                {members.map((m) => <option key={m.userId} value={m.userId}>{m.fullName}</option>)}
              </select>
            </div>
          </div>
          <input
            type="number"
            step="0.01"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            placeholder="Amount"
            required
            className="w-full rounded-md border border-gray-300 px-2 py-1.5 text-sm"
          />
          <input
            value={note}
            onChange={(e) => setNote(e.target.value)}
            placeholder="Note (optional, e.g. 'Paid via UPI')"
            className="w-full rounded-md border border-gray-300 px-2 py-1.5 text-sm"
          />
          <button
            type="submit"
            disabled={recordSettlement.isPending}
            className="w-full bg-brand-600 hover:bg-brand-700 text-white text-sm font-medium rounded-md py-1.5 disabled:opacity-60"
          >
            Save payment
          </button>
        </form>
      )}

      {isLoading ? (
        <p className="text-sm text-gray-500">Calculating…</p>
      ) : suggestions && suggestions.length > 0 ? (
        <ul className="space-y-2">
          {suggestions.map((s, idx) => (
            <li key={idx} className="flex items-center justify-between text-sm bg-gray-50 rounded-lg px-3 py-2">
              <span className="text-gray-700">
                <span className="font-medium">{s.fromName}</span> pays <span className="font-medium">{s.toName}</span>
              </span>
              <div className="flex items-center gap-2">
                <span className="font-semibold text-gray-900">{formatCurrency(s.amount)}</span>
                <button
                  onClick={() => recordSuggestion(s.fromUserId, s.toUserId, s.amount)}
                  disabled={recordSettlement.isPending}
                  className="text-xs bg-brand-600 hover:bg-brand-700 text-white rounded px-2 py-1 disabled:opacity-60"
                >
                  Mark paid
                </button>
              </div>
            </li>
          ))}
        </ul>
      ) : (
        <p className="text-sm text-gray-500">Everyone's settled up 🎉</p>
      )}
    </div>
  )
}

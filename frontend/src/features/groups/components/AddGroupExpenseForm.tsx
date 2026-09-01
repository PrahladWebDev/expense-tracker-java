import { useState } from 'react'
import type { GroupMember, SplitType } from '../types/group.types'
import { useAddGroupExpense } from '../hooks/useGroups'

interface Props {
  groupId: number
  members: GroupMember[]
  currentUserId: number
  onDone: () => void
}

function todayIso() {
  return new Date().toISOString().slice(0, 10)
}

/**
 * One shared form for all three split types. `values` holds the raw text
 * the user typed per participant - interpreted as an exact rupee amount or
 * a percentage depending on `splitType`, and ignored entirely for EQUAL.
 */
export default function AddGroupExpenseForm({ groupId, members, currentUserId, onDone }: Props) {
  const addExpense = useAddGroupExpense(groupId)

  const [amount, setAmount] = useState('')
  const [description, setDescription] = useState('')
  const [expenseDate, setExpenseDate] = useState(todayIso())
  const [paidByUserId, setPaidByUserId] = useState(currentUserId)
  const [splitType, setSplitType] = useState<SplitType>('EQUAL')
  const [participantIds, setParticipantIds] = useState<number[]>(members.map((m) => m.userId))
  const [values, setValues] = useState<Record<number, string>>({})
  const [error, setError] = useState<string | null>(null)

  function toggleParticipant(userId: number) {
    setParticipantIds((prev) => (prev.includes(userId) ? prev.filter((id) => id !== userId) : [...prev, userId]))
  }

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError(null)

    if (participantIds.length === 0) {
      setError('Select at least one participant')
      return
    }

    try {
      await addExpense.mutateAsync({
        amount: Number(amount),
        description: description || undefined,
        expenseDate,
        paidByUserId,
        splitType,
        shares: participantIds.map((userId) => ({
          userId,
          value: splitType === 'EQUAL' ? undefined : Number(values[userId] || 0),
        })),
      })
      setAmount('')
      setDescription('')
      setValues({})
      onDone()
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Something went wrong')
    }
  }

  return (
    <form onSubmit={onSubmit} className="space-y-3">
      {error && <p className="text-sm text-red-600">{error}</p>}

      <div className="grid grid-cols-2 gap-3">
        <div>
          <label className="block text-sm text-gray-700 mb-1">Amount</label>
          <input
            type="number"
            step="0.01"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            required
            className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
          />
        </div>
        <div>
          <label className="block text-sm text-gray-700 mb-1">Date</label>
          <input
            type="date"
            value={expenseDate}
            onChange={(e) => setExpenseDate(e.target.value)}
            max={todayIso()}
            required
            className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
          />
        </div>
      </div>

      <div>
        <label className="block text-sm text-gray-700 mb-1">Description</label>
        <input
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          maxLength={255}
          placeholder="e.g. Dinner at the beach shack"
          className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
        />
      </div>

      <div>
        <label className="block text-sm text-gray-700 mb-1">Paid by</label>
        <select
          value={paidByUserId}
          onChange={(e) => setPaidByUserId(Number(e.target.value))}
          className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
        >
          {members.map((m) => (
            <option key={m.userId} value={m.userId}>{m.fullName}</option>
          ))}
        </select>
      </div>

      <div>
        <label className="block text-sm text-gray-700 mb-1">Split</label>
        <div className="flex gap-2">
          {(['EQUAL', 'EXACT', 'PERCENTAGE'] as SplitType[]).map((t) => (
            <button
              key={t}
              type="button"
              onClick={() => setSplitType(t)}
              className={`flex-1 rounded-md border px-3 py-1.5 text-sm font-medium transition ${
                splitType === t
                  ? 'bg-brand-600 border-brand-600 text-white'
                  : 'border-gray-300 text-gray-600 hover:bg-gray-50'
              }`}
            >
              {t === 'EQUAL' ? 'Equally' : t === 'EXACT' ? 'Exact amounts' : 'Percentages'}
            </button>
          ))}
        </div>
      </div>

      <div>
        <label className="block text-sm text-gray-700 mb-1">
          Split between {splitType !== 'EQUAL' && '(enter each person\'s ' + (splitType === 'EXACT' ? 'amount' : '%') + ')'}
        </label>
        <div className="space-y-1.5">
          {members.map((m) => {
            const checked = participantIds.includes(m.userId)
            return (
              <div key={m.userId} className="flex items-center gap-2">
                <input
                  type="checkbox"
                  checked={checked}
                  onChange={() => toggleParticipant(m.userId)}
                  className="rounded border-gray-300"
                />
                <span className="flex-1 text-sm text-gray-700">{m.fullName}</span>
                {checked && splitType !== 'EQUAL' && (
                  <input
                    type="number"
                    step="0.01"
                    value={values[m.userId] || ''}
                    onChange={(e) => setValues((v) => ({ ...v, [m.userId]: e.target.value }))}
                    placeholder={splitType === 'EXACT' ? '₹' : '%'}
                    className="w-24 rounded-md border border-gray-300 px-2 py-1 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
                  />
                )}
              </div>
            )
          })}
        </div>
      </div>

      <button
        type="submit"
        disabled={addExpense.isPending}
        className="w-full bg-brand-600 hover:bg-brand-700 text-white text-sm font-medium rounded-md py-2 disabled:opacity-60"
      >
        Add group expense
      </button>
    </form>
  )
}

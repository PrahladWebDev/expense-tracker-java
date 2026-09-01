import { useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useAuth } from '@/app/providers/AuthProvider'
import { formatCurrency } from '@/utils/format'
import { groupApi } from '../api/groupApi'
import AddGroupExpenseForm from '../components/AddGroupExpenseForm'
import SettleUpPanel from '../components/SettleUpPanel'
import {
  useAddMember,
  useDeleteGroup,
  useGroup,
  useGroupBalances,
  useGroupExpenses,
  useRemoveGroupExpense,
  useRemoveMember,
  useSettlements,
} from '../hooks/useGroups'

function openPdfInNewTab(objectUrl: string) {
  window.open(objectUrl, '_blank', 'noopener,noreferrer')
}

export default function GroupDetailPage() {
  const { id } = useParams<{ id: string }>()
  const groupId = Number(id)
  const navigate = useNavigate()
  const { user } = useAuth()

  const { data: group, isLoading } = useGroup(groupId)
  const { data: balances } = useGroupBalances(groupId)
  const { data: expenses } = useGroupExpenses(groupId)
  const { data: settlements } = useSettlements(groupId)

  const addMember = useAddMember(groupId)
  const removeMember = useRemoveMember(groupId)
  const deleteGroup = useDeleteGroup()
  const removeExpense = useRemoveGroupExpense(groupId)

  const [memberEmail, setMemberEmail] = useState('')
  const [memberError, setMemberError] = useState<string | null>(null)
  const [showAddExpense, setShowAddExpense] = useState(false)
  const [downloadingReport, setDownloadingReport] = useState(false)
  const [downloadingStatementFor, setDownloadingStatementFor] = useState<number | null>(null)

  if (isLoading || !group) {
    return <p className="text-sm text-gray-500">Loading group…</p>
  }

  const isOwner = group.createdByUserId === user?.id
  const balanceByUserId = new Map((balances || []).map((b) => [b.userId, b]))

  async function onAddMember(e: React.FormEvent) {
    e.preventDefault()
    setMemberError(null)
    try {
      await addMember.mutateAsync({ email: memberEmail })
      setMemberEmail('')
    } catch (err: any) {
      setMemberError(err?.response?.data?.message || 'Could not add that member')
    }
  }

  async function onDownloadGroupReport() {
    setDownloadingReport(true)
    try {
      const url = await groupApi.downloadGroupReportPdf(groupId)
      openPdfInNewTab(url)
    } finally {
      setDownloadingReport(false)
    }
  }

  async function onDownloadStatement(memberUserId: number) {
    setDownloadingStatementFor(memberUserId)
    try {
      const url = await groupApi.downloadMemberStatementPdf(groupId, memberUserId)
      openPdfInNewTab(url)
    } finally {
      setDownloadingStatementFor(null)
    }
  }

  async function onDeleteGroup() {
    if (!confirm('Delete this group? This only works if it has no expenses recorded.')) return
    try {
      await deleteGroup.mutateAsync(groupId)
      navigate('/groups')
    } catch (err: any) {
      alert(err?.response?.data?.message || 'Could not delete group')
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between gap-3 flex-wrap">
        <div>
          <h1 className="text-xl font-semibold text-gray-900">{group.name}</h1>
          {group.description && <p className="text-sm text-gray-500 mt-0.5">{group.description}</p>}
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={onDownloadGroupReport}
            disabled={downloadingReport}
            className="text-sm rounded-md border border-gray-300 px-3 py-1.5 hover:bg-gray-50 disabled:opacity-60"
          >
            {downloadingReport ? 'Preparing…' : '📄 Group PDF report'}
          </button>
          {isOwner && (
            <button
              onClick={onDeleteGroup}
              className="text-sm rounded-md border border-red-200 text-red-600 px-3 py-1.5 hover:bg-red-50"
            >
              Delete group
            </button>
          )}
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Members + balances */}
        <div className="lg:col-span-1 space-y-6">
          <div className="bg-white border border-gray-200 rounded-xl p-5">
            <h2 className="font-semibold text-gray-900 mb-3">Members</h2>
            <ul className="space-y-2 mb-4">
              {group.members.map((m) => {
                const balance = balanceByUserId.get(m.userId)
                return (
                  <li key={m.userId} className="flex items-center justify-between text-sm">
                    <div>
                      <p className="text-gray-900">
                        {m.fullName} {m.role === 'OWNER' && <span className="text-xs text-brand-600">(owner)</span>}
                      </p>
                      {balance && (
                        <p className={`text-xs ${balance.netBalance > 0 ? 'text-green-600' : balance.netBalance < 0 ? 'text-red-600' : 'text-gray-500'}`}>
                          {balance.netBalance > 0
                            ? `is owed ${formatCurrency(balance.netBalance)}`
                            : balance.netBalance < 0
                            ? `owes ${formatCurrency(Math.abs(balance.netBalance))}`
                            : 'settled up'}
                        </p>
                      )}
                    </div>
                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => onDownloadStatement(m.userId)}
                        disabled={downloadingStatementFor === m.userId}
                        className="text-xs text-brand-600 hover:underline disabled:opacity-60"
                      >
                        {downloadingStatementFor === m.userId ? '…' : 'Statement PDF'}
                      </button>
                      {isOwner && m.role !== 'OWNER' && (
                        <button
                          onClick={() => removeMember.mutate(m.userId)}
                          className="text-xs text-red-600 hover:underline"
                        >
                          Remove
                        </button>
                      )}
                    </div>
                  </li>
                )
              })}
            </ul>

            {memberError && <p className="text-sm text-red-600 mb-2">{memberError}</p>}
            <form onSubmit={onAddMember} className="flex gap-2">
              <input
                type="email"
                value={memberEmail}
                onChange={(e) => setMemberEmail(e.target.value)}
                placeholder="Add member by email"
                required
                className="flex-1 rounded-md border border-gray-300 px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
              />
              <button
                type="submit"
                disabled={addMember.isPending}
                className="bg-brand-600 hover:bg-brand-700 text-white text-sm font-medium rounded-md px-3 disabled:opacity-60"
              >
                Add
              </button>
            </form>
          </div>

          <SettleUpPanel groupId={groupId} members={group.members} />

          <div className="bg-white border border-gray-200 rounded-xl p-5">
            <h2 className="font-semibold text-gray-900 mb-3">Settlement history</h2>
            {settlements && settlements.length > 0 ? (
              <ul className="space-y-2">
                {settlements.map((s) => (
                  <li key={s.id} className="text-sm text-gray-700">
                    <span className="font-medium">{s.fromName}</span> paid <span className="font-medium">{s.toName}</span>{' '}
                    {formatCurrency(s.amount)}
                    {s.note && <span className="text-gray-400"> — {s.note}</span>}
                    <p className="text-xs text-gray-400">{new Date(s.settledAt).toLocaleString()}</p>
                  </li>
                ))}
              </ul>
            ) : (
              <p className="text-sm text-gray-500">No settlements recorded yet.</p>
            )}
          </div>
        </div>

        {/* Expenses */}
        <div className="lg:col-span-2 space-y-4">
          <div className="bg-white border border-gray-200 rounded-xl p-5">
            <div className="flex items-center justify-between mb-4">
              <h2 className="font-semibold text-gray-900">Group expenses</h2>
              <button
                onClick={() => setShowAddExpense((o) => !o)}
                className="text-sm bg-brand-600 hover:bg-brand-700 text-white font-medium rounded-md px-3 py-1.5"
              >
                {showAddExpense ? 'Cancel' : '+ Add expense'}
              </button>
            </div>

            {showAddExpense && (
              <div className="mb-5 border border-gray-200 rounded-lg p-4">
                <AddGroupExpenseForm
                  groupId={groupId}
                  members={group.members}
                  currentUserId={user?.id ?? group.members[0].userId}
                  onDone={() => setShowAddExpense(false)}
                />
              </div>
            )}

            {expenses && expenses.length > 0 ? (
              <ul className="space-y-3">
                {expenses.map((e) => (
                  <li key={e.id} className="border border-gray-100 rounded-lg p-3">
                    <div className="flex items-start justify-between">
                      <div>
                        <p className="font-medium text-gray-900">{e.description || 'Group expense'}</p>
                        <p className="text-xs text-gray-500">
                          {e.expenseDate} · paid by {e.paidByName} · {e.splitType.toLowerCase()} split
                        </p>
                      </div>
                      <div className="text-right">
                        <p className="font-semibold text-gray-900">{formatCurrency(e.amount)}</p>
                        <button
                          onClick={() => removeExpense.mutate(e.id)}
                          className="text-xs text-red-600 hover:underline"
                        >
                          Delete
                        </button>
                      </div>
                    </div>
                    <p className="text-xs text-gray-400 mt-2">
                      {e.shares.map((s) => `${s.fullName}: ${formatCurrency(s.shareAmount)}`).join(' · ')}
                    </p>
                  </li>
                ))}
              </ul>
            ) : (
              <p className="text-sm text-gray-500">No group expenses yet.</p>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

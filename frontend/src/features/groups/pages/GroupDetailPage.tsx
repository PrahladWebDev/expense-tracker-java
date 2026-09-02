import { useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { useAuth } from '@/app/providers/AuthProvider'
import { formatCurrency } from '@/utils/format'
import { groupApi } from '../api/groupApi'
import AddGroupExpenseForm from '../components/AddGroupExpenseForm'
import SettleUpPanel from '../components/SettleUpPanel'
import ActivityFeed from '../components/ActivityFeed'
import ExpenseComments from '../components/ExpenseComments'
import CollapsibleSection from '@/components/CollapsibleSection'
import {
  useAddMember,
  useCloseGroup,
  useDeleteGroup,
  useGroup,
  useGroupBalances,
  useGroupExpenses,
  useReopenGroup,
  useRegenerateInviteCode,
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
  const { t } = useTranslation()

  const { data: group, isLoading } = useGroup(groupId)
  const { data: balances } = useGroupBalances(groupId)
  const { data: expenses } = useGroupExpenses(groupId)
  const { data: settlements } = useSettlements(groupId)

  const addMember = useAddMember(groupId)
  const removeMember = useRemoveMember(groupId)
  const deleteGroup = useDeleteGroup()
  const removeExpense = useRemoveGroupExpense(groupId)
  const closeGroup = useCloseGroup(groupId)
  const reopenGroup = useReopenGroup(groupId)
  const regenerateInviteCode = useRegenerateInviteCode(groupId)

  const [memberEmail, setMemberEmail] = useState('')
  const [memberError, setMemberError] = useState<string | null>(null)
  const [showAddExpense, setShowAddExpense] = useState(false)
  const [downloadingReport, setDownloadingReport] = useState(false)
  const [downloadingCsv, setDownloadingCsv] = useState(false)
  const [downloadingStatementFor, setDownloadingStatementFor] = useState<number | null>(null)
  const [expandedExpenseId, setExpandedExpenseId] = useState<number | null>(null)
  const [inviteCopied, setInviteCopied] = useState(false)

  if (isLoading || !group) {
    return <p className="text-sm text-gray-500 dark:text-gray-400">Loading group…</p>
  }

  const isOwner = group.createdByUserId === user?.id
  const isClosed = group.status === 'CLOSED'
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

  async function onDownloadCsv() {
    setDownloadingCsv(true)
    try {
      const url = await groupApi.downloadGroupReportCsv(groupId)
      const a = document.createElement('a')
      a.href = url
      a.download = `group-${groupId}-report.csv`
      a.click()
    } finally {
      setDownloadingCsv(false)
    }
  }

  function inviteLink() {
    return `${window.location.origin}/groups/join/${group?.inviteCode ?? ''}`
  }

  async function onCopyInviteLink() {
    await navigator.clipboard.writeText(inviteLink())
    setInviteCopied(true)
    setTimeout(() => setInviteCopied(false), 2000)
  }

  async function onRegenerateInviteLink() {
    if (!confirm('Regenerate the invite link? The old link will stop working.')) return
    await regenerateInviteCode.mutateAsync()
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

  async function onCloseGroup() {
    if (!confirm('Close this group? It becomes read-only - no new expenses, members or settlements until you reopen it.')) return
    try {
      await closeGroup.mutateAsync()
      setShowAddExpense(false)
    } catch (err: any) {
      alert(err?.response?.data?.message || 'Could not close group')
    }
  }

  async function onReopenGroup() {
    try {
      await reopenGroup.mutateAsync()
    } catch (err: any) {
      alert(err?.response?.data?.message || 'Could not reopen group')
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between gap-3 flex-wrap">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-xl font-semibold text-gray-900 dark:text-gray-100">{group.name}</h1>
            {isClosed && (
              <span className="text-xs font-medium text-amber-700 dark:text-amber-200 bg-amber-100 dark:bg-amber-900/40 rounded-full px-2 py-0.5">
                Closed
              </span>
            )}
          </div>
          {group.description && <p className="text-sm text-gray-500 dark:text-gray-400 mt-0.5">{group.description}</p>}
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={onDownloadGroupReport}
            disabled={downloadingReport}
            className="text-sm rounded-md border border-gray-300 dark:border-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-800 disabled:opacity-60"
          >
            {downloadingReport ? 'Preparing…' : '📄 Group PDF report'}
          </button>
          <button
            onClick={onDownloadCsv}
            disabled={downloadingCsv}
            className="text-sm rounded-md border border-gray-300 dark:border-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-800 disabled:opacity-60"
          >
            {downloadingCsv ? 'Preparing…' : '📊 Export CSV'}
          </button>
          {isOwner && !isClosed && (
            <button
              onClick={onCloseGroup}
              disabled={closeGroup.isPending}
              className="text-sm rounded-md border border-amber-300 dark:border-amber-700 text-amber-700 dark:text-amber-300 px-3 py-1.5 hover:bg-amber-50 dark:hover:bg-amber-900/30 disabled:opacity-60"
            >
              {closeGroup.isPending ? 'Closing…' : 'Close group'}
            </button>
          )}
          {isOwner && isClosed && (
            <button
              onClick={onReopenGroup}
              disabled={reopenGroup.isPending}
              className="text-sm rounded-md border border-brand-300 dark:border-brand-700 text-brand-700 dark:text-brand-100 px-3 py-1.5 hover:bg-brand-50 dark:hover:bg-brand-900/30 disabled:opacity-60"
            >
              {reopenGroup.isPending ? 'Reopening…' : 'Reopen group'}
            </button>
          )}
          {isOwner && (
            <button
              onClick={onDeleteGroup}
              className="text-sm rounded-md border border-red-200 dark:border-red-800 text-red-600 dark:text-red-300 px-3 py-1.5 hover:bg-red-50 dark:hover:bg-red-900/30"
            >
              Delete group
            </button>
          )}
        </div>
      </div>

      {isClosed && (
        <div className="bg-amber-50 dark:bg-amber-900/30 border border-amber-200 dark:border-amber-800 text-amber-800 dark:text-amber-200 text-sm rounded-lg px-4 py-2">
          This group is closed. You can still view all expenses, balances and settlement history, but nothing new
          can be added until it's reopened.
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Members + balances */}
        <div className="lg:col-span-1 space-y-6">
          <div className="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-5">
            <CollapsibleSection title="Members">
            <ul className="space-y-2 mb-4">
              {group.members.map((m) => {
                const balance = balanceByUserId.get(m.userId)
                return (
                  <li key={m.userId} className="flex items-center justify-between text-sm">
                    <div>
                      <p className="text-gray-900 dark:text-gray-100">
                        {m.fullName} {m.role === 'OWNER' && <span className="text-xs text-brand-600 dark:text-brand-100">({t('settlement.owner')})</span>}
                      </p>
                      {balance && (
                        <p className={`text-xs ${balance.netBalance > 0 ? 'text-green-600 dark:text-green-400' : balance.netBalance < 0 ? 'text-red-600 dark:text-red-400' : 'text-gray-500 dark:text-gray-400'}`}>
                          {balance.netBalance > 0
                            ? t('settlement.isOwed', { amount: formatCurrency(balance.netBalance) })
                            : balance.netBalance < 0
                            ? t('settlement.owes', { amount: formatCurrency(Math.abs(balance.netBalance)) })
                            : t('settlement.settledUp')}
                        </p>
                      )}
                    </div>
                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => onDownloadStatement(m.userId)}
                        disabled={downloadingStatementFor === m.userId}
                        className="text-xs text-brand-600 dark:text-brand-100 hover:underline disabled:opacity-60"
                      >
                        {downloadingStatementFor === m.userId ? '…' : t('settlement.statementPdf')}
                      </button>
                      {isOwner && m.role !== 'OWNER' && !isClosed && (
                        <button
                          onClick={() => removeMember.mutate(m.userId)}
                          className="text-xs text-red-600 dark:text-red-400 hover:underline"
                        >
                          {t('settlement.remove')}
                        </button>
                      )}
                    </div>
                  </li>
                )
              })}
            </ul>

            {memberError && <p className="text-sm text-red-600 dark:text-red-400 mb-2">{memberError}</p>}
            {!isClosed && (
              <form onSubmit={onAddMember} className="flex gap-2">
                <input
                  type="email"
                  value={memberEmail}
                  onChange={(e) => setMemberEmail(e.target.value)}
                  placeholder="Add member by email"
                  required
                  className="flex-1 rounded-md border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-900 text-gray-900 dark:text-gray-100 px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
                />
                <button
                  type="submit"
                  disabled={addMember.isPending}
                  className="bg-brand-600 hover:bg-brand-700 text-white text-sm font-medium rounded-md px-3 disabled:opacity-60"
                >
                  Add
                </button>
              </form>
            )}
            </CollapsibleSection>
          </div>

          {!isClosed && <SettleUpPanel groupId={groupId} members={group.members} />}

          {isOwner && (
            <div className="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-5">
              <h2 className="font-semibold text-gray-900 dark:text-gray-100 mb-2">Invite link</h2>
              <p className="text-xs text-gray-500 dark:text-gray-400 mb-3">
                Share this link so a friend can join the group directly, even if they're not in anyone's contacts.
              </p>
              <div className="flex gap-2">
                <input
                  readOnly
                  value={inviteLink()}
                  onFocus={(e) => e.target.select()}
                  className="flex-1 rounded-md border border-gray-300 dark:border-gray-700 px-2 py-1.5 text-xs text-gray-600 dark:text-gray-300 bg-gray-50 dark:bg-gray-800"
                />
                <button
                  onClick={onCopyInviteLink}
                  className="text-xs rounded-md border border-gray-300 dark:border-gray-700 text-gray-700 dark:text-gray-300 px-2.5 py-1.5 hover:bg-gray-50 dark:hover:bg-gray-800 shrink-0"
                >
                  {inviteCopied ? 'Copied!' : 'Copy'}
                </button>
              </div>
              {!isClosed && (
                <button
                  onClick={onRegenerateInviteLink}
                  disabled={regenerateInviteCode.isPending}
                  className="mt-2 text-xs text-red-600 dark:text-red-400 hover:underline disabled:opacity-60"
                >
                  Regenerate link (invalidates the old one)
                </button>
              )}
            </div>
          )}

          <div className="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-5">
            <CollapsibleSection title="Activity">
              <ActivityFeed groupId={groupId} />
            </CollapsibleSection>
          </div>

          <div className="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-5">
            <CollapsibleSection title="Settlement history">
              {settlements && settlements.length > 0 ? (
                <ul className="space-y-2">
                  {settlements.map((s) => (
                    <li key={s.id} className="text-sm text-gray-700 dark:text-gray-300">
                      <span className="font-medium">{s.fromName}</span> paid <span className="font-medium">{s.toName}</span>{' '}
                      {formatCurrency(s.amount)}
                      {s.note && <span className="text-gray-400 dark:text-gray-500"> — {s.note}</span>}
                      <p className="text-xs text-gray-400 dark:text-gray-500">{new Date(s.settledAt).toLocaleString()}</p>
                    </li>
                  ))}
                </ul>
              ) : (
                <p className="text-sm text-gray-500 dark:text-gray-400">No settlements recorded yet.</p>
              )}
            </CollapsibleSection>
          </div>
        </div>

        {/* Expenses */}
        <div className="lg:col-span-2 space-y-4">
          <div className="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-5">
            <div className="flex items-center justify-between mb-4">
              <h2 className="font-semibold text-gray-900 dark:text-gray-100">Group expenses</h2>
              {!isClosed && (
                <button
                  onClick={() => setShowAddExpense((o) => !o)}
                  className="text-sm bg-brand-600 hover:bg-brand-700 text-white font-medium rounded-md px-3 py-1.5"
                >
                  {showAddExpense ? 'Cancel' : '+ Add expense'}
                </button>
              )}
            </div>

            {showAddExpense && !isClosed && (
              <div className="mb-5 border border-gray-200 dark:border-gray-700 rounded-lg p-4">
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
                  <li
                    key={e.id}
                    className={`border rounded-lg p-3 ${
                      e.deleted ? 'border-gray-100 dark:border-gray-800 bg-gray-50 dark:bg-gray-800/60' : 'border-gray-100 dark:border-gray-800'
                    }`}
                  >
                    <div className="flex items-start justify-between">
                      <div className={e.deleted ? 'line-through decoration-red-400 opacity-60' : undefined}>
                        <p className="font-medium text-gray-900 dark:text-gray-100">{e.description || 'Group expense'}</p>
                        <p className="text-xs text-gray-500 dark:text-gray-400">
                          {e.expenseDate} · paid by {e.paidByName} · {e.splitType.toLowerCase()} split
                          {e.hasReceipt && ' · 📎 receipt'}
                        </p>
                      </div>
                      <div className="text-right">
                        <p
                          className={`font-semibold ${
                            e.deleted ? 'text-gray-400 dark:text-gray-500 line-through decoration-red-400' : 'text-gray-900 dark:text-gray-100'
                          }`}
                        >
                          {formatCurrency(e.amount)}
                        </p>
                        {!isClosed && !e.deleted && (
                          <button
                            onClick={() => removeExpense.mutate(e.id)}
                            className="text-xs text-red-600 dark:text-red-400 hover:underline"
                          >
                            Delete
                          </button>
                        )}
                      </div>
                    </div>
                    {e.deleted ? (
                      <p className="text-xs text-red-500 dark:text-red-400 mt-2">
                        Deleted by {e.deletedByName ?? 'a member'} · excluded from totals and balances
                      </p>
                    ) : (
                      <p className="text-xs text-gray-400 dark:text-gray-500 mt-2">
                        {e.shares.map((s) => `${s.fullName}: ${formatCurrency(s.shareAmount)}`).join(' · ')}
                      </p>
                    )}
                    {!e.deleted && (
                      <button
                        onClick={() => setExpandedExpenseId((cur) => (cur === e.id ? null : e.id))}
                        className="text-xs text-brand-600 dark:text-brand-100 hover:underline mt-2"
                      >
                        {expandedExpenseId === e.id ? 'Hide notes' : 'Notes & receipt'}
                      </button>
                    )}
                    {expandedExpenseId === e.id && !e.deleted && (
                      <ExpenseComments
                        groupId={groupId}
                        expense={e}
                        currentUserId={user?.id ?? 0}
                        isClosed={isClosed}
                      />
                    )}
                  </li>
                ))}
              </ul>
            ) : (
              <p className="text-sm text-gray-500 dark:text-gray-400">No group expenses yet.</p>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

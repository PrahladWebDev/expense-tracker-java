import { useGroupActivity } from '../hooks/useGroups'
import { formatDateTime } from '@/utils/format'

interface Props {
  groupId: number
}

const ICONS: Record<string, string> = {
  EXPENSE_ADDED: '💸',
  EXPENSE_DELETED: '🗑️',
  MEMBER_ADDED: '➕',
  MEMBER_REMOVED: '➖',
  SETTLEMENT_RECORDED: '✅',
  COMMENT_ADDED: '💬',
  GROUP_CLOSED: '🔒',
  GROUP_REOPENED: '🔓',
  GROUP_CREATED: '🎉',
}

// Pull-based feed (no websockets/push here) - the parent page re-fetches
// this on the same schedule as balances/expenses, right after any mutation.
export default function ActivityFeed({ groupId }: Props) {
  const { data: activity, isLoading } = useGroupActivity(groupId)

  if (isLoading) return <p className="text-sm text-gray-400 dark:text-gray-500">Loading activity…</p>

  if (!activity || activity.length === 0) {
    return <p className="text-sm text-gray-400 dark:text-gray-500">No activity yet.</p>
  }

  return (
    <ul className="space-y-2 max-h-80 overflow-y-auto">
      {activity.map((a) => (
        <li key={a.id} className="flex items-start gap-2 text-sm">
          <span>{ICONS[a.type] || '•'}</span>
          <div>
            <p className="text-gray-700 dark:text-gray-300">{a.message}</p>
            <p className="text-xs text-gray-400 dark:text-gray-500">{formatDateTime(a.createdAt)}</p>
          </div>
        </li>
      ))}
    </ul>
  )
}

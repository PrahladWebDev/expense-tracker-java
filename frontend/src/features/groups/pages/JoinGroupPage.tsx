import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useJoinByInviteCode } from '../hooks/useGroups'

// CONCEPT: join-by-link flow
// The link a group owner shares looks like /groups/join/AB12CD34EF. This
// page fires the join mutation once on mount, then redirects into the
// group's detail page on success (or back to the groups list with an
// error if the code is invalid/expired or they're already a member).
export default function JoinGroupPage() {
  const { code } = useParams<{ code: string }>()
  const navigate = useNavigate()
  const joinByCode = useJoinByInviteCode()
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!code) return
    joinByCode.mutate(code, {
      onSuccess: (group) => navigate(`/groups/${group.id}`, { replace: true }),
      onError: (err: any) => setError(err?.response?.data?.message || 'This invite link is invalid or has expired.'),
    })
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [code])

  return (
    <div className="max-w-md mx-auto mt-16 text-center">
      {!error ? (
        <>
          <div className="animate-pulse text-brand-600 font-medium">Joining group…</div>
          <p className="text-sm text-gray-500 mt-2">Just a moment while we add you to the group.</p>
        </>
      ) : (
        <div className="bg-red-50 border border-red-200 text-red-700 rounded-lg px-4 py-3">
          <p className="font-medium">Couldn't join group</p>
          <p className="text-sm mt-1">{error}</p>
          <button
            onClick={() => navigate('/groups')}
            className="mt-3 text-sm rounded-md border border-red-300 px-3 py-1.5 hover:bg-red-100"
          >
            Back to groups
          </button>
        </div>
      )}
    </div>
  )
}

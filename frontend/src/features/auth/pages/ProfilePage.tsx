import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useEffect, useState } from 'react'
import { useProfile, useUpdateProfile } from '../hooks/useProfile'
import { useAuth } from '@/app/providers/AuthProvider'

const profileSchema = z.object({
  fullName: z.string().min(1, 'Full name is required').max(100),
})

type ProfileFormValues = z.infer<typeof profileSchema>

// Deliberately does not let the user change email or password here - see
// the comment on the backend's UpdateProfileRequest for why those are kept
// as separate, more carefully-guarded concerns rather than folded into a
// generic "save profile" form.
export default function ProfilePage() {
  const { data: profile, isLoading } = useProfile()
  const updateProfile = useUpdateProfile()
  const { logout } = useAuth()
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting, isDirty },
  } = useForm<ProfileFormValues>({ resolver: zodResolver(profileSchema) })

  useEffect(() => {
    if (profile) reset({ fullName: profile.fullName })
  }, [profile, reset])

  async function onSubmit(values: ProfileFormValues) {
    setError(null)
    setSuccessMessage(null)
    try {
      await updateProfile.mutateAsync(values)
      setSuccessMessage('Profile updated')
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Something went wrong')
    }
  }

  if (isLoading || !profile) {
    return <p className="text-sm text-gray-500">Loading profile…</p>
  }

  return (
    <div className="max-w-md space-y-6">
      <h1 className="text-xl font-semibold text-gray-900">Profile</h1>

      <div className="bg-white border border-gray-200 rounded-xl p-6 space-y-4">
        {successMessage && (
          <div className="text-sm text-green-700 bg-green-50 border border-green-200 rounded-md px-3 py-2">
            {successMessage}
          </div>
        )}
        {error && (
          <div className="text-sm text-red-700 bg-red-50 border border-red-200 rounded-md px-3 py-2">{error}</div>
        )}

        <div>
          <label className="block text-sm text-gray-700 mb-1">Email</label>
          <input
            value={profile.email}
            disabled
            className="w-full rounded-md border border-gray-200 bg-gray-50 px-3 py-2 text-sm text-gray-500"
          />
          <p className="text-xs text-gray-400 mt-1">Email can't be changed here.</p>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div>
            <label className="block text-sm text-gray-700 mb-1">Full name</label>
            <input
              {...register('fullName')}
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
            />
            {errors.fullName && <p className="text-xs text-red-600 mt-1">{errors.fullName.message}</p>}
          </div>

          <button
            type="submit"
            disabled={isSubmitting || !isDirty}
            className="bg-brand-600 hover:bg-brand-700 text-white text-sm font-medium rounded-md px-4 py-2 disabled:opacity-50"
          >
            {isSubmitting ? 'Saving…' : 'Save changes'}
          </button>
        </form>

        <div className="pt-2 border-t border-gray-100 text-xs text-gray-400">
          Member since {new Date(profile.createdAt).toLocaleDateString()} · Role: {profile.role}
        </div>
      </div>

      <button onClick={() => logout()} className="text-sm text-red-600 hover:underline">
        Log out
      </button>
    </div>
  )
}

export interface Profile {
  id: number
  fullName: string
  email: string
  role: string
  createdAt: string
}

export interface UpdateProfilePayload {
  fullName: string
}

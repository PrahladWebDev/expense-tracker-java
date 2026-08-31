export interface UserSummary {
  id: number
  fullName: string
  email: string
  role: string
}

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  user: UserSummary
}

export interface LoginPayload {
  email: string
  password: string
}

export interface RegisterPayload {
  fullName: string
  email: string
  password: string
}

import React from 'react'
import ReactDOM from 'react-dom/client'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { RouterProvider } from 'react-router-dom'
import { router } from './app/router/router'
import './index.css'
import './i18n/config'

// CONCEPT: TanStack Query's QueryClient
// The QueryClient is the central cache for all "server state" (data that
// actually lives on the backend - expenses, categories, etc.) as opposed to
// "client state" (UI-only things like "is this modal open").
// WHY separate server state from client state (React's useState)?
// Server data can go stale, needs refetching, background updates, retries
// on failure, and de-duplication of identical in-flight requests. Writing
// that by hand with useState/useEffect for every API call is repetitive and
// error-prone. TanStack Query does all of it for us behind two primitives:
// useQuery (for reads) and useMutation (for writes).
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
      staleTime: 30_000, // data is considered "fresh" for 30s before a refetch is triggered
    },
  },
})

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
    </QueryClientProvider>
  </React.StrictMode>,
)

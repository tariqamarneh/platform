<!-- BEGIN:nextjs-agent-rules -->
# This is NOT the Next.js you know

This version has breaking changes — APIs, conventions, and file structure may all differ from your training data. Read the relevant guide in `node_modules/next/dist/docs/` before writing any code. Heed deprecation notices.
<!-- END:nextjs-agent-rules -->

# Web Service Agent Rules

## Critical Next.js 16 Changes
- **`middleware.ts` is DEPRECATED** → use `proxy.ts` with `export function proxy()`
- **`cookies()` is fully async** → must use `await cookies()`
- **`params` and `searchParams` are async** → must await them
- **Turbopack is default** → no `--turbopack` flag needed

## Project Structure
```
src/
├── app/
│   ├── (public)/          Public routes (login, register)
│   ├── (dashboard)/       Protected routes (requires auth)
│   ├── layout.tsx         Root layout
│   ├── page.tsx           Landing page
│   └── globals.css        Dark theme design system
├── components/
│   ├── landing/           Landing page sections (hero, features, pricing, etc.)
│   ├── auth/              Login/register forms, auth layout
│   └── ui/                Shared UI components
├── lib/
│   ├── auth/
│   │   ├── actions.ts     Server actions (login, register, logout, refresh)
│   │   ├── cookies.ts     HTTP-only cookie helpers
│   │   ├── context.tsx    Client-side auth context
│   │   └── types.ts       Auth types
│   └── api/
│       └── client.ts      Auth-service API client
├── hooks/                 Custom React hooks
└── proxy.ts               Route protection (replaces middleware.ts)
```

## Authentication
- JWT tokens stored in HTTP-only secure cookies (NOT localStorage)
- Server actions call auth-service API
- Proxy (`proxy.ts`) protects dashboard routes
- Token refresh handled server-side transparently
- Auth context provides client-side user state

## Key Conventions
- Dark theme: background #030712, accent blue/purple gradients
- Glass morphism: `bg-white/5 backdrop-blur-xl border border-white/10`
- All framer-motion components must be `'use client'`
- Use `@/` path alias for all imports
- Server components by default, `'use client'` only when needed
- Tailwind CSS for all styling, no CSS modules

## Environment Variables
- `AUTH_SERVICE_URL` — auth-service base URL (server-side only, not NEXT_PUBLIC)
- `NEXT_PUBLIC_APP_URL` — public app URL

## Adding New Pages
1. Public pages go in `src/app/(public)/`
2. Protected pages go in `src/app/(dashboard)/`
3. Dashboard layout auto-checks authentication via `getCurrentUser()`
4. Add new public paths to the `PUBLIC_PATHS` array in `src/proxy.ts`

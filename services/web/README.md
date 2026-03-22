# Web Service

Frontend web application for the Business Agent platform. Built with Next.js 16, React 19, and Tailwind CSS.

## Features

- **Landing Page** — Modern dark-themed page with animated hero, features grid, pricing, and CTAs
- **Authentication** — Login and register forms with server-side JWT handling via HTTP-only cookies
- **Route Protection** — Next.js 16 proxy protects dashboard routes, redirects unauthenticated users
- **Responsive** — Mobile-first design with glass morphism and gradient accents

## Tech Stack

| | |
|---|---|
| Framework | Next.js 16 (App Router, Turbopack) |
| UI | React 19, Tailwind CSS 4 |
| Animations | Framer Motion |
| Auth | Custom JWT via HTTP-only cookies (no NextAuth) |
| Build | Standalone output for Docker |

## Quick Start

```bash
# With Docker Compose (from platform root)
docker compose up --build

# Or locally
cd services/web
npm install
npm run dev
```

The app runs on **http://localhost:3000**. Requires auth-service running on port 8080.

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| AUTH_SERVICE_URL | http://localhost:8080 | Auth service base URL (server-side only) |
| NEXT_PUBLIC_APP_URL | http://localhost:3000 | Public app URL |

## Pages

| Path | Auth | Description |
|---|---|---|
| / | No | Landing page |
| /login | No | Login form |
| /register | No | Registration form |
| /dashboard | JWT | Dashboard (placeholder) |

## Project Structure

```
src/
├── app/                 Pages and layouts
│   ├── (public)/        Login, register
│   └── (dashboard)/     Protected routes
├── components/
│   ├── landing/         Hero, features, pricing, etc.
│   └── auth/            Auth forms and layout
├── lib/
│   ├── auth/            Server actions, cookies, types
│   └── api/             Auth-service client
└── proxy.ts             Route protection
```

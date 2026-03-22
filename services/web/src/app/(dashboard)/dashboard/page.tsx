import type { Metadata } from 'next';
import { getCurrentUser } from '@/lib/auth/actions';
import { redirect } from 'next/navigation';
import { LogoutButton } from '@/components/dashboard/logout-button';

export const metadata: Metadata = {
  title: 'Dashboard | Business Agent',
};

export default async function DashboardPage() {
  const user = await getCurrentUser();
  if (!user) redirect('/login');

  return (
    <div className="flex flex-1 flex-col items-center justify-center px-6 text-center">
      {/* User greeting */}
      <div className="mb-2 text-sm text-muted">Welcome back,</div>
      <h1 className="text-3xl font-bold">{user.firstName} {user.lastName}</h1>
      <p className="mt-1 text-sm text-muted">{user.email}</p>

      {/* Coming soon card */}
      <div className="mt-10 max-w-md rounded-2xl border border-white/10 bg-white/5 p-8 backdrop-blur-xl">
        <div className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-gradient-to-r from-blue-500/20 to-purple-500/20">
          {/* Rocket SVG icon */}
          <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" className="text-blue-400">
            <path d="M4.5 16.5c-1.5 1.26-2 5-2 5s3.74-.5 5-2c.71-.84.7-2.13-.09-2.91a2.18 2.18 0 0 0-2.91-.09z"/>
            <path d="m12 15-3-3a22 22 0 0 1 2-3.95A12.88 12.88 0 0 1 22 2c0 2.72-.78 7.5-6 11a22.35 22.35 0 0 1-4 2z"/>
            <path d="M9 12H4s.55-3.03 2-4c1.62-1.08 5 0 5 0"/>
            <path d="M12 15v5s3.03-.55 4-2c1.08-1.62 0-5 0-5"/>
          </svg>
        </div>
        <h2 className="text-xl font-semibold">Dashboard Coming Soon</h2>
        <p className="mt-3 text-sm text-muted leading-relaxed">
          We&apos;re building your command center. Soon you&apos;ll be able to manage your WhatsApp bot,
          view analytics, and configure AI responses — all from right here.
        </p>

        {/* What's coming list */}
        <div className="mt-6 space-y-3 text-left text-sm">
          <div className="flex items-center gap-3">
            <div className="h-1.5 w-1.5 rounded-full bg-blue-400" />
            <span className="text-muted">WhatsApp bot configuration</span>
          </div>
          <div className="flex items-center gap-3">
            <div className="h-1.5 w-1.5 rounded-full bg-purple-400" />
            <span className="text-muted">Conversation analytics</span>
          </div>
          <div className="flex items-center gap-3">
            <div className="h-1.5 w-1.5 rounded-full bg-blue-400" />
            <span className="text-muted">AI training & customization</span>
          </div>
          <div className="flex items-center gap-3">
            <div className="h-1.5 w-1.5 rounded-full bg-purple-400" />
            <span className="text-muted">API key management</span>
          </div>
        </div>

        {/* Status indicator */}
        <div className="mt-6 flex items-center justify-center gap-2 text-xs text-muted">
          <span className="relative flex h-2 w-2">
            <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-emerald-400 opacity-75" />
            <span className="relative inline-flex h-2 w-2 rounded-full bg-emerald-500" />
          </span>
          In Development
        </div>
      </div>

      {/* Logout */}
      <div className="mt-8">
        <LogoutButton />
      </div>
    </div>
  );
}

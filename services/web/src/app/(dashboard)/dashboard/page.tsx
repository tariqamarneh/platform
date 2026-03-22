import type { Metadata } from 'next';
import { getCurrentUser } from '@/lib/auth/actions';
import { redirect } from 'next/navigation';
import { LogoutButton } from '@/components/dashboard/logout-button';

export const metadata: Metadata = {
  title: 'Dashboard | Business Agent',
};

const features = [
  {
    label: 'WhatsApp bot configuration',
    icon: 'M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z',
    color: 'text-blue-400',
  },
  {
    label: 'Conversation analytics',
    icon: 'M18 20V10M12 20V4M6 20v-6',
    color: 'text-purple-400',
  },
  {
    label: 'AI training & customization',
    icon: 'M12 2a7 7 0 0 0-7 7c0 2.4 1.2 4.5 3 5.7V17a2 2 0 0 0 2 2h4a2 2 0 0 0 2-2v-2.3c1.8-1.2 3-3.3 3-5.7a7 7 0 0 0-7-7z',
    color: 'text-cyan-400',
  },
  {
    label: 'API key management',
    icon: 'M21 2l-2 2m-7.61 7.61a5.5 5.5 0 1 1-7.778 7.778 5.5 5.5 0 0 1 7.777-7.777zm0 0L15.5 7.5m0 0l3 3L22 7l-3-3m-3.5 3.5L19 4',
    color: 'text-rose-400',
  },
];

export default async function DashboardPage() {
  const user = await getCurrentUser();
  if (!user) redirect('/login');

  return (
    <div className="relative flex flex-1 flex-col items-center justify-center px-6 text-center">
      {/* Muted gradient mesh background */}
      <div className="pointer-events-none absolute inset-0 overflow-hidden" aria-hidden="true">
        <div
          className="absolute left-1/4 top-1/4 h-96 w-96 rounded-full bg-blue-500/5 blur-3xl"
          style={{ animation: 'mesh-move 15s ease-in-out infinite' }}
        />
        <div
          className="absolute right-1/4 bottom-1/4 h-96 w-96 rounded-full bg-purple-500/5 blur-3xl"
          style={{ animation: 'mesh-move-reverse 12s ease-in-out infinite' }}
        />
        <div
          className="absolute left-1/2 top-1/2 h-64 w-64 -translate-x-1/2 -translate-y-1/2 rounded-full bg-cyan-500/3 blur-3xl"
          style={{ animation: 'mesh-move-slow 18s ease-in-out infinite' }}
        />
      </div>

      {/* User greeting */}
      <div className="relative z-10">
        <div className="mb-2 text-sm text-muted">Welcome back,</div>
        <h1 className="text-3xl font-bold">{user.firstName} {user.lastName}</h1>
        <p className="mt-1 text-sm text-muted">{user.email}</p>
      </div>

      {/* Coming soon card with animated gradient border */}
      <div className="gradient-border relative z-10 mt-10 max-w-md">
        <div className="rounded-2xl bg-white/5 p-8 backdrop-blur-xl">
          {/* Progress bar */}
          <div className="mb-6">
            <div className="mb-1.5 flex items-center justify-between text-xs">
              <span className="text-muted">Building...</span>
              <span className="font-medium text-blue-400">30%</span>
            </div>
            <div className="h-1.5 w-full overflow-hidden rounded-full bg-white/10">
              <div
                className="h-full rounded-full bg-gradient-to-r from-blue-500 to-purple-500"
                style={{ width: '30%' }}
              />
            </div>
          </div>

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

          {/* What's coming list with icons */}
          <div className="mt-6 space-y-3 text-left text-sm">
            {features.map((feature) => (
              <div key={feature.label} className="flex items-center gap-3">
                <div className="flex h-7 w-7 shrink-0 items-center justify-center rounded-lg bg-white/5">
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="14"
                    height="14"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="1.5"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    className={feature.color}
                  >
                    <path d={feature.icon} />
                  </svg>
                </div>
                <span className="text-muted">{feature.label}</span>
              </div>
            ))}
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
      </div>

      {/* Logout */}
      <div className="relative z-10 mt-8">
        <LogoutButton />
      </div>
    </div>
  );
}

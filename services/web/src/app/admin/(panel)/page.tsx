import type { Metadata } from 'next';
import { adminFetch } from '@/lib/auth/admin-actions';

export const metadata: Metadata = {
  title: 'Admin Overview | Business Agent',
};

interface Stats {
  totalBusinesses: number;
  activeBusinesses: number;
  suspendedBusinesses: number;
  totalUsers: number;
  freeBusinesses: number;
  paidBusinesses: number;
}

export default async function AdminOverviewPage() {
  let stats: Stats | null = null;
  let error: string | null = null;

  try {
    const response = await adminFetch('/api/v1/admin/stats');
    if (response.ok) {
      stats = await response.json();
    } else {
      error = 'Failed to load stats';
    }
  } catch {
    error = 'Unable to connect to the server';
  }

  const cards = stats
    ? [
        { label: 'Total Businesses', value: stats.totalBusinesses, color: 'blue' },
        { label: 'Active', value: stats.activeBusinesses, color: 'emerald' },
        { label: 'Suspended', value: stats.suspendedBusinesses, color: 'red' },
        { label: 'Total Users', value: stats.totalUsers, color: 'purple' },
        { label: 'Free Plan', value: stats.freeBusinesses, color: 'gray' },
        { label: 'Paid Plan', value: stats.paidBusinesses, color: 'amber' },
      ]
    : [];

  return (
    <div>
      <h1 className="text-2xl font-bold">Overview</h1>
      <p className="mt-1 text-sm text-muted">Platform statistics</p>

      {error ? (
        <div className="mt-6 rounded-xl border border-red-500/20 bg-red-500/10 p-6 text-center text-sm text-red-400">
          {error}
        </div>
      ) : stats && (
        <div className="mt-6 grid grid-cols-2 gap-4 sm:grid-cols-3">
          {cards.map(card => (
            <div key={card.label} className="rounded-xl border border-white/10 bg-white/5 p-5">
              <div className="text-sm text-muted">{card.label}</div>
              <div className="mt-2 text-3xl font-bold">{card.value}</div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

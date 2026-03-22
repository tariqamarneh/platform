import type { Metadata } from 'next';
import { adminFetch } from '@/lib/auth/admin-actions';
import Link from 'next/link';
import { BusinessActions } from '@/components/admin/business-actions';
import { notFound } from 'next/navigation';

export const metadata: Metadata = {
  title: 'Business Detail | Admin',
};

interface Business {
  id: string;
  name: string;
  plan: string;
  status: string;
  userCount: number;
  createdAt: string;
  updatedAt: string;
}

interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
  createdAt: string;
}

interface PageResponse {
  content: User[];
  totalElements: number;
}

export default async function BusinessDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;

  let business: Business;
  try {
    const bizResponse = await adminFetch(`/api/v1/admin/businesses/${id}`);
    if (!bizResponse.ok) {
      if (bizResponse.status === 404) notFound();
      throw new Error('Failed to load business');
    }
    business = await bizResponse.json();
  } catch {
    notFound();
  }

  const usersResponse = await adminFetch(`/api/v1/admin/businesses/${id}/users?page=0&size=50`);
  const users: PageResponse = usersResponse.ok
    ? await usersResponse.json()
    : { content: [], totalElements: 0 };

  return (
    <div>
      <Link href="/admin/businesses" className="text-sm text-muted hover:text-foreground transition-colors">
        &larr; Back to businesses
      </Link>

      <div className="mt-4 flex items-start justify-between">
        <div>
          <h1 className="text-2xl font-bold">{business.name}</h1>
          <div className="mt-2 flex items-center gap-3">
            <span className={`inline-flex rounded-full px-2.5 py-0.5 text-xs font-medium ${
              business.plan === 'PAID' ? 'bg-amber-500/10 text-amber-400' : 'bg-white/5 text-muted'
            }`}>
              {business.plan}
            </span>
            <span className={`inline-flex items-center gap-1.5 text-xs ${
              business.status === 'ACTIVE' ? 'text-emerald-400' : 'text-red-400'
            }`}>
              <span className={`h-1.5 w-1.5 rounded-full ${
                business.status === 'ACTIVE' ? 'bg-emerald-400' : 'bg-red-400'
              }`} />
              {business.status}
            </span>
          </div>
        </div>
        <BusinessActions businessId={id} currentPlan={business.plan} currentStatus={business.status} />
      </div>

      {/* Info cards */}
      <div className="mt-6 grid grid-cols-3 gap-4">
        <div className="rounded-xl border border-white/10 bg-white/5 p-4">
          <div className="text-xs text-muted">Users</div>
          <div className="mt-1 text-2xl font-bold">{business.userCount}</div>
        </div>
        <div className="rounded-xl border border-white/10 bg-white/5 p-4">
          <div className="text-xs text-muted">Created</div>
          <div className="mt-1 text-sm font-medium">{new Date(business.createdAt).toLocaleDateString()}</div>
        </div>
        <div className="rounded-xl border border-white/10 bg-white/5 p-4">
          <div className="text-xs text-muted">Last Updated</div>
          <div className="mt-1 text-sm font-medium">{new Date(business.updatedAt).toLocaleDateString()}</div>
        </div>
      </div>

      {/* Users table */}
      <h2 className="mt-8 text-lg font-semibold">Users ({users.totalElements})</h2>
      <div className="mt-3 overflow-hidden rounded-xl border border-white/10">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-white/10 bg-white/5">
              <th className="px-4 py-3 text-left font-medium text-muted">Name</th>
              <th className="px-4 py-3 text-left font-medium text-muted">Email</th>
              <th className="px-4 py-3 text-left font-medium text-muted">Role</th>
              <th className="px-4 py-3 text-left font-medium text-muted">Joined</th>
            </tr>
          </thead>
          <tbody>
            {users.content.map(user => (
              <tr key={user.id} className="border-b border-white/5">
                <td className="px-4 py-3 font-medium">{user.firstName} {user.lastName}</td>
                <td className="px-4 py-3 text-muted">{user.email}</td>
                <td className="px-4 py-3">
                  <span className="inline-flex rounded-full bg-white/5 px-2 py-0.5 text-xs font-medium text-muted">
                    {user.role}
                  </span>
                </td>
                <td className="px-4 py-3 text-muted">{new Date(user.createdAt).toLocaleDateString()}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

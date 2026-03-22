import type { Metadata } from 'next';
import { adminFetch } from '@/lib/auth/admin-actions';
import Link from 'next/link';

export const metadata: Metadata = {
  title: 'Businesses | Admin',
};

interface Business {
  id: string;
  name: string;
  plan: string;
  status: string;
  userCount: number;
  createdAt: string;
}

interface PageResponse {
  content: Business[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export default async function BusinessesPage({ searchParams }: { searchParams: Promise<{ page?: string }> }) {
  const params = await searchParams;
  const page = parseInt(params.page || '0');

  const response = await adminFetch(`/api/v1/admin/businesses?page=${page}&size=20&sortBy=createdAt&direction=desc`);
  const data: PageResponse = await response.json();

  return (
    <div>
      <h1 className="text-2xl font-bold">Businesses</h1>
      <p className="mt-1 text-sm text-muted">{data.totalElements} total</p>

      <div className="mt-6 overflow-hidden rounded-xl border border-white/10">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-white/10 bg-white/5">
              <th className="px-4 py-3 text-left font-medium text-muted">Name</th>
              <th className="px-4 py-3 text-left font-medium text-muted">Plan</th>
              <th className="px-4 py-3 text-left font-medium text-muted">Status</th>
              <th className="px-4 py-3 text-left font-medium text-muted">Users</th>
              <th className="px-4 py-3 text-left font-medium text-muted">Created</th>
            </tr>
          </thead>
          <tbody>
            {data.content.map(biz => (
              <tr key={biz.id} className="border-b border-white/5 hover:bg-white/[0.02] transition-colors">
                <td className="px-4 py-3">
                  <Link href={`/admin/businesses/${biz.id}`} className="font-medium hover:text-blue-400 transition-colors">
                    {biz.name}
                  </Link>
                </td>
                <td className="px-4 py-3">
                  <span className={`inline-flex rounded-full px-2 py-0.5 text-xs font-medium ${
                    biz.plan === 'PAID' ? 'bg-amber-500/10 text-amber-400' : 'bg-white/5 text-muted'
                  }`}>
                    {biz.plan}
                  </span>
                </td>
                <td className="px-4 py-3">
                  <span className={`inline-flex items-center gap-1.5 text-xs ${
                    biz.status === 'ACTIVE' ? 'text-emerald-400' : 'text-red-400'
                  }`}>
                    <span className={`h-1.5 w-1.5 rounded-full ${
                      biz.status === 'ACTIVE' ? 'bg-emerald-400' : 'bg-red-400'
                    }`} />
                    {biz.status}
                  </span>
                </td>
                <td className="px-4 py-3 text-muted">{biz.userCount}</td>
                <td className="px-4 py-3 text-muted">{new Date(biz.createdAt).toLocaleDateString()}</td>
              </tr>
            ))}
            {data.content.length === 0 && (
              <tr><td colSpan={5} className="px-4 py-8 text-center text-muted">No businesses yet</td></tr>
            )}
          </tbody>
        </table>
      </div>

      {/* Pagination */}
      {data.totalPages > 1 && (
        <div className="mt-4 flex items-center justify-between text-sm">
          <span className="text-muted">Page {data.number + 1} of {data.totalPages}</span>
          <div className="flex gap-2">
            {data.number > 0 && (
              <Link href={`/admin/businesses?page=${data.number - 1}`}
                className="rounded-lg border border-white/10 px-3 py-1.5 text-sm hover:bg-white/5 transition-colors">
                Previous
              </Link>
            )}
            {data.number < data.totalPages - 1 && (
              <Link href={`/admin/businesses?page=${data.number + 1}`}
                className="rounded-lg border border-white/10 px-3 py-1.5 text-sm hover:bg-white/5 transition-colors">
                Next
              </Link>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

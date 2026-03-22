import { getAdminToken } from '@/lib/auth/admin-actions';
import { redirect } from 'next/navigation';
import { AdminSidebar } from '@/components/admin/admin-sidebar';

export default async function AdminPanelLayout({ children }: { children: React.ReactNode }) {
  const token = await getAdminToken();
  if (!token) redirect('/admin/login');

  return (
    <div className="flex min-h-screen bg-background text-foreground">
      <AdminSidebar />
      <main className="flex-1 overflow-auto">
        <div className="mx-auto max-w-6xl px-6 py-8">
          {children}
        </div>
      </main>
    </div>
  );
}

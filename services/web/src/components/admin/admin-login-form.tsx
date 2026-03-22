'use client';

import { useActionState } from 'react';
import { useRouter } from 'next/navigation';
import { useEffect } from 'react';
import { adminLogin } from '@/lib/auth/admin-actions';
import type { ActionResult } from '@/lib/auth/types';

export function AdminLoginForm() {
  const router = useRouter();

  async function handleLogin(_prevState: ActionResult, formData: FormData): Promise<ActionResult> {
    const email = formData.get('email') as string;
    const password = formData.get('password') as string;
    if (!email || !password) return { success: false, error: 'All fields required' };
    return adminLogin({ email, password });
  }

  const [state, formAction, pending] = useActionState(handleLogin, { success: false } as ActionResult);

  useEffect(() => {
    if (state.success) router.push('/admin');
  }, [state.success, router]);

  return (
    <form action={formAction} className="space-y-4">
      {state.error && (
        <div className="rounded-lg border border-red-500/20 bg-red-500/10 px-4 py-3 text-sm text-red-400">
          {state.error}
        </div>
      )}
      <div>
        <label htmlFor="email" className="block text-sm font-medium text-muted mb-1.5">Email</label>
        <input id="email" name="email" type="email" required
          className="w-full rounded-lg border border-white/10 bg-white/5 px-4 py-2.5 text-sm text-foreground placeholder-gray-500 outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500" />
      </div>
      <div>
        <label htmlFor="password" className="block text-sm font-medium text-muted mb-1.5">Password</label>
        <input id="password" name="password" type="password" required
          className="w-full rounded-lg border border-white/10 bg-white/5 px-4 py-2.5 text-sm text-foreground placeholder-gray-500 outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500" />
      </div>
      <button type="submit" disabled={pending}
        className="w-full rounded-lg bg-foreground py-2.5 text-sm font-medium text-background transition-opacity hover:opacity-90 disabled:opacity-50">
        {pending ? 'Signing in...' : 'Sign in'}
      </button>
    </form>
  );
}

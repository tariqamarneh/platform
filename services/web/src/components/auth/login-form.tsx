'use client';

import { useActionState, useEffect } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import Link from 'next/link';
import { login } from '@/lib/auth/actions';
import type { ActionResult } from '@/lib/auth/types';

export function LoginForm() {
  const router = useRouter();
  const searchParams = useSearchParams();

  async function loginAction(
    _prevState: ActionResult,
    formData: FormData,
  ): Promise<ActionResult> {
    const email = formData.get('email') as string;
    const password = formData.get('password') as string;

    if (!email || !password) {
      return { success: false, error: 'Email and password are required' };
    }

    const result = await login({ email, password });
    return result;
  }

  const [state, formAction, pending] = useActionState(loginAction, {
    success: false,
  } as ActionResult);

  useEffect(() => {
    if (state.success) {
      const callbackUrl = searchParams.get('callbackUrl') || '/dashboard';
      router.push(callbackUrl);
    }
  }, [state.success, router, searchParams]);

  return (
    <form action={formAction} className="space-y-5">
      {/* Error message with fade-in */}
      <div
        role="alert"
        className={`grid transition-all duration-300 ease-out ${
          state.error
            ? 'grid-rows-[1fr] opacity-100'
            : 'grid-rows-[0fr] opacity-0'
        }`}
      >
        <div className="overflow-hidden">
          <div className="rounded-lg border border-red-500/20 bg-red-500/10 px-4 py-3 text-sm text-red-400">
            {state.error}
          </div>
        </div>
      </div>

      {/* Email */}
      <div>
        <label htmlFor="email" className="mb-1.5 block text-sm font-medium">
          Email address
        </label>
        <input
          id="email"
          name="email"
          type="email"
          autoComplete="email"
          required
          placeholder="you@company.com"
          className="w-full rounded-lg border border-white/10 bg-white/5 px-4 py-3 text-white placeholder-gray-500 outline-none transition-all focus:border-blue-500 focus:ring-1 focus:ring-blue-500 focus:border-l-blue-500 focus:[border-left-width:2px]"
        />
      </div>

      {/* Password */}
      <div>
        <label
          htmlFor="password"
          className="mb-1.5 block text-sm font-medium"
        >
          Password
        </label>
        <input
          id="password"
          name="password"
          type="password"
          autoComplete="current-password"
          required
          placeholder="Enter your password"
          className="w-full rounded-lg border border-white/10 bg-white/5 px-4 py-3 text-white placeholder-gray-500 outline-none transition-all focus:border-blue-500 focus:ring-1 focus:ring-blue-500 focus:border-l-blue-500 focus:[border-left-width:2px]"
        />
      </div>

      {/* Submit */}
      <button
        type="submit"
        disabled={pending}
        className="relative w-full rounded-lg bg-gradient-to-r from-blue-500 to-purple-600 py-3 font-medium text-white transition-all hover:from-blue-400 hover:to-purple-500 hover:shadow-lg hover:shadow-blue-500/20 disabled:opacity-50 disabled:hover:shadow-none"
      >
        <span className={`inline-flex items-center gap-2 transition-opacity ${pending ? 'opacity-100' : ''}`}>
          {pending && (
            <svg className="h-4 w-4 animate-spin" viewBox="0 0 24 24" fill="none">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="3" />
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
            </svg>
          )}
          {pending ? 'Signing in...' : 'Sign in'}
        </span>
      </button>

      {/* Divider */}
      <div className="flex items-center gap-4">
        <div className="h-px flex-1 bg-white/10" />
        <span className="text-xs text-muted">or</span>
        <div className="h-px flex-1 bg-white/10" />
      </div>

      {/* Register link */}
      <p className="text-center text-sm text-muted">
        Don&apos;t have an account?{' '}
        <Link
          href="/register"
          className="font-medium text-blue-400 transition-colors hover:text-blue-300"
        >
          Sign up
        </Link>
      </p>
    </form>
  );
}

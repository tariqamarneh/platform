'use client';

import { useActionState, useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { register } from '@/lib/auth/actions';
import type { ActionResult } from '@/lib/auth/types';

function getPasswordStrength(password: string) {
  const checks = {
    length: password.length >= 8,
    number: /\d/.test(password),
    uppercase: /[A-Z]/.test(password),
  };
  const score = Object.values(checks).filter(Boolean).length;
  return { checks, score };
}

export function RegisterForm() {
  const router = useRouter();
  const [password, setPassword] = useState('');

  async function registerAction(
    _prevState: ActionResult,
    formData: FormData,
  ): Promise<ActionResult> {
    const businessName = formData.get('businessName') as string;
    const firstName = formData.get('firstName') as string;
    const lastName = formData.get('lastName') as string;
    const email = formData.get('email') as string;
    const pw = formData.get('password') as string;
    const terms = formData.get('terms');

    if (!businessName || !firstName || !lastName || !email || !pw) {
      return { success: false, error: 'All fields are required' };
    }

    if (!terms) {
      return {
        success: false,
        error: 'You must agree to the terms and conditions',
      };
    }

    if (pw.length < 8) {
      return {
        success: false,
        error: 'Password must be at least 8 characters',
      };
    }

    const result = await register({
      businessName,
      firstName,
      lastName,
      email,
      password: pw,
    });
    return result;
  }

  const [state, formAction, pending] = useActionState(registerAction, {
    success: false,
  } as ActionResult);

  useEffect(() => {
    if (state.success) {
      router.push('/dashboard');
    }
  }, [state.success, router]);

  const strength = getPasswordStrength(password);

  return (
    <form action={formAction} className="space-y-5">
      {/* Error message */}
      {state.error && (
        <div
          role="alert"
          className="rounded-lg border border-red-500/20 bg-red-500/10 px-4 py-3 text-sm text-red-400"
        >
          {state.error}
        </div>
      )}

      {/* Business name */}
      <div>
        <label
          htmlFor="businessName"
          className="mb-1.5 block text-sm font-medium"
        >
          Business name
        </label>
        <input
          id="businessName"
          name="businessName"
          type="text"
          required
          placeholder="Acme Inc."
          className="w-full rounded-lg border border-white/10 bg-white/5 px-4 py-3 text-white placeholder-gray-500 outline-none transition-colors focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
        />
      </div>

      {/* First name + Last name */}
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label
            htmlFor="firstName"
            className="mb-1.5 block text-sm font-medium"
          >
            First name
          </label>
          <input
            id="firstName"
            name="firstName"
            type="text"
            autoComplete="given-name"
            required
            placeholder="Jane"
            className="w-full rounded-lg border border-white/10 bg-white/5 px-4 py-3 text-white placeholder-gray-500 outline-none transition-colors focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
          />
        </div>
        <div>
          <label
            htmlFor="lastName"
            className="mb-1.5 block text-sm font-medium"
          >
            Last name
          </label>
          <input
            id="lastName"
            name="lastName"
            type="text"
            autoComplete="family-name"
            required
            placeholder="Doe"
            className="w-full rounded-lg border border-white/10 bg-white/5 px-4 py-3 text-white placeholder-gray-500 outline-none transition-colors focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
          />
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
          className="w-full rounded-lg border border-white/10 bg-white/5 px-4 py-3 text-white placeholder-gray-500 outline-none transition-colors focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
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
          autoComplete="new-password"
          required
          placeholder="Create a password"
          onChange={(e) => setPassword(e.target.value)}
          className="w-full rounded-lg border border-white/10 bg-white/5 px-4 py-3 text-white placeholder-gray-500 outline-none transition-colors focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
        />

        {/* Strength indicator */}
        {password.length > 0 && (
          <div className="mt-2 space-y-2">
            <div className="flex gap-1">
              {[1, 2, 3].map((level) => (
                <div
                  key={level}
                  className={`h-1 flex-1 rounded-full transition-colors ${
                    strength.score >= level
                      ? strength.score === 3
                        ? 'bg-green-500'
                        : strength.score === 2
                          ? 'bg-yellow-500'
                          : 'bg-red-500'
                      : 'bg-white/10'
                  }`}
                />
              ))}
            </div>
            <ul className="space-y-1 text-xs text-muted">
              <li
                className={
                  strength.checks.length ? 'text-green-400' : ''
                }
              >
                {strength.checks.length ? '\u2713' : '\u2022'} At least 8
                characters
              </li>
              <li
                className={
                  strength.checks.number ? 'text-green-400' : ''
                }
              >
                {strength.checks.number ? '\u2713' : '\u2022'} Contains a
                number
              </li>
              <li
                className={
                  strength.checks.uppercase ? 'text-green-400' : ''
                }
              >
                {strength.checks.uppercase ? '\u2713' : '\u2022'} Contains
                an uppercase letter
              </li>
            </ul>
          </div>
        )}
      </div>

      {/* Terms */}
      <div className="flex items-start gap-3">
        <input
          id="terms"
          name="terms"
          type="checkbox"
          required
          className="mt-1 h-4 w-4 rounded border-white/10 bg-white/5 text-blue-500 focus:ring-blue-500"
        />
        <label htmlFor="terms" className="text-sm text-muted">
          I agree to the{' '}
          <span className="text-blue-400 hover:text-blue-300 cursor-pointer">
            Terms of Service
          </span>{' '}
          and{' '}
          <span className="text-blue-400 hover:text-blue-300 cursor-pointer">
            Privacy Policy
          </span>
        </label>
      </div>

      {/* Submit */}
      <button
        type="submit"
        disabled={pending}
        className="w-full rounded-lg bg-gradient-to-r from-blue-500 to-purple-600 py-3 font-medium text-white transition-opacity disabled:opacity-50"
      >
        {pending ? 'Creating account...' : 'Create account'}
      </button>

      {/* Login link */}
      <p className="text-center text-sm text-muted">
        Already have an account?{' '}
        <Link
          href="/login"
          className="font-medium text-blue-400 transition-colors hover:text-blue-300"
        >
          Sign in
        </Link>
      </p>
    </form>
  );
}

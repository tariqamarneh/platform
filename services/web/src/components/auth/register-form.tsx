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

const strengthLabels = ['', 'Weak', 'Medium', 'Strong'] as const;
const strengthColors = ['', 'bg-red-500', 'bg-yellow-500', 'bg-green-500'] as const;

export function RegisterForm() {
  const router = useRouter();
  const [password, setPassword] = useState('');
  const [termsChecked, setTermsChecked] = useState(false);

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
  const inputClass =
    'w-full rounded-lg border border-white/10 bg-white/5 px-4 py-3 text-white placeholder-gray-500 outline-none transition-all focus:border-blue-500 focus:ring-1 focus:ring-blue-500 focus:border-l-blue-500 focus:[border-left-width:2px]';

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

      {/* ---- Business Info Section ---- */}
      <div className="space-y-4">
        <div className="flex items-center gap-2">
          <div className="h-px w-4 bg-gradient-to-r from-blue-500/50 to-transparent" />
          <span className="text-[11px] font-semibold uppercase tracking-widest text-muted">
            Business Info
          </span>
          <div className="h-px flex-1 bg-white/5" />
        </div>

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
            className={inputClass}
          />
        </div>

        {/* First name + Last name */}
        <div className="grid grid-cols-2 gap-4">
          <div className="flex-1">
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
              className={inputClass}
            />
          </div>
          <div className="flex-1">
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
              className={inputClass}
            />
          </div>
        </div>
      </div>

      {/* ---- Account Info Section ---- */}
      <div className="space-y-4">
        <div className="flex items-center gap-2">
          <div className="h-px w-4 bg-gradient-to-r from-purple-500/50 to-transparent" />
          <span className="text-[11px] font-semibold uppercase tracking-widest text-muted">
            Account Info
          </span>
          <div className="h-px flex-1 bg-white/5" />
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
            className={inputClass}
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
            className={inputClass}
          />

          {/* Segmented strength indicator */}
          {password.length > 0 && (
            <div className="mt-3 space-y-2">
              <div className="flex items-center gap-2">
                <div className="flex flex-1 gap-1">
                  {[1, 2, 3].map((level) => (
                    <div
                      key={level}
                      className={`h-1.5 flex-1 rounded-full transition-all duration-300 ${
                        strength.score >= level
                          ? strengthColors[strength.score]
                          : 'bg-white/10'
                      }`}
                    />
                  ))}
                </div>
                <span
                  className={`text-[11px] font-medium transition-colors ${
                    strength.score === 3
                      ? 'text-green-400'
                      : strength.score === 2
                        ? 'text-yellow-400'
                        : 'text-red-400'
                  }`}
                >
                  {strengthLabels[strength.score]}
                </span>
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
      </div>

      {/* Terms — custom styled checkbox */}
      <div className="flex items-start gap-3">
        <button
          type="button"
          role="checkbox"
          aria-checked={termsChecked}
          onClick={() => setTermsChecked(!termsChecked)}
          className={`mt-0.5 flex h-[18px] w-[18px] shrink-0 items-center justify-center rounded border transition-all ${
            termsChecked
              ? 'border-blue-500 bg-blue-500'
              : 'border-white/20 bg-white/5 hover:border-white/30'
          }`}
        >
          {termsChecked && (
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="12"
              height="12"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="3"
              strokeLinecap="round"
              strokeLinejoin="round"
              className="text-white"
            >
              <polyline points="20 6 9 17 4 12" />
            </svg>
          )}
        </button>
        {/* Hidden native checkbox for form submission */}
        <input
          type="checkbox"
          name="terms"
          value="on"
          checked={termsChecked}
          onChange={() => setTermsChecked(!termsChecked)}
          className="sr-only"
          tabIndex={-1}
          aria-hidden="true"
        />
        <label
          onClick={() => setTermsChecked(!termsChecked)}
          className="cursor-pointer text-sm text-muted"
        >
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
        className="relative w-full rounded-lg bg-gradient-to-r from-blue-500 to-purple-600 py-3 font-medium text-white transition-all hover:from-blue-400 hover:to-purple-500 hover:shadow-lg hover:shadow-blue-500/20 disabled:opacity-50 disabled:hover:shadow-none"
      >
        <span className="inline-flex items-center gap-2">
          {pending && (
            <svg className="h-4 w-4 animate-spin" viewBox="0 0 24 24" fill="none">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="3" />
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
            </svg>
          )}
          {pending ? 'Creating account...' : 'Create account'}
        </span>
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

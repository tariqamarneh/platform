import type { Metadata } from 'next';
import { Suspense } from 'react';
import { AuthLayout } from '@/components/auth/auth-layout';
import { LoginForm } from '@/components/auth/login-form';

export const metadata: Metadata = {
  title: 'Login | Business Agent',
  description: 'Log in to your Business Agent account',
};

export default function LoginPage() {
  return (
    <AuthLayout
      title="Welcome back"
      subtitle="Log in to manage your AI chatbot"
    >
      <Suspense>
        <LoginForm />
      </Suspense>
    </AuthLayout>
  );
}

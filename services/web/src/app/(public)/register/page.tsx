import type { Metadata } from 'next';
import { AuthLayout } from '@/components/auth/auth-layout';
import { RegisterForm } from '@/components/auth/register-form';

export const metadata: Metadata = {
  title: 'Register | Business Agent',
  description: 'Create your Business Agent account',
};

export default function RegisterPage() {
  return (
    <AuthLayout
      title="Create your account"
      subtitle="Start automating your customer service today"
    >
      <RegisterForm />
    </AuthLayout>
  );
}

'use server';

import { cookies } from 'next/headers';
import { redirect } from 'next/navigation';
import { authServiceFetch } from '@/lib/api/client';
import type { LoginData, ActionResult } from './types';

const ADMIN_TOKEN_COOKIE = 'admin_access_token';

export async function adminLogin(data: LoginData): Promise<ActionResult> {
  try {
    const response = await authServiceFetch('/api/v1/admin/login', {
      method: 'POST',
      body: JSON.stringify(data),
    });

    if (!response.ok) {
      const error = await response.json();
      return { success: false, error: error.message || 'Invalid credentials' };
    }

    const result = await response.json();
    const cookieStore = await cookies();
    cookieStore.set(ADMIN_TOKEN_COOKIE, result.accessToken, {
      httpOnly: true,
      secure: process.env.NODE_ENV === 'production',
      sameSite: 'lax',
      path: '/',
      maxAge: result.expiresIn,
    });

    return { success: true };
  } catch {
    return { success: false, error: 'Unable to connect to the server' };
  }
}

export async function adminLogout(): Promise<void> {
  const cookieStore = await cookies();
  cookieStore.delete(ADMIN_TOKEN_COOKIE);
  redirect('/admin/login');
}

export async function getAdminToken(): Promise<string | undefined> {
  const cookieStore = await cookies();
  return cookieStore.get(ADMIN_TOKEN_COOKIE)?.value;
}

export async function adminFetch(path: string, options: RequestInit = {}): Promise<Response> {
  const token = await getAdminToken();
  if (!token) redirect('/admin/login');

  return authServiceFetch(path, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
      ...options.headers,
    },
  });
}

export async function updateBusiness(
  businessId: string,
  data: { plan?: string; status?: string }
): Promise<{ success: boolean; error?: string }> {
  try {
    const token = await getAdminToken();
    if (!token) return { success: false, error: 'Not authenticated' };

    const response = await authServiceFetch(`/api/v1/admin/businesses/${businessId}`, {
      method: 'PATCH',
      cache: 'no-store',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(data),
    });

    if (!response.ok) {
      return { success: false, error: 'Failed to update business' };
    }
    return { success: true };
  } catch {
    return { success: false, error: 'Unable to connect to the server' };
  }
}

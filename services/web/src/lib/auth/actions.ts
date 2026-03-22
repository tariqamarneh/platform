'use server';

import { cache } from 'react';
import { redirect } from 'next/navigation';
import { authServiceFetch } from '@/lib/api/client';
import { setAuthCookies, getAccessToken, getRefreshToken, clearAuthCookies } from './cookies';
import type { LoginData, RegisterData, ActionResult, User } from './types';

export async function login(data: LoginData): Promise<ActionResult> {
  try {
    const response = await authServiceFetch('/api/v1/auth/login', {
      method: 'POST',
      body: JSON.stringify(data),
    });

    if (!response.ok) {
      let message = 'Invalid email or password';
      try {
        const error = await response.json();
        message = error.message || message;
      } catch {
        // Response wasn't JSON
      }
      return { success: false, error: message };
    }

    const tokens = await response.json();
    await setAuthCookies(tokens.accessToken, tokens.refreshToken, tokens.expiresIn);
    return { success: true };
  } catch {
    return { success: false, error: 'Unable to connect to the server' };
  }
}

export async function register(data: RegisterData): Promise<ActionResult> {
  try {
    const response = await authServiceFetch('/api/v1/auth/register', {
      method: 'POST',
      body: JSON.stringify(data),
    });

    if (!response.ok) {
      let message = 'Registration failed';
      try {
        const error = await response.json();
        message = error.message || message;
      } catch {
        // Response wasn't JSON
      }
      return { success: false, error: message };
    }

    const tokens = await response.json();
    await setAuthCookies(tokens.accessToken, tokens.refreshToken, tokens.expiresIn);
    return { success: true };
  } catch {
    return { success: false, error: 'Unable to connect to the server' };
  }
}

export async function logout(): Promise<void> {
  await clearAuthCookies();
  redirect('/login');
}

export async function refreshTokens(): Promise<boolean> {
  const refreshToken = await getRefreshToken();
  if (!refreshToken) return false;

  try {
    const response = await authServiceFetch('/api/v1/auth/refresh', {
      method: 'POST',
      body: JSON.stringify({ refreshToken }),
    });

    if (!response.ok) return false;

    const tokens = await response.json();
    await setAuthCookies(tokens.accessToken, tokens.refreshToken, tokens.expiresIn);
    return true;
  } catch {
    return false;
  }
}

export const getCurrentUser = cache(async function getCurrentUser(): Promise<User | null> {
  let accessToken = await getAccessToken();

  if (!accessToken) {
    const refreshed = await refreshTokens();
    if (!refreshed) return null;
    accessToken = await getAccessToken();
    if (!accessToken) return null;
  }

  try {
    const response = await authServiceFetch('/api/v1/users/me', {
      headers: { Authorization: `Bearer ${accessToken}` },
    });

    if (response.status === 401 || response.status === 403) {
      const refreshed = await refreshTokens();
      if (!refreshed) return null;

      const newToken = await getAccessToken();
      if (!newToken) return null;

      const retryResponse = await authServiceFetch('/api/v1/users/me', {
        headers: { Authorization: `Bearer ${newToken}` },
      });

      if (!retryResponse.ok) return null;
      return retryResponse.json();
    }

    if (!response.ok) return null;
    return response.json();
  } catch {
    return null;
  }
});

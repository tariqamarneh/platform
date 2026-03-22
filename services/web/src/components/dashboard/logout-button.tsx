'use client';

import { logout } from '@/lib/auth/actions';

export function LogoutButton() {
  return (
    <button
      onClick={() => logout()}
      className="text-sm text-muted hover:text-foreground transition-colors"
    >
      Sign out
    </button>
  );
}

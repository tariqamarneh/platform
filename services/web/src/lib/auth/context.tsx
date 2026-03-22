'use client';

import { createContext, useContext, useState, useCallback } from 'react';
import type { User, AuthState } from './types';

interface AuthContextType extends AuthState {
  setUser: (user: User | null) => void;
  clearAuth: () => void;
}

const AuthContext = createContext<AuthContextType>({
  user: null,
  isAuthenticated: false,
  setUser: () => {},
  clearAuth: () => {},
});

export function AuthProvider({ children, initialUser }: { children: React.ReactNode; initialUser: User | null }) {
  const [user, setUser] = useState<User | null>(initialUser);

  const clearAuth = useCallback(() => {
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider value={{
      user,
      isAuthenticated: !!user,
      setUser,
      clearAuth,
    }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}

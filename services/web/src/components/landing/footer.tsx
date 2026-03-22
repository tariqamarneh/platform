import Link from 'next/link';

export function Footer() {
  return (
    <footer className="border-t border-white/5 px-6 py-6">
      <div className="mx-auto flex max-w-6xl items-center justify-between">
        <p className="text-xs text-muted/60">
          &copy; 2026 Business Agent
        </p>
        <nav className="flex items-center gap-4">
          <Link
            href="/login"
            className="text-xs text-muted/60 transition-colors hover:text-muted"
          >
            Login
          </Link>
          <span className="text-muted/20">&bull;</span>
          <Link
            href="/register"
            className="text-xs text-muted/60 transition-colors hover:text-muted"
          >
            Register
          </Link>
        </nav>
      </div>
    </footer>
  );
}

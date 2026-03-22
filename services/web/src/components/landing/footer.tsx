import Link from 'next/link';

const TwitterIcon = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
    <path d="M18.244 2.25h3.308l-7.227 8.26 8.502 11.24H16.17l-5.214-6.817L4.99 21.75H1.68l7.73-8.835L1.254 2.25H8.08l4.713 6.231zm-1.161 17.52h1.833L7.084 4.126H5.117z" />
  </svg>
);

const GitHubIcon = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
    <path d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.531 1.032 1.531 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0 1 12 6.844a9.59 9.59 0 0 1 2.504.337c1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.02 10.02 0 0 0 22 12.017C22 6.484 17.522 2 12 2z" />
  </svg>
);

export function Footer() {
  return (
    <footer className="relative px-6 py-8">
      {/* Top border gradient */}
      <div
        className="pointer-events-none absolute inset-x-0 top-0 h-px"
        style={{
          background:
            'linear-gradient(90deg, transparent 0%, rgba(59,130,246,0.2) 30%, rgba(124,58,237,0.2) 70%, transparent 100%)',
        }}
      />

      <div className="mx-auto max-w-6xl">
        {/* Top row */}
        <div className="flex flex-col items-center justify-between gap-4 sm:flex-row">
          <span className="text-sm font-semibold tracking-tight">
            Business Agent
          </span>
          <nav className="flex items-center gap-5">
            <Link
              href="/login"
              className="text-xs text-muted/60 transition-colors hover:text-muted"
            >
              Login
            </Link>
            <Link
              href="/register"
              className="text-xs text-muted/60 transition-colors hover:text-muted"
            >
              Register
            </Link>
            <Link
              href="#features"
              className="text-xs text-muted/60 transition-colors hover:text-muted"
            >
              Features
            </Link>
            <Link
              href="#pricing"
              className="text-xs text-muted/60 transition-colors hover:text-muted"
            >
              Pricing
            </Link>
          </nav>
        </div>

        {/* Divider */}
        <div className="my-5 h-px bg-white/5" />

        {/* Bottom row */}
        <div className="flex flex-col items-center justify-between gap-3 sm:flex-row">
          <div className="flex items-center gap-3">
            <p className="text-xs text-muted/40">
              &copy; 2026 Business Agent
            </p>
            <span className="text-muted/20">&middot;</span>
            <p className="text-xs text-muted/40">
              Built with AI
            </p>
          </div>
          <div className="flex items-center gap-3">
            <a
              href="https://twitter.com"
              target="_blank"
              rel="noopener noreferrer"
              className="text-muted/30 transition-colors hover:text-muted/60"
              aria-label="Twitter"
            >
              <TwitterIcon />
            </a>
            <a
              href="https://github.com"
              target="_blank"
              rel="noopener noreferrer"
              className="text-muted/30 transition-colors hover:text-muted/60"
              aria-label="GitHub"
            >
              <GitHubIcon />
            </a>
          </div>
        </div>
      </div>
    </footer>
  );
}

import Link from 'next/link';

export function AuthLayout({
  children,
  title,
  subtitle,
}: {
  children: React.ReactNode;
  title: string;
  subtitle: string;
}) {
  return (
    <div className="flex min-h-screen bg-background text-foreground">
      {/* Left: Form */}
      <div className="flex flex-1 flex-col justify-center px-6 py-12 lg:px-20">
        <div className="mx-auto w-full max-w-md">
          <Link
            href="/"
            className="inline-flex items-center gap-2 text-sm text-muted transition-colors hover:text-foreground"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="16"
              height="16"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <path d="M19 12H5" />
              <path d="M12 19l-7-7 7-7" />
            </svg>
            Business Agent
          </Link>

          <h1 className="mt-8 text-3xl font-bold tracking-tight">
            {title}
          </h1>
          <p className="mt-2 text-muted">{subtitle}</p>

          <div className="mt-8">{children}</div>
        </div>
      </div>

      {/* Right: Branded visual (hidden on mobile) */}
      <div className="relative hidden items-center justify-center overflow-hidden bg-gradient-to-br from-blue-600/20 to-purple-600/20 lg:flex lg:flex-1">
        <div className="z-10 text-center">
          <h2 className="text-4xl font-bold bg-gradient-to-r from-blue-400 to-purple-400 bg-clip-text text-transparent">
            AI-Powered Support
          </h2>
          <p className="mt-4 text-lg text-gray-400">
            for Modern Businesses
          </p>
        </div>

        {/* Animated gradient orb */}
        <div
          className="absolute h-96 w-96 rounded-full bg-gradient-to-r from-blue-500/30 to-purple-500/30 blur-3xl animate-pulse"
          aria-hidden="true"
        />
      </div>
    </div>
  );
}

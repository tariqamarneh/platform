import Link from 'next/link';

const floatingCards = [
  { label: 'AI', icon: 'M12 2a4 4 0 0 0-4 4v2H6a2 2 0 0 0-2 2v10a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V10a2 2 0 0 0-2-2h-2V6a4 4 0 0 0-4-4zm0 2a2 2 0 0 1 2 2v2H10V6a2 2 0 0 1 2-2z', top: '12%', left: '10%', rotate: '-6deg', delay: '0s', duration: '6s' },
  { label: '24/7', icon: 'M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10 10-4.5 10-10S17.5 2 12 2zm0 2a8 8 0 1 1 0 16 8 8 0 0 1 0-16zm-1 3v6l5.2 3.1.8-1.4-4.5-2.7V7h-1.5z', top: '18%', right: '8%', rotate: '4deg', delay: '1s', duration: '7s' },
  { label: 'Fast', icon: 'M13 2L3 14h9l-1 8 10-12h-9l1-8z', top: '62%', left: '6%', rotate: '3deg', delay: '0.5s', duration: '5.5s' },
  { label: 'Smart', icon: 'M12 2a7 7 0 0 0-7 7c0 2.4 1.2 4.5 3 5.7V17a2 2 0 0 0 2 2h4a2 2 0 0 0 2-2v-2.3c1.8-1.2 3-3.3 3-5.7a7 7 0 0 0-7-7zm-1 17v1a1 1 0 0 0 1 1 1 1 0 0 0 1-1v-1h-2z', top: '68%', right: '12%', rotate: '-3deg', delay: '1.5s', duration: '6.5s' },
];

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
      <div className="relative flex flex-1 flex-col justify-center px-6 py-12 lg:px-20">
        {/* Subtle grid pattern background */}
        <div className="absolute inset-0 grid-pattern opacity-50" aria-hidden="true" />

        <div className="relative z-10 mx-auto w-full max-w-md">
          <Link
            href="/"
            className="group inline-flex items-center gap-2.5 text-sm text-muted transition-colors hover:text-foreground"
          >
            {/* Back arrow */}
            <span className="flex h-8 w-8 items-center justify-center rounded-lg border border-white/10 bg-white/5 transition-all group-hover:border-white/20 group-hover:bg-white/10">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                width="14"
                height="14"
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
            </span>
            {/* Logo icon */}
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="18"
              height="18"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="1.5"
              strokeLinecap="round"
              strokeLinejoin="round"
              className="text-blue-400"
            >
              <path d="M12 2a7 7 0 0 0-7 7c0 2.4 1.2 4.5 3 5.7V17a2 2 0 0 0 2 2h4a2 2 0 0 0 2-2v-2.3c1.8-1.2 3-3.3 3-5.7a7 7 0 0 0-7-7z" />
              <path d="M9 21v1a1 1 0 0 0 1 1h4a1 1 0 0 0 1-1v-1" />
            </svg>
            <span className="font-medium">Business Agent</span>
          </Link>

          <h1 className="mt-10 text-3xl font-bold tracking-tight">
            {title}
          </h1>
          {/* Decorative gradient line */}
          <div
            className="mt-3 h-[2px] w-10 rounded-full bg-gradient-to-r from-blue-500 to-purple-500"
            aria-hidden="true"
          />
          <p className="mt-4 text-muted">{subtitle}</p>

          <div className="mt-10">{children}</div>
        </div>
      </div>

      {/* Right: Branded visual (hidden on mobile) */}
      <div className="relative hidden items-center justify-center overflow-hidden lg:flex lg:flex-1">
        {/* Rich gradient background */}
        <div className="absolute inset-0 bg-gradient-to-br from-blue-600/20 via-purple-600/20 to-cyan-600/10" />
        <div className="absolute inset-0 bg-gradient-to-tl from-rose-600/10 via-transparent to-transparent" />

        {/* Abstract blob shape */}
        <div className="absolute inset-0 flex items-center justify-center" aria-hidden="true">
          <div
            className="h-[500px] w-[500px] rounded-full opacity-40 blur-3xl"
            style={{
              background: 'radial-gradient(ellipse at 30% 50%, rgba(59,130,246,0.4), transparent 60%), radial-gradient(ellipse at 70% 30%, rgba(124,58,237,0.4), transparent 60%), radial-gradient(ellipse at 50% 80%, rgba(6,182,212,0.3), transparent 60%)',
              borderRadius: '30% 70% 70% 30% / 30% 30% 70% 70%',
              animation: 'mesh-move 12s ease-in-out infinite',
            }}
          />
          {/* Secondary blob */}
          <div
            className="absolute h-[350px] w-[350px] opacity-30 blur-2xl"
            style={{
              background: 'radial-gradient(ellipse at 60% 40%, rgba(236,72,153,0.3), transparent 60%), radial-gradient(ellipse at 40% 70%, rgba(59,130,246,0.3), transparent 60%)',
              borderRadius: '60% 40% 30% 70% / 60% 30% 70% 40%',
              animation: 'mesh-move-reverse 10s ease-in-out infinite',
            }}
          />
        </div>

        {/* Floating glass cards */}
        {floatingCards.map((card) => (
          <div
            key={card.label}
            className="absolute z-10 flex items-center gap-2 rounded-xl border border-white/10 bg-white/5 px-3.5 py-2.5 text-xs font-medium text-white/80 shadow-lg backdrop-blur-md"
            style={{
              top: card.top,
              left: card.left,
              right: card.right,
              transform: `rotate(${card.rotate})`,
              animation: `float ${card.duration} ease-in-out ${card.delay} infinite`,
            } as React.CSSProperties}
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="14"
              height="14"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="1.5"
              className="text-blue-400"
            >
              <path d={card.icon} />
            </svg>
            {card.label}
          </div>
        ))}

        {/* Center text */}
        <div className="z-10 text-center">
          <h2 className="text-4xl font-bold bg-gradient-to-r from-blue-400 via-purple-400 to-cyan-400 bg-clip-text text-transparent">
            AI-Powered Support
          </h2>
          <p className="mt-4 text-lg text-gray-400">
            for Modern Businesses
          </p>
        </div>
      </div>
    </div>
  );
}

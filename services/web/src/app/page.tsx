export default function Home() {
  return (
    <div className="flex flex-1 flex-col items-center justify-center px-6 text-center">
      <div className="mb-8">
        <div className="mx-auto mb-6 flex h-20 w-20 items-center justify-center rounded-2xl bg-foreground text-background">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="36"
            height="36"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <path d="M12 2L2 7l10 5 10-5-10-5z" />
            <path d="M2 17l10 5 10-5" />
            <path d="M2 12l10 5 10-5" />
          </svg>
        </div>
        <h1 className="text-4xl font-bold tracking-tight sm:text-5xl">
          Coming Soon
        </h1>
      </div>
      <p className="max-w-md text-lg leading-relaxed text-zinc-500 dark:text-zinc-400">
        We&apos;re building something great. Our platform is under development
        and will be available shortly.
      </p>
      <div className="mt-10 flex items-center gap-2 text-sm text-zinc-400 dark:text-zinc-500">
        <span className="relative flex h-2 w-2">
          <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-emerald-400 opacity-75" />
          <span className="relative inline-flex h-2 w-2 rounded-full bg-emerald-500" />
        </span>
        In Development
      </div>
    </div>
  );
}

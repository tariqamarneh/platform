'use client';

import { motion } from 'framer-motion';
import Link from 'next/link';

export function Hero() {
  return (
    <section className="relative flex min-h-screen items-center justify-center overflow-hidden px-6 pt-14">
      {/* Animated gradient mesh background */}
      <div className="pointer-events-none absolute inset-0">
        {/* Blue blob */}
        <div
          className="absolute left-[10%] top-[15%] h-[500px] w-[500px] rounded-full opacity-40 blur-[120px]"
          style={{
            background: 'radial-gradient(circle, #3b82f6 0%, transparent 70%)',
            animation: 'mesh-move 12s ease-in-out infinite, float 6s ease-in-out infinite',
          }}
        />
        {/* Purple blob */}
        <div
          className="absolute right-[15%] top-[30%] h-[450px] w-[450px] rounded-full opacity-30 blur-[120px]"
          style={{
            background: 'radial-gradient(circle, #7c3aed 0%, transparent 70%)',
            animation: 'mesh-move-reverse 15s ease-in-out infinite, float 8s ease-in-out 1s infinite',
          }}
        />
        {/* Cyan/teal blob */}
        <div
          className="absolute bottom-[20%] left-[20%] h-[420px] w-[420px] rounded-full opacity-30 blur-[120px]"
          style={{
            background: 'radial-gradient(circle, #06b6d4 0%, transparent 70%)',
            animation: 'mesh-move-slow 18s ease-in-out infinite, float 7s ease-in-out 0.5s infinite',
          }}
        />
        {/* Pink/rose blob */}
        <div
          className="absolute right-[10%] top-[55%] h-[380px] w-[380px] rounded-full opacity-25 blur-[120px]"
          style={{
            background: 'radial-gradient(circle, #f43f5e 0%, transparent 70%)',
            animation: 'mesh-move 20s ease-in-out infinite, float 9s ease-in-out 2s infinite',
          }}
        />
        {/* Extra blue blob bottom */}
        <div
          className="absolute bottom-[5%] right-[30%] h-[350px] w-[350px] rounded-full opacity-20 blur-[120px]"
          style={{
            background: 'radial-gradient(circle, #3b82f6 0%, transparent 70%)',
            animation: 'mesh-move-reverse 22s ease-in-out infinite, float 10s ease-in-out 3s infinite',
          }}
        />
      </div>

      {/* Grid pattern overlay */}
      <div className="grid-pattern pointer-events-none absolute inset-0" />

      <div className="relative z-10 mx-auto w-full max-w-6xl">
        {/* Beta badge */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6 }}
          className="mb-8"
        >
          <span className="relative inline-flex items-center gap-2 overflow-hidden rounded-full border border-white/10 bg-white/5 px-4 py-1.5 text-xs font-medium text-muted backdrop-blur-sm">
            <span
              className="pointer-events-none absolute inset-0 rounded-full"
              style={{
                backgroundImage: 'linear-gradient(90deg, transparent 30%, rgba(59,130,246,0.15), rgba(124,58,237,0.15), transparent 70%)',
                backgroundSize: '200% 100%',
                animation: 'shimmer 3s linear infinite',
              }}
              aria-hidden="true"
            />
            <span className="relative h-1.5 w-1.5 rounded-full bg-blue-400" />
            <span className="relative">Now in Beta</span>
          </span>
        </motion.div>

        {/* Headline */}
        <motion.h1
          initial={{ opacity: 0, y: 40 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8, ease: [0.25, 0.46, 0.45, 0.94] }}
          className="text-6xl font-bold leading-[0.9] tracking-tighter sm:text-7xl lg:text-8xl xl:text-[10rem]"
        >
          AI That Talks
          <br />
          To Your{' '}
          <span className="bg-gradient-to-r from-blue-400 via-violet-400 to-purple-500 bg-clip-text text-transparent">
            Customers
          </span>
        </motion.h1>

        {/* Second gradient text line */}
        <motion.p
          initial={{ opacity: 0, y: 30 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.7, delay: 0.15 }}
          className="mt-4 text-2xl font-semibold tracking-tight sm:text-3xl lg:text-4xl"
        >
          <span className="bg-gradient-to-r from-cyan-400 via-blue-400 to-violet-400 bg-clip-text text-transparent">
            24/7 support, zero wait time.
          </span>
        </motion.p>

        {/* Description */}
        <motion.p
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.3 }}
          className="mt-8 max-w-md text-base text-muted sm:text-lg"
        >
          Intelligent WhatsApp automation that handles support, sales, and
          engagement around the clock.
        </motion.p>

        {/* CTA button with glow */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.5 }}
          className="mt-10"
        >
          <Link
            href="/register"
            className="group inline-flex h-12 items-center gap-2 rounded-full bg-white px-8 text-sm font-semibold text-[#030712] transition-all duration-300 hover:scale-105"
            style={{
              animation: 'pulse-glow 3s ease-in-out infinite',
            }}
          >
            Start Free
            <svg
              aria-hidden="true"
              width="16"
              height="16"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
              className="transition-transform duration-200 group-hover:translate-x-1"
            >
              <line x1="5" y1="12" x2="19" y2="12" />
              <polyline points="12 5 19 12 12 19" />
            </svg>
          </Link>
        </motion.div>

        {/* Trusted by logos */}
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ duration: 0.8, delay: 0.7 }}
          className="mt-14 flex flex-wrap items-center gap-x-6 gap-y-2"
        >
          <span className="text-xs uppercase tracking-wider text-muted/50">
            Trusted by
          </span>
          <div className="flex flex-wrap items-center gap-x-5 gap-y-2 text-sm font-medium text-muted/40">
            <span>TechCorp</span>
            <span className="text-muted/20">&bull;</span>
            <span>StartupX</span>
            <span className="text-muted/20">&bull;</span>
            <span>GlobalCo</span>
            <span className="text-muted/20">&bull;</span>
            <span>NovaTech</span>
            <span className="text-muted/20">&bull;</span>
            <span>CloudBase</span>
          </div>
        </motion.div>
      </div>

      {/* Scroll indicator */}
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ delay: 1.2, duration: 0.6 }}
        className="absolute bottom-8 left-1/2 z-10 -translate-x-1/2"
      >
        <div
          className="flex flex-col items-center gap-1"
          style={{ animation: 'scroll-bounce 2s ease-in-out infinite' }}
        >
          <span className="text-[10px] uppercase tracking-widest text-muted/40">
            Scroll
          </span>
          <svg
            aria-hidden="true"
            width="16"
            height="16"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="1.5"
            strokeLinecap="round"
            strokeLinejoin="round"
            className="text-muted/40"
          >
            <polyline points="6 9 12 15 18 9" />
          </svg>
        </div>
      </motion.div>
    </section>
  );
}

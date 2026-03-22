'use client';

import { motion } from 'framer-motion';
import Link from 'next/link';

export function Hero() {
  return (
    <section className="relative flex min-h-screen items-center justify-center overflow-hidden px-6 pt-12">
      {/* Animated gradient mesh background */}
      <div className="pointer-events-none absolute inset-0">
        <div
          className="absolute left-[10%] top-[15%] h-[500px] w-[500px] rounded-full opacity-40 blur-[120px]"
          style={{
            background: 'radial-gradient(circle, #3b82f6 0%, transparent 70%)',
            animation: 'mesh-move 12s ease-in-out infinite',
          }}
        />
        <div
          className="absolute right-[15%] top-[30%] h-[450px] w-[450px] rounded-full opacity-30 blur-[120px]"
          style={{
            background: 'radial-gradient(circle, #7c3aed 0%, transparent 70%)',
            animation: 'mesh-move-reverse 15s ease-in-out infinite',
          }}
        />
        <div
          className="absolute bottom-[10%] left-[30%] h-[400px] w-[400px] rounded-full opacity-25 blur-[120px]"
          style={{
            background: 'radial-gradient(circle, #3b82f6 0%, transparent 70%)',
            animation: 'mesh-move-slow 18s ease-in-out infinite',
          }}
        />
        <div
          className="absolute right-[5%] top-[60%] h-[350px] w-[350px] rounded-full opacity-20 blur-[120px]"
          style={{
            background: 'radial-gradient(circle, #7c3aed 0%, transparent 70%)',
            animation: 'mesh-move 20s ease-in-out infinite',
          }}
        />
      </div>

      {/* Grid pattern overlay */}
      <div className="grid-pattern pointer-events-none absolute inset-0" />

      <div className="relative z-10 mx-auto w-full max-w-6xl">
        <motion.h1
          initial={{ opacity: 0, y: 40 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8, ease: [0.25, 0.46, 0.45, 0.94] }}
          className="text-6xl font-bold leading-[0.9] tracking-tighter sm:text-7xl lg:text-8xl xl:text-[9rem]"
        >
          AI That Talks
          <br />
          To Your{' '}
          <span className="bg-gradient-to-r from-blue-400 via-violet-400 to-purple-500 bg-clip-text text-transparent">
            Customers
          </span>
        </motion.h1>

        <motion.p
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.3 }}
          className="mt-8 max-w-md text-base text-muted sm:text-lg"
        >
          Intelligent WhatsApp automation that handles support, sales, and
          engagement around the clock.
        </motion.p>

        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.5 }}
          className="mt-10"
        >
          <Link
            href="/register"
            className="group inline-flex h-12 items-center gap-2 rounded-full bg-white px-8 text-sm font-semibold text-[#030712] transition-all duration-300 hover:scale-105 hover:shadow-[0_0_30px_rgba(59,130,246,0.3)]"
          >
            Start Free
            <svg
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
      </div>
    </section>
  );
}

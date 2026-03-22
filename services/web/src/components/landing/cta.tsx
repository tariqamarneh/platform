'use client';

import { motion, useInView } from 'framer-motion';
import { useRef } from 'react';
import Link from 'next/link';

const floatingDots = [
  { size: 4, x: '12%', y: '20%', duration: 6, delay: 0 },
  { size: 3, x: '85%', y: '30%', duration: 7, delay: 1.2 },
  { size: 5, x: '25%', y: '75%', duration: 8, delay: 0.5 },
  { size: 3, x: '70%', y: '80%', duration: 6.5, delay: 2 },
  { size: 4, x: '50%', y: '15%', duration: 7.5, delay: 0.8 },
  { size: 3, x: '90%', y: '60%', duration: 6, delay: 1.5 },
];

export function CTA() {
  const ref = useRef(null);
  const isInView = useInView(ref, { once: true, margin: '-80px' });

  return (
    <section className="relative px-6 py-24 sm:py-32" ref={ref}>
      {/* Background gradient */}
      <div className="pointer-events-none absolute inset-0 overflow-hidden">
        <div
          className="absolute left-1/2 top-1/2 h-[600px] w-[800px] -translate-x-1/2 -translate-y-1/2 rounded-full opacity-20 blur-[120px]"
          style={{
            background: 'radial-gradient(ellipse, #3b82f6 0%, #7c3aed 50%, transparent 70%)',
          }}
        />
      </div>

      {/* Floating dots */}
      <div className="pointer-events-none absolute inset-0 overflow-hidden">
        {floatingDots.map((dot, i) => (
          <motion.div
            key={i}
            className="absolute rounded-full bg-blue-400/20"
            style={{
              width: dot.size,
              height: dot.size,
              left: dot.x,
              top: dot.y,
            }}
            animate={{
              y: [0, -20, 0, 15, 0],
              x: [0, 10, -5, 8, 0],
              opacity: [0.2, 0.5, 0.3, 0.5, 0.2],
            }}
            transition={{
              duration: dot.duration,
              delay: dot.delay,
              repeat: Infinity,
              ease: 'easeInOut',
            }}
          />
        ))}
      </div>

      <motion.div
        initial={{ opacity: 0, y: 30 }}
        animate={isInView ? { opacity: 1, y: 0 } : { opacity: 0, y: 30 }}
        transition={{ duration: 0.6 }}
        className="relative mx-auto max-w-3xl text-center"
      >
        <h2 className="text-5xl font-bold tracking-tighter sm:text-6xl lg:text-7xl">
          Ready to{' '}
          <span className="bg-gradient-to-r from-blue-400 via-violet-400 to-purple-500 bg-clip-text text-transparent">
            automate?
          </span>
        </h2>
        <p className="mx-auto mt-3 max-w-md text-lg text-muted/80 sm:text-xl">
          Your customers are waiting.
        </p>
        <p className="mx-auto mt-2 max-w-md text-sm text-muted sm:text-base">
          Join hundreds of businesses already using AI to handle customer
          conversations.
        </p>

        <div className="mt-10">
          <Link
            href="/register"
            className="group inline-flex h-16 items-center gap-3 rounded-full bg-white px-12 text-base font-semibold text-[#030712] transition-all duration-300 hover:scale-105 hover:shadow-[0_0_50px_rgba(59,130,246,0.3)]"
          >
            Get Started Free
            <svg
              aria-hidden="true"
              width="18"
              height="18"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
              className="transition-transform duration-300 group-hover:translate-x-1.5"
            >
              <line x1="5" y1="12" x2="19" y2="12" />
              <polyline points="12 5 19 12 12 19" />
            </svg>
          </Link>
        </div>

        <p className="mt-4 text-xs text-muted/50">
          No credit card required
        </p>
      </motion.div>
    </section>
  );
}

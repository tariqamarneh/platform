'use client';

import { useState } from 'react';
import { motion, useMotionValueEvent, useScroll } from 'framer-motion';
import Link from 'next/link';

export function Navbar() {
  const [scrolled, setScrolled] = useState(false);
  const { scrollY } = useScroll();

  useMotionValueEvent(scrollY, 'change', (latest) => {
    setScrolled(latest > 20);
  });

  return (
    <motion.header
      className={`fixed inset-x-0 top-0 z-50 transition-all duration-500 ${
        scrolled
          ? 'border-b border-white/5 bg-[#030712]/70 backdrop-blur-2xl'
          : 'bg-transparent'
      }`}
    >
      <nav className="mx-auto flex h-12 max-w-6xl items-center justify-between px-6">
        <Link
          href="/"
          className="text-sm font-semibold tracking-tight text-foreground"
        >
          Business Agent
        </Link>

        <div className="flex items-center gap-5">
          <Link
            href="/login"
            className="text-xs text-muted transition-colors duration-200 hover:text-foreground"
          >
            Login
          </Link>
          <Link
            href="/register"
            className="inline-flex h-7 items-center rounded-full bg-white px-4 text-xs font-medium text-[#030712] transition-transform duration-200 hover:scale-105"
          >
            Start Free
          </Link>
        </div>
      </nav>
    </motion.header>
  );
}

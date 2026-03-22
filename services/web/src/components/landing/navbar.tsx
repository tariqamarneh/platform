'use client';

import { useState } from 'react';
import { motion, useMotionValueEvent, useScroll, AnimatePresence } from 'framer-motion';
import Link from 'next/link';

export function Navbar() {
  const [scrolled, setScrolled] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const { scrollY } = useScroll();

  useMotionValueEvent(scrollY, 'change', (latest) => {
    setScrolled(latest > 20);
  });

  const navLinks = [
    { label: 'Features', href: '#features' },
    { label: 'Pricing', href: '#pricing' },
  ];

  return (
    <>
      <motion.header
        className={`fixed inset-x-0 top-0 z-50 transition-all duration-500 ${
          scrolled
            ? 'bg-[#030712]/70 backdrop-blur-2xl'
            : 'bg-transparent'
        }`}
      >
        {/* Gradient bottom line when scrolled */}
        {scrolled && (
          <div
            className="absolute inset-x-0 bottom-0 h-px"
            style={{
              background: 'linear-gradient(90deg, transparent, #3b82f6, #7c3aed, transparent)',
            }}
          />
        )}

        <nav className="mx-auto flex h-14 max-w-6xl items-center justify-between px-6">
          <Link
            href="/"
            className="text-sm font-semibold tracking-tight text-foreground"
          >
            Business Agent
          </Link>

          {/* Desktop nav */}
          <div className="hidden items-center gap-6 sm:flex">
            {navLinks.map((link) => (
              <a
                key={link.href}
                href={link.href}
                className="text-xs text-muted transition-colors duration-200 hover:text-foreground"
              >
                {link.label}
              </a>
            ))}
            <Link
              href="/login"
              className="text-xs text-muted transition-colors duration-200 hover:text-foreground"
            >
              Login
            </Link>
            <Link
              href="/register"
              className="glow-blue inline-flex h-8 items-center rounded-full bg-white px-5 text-xs font-medium text-[#030712] transition-all duration-300 hover:scale-105"
            >
              Start Free
            </Link>
          </div>

          {/* Mobile hamburger */}
          <button
            onClick={() => setMobileOpen(!mobileOpen)}
            className="relative z-50 flex h-8 w-8 flex-col items-center justify-center gap-1.5 sm:hidden"
            aria-label="Toggle menu"
          >
            <motion.span
              animate={mobileOpen ? { rotate: 45, y: 6 } : { rotate: 0, y: 0 }}
              className="block h-px w-5 bg-foreground"
            />
            <motion.span
              animate={mobileOpen ? { opacity: 0 } : { opacity: 1 }}
              className="block h-px w-5 bg-foreground"
            />
            <motion.span
              animate={mobileOpen ? { rotate: -45, y: -6 } : { rotate: 0, y: 0 }}
              className="block h-px w-5 bg-foreground"
            />
          </button>
        </nav>
      </motion.header>

      {/* Mobile overlay */}
      <AnimatePresence>
        {mobileOpen && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.3 }}
            className="fixed inset-0 z-40 flex flex-col items-center justify-center gap-8 bg-[#030712]/95 backdrop-blur-xl sm:hidden"
          >
            {navLinks.map((link, i) => (
              <motion.a
                key={link.href}
                href={link.href}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: 10 }}
                transition={{ delay: i * 0.08 }}
                onClick={() => setMobileOpen(false)}
                className="text-2xl font-medium text-foreground transition-colors hover:text-accent"
              >
                {link.label}
              </motion.a>
            ))}
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: 10 }}
              transition={{ delay: 0.16 }}
            >
              <Link
                href="/login"
                onClick={() => setMobileOpen(false)}
                className="text-2xl font-medium text-muted transition-colors hover:text-foreground"
              >
                Login
              </Link>
            </motion.div>
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: 10 }}
              transition={{ delay: 0.24 }}
            >
              <Link
                href="/register"
                onClick={() => setMobileOpen(false)}
                className="glow-blue inline-flex h-12 items-center rounded-full bg-white px-8 text-sm font-semibold text-[#030712]"
              >
                Start Free
              </Link>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </>
  );
}

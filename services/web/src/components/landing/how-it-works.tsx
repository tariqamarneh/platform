'use client';

import { motion, useInView } from 'framer-motion';
import { useRef } from 'react';

const steps = [
  {
    number: '01',
    title: 'Connect',
    description: 'Link your WhatsApp Business account in a few clicks.',
    color: 'blue',
    icon: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71" />
        <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71" />
      </svg>
    ),
  },
  {
    number: '02',
    title: 'Configure',
    description: 'Upload FAQs, docs, or your website. AI learns instantly.',
    color: 'violet',
    icon: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <path d="M12 3l1.912 5.813a2 2 0 0 0 1.275 1.275L21 12l-5.813 1.912a2 2 0 0 0-1.275 1.275L12 21l-1.912-5.813a2 2 0 0 0-1.275-1.275L3 12l5.813-1.912a2 2 0 0 0 1.275-1.275L12 3z" />
      </svg>
    ),
  },
  {
    number: '03',
    title: 'Launch',
    description: 'Go live and start handling customer queries automatically.',
    color: 'emerald',
    icon: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <path d="M4.5 16.5c-1.5 1.26-2 5-2 5s3.74-.5 5-2c.71-.84.7-2.13-.09-2.91a2.18 2.18 0 0 0-2.91-.09z" />
        <path d="M12 15l-3-3a22 22 0 0 1 2-3.95A12.88 12.88 0 0 1 22 2c0 2.72-.78 7.5-6 11a22.35 22.35 0 0 1-4 2z" />
        <path d="M9 12H4s.55-3.03 2-4c1.62-1.08 5 0 5 0" />
        <path d="M12 15v5s3.03-.55 4-2c1.08-1.62 0-5 0-5" />
      </svg>
    ),
  },
];

const colorMap: Record<string, { bar: string; text: string; bg: string }> = {
  blue: {
    bar: 'bg-blue-500',
    text: 'text-blue-400',
    bg: 'bg-blue-500/10',
  },
  violet: {
    bar: 'bg-violet-500',
    text: 'text-violet-400',
    bg: 'bg-violet-500/10',
  },
  emerald: {
    bar: 'bg-emerald-500',
    text: 'text-emerald-400',
    bg: 'bg-emerald-500/10',
  },
};

export function HowItWorks() {
  const ref = useRef(null);
  const isInView = useInView(ref, { once: true, margin: '-80px' });

  return (
    <section id="how-it-works" className="relative px-6 py-20 sm:py-28">
      <div className="mx-auto max-w-5xl" ref={ref}>
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={isInView ? { opacity: 1, y: 0 } : { opacity: 0, y: 20 }}
          transition={{ duration: 0.5 }}
          className="mb-16"
        >
          <span className="mb-4 inline-block text-xs font-medium uppercase tracking-widest text-blue-400">
            Process
          </span>
          <h2 className="text-2xl font-bold tracking-tighter sm:text-3xl">
            Three steps to{' '}
            <span className="bg-gradient-to-r from-blue-400 to-violet-400 bg-clip-text text-transparent">
              launch
            </span>
          </h2>
        </motion.div>

        <div className="relative flex flex-col gap-8 sm:flex-row sm:items-start sm:gap-6">
          {/* Dashed connecting line (desktop) */}
          <svg
            className="pointer-events-none absolute left-[calc(16.67%+20px)] right-[calc(16.67%+20px)] top-[52px] hidden sm:block"
            height="2"
            preserveAspectRatio="none"
            style={{ width: 'calc(100% - 33.34% - 40px)' }}
          >
            <motion.line
              x1="0"
              y1="1"
              x2="100%"
              y2="1"
              stroke="url(#dashedGradient)"
              strokeWidth="2"
              strokeDasharray="6 6"
              initial={{ pathLength: 0 }}
              animate={isInView ? { pathLength: 1 } : { pathLength: 0 }}
              transition={{ duration: 1.2, delay: 0.4, ease: 'easeOut' }}
            />
            <defs>
              <linearGradient id="dashedGradient" x1="0%" y1="0%" x2="100%" y2="0%">
                <stop offset="0%" stopColor="#3b82f6" stopOpacity="0.5" />
                <stop offset="50%" stopColor="#7c3aed" stopOpacity="0.5" />
                <stop offset="100%" stopColor="#10b981" stopOpacity="0.5" />
              </linearGradient>
            </defs>
          </svg>

          {steps.map((step, i) => {
            const colors = colorMap[step.color];
            return (
              <motion.div
                key={step.number}
                initial={{ opacity: 0, y: 24 }}
                animate={isInView ? { opacity: 1, y: 0 } : { opacity: 0, y: 24 }}
                transition={{ duration: 0.5, delay: 0.2 + i * 0.15 }}
                className="group relative flex-1 text-center sm:px-4"
              >
                {/* Glass card */}
                <div className="relative mx-auto max-w-[260px] overflow-hidden rounded-2xl border border-white/[0.06] bg-white/[0.03] p-6 backdrop-blur-sm transition-all duration-300 group-hover:-translate-y-1 group-hover:border-white/[0.1] group-hover:shadow-lg group-hover:shadow-black/20">
                  {/* Color accent bar */}
                  <div className={`absolute inset-x-0 top-0 h-[2px] ${colors.bar} opacity-60`} />

                  {/* Number + icon row */}
                  <div className="mb-4 flex items-center justify-center gap-3">
                    <div className={`flex h-10 w-10 items-center justify-center rounded-xl ${colors.bg} ${colors.text}`}>
                      {step.icon}
                    </div>
                    <span className={`text-sm font-bold ${colors.text} opacity-60`}>
                      {step.number}
                    </span>
                  </div>

                  <h3 className="text-base font-semibold tracking-tight">
                    {step.title}
                  </h3>
                  <p className="mx-auto mt-2 max-w-[200px] text-sm leading-relaxed text-muted">
                    {step.description}
                  </p>
                </div>
              </motion.div>
            );
          })}
        </div>
      </div>
    </section>
  );
}

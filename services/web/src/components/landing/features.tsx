'use client';

import { motion, useInView } from 'framer-motion';
import { useRef } from 'react';

const features = [
  {
    title: 'WhatsApp Native',
    description:
      'Built from the ground up for WhatsApp Business API. Not a generic chatbot bolted onto a messaging platform — a purpose-built conversational engine.',
    icon: (
      <svg aria-hidden="true" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z" />
      </svg>
    ),
    size: 'big' as const,
  },
  {
    title: 'Sub-Second Replies',
    description: 'Instant AI responses that feel natural. Your customers never wait.',
    icon: (
      <svg aria-hidden="true" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2" />
      </svg>
    ),
    size: 'small' as const,
  },
  {
    title: 'Multi-Language',
    description: 'Speaks your customers\u2019 language. 50+ languages, zero configuration.',
    icon: (
      <svg aria-hidden="true" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <circle cx="12" cy="12" r="10" />
        <line x1="2" y1="12" x2="22" y2="12" />
        <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z" />
      </svg>
    ),
    size: 'small' as const,
  },
  {
    title: 'Learns & Improves',
    description:
      'Every conversation makes your agent smarter. Machine learning that continuously adapts to your business and customer patterns.',
    icon: (
      <svg aria-hidden="true" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z" />
        <path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z" />
      </svg>
    ),
    size: 'big' as const,
  },
  {
    title: 'Enterprise Ready',
    description:
      'SOC2 compliant, end-to-end encryption, 99.9% uptime SLA, and dedicated support. Built for businesses that demand reliability.',
    icon: (
      <svg aria-hidden="true" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
      </svg>
    ),
    size: 'full' as const,
  },
];

function PulsingDot() {
  return (
    <span className="absolute right-4 top-4 flex h-2.5 w-2.5">
      <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-blue-400 opacity-40" />
      <span className="relative inline-flex h-2.5 w-2.5 rounded-full bg-blue-500" />
    </span>
  );
}

function BentoCard({
  feature,
  index,
}: {
  feature: (typeof features)[number];
  index: number;
}) {
  const ref = useRef(null);
  const isInView = useInView(ref, { once: true, margin: '-60px' });

  const isBig = feature.size === 'big';
  const isFull = feature.size === 'full';

  return (
    <motion.div
      ref={ref}
      initial={{ opacity: 0, y: 30 }}
      animate={isInView ? { opacity: 1, y: 0 } : { opacity: 0, y: 30 }}
      transition={{ duration: 0.6, delay: index * 0.1, ease: 'easeOut' }}
      className={`bento-card group relative overflow-hidden p-8 backdrop-blur-sm ${
        isBig
          ? 'sm:col-span-2'
          : isFull
            ? 'sm:col-span-3'
            : 'sm:col-span-1'
      } ${isFull ? 'bg-gradient-to-r from-blue-500/[0.06] via-violet-500/[0.04] to-transparent' : ''}`}
    >
      {/* Top highlight line */}
      <div
        className="pointer-events-none absolute inset-x-0 top-0 h-px"
        style={{
          background:
            'linear-gradient(90deg, transparent 10%, #3b82f6 30%, #7c3aed 70%, transparent 90%)',
          opacity: 0.4,
        }}
      />

      <div className="bento-glow" />

      {/* Pulsing dot on big cards */}
      {isBig && <PulsingDot />}

      <div className="relative z-10">
        {/* Icon with glow */}
        <div className="relative mb-5 inline-flex h-12 w-12 items-center justify-center rounded-xl bg-white/5 text-blue-400">
          <div
            className="pointer-events-none absolute inset-0 rounded-xl opacity-50"
            style={{
              background: 'radial-gradient(circle at center, rgba(59,130,246,0.15) 0%, transparent 70%)',
            }}
          />
          <span className="relative z-10">{feature.icon}</span>
        </div>

        <h3 className="text-xl font-semibold tracking-tight transition-colors duration-300 group-hover:text-blue-300">
          {feature.title}
        </h3>
        <p className="mt-2 max-w-md text-sm leading-relaxed text-muted">
          {feature.description}
        </p>
      </div>
    </motion.div>
  );
}

export function Features() {
  const headingRef = useRef(null);
  const headingInView = useInView(headingRef, { once: true, margin: '-80px' });

  return (
    <section id="features" className="relative px-6 py-24 sm:py-32">
      <div className="mx-auto max-w-5xl">
        <motion.div
          ref={headingRef}
          initial={{ opacity: 0, y: 20 }}
          animate={headingInView ? { opacity: 1, y: 0 } : { opacity: 0, y: 20 }}
          transition={{ duration: 0.5 }}
          className="mb-16"
        >
          <span className="mb-4 inline-block text-xs font-medium uppercase tracking-widest text-blue-400">
            Capabilities
          </span>
          <h2 className="text-3xl font-bold tracking-tighter sm:text-4xl lg:text-5xl">
            Not another
            <br />
            <span className="bg-gradient-to-r from-blue-400 to-violet-400 bg-clip-text text-transparent">
              generic chatbot
            </span>
          </h2>
        </motion.div>

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
          {features.map((feature, i) => (
            <BentoCard key={feature.title} feature={feature} index={i} />
          ))}
        </div>
      </div>
    </section>
  );
}

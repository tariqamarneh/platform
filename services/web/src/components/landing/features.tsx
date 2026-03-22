'use client';

import { motion, useInView } from 'framer-motion';
import { useRef } from 'react';

const features = [
  {
    title: 'WhatsApp Native',
    description:
      'Built from the ground up for WhatsApp Business API. Not a generic chatbot bolted onto a messaging platform — a purpose-built conversational engine.',
    icon: (
      <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z" />
      </svg>
    ),
    size: 'big' as const,
  },
  {
    title: 'Sub-Second Replies',
    description: 'Instant AI responses that feel natural. Your customers never wait.',
    icon: (
      <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2" />
      </svg>
    ),
    size: 'small' as const,
  },
  {
    title: 'Multi-Language',
    description: 'Speaks your customers\u2019 language. 50+ languages, zero configuration.',
    icon: (
      <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
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
      <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
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
      <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
      </svg>
    ),
    size: 'full' as const,
  },
];

function BentoCard({
  feature,
  index,
}: {
  feature: (typeof features)[number];
  index: number;
}) {
  const ref = useRef(null);
  const isInView = useInView(ref, { once: true, margin: '-60px' });

  const gradients: Record<string, string> = {
    0: 'from-blue-500/10 to-transparent',
    1: '',
    2: '',
    3: 'from-violet-500/10 to-transparent',
    4: 'from-blue-500/5 via-violet-500/5 to-transparent',
  };

  return (
    <motion.div
      ref={ref}
      initial={{ opacity: 0, y: 30 }}
      animate={isInView ? { opacity: 1, y: 0 } : { opacity: 0, y: 30 }}
      transition={{ duration: 0.6, delay: index * 0.1, ease: 'easeOut' }}
      className={`bento-card p-8 backdrop-blur-sm ${
        feature.size === 'big'
          ? 'sm:col-span-2'
          : feature.size === 'full'
            ? 'sm:col-span-3'
            : 'sm:col-span-1'
      } ${gradients[index] ? `bg-gradient-to-br ${gradients[index]}` : ''}`}
    >
      <div className="bento-glow" />
      <div className="relative z-10">
        <div className="mb-5 inline-flex h-11 w-11 items-center justify-center rounded-xl bg-white/5 text-blue-400">
          {feature.icon}
        </div>
        <h3 className="text-lg font-semibold tracking-tight">{feature.title}</h3>
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
          <h2 className="text-3xl font-bold tracking-tighter sm:text-4xl lg:text-5xl">
            Not another
            <br />
            <span className="bg-gradient-to-r from-blue-400 to-violet-400 bg-clip-text text-transparent">
              generic chatbot
            </span>
          </h2>
        </motion.div>

        {/* Bento grid: row 1 = big + small, row 2 = small + big, row 3 = full */}
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
          {features.map((feature, i) => (
            <BentoCard key={feature.title} feature={feature} index={i} />
          ))}
        </div>
      </div>
    </section>
  );
}

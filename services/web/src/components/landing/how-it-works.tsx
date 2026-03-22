'use client';

import { motion, useInView } from 'framer-motion';
import { useRef } from 'react';

const steps = [
  {
    number: '01',
    title: 'Connect',
    description: 'Link your WhatsApp Business account in a few clicks.',
  },
  {
    number: '02',
    title: 'Configure',
    description: 'Upload FAQs, docs, or your website. AI learns instantly.',
  },
  {
    number: '03',
    title: 'Launch',
    description: 'Go live and start handling customer queries automatically.',
  },
];

export function HowItWorks() {
  const ref = useRef(null);
  const isInView = useInView(ref, { once: true, margin: '-80px' });

  return (
    <section id="how-it-works" className="relative px-6 py-20 sm:py-28">
      <div className="mx-auto max-w-5xl" ref={ref}>
        <motion.h2
          initial={{ opacity: 0, y: 20 }}
          animate={isInView ? { opacity: 1, y: 0 } : { opacity: 0, y: 20 }}
          transition={{ duration: 0.5 }}
          className="mb-16 text-2xl font-bold tracking-tighter sm:text-3xl"
        >
          Three steps to{' '}
          <span className="bg-gradient-to-r from-blue-400 to-violet-400 bg-clip-text text-transparent">
            launch
          </span>
        </motion.h2>

        <div className="relative flex flex-col gap-12 sm:flex-row sm:items-start sm:gap-0">
          {/* Connecting line (desktop) */}
          <motion.div
            initial={{ scaleX: 0 }}
            animate={isInView ? { scaleX: 1 } : { scaleX: 0 }}
            transition={{ duration: 1, delay: 0.3, ease: 'easeOut' }}
            className="absolute left-[calc(16.67%-12px)] right-[calc(16.67%-12px)] top-6 hidden h-px origin-left bg-gradient-to-r from-blue-500/40 via-violet-500/40 to-blue-500/40 sm:block"
          />

          {steps.map((step, i) => (
            <motion.div
              key={step.number}
              initial={{ opacity: 0, y: 20 }}
              animate={isInView ? { opacity: 1, y: 0 } : { opacity: 0, y: 20 }}
              transition={{ duration: 0.5, delay: 0.2 + i * 0.15 }}
              className="relative flex-1 text-center sm:px-4"
            >
              {/* Step circle */}
              <div className="relative z-10 mx-auto mb-4 flex h-12 w-12 items-center justify-center rounded-full border border-white/10 bg-[#030712] text-sm font-bold text-muted">
                {step.number}
              </div>
              <h3 className="text-base font-semibold tracking-tight">
                {step.title}
              </h3>
              <p className="mx-auto mt-2 max-w-[200px] text-sm leading-relaxed text-muted">
                {step.description}
              </p>
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  );
}

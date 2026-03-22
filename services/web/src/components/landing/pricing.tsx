'use client';

import { motion, useInView } from 'framer-motion';
import { useRef, useState } from 'react';
import Link from 'next/link';

const Check = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="shrink-0 text-blue-400">
    <polyline points="20 6 9 17 4 12" />
  </svg>
);

const Dash = () => (
  <span className="inline-block w-4 text-center text-white/20">&mdash;</span>
);

const plans = [
  {
    name: 'Free',
    monthlyPrice: '$0',
    annualPrice: '$0',
    description: 'Get started and explore.',
    features: [
      { text: '100 messages/day', included: true },
      { text: '1 WhatsApp number', included: true },
      { text: 'Basic analytics', included: true },
      { text: 'Community support', included: true },
      { text: 'Custom AI training', included: false },
      { text: 'Priority support', included: false },
    ],
    cta: 'Start Free',
    href: '/register',
    highlighted: false,
  },
  {
    name: 'Pro',
    monthlyPrice: '$49',
    annualPrice: '$39',
    description: 'For growing businesses.',
    features: [
      { text: 'Unlimited messages', included: true },
      { text: 'Multiple WhatsApp numbers', included: true },
      { text: 'Advanced analytics', included: true },
      { text: 'Priority support', included: true },
      { text: 'Custom AI training', included: true },
      { text: 'Dedicated account manager', included: true },
    ],
    cta: 'Get Started',
    href: '/register',
    highlighted: true,
  },
];

export function Pricing() {
  const [annual, setAnnual] = useState(false);
  const ref = useRef(null);
  const isInView = useInView(ref, { once: true, margin: '-80px' });

  return (
    <section id="pricing" className="relative px-6 py-24 sm:py-32">
      <div className="mx-auto max-w-4xl" ref={ref}>
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={isInView ? { opacity: 1, y: 0 } : { opacity: 0, y: 20 }}
          transition={{ duration: 0.5 }}
          className="mb-16"
        >
          <span className="mb-4 inline-block text-xs font-medium uppercase tracking-widest text-blue-400">
            Pricing
          </span>
          <h2 className="text-3xl font-bold tracking-tighter sm:text-4xl lg:text-5xl">
            Simple{' '}
            <span className="bg-gradient-to-r from-blue-400 to-violet-400 bg-clip-text text-transparent">
              pricing
            </span>
          </h2>
          <p className="mt-3 text-sm text-muted">
            Start free, upgrade when you need more. No hidden fees.
          </p>

          {/* Billing toggle */}
          <div className="mt-8 flex items-center gap-3">
            <span className={`text-sm transition-colors duration-300 ${!annual ? 'text-foreground' : 'text-muted'}`}>
              Monthly
            </span>
            <button
              role="switch"
              aria-checked={annual}
              aria-label="Toggle annual billing"
              onClick={() => setAnnual(!annual)}
              className={`relative h-7 w-12 rounded-full transition-colors duration-300 ${
                annual ? 'bg-blue-500' : 'bg-white/10'
              }`}
            >
              <motion.div
                className="absolute top-0.5 h-6 w-6 rounded-full bg-white shadow-sm"
                animate={{ x: annual ? 22 : 2 }}
                transition={{ type: 'spring', stiffness: 500, damping: 30 }}
              />
            </button>
            <span className={`text-sm transition-colors duration-300 ${annual ? 'text-foreground' : 'text-muted'}`}>
              Annual
            </span>
            <motion.span
              initial={false}
              animate={{ opacity: annual ? 1 : 0, scale: annual ? 1 : 0.8 }}
              transition={{ duration: 0.2 }}
              className="rounded-full bg-green-500/10 px-2.5 py-0.5 text-xs font-medium text-green-400"
            >
              Save 20%
            </motion.span>
          </div>
        </motion.div>

        <div className="grid gap-6 sm:grid-cols-2">
          {plans.map((plan, i) => (
            <motion.div
              key={plan.name}
              initial={{ opacity: 0, y: 30 }}
              animate={isInView ? { opacity: 1, y: 0 } : { opacity: 0, y: 30 }}
              transition={{ duration: 0.5, delay: i * 0.15 }}
              className={`relative rounded-2xl p-8 ${
                plan.highlighted
                  ? 'gradient-border bg-white/[0.04] scale-[1.02] sm:scale-105'
                  : 'border border-white/[0.06] bg-white/[0.02]'
              }`}
            >
              {plan.highlighted && (
                <span className="absolute -top-3.5 right-6 z-10 overflow-hidden rounded-full bg-gradient-to-r from-blue-500 to-violet-500 px-4 py-1 text-[10px] font-bold uppercase tracking-wider text-white">
                  Most Popular
                  {/* Shimmer overlay */}
                  <span
                    className="absolute inset-0 -translate-x-full animate-[shimmer_2.5s_ease-in-out_infinite]"
                    style={{
                      background:
                        'linear-gradient(90deg, transparent 0%, rgba(255,255,255,0.25) 50%, transparent 100%)',
                    }}
                  />
                </span>
              )}

              <h3 className="text-lg font-semibold">{plan.name}</h3>
              <p className="mt-1 text-xs text-muted">{plan.description}</p>

              <motion.div
                className="mt-6 flex items-baseline gap-1"
                initial={{ opacity: 0, scale: 0.9 }}
                animate={isInView ? { opacity: 1, scale: 1 } : { opacity: 0, scale: 0.9 }}
                transition={{ duration: 0.4, delay: 0.3 + i * 0.15 }}
              >
                <span className="text-4xl font-bold tracking-tight">
                  {annual ? plan.annualPrice : plan.monthlyPrice}
                </span>
                <span className="text-sm text-muted">/mo</span>
              </motion.div>
              {plan.highlighted && (
                <p className="mt-1 text-xs text-muted/60">per seat</p>
              )}

              <ul className="mt-8 space-y-3">
                {plan.features.map((feature) => (
                  <li key={feature.text} className="flex items-center gap-3 text-sm text-muted">
                    {feature.included ? <Check /> : <Dash />}
                    <span className={feature.included ? '' : 'opacity-40'}>
                      {feature.text}
                    </span>
                  </li>
                ))}
              </ul>

              <Link
                href={plan.href}
                className={`mt-8 block w-full rounded-xl py-3 text-center text-sm font-semibold transition-all duration-200 ${
                  plan.highlighted
                    ? 'bg-white text-[#030712] shadow-[0_0_20px_rgba(59,130,246,0.15)] hover:scale-[1.02] hover:shadow-[0_0_30px_rgba(59,130,246,0.3)]'
                    : 'border border-white/10 text-foreground hover:bg-white/5'
                }`}
              >
                {plan.cta}
              </Link>
            </motion.div>
          ))}
        </div>
      </div>

      {/* Shimmer keyframes injected via style tag */}
      <style>{`
        @keyframes shimmer {
          0% { transform: translateX(-100%); }
          100% { transform: translateX(200%); }
        }
      `}</style>
    </section>
  );
}

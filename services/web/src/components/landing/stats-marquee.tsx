export function StatsMarquee() {
  const items = [
    '10,000+ Messages Handled',
    '500+ Businesses',
    '99.9% Uptime',
    '24/7 Available',
    '3s Average Response',
  ];

  const Diamond = () => (
    <svg width="8" height="8" viewBox="0 0 8 8" className="text-blue-500/30">
      <rect x="4" y="0" width="5" height="5" rx="0.5" transform="rotate(45 4 4)" fill="currentColor" />
    </svg>
  );

  const track = items.map((item) => (
    <span key={item} className="flex items-center gap-6 whitespace-nowrap px-6">
      <span className="text-sm font-medium uppercase tracking-widest text-muted/60">
        {item}
      </span>
      <Diamond />
    </span>
  ));

  return (
    <section className="relative z-10 overflow-hidden py-5">
      {/* Top/bottom border glow */}
      <div
        className="pointer-events-none absolute inset-x-0 top-0 h-px"
        style={{
          background:
            'linear-gradient(90deg, transparent 0%, rgba(59,130,246,0.15) 30%, rgba(124,58,237,0.15) 70%, transparent 100%)',
        }}
      />
      <div
        className="pointer-events-none absolute inset-x-0 bottom-0 h-px"
        style={{
          background:
            'linear-gradient(90deg, transparent 0%, rgba(59,130,246,0.15) 30%, rgba(124,58,237,0.15) 70%, transparent 100%)',
        }}
      />

      {/* Left fade */}
      <div className="pointer-events-none absolute inset-y-0 left-0 z-10 w-24 bg-gradient-to-r from-[#030712] to-transparent" />
      {/* Right fade */}
      <div className="pointer-events-none absolute inset-y-0 right-0 z-10 w-24 bg-gradient-to-l from-[#030712] to-transparent" />

      <div className="marquee-track">
        {track}
        {track}
        {track}
        {track}
      </div>
    </section>
  );
}

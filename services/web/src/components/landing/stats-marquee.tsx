export function StatsMarquee() {
  const items = [
    '10,000+ Messages Handled',
    '500+ Businesses',
    '99.9% Uptime',
    '24/7 Available',
    '3s Average Response',
  ];

  const track = items.map((item) => (
    <span key={item} className="flex items-center gap-6 whitespace-nowrap px-6">
      <span className="text-xs font-medium uppercase tracking-widest text-muted/60">
        {item}
      </span>
      <span className="text-muted/20">&bull;</span>
    </span>
  ));

  return (
    <section className="relative z-10 border-y border-white/5 py-4 overflow-hidden">
      <div className="marquee-track">
        {/* Duplicate the track for seamless loop */}
        {track}
        {track}
        {track}
        {track}
      </div>
    </section>
  );
}

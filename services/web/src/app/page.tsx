import { Navbar } from '@/components/landing/navbar';
import { Hero } from '@/components/landing/hero';
import { StatsMarquee } from '@/components/landing/stats-marquee';
import { Features } from '@/components/landing/features';
import { HowItWorks } from '@/components/landing/how-it-works';
import { Pricing } from '@/components/landing/pricing';
import { CTA } from '@/components/landing/cta';
import { Footer } from '@/components/landing/footer';

export default function Home() {
  return (
    <div className="noise">
      <Navbar />
      <main>
        <Hero />
        <StatsMarquee />
        <Features />
        <HowItWorks />
        <Pricing />
        <CTA />
      </main>
      <Footer />
    </div>
  );
}

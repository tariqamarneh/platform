'use client';

import { useState } from 'react';
import { updateBusiness } from '@/lib/auth/admin-actions';
import { useRouter } from 'next/navigation';

interface Props {
  businessId: string;
  currentPlan: string;
  currentStatus: string;
}

export function BusinessActions({ businessId, currentPlan, currentStatus }: Props) {
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleUpdate(data: { plan?: string; status?: string }) {
    setLoading(true);
    setError(null);
    const result = await updateBusiness(businessId, data);
    setLoading(false);
    if (result.success) {
      router.refresh();
    } else {
      setError(result.error || 'Update failed');
    }
  }

  return (
    <div className="flex flex-col items-end gap-2">
      <div className="flex gap-2">
        <button
          onClick={() => handleUpdate({ plan: currentPlan === 'FREE' ? 'PAID' : 'FREE' })}
          disabled={loading}
          className="rounded-lg border border-white/10 px-3 py-1.5 text-xs hover:bg-white/5 transition-colors disabled:opacity-50"
        >
          {currentPlan === 'FREE' ? 'Upgrade to Paid' : 'Downgrade to Free'}
        </button>
        <button
          onClick={() => handleUpdate({ status: currentStatus === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE' })}
          disabled={loading}
          className={`rounded-lg px-3 py-1.5 text-xs transition-colors disabled:opacity-50 ${
            currentStatus === 'ACTIVE'
              ? 'border border-red-500/20 text-red-400 hover:bg-red-500/10'
              : 'border border-emerald-500/20 text-emerald-400 hover:bg-emerald-500/10'
          }`}
        >
          {currentStatus === 'ACTIVE' ? 'Suspend' : 'Activate'}
        </button>
      </div>
      {error && <p className="text-xs text-red-400">{error}</p>}
    </div>
  );
}

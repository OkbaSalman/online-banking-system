import React from 'react';
import { Landmark, Layers, LockKeyhole, ScrollText } from 'lucide-react';
import { BRAND } from '../../config';

const HIGHLIGHTS = [
  {
    icon: ScrollText,
    title: 'Hash-chained ledger',
    body: 'Every posting is appended to a per-account chain you can verify yourself, so no balance can be quietly rewritten.',
  },
  {
    icon: Layers,
    title: 'Shared accounts',
    body: 'Invite a partner as a co-owner and both of you keep full visibility over the same account.',
  },
  {
    icon: LockKeyhole,
    title: 'Verified identity',
    body: 'Email verification and KYC review gate the operations that move real money.',
  },
];

export default function AuthShell({ children }) {
  return (
    <div
      dir="ltr"
      className="relative flex min-h-screen items-center justify-center overflow-x-hidden bg-[#f8f9ff] p-3 font-sans text-[#0b1c30] sm:p-6 lg:p-12"
    >
      <div className="fixed left-[-5%] top-[-10%] -z-10 h-[40vw] w-[40vw] rounded-full bg-[#e5eeff] opacity-40 blur-[120px]" />
      <div className="fixed bottom-[-10%] right-[-5%] -z-10 h-[50vw] w-[50vw] rounded-full bg-[#3525cd]/5 opacity-35 blur-[150px]" />

      <main className="grid min-h-[760px] w-full max-w-6xl grid-cols-1 overflow-hidden rounded-[2rem] bg-white shadow-[0_40px_100px_rgba(11,28,48,0.08)] lg:grid-cols-12">
        <div className="relative hidden flex-col justify-between overflow-hidden bg-[#0b1c30] p-12 text-left lg:col-span-5 lg:flex">
          <div>
            <div className="mb-14 flex items-center gap-3">
              <div className="flex h-10 w-10 items-center justify-center rounded-full bg-[#4f46e5] text-white shadow-md">
                <Landmark size={20} />
              </div>
              <span className="font-headline text-2xl font-extrabold tracking-tighter text-white">
                {BRAND.name}
              </span>
            </div>

            <h1 className="mb-6 font-display text-[2.75rem] font-extrabold leading-[1.15] tracking-tight text-white">
              Banking that shows
              <br />
              <span className="text-[#a59bff]">its work.</span>
            </h1>

            <p className="max-w-sm font-body text-sm leading-relaxed text-[#c7c4d8]">
              Accounts, transfers, cards and bills on one double-entry core. Every movement leaves
              an auditable trail you can open and inspect.
            </p>
          </div>

          <div className="relative z-10 mt-auto space-y-5">
            {HIGHLIGHTS.map(({ icon: Icon, title, body }) => (
              <div key={title} className="flex items-start gap-3">
                <div className="mt-0.5 flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-white/10 text-[#a59bff]">
                  <Icon size={16} />
                </div>
                <div>
                  <p className="text-sm font-bold text-white">{title}</p>
                  <p className="mt-0.5 max-w-xs text-xs leading-relaxed text-[#c7c4d8]/80">{body}</p>
                </div>
              </div>
            ))}
          </div>

          <div className="pointer-events-none absolute inset-0">
            <div className="absolute left-[-10%] top-1/4 h-[80%] w-[80%] rounded-full border border-[#3525cd]/15" />
          </div>
        </div>

        <div className="relative flex flex-col justify-center p-6 sm:p-12 lg:col-span-7 lg:p-16">
          <div className="mx-auto mb-8 flex w-full max-w-md items-center justify-between lg:hidden">
            <div className="flex items-center gap-2">
              <div className="flex h-8 w-8 items-center justify-center rounded-full bg-[#3525cd] text-white">
                <Landmark size={16} />
              </div>
              <span className="font-headline text-lg font-bold text-[#0b1c30]">{BRAND.name}</span>
            </div>
          </div>

          {children}

          <div className="pointer-events-none absolute bottom-8 right-8 -z-10 h-24 w-24 rounded-full bg-[#3525cd]/5 blur-2xl" />
        </div>
      </main>
    </div>
  );
}

import React, { useState } from 'react';
import { Link, NavLink } from 'react-router-dom';
import {
  CreditCard,
  LayoutDashboard,
  Landmark,
  LogOut,
  Menu,
  Receipt,
  Send,
  ShieldCheck,
  Sliders,
  X,
} from 'lucide-react';
import { BRAND } from '../../config';
import { useAuth } from '../../context/AuthContext';
import { displayNameFromEmail, initialsFromEmail } from '../../lib/format';

const CUSTOMER_NAV = [
  { to: '/', label: 'Dashboard', icon: LayoutDashboard, end: true },
  { to: '/accounts', label: 'Accounts', icon: Landmark },
  { to: '/payments', label: 'Payments', icon: Send },
  { to: '/cards', label: 'Cards', icon: CreditCard },
  { to: '/billing', label: 'Bills & Subscriptions', icon: Receipt },
  { to: '/compliance', label: 'Identity & KYC', icon: ShieldCheck },
];

const ADMIN_NAV = [{ to: '/admin', label: 'Admin Console', icon: Sliders, end: true }];

function NavItems({ isAdmin, onNavigate }) {
  const items = isAdmin ? ADMIN_NAV : CUSTOMER_NAV;

  return (
    <nav className="space-y-1">
      {items.map(({ to, label, icon: Icon, end }) => (
        <NavLink
          key={to}
          to={to}
          end={end}
          onClick={onNavigate}
          className={({ isActive }) =>
            `flex items-center gap-3 rounded-xl px-4 py-3 text-sm transition-all ${
              isActive
                ? 'bg-white/10 font-semibold text-white'
                : 'text-slate-400 hover:bg-white/5 hover:text-white'
            }`
          }
        >
          <Icon size={18} />
          <span>{label}</span>
        </NavLink>
      ))}
    </nav>
  );
}

function SidebarContent({ user, isAdmin, onSignOut, onNavigate }) {
  return (
    <div className="flex h-full flex-col justify-between">
      <div>
        <Link to={isAdmin ? '/admin' : '/'} onClick={onNavigate} className="mb-10 flex items-center gap-3">
          <div className="flex h-9 w-9 items-center justify-center rounded-full bg-[#4f46e5] text-white shadow-md">
            <Landmark size={18} />
          </div>
          <div>
            <h1 className="font-headline text-lg font-extrabold leading-none text-white">
              {BRAND.name}
            </h1>
            <span className="text-[9px] font-semibold uppercase tracking-widest text-[#a59bff]/70">
              {BRAND.tagline}
            </span>
          </div>
        </Link>

        <NavItems isAdmin={isAdmin} onNavigate={onNavigate} />
      </div>

      <div className="mt-8 space-y-4 border-t border-slate-800 pt-6">
        <div className="flex items-center gap-3 rounded-2xl border border-slate-800 bg-slate-900/70 p-3">
          <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-[#4f46e5]/20 text-xs font-black text-[#a59bff]">
            {initialsFromEmail(user?.email)}
          </div>
          <div className="min-w-0">
            <p className="truncate text-xs font-bold text-white">
              {displayNameFromEmail(user?.email)}
            </p>
            <p className="truncate text-[10px] text-slate-400">{user?.email}</p>
          </div>
        </div>

        {isAdmin && (
          <p className="rounded-lg bg-[#4f46e5]/15 px-3 py-2 text-center text-[9px] font-black uppercase tracking-widest text-[#a59bff]">
            Administrator access
          </p>
        )}

        <button
          type="button"
          onClick={onSignOut}
          className="flex w-full cursor-pointer items-center justify-center gap-2 rounded-xl border border-slate-700 bg-slate-800 px-4 py-3 text-xs font-bold text-slate-300 transition-all hover:border-rose-800 hover:bg-rose-900 hover:text-white"
        >
          <LogOut size={14} />
          <span>Sign out</span>
        </button>
      </div>
    </div>
  );
}

export default function AppLayout({ eyebrow, title, description, actions, children }) {
  const { user, isAdmin, signOut } = useAuth();
  const [mobileNavOpen, setMobileNavOpen] = useState(false);

  const closeNav = () => setMobileNavOpen(false);

  return (
    <div dir="ltr" className="min-h-screen bg-[#f8f9ff] text-left font-sans text-[#0b1c30]">
      <div className="flex min-h-screen">
        <aside className="hidden w-64 shrink-0 bg-[#0b1c30] p-6 text-slate-300 md:block">
          <SidebarContent user={user} isAdmin={isAdmin} onSignOut={signOut} />
        </aside>

        {mobileNavOpen && (
          <div className="fixed inset-0 z-50 flex md:hidden">
            <div
              className="absolute inset-0 bg-[#0b1c30]/50 backdrop-blur-sm"
              onClick={closeNav}
              aria-hidden
            />
            <aside className="relative h-full w-72 overflow-y-auto bg-[#0b1c30] p-6 text-slate-300 shadow-2xl">
              <button
                type="button"
                onClick={closeNav}
                className="absolute right-5 top-6 cursor-pointer text-slate-400 hover:text-white"
                aria-label="Close navigation"
              >
                <X size={20} />
              </button>
              <SidebarContent
                user={user}
                isAdmin={isAdmin}
                onSignOut={signOut}
                onNavigate={closeNav}
              />
            </aside>
          </div>
        )}

        <main className="min-w-0 flex-1 bg-slate-50/50 p-5 sm:p-8 md:h-screen md:overflow-y-auto lg:p-10">
          <header className="mb-8 flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
            <div className="flex items-start gap-3">
              <button
                type="button"
                onClick={() => setMobileNavOpen(true)}
                className="mt-1 cursor-pointer rounded-lg border border-slate-200 bg-white p-2 text-[#0b1c30] shadow-sm md:hidden"
                aria-label="Open navigation"
              >
                <Menu size={18} />
              </button>
              <div>
                {eyebrow && (
                  <p className="text-[11px] font-bold uppercase leading-none tracking-widest text-slate-500">
                    {eyebrow}
                  </p>
                )}
                <h2 className="mt-1 font-headline text-2xl font-extrabold tracking-tight text-[#0b1c30]">
                  {title}
                </h2>
                {description && (
                  <p className="mt-1.5 max-w-2xl text-xs leading-relaxed text-[#464555]">
                    {description}
                  </p>
                )}
              </div>
            </div>

            {actions && <div className="flex flex-wrap gap-3">{actions}</div>}
          </header>

          {children}
        </main>
      </div>
    </div>
  );
}

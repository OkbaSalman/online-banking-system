import React, { useState } from 'react';
import { Eye, EyeOff } from 'lucide-react';

export function AuthField({
  id,
  label,
  icon: Icon,
  type = 'text',
  action,
  className = '',
  ...props
}) {
  const [focused, setFocused] = useState(false);
  const [revealed, setRevealed] = useState(false);
  const isPassword = type === 'password';

  return (
    <div className="space-y-2">
      <div className="flex items-center justify-between px-1">
        <label
          htmlFor={id}
          className="block text-left font-label text-xs font-semibold uppercase tracking-wider text-[#464555]"
        >
          {label}
        </label>
        {action}
      </div>

      <div
        className={`relative rounded-full transition-all duration-300 ${
          focused ? 'scale-[1.01] bg-white ring-2 ring-[#3525cd]/20' : 'bg-[#eff4ff]'
        }`}
      >
        {Icon && (
          <span className="absolute left-4 top-1/2 -translate-y-1/2 text-[#777587]">
            <Icon size={20} />
          </span>
        )}
        <input
          id={id}
          type={isPassword && revealed ? 'text' : type}
          dir="ltr"
          onFocus={() => setFocused(true)}
          onBlur={() => setFocused(false)}
          className={`w-full rounded-full border-0 bg-transparent py-4 font-body text-sm text-[#0b1c30] placeholder:text-[#777587]/50 focus:outline-none focus:ring-0 ${
            Icon ? 'pl-12' : 'pl-6'
          } ${isPassword ? 'pr-12 [&::-ms-reveal]:hidden [&::-ms-clear]:hidden [&::-webkit-credentials-auto-fill-button]:hidden' : 'pr-4'} ${className}`}
          {...props}
        />
        {isPassword && (
          <button
            type="button"
            onClick={() => setRevealed((value) => !value)}
            className="absolute right-4 top-1/2 -translate-y-1/2 cursor-pointer text-[#777587] transition-colors hover:text-[#0b1c30]"
            aria-label={revealed ? 'Hide password' : 'Show password'}
          >
            {revealed ? <EyeOff size={18} /> : <Eye size={18} />}
          </button>
        )}
      </div>
    </div>
  );
}

export function AuthSubmit({ loading, loadingLabel, children }) {
  return (
    <button
      type="submit"
      disabled={loading}
      className="group flex w-full cursor-pointer items-center justify-center gap-2 rounded-full bg-gradient-to-r from-[#3525cd] to-[#4f46e5] px-6 py-4 font-headline font-bold text-white shadow-[0_15px_30px_rgba(53,37,205,0.2)] transition-all duration-300 hover:-translate-y-0.5 hover:to-[#3525cd] hover:shadow-[0_20px_40px_rgba(53,37,205,0.35)] active:translate-y-0 disabled:cursor-not-allowed disabled:opacity-70"
    >
      {loading ? (
        <>
          <span className="h-5 w-5 animate-spin rounded-full border-2 border-white/40 border-t-white" />
          <span>{loadingLabel}</span>
        </>
      ) : (
        children
      )}
    </button>
  );
}

export function AuthLegal() {
  return (
    <p className="mt-12 text-center font-label text-[9px] uppercase tracking-[0.2em] text-[#777587] opacity-75">
      By continuing you agree to our{' '}
      <a href="#privacy" className="underline transition-colors hover:text-[#0b1c30]">
        Privacy Policy
      </a>{' '}
      and{' '}
      <a href="#terms" className="underline transition-colors hover:text-[#0b1c30]">
        Terms of Service
      </a>
    </p>
  );
}

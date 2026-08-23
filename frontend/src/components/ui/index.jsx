import React, { useEffect } from 'react';
import { AnimatePresence, motion } from 'motion/react';
import { AlertTriangle, Loader2, X } from 'lucide-react';

export function Card({ className = '', children, ...props }) {
  return (
    <div
      className={`rounded-[1.75rem] border border-slate-100 bg-white shadow-[0_10px_35px_rgba(11,28,48,0.05)] ${className}`}
      {...props}
    >
      {children}
    </div>
  );
}

export function SectionTitle({ icon: Icon, title, description, action }) {
  return (
    <div className="mb-5 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h3 className="flex items-center gap-2 font-headline text-lg font-extrabold text-[#0b1c30]">
          {Icon && <Icon size={18} className="text-[#4f46e5]" />}
          <span>{title}</span>
        </h3>
        {description && <p className="mt-1 text-xs text-[#464555]">{description}</p>}
      </div>
      {action}
    </div>
  );
}

const BUTTON_VARIANTS = {
  primary:
    'bg-[#0b1c30] text-white hover:bg-[#3525cd] shadow-[0_10px_25px_rgba(11,28,48,0.18)] hover:shadow-[0_14px_30px_rgba(53,37,205,0.28)]',
  accent:
    'bg-gradient-to-r from-[#3525cd] to-[#4f46e5] text-white hover:to-[#3525cd] shadow-[0_12px_28px_rgba(53,37,205,0.25)]',
  secondary: 'bg-white text-[#0b1c30] border border-slate-200 hover:bg-slate-50',
  subtle: 'bg-[#eff4ff] text-[#3525cd] hover:bg-[#e0e8ff]',
  danger: 'bg-rose-50 text-rose-700 hover:bg-rose-100 border border-rose-100',
  ghost: 'text-[#464555] hover:bg-slate-100',
};

const BUTTON_SIZES = {
  sm: 'px-3 py-2 text-[11px]',
  md: 'px-5 py-3 text-sm',
  lg: 'w-full px-6 py-4 text-sm',
};

export function Button({
  variant = 'primary',
  size = 'md',
  loading = false,
  icon: Icon,
  children,
  className = '',
  disabled,
  ...props
}) {
  return (
    <button
      disabled={disabled || loading}
      className={`inline-flex cursor-pointer items-center justify-center gap-2 rounded-xl font-bold transition-all duration-300 disabled:cursor-not-allowed disabled:opacity-60 ${BUTTON_VARIANTS[variant]} ${BUTTON_SIZES[size]} ${className}`}
      {...props}
    >
      {loading ? <Loader2 size={16} className="animate-spin" /> : Icon && <Icon size={16} />}
      {children}
    </button>
  );
}

export function Label({ children, htmlFor }) {
  return (
    <label
      htmlFor={htmlFor}
      className="mb-2 block font-label text-[11px] font-bold uppercase tracking-wider text-[#464555]"
    >
      {children}
    </label>
  );
}

const FIELD_CLASS =
  'w-full rounded-xl border-0 bg-[#eff4ff] p-4 text-sm text-[#0b1c30] placeholder:text-[#777587]/60 transition-all focus:bg-white focus:outline-none focus:ring-2 focus:ring-[#3525cd]/25 disabled:opacity-60';

export function TextField({ label, hint, error, className = '', id, ...props }) {
  const fieldId = id || props.name;
  return (
    <div>
      {label && <Label htmlFor={fieldId}>{label}</Label>}
      <input id={fieldId} className={`${FIELD_CLASS} ${className}`} {...props} />
      {hint && !error && <p className="mt-1.5 text-[11px] text-[#777587]">{hint}</p>}
      {error && <p className="mt-1.5 text-[11px] font-semibold text-rose-600">{error}</p>}
    </div>
  );
}

export function MoneyField({ label, hint, className = '', id, ...props }) {
  const fieldId = id || props.name;
  return (
    <div>
      {label && <Label htmlFor={fieldId}>{label}</Label>}
      <div className="relative">
        <span className="absolute left-4 top-1/2 -translate-y-1/2 font-bold text-[#777587]">$</span>
        <input
          id={fieldId}
          type="number"
          step="0.01"
          min="0"
          className={`${FIELD_CLASS} pl-8 font-semibold ${className}`}
          {...props}
        />
      </div>
      {hint && <p className="mt-1.5 text-[11px] text-[#777587]">{hint}</p>}
    </div>
  );
}

export function SelectField({ label, hint, children, className = '', id, ...props }) {
  const fieldId = id || props.name;
  return (
    <div>
      {label && <Label htmlFor={fieldId}>{label}</Label>}
      <select id={fieldId} className={`${FIELD_CLASS} cursor-pointer ${className}`} {...props}>
        {children}
      </select>
      {hint && <p className="mt-1.5 text-[11px] text-[#777587]">{hint}</p>}
    </div>
  );
}

export function TextAreaField({ label, hint, className = '', id, ...props }) {
  const fieldId = id || props.name;
  return (
    <div>
      {label && <Label htmlFor={fieldId}>{label}</Label>}
      <textarea id={fieldId} rows={3} className={`${FIELD_CLASS} resize-none ${className}`} {...props} />
      {hint && <p className="mt-1.5 text-[11px] text-[#777587]">{hint}</p>}
    </div>
  );
}

const BADGE_TONES = {
  neutral: 'bg-slate-100 text-slate-600',
  indigo: 'bg-indigo-50 text-[#3525cd]',
  amber: 'bg-amber-100 text-amber-800',
  emerald: 'bg-emerald-50 text-emerald-700',
  rose: 'bg-rose-50 text-rose-700',
  dark: 'bg-[#0b1c30] text-white',
};

export function Badge({ tone = 'neutral', children, className = '' }) {
  return (
    <span
      className={`inline-flex items-center gap-1 rounded-md px-2 py-0.5 text-[9px] font-black uppercase tracking-widest ${BADGE_TONES[tone]} ${className}`}
    >
      {children}
    </span>
  );
}

/** Maps the protobuf status enums the gateway returns onto badge colours. */
export function StatusBadge({ status }) {
  const value = String(status || '').toUpperCase();
  const tone = value.includes('COMPLETED') || value.includes('APPROVED') || value.includes('ACTIVE')
    ? 'emerald'
    : value.includes('PENDING')
      ? 'amber'
      : value.includes('FAILED') || value.includes('REJECTED') || value.includes('BLOCKED')
        ? 'rose'
        : value.includes('FROZEN') || value.includes('PAUSED')
          ? 'indigo'
          : 'neutral';

  const label = value
    .replace(/^(TRANSFER_STATUS_|KYC_STATUS_|INVITATION_STATUS_|CARD_STATUS_)/, '')
    .replace(/_/g, ' ');

  return <Badge tone={tone}>{label || 'Unknown'}</Badge>;
}

export function Spinner({ label }) {
  return (
    <div className="flex flex-col items-center justify-center gap-2 p-12">
      <Loader2 size={26} className="animate-spin text-[#4f46e5]" />
      {label && <p className="text-xs font-medium text-[#777587]">{label}</p>}
    </div>
  );
}

export function EmptyState({ icon: Icon, title, description, action }) {
  return (
    <div className="flex flex-col items-center justify-center gap-3 rounded-2xl border border-dashed border-slate-200 bg-slate-50/60 p-10 text-center">
      {Icon && (
        <div className="flex h-11 w-11 items-center justify-center rounded-full bg-white text-[#4f46e5] shadow-sm">
          <Icon size={20} />
        </div>
      )}
      <p className="text-sm font-bold text-[#0b1c30]">{title}</p>
      {description && <p className="max-w-sm text-xs leading-relaxed text-[#777587]">{description}</p>}
      {action}
    </div>
  );
}

export function ErrorNotice({ children }) {
  if (!children) return null;
  return (
    <div className="flex items-start gap-2 rounded-xl border-l-4 border-rose-500 bg-rose-50 p-3 text-xs font-medium leading-relaxed text-rose-800">
      <AlertTriangle size={14} className="mt-0.5 shrink-0" />
      <span className="min-w-0 break-words">{children}</span>
    </div>
  );
}

export function SuccessNotice({ children }) {
  if (!children) return null;
  return (
    <div className="rounded-xl border-l-4 border-emerald-500 bg-emerald-50 p-3 text-xs font-medium leading-relaxed text-emerald-800">
      {children}
    </div>
  );
}

export function InfoNotice({ icon: Icon, title, children }) {
  return (
    <div className="flex items-start gap-3 rounded-2xl bg-[#e5eeff] p-4 text-left">
      {Icon && (
        <div className="mt-0.5 shrink-0 text-[#3525cd]">
          <Icon size={20} />
        </div>
      )}
      <p className="font-body text-xs leading-relaxed text-[#464555]">
        {title && <span className="font-bold text-[#0b1c30]">{title} </span>}
        {children}
      </p>
    </div>
  );
}

export function Modal({ open, onClose, title, description, children, size = 'md', elevated = false }) {
  useEffect(() => {
    if (!open) return undefined;
    const onKeyDown = (event) => {
      if (event.key !== 'Escape') return;
      // Nested/stacked modals: only the topmost one should close.
      if (elevated || !document.querySelector('[data-modal-elevated="true"]')) {
        onClose?.();
      }
    };
    window.addEventListener('keydown', onKeyDown);
    document.body.style.overflow = 'hidden';
    return () => {
      window.removeEventListener('keydown', onKeyDown);
      document.body.style.overflow = '';
    };
  }, [open, onClose, elevated]);

  const widths = {
    sm: 'max-w-sm',
    md: 'max-w-md',
    lg: 'max-w-lg',
    xl: 'max-w-2xl',
    '2xl': 'max-w-4xl',
  };

  return (
    <AnimatePresence>
      {open && (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          data-modal-elevated={elevated ? 'true' : undefined}
          className={`fixed inset-0 flex items-center justify-center bg-[#0b1c30]/45 p-4 backdrop-blur-sm ${
            elevated ? 'z-[60]' : 'z-50'
          }`}
          onMouseDown={(event) => event.target === event.currentTarget && onClose?.()}
        >
          <motion.div
            initial={{ opacity: 0, y: 18, scale: 0.97 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: 12, scale: 0.98 }}
            transition={{ duration: 0.22, ease: 'easeOut' }}
            className={`relative max-h-[90vh] w-full ${widths[size]} overflow-y-auto rounded-[1.75rem] bg-white p-6 text-left shadow-[0_40px_90px_rgba(11,28,48,0.28)] sm:p-8`}
          >
            <button
              type="button"
              onClick={onClose}
              className="absolute right-6 top-6 cursor-pointer text-slate-400 transition-colors hover:text-slate-700"
              aria-label="Close dialog"
            >
              <X size={20} />
            </button>
            {title && (
              <h3 className="pr-8 font-headline text-xl font-black text-[#0b1c30]">{title}</h3>
            )}
            {description && <p className="mb-6 mt-2 text-xs text-[#464555]">{description}</p>}
            {children}
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}

export function CopyableId({ value, className = '' }) {
  return (
    <span className={`select-all font-mono text-[10px] text-slate-400 ${className}`}>{value}</span>
  );
}

export function DataRow({ label, children }) {
  return (
    <div className="flex items-start justify-between gap-4 py-2">
      <span className="shrink-0 text-[11px] font-bold uppercase tracking-wider text-[#777587]">
        {label}
      </span>
      <span className="min-w-0 break-words text-right text-xs font-semibold text-[#0b1c30]">
        {children}
      </span>
    </div>
  );
}

import React, { createContext, useCallback, useContext, useMemo, useState } from 'react';
import { AnimatePresence, motion } from 'motion/react';
import { AlertTriangle, CheckCircle2, Info, X } from 'lucide-react';

const ToastContext = createContext(null);

const TONES = {
  success: {
    icon: CheckCircle2,
    ring: 'border-emerald-200',
    accent: 'text-emerald-600',
    bar: 'bg-emerald-500',
  },
  error: {
    icon: AlertTriangle,
    ring: 'border-rose-200',
    accent: 'text-rose-600',
    bar: 'bg-rose-500',
  },
  info: {
    icon: Info,
    ring: 'border-indigo-200',
    accent: 'text-[#3525cd]',
    bar: 'bg-[#3525cd]',
  },
};

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);

  const dismiss = useCallback((id) => {
    setToasts((current) => current.filter((toast) => toast.id !== id));
  }, []);

  const push = useCallback(
    (tone, title, description) => {
      const id = `${Date.now()}-${Math.random()}`;
      setToasts((current) => [...current, { id, tone, title, description }]);
      setTimeout(() => dismiss(id), 6000);
    },
    [dismiss],
  );

  const value = useMemo(
    () => ({
      success: (title, description) => push('success', title, description),
      error: (title, description) => push('error', title, description),
      info: (title, description) => push('info', title, description),
    }),
    [push],
  );

  return (
    <ToastContext.Provider value={value}>
      {children}
      <div className="fixed bottom-6 right-6 z-[100] flex w-[min(92vw,24rem)] flex-col gap-3">
        <AnimatePresence initial={false}>
          {toasts.map((toast) => {
            const tone = TONES[toast.tone] || TONES.info;
            const Icon = tone.icon;
            return (
              <motion.div
                key={toast.id}
                layout
                initial={{ opacity: 0, y: 16, scale: 0.97 }}
                animate={{ opacity: 1, y: 0, scale: 1 }}
                exit={{ opacity: 0, x: 24, scale: 0.97 }}
                transition={{ duration: 0.22, ease: 'easeOut' }}
                className={`relative overflow-hidden rounded-2xl border ${tone.ring} bg-white p-4 pl-5 shadow-[0_20px_45px_rgba(11,28,48,0.14)]`}
              >
                <span className={`absolute left-0 top-0 h-full w-1 ${tone.bar}`} />
                <div className="flex items-start gap-3">
                  <Icon size={18} className={`mt-0.5 shrink-0 ${tone.accent}`} />
                  <div className="min-w-0 flex-1">
                    <p className="text-sm font-bold text-[#0b1c30]">{toast.title}</p>
                    {toast.description && (
                      <p className="mt-1 text-xs leading-relaxed text-[#464555]">
                        {toast.description}
                      </p>
                    )}
                  </div>
                  <button
                    type="button"
                    onClick={() => dismiss(toast.id)}
                    className="shrink-0 cursor-pointer text-slate-300 transition-colors hover:text-slate-600"
                    aria-label="Dismiss notification"
                  >
                    <X size={16} />
                  </button>
                </div>
              </motion.div>
            );
          })}
        </AnimatePresence>
      </div>
    </ToastContext.Provider>
  );
}

export function useToast() {
  const context = useContext(ToastContext);
  if (!context) throw new Error('useToast must be used inside a ToastProvider');
  return context;
}

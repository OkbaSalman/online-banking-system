import React, { useEffect, useState } from 'react';
import { motion } from 'motion/react';
import { ArrowLeft, ArrowRight, Lock, Mail, ShieldCheck } from 'lucide-react';
import { AuthField, AuthLegal, AuthSubmit } from './AuthField';
import { ErrorNotice, InfoNotice, SuccessNotice } from '../ui';
import { register, resendVerification, verifyEmail } from '../../services/authService';
import { useAuth } from '../../context/AuthContext';

const fade = {
  initial: { opacity: 0, y: 15 },
  animate: { opacity: 1, y: 0 },
  exit: { opacity: 0, y: -15 },
  transition: { duration: 0.4, ease: 'easeOut' },
};

export default function RegisterForm({ onToggleForm, presetEmail }) {
  const { signIn } = useAuth();

  const [email, setEmail] = useState(presetEmail || '');
  const [password, setPassword] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [notice, setNotice] = useState(null);

  // Set as soon as the account exists, or when login reported an unverified user.
  const [awaitingVerification, setAwaitingVerification] = useState(Boolean(presetEmail));
  const [code, setCode] = useState('');

  useEffect(() => {
    if (presetEmail) {
      setEmail(presetEmail);
      setAwaitingVerification(true);
      setNotice('This account still needs email verification. Enter the code we sent you.');
    }
  }, [presetEmail]);

  const handleRegister = async (event) => {
    event.preventDefault();
    setError(null);
    setNotice(null);

    if (!email.includes('@')) {
      setError('Enter a valid email address.');
      return;
    }
    if (password.length < 8) {
      setError('Choose a password of at least 8 characters.');
      return;
    }

    setSubmitting(true);
    try {
      await register(email.trim(), password);
      setNotice(`We sent a 6-digit verification code to ${email.trim()}.`);
      setAwaitingVerification(true);
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  const handleVerify = async (event) => {
    event.preventDefault();
    setError(null);
    setSubmitting(true);

    try {
      const result = await verifyEmail(email.trim(), code.trim());
      if (!result.verified) {
        setError('That code was not accepted. Request a new one and try again.');
        return;
      }
      if (password) {
        // Straight into the app when the password from this session is still in memory.
        await signIn(email.trim(), password);
      } else {
        setNotice('Email verified. You can sign in now.');
        setAwaitingVerification(false);
        onToggleForm?.();
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  const handleResend = async () => {
    setError(null);
    setNotice(null);
    try {
      await resendVerification(email.trim());
      setNotice('A new verification code is on its way.');
    } catch (err) {
      setError(err.message);
    }
  };

  if (awaitingVerification) {
    return (
      <motion.div {...fade} className="mx-auto w-full max-w-md">
        <button
          type="button"
          onClick={() => {
            setAwaitingVerification(false);
            setError(null);
            setNotice(null);
          }}
          className="mb-6 flex cursor-pointer items-center gap-2 text-xs font-bold text-slate-500 transition-colors hover:text-[#0b1c30]"
        >
          <ArrowLeft size={16} />
          <span>BACK</span>
        </button>

        <div className="mb-8 text-left">
          <h2 className="mb-3 font-display text-3xl font-extrabold tracking-tight text-[#0b1c30]">
            Verify your email
          </h2>
          <p className="font-body text-sm text-[#464555]">
            We sent a code to <strong className="text-[#0b1c30]">{email}</strong>. It confirms the
            address before any account can be opened.
          </p>
        </div>

        {notice && (
          <div className="mb-6">
            <SuccessNotice>{notice}</SuccessNotice>
          </div>
        )}

        <form onSubmit={handleVerify} className="space-y-6">
          <ErrorNotice>{error}</ErrorNotice>

          <div className="space-y-2">
            <label
              htmlFor="verify_code"
              className="block text-left font-label text-xs font-semibold uppercase tracking-wider text-[#464555]"
            >
              6-digit verification code
            </label>
            <div className="rounded-full bg-[#eff4ff] transition-all duration-300 focus-within:scale-[1.01] focus-within:ring-2 focus-within:ring-[#3525cd]/20">
              <input
                id="verify_code"
                type="text"
                inputMode="numeric"
                maxLength={6}
                value={code}
                onChange={(event) => setCode(event.target.value.replace(/\D/g, ''))}
                placeholder="123456"
                className="w-full rounded-full border-0 bg-transparent px-6 py-4 text-center font-mono text-lg font-black tracking-[0.4em] text-[#0b1c30] focus:outline-none focus:ring-0"
                dir="ltr"
                required
              />
            </div>
          </div>

          <AuthSubmit loading={submitting} loadingLabel="Confirming…">
            <span>Verify email</span>
            <ArrowRight size={20} />
          </AuthSubmit>
        </form>

        <div className="mt-8 text-center font-body text-xs text-slate-500">
          <p className="mb-2">Didn&apos;t get the email?</p>
          <button
            type="button"
            onClick={handleResend}
            className="cursor-pointer font-bold text-[#3525cd] hover:underline"
          >
            Send a new code
          </button>
        </div>
      </motion.div>
    );
  }

  return (
    <motion.div {...fade} className="mx-auto w-full max-w-md">
      <div className="mb-8 text-left">
        <h2 className="mb-3 font-display text-3xl font-extrabold tracking-tight text-[#0b1c30]">
          Open an account
        </h2>
        <p className="font-body text-sm text-[#464555] md:text-base">
          Start with an email and a password. You can add checking and savings accounts once you
          are in.
        </p>
      </div>

      {notice && (
        <div className="mb-5">
          <SuccessNotice>{notice}</SuccessNotice>
        </div>
      )}

      <form onSubmit={handleRegister} className="space-y-5">
        <ErrorNotice>{error}</ErrorNotice>

        <AuthField
          id="register_email"
          label="Email address"
          icon={Mail}
          type="email"
          value={email}
          onChange={(event) => setEmail(event.target.value)}
          placeholder="you@example.com"
          autoComplete="email"
          required
        />

        <AuthField
          id="register_password"
          label="Password"
          icon={Lock}
          type="password"
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          placeholder="At least 8 characters"
          autoComplete="new-password"
          required
        />

        <InfoNotice icon={ShieldCheck} title="What happens next:">
          We email a 6-digit code to confirm the address. Moving money also requires identity
          verification, which you can complete from the Identity &amp; KYC page.
        </InfoNotice>

        <AuthSubmit loading={submitting} loadingLabel="Creating account…">
          <span>Create account</span>
          <ArrowRight size={20} className="transition-transform group-hover:translate-x-1" />
        </AuthSubmit>
      </form>

      <div className="mt-10 text-center">
        <p className="mb-3 font-body text-[#464555]">Already have an account?</p>
        <button
          type="button"
          onClick={onToggleForm}
          className="group inline-flex cursor-pointer items-center gap-2 font-bold text-[#3525cd] transition-colors hover:text-[#4f46e5]"
        >
          <span>Sign in</span>
          <span className="h-1.5 w-1.5 rounded-full bg-[#3525cd]/20 transition-colors group-hover:bg-[#3525cd]" />
        </button>
      </div>

      <AuthLegal />
    </motion.div>
  );
}

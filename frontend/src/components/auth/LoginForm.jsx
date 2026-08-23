import React, { useState } from 'react';
import { motion } from 'motion/react';
import { ArrowLeft, ArrowRight, KeyRound, Lock, Mail, MailCheck } from 'lucide-react';
import { AuthField, AuthLegal, AuthSubmit } from './AuthField';
import { ErrorNotice, InfoNotice, SuccessNotice } from '../ui';
import { useAuth } from '../../context/AuthContext';
import { forgotPassword, resetPassword } from '../../services/authService';
import { BRAND } from '../../config';

const fade = {
  initial: { opacity: 0, y: 15 },
  animate: { opacity: 1, y: 0 },
  exit: { opacity: 0, y: -15 },
  transition: { duration: 0.4, ease: 'easeOut' },
};

export default function LoginForm({ onToggleForm, onNeedsVerification }) {
  const { signIn } = useAuth();

  const [view, setView] = useState('login');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [notice, setNotice] = useState(null);

  const [resetToken, setResetToken] = useState('');
  const [newPassword, setNewPassword] = useState('');

  const handleLogin = async (event) => {
    event.preventDefault();
    setError(null);
    setNotice(null);
    setSubmitting(true);

    try {
      const result = await signIn(email.trim(), password);
      if (result.verificationRequired) {
        onNeedsVerification?.(email.trim());
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  const handleForgot = async (event) => {
    event.preventDefault();
    setError(null);
    setNotice(null);
    setSubmitting(true);

    try {
      await forgotPassword(email.trim());
      setNotice(
        'If that email is registered, a reset link has been sent. Paste the token from the email below.',
      );
      setView('reset');
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  const handleReset = async (event) => {
    event.preventDefault();
    setError(null);
    setSubmitting(true);

    if (newPassword.length < 8) {
      setError('Choose a password of at least 8 characters.');
      setSubmitting(false);
      return;
    }

    try {
      await resetPassword(resetToken.trim(), newPassword);
      setPassword('');
      setNewPassword('');
      setResetToken('');
      setNotice('Password updated. Sign in with your new password.');
      setView('login');
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  if (view === 'forgot') {
    return (
      <motion.div {...fade} className="mx-auto w-full max-w-md">
        <button
          type="button"
          onClick={() => {
            setView('login');
            setError(null);
          }}
          className="mb-6 flex cursor-pointer items-center gap-2 text-xs font-bold text-slate-500 transition-colors hover:text-[#0b1c30]"
        >
          <ArrowLeft size={16} />
          <span>BACK TO SIGN IN</span>
        </button>

        <div className="mb-8 text-left">
          <h2 className="mb-3 font-display text-3xl font-extrabold tracking-tight text-[#0b1c30]">
            Reset your password
          </h2>
          <p className="font-body text-sm text-[#464555]">
            Enter the email on your account and we will send you a reset token.
          </p>
        </div>

        <form onSubmit={handleForgot} className="space-y-6">
          <ErrorNotice>{error}</ErrorNotice>

          <AuthField
            id="forgot_email"
            label="Email address"
            icon={Mail}
            type="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            placeholder="you@example.com"
            required
          />

          <AuthSubmit loading={submitting} loadingLabel="Sending…">
            Send reset token
          </AuthSubmit>
        </form>
      </motion.div>
    );
  }

  if (view === 'reset') {
    return (
      <motion.div {...fade} className="mx-auto w-full max-w-md">
        <button
          type="button"
          onClick={() => setView('forgot')}
          className="mb-6 flex cursor-pointer items-center gap-2 text-xs font-bold text-slate-500 transition-colors hover:text-[#0b1c30]"
        >
          <ArrowLeft size={16} />
          <span>REQUEST A NEW TOKEN</span>
        </button>

        <div className="mb-8 text-left">
          <h2 className="mb-3 font-display text-3xl font-extrabold tracking-tight text-[#0b1c30]">
            Choose a new password
          </h2>
          <p className="font-body text-sm text-[#464555]">
            Paste the token from the reset email, then pick the password you want to use.
          </p>
        </div>

        {notice && (
          <div className="mb-6">
            <SuccessNotice>{notice}</SuccessNotice>
          </div>
        )}

        <form onSubmit={handleReset} className="space-y-5">
          <ErrorNotice>{error}</ErrorNotice>

          <AuthField
            id="reset_token"
            label="Reset token"
            icon={KeyRound}
            value={resetToken}
            onChange={(event) => setResetToken(event.target.value)}
            placeholder="Paste the token from your email"
            className="font-mono"
            required
          />

          <AuthField
            id="reset_password"
            label="New password"
            icon={Lock}
            type="password"
            value={newPassword}
            onChange={(event) => setNewPassword(event.target.value)}
            placeholder="••••••••••••"
            required
          />

          <AuthSubmit loading={submitting} loadingLabel="Updating…">
            Update password
          </AuthSubmit>
        </form>
      </motion.div>
    );
  }

  return (
    <motion.div {...fade} className="mx-auto w-full max-w-md">
      <div className="mb-8 text-left">
        <h2 className="mb-3 font-display text-3xl font-extrabold tracking-tight text-[#0b1c30]">
          Welcome back
        </h2>
        <p className="font-body text-sm text-[#464555] md:text-base">
          Sign in to reach your accounts, transfers and cards.
        </p>
      </div>

      {notice && (
        <div className="mb-5">
          <SuccessNotice>{notice}</SuccessNotice>
        </div>
      )}

      <form onSubmit={handleLogin} className="space-y-5">
        <ErrorNotice>{error}</ErrorNotice>

        <AuthField
          id="login_email"
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
          id="login_password"
          label="Password"
          icon={Lock}
          type="password"
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          placeholder="••••••••••••"
          autoComplete="current-password"
          required
          action={
            <button
              type="button"
              onClick={() => {
                setView('forgot');
                setError(null);
                setNotice(null);
              }}
              className="cursor-pointer text-xs font-semibold text-[#3525cd] hover:underline"
            >
              Forgot password?
            </button>
          }
        />

        <InfoNotice icon={MailCheck} title="Verified sign-in:">
          Sessions last 15 minutes and refresh automatically in the background. Signing out revokes
          the refresh token on the server.
        </InfoNotice>

        <AuthSubmit loading={submitting} loadingLabel="Signing in…">
          <span>Sign in</span>
          <ArrowRight size={20} className="transition-transform group-hover:translate-x-1" />
        </AuthSubmit>
      </form>

      <div className="mt-10 text-center">
        <p className="mb-3 font-body text-[#464555]">
          New to <span className="font-semibold text-[#0b1c30]">{BRAND.name}</span>?
        </p>
        <button
          type="button"
          onClick={onToggleForm}
          className="group inline-flex cursor-pointer items-center gap-2 font-bold text-[#3525cd] transition-colors hover:text-[#4f46e5]"
        >
          <span>Open an account</span>
          <span className="h-1.5 w-1.5 rounded-full bg-[#3525cd]/20 transition-colors group-hover:bg-[#3525cd]" />
        </button>
      </div>

      <AuthLegal />
    </motion.div>
  );
}

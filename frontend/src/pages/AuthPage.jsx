import React, { useState } from 'react';
import { AnimatePresence } from 'motion/react';
import AuthShell from '../components/auth/AuthShell';
import LoginForm from '../components/auth/LoginForm';
import RegisterForm from '../components/auth/RegisterForm';

export default function AuthPage() {
  const [view, setView] = useState('login');
  const [unverifiedEmail, setUnverifiedEmail] = useState('');

  return (
    <AuthShell>
      <AnimatePresence mode="wait">
        {view === 'login' ? (
          <LoginForm
            key="login"
            onToggleForm={() => {
              setUnverifiedEmail('');
              setView('register');
            }}
            onNeedsVerification={(email) => {
              setUnverifiedEmail(email);
              setView('register');
            }}
          />
        ) : (
          <RegisterForm
            key="register"
            presetEmail={unverifiedEmail}
            onToggleForm={() => {
              setUnverifiedEmail('');
              setView('login');
            }}
          />
        )}
      </AnimatePresence>
    </AuthShell>
  );
}

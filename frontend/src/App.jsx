import React from 'react';
import { HashRouter, Navigate, Route, Routes } from 'react-router-dom';
import { Landmark } from 'lucide-react';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ToastProvider } from './context/ToastContext';
import AuthPage from './pages/AuthPage';
import DashboardPage from './pages/DashboardPage';
import AccountsPage from './pages/AccountsPage';
import PaymentsPage from './pages/PaymentsPage';
import CardsPage from './pages/CardsPage';
import BillingPage from './pages/BillingPage';
import CompliancePage from './pages/CompliancePage';
import AdminPage from './pages/AdminPage';
import { BRAND } from './config';

function SplashScreen() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-4 bg-[#f8f9ff]">
      <div className="flex h-12 w-12 animate-pulse items-center justify-center rounded-full bg-[#4f46e5] text-white">
        <Landmark size={22} />
      </div>
      <p className="font-headline text-sm font-bold text-[#0b1c30]">{BRAND.name}</p>
      <p className="text-xs text-[#777587]">Restoring your session…</p>
    </div>
  );
}

function RequireAuth({ children }) {
  const { status } = useAuth();
  if (status === 'loading') return <SplashScreen />;
  if (status !== 'authenticated') return <Navigate to="/login" replace />;
  return children;
}

function RequireAdmin({ children }) {
  const { isAdmin } = useAuth();
  if (!isAdmin) return <Navigate to="/" replace />;
  return children;
}

/** Admins only use the admin console — keep them out of retail screens. */
function RequireCustomer({ children }) {
  const { isAdmin } = useAuth();
  if (isAdmin) return <Navigate to="/admin" replace />;
  return children;
}

function homeFor(status, isAdmin) {
  if (status !== 'authenticated') return '/login';
  return isAdmin ? '/admin' : '/';
}

function AppRoutes() {
  const { status, isAdmin } = useAuth();

  if (status === 'loading') return <SplashScreen />;

  return (
    <Routes>
      <Route
        path="/login"
        element={
          status === 'authenticated' ? (
            <Navigate to={homeFor(status, isAdmin)} replace />
          ) : (
            <AuthPage />
          )
        }
      />
      <Route
        path="/"
        element={
          <RequireAuth>
            <RequireCustomer>
              <DashboardPage />
            </RequireCustomer>
          </RequireAuth>
        }
      />
      <Route
        path="/accounts"
        element={
          <RequireAuth>
            <RequireCustomer>
              <AccountsPage />
            </RequireCustomer>
          </RequireAuth>
        }
      />
      <Route
        path="/payments"
        element={
          <RequireAuth>
            <RequireCustomer>
              <PaymentsPage />
            </RequireCustomer>
          </RequireAuth>
        }
      />
      <Route
        path="/cards"
        element={
          <RequireAuth>
            <RequireCustomer>
              <CardsPage />
            </RequireCustomer>
          </RequireAuth>
        }
      />
      <Route
        path="/billing"
        element={
          <RequireAuth>
            <RequireCustomer>
              <BillingPage />
            </RequireCustomer>
          </RequireAuth>
        }
      />
      <Route
        path="/compliance"
        element={
          <RequireAuth>
            <RequireCustomer>
              <CompliancePage />
            </RequireCustomer>
          </RequireAuth>
        }
      />
      <Route
        path="/admin"
        element={
          <RequireAuth>
            <RequireAdmin>
              <AdminPage />
            </RequireAdmin>
          </RequireAuth>
        }
      />
      <Route path="*" element={<Navigate to={homeFor(status, isAdmin)} replace />} />
    </Routes>
  );
}

export default function App() {
  return (
    <HashRouter>
      <ToastProvider>
        <AuthProvider>
          <AppRoutes />
        </AuthProvider>
      </ToastProvider>
    </HashRouter>
  );
}

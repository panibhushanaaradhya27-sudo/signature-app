import React, { useEffect, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { FileSignature } from 'lucide-react';
import { Dashboard } from './pages/Dashboard.jsx';
import { PublicSign } from './pages/PublicSign.jsx';
import { Login } from './pages/Login.jsx';
import './styles.css';

function App() {
  const [session, setSession] = useState(() => {
    const saved = localStorage.getItem('signature-session');
    return saved ? JSON.parse(saved) : null;
  });

  useEffect(() => {
    if (session) {
      localStorage.setItem('signature-session', JSON.stringify(session));
    } else {
      localStorage.removeItem('signature-session');
    }
  }, [session]);

  const isPublicSigning = window.location.pathname.startsWith('/sign/');

  if (isPublicSigning) {
    return <PublicSign />;
  }

  return (
    <div className="min-h-screen bg-zinc-50 text-ink">
      <header className="border-b bg-white">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-4 py-3">
          <div className="flex items-center gap-2 font-semibold">
            <FileSignature className="h-6 w-6 text-mint" />
            <span>Signature App</span>
          </div>
          {session && (
            <button className="btn-secondary" onClick={() => setSession(null)}>
              Logout
            </button>
          )}
        </div>
      </header>
      {session ? <Dashboard session={session} /> : <Login onSession={setSession} />}
    </div>
  );
}

createRoot(document.getElementById('root')).render(<App />);

import React, { useState } from 'react';
import { Lock, Mail, UserRound } from 'lucide-react';
import { api } from '../utils/api.js';

export function Login({ onSession }) {
  const [mode, setMode] = useState('login');
  const [form, setForm] = useState({ name: '', email: '', password: '' });
  const [error, setError] = useState('');

  async function submit(event) {
    event.preventDefault();
    setError('');
    try {
      const session = await api(`/api/auth/${mode}`, {
        method: 'POST',
        body: JSON.stringify(form)
      });
      onSession(session);
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <main className="mx-auto grid min-h-[calc(100vh-57px)] max-w-7xl items-center gap-8 px-4 py-8 lg:grid-cols-[1fr_420px]">
      <section>
        <p className="text-sm font-semibold uppercase tracking-wide text-mint">Enterprise Java portfolio project</p>
        <h1 className="mt-3 max-w-3xl text-4xl font-bold leading-tight md:text-6xl">
          Secure document signing with audit-ready PDFs.
        </h1>
        <p className="mt-5 max-w-2xl text-lg text-zinc-600">
          Upload PDFs, place signature fields, share tokenized signing links, generate signed copies, and keep a traceable audit history.
        </p>
      </section>

      <form onSubmit={submit} className="rounded-lg border bg-white p-5 shadow-sm">
        <div className="mb-5 grid grid-cols-2 rounded-md bg-zinc-100 p-1">
          <button type="button" className={mode === 'login' ? 'tab-active' : 'tab'} onClick={() => setMode('login')}>
            Login
          </button>
          <button type="button" className={mode === 'register' ? 'tab-active' : 'tab'} onClick={() => setMode('register')}>
            Register
          </button>
        </div>

        {mode === 'register' && (
          <label className="field">
            <span>Name</span>
            <div className="input-row">
              <UserRound className="h-4 w-4" />
              <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
            </div>
          </label>
        )}

        <label className="field">
          <span>Email</span>
          <div className="input-row">
            <Mail className="h-4 w-4" />
            <input type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} required />
          </div>
        </label>

        <label className="field">
          <span>Password</span>
          <div className="input-row">
            <Lock className="h-4 w-4" />
            <input type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} required />
          </div>
        </label>

        {error && <p className="mb-3 rounded-md bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p>}

        <button className="btn-primary w-full" type="submit">
          {mode === 'login' ? 'Login' : 'Create account'}
        </button>
      </form>
    </main>
  );
}

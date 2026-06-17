import React, { useEffect, useState } from 'react';
import { CheckCircle2, FileSignature, XCircle } from 'lucide-react';
import { api, API_BASE } from '../utils/api.js';

export function PublicSign() {
  const token = window.location.pathname.split('/').pop();
  const [request, setRequest] = useState(null);
  const [name, setName] = useState('');
  const [reason, setReason] = useState('');
  const [message, setMessage] = useState('');

  useEffect(() => {
    api(`/api/public/sign/${token}`)
      .then((data) => {
        setRequest(data);
        setName(data.signerName || '');
      })
      .catch((err) => setMessage(err.message));
  }, [token]);

  async function sign() {
    try {
      const updated = await api(`/api/public/sign/${token}`, {
        method: 'POST',
        body: JSON.stringify({ signerName: name })
      });
      setRequest(updated);
      setMessage('Document signed successfully.');
    } catch (err) {
      setMessage(err.message);
    }
  }

  async function reject() {
    try {
      const updated = await api(`/api/public/sign/${token}/reject`, {
        method: 'POST',
        body: JSON.stringify({ reason })
      });
      setRequest(updated);
      setMessage('Document rejected.');
    } catch (err) {
      setMessage(err.message);
    }
  }

  return (
    <div className="min-h-screen bg-zinc-50">
      <header className="border-b bg-white">
        <div className="mx-auto flex max-w-6xl items-center gap-2 px-4 py-3 font-semibold">
          <FileSignature className="h-6 w-6 text-mint" />
          Signature request
        </div>
      </header>
      <main className="mx-auto grid max-w-6xl gap-4 px-4 py-4 lg:grid-cols-[1fr_340px]">
        <section className="rounded-lg border bg-white p-3">
          <iframe className="h-[78vh] w-full rounded-md border" src={`${API_BASE}/api/public/sign/${token}/file`} title="Document preview" />
        </section>
        <aside className="rounded-lg border bg-white p-4">
          {request ? (
            <>
              <h1 className="text-xl font-semibold">Review and sign</h1>
              <p className="mt-2 text-sm text-zinc-500">Request for {request.signerEmail}</p>
              <p className="mt-2 rounded-full bg-zinc-100 px-3 py-1 text-sm">{request.status}</p>
              <label className="field mt-4">
                <span>Legal signer name</span>
                <input className="plain-input" value={name} onChange={(e) => setName(e.target.value)} />
              </label>
              <button className="btn-primary mt-2 flex w-full items-center justify-center gap-2" onClick={sign} disabled={request.status !== 'PENDING'}>
                <CheckCircle2 className="h-4 w-4" />
                Sign document
              </button>
              <label className="field mt-4">
                <span>Rejection reason</span>
                <textarea className="plain-input min-h-24" value={reason} onChange={(e) => setReason(e.target.value)} />
              </label>
              <button className="btn-secondary mt-2 flex w-full items-center justify-center gap-2" onClick={reject} disabled={request.status !== 'PENDING'}>
                <XCircle className="h-4 w-4" />
                Reject
              </button>
              {message && <p className="mt-3 rounded-md bg-zinc-100 px-3 py-2 text-sm">{message}</p>}
            </>
          ) : (
            <p>{message || 'Loading request...'}</p>
          )}
        </aside>
      </main>
    </div>
  );
}

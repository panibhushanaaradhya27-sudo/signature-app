import React, { useEffect, useMemo, useState } from 'react';
import { CheckCircle2, Clock3, FileUp, Search, XCircle } from 'lucide-react';
import { api } from '../utils/api.js';
import { DocumentWorkspace } from '../components/DocumentWorkspace.jsx';

const statusIcon = {
  DRAFT: Clock3,
  PENDING: Clock3,
  SIGNED: CheckCircle2,
  REJECTED: XCircle
};

export function Dashboard({ session }) {
  const [documents, setDocuments] = useState([]);
  const [selectedId, setSelectedId] = useState(null);
  const [filter, setFilter] = useState('ALL');
  const [error, setError] = useState('');

  async function load() {
    const docs = await api('/api/docs', {}, session.token);
    setDocuments(docs);
    setSelectedId((current) => current || docs[0]?.id || null);
  }

  useEffect(() => {
    load().catch((err) => setError(err.message));
  }, []);

  async function upload(event) {
    const file = event.target.files?.[0];
    if (!file) return;
    const data = new FormData();
    data.append('file', file);
    try {
      await api('/api/docs/upload', { method: 'POST', body: data }, session.token);
      await load();
    } catch (err) {
      setError(err.message);
    } finally {
      event.target.value = '';
    }
  }

  const filtered = useMemo(() => {
    return filter === 'ALL' ? documents : documents.filter((doc) => doc.status === filter);
  }, [documents, filter]);

  const selected = documents.find((doc) => doc.id === selectedId) || null;

  return (
    <main className="mx-auto grid max-w-7xl gap-4 px-4 py-4 lg:grid-cols-[340px_1fr]">
      <aside className="rounded-lg border bg-white">
        <div className="border-b p-4">
          <label className="btn-primary flex cursor-pointer items-center justify-center gap-2">
            <FileUp className="h-4 w-4" />
            Upload PDF
            <input className="hidden" type="file" accept="application/pdf" onChange={upload} />
          </label>
          <div className="mt-3 flex items-center gap-2 rounded-md border px-3 py-2 text-sm">
            <Search className="h-4 w-4 text-zinc-400" />
            <select className="w-full bg-transparent outline-none" value={filter} onChange={(e) => setFilter(e.target.value)}>
              <option value="ALL">All documents</option>
              <option value="DRAFT">Draft</option>
              <option value="PENDING">Pending</option>
              <option value="SIGNED">Signed</option>
              <option value="REJECTED">Rejected</option>
            </select>
          </div>
          {error && <p className="mt-3 rounded-md bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p>}
        </div>

        <div className="max-h-[calc(100vh-220px)] overflow-auto p-2">
          {filtered.map((doc) => {
            const Icon = statusIcon[doc.status] || Clock3;
            return (
              <button
                key={doc.id}
                className={`w-full rounded-md p-3 text-left hover:bg-zinc-50 ${selectedId === doc.id ? 'bg-teal-50 ring-1 ring-mint' : ''}`}
                onClick={() => setSelectedId(doc.id)}
              >
                <div className="flex items-start justify-between gap-2">
                  <p className="line-clamp-2 font-medium">{doc.originalName}</p>
                  <Icon className="h-4 w-4 shrink-0 text-mint" />
                </div>
                <p className="mt-1 text-xs text-zinc-500">{doc.status} · {(doc.sizeBytes / 1024).toFixed(1)} KB</p>
              </button>
            );
          })}
          {!filtered.length && <p className="p-4 text-sm text-zinc-500">Upload a PDF to start a signing workflow.</p>}
        </div>
      </aside>

      <DocumentWorkspace document={selected} session={session} onChanged={load} />
    </main>
  );
}

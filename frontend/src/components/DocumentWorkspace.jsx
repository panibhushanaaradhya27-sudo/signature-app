import React, { useEffect, useMemo, useState } from 'react';
import { DndContext, useDraggable } from '@dnd-kit/core';
import { CSS } from '@dnd-kit/utilities';
import { Copy, ExternalLink, FileCheck2, Send } from 'lucide-react';
import { Document, Page, pdfjs } from 'react-pdf';
import { api, API_BASE } from '../utils/api.js';
import 'react-pdf/dist/Page/AnnotationLayer.css';
import 'react-pdf/dist/Page/TextLayer.css';

pdfjs.GlobalWorkerOptions.workerSrc = new URL('pdfjs-dist/build/pdf.worker.min.mjs', import.meta.url).toString();

function SignatureBox({ position }) {
  const { attributes, listeners, setNodeRef, transform } = useDraggable({ id: 'signature-box' });
  const style = {
    left: position.x,
    top: position.y,
    transform: CSS.Translate.toString(transform)
  };

  return (
    <div ref={setNodeRef} style={style} className="signature-box" {...listeners} {...attributes}>
      Signature
    </div>
  );
}

export function DocumentWorkspace({ document, session, onChanged }) {
  const [numPages, setNumPages] = useState(1);
  const [pageNumber, setPageNumber] = useState(1);
  const [position, setPosition] = useState({ x: 72, y: 140 });
  const [signer, setSigner] = useState({ signerName: '', signerEmail: '' });
  const [signatures, setSignatures] = useState([]);
  const [audit, setAudit] = useState([]);
  const [message, setMessage] = useState('');

  const file = useMemo(() => {
    if (!document) return null;
    return {
      url: `${API_BASE}${document.previewUrl}`,
      httpHeaders: { Authorization: `Bearer ${session.token}` }
    };
  }, [document, session.token]);

  async function loadDetails() {
    if (!document) return;
    const [sigRows, auditRows] = await Promise.all([
      api(`/api/signatures/${document.id}`, {}, session.token),
      api(`/api/audit/${document.id}`, {}, session.token)
    ]);
    setSignatures(sigRows);
    setAudit(auditRows);
  }

  useEffect(() => {
    setMessage('');
    loadDetails().catch((err) => setMessage(err.message));
  }, [document?.id]);

  if (!document) {
    return (
      <section className="rounded-lg border bg-white p-8 text-center text-zinc-500">
        Upload or select a document to manage signatures.
      </section>
    );
  }

  async function saveSignature() {
    setMessage('');
    try {
      await api('/api/signatures', {
        method: 'POST',
        body: JSON.stringify({
          documentId: document.id,
          ...signer,
          x: Math.round(position.x),
          y: Math.round(position.y),
          pageNumber
        })
      }, session.token);
      setSigner({ signerName: '', signerEmail: '' });
      await loadDetails();
      await onChanged();
      setMessage('Signature request created. Link is shown below and email is attempted in dev mode.');
    } catch (err) {
      setMessage(err.message);
    }
  }

  async function finalize() {
    try {
      await api(`/api/signatures/finalize/${document.id}`, { method: 'POST' }, session.token);
      await onChanged();
      await loadDetails();
      setMessage('Finalize checked. Signed PDF appears when all requests are signed.');
    } catch (err) {
      setMessage(err.message);
    }
  }

  async function openSignedPdf() {
    const response = await fetch(`${API_BASE}${document.signedPreviewUrl}`, {
      headers: { Authorization: `Bearer ${session.token}` }
    });
    const blob = await response.blob();
    window.open(URL.createObjectURL(blob), '_blank');
  }

  return (
    <section className="grid gap-4 xl:grid-cols-[minmax(0,1fr)_360px]">
      <div className="rounded-lg border bg-white">
        <div className="flex flex-wrap items-center justify-between gap-3 border-b p-3">
          <div>
            <h2 className="font-semibold">{document.originalName}</h2>
            <p className="text-sm text-zinc-500">{document.status}</p>
          </div>
          <div className="flex items-center gap-2">
            <button className="btn-secondary" disabled={pageNumber <= 1} onClick={() => setPageNumber(pageNumber - 1)}>
              Prev
            </button>
            <span className="text-sm text-zinc-600">{pageNumber} / {numPages}</span>
            <button className="btn-secondary" disabled={pageNumber >= numPages} onClick={() => setPageNumber(pageNumber + 1)}>
              Next
            </button>
          </div>
        </div>

        <DndContext
          onDragEnd={({ delta }) => setPosition((current) => ({
            x: Math.max(0, current.x + delta.x),
            y: Math.max(0, current.y + delta.y)
          }))}
        >
          <div className="pdf-stage">
            <Document file={file} onLoadSuccess={({ numPages }) => setNumPages(numPages)}>
              <Page pageNumber={pageNumber} width={760} />
            </Document>
            <SignatureBox position={position} />
          </div>
        </DndContext>
      </div>

      <aside className="space-y-4">
        <div className="rounded-lg border bg-white p-4">
          <h3 className="font-semibold">Signature request</h3>
          <label className="field mt-3">
            <span>Signer name</span>
            <input className="plain-input" value={signer.signerName} onChange={(e) => setSigner({ ...signer, signerName: e.target.value })} />
          </label>
          <label className="field">
            <span>Signer email</span>
            <input className="plain-input" type="email" value={signer.signerEmail} onChange={(e) => setSigner({ ...signer, signerEmail: e.target.value })} />
          </label>
          <button className="btn-primary mt-2 flex w-full items-center justify-center gap-2" onClick={saveSignature}>
            <Send className="h-4 w-4" />
            Save and send link
          </button>
          <button className="btn-secondary mt-2 flex w-full items-center justify-center gap-2" onClick={finalize}>
            <FileCheck2 className="h-4 w-4" />
            Finalize PDF
          </button>
          {message && <p className="mt-3 rounded-md bg-zinc-100 px-3 py-2 text-sm text-zinc-700">{message}</p>}
        </div>

        {document.signedPreviewUrl && (
          <button className="btn-primary flex w-full items-center justify-center gap-2" onClick={openSignedPdf}>
            <ExternalLink className="h-4 w-4" />
            Open signed PDF
          </button>
        )}

        <div className="rounded-lg border bg-white p-4">
          <h3 className="font-semibold">Requests</h3>
          <div className="mt-3 space-y-2">
            {signatures.map((sig) => (
              <div key={sig.id} className="rounded-md border p-3 text-sm">
                <div className="flex items-center justify-between gap-2">
                  <p className="font-medium">{sig.signerName}</p>
                  <span className="rounded-full bg-zinc-100 px-2 py-1 text-xs">{sig.status}</span>
                </div>
                <p className="text-zinc-500">{sig.signerEmail}</p>
                <button className="mt-2 inline-flex items-center gap-1 text-mint" onClick={() => navigator.clipboard.writeText(sig.publicUrl)}>
                  <Copy className="h-3 w-3" />
                  Copy public link
                </button>
              </div>
            ))}
            {!signatures.length && <p className="text-sm text-zinc-500">No signature requests yet.</p>}
          </div>
        </div>

        <div className="rounded-lg border bg-white p-4">
          <h3 className="font-semibold">Audit trail</h3>
          <div className="mt-3 max-h-64 space-y-2 overflow-auto">
            {audit.map((row) => (
              <div key={row.id} className="border-l-2 border-mint pl-3 text-sm">
                <p className="font-medium">{row.action}</p>
                <p className="text-zinc-500">{row.message}</p>
                <p className="text-xs text-zinc-400">{new Date(row.createdAt).toLocaleString()} · {row.ipAddress}</p>
              </div>
            ))}
          </div>
        </div>
      </aside>
    </section>
  );
}

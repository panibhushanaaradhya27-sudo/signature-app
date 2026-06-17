export const API_BASE = 'http://localhost:8080';

export async function api(path, options = {}, token) {
  const headers = {
    ...(options.body instanceof FormData ? {} : { 'Content-Type': 'application/json' }),
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...options.headers
  };

  const response = await fetch(`${API_BASE}${path}`, { ...options, headers });
  if (!response.ok) {
    let message = 'Request failed';
    try {
      const error = await response.json();
      message = error.message || message;
    } catch {
      message = await response.text();
    }
    throw new Error(message);
  }
  return response.status === 204 ? null : response.json();
}

export function authFileUrl(path, token) {
  return `${API_BASE}${path}?token=${encodeURIComponent(token)}`;
}

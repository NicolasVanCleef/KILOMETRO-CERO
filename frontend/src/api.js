// Calls to the backend. Vite forwards /api to http://localhost:8080 (vite.config.js).

export class ApiError extends Error {
  constructor(message, status, fieldErrors = {}) {
    super(message)
    this.status = status
    this.fieldErrors = fieldErrors
  }
}

async function request(path, options = {}) {
  let response
  try {
    response = await fetch(`/api${path}`, {
      headers: { 'Content-Type': 'application/json' },
      ...options,
    })
  } catch {
    throw new ApiError('No se pudo conectar con el servidor. ¿Está corriendo el backend?', 0)
  }

  const body = response.status === 204 ? null : await response.json().catch(() => null)
  if (!response.ok) {
    throw new ApiError(body?.message ?? `Error ${response.status}`, response.status, body?.fieldErrors ?? {})
  }
  return body
}

export const api = {
  getDepartments: () => request('/departments'),
  registerBusiness: (data) => request('/businesses', { method: 'POST', body: JSON.stringify(data) }),
  listBusinesses: (status) => request(`/admin/businesses?status=${status}`),
  getBusiness: (id) => request(`/admin/businesses/${id}`),
  approveBusiness: (id) => request(`/admin/businesses/${id}/approve`, { method: 'POST' }),
  rejectBusiness: (id) => request(`/admin/businesses/${id}/reject`, { method: 'POST' }),
}

// Texts shown for the backend's enum values.
export const PERSON_TYPE_LABELS = {
  INDIVIDUAL: 'Persona física',
  COMPANY: 'Persona jurídica',
}

export const STATUS_LABELS = {
  PENDING: 'Pendiente',
  APPROVED: 'Aprobado',
  REJECTED: 'Rechazado',
}

export function formatDate(isoDate) {
  if (!isoDate) return '—'
  return new Date(isoDate).toLocaleString('es-UY', { dateStyle: 'short', timeStyle: 'short' })
}

// Calls to the backend. Vite forwards /api to http://localhost:8080 (vite.config.js).
import { loadSession } from './session.js'

export class ApiError extends Error {
  constructor(message, status, fieldErrors = {}) {
    super(message)
    this.status = status
    this.fieldErrors = fieldErrors
  }
}

// App.jsx registers here what to do when the backend says the session is no longer valid.
let onSessionExpired = () => {}
export function setSessionExpiredHandler(handler) {
  onSessionExpired = handler
}

async function request(path, options = {}) {
  // If someone is logged in, every request carries their token.
  const token = loadSession()?.token
  const headers = { 'Content-Type': 'application/json' }
  if (token) headers.Authorization = `Bearer ${token}`

  let response
  try {
    response = await fetch(`/api${path}`, { headers, ...options })
  } catch {
    throw new ApiError('No se pudo conectar con el servidor. ¿Está corriendo el backend?', 0)
  }

  const body = response.status === 204 ? null : await response.json().catch(() => null)
  if (!response.ok) {
    if (response.status === 401 && token) onSessionExpired()
    throw new ApiError(body?.message ?? `Error ${response.status}`, response.status, body?.fieldErrors ?? {})
  }
  return body
}

const post = (path, data) => request(path, { method: 'POST', body: data ? JSON.stringify(data) : undefined })
const put = (path, data) => request(path, { method: 'PUT', body: JSON.stringify(data) })

export const api = {
  // Accounts
  login: (data) => post('/auth/login', data),
  registerTourist: (data) => post('/tourists', data),
  requestPasswordReset: (email) => post('/auth/password-reset', { email }),
  confirmPasswordReset: (token, password) => post('/auth/password-reset/confirm', { token, password }),
  getTouristProfile: () => request('/tourist/profile'),
  updateTouristProfile: (data) => put('/tourist/profile', data),

  // Businesses
  getDepartments: () => request('/departments'),
  registerBusiness: (data) => post('/businesses', data),
  getOwnBusiness: () => request('/provider/business'),
  updateOwnBusiness: (data) => put('/provider/business', data),
  listBusinesses: (status) => request(`/admin/businesses?status=${status}`),
  getBusiness: (id) => request(`/admin/businesses/${id}`),
  approveBusiness: (id) => post(`/admin/businesses/${id}/approve`),
  rejectBusiness: (id) => post(`/admin/businesses/${id}/reject`),

  // Tags and equipment (the lists are public; only the administrator changes them)
  listTags: () => request('/tags'),
  createTag: (name) => post('/admin/tags', { name }),
  renameTag: (id, name) => put(`/admin/tags/${id}`, { name }),
  deleteTag: (id) => request(`/admin/tags/${id}`, { method: 'DELETE' }),
  listEquipment: () => request('/equipment'),
  createEquipment: (name) => post('/admin/equipment', { name }),
  renameEquipment: (id, name) => put(`/admin/equipment/${id}`, { name }),
  deleteEquipment: (id) => request(`/admin/equipment/${id}`, { method: 'DELETE' }),

  // Payments
  pay: (data) => post('/payments', data),
  listPayments: () => request('/payments'),
}

export const PAYMENT_STATUS = {
  APPROVED: { label: 'Aprobado', className: 'approved' },
  REJECTED: { label: 'Rechazado', className: 'rejected' },
  PENDING: { label: 'Pendiente', className: 'pending' },
  REFUNDED: { label: 'Reembolsado', className: 'pending' },
}

export function formatMoney(amount) {
  return '$ ' + Number(amount).toLocaleString('es-UY')
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

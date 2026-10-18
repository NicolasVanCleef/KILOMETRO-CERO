// Users you can enter the page as (actors of the ERS, section 5.1).
// For now choosing one is a simulation: the real login with email and password comes in Part 4.

export const ROLES = {
  VISITOR: {
    label: 'Visitante',
    description: 'Mirá la oferta turística sin registrarte.',
    links: [],
  },
  TOURIST: {
    label: 'Turista',
    description: 'Reservá actividades y consultá tus reservas.',
    links: [],
  },
  PROVIDER: {
    label: 'Prestador',
    description: 'Registrá tu emprendimiento y ofrecé tus actividades.',
    links: [{ to: '/registro-prestador', label: 'Registrar emprendimiento' }],
  },
  ADMIN: {
    label: 'Administrador',
    description: 'Revisá y aprobá los emprendimientos nuevos.',
    links: [{ to: '/admin/emprendimientos', label: 'Emprendimientos' }],
  },
}

const STORAGE_KEY = 'uruguay-mnatural-role'

export function loadRole() {
  try {
    const role = localStorage.getItem(STORAGE_KEY)
    return role in ROLES ? role : null
  } catch {
    return null
  }
}

export function saveRole(role) {
  try {
    if (role) localStorage.setItem(STORAGE_KEY, role)
    else localStorage.removeItem(STORAGE_KEY)
  } catch {
    // Without storage the role is only kept until the page is reloaded.
  }
}

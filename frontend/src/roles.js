// What each kind of user sees in the menu (actors of the ERS, section 5.1).
// A visitor is anyone who has not logged in.

export const ROLES = {
  VISITOR: {
    label: 'Visitante',
    links: [],
  },
  TOURIST: {
    label: 'Turista',
    links: [
      { to: '/pagos', label: 'Pagar (sandbox)' },
      { to: '/perfil', label: 'Mi perfil' },
    ],
  },
  PROVIDER: {
    label: 'Prestador',
    links: [{ to: '/prestador/emprendimiento', label: 'Mi emprendimiento' }],
  },
  ADMIN: {
    label: 'Administrador',
    links: [
      { to: '/admin/emprendimientos', label: 'Emprendimientos' },
      { to: '/admin/catalogo', label: 'Etiquetas y equipamiento' },
    ],
  },
}

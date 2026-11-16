// What each kind of user sees in the menu (actors of the ERS, section 5.1).
// A visitor is anyone who has not logged in. Everyone also sees "Actividades"; the logo goes home.

export const ROLES = {
  VISITOR: {
    label: 'Visitante',
    links: [],
  },
  TOURIST: {
    label: 'Turista',
    links: [
      { to: '/mis-reservas', label: 'Mis reservas' },
      { to: '/perfil', label: 'Mi perfil' },
    ],
  },
  PROVIDER: {
    label: 'Prestador',
    links: [
      { to: '/prestador/actividades', label: 'Mis actividades' },
      { to: '/prestador/emprendimiento', label: 'Mi emprendimiento' },
    ],
  },
  ADMIN: {
    label: 'Administrador',
    links: [
      { to: '/admin/emprendimientos', label: 'Emprendimientos' },
      { to: '/admin/actividades', label: 'Moderación' },
      { to: '/admin/catalogo', label: 'Catálogo' },
    ],
  },
}

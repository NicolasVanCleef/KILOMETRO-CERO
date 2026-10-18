import { Link } from 'react-router-dom'

// What each user can do from the home page. "soon" marks parts not built yet.
const ACTIONS = {
  VISITOR: [
    { title: 'Explorar actividades', text: 'Buscá actividades por destino, tipo, fecha y precio.', soon: true },
    { title: 'Crear una cuenta de turista', text: 'Registrate para poder reservar.', soon: true },
  ],
  TOURIST: [
    { title: 'Explorar y reservar', text: 'Elegí una actividad, una fecha y la cantidad de personas.', soon: true },
    { title: 'Mis reservas', text: 'Consultá el estado de tus reservas.', soon: true },
  ],
  PROVIDER: [
    {
      title: 'Registrar emprendimiento',
      text: 'Registrá tu emprendimiento como persona física o jurídica. Un administrador lo revisa y te avisa por correo.',
      to: '/registro-prestador',
      button: 'Registrar emprendimiento',
    },
    { title: 'Mis actividades', text: 'Publicá tus actividades y sus fechas.', soon: true },
  ],
  ADMIN: [
    {
      title: 'Emprendimientos',
      text: 'Revisá los emprendimientos nuevos y aceptalos o rechazalos.',
      to: '/admin/emprendimientos',
      button: 'Ver emprendimientos',
    },
    { title: 'Etiquetas y equipamiento', text: 'Mantené los tipos de actividad y de equipamiento.', soon: true },
  ],
}

function HomePage({ role }) {
  return (
    <div className="page">
      <section className="hero">
        <h1>Toda la oferta turística de Uruguay, en un solo lugar</h1>
        <p>
          Uruguay Mnatural reúne a empresas, emprendimientos y prestadores particulares de todo el
          país para que los turistas encuentren y reserven sus actividades.
        </p>
      </section>

      <section className="cards">
        {ACTIONS[role].map((action) => (
          <article key={action.title} className={`card${action.soon ? ' soon' : ''}`}>
            <h2>{action.title}</h2>
            <p>{action.text}</p>
            {action.soon
              ? <span className="badge pending">Próximamente</span>
              : <Link to={action.to} className="button">{action.button}</Link>}
          </article>
        ))}
      </section>
    </div>
  )
}

export default HomePage

import { Link } from 'react-router-dom'

function HomePage() {
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
        <article className="card">
          <h2>¿Ofrecés actividades turísticas?</h2>
          <p>
            Registrá tu emprendimiento, como persona física o jurídica. Un administrador de Uruguay
            Mnatural lo revisa y te avisa por correo cuando esté aprobado.
          </p>
          <Link to="/registro-prestador" className="button">Registrar emprendimiento</Link>
        </article>

        <article className="card">
          <h2>Administración</h2>
          <p>Revisá los emprendimientos nuevos y aceptalos o rechazalos.</p>
          <Link to="/admin/emprendimientos" className="button secondary">Ir a administración</Link>
        </article>
      </section>
    </div>
  )
}

export default HomePage

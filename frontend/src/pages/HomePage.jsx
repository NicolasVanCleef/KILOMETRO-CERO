import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { api } from '../api.js'
import ActivityCard from '../components/ActivityCard.jsx'
import TagIcon from '../components/TagIcon.jsx'

// Shortcuts of each logged-in user, shown above the public content of the home page.
const SHORTCUTS = {
  TOURIST: [
    { title: 'Mis reservas', text: 'Consultá tus reservas, pagá las pendientes o cancelalas.', to: '/mis-reservas', button: 'Ver mis reservas' },
    { title: 'Mi perfil', text: 'Mirá y cambiá tus datos.', to: '/perfil', button: 'Ver mi perfil' },
  ],
  PROVIDER: [
    { title: 'Mis actividades', text: 'Cargá tus actividades y programalas en fechas, cada una con su cupo y su precio.', to: '/prestador/actividades', button: 'Ver mis actividades' },
    { title: 'Mi emprendimiento', text: 'Mirá y cambiá los datos de contacto, la ubicación y la descripción.', to: '/prestador/emprendimiento', button: 'Ver mi emprendimiento' },
  ],
  ADMIN: [
    { title: 'Emprendimientos', text: 'Revisá los emprendimientos nuevos, aceptalos o rechazalos, y controlá a los prestadores.', to: '/admin/emprendimientos', button: 'Ver emprendimientos' },
    { title: 'Moderar actividades', text: 'Mirá las actividades publicadas y eliminá las que sean inapropiadas.', to: '/admin/actividades', button: 'Ver actividades' },
    { title: 'Etiquetas y equipamiento', text: 'Mantené los tipos de actividad y de equipamiento que usan los prestadores.', to: '/admin/catalogo', button: 'Ver etiquetas y equipamiento' },
  ],
}

// Where the destination labels sit around the "kilómetro cero" drawing (percent of its size).
const LABEL_SPOTS = [
  { left: '58%', top: '8%' },
  { left: '66%', top: '46%' },
  { left: '2%', top: '80%' },
]

function HomePage({ role }) {
  const navigate = useNavigate()
  const [departments, setDepartments] = useState([])
  const [tags, setTags] = useState([])
  const [activities, setActivities] = useState(null)
  const [search, setSearch] = useState({ department: '', tagId: '', date: '' })

  useEffect(() => {
    api.getDepartments().then(setDepartments).catch(() => {})
    api.listTags().then(setTags).catch(() => {})
    api.searchActivities({}).then(setActivities).catch(() => setActivities([]))
  }, [])

  // The search box sends the visitor to the activities page with the filters in the address.
  function handleSearch(event) {
    event.preventDefault()
    const filled = Object.entries(search).filter(([, value]) => value !== '')
    navigate(`/actividades?${new URLSearchParams(filled)}`)
  }

  // The three activities with the closest dates.
  const upcoming = (activities ?? [])
    .filter((activity) => activity.nextDate)
    .sort((a, b) => a.nextDate.localeCompare(b.nextDate))
    .slice(0, 3)

  // Destinations of the drawing: departments that have activities, with their first tag.
  const destinations = []
  for (const activity of activities ?? []) {
    if (destinations.length === 3) break
    if (!destinations.some((d) => d.department === activity.department)) {
      destinations.push({ department: activity.department, tag: activity.tags[0] })
    }
  }

  const departmentCount = new Set((activities ?? []).map((a) => a.department)).size

  return (
    <div className="page">
      <section className="hero">
        <div className="hero-text">
          <span className="eyebrow">Tu punto de partida para recorrer Uruguay</span>
          <h1>Toda la oferta turística del país, en un solo lugar.</h1>
          <p>
            Empresas, emprendimientos y prestadores de todo Uruguay. Elegí la fecha, reservá tu
            lugar y pagá online.
          </p>

          <form className="search-box" role="search" aria-label="Buscar actividades" onSubmit={handleSearch}>
            <label>
              Destino
              <select value={search.department} onChange={(e) => setSearch({ ...search, department: e.target.value })}>
                <option value="">Todo Uruguay</option>
                {departments.map((d) => <option key={d}>{d}</option>)}
              </select>
            </label>
            <label>
              Tipo de actividad
              <select value={search.tagId} onChange={(e) => setSearch({ ...search, tagId: e.target.value })}>
                <option value="">Cualquiera</option>
                {tags.map((tag) => <option key={tag.id} value={tag.id}>{tag.name}</option>)}
              </select>
            </label>
            <label>
              Fecha
              <input type="date" value={search.date} onChange={(e) => setSearch({ ...search, date: e.target.value })} />
            </label>
            <button className="button accent" type="submit">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.4"
                strokeLinecap="round" aria-hidden="true"><circle cx="11" cy="11" r="7" /><path d="M20 20l-3.5-3.5" /></svg>
              Buscar
            </button>
          </form>
          {activities?.length > 0 && (
            <p className="small muted">
              {activities.length} {activities.length === 1 ? 'actividad' : 'actividades'} en {departmentCount}{' '}
              {departmentCount === 1 ? 'departamento' : 'departamentos'}
            </p>
          )}
        </div>

        <div className="hero-art">
          <Km0 destinations={destinations} />
        </div>
      </section>

      {SHORTCUTS[role] && (
        <section className="home-section" aria-labelledby="mine">
          <div className="section-title"><h2 id="mine">Tu espacio</h2></div>
          <div className="cards">
            {SHORTCUTS[role].map((item) => (
              <article key={item.title} className="card">
                <h2>{item.title}</h2>
                <p className="muted">{item.text}</p>
                <Link to={item.to} className="button">{item.button}</Link>
              </article>
            ))}
          </div>
        </section>
      )}

      {tags.length > 0 && (
        <section className="home-section" aria-labelledby="cats">
          <div className="section-title"><h2 id="cats">Explorá por tipo de actividad</h2></div>
          <div className="categories">
            {tags.map((tag) => (
              <Link key={tag.id} to={`/actividades?tagId=${tag.id}`} className="category">
                <TagIcon name={tag.name} />
                {tag.name}
              </Link>
            ))}
          </div>
        </section>
      )}

      <section className="home-section" aria-labelledby="next">
        <div className="section-title">
          <div>
            <h2 id="next">Próximas salidas</h2>
            <p>Las actividades con las fechas más cercanas.</p>
          </div>
          <Link to="/actividades" className="more-link">Ver todas las actividades →</Link>
        </div>
        {activities === null && <p className="muted">Cargando…</p>}
        {activities !== null && upcoming.length === 0 && (
          <p className="muted">Todavía no hay actividades con fechas próximas.</p>
        )}
        <div className="cards">
          {upcoming.map((activity) => <ActivityCard key={activity.id} activity={activity} />)}
        </div>
      </section>

      <section className="home-section" aria-labelledby="how">
        <div className="section-title"><h2 id="how">Reservar es así de simple</h2></div>
        <ol className="steps">
          <li>
            <span className="step-number">01</span>
            <strong>Elegí actividad y fecha</strong>
            <span>Filtrá por destino, tipo, día y precio, y mirá el pronóstico del clima de cada fecha.</span>
          </li>
          <li>
            <span className="step-number">02</span>
            <strong>Te guardamos los lugares</strong>
            <span>Durante 10 minutos nadie más los puede tomar, mientras cargás los datos de cada persona.</span>
          </li>
          <li>
            <span className="step-number">03</span>
            <strong>Pagá y recibí tu código</strong>
            <span>Pagás con tarjeta y te llega el código de reserva por correo.</span>
          </li>
        </ol>
      </section>

      {role === 'VISITOR' && (
        <section className="home-section">
          <div className="band">
            <div>
              <h2>¿Ofrecés actividades?</h2>
              <p>
                Registrá tu emprendimiento como persona física o jurídica. Un administrador lo revisa
                y te avisa por correo; después cargás tus actividades y sus fechas.
              </p>
            </div>
            <Link to="/registro-prestador" className="button accent">Registrar mi emprendimiento</Link>
          </div>
        </section>
      )}
    </div>
  )
}

// The team's logo turned into a map: routes leaving "kilómetro cero" towards the destinations.
function Km0({ destinations }) {
  return (
    <div className="km0">
      <svg viewBox="0 0 440 440" width="100%" height="100%" aria-hidden="true">
        <circle cx="220" cy="220" r="196" fill="none" stroke="#7FAF9F" strokeWidth="2" strokeDasharray="2 8" strokeLinecap="round" />
        {[
          'M220 220 C 250 160, 300 120, 340 92',
          'M220 220 C 290 230, 340 228, 392 214',
          'M220 220 C 250 280, 290 330, 318 372',
          'M220 220 C 180 270, 150 320, 136 372',
          'M220 220 C 160 210, 110 212, 54 226',
          'M220 220 C 190 160, 150 116, 112 92',
        ].map((d) => <path key={d} d={d} fill="none" stroke="#106F5E" strokeWidth="5" strokeLinecap="round" />)}
        {[[340, 92], [392, 214], [318, 372], [136, 372], [54, 226], [112, 92]].map(([cx, cy]) => (
          <circle key={`${cx}-${cy}`} cx={cx} cy={cy} r="9" fill="#106F5E" />
        ))}
        <circle cx="220" cy="220" r="34" fill="#F6CDB6" />
        <circle cx="220" cy="220" r="20" fill="#E07038" />
      </svg>
      {destinations.map((destination, index) => (
        <Link key={destination.department} className="km0-label" style={LABEL_SPOTS[index]}
          to={`/actividades?department=${encodeURIComponent(destination.department)}`}>
          {destination.department}{destination.tag && ` · ${destination.tag}`}
        </Link>
      ))}
      <span className="km0-center">KM 0</span>
    </div>
  )
}

export default HomePage

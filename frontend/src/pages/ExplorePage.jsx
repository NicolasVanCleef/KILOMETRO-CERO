import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api, formatDay, formatDuration, formatMoney } from '../api.js'

const NO_FILTERS = { q: '', department: '', locality: '', tagId: '', date: '', maxPrice: '' }

// CU-13: anyone can look at the published activities and filter them by destination,
// type of activity, date and price, without an account.
function ExplorePage() {
  const [filters, setFilters] = useState(NO_FILTERS)
  const [activities, setActivities] = useState(null)
  const [departments, setDepartments] = useState([])
  const [tags, setTags] = useState([])
  const [error, setError] = useState('')

  useEffect(() => {
    api.getDepartments().then(setDepartments).catch(() => {})
    api.listTags().then(setTags).catch(() => {})
    api.searchActivities(NO_FILTERS).then(setActivities).catch((e) => setError(e.message))
  }, [])

  async function search(newFilters) {
    setFilters(newFilters)
    setError('')
    try {
      setActivities(await api.searchActivities(newFilters))
    } catch (e) {
      setError(e.message)
    }
  }

  function update(field) {
    return (event) => setFilters({ ...filters, [field]: event.target.value })
  }

  function handleSubmit(event) {
    event.preventDefault()
    search(filters)
  }

  return (
    <div className="page">
      <h1>Actividades</h1>
      <p className="lead">Buscá qué hacer en Uruguay por destino, tipo de actividad, fecha y precio.</p>

      <form className="filters" onSubmit={handleSubmit}>
        <label>
          <span>Buscar</span>
          <input value={filters.q} onChange={update('q')} placeholder="Palabra clave" />
        </label>
        <label>
          <span>Departamento</span>
          <select value={filters.department} onChange={update('department')}>
            <option value="">Todos</option>
            {departments.map((d) => <option key={d}>{d}</option>)}
          </select>
        </label>
        <label>
          <span>Localidad</span>
          <input value={filters.locality} onChange={update('locality')} />
        </label>
        <label>
          <span>Tipo de actividad</span>
          <select value={filters.tagId} onChange={update('tagId')}>
            <option value="">Todos</option>
            {tags.map((tag) => <option key={tag.id} value={tag.id}>{tag.name}</option>)}
          </select>
        </label>
        <label>
          <span>Fecha</span>
          <input value={filters.date} onChange={update('date')} type="date" />
        </label>
        <label>
          <span>Precio máximo ($)</span>
          <input value={filters.maxPrice} onChange={update('maxPrice')} type="number" min="0" />
        </label>
        <div className="filter-buttons">
          <button className="button" type="submit">Buscar</button>
          <button className="button secondary" type="button" onClick={() => search(NO_FILTERS)}>Limpiar</button>
        </div>
      </form>

      {error && <p className="alert error">{error}</p>}
      {activities === null && !error && <p className="muted">Cargando…</p>}
      {activities?.length === 0 && <p className="muted">No encontramos actividades con esos filtros.</p>}

      <section className="cards">
        {activities?.map((activity) => (
          <Link key={activity.id} to={`/actividades/${activity.id}`} className="card link-card">
            <h2>{activity.name}</h2>
            <p className="muted small">
              {activity.locality}, {activity.department} · {formatDuration(activity.durationMinutes)}
            </p>
            <p className="tag-row">
              {activity.tags.map((tag) => <span key={tag} className="tag">{tag}</span>)}
            </p>
            <p className="small">Ofrecida por {activity.businessName}</p>
            <p>
              {Number(activity.priceFrom) === 0
                ? <strong>Sin costo</strong>
                : <>Desde <strong>{formatMoney(activity.priceFrom)}</strong> por persona</>}
            </p>
            <p className="small muted">
              {activity.nextDate ? `Próxima fecha: ${formatDay(activity.nextDate)}` : 'Sin fechas próximas'}
            </p>
          </Link>
        ))}
      </section>
    </div>
  )
}

export default ExplorePage

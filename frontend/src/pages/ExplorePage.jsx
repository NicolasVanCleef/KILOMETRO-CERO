import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { api } from '../api.js'
import ActivityCard from '../components/ActivityCard.jsx'

const NO_FILTERS = { q: '', department: '', locality: '', tagId: '', date: '', maxPrice: '' }

// The filters written in the address (?department=Rocha&tagId=3), so the home page's search
// box and its category links open this page already filtered.
function filtersFrom(searchParams) {
  return Object.fromEntries(Object.keys(NO_FILTERS).map((key) => [key, searchParams.get(key) ?? '']))
}

// CU-13: anyone can look at the published activities and filter them by destination,
// type of activity, date and price, without an account.
function ExplorePage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const [result, setResult] = useState({ activities: null, error: '' })
  const [departments, setDepartments] = useState([])
  const [tags, setTags] = useState([])

  useEffect(() => {
    api.getDepartments().then(setDepartments).catch(() => {})
    api.listTags().then(setTags).catch(() => {})
  }, [])

  // Search again every time the filters in the address change.
  useEffect(() => {
    api.searchActivities(filtersFrom(searchParams))
      .then((activities) => setResult({ activities, error: '' }))
      .catch((e) => setResult({ activities: [], error: e.message }))
  }, [searchParams])

  function search(newFilters) {
    setSearchParams(Object.entries(newFilters).filter(([, value]) => value !== ''))
  }

  const { activities, error } = result

  return (
    <div className="page">
      <h1>Actividades</h1>
      <p className="lead">Buscá qué hacer en Uruguay por destino, tipo de actividad, fecha y precio.</p>

      {/* The key starts the form over with the address's filters whenever they change. */}
      <Filters key={searchParams.toString()} initial={filtersFrom(searchParams)} departments={departments}
        tags={tags} onSearch={search} />

      {error && <p className="alert error">{error}</p>}
      {activities === null && !error && <p className="muted">Cargando…</p>}
      {activities?.length === 0 && !error && <p className="muted">No encontramos actividades con esos filtros.</p>}
      {activities?.length > 0 && (
        <p className="results-count">
          {activities.length} {activities.length === 1 ? 'actividad encontrada' : 'actividades encontradas'}
        </p>
      )}

      <section className="cards">
        {activities?.map((activity) => <ActivityCard key={activity.id} activity={activity} />)}
      </section>
    </div>
  )
}

function Filters({ initial, departments, tags, onSearch }) {
  const [filters, setFilters] = useState(initial)

  function update(field) {
    return (event) => setFilters({ ...filters, [field]: event.target.value })
  }

  function handleSubmit(event) {
    event.preventDefault()
    onSearch(filters)
  }

  return (
    <form className="filters" onSubmit={handleSubmit}>
      <label>
        <span>Buscar</span>
        <input value={filters.q} onChange={update('q')} placeholder="Kayak, bodega, cerro…" />
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
        <button className="button accent" type="submit">Buscar</button>
        <button className="button secondary" type="button" onClick={() => onSearch(NO_FILTERS)}>Limpiar</button>
      </div>
    </form>
  )
}

export default ExplorePage

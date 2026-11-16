import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api, formatDuration, formatMoney } from '../api.js'
import ActivityArt from '../components/ActivityArt.jsx'

// CU-10: the provider sees the activities of their business and can create, modify or delete them.
function ProviderActivitiesPage() {
  const [activities, setActivities] = useState(null)
  const [deleting, setDeleting] = useState(null)
  const [notice, setNotice] = useState(null)

  useEffect(() => {
    api.listOwnActivities().then(setActivities).catch((e) => setNotice({ type: 'error', text: e.message }))
  }, [])

  async function handleDelete(activity) {
    try {
      await api.deleteOwnActivity(activity.id)
      setActivities(await api.listOwnActivities())
      setNotice({
        type: 'info',
        text: `Eliminaste "${activity.name}". Sus fechas se cancelaron y, si había reservas pagadas, se devolvió el 100 %.`,
      })
    } catch (e) {
      setNotice({ type: 'error', text: e.message })
    }
    setDeleting(null)
  }

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1>Mis actividades</h1>
          <p className="lead">Lo que ofrece tu emprendimiento. Cada actividad se programa después en fechas concretas.</p>
        </div>
        <Link to="/prestador/actividades/nueva" className="button">Nueva actividad</Link>
      </div>

      {notice && <p className={`alert ${notice.type}`}>{notice.text}</p>}
      {activities === null && !notice && <p className="muted">Cargando…</p>}
      {activities?.length === 0 && <p className="muted">Todavía no cargaste ninguna actividad.</p>}

      <section className="cards">
        {activities?.map((activity) => (
          <article key={activity.id} className="card activity-card">
            <ActivityArt tags={activity.tags}>
              <span className="tag-row">
                {activity.tags.map((tag) => <span key={tag.id} className="date-pill">{tag.name}</span>)}
              </span>
            </ActivityArt>
            <div className="activity-body">
              <h3>{activity.name}</h3>
              <p>
                {activity.locality}, {activity.department} · {formatDuration(activity.durationMinutes)}
              </p>
              <p>
                {Number(activity.defaultPrice) === 0
                  ? <span className="free">Sin costo</span>
                  : <><span className="price">{formatMoney(activity.defaultPrice)}</span> por persona</>}
                {' '}· cupo {activity.defaultCapacity}
              </p>

              {deleting === activity.id ? (
                <div className="confirm">
                  <p>
                    ¿Eliminar esta actividad? Se cancelan todas sus fechas y a los turistas que pagaron se
                    les devuelve el 100 %.
                  </p>
                  <button className="button danger" onClick={() => handleDelete(activity)}>Sí, eliminar</button>
                  <button className="button secondary" onClick={() => setDeleting(null)}>Cancelar</button>
                </div>
              ) : (
                <div className="actions">
                  <Link to={`/prestador/actividades/${activity.id}/fechas`} className="button small">Fechas</Link>
                  <Link to={`/prestador/actividades/${activity.id}/valoraciones`} className="button small secondary">
                    Valoraciones
                  </Link>
                  <Link to={`/prestador/actividades/${activity.id}`} className="button small secondary">Modificar</Link>
                  <button className="button small secondary" onClick={() => setDeleting(activity.id)}>Eliminar</button>
                </div>
              )}
            </div>
          </article>
        ))}
      </section>
    </div>
  )
}

export default ProviderActivitiesPage

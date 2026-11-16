import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api, formatMoney } from '../api.js'

// CU-24: the administrator looks through the published activities and can take down one
// that is inappropriate. The administrator does not create or modify activities (RN-11).
function AdminActivitiesPage() {
  const [activities, setActivities] = useState(null)
  const [deleting, setDeleting] = useState(null)
  const [notice, setNotice] = useState(null)

  useEffect(() => {
    api.listAllActivities().then(setActivities).catch((e) => setNotice({ type: 'error', text: e.message }))
  }, [])

  async function handleDelete(activity) {
    try {
      await api.removeActivity(activity.id)
      setActivities(await api.listAllActivities())
      setNotice({
        type: 'info',
        text: `Eliminaste "${activity.name}". Se canceló cada fecha, se devolvió el 100 % a los turistas que habían pagado y se le avisó al prestador.`,
      })
    } catch (e) {
      setNotice({ type: 'error', text: e.message })
    }
    setDeleting(null)
  }

  return (
    <div className="page">
      <h1>Actividades publicadas</h1>
      <p className="lead">
        Las actividades las manejan los prestadores. Desde acá solo podés eliminar una si es inapropiada.
      </p>

      {notice && <p className={`alert ${notice.type}`}>{notice.text}</p>}
      {activities === null && !notice && <p className="muted">Cargando…</p>}
      {activities?.length === 0 && <p className="muted">No hay actividades publicadas.</p>}

      {activities?.length > 0 && (
        <section className="list">
          <table>
            <thead>
              <tr>
                <th>Actividad</th>
                <th>Emprendimiento</th>
                <th>Lugar</th>
                <th>Precio</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {activities.map((activity) => (
                <tr key={activity.id} className="plain">
                  <td><Link to={`/actividades/${activity.id}`}>{activity.name}</Link></td>
                  <td>{activity.businessName}</td>
                  <td>{activity.locality}, {activity.department}</td>
                  <td>{formatMoney(activity.defaultPrice)}</td>
                  <td>
                    {deleting === activity.id ? (
                      <span className="row-actions">
                        <button className="button small danger" onClick={() => handleDelete(activity)}>
                          Sí, eliminar
                        </button>
                        <button className="button small secondary" onClick={() => setDeleting(null)}>No</button>
                      </span>
                    ) : (
                      <button className="button small secondary" onClick={() => setDeleting(activity.id)}>
                        Eliminar
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>
      )}
    </div>
  )
}

export default AdminActivitiesPage

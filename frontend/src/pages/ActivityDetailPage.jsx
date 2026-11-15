import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { api, formatDay, formatDuration, formatMoney, formatTime } from '../api.js'

// CU-14: everything about one activity, to decide whether to do it.
function ActivityDetailPage({ role }) {
  const { id } = useParams()
  const [detail, setDetail] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    api.getActivityDetail(id).then(setDetail).catch((e) => setError(e.message))
  }, [id])

  if (!detail) {
    return (
      <div className="page">
        {error ? <p className="alert error">{error}</p> : <p className="muted">Cargando…</p>}
      </div>
    )
  }

  const { activity, schedules } = detail

  return (
    <div className="page">
      <p className="small"><Link to="/actividades">← Todas las actividades</Link></p>
      <h1>{activity.name}</h1>
      <p className="lead">
        {activity.locality}, {activity.department} · Ofrecida por <strong>{activity.businessName}</strong>
      </p>
      <p className="tag-row">
        {activity.tags.map((tag) => <span key={tag.id} className="tag">{tag.name}</span>)}
      </p>

      <div className="admin-layout">
        <section className="list">
          <h2>La experiencia</h2>
          <p className="description">{activity.description}</p>

          {activity.requirements && (
            <>
              <h3>Requisitos</h3>
              <p className="description">{activity.requirements}</p>
            </>
          )}
          {activity.observations && (
            <>
              <h3>Observaciones</h3>
              <p className="description">{activity.observations}</p>
            </>
          )}

          <h3>Equipamiento</h3>
          <EquipmentList title="Tenés que llevar" items={activity.requiredEquipment} />
          <EquipmentList title="Te lo da el prestador" items={activity.providedEquipment} />
          {activity.requiredEquipment.length + activity.providedEquipment.length === 0 && (
            <p className="muted">Esta actividad no necesita equipamiento.</p>
          )}

          <h3>Próximas fechas</h3>
          {schedules.length === 0 && <p className="muted">Por ahora no hay fechas próximas.</p>}
          {schedules.length > 0 && (
            <table>
              <thead>
                <tr>
                  <th>Fecha</th>
                  <th>Hora</th>
                  <th>Lugares</th>
                  <th>Precio por persona</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {schedules.map((schedule) => (
                  <tr key={schedule.id} className="plain">
                    <td>{formatDay(schedule.date)}</td>
                    <td>{formatTime(schedule.startTime)}</td>
                    <td>{schedule.availableCapacity === 0 ? 'Sin lugares' : `${schedule.availableCapacity} libres`}</td>
                    <td>{Number(schedule.price) === 0 ? 'Sin costo' : formatMoney(schedule.price)}</td>
                    <td><ScheduleAction schedule={schedule} role={role} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </section>

        <aside className="detail">
          <h2>Datos</h2>
          <dl>
            <div><dt>Duración</dt><dd>{formatDuration(activity.durationMinutes)}</dd></div>
            <div>
              <dt>Precio</dt>
              <dd>{Number(activity.defaultPrice) === 0 ? 'Sin costo' : `${formatMoney(activity.defaultPrice)} por persona`}</dd>
            </div>
            <div><dt>Departamento</dt><dd>{activity.department}</dd></div>
            <div><dt>Localidad</dt><dd>{activity.locality}</dd></div>
            {activity.address && <div><dt>Dirección</dt><dd>{activity.address}</dd></div>}
            <div>
              <dt>Reservas</dt>
              <dd>
                {activity.minBookingMinutes === 0
                  ? 'Hasta la hora de inicio'
                  : `Hasta ${formatDuration(activity.minBookingMinutes)} antes`}
              </dd>
            </div>
            <div><dt>Cancelación</dt><dd>{cancellationText(activity)}</dd></div>
          </dl>
        </aside>
      </div>
    </div>
  )
}

function EquipmentList({ title, items }) {
  if (items.length === 0) return null
  return (
    <p>
      <strong>{title}:</strong> {items.map((item) => item.name).join(', ')}
    </p>
  )
}

// What the visitor can do with a date. Booking itself is added with the bookings part.
function ScheduleAction({ schedule, role }) {
  if (!schedule.bookable) return <span className="muted small">No disponible</span>
  if (role === 'VISITOR') return <Link to="/ingresar" className="small">Iniciá sesión para reservar</Link>
  return null
}

function cancellationText(activity) {
  if (!activity.cancellable) return 'No se puede cancelar'
  const deadline = `hasta ${formatDuration(activity.cancellationMinutes)} antes`
  if (!activity.refundable) return `Se puede cancelar ${deadline}, sin reembolso`
  return `Se puede cancelar ${deadline}, con ${activity.refundPercentage} % de reembolso`
}

export default ActivityDetailPage

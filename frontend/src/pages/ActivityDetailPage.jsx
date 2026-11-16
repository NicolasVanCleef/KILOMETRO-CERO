import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import ActivityArt from '../components/ActivityArt.jsx'
import Reviews from '../components/Reviews.jsx'
import { api, formatDay, formatDuration, formatMoney, formatTime } from '../api.js'

// CU-14: everything about one activity, to decide whether to do it.
function ActivityDetailPage({ role }) {
  const { id } = useParams()
  const [detail, setDetail] = useState(null)
  const [error, setError] = useState('')
  const [weather, setWeather] = useState({}) // forecast by day: { '2026-12-05': {...} }

  function load() {
    api.getActivityDetail(id).then(setDetail).catch((e) => setError(e.message))
  }

  // Load the activity when the page opens or the id in the address changes.
  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(load, [id])

  // RF-06: the forecast is asked for apart. If the weather service fails, the page is shown
  // just the same, without the weather column (RNF-14).
  useEffect(() => {
    api.getActivityWeather(id)
      .then((days) => setWeather(Object.fromEntries(days.map((day) => [day.date, day]))))
      .catch(() => setWeather({}))
  }, [id])

  if (!detail) {
    return (
      <div className="page">
        {error ? <p className="alert error">{error}</p> : <p className="muted">Cargando…</p>}
      </div>
    )
  }

  const { activity, schedules, reviews, averageRating } = detail
  const hasWeather = Object.keys(weather).length > 0

  return (
    <div className="page">
      <p className="small"><Link to="/actividades">← Todas las actividades</Link></p>

      <div className="detail-hero">
        <ActivityArt tags={activity.tags}>
          <span className="tag-row">
            {activity.tags.map((tag) => <span key={tag.id} className="date-pill">{tag.name}</span>)}
          </span>
        </ActivityArt>
        <h1>{activity.name}</h1>
        <p className="lead">
          {activity.locality}, {activity.department} · Ofrecida por <strong>{activity.businessName}</strong>
          {averageRating !== null && <> · <span className="stars">★</span> {averageRating} ({reviews.length})</>}
        </p>
      </div>

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
          {activity.requiredEquipment.length + activity.providedEquipment.length === 0
            ? <p className="muted">Esta actividad no necesita equipamiento.</p>
            : (
              <div className="equipment">
                <EquipmentList title="Tenés que llevar" items={activity.requiredEquipment} />
                <EquipmentList title="Te lo da el prestador" items={activity.providedEquipment} />
              </div>
            )}

          <h3 id="fechas">Próximas fechas</h3>
          {schedules.length === 0 && <p className="muted">Por ahora no hay fechas próximas.</p>}
          {schedules.length > 0 && (
            <div className="schedule-list">
              {schedules.map((schedule) => (
                <div key={schedule.id} className="schedule-row">
                  <span className="schedule-when">
                    <strong>{formatDay(schedule.date)}</strong>
                    <span className="small muted">{formatTime(schedule.startTime)} h</span>
                  </span>
                  <span className="schedule-meta">
                    {Number(schedule.price) === 0 ? 'Sin costo' : <><strong>{formatMoney(schedule.price)}</strong> por persona</>}
                    <br />
                    {schedule.availableCapacity === 0 ? 'Sin lugares' : `${schedule.availableCapacity} lugares libres`}
                  </span>
                  {hasWeather && <span className="weather"><Forecast day={weather[schedule.date]} /></span>}
                  <ScheduleAction schedule={schedule} role={role} activityId={activity.id} />
                </div>
              ))}
            </div>
          )}

          <Reviews activityId={activity.id} reviews={reviews} averageRating={averageRating} role={role}
            onChange={load} />
        </section>

        <aside className="detail price-box">
          <p className="small muted">{Number(activity.defaultPrice) === 0 ? 'Precio' : 'Desde'}</p>
          <p className="price-big">
            {Number(activity.defaultPrice) === 0 ? 'Sin costo' : formatMoney(activity.defaultPrice)}
          </p>
          {Number(activity.defaultPrice) !== 0 && <p className="small muted">por persona</p>}
          {schedules.some((schedule) => schedule.bookable) && (
            <a href="#fechas" className="button accent" style={{ width: '100%', marginTop: 12 }}>Ver fechas y reservar</a>
          )}
          <dl>
            <div><dt>Duración</dt><dd>{formatDuration(activity.durationMinutes)}</dd></div>
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

// The forecast of one day: "Despejado · 14° a 25° · lluvia 10 %".
function Forecast({ day }) {
  if (!day) return <span className="muted small">Sin pronóstico todavía</span>
  return (
    <span className="small">
      {day.description} · {Math.round(day.minTemperature)}° a {Math.round(day.maxTemperature)}°
      {day.rainProbability !== null && <> · lluvia {day.rainProbability} %</>}
    </span>
  )
}

function EquipmentList({ title, items }) {
  if (items.length === 0) return null
  return (
    <div>
      <strong>{title}</strong>
      {items.map((item) => item.name).join(', ')}
    </div>
  )
}

// What the visitor can do with a date: a tourist books it, a visitor has to log in first (RN-12).
function ScheduleAction({ schedule, role, activityId }) {
  if (!schedule.bookable) return <span className="muted small">No disponible</span>
  if (role === 'VISITOR') return <Link to="/ingresar" className="button secondary">Iniciá sesión para reservar</Link>
  if (role === 'TOURIST') {
    return <Link to={`/reservar/${activityId}/${schedule.id}`} className="button accent">Reservar</Link>
  }
  return null
}

function cancellationText(activity) {
  if (!activity.cancellable) return 'No se puede cancelar'
  const deadline = `hasta ${formatDuration(activity.cancellationMinutes)} antes`
  if (!activity.refundable) return `Se puede cancelar ${deadline}, sin reembolso`
  return `Se puede cancelar ${deadline}, con ${activity.refundPercentage} % de reembolso`
}

export default ActivityDetailPage

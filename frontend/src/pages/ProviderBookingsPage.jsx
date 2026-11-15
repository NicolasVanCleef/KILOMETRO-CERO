import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { api, BOOKING_STATUS, formatDay, formatMoney, formatTime } from '../api.js'

// CU-22: the provider sees who booked one date, to organise the activity.
function ProviderBookingsPage() {
  const { id } = useParams()
  const [data, setData] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    api.listScheduleBookings(id).then(setData).catch((e) => setError(e.message))
  }, [id])

  if (!data) {
    return (
      <div className="page">
        {error ? <p className="alert error">{error}</p> : <p className="muted">Cargando…</p>}
      </div>
    )
  }

  const { activityName, schedule, bookings } = data
  const confirmedPeople = bookings
    .filter((booking) => booking.status === 'CONFIRMED')
    .reduce((sum, booking) => sum + booking.participantCount, 0)

  return (
    <div className="page">
      <p className="small">
        <Link to={`/prestador/actividades/${schedule.activityId}/fechas`}>← Fechas de {activityName}</Link>
      </p>
      <h1>Reservas del {formatDay(schedule.date)}, {formatTime(schedule.startTime)}</h1>
      <p className="lead">
        {activityName} · {confirmedPeople} {confirmedPeople === 1 ? 'persona confirmada' : 'personas confirmadas'} ·{' '}
        {schedule.availableCapacity} lugares libres de {schedule.totalCapacity}
      </p>

      {bookings.length === 0 && <p className="muted">Todavía no hay reservas para esta fecha.</p>}

      <section className="booking-list">
        {bookings.map((booking) => {
          const status = BOOKING_STATUS[booking.status]
          return (
            <article key={booking.id} className="card booking">
              <div className="detail-header">
                <h2>{booking.touristName}</h2>
                <span className={`badge ${status.className}`}>{status.label}</span>
              </div>
              <dl className="inline">
                <div><dt>Correo</dt><dd>{booking.touristEmail}</dd></div>
                <div><dt>Personas</dt><dd>{booking.participantCount}</dd></div>
                <div><dt>Total</dt><dd>{Number(booking.total) === 0 ? 'Sin costo' : formatMoney(booking.total)}</dd></div>
                <div><dt>Código</dt><dd className="mono">{booking.code}</dd></div>
              </dl>

              {booking.participants.length === 0
                ? <p className="muted small">Todavía no cargó los datos de los participantes.</p>
                : (
                  <div className="list">
                    <table>
                      <thead>
                        <tr><th>Nombre</th><th>Apellido</th><th>Nacimiento</th><th>Cédula</th></tr>
                      </thead>
                      <tbody>
                        {booking.participants.map((p, index) => (
                          <tr key={index} className="plain">
                            <td>{p.firstName}</td>
                            <td>{p.lastName}</td>
                            <td>{new Date(`${p.birthDate}T12:00:00`).toLocaleDateString('es-UY')}</td>
                            <td>{p.document}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
            </article>
          )
        })}
      </section>
    </div>
  )
}

export default ProviderBookingsPage

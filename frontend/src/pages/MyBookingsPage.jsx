import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api, BOOKING_STATUS, formatDate, formatDay, formatMoney, formatTime } from '../api.js'

// CU-18: the tourist sees their bookings and their state. From here they can cancel one (CU-19).
function MyBookingsPage() {
  const [bookings, setBookings] = useState(null)
  const [cancelling, setCancelling] = useState(null)
  const [notice, setNotice] = useState(null)

  useEffect(() => {
    api.listOwnBookings().then(setBookings).catch((e) => setNotice({ type: 'error', text: e.message }))
  }, [])

  async function handleCancel(booking) {
    try {
      await api.cancelOwnBooking(booking.id)
      setBookings(await api.listOwnBookings())
      setNotice({
        type: 'info',
        text: Number(booking.refundAmount) > 0
          ? `Cancelaste la reserva ${booking.code}. Se te devuelven ${formatMoney(booking.refundAmount)}.`
          : `Cancelaste la reserva ${booking.code}.`,
      })
    } catch (e) {
      setNotice({ type: 'error', text: e.message })
    }
    setCancelling(null)
  }

  return (
    <div className="page">
      <h1>Mis reservas</h1>
      <p className="lead">Una reserva no se puede cambiar de fecha: para eso cancelala y reservá de nuevo.</p>

      {notice && <p className={`alert ${notice.type}`}>{notice.text}</p>}
      {bookings === null && !notice && <p className="muted">Cargando…</p>}
      {bookings?.length === 0 && (
        <p className="muted">Todavía no tenés reservas. <Link to="/actividades">Mirá las actividades</Link>.</p>
      )}

      <section className="booking-list">
        {bookings?.map((booking) => {
          const status = BOOKING_STATUS[booking.status]
          const pending = booking.status === 'PENDING_PAYMENT'
          return (
            <article key={booking.id} className="card booking">
              <div className="detail-header">
                <h2><Link to={`/actividades/${booking.activityId}`}>{booking.activityName}</Link></h2>
                <span className={`badge ${status.className}`}>{status.label}</span>
              </div>
              <p className="muted small">{booking.businessName}</p>

              <dl className="inline">
                <div><dt>Fecha</dt><dd>{formatDay(booking.date)} a las {formatTime(booking.startTime)}</dd></div>
                <div><dt>Personas</dt><dd>{booking.participantCount}</dd></div>
                <div><dt>Total</dt><dd>{Number(booking.total) === 0 ? 'Sin costo' : formatMoney(booking.total)}</dd></div>
                <div><dt>Código</dt><dd className="mono">{booking.code}</dd></div>
              </dl>

              {booking.participants.length > 0 && (
                <p className="small">
                  <strong>Participantes:</strong>{' '}
                  {booking.participants.map((p) => `${p.firstName} ${p.lastName}`).join(', ')}
                </p>
              )}

              {pending && (
                <p className="alert info small">
                  Pendiente de pago: tus lugares están retenidos hasta el {formatDate(booking.paymentDeadline)}.
                  {booking.paymentMessage && <> El último intento de pago no salió: {booking.paymentMessage}.</>}
                </p>
              )}

              {cancelling === booking.id ? (
                <div className="confirm">
                  <p>¿Cancelar esta reserva? {booking.cancelNote}</p>
                  <button className="button danger" onClick={() => handleCancel(booking)}>Sí, cancelar</button>
                  <button className="button secondary" onClick={() => setCancelling(null)}>No</button>
                </div>
              ) : (
                <div className="actions">
                  {booking.canCancel && (
                    <button className="button small secondary" onClick={() => setCancelling(booking.id)}>
                      Cancelar reserva
                    </button>
                  )}
                  {!booking.canCancel && booking.status === 'CONFIRMED' && !booking.canReview && (
                    <span className="muted small">{booking.cancelNote}</span>
                  )}
                </div>
              )}
            </article>
          )
        })}
      </section>
    </div>
  )
}

export default MyBookingsPage

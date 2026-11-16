import { useEffect, useState } from 'react'
import { Link, useParams, useSearchParams } from 'react-router-dom'
import { api, formatClock, formatDay, formatMoney, formatTime } from '../api.js'
import Field from '../components/Field.jsx'
import Countdown from '../components/Countdown.jsx'

const EMPTY_PARTICIPANT = { firstName: '', lastName: '', birthDate: '', document: '' }

// CU-15: booking "like a flight". Step 1: how many places (they are held for 10 minutes).
// Step 2: the data of each person. Step 3: what is left to pay, or the confirmation if it is free.
function BookingPage() {
  const { activityId, scheduleId } = useParams()
  // ?reserva=ID continues a booking whose places are already held (it comes from "Mis reservas").
  const [searchParams] = useSearchParams()
  const resumeId = searchParams.get('reserva')
  const [activity, setActivity] = useState(null)
  const [schedule, setSchedule] = useState(null)
  const [count, setCount] = useState(1)
  const [booking, setBooking] = useState(null)
  const [participants, setParticipants] = useState([])
  const [errors, setErrors] = useState({})
  const [message, setMessage] = useState('')
  const [sending, setSending] = useState(false)
  const [timeUp, setTimeUp] = useState(false)

  useEffect(() => {
    api.getActivityDetail(activityId)
      .then((detail) => {
        setActivity(detail.activity)
        setSchedule(detail.schedules.find((s) => String(s.id) === scheduleId) ?? null)
      })
      .catch((e) => setMessage(e.message))
  }, [activityId, scheduleId])

  useEffect(() => {
    if (!resumeId) return
    api.getOwnBooking(resumeId)
      .then((held) => {
        setBooking(held)
      window.scrollTo(0, 0)
        setParticipants(Array.from({ length: held.participantCount }, () => ({ ...EMPTY_PARTICIPANT })))
      })
      .catch((e) => setMessage(e.message))
  }, [resumeId])

  // Step 1 -> 2: asks the backend to hold the places.
  async function handleHold(event) {
    event.preventDefault()
    setMessage('')
    setSending(true)
    try {
      const held = await api.holdBooking({ scheduleId: Number(scheduleId), participantCount: Number(count) })
      setBooking(held)
      setParticipants(Array.from({ length: held.participantCount }, () => ({ ...EMPTY_PARTICIPANT })))
    } catch (e) {
      setMessage(e.message)
      // Someone may have taken places meanwhile: show the ones really left.
      api.getActivityDetail(activityId)
        .then((detail) => setSchedule(detail.schedules.find((s) => String(s.id) === scheduleId) ?? null))
        .catch(() => {})
    } finally {
      setSending(false)
    }
  }

  function updateParticipant(index, field) {
    return (event) => {
      setParticipants(participants.map((p, i) => (i === index ? { ...p, [field]: event.target.value } : p)))
      setErrors({ ...errors, [`participants[${index}].${field}`]: undefined })
    }
  }

  // Step 2 -> 3: saves the participants. A free activity comes back already confirmed.
  async function handleParticipants(event) {
    event.preventDefault()
    setMessage('')
    setSending(true)
    try {
      const data = participants.map((p) => ({ ...p, birthDate: p.birthDate || null }))
      setBooking(await api.saveParticipants(booking.id, data))
      window.scrollTo(0, 0)
      setErrors({})
    } catch (e) {
      setErrors(e.fieldErrors)
      setMessage(e.message)
    } finally {
      setSending(false)
    }
  }

  if (!activity) {
    return (
      <div className="page narrow">
        {message ? <p className="alert error">{message}</p> : <p className="muted">Cargando…</p>}
      </div>
    )
  }

  const back = <p className="small"><Link to={`/actividades/${activityId}`}>← Volver a la actividad</Link></p>

  if (!schedule && !booking && !resumeId) {
    return (
      <div className="page narrow">
        {back}
        <p className="alert error">Esta fecha ya no está disponible.</p>
      </div>
    )
  }

  // Step 3 (free activity): confirmed.
  if (booking?.status === 'CONFIRMED') {
    return (
      <div className="page narrow">
        <div className="success-box">
          <h1>¡Reserva confirmada!</h1>
          <p>
            Reservaste <strong>{booking.activityName}</strong> para el {formatDay(booking.date)} a
            las {formatTime(booking.startTime)}, {booking.participantCount}{' '}
            {booking.participantCount === 1 ? 'persona' : 'personas'}.
          </p>
          <p>Tu código de reserva es <strong className="mono">{booking.code}</strong>. Te lo mandamos también por correo.</p>
          <Link to="/mis-reservas" className="button">Ver mis reservas</Link>
        </div>
      </div>
    )
  }

  // The 10 minutes ran out before finishing (CU-15, 5a).
  if (timeUp) {
    return (
      <div className="page narrow">
        {back}
        <p className="alert error">
          Pasaron los 10 minutos y los lugares se liberaron. Tenés que empezar la reserva de nuevo.
        </p>
        <Link to={`/actividades/${activityId}`} className="button">Volver a la actividad</Link>
      </div>
    )
  }

  // Step 3 (activity with cost): the participants are loaded, what is left is to pay.
  if (booking?.participants.length > 0) {
    return (
      <div className="page narrow">
        <h1>Falta el pago</h1>
        <p className="lead">
          {booking.activityName} · {formatDay(booking.date)} a las {formatTime(booking.startTime)} ·{' '}
          {booking.participantCount} {booking.participantCount === 1 ? 'persona' : 'personas'}
        </p>
        <div className="summary">
          <p>Total a pagar</p>
          <strong>{formatMoney(booking.total)}</strong>
          <p className="small muted">
            {booking.participantCount} × {formatMoney(booking.unitPrice)}
          </p>
        </div>
        <p className="alert info">
          Tus lugares están retenidos hasta las {formatClock(booking.paymentDeadline)} (
          <Countdown until={booking.paymentDeadline} onEnd={() => setTimeUp(true)} />
          ). La reserva se confirma cuando se aprueba el pago.
        </p>
        <Link to={`/mis-reservas/${booking.id}/pagar`} className="button accent">Ir a pagar</Link>
      </div>
    )
  }

  // Step 2: the data of each participant.
  if (booking) {
    return (
      <div className="page narrow">
        <h1>Datos de los participantes</h1>
        <p className="alert info">
          Te guardamos {booking.participantCount} {booking.participantCount === 1 ? 'lugar' : 'lugares'} por 10
          minutos. Te quedan <Countdown until={booking.paymentDeadline} onEnd={() => setTimeUp(true)} />.
        </p>

        <form className="form" onSubmit={handleParticipants} noValidate>
          {participants.map((participant, index) => (
            <fieldset key={index}>
              <legend>Persona {index + 1}</legend>
              <div className="grid">
                <Field label="Nombre" error={errors[`participants[${index}].firstName`]}>
                  <input value={participant.firstName} onChange={updateParticipant(index, 'firstName')} />
                </Field>
                <Field label="Apellido" error={errors[`participants[${index}].lastName`]}>
                  <input value={participant.lastName} onChange={updateParticipant(index, 'lastName')} />
                </Field>
                <Field label="Fecha de nacimiento" error={errors[`participants[${index}].birthDate`]}>
                  <input value={participant.birthDate} onChange={updateParticipant(index, 'birthDate')} type="date" />
                </Field>
                <Field label="Cédula" error={errors[`participants[${index}].document`]}>
                  <input value={participant.document} onChange={updateParticipant(index, 'document')} />
                </Field>
              </div>
            </fieldset>
          ))}

          {message && <p className="alert error">{message}</p>}

          <button className="button" type="submit" disabled={sending}>
            {sending ? 'Guardando…' : 'Siguiente'}
          </button>
        </form>
      </div>
    )
  }

  if (!schedule) return <div className="page narrow"><p className="muted">Cargando…</p></div>

  // Step 1: how many people.
  const free = Number(schedule.price) === 0
  return (
    <div className="page narrow">
      {back}
      <h1>Reservar {activity.name}</h1>
      <p className="lead">
        {formatDay(schedule.date)} a las {formatTime(schedule.startTime)} ·{' '}
        {free ? 'Sin costo' : `${formatMoney(schedule.price)} por persona`}
      </p>

      <form className="form" onSubmit={handleHold}>
        <fieldset>
          <Field label="¿Cuántas personas van?"
            hint={schedule.availableCapacity === 1 ? 'Queda 1 lugar' : `Quedan ${schedule.availableCapacity} lugares`}>
            <input value={count} onChange={(e) => setCount(e.target.value)} type="number" min="1"
              max={schedule.availableCapacity} />
          </Field>
          {!free && count > 0 && (
            <p>Total: <strong>{formatMoney(Number(schedule.price) * Number(count))}</strong></p>
          )}
        </fieldset>

        {message && <p className="alert error">{message}</p>}

        <button className="button accent" type="submit" disabled={sending || !schedule.bookable}>
          {sending ? 'Reservando…' : 'Reservar lugares'}
        </button>
        <p className="note">
          Al continuar te guardamos los lugares por 10 minutos mientras cargás los datos de cada persona.
        </p>
      </form>
    </div>
  )
}

export default BookingPage

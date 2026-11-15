import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { api, formatDate, formatDay, formatMoney, formatTime } from '../api.js'
import Field from '../components/Field.jsx'

// Test cards of the payment sandbox (backend/.../sandbox/PaymentSandboxController.java).
const TEST_CARDS = [
  { number: '4111 1111 1111 1111', result: 'Pago aprobado' },
  { number: '4000 0000 0000 0002', result: 'Pago rechazado' },
  { number: '4000 0000 0000 0500', result: 'El servicio devuelve un error' },
  { number: '4000 0000 0000 0408', result: 'El servicio no responde' },
]

const EMPTY_CARD = { cardNumber: '', holder: '', expiry: '', cvc: '' }

// CU-16: the tourist pays a pending booking with a saved card or a new one. The payment goes
// to the external payment service (sandbox); the booking is confirmed only if it is approved.
function PayBookingPage() {
  const { id } = useParams()
  const [booking, setBooking] = useState(null)
  const [cards, setCards] = useState([])
  const [selected, setSelected] = useState('new') // id of a saved card, or 'new'
  const [form, setForm] = useState(EMPTY_CARD)
  const [errors, setErrors] = useState({})
  const [message, setMessage] = useState('')
  const [paying, setPaying] = useState(false)
  const [seconds, setSeconds] = useState(0)
  const [failed, setFailed] = useState(null) // the last attempt, if it did not go through

  useEffect(() => {
    Promise.all([api.getOwnBooking(id), api.listCards()])
      .then(([own, saved]) => {
        setBooking(own)
        setCards(saved)
        if (saved.length > 0) setSelected(saved[0].id)
      })
      .catch((e) => setMessage(e.message))
  }, [id])

  // Counts the seconds while we wait for the payment service.
  useEffect(() => {
    if (!paying) return undefined
    const timer = setInterval(() => setSeconds((s) => s + 1), 1000)
    return () => clearInterval(timer)
  }, [paying])

  function update(field, transform = (v) => v) {
    return (event) => {
      setForm({ ...form, [field]: transform(event.target.value) })
      setErrors({ ...errors, [field]: undefined })
    }
  }

  const groupCard = (value) => value.replace(/\D/g, '').slice(0, 19).replace(/(\d{4})(?=\d)/g, '$1 ')
  const formatExpiry = (value) => {
    const digits = value.replace(/\D/g, '').slice(0, 4)
    return digits.length > 2 ? `${digits.slice(0, 2)}/${digits.slice(2)}` : digits
  }
  const onlyDigits = (max) => (value) => value.replace(/\D/g, '').slice(0, max)

  function fillTestCard(number) {
    setSelected('new')
    setForm({ cardNumber: number, holder: form.holder || 'Sofía Martínez', expiry: form.expiry || '12/29', cvc: form.cvc || '123' })
    setErrors({})
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setMessage('')
    setFailed(null)
    setSeconds(0)
    setPaying(true)
    try {
      const result = await api.payBooking(id, selected === 'new' ? form : { cardId: selected })
      setBooking(result.booking)
      if (result.payment.status !== 'APPROVED') setFailed(result.payment)
    } catch (e) {
      setErrors(e.fieldErrors)
      setMessage(e.message)
    } finally {
      setPaying(false)
    }
  }

  if (!booking) {
    return (
      <div className="page narrow">
        {message ? <p className="alert error">{message}</p> : <p className="muted">Cargando…</p>}
      </div>
    )
  }

  if (booking.status === 'CONFIRMED') {
    return (
      <div className="page narrow">
        <div className="success-box">
          <h1>¡Reserva confirmada!</h1>
          <p>
            Pagaste {formatMoney(booking.total)} por <strong>{booking.activityName}</strong>, el{' '}
            {formatDay(booking.date)} a las {formatTime(booking.startTime)}.
          </p>
          <p>Tu código de reserva es <strong className="mono">{booking.code}</strong>. Te lo mandamos también por correo.</p>
          <Link to="/mis-reservas" className="button">Ver mis reservas</Link>
        </div>
      </div>
    )
  }

  if (booking.status !== 'PENDING_PAYMENT') {
    return (
      <div className="page narrow">
        <p className="alert info">Esta reserva ya no está pendiente de pago.</p>
        <Link to="/mis-reservas" className="button">Ver mis reservas</Link>
      </div>
    )
  }

  if (booking.participants.length === 0) {
    return (
      <div className="page narrow">
        <p className="alert info">Antes de pagar tenés que cargar los datos de los participantes.</p>
        <Link to={`/reservar/${booking.activityId}/0?reserva=${booking.id}`} className="button">Cargar los datos</Link>
      </div>
    )
  }

  return (
    <div className="page">
      <p className="small"><Link to="/mis-reservas">← Mis reservas</Link></p>
      <h1>Pagar la reserva</h1>
      <p className="lead">
        {booking.activityName} · {formatDay(booking.date)} a las {formatTime(booking.startTime)} ·{' '}
        {booking.participantCount} {booking.participantCount === 1 ? 'persona' : 'personas'}
      </p>

      <div className="admin-layout">
        <section className="list">
          <div className="summary">
            <p>Total a pagar</p>
            <strong>{formatMoney(booking.total)}</strong>
            <p className="small muted">{booking.participantCount} × {formatMoney(booking.unitPrice)}</p>
          </div>

          {failed && <FailedPayment payment={failed} deadline={booking.paymentDeadline} />}

          <form className="form" onSubmit={handleSubmit} noValidate>
            <fieldset>
              <legend>¿Con qué tarjeta?</legend>
              {cards.map((card) => (
                <label key={card.id} className="check">
                  <input type="radio" name="card" checked={selected === card.id} onChange={() => setSelected(card.id)} />
                  Tarjeta guardada terminada en <span className="mono">{card.last4}</span>
                </label>
              ))}
              <label className="check">
                <input type="radio" name="card" checked={selected === 'new'} onChange={() => setSelected('new')} />
                Una tarjeta nueva
              </label>

              {selected === 'new' && (
                <>
                  <div className="grid">
                    <Field label="Número de tarjeta" error={errors.cardNumber}>
                      <input value={form.cardNumber} onChange={update('cardNumber', groupCard)} inputMode="numeric"
                        autoComplete="off" placeholder="4111 1111 1111 1111" />
                    </Field>
                    <Field label="Nombre en la tarjeta" error={errors.holder}>
                      <input value={form.holder} onChange={update('holder')} autoComplete="off" />
                    </Field>
                    <Field label="Vencimiento" error={errors.expiry}>
                      <input value={form.expiry} onChange={update('expiry', formatExpiry)} inputMode="numeric"
                        autoComplete="off" placeholder="MM/AA" />
                    </Field>
                    <Field label="Código de seguridad" error={errors.cvc}>
                      <input value={form.cvc} onChange={update('cvc', onlyDigits(4))} inputMode="numeric"
                        autoComplete="off" placeholder="123" />
                    </Field>
                  </div>
                  <p className="small muted">
                    Si el pago se aprueba, la tarjeta queda guardada para la próxima. No guardamos el número:
                    solo sus últimos 4 dígitos y un identificador que nos da el servicio de pagos.
                  </p>
                </>
              )}
            </fieldset>

            {message && <p className="alert error">{message}</p>}

            <button className="button" type="submit" disabled={paying}>
              {paying ? 'Procesando…' : `Pagar ${formatMoney(booking.total)}`}
            </button>
          </form>

          {paying && (
            <div className="alert info" style={{ marginTop: 16 }}>
              Esperando la respuesta del servicio de pagos… <strong>{seconds} s</strong> (máximo 1 minuto)
            </div>
          )}
        </section>

        <aside className="detail">
          <h2>Tarjetas de prueba</h2>
          <p className="muted">
            El pago lo procesa un servicio externo (sandbox), que responde según el número de tarjeta.
          </p>
          <table className="test-cards">
            <tbody>
              {TEST_CARDS.map((card) => (
                <tr key={card.number}>
                  <td className="mono">{card.number}</td>
                  <td>{card.result}</td>
                  <td>
                    <button type="button" className="button secondary small" onClick={() => fillTestCard(card.number)}>
                      Usar
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </aside>
      </div>
    </div>
  )
}

// RF-23: if the payment does not go through, the booking is not confirmed and stays pending 24 hours.
function FailedPayment({ payment, deadline }) {
  return (
    <div className={`alert ${payment.status === 'REJECTED' ? 'error' : 'info'}`}>
      <strong>{payment.status === 'REJECTED' ? 'Pago rechazado' : 'El pago no se completó'}:</strong>{' '}
      {payment.message}. No se cobró nada y la reserva no está confirmada. Queda pendiente de pago y tus
      lugares siguen retenidos hasta el {formatDate(deadline)}: podés volver a intentar ahora o más tarde
      desde "Mis reservas".
    </div>
  )
}

export default PayBookingPage

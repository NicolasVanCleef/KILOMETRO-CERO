import { useEffect, useState } from 'react'
import { api, formatDate, formatMoney, PAYMENT_STATUS } from '../api.js'
import Field from '../components/Field.jsx'

// Test cards of the payment sandbox (backend/.../sandbox/PaymentSandboxController.java).
const TEST_CARDS = [
  { number: '4111 1111 1111 1111', result: 'Pago aprobado' },
  { number: '4000 0000 0000 0002', result: 'Pago rechazado' },
  { number: '4000 0000 0000 0500', result: 'El servicio devuelve un error' },
  { number: '4000 0000 0000 0408', result: 'El servicio no responde' },
]

const EMPTY_FORM = { amount: '2100', cardNumber: '', holder: '', expiry: '', cvc: '' }

// CU-16: the tourist pays with a card; the payment goes to the external payment service (sandbox).
// For now a test amount is paid: once bookings exist, the amount comes from the booking.
function PaymentPage() {
  const [form, setForm] = useState(EMPTY_FORM)
  const [errors, setErrors] = useState({})
  const [message, setMessage] = useState('')
  const [paying, setPaying] = useState(false)
  const [seconds, setSeconds] = useState(0)
  const [result, setResult] = useState(null)
  const [history, setHistory] = useState([])

  useEffect(() => {
    api.listPayments().then(setHistory).catch(() => {})
  }, [])

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
    setForm({ ...form, cardNumber: number, holder: form.holder || 'Sofía Martínez', expiry: form.expiry || '12/29', cvc: form.cvc || '123' })
    setErrors({})
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setMessage('')
    setResult(null)
    setSeconds(0)
    setPaying(true)
    try {
      const payment = await api.pay({ ...form, amount: Number(form.amount) })
      setResult(payment)
      setHistory(await api.listPayments())
    } catch (e) {
      setErrors(e.fieldErrors)
      setMessage(e.message)
    } finally {
      setPaying(false)
    }
  }

  return (
    <div className="page">
      <h1>Pagar (sandbox)</h1>
      <p className="lead">
        El pago lo procesa un servicio de pagos externo. Uruguay Mnatural le manda los datos de la
        tarjeta, espera la respuesta hasta 1 minuto y guarda el resultado. De la tarjeta solo se
        guardan los últimos 4 dígitos.
      </p>

      <div className="admin-layout">
        <section className="list">
          <form className="form" onSubmit={handleSubmit} noValidate>
            <div className="grid">
              <Field label="Monto ($)" error={errors.amount} hint="Monto de prueba">
                <input value={form.amount} onChange={update('amount', onlyDigits(8))} inputMode="numeric" />
              </Field>
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

            {message && <p className="alert error">{message}</p>}

            <button className="button" type="submit" disabled={paying}>
              {paying ? 'Procesando…' : `Pagar ${form.amount ? formatMoney(form.amount) : ''}`}
            </button>
          </form>

          {paying && (
            <div className="alert info" style={{ marginTop: 16 }}>
              Esperando la respuesta del servicio de pagos… <strong>{seconds} s</strong> (máximo 1 minuto)
            </div>
          )}

          {result && <PaymentResult payment={result} />}
        </section>

        <aside className="detail">
          <h2>Tarjetas de prueba</h2>
          <p className="muted">El sandbox responde según el número de tarjeta.</p>
          <table className="test-cards">
            <tbody>
              {TEST_CARDS.map((card) => (
                <tr key={card.number}>
                  <td className="mono">{card.number}</td>
                  <td>{card.result}</td>
                  <td><button type="button" className="button secondary small" onClick={() => fillTestCard(card.number)}>Usar</button></td>
                </tr>
              ))}
            </tbody>
          </table>
        </aside>
      </div>

      <section className="list" style={{ marginTop: 20 }}>
        <h2>Últimos pagos</h2>
        {history.length === 0 && <p className="muted">Todavía no hay pagos.</p>}
        {history.length > 0 && (
          <table>
            <thead>
              <tr><th>#</th><th>Fecha</th><th>Monto</th><th>Tarjeta</th><th>Estado</th><th>Referencia / motivo</th></tr>
            </thead>
            <tbody>
              {history.map((p) => (
                <tr key={p.id}>
                  <td>{p.id}</td>
                  <td>{formatDate(p.createdAt)}</td>
                  <td>{formatMoney(p.amount)}</td>
                  <td className="mono">•••• {p.cardLast4}</td>
                  <td><span className={`badge ${PAYMENT_STATUS[p.status].className}`}>{PAYMENT_STATUS[p.status].label}</span></td>
                  <td className="mono small">{p.externalReference || p.message}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>
    </div>
  )
}

function PaymentResult({ payment }) {
  if (payment.status === 'APPROVED') {
    return (
      <div className="alert success" style={{ marginTop: 16 }}>
        <strong>Pago aprobado.</strong> Se cobró {formatMoney(payment.amount)} a la tarjeta terminada en{' '}
        {payment.cardLast4}. Referencia del servicio de pagos: <span className="mono">{payment.externalReference}</span>.
      </div>
    )
  }
  if (payment.status === 'REJECTED') {
    return (
      <div className="alert error" style={{ marginTop: 16 }}>
        <strong>Pago rechazado:</strong> {payment.message}. No se cobró nada. Probá con otra tarjeta.
      </div>
    )
  }
  return (
    <div className="alert info" style={{ marginTop: 16 }}>
      <strong>El pago no se completó:</strong> {payment.message}. No se cobró ni se confirmó nada: el pago
      queda pendiente. Cuando estén las reservas, la reserva queda pendiente 24 horas y se puede reintentar.
    </div>
  )
}

export default PaymentPage

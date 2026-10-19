import { useEffect, useRef, useState } from 'react'
import { loadMercadoPago } from '@mercadopago/sdk-js'
import { api, formatDate, formatMoney, PAYMENT_STATUS } from '../api.js'
import Field from '../components/Field.jsx'

// Public Key of Mercado Pago (TEST-...). It goes in frontend/.env.local, which is not uploaded to git.
const PUBLIC_KEY = import.meta.env.VITE_MP_PUBLIC_KEY

// Mercado Pago test cards for Uruguay. The result depends on the card holder's name.
const TEST_CARDS = [
  { brand: 'Visa', number: '4509 9535 6623 3704' },
  { brand: 'Mastercard', number: '5031 7557 3453 0604' },
]
const TEST_NAMES = [
  { name: 'APRO', result: 'Pago aprobado' },
  { name: 'FUND', result: 'Rechazado: fondos insuficientes' },
  { name: 'SECU', result: 'Rechazado: código de seguridad' },
  { name: 'CONT', result: 'Queda pendiente' },
  { name: 'OTHE', result: 'Rechazado: error general' },
]

// CU-16: the tourist pays with a card through Mercado Pago (test credentials = sandbox).
// The card number, expiry and code are typed into Mercado Pago's own secure fields: our
// system only receives a one-time token. For now a test amount is paid.
function PaymentPage() {
  const mpRef = useRef(null)
  const [ready, setReady] = useState(false)
  const [loadError, setLoadError] = useState(PUBLIC_KEY ? '' : 'Falta la Public Key de Mercado Pago (VITE_MP_PUBLIC_KEY en frontend/.env.local).')
  const [docTypes, setDocTypes] = useState([])
  const [paymentMethod, setPaymentMethod] = useState(null)
  const [issuerId, setIssuerId] = useState('')
  const [form, setForm] = useState({ amount: '2100', holder: '', email: 'test@testuser.com', docType: 'CI', docNumber: '12345678' })
  const [errors, setErrors] = useState({})
  const [message, setMessage] = useState('')
  const [paying, setPaying] = useState(false)
  const [seconds, setSeconds] = useState(0)
  const [result, setResult] = useState(null)
  const [history, setHistory] = useState([])

  useEffect(() => {
    api.listPayments().then(setHistory).catch(() => {})
  }, [])

  // Loads Mercado Pago's SDK and mounts its secure fields.
  useEffect(() => {
    if (!PUBLIC_KEY) return undefined
    let cancelled = false
    const mounted = []
    loadMercadoPago()
      .then(async () => {
        if (cancelled) return
        const mp = new window.MercadoPago(PUBLIC_KEY, { locale: 'es-UY' })
        mpRef.current = mp
        const cardNumber = mp.fields.create('cardNumber', { placeholder: 'Número de la tarjeta' }).mount('mp-cardNumber')
        const expiration = mp.fields.create('expirationDate', { placeholder: 'MM/AA' }).mount('mp-expirationDate')
        const code = mp.fields.create('securityCode', { placeholder: '123' }).mount('mp-securityCode')
        mounted.push(cardNumber, expiration, code)

        // With the first digits Mercado Pago tells which card it is (Visa, Mastercard...).
        cardNumber.on('binChange', async ({ bin }) => {
          if (!bin) { setPaymentMethod(null); return }
          const { results } = await mp.getPaymentMethods({ bin })
          const method = results[0]
          setPaymentMethod(method ? { id: method.id, name: method.name, thumbnail: method.secure_thumbnail || method.thumbnail } : null)
          if (method) {
            const issuers = await mp.getIssuers({ paymentMethodId: method.id, bin })
            setIssuerId(issuers[0]?.id ? String(issuers[0].id) : '')
          }
        })
        setDocTypes(await mp.getIdentificationTypes())
        setReady(true)
      })
      .catch(() => setLoadError('No se pudo cargar Mercado Pago. Revisá la conexión a internet.'))
    return () => {
      cancelled = true
      mounted.forEach((field) => { try { field.unmount() } catch { /* already gone */ } })
    }
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

  async function handleSubmit(event) {
    event.preventDefault()
    setMessage('')
    setResult(null)
    if (!form.holder.trim()) { setErrors({ holder: 'Ingresá el nombre del titular.' }); return }
    if (!paymentMethod) { setMessage('Completá el número de tarjeta.'); return }

    setSeconds(0)
    setPaying(true)
    try {
      let token
      try {
        token = await mpRef.current.fields.createCardToken({
          cardholderName: form.holder.trim(),
          identificationType: form.docType,
          identificationNumber: form.docNumber.trim(),
        })
      } catch {
        setMessage('Mercado Pago no aceptó los datos de la tarjeta. Revisá el número, el vencimiento y el código.')
        return
      }
      const payment = await api.pay({
        amount: Number(form.amount),
        cardToken: token.id,
        paymentMethodId: paymentMethod.id,
        issuerId,
        payerEmail: form.email,
        identificationType: form.docType,
        identificationNumber: form.docNumber,
      })
      setResult(payment)
      setHistory(await api.listPayments())
    } catch (e) {
      setErrors(e.fieldErrors ?? {})
      setMessage(e.message)
    } finally {
      setPaying(false)
    }
  }

  return (
    <div className="page">
      <h1>Pagar con Mercado Pago</h1>
      <p className="lead">
        El pago lo procesa Mercado Pago, en su entorno de pruebas (sandbox): no se mueve plata real.
        Los datos de la tarjeta se escriben en campos seguros de Mercado Pago y Uruguay Mnatural nunca
        los ve. Esperamos la respuesta hasta 1 minuto.
      </p>

      {loadError && <p className="alert error">{loadError}</p>}

      <div className="admin-layout">
        <section className="list">
          <form className="form" onSubmit={handleSubmit} noValidate>
            <div className="grid">
              <Field label="Monto ($)" error={errors.amount} hint="Monto de prueba">
                <input value={form.amount} onChange={update('amount', (v) => v.replace(/\D/g, '').slice(0, 8))} inputMode="numeric" />
              </Field>
              <label className="field">
                <span className="field-label">
                  Número de tarjeta {paymentMethod && <span className="muted">· {paymentMethod.name}</span>}
                </span>
                <div id="mp-cardNumber" className="mp-field" />
              </label>
              <label className="field">
                <span className="field-label">Vencimiento</span>
                <div id="mp-expirationDate" className="mp-field" />
              </label>
              <label className="field">
                <span className="field-label">Código de seguridad</span>
                <div id="mp-securityCode" className="mp-field" />
              </label>
              <Field label="Nombre del titular" error={errors.holder} hint="En pruebas, define el resultado (ver al costado)">
                <input value={form.holder} onChange={update('holder')} autoComplete="off" />
              </Field>
              <Field label="Correo" error={errors.payerEmail}>
                <input value={form.email} onChange={update('email')} type="email" />
              </Field>
              <Field label="Tipo de documento" error={errors.identificationType}>
                <select value={form.docType} onChange={update('docType')}>
                  {docTypes.length === 0 && <option value="CI">CI</option>}
                  {docTypes.map((t) => <option key={t.id} value={t.id}>{t.name}</option>)}
                </select>
              </Field>
              <Field label="Número de documento" error={errors.identificationNumber}>
                <input value={form.docNumber} onChange={update('docNumber', (v) => v.replace(/\D/g, ''))} inputMode="numeric" />
              </Field>
            </div>

            {message && <p className="alert error">{message}</p>}

            <button className="button" type="submit" disabled={paying || !ready}>
              {paying ? 'Procesando…' : `Pagar ${form.amount ? formatMoney(form.amount) : ''}`}
            </button>
          </form>

          {paying && (
            <div className="alert info" style={{ marginTop: 16 }}>
              Esperando la respuesta de Mercado Pago… <strong>{seconds} s</strong> (máximo 1 minuto)
            </div>
          )}

          {result && <PaymentResult payment={result} />}
        </section>

        <aside className="detail">
          <h2>Datos de prueba</h2>
          <p className="muted">Tarjetas de prueba de Mercado Pago para Uruguay. Vencimiento 11/30, código 123.</p>
          <table className="test-cards">
            <tbody>
              {TEST_CARDS.map((card) => (
                <tr key={card.number}><td>{card.brand}</td><td className="mono">{card.number}</td></tr>
              ))}
            </tbody>
          </table>
          <h3>El nombre del titular define el resultado</h3>
          <table className="test-cards">
            <tbody>
              {TEST_NAMES.map((t) => (
                <tr key={t.name}>
                  <td className="mono">{t.name}</td>
                  <td>{t.result}</td>
                  <td><button type="button" className="button secondary small" onClick={() => setForm({ ...form, holder: t.name })}>Usar</button></td>
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
              <tr><th>#</th><th>Fecha</th><th>Monto</th><th>Tarjeta</th><th>Estado</th><th>Id Mercado Pago / motivo</th></tr>
            </thead>
            <tbody>
              {history.map((p) => (
                <tr key={p.id}>
                  <td>{p.id}</td>
                  <td>{formatDate(p.createdAt)}</td>
                  <td>{formatMoney(p.amount)}</td>
                  <td className="mono">{p.cardLast4 ? `•••• ${p.cardLast4}` : '—'}</td>
                  <td><span className={`badge ${PAYMENT_STATUS[p.status].className}`}>{PAYMENT_STATUS[p.status].label}</span></td>
                  <td className="small">{[p.externalReference, p.message].filter(Boolean).join(' · ')}</td>
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
        <strong>Pago aprobado por Mercado Pago.</strong> Se cobró {formatMoney(payment.amount)} a la tarjeta
        terminada en {payment.cardLast4}. Id del pago en Mercado Pago: <span className="mono">{payment.externalReference}</span>.
      </div>
    )
  }
  if (payment.status === 'REJECTED') {
    return (
      <div className="alert error" style={{ marginTop: 16 }}>
        <strong>Pago rechazado:</strong> {payment.message}. No se cobró nada.
      </div>
    )
  }
  return (
    <div className="alert info" style={{ marginTop: 16 }}>
      <strong>El pago no se completó:</strong> {payment.message}. No se confirmó nada: queda pendiente.
      Cuando estén las reservas, la reserva queda pendiente 24 horas y se puede reintentar.
    </div>
  )
}

export default PaymentPage

import { useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api.js'
import Field from '../components/Field.jsx'

// CU-05, first step: the user asks for the email with the link to change the password.
function ForgotPasswordPage() {
  const [email, setEmail] = useState('')
  const [errors, setErrors] = useState({})
  const [message, setMessage] = useState('')
  const [sending, setSending] = useState(false)
  const [sent, setSent] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    setMessage('')
    setErrors({})
    setSending(true)
    try {
      await api.requestPasswordReset(email)
      setSent(true)
    } catch (e) {
      setErrors(e.fieldErrors)
      setMessage(e.message)
    } finally {
      setSending(false)
    }
  }

  if (sent) {
    return (
      <div className="page narrow">
        <div className="success-box">
          <h1>Te mandamos un correo</h1>
          <p>
            Enviamos a <strong>{email}</strong> un enlace para poner una contraseña nueva. Vale por
            30 minutos.
          </p>
          <p className="small muted">
            En esta versión los correos son simulados: el enlace aparece en la consola del backend.
          </p>
          <Link to="/ingresar" className="button">Volver a iniciar sesión</Link>
        </div>
      </div>
    )
  }

  return (
    <div className="page narrow">
      <h1>Recuperar contraseña</h1>
      <p className="lead">Ingresá el correo de tu cuenta y te mandamos un enlace para cambiarla.</p>

      <form className="form" onSubmit={handleSubmit} noValidate>
        <fieldset>
          <Field label="Correo electrónico" error={errors.email}>
            <input value={email} onChange={(e) => setEmail(e.target.value)} type="email" autoComplete="email" />
          </Field>
        </fieldset>

        {message && <p className="alert error">{message}</p>}

        <button className="button" type="submit" disabled={sending}>
          {sending ? 'Enviando…' : 'Enviar enlace'}
        </button>
      </form>
    </div>
  )
}

export default ForgotPasswordPage

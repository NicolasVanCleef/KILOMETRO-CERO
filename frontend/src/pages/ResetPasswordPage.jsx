import { useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { api } from '../api.js'
import Field from '../components/Field.jsx'

// CU-05, second step: the link of the email opens this page with ?token=... in the address.
function ResetPasswordPage() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token')

  const [password, setPassword] = useState('')
  const [confirmation, setConfirmation] = useState('')
  const [errors, setErrors] = useState({})
  const [message, setMessage] = useState('')
  const [sending, setSending] = useState(false)
  const [done, setDone] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    setMessage('')
    if (password !== confirmation) {
      setErrors({ confirmation: 'Las contraseñas no coinciden.' })
      return
    }
    setErrors({})
    setSending(true)
    try {
      await api.confirmPasswordReset(token, password)
      setDone(true)
    } catch (e) {
      setErrors(e.fieldErrors)
      setMessage(e.message)
    } finally {
      setSending(false)
    }
  }

  if (!token) {
    return (
      <div className="page narrow">
        <p className="alert error">
          Este enlace no es válido. <Link to="/recuperar">Pedí uno nuevo</Link>.
        </p>
      </div>
    )
  }

  if (done) {
    return (
      <div className="page narrow">
        <div className="success-box">
          <h1>Contraseña cambiada</h1>
          <p>Ya podés iniciar sesión con tu contraseña nueva.</p>
          <Link to="/ingresar" className="button">Iniciar sesión</Link>
        </div>
      </div>
    )
  }

  return (
    <div className="page narrow">
      <h1>Poner una contraseña nueva</h1>

      <form className="form" onSubmit={handleSubmit} noValidate>
        <fieldset>
          <Field label="Contraseña nueva" error={errors.password} hint="Al menos 8 caracteres">
            <input value={password} onChange={(e) => setPassword(e.target.value)} type="password"
              autoComplete="new-password" />
          </Field>
          <Field label="Repetir contraseña" error={errors.confirmation}>
            <input value={confirmation} onChange={(e) => setConfirmation(e.target.value)} type="password"
              autoComplete="new-password" />
          </Field>
        </fieldset>

        {message && (
          <p className="alert error">
            {message} <Link to="/recuperar">Recuperar contraseña</Link>
          </p>
        )}

        <button className="button" type="submit" disabled={sending}>
          {sending ? 'Guardando…' : 'Guardar contraseña'}
        </button>
      </form>
    </div>
  )
}

export default ResetPasswordPage

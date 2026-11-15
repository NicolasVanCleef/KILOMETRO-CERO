import { useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api.js'
import Field from '../components/Field.jsx'

// CU-04: the user logs in with email and password. The same screen is for the three roles.
function LoginPage({ onLogin }) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [errors, setErrors] = useState({})
  const [message, setMessage] = useState('')
  const [sending, setSending] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    setMessage('')
    setErrors({})
    setSending(true)
    try {
      onLogin(await api.login({ email, password }))
    } catch (e) {
      setErrors(e.fieldErrors)
      setMessage(e.message)
      setSending(false)
    }
  }

  return (
    <div className="page narrow">
      <h1>Iniciar sesión</h1>
      <p className="lead">Entrá con tu correo y tu contraseña.</p>

      <form className="form" onSubmit={handleSubmit} noValidate>
        <fieldset>
          <Field label="Correo electrónico" error={errors.email}>
            <input value={email} onChange={(e) => setEmail(e.target.value)} type="email" autoComplete="username" />
          </Field>
          <Field label="Contraseña" error={errors.password}>
            <input value={password} onChange={(e) => setPassword(e.target.value)} type="password"
              autoComplete="current-password" />
          </Field>
          <p className="small"><Link to="/recuperar">Me olvidé la contraseña</Link></p>
        </fieldset>

        {message && <p className="alert error">{message}</p>}

        <button className="button" type="submit" disabled={sending}>
          {sending ? 'Entrando…' : 'Entrar'}
        </button>
      </form>

      <p className="note">
        ¿No tenés cuenta? <Link to="/registro">Registrate como turista</Link> o{' '}
        <Link to="/registro-prestador">registrá tu emprendimiento</Link>.
      </p>
    </div>
  )
}

export default LoginPage

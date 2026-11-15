import { useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api.js'
import Field from '../components/Field.jsx'
import TouristFields from '../components/TouristFields.jsx'

const EMPTY_FORM = {
  firstName: '',
  lastName: '',
  country: '',
  birthDate: '',
  preferredLanguage: '',
  document: '',
  email: '',
  password: '',
  passwordConfirmation: '',
}

// CU-03: a visitor creates a tourist account to be able to book.
function RegisterTouristPage() {
  const [form, setForm] = useState(EMPTY_FORM)
  const [errors, setErrors] = useState({})
  const [message, setMessage] = useState('')
  const [sending, setSending] = useState(false)
  const [registered, setRegistered] = useState(null)

  function update(field) {
    return (event) => {
      setForm({ ...form, [field]: event.target.value })
      setErrors({ ...errors, [field]: undefined })
    }
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setMessage('')

    if (form.password !== form.passwordConfirmation) {
      setErrors({ passwordConfirmation: 'Las contraseñas no coinciden.' })
      return
    }

    const { passwordConfirmation: _, ...data } = form
    setSending(true)
    try {
      // An empty date goes as null so the backend answers "Ingresá tu fecha de nacimiento".
      setRegistered(await api.registerTourist({ ...data, birthDate: data.birthDate || null }))
    } catch (e) {
      setErrors(e.fieldErrors)
      setMessage(e.message)
    } finally {
      setSending(false)
    }
  }

  if (registered) {
    return (
      <div className="page narrow">
        <div className="success-box">
          <h1>¡Listo, {registered.firstName}!</h1>
          <p>Tu cuenta quedó creada con el correo <strong>{registered.email}</strong>.</p>
          <Link to="/ingresar" className="button">Iniciar sesión</Link>
        </div>
      </div>
    )
  }

  return (
    <div className="page narrow">
      <h1>Crear cuenta de turista</h1>
      <p className="lead">Con tu cuenta vas a poder reservar actividades y ver tus reservas.</p>

      <form className="form" onSubmit={handleSubmit} noValidate>
        <fieldset>
          <legend>Tus datos</legend>
          <TouristFields form={form} errors={errors} update={update} />
        </fieldset>

        <fieldset>
          <legend>Para iniciar sesión</legend>
          <Field label="Correo electrónico" error={errors.email}>
            <input value={form.email} onChange={update('email')} type="email" autoComplete="email" />
          </Field>
          <div className="grid">
            <Field label="Contraseña" error={errors.password} hint="Al menos 8 caracteres">
              <input value={form.password} onChange={update('password')} type="password" autoComplete="new-password" />
            </Field>
            <Field label="Repetir contraseña" error={errors.passwordConfirmation}>
              <input value={form.passwordConfirmation} onChange={update('passwordConfirmation')}
                type="password" autoComplete="new-password" />
            </Field>
          </div>
        </fieldset>

        {message && <p className="alert error">{message}</p>}

        <button className="button" type="submit" disabled={sending}>
          {sending ? 'Creando cuenta…' : 'Crear cuenta'}
        </button>
      </form>

      <p className="note">
        ¿Ya tenés cuenta? <Link to="/ingresar">Iniciá sesión</Link>. ¿Ofrecés actividades?{' '}
        <Link to="/registro-prestador">Registrá tu emprendimiento</Link>.
      </p>
    </div>
  )
}

export default RegisterTouristPage

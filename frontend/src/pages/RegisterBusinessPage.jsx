import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api.js'
import Field from '../components/Field.jsx'

const EMPTY_FORM = {
  personType: 'INDIVIDUAL',
  document: '',
  legalName: '',
  representativeName: '',
  phone: '',
  email: '',
  department: '',
  locality: '',
  address: '',
  description: '',
  password: '',
  passwordConfirmation: '',
}

// CU-01: a provider registers their business. It stays pending until an administrator reviews it.
function RegisterBusinessPage() {
  const [form, setForm] = useState(EMPTY_FORM)
  const [departments, setDepartments] = useState([])
  const [errors, setErrors] = useState({})
  const [message, setMessage] = useState('')
  const [sending, setSending] = useState(false)
  const [registered, setRegistered] = useState(null)

  useEffect(() => {
    api.getDepartments().then(setDepartments).catch((e) => setMessage(e.message))
  }, [])

  const isCompany = form.personType === 'COMPANY'

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
      setRegistered(await api.registerBusiness(data))
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
          <h1>¡Listo! Recibimos tu solicitud</h1>
          <p>
            Registramos <strong>{registered.legalName}</strong>. Un administrador de Uruguay
            Mnatural va a revisar los datos y te va a avisar por correo a{' '}
            <strong>{registered.email}</strong>.
          </p>
          <p>Hasta que el emprendimiento esté aprobado no vas a poder iniciar sesión.</p>
          <Link to="/" className="button">Volver al inicio</Link>
        </div>
      </div>
    )
  }

  return (
    <div className="page narrow">
      <h1>Registrar emprendimiento</h1>
      <p className="lead">
        Completá los datos de tu emprendimiento. Queda pendiente hasta que un administrador lo apruebe.
      </p>

      <form className="form" onSubmit={handleSubmit} noValidate>
        <fieldset>
          <legend>¿Quién ofrece las actividades?</legend>
          <div className="choice">
            <label className={form.personType === 'INDIVIDUAL' ? 'selected' : ''}>
              <input type="radio" name="personType" value="INDIVIDUAL"
                checked={form.personType === 'INDIVIDUAL'} onChange={update('personType')} />
              <strong>Persona física</strong>
              <span>Un particular, se identifica con CI</span>
            </label>
            <label className={isCompany ? 'selected' : ''}>
              <input type="radio" name="personType" value="COMPANY"
                checked={isCompany} onChange={update('personType')} />
              <strong>Persona jurídica</strong>
              <span>Una empresa, se identifica con RUT</span>
            </label>
          </div>
        </fieldset>

        <fieldset>
          <legend>Identificación</legend>
          <div className="grid">
            <Field label={isCompany ? 'RUT' : 'CI'} error={errors.document}
              hint={isCompany ? '12 dígitos' : 'Con o sin puntos y guion'}>
              <input value={form.document} onChange={update('document')} inputMode="numeric" />
            </Field>
            <Field label="Razón social" error={errors.legalName}>
              <input value={form.legalName} onChange={update('legalName')} />
            </Field>
            {isCompany && (
              <Field label="Representante" error={errors.representativeName}>
                <input value={form.representativeName} onChange={update('representativeName')} />
              </Field>
            )}
          </div>
        </fieldset>

        <fieldset>
          <legend>Contacto y ubicación</legend>
          <div className="grid">
            <Field label="Teléfono" error={errors.phone}>
              <input value={form.phone} onChange={update('phone')} type="tel" />
            </Field>
            <Field label="Correo electrónico" error={errors.email} hint="Con este correo vas a iniciar sesión">
              <input value={form.email} onChange={update('email')} type="email" />
            </Field>
            <Field label="Departamento" error={errors.department}>
              <select value={form.department} onChange={update('department')}>
                <option value="">Elegí…</option>
                {departments.map((d) => <option key={d}>{d}</option>)}
              </select>
            </Field>
            <Field label="Localidad" error={errors.locality}>
              <input value={form.locality} onChange={update('locality')} />
            </Field>
            <Field label="Dirección (opcional)" error={errors.address}>
              <input value={form.address} onChange={update('address')} />
            </Field>
          </div>
          <Field label="Descripción de los servicios" error={errors.description}>
            <textarea rows="4" value={form.description} onChange={update('description')}
              placeholder="Qué actividades ofrecés, dónde, para quién…" />
          </Field>
        </fieldset>

        <fieldset>
          <legend>Contraseña</legend>
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
          {sending ? 'Enviando…' : 'Enviar solicitud'}
        </button>
      </form>
    </div>
  )
}

export default RegisterBusinessPage

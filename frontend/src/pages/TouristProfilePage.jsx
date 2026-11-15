import { useEffect, useState } from 'react'
import { api } from '../api.js'
import Field from '../components/Field.jsx'
import TouristFields from '../components/TouristFields.jsx'

// CU-06: the tourist sees and changes their data. The email cannot be changed.
function TouristProfilePage({ onNameChange }) {
  const [form, setForm] = useState(null)
  const [errors, setErrors] = useState({})
  const [notice, setNotice] = useState(null)
  const [sending, setSending] = useState(false)

  useEffect(() => {
    api.getTouristProfile()
      .then(setForm)
      .catch((e) => setNotice({ type: 'error', text: e.message }))
  }, [])

  function update(field) {
    return (event) => {
      setForm({ ...form, [field]: event.target.value })
      setErrors({ ...errors, [field]: undefined })
      setNotice(null)
    }
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setNotice(null)
    setSending(true)
    try {
      const { id: _id, email: _email, ...data } = form
      const saved = await api.updateTouristProfile({ ...data, birthDate: data.birthDate || null })
      setForm(saved)
      setErrors({})
      setNotice({ type: 'success', text: 'Guardamos tus datos.' })
      onNameChange(saved.firstName)
    } catch (e) {
      setErrors(e.fieldErrors)
      setNotice({ type: 'error', text: e.message })
    } finally {
      setSending(false)
    }
  }

  if (!form) {
    return (
      <div className="page narrow">
        {notice ? <p className="alert error">{notice.text}</p> : <p className="muted">Cargando…</p>}
      </div>
    )
  }

  return (
    <div className="page narrow">
      <h1>Mi perfil</h1>
      <p className="lead">Podés cambiar todos tus datos menos el correo, que es con el que iniciás sesión.</p>

      <form className="form" onSubmit={handleSubmit} noValidate>
        <fieldset>
          <Field label="Correo electrónico" hint="No se puede cambiar">
            <input value={form.email} disabled />
          </Field>
          <TouristFields form={form} errors={errors} update={update} />
        </fieldset>

        {notice && <p className={`alert ${notice.type}`}>{notice.text}</p>}

        <button className="button" type="submit" disabled={sending}>
          {sending ? 'Guardando…' : 'Guardar cambios'}
        </button>
      </form>
    </div>
  )
}

export default TouristProfilePage

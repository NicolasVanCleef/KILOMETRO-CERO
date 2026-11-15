import { useEffect, useState } from 'react'
import { api, PERSON_TYPE_LABELS } from '../api.js'
import Field from '../components/Field.jsx'

// CU-06 / RF-33: the provider's own space to manage the data of their business.
// Person type, document and legal name are shown but cannot be changed: the administrator
// approved the business with those.
function BusinessProfilePage() {
  const [business, setBusiness] = useState(null)
  const [form, setForm] = useState(null)
  const [departments, setDepartments] = useState([])
  const [errors, setErrors] = useState({})
  const [notice, setNotice] = useState(null)
  const [sending, setSending] = useState(false)

  useEffect(() => {
    Promise.all([api.getOwnBusiness(), api.getDepartments()])
      .then(([own, list]) => {
        setBusiness(own)
        setForm(toForm(own))
        setDepartments(list)
      })
      .catch((e) => setNotice({ type: 'error', text: e.message }))
  }, [])

  function update(field) {
    return (event) => {
      setForm({ ...form, [field]: event.target.value })
      setErrors({ ...errors, [field]: undefined })
      setNotice(null)
    }
  }

  function updatePhone(event) {
    setForm({ ...form, phone: event.target.value.replace(/\D/g, '').slice(0, 9) })
    setErrors({ ...errors, phone: undefined })
    setNotice(null)
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setNotice(null)
    setSending(true)
    try {
      const saved = await api.updateOwnBusiness(form)
      setBusiness(saved)
      setForm(toForm(saved))
      setErrors({})
      setNotice({ type: 'success', text: 'Guardamos los datos de tu emprendimiento.' })
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

  const isCompany = business.personType === 'COMPANY'

  return (
    <div className="page narrow">
      <h1>Mi emprendimiento</h1>
      <p className="lead">
        Podés cambiar el contacto, la ubicación y la descripción. La identificación no se puede
        cambiar porque es la que aprobó Uruguay Mnatural.
      </p>

      <form className="form" onSubmit={handleSubmit} noValidate>
        <fieldset>
          <legend>Identificación (no se puede cambiar)</legend>
          <div className="grid">
            <Field label="Tipo">
              <input value={PERSON_TYPE_LABELS[business.personType]} disabled />
            </Field>
            <Field label={isCompany ? 'RUT' : 'CI'}>
              <input value={business.document} disabled />
            </Field>
            <Field label="Razón social">
              <input value={business.legalName} disabled />
            </Field>
            <Field label="Correo electrónico">
              <input value={business.email} disabled />
            </Field>
          </div>
        </fieldset>

        <fieldset>
          <legend>Contacto y ubicación</legend>
          <div className="grid">
            {isCompany && (
              <Field label="Representante" error={errors.representativeName}>
                <input value={form.representativeName} onChange={update('representativeName')} />
              </Field>
            )}
            <Field label="Teléfono" error={errors.phone} hint="9 dígitos, por ejemplo 099123456">
              <input value={form.phone} onChange={updatePhone} type="tel" inputMode="numeric" maxLength={9} />
            </Field>
            <Field label="Departamento" error={errors.department}>
              <select value={form.department} onChange={update('department')}>
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
            <textarea rows="4" value={form.description} onChange={update('description')} />
          </Field>
        </fieldset>

        {notice && <p className={`alert ${notice.type}`}>{notice.text}</p>}

        <button className="button" type="submit" disabled={sending}>
          {sending ? 'Guardando…' : 'Guardar cambios'}
        </button>
      </form>
    </div>
  )
}

// Only the fields the provider can change, with empty text instead of null for the inputs.
function toForm(business) {
  return {
    phone: business.phone,
    representativeName: business.representativeName ?? '',
    description: business.description,
    department: business.department,
    locality: business.locality,
    address: business.address ?? '',
  }
}

export default BusinessProfilePage

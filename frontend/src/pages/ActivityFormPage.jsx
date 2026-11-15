import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { api } from '../api.js'
import Field from '../components/Field.jsx'

const EMPTY_FORM = {
  name: '',
  description: '',
  requirements: '',
  observations: '',
  department: '',
  locality: '',
  address: '',
  latitude: '',
  longitude: '',
  durationMinutes: '',
  defaultCapacity: '',
  defaultPrice: '',
  minBookingMinutes: '0',
  maxBookingMinutes: '',
  cancellable: false,
  cancellationMinutes: '',
  refundable: false,
  refundPercentage: '',
  tagIds: [],
  requiredEquipmentIds: [],
  providedEquipmentIds: [],
}

// CU-10: form to create an activity (no id in the address) or modify one (with id).
function ActivityFormPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [form, setForm] = useState(null)
  const [lists, setLists] = useState({ departments: [], tags: [], equipment: [] })
  const [errors, setErrors] = useState({})
  const [message, setMessage] = useState('')
  const [sending, setSending] = useState(false)

  useEffect(() => {
    Promise.all([
      api.getDepartments(),
      api.listTags(),
      api.listEquipment(),
      id ? api.getOwnActivity(id) : null,
    ])
      .then(([departments, tags, equipment, activity]) => {
        setLists({ departments, tags, equipment })
        setForm(activity ? toForm(activity) : EMPTY_FORM)
      })
      .catch((e) => setMessage(e.message))
  }, [id])

  function update(field) {
    return (event) => {
      const value = event.target.type === 'checkbox' ? event.target.checked : event.target.value
      setForm({ ...form, [field]: value })
      setErrors({ ...errors, [field]: undefined })
    }
  }

  // Adds or removes an id from one of the lists (tags, required or provided equipment).
  function toggle(field, itemId) {
    const current = form[field]
    const next = current.includes(itemId) ? current.filter((x) => x !== itemId) : [...current, itemId]
    const changes = { [field]: next }
    // The same equipment cannot be required and provided at once: ticking one side unticks the other.
    if (field === 'requiredEquipmentIds') {
      changes.providedEquipmentIds = form.providedEquipmentIds.filter((x) => x !== itemId)
    }
    if (field === 'providedEquipmentIds') {
      changes.requiredEquipmentIds = form.requiredEquipmentIds.filter((x) => x !== itemId)
    }
    setForm({ ...form, ...changes })
    setErrors({ ...errors, [field]: undefined })
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setMessage('')
    setSending(true)
    try {
      const data = toRequest(form)
      if (id) await api.updateOwnActivity(id, data)
      else await api.createOwnActivity(data)
      navigate('/prestador/actividades')
    } catch (e) {
      setErrors(e.fieldErrors)
      setMessage(e.message)
      setSending(false)
    }
  }

  if (!form) {
    return (
      <div className="page narrow">
        {message ? <p className="alert error">{message}</p> : <p className="muted">Cargando…</p>}
      </div>
    )
  }

  return (
    <div className="page narrow">
      <p className="small"><Link to="/prestador/actividades">← Mis actividades</Link></p>
      <h1>{id ? 'Modificar actividad' : 'Nueva actividad'}</h1>

      <form className="form" onSubmit={handleSubmit} noValidate>
        <fieldset>
          <legend>La actividad</legend>
          <Field label="Nombre" error={errors.name}>
            <input value={form.name} onChange={update('name')} />
          </Field>
          <Field label="Descripción" error={errors.description}>
            <textarea rows="4" value={form.description} onChange={update('description')} />
          </Field>
          <Field label="Requisitos (opcional)" error={errors.requirements} hint="Por ejemplo, edad mínima">
            <textarea rows="2" value={form.requirements} onChange={update('requirements')} />
          </Field>
          <Field label="Observaciones (opcional)" error={errors.observations}>
            <textarea rows="2" value={form.observations} onChange={update('observations')} />
          </Field>
          <Field label="Duración aproximada (minutos)" error={errors.durationMinutes}>
            <input value={form.durationMinutes} onChange={update('durationMinutes')} type="number" min="1" />
          </Field>
        </fieldset>

        <fieldset>
          <legend>Etiquetas</legend>
          <CheckList items={lists.tags} selected={form.tagIds} onToggle={(itemId) => toggle('tagIds', itemId)}
            empty="El administrador todavía no cargó etiquetas." />
          {errors.tagIds && <span className="field-error">{errors.tagIds}</span>}
        </fieldset>

        <fieldset>
          <legend>Ubicación</legend>
          <div className="grid">
            <Field label="Departamento" error={errors.department}>
              <select value={form.department} onChange={update('department')}>
                <option value="">Elegí…</option>
                {lists.departments.map((d) => <option key={d}>{d}</option>)}
              </select>
            </Field>
            <Field label="Localidad" error={errors.locality}>
              <input value={form.locality} onChange={update('locality')} />
            </Field>
            <Field label="Dirección (opcional)" error={errors.address}>
              <input value={form.address} onChange={update('address')} />
            </Field>
            <Field label="Latitud (opcional)" error={errors.latitude} hint="Por ejemplo -34.65">
              <input value={form.latitude} onChange={update('latitude')} type="number" step="any" />
            </Field>
            <Field label="Longitud (opcional)" error={errors.longitude} hint="Por ejemplo -54.16">
              <input value={form.longitude} onChange={update('longitude')} type="number" step="any" />
            </Field>
          </div>
        </fieldset>

        <fieldset>
          <legend>Precio, cupo y reservas</legend>
          <div className="grid">
            <Field label="Precio por persona ($)" error={errors.defaultPrice} hint="0 si no tiene costo">
              <input value={form.defaultPrice} onChange={update('defaultPrice')} type="number" min="0" step="any" />
            </Field>
            <Field label="Cupo" error={errors.defaultCapacity} hint="Personas por fecha">
              <input value={form.defaultCapacity} onChange={update('defaultCapacity')} type="number" min="1" />
            </Field>
            <Field label="Se puede reservar hasta (minutos antes)" error={errors.minBookingMinutes}
              hint="0 = hasta la hora de inicio">
              <input value={form.minBookingMinutes} onChange={update('minBookingMinutes')} type="number" min="0" />
            </Field>
            <Field label="Se puede reservar desde (minutos antes, opcional)" error={errors.maxBookingMinutes}
              hint="Vacío = sin límite">
              <input value={form.maxBookingMinutes} onChange={update('maxBookingMinutes')} type="number" min="1" />
            </Field>
          </div>
          <p className="small muted">El precio y el cupo son los valores por defecto: cada fecha puede tener los suyos.</p>
        </fieldset>

        <fieldset>
          <legend>Cancelación</legend>
          <label className="check">
            <input type="checkbox" checked={form.cancellable} onChange={update('cancellable')} />
            El turista puede cancelar su reserva
          </label>
          {form.cancellable && (
            <>
              <Field label="Hasta cuántos minutos antes del inicio" error={errors.cancellationMinutes}
                hint="Por ejemplo 1440 = un día antes">
                <input value={form.cancellationMinutes} onChange={update('cancellationMinutes')} type="number" min="0" />
              </Field>
              <label className="check">
                <input type="checkbox" checked={form.refundable} onChange={update('refundable')} />
                La cancelación tiene reembolso
              </label>
              {form.refundable && (
                <Field label="Porcentaje que se devuelve" error={errors.refundPercentage} hint="De 0 a 100">
                  <input value={form.refundPercentage} onChange={update('refundPercentage')} type="number"
                    min="0" max="100" />
                </Field>
              )}
            </>
          )}
        </fieldset>

        <fieldset>
          <legend>Equipamiento</legend>
          <p className="field-label">Lo tiene que llevar el turista</p>
          <CheckList items={lists.equipment} selected={form.requiredEquipmentIds}
            onToggle={(itemId) => toggle('requiredEquipmentIds', itemId)}
            empty="El administrador todavía no cargó equipamiento." />
          <p className="field-label">Lo da el prestador</p>
          <CheckList items={lists.equipment} selected={form.providedEquipmentIds}
            onToggle={(itemId) => toggle('providedEquipmentIds', itemId)} empty="" />
          {errors.providedEquipmentIds && <span className="field-error">{errors.providedEquipmentIds}</span>}
        </fieldset>

        {message && <p className="alert error">{message}</p>}

        <button className="button" type="submit" disabled={sending}>
          {sending ? 'Guardando…' : 'Guardar actividad'}
        </button>
      </form>
    </div>
  )
}

function CheckList({ items, selected, onToggle, empty }) {
  if (items.length === 0) return <p className="muted small">{empty}</p>
  return (
    <div className="check-list">
      {items.map((item) => (
        <label key={item.id} className={`chip${selected.includes(item.id) ? ' selected' : ''}`}>
          <input type="checkbox" checked={selected.includes(item.id)} onChange={() => onToggle(item.id)} />
          {item.name}
        </label>
      ))}
    </div>
  )
}

// The inputs work with text; null becomes '' and lists of {id, name} become lists of ids.
function toForm(activity) {
  const text = (value) => (value === null || value === undefined ? '' : String(value))
  return {
    ...EMPTY_FORM,
    ...Object.fromEntries(Object.keys(EMPTY_FORM).filter((key) => key in activity).map((key) => [key, text(activity[key])])),
    cancellable: activity.cancellable,
    refundable: activity.refundable,
    tagIds: activity.tags.map((tag) => tag.id),
    requiredEquipmentIds: activity.requiredEquipment.map((item) => item.id),
    providedEquipmentIds: activity.providedEquipment.map((item) => item.id),
  }
}

// What the backend expects: numbers as numbers and empty fields as null.
function toRequest(form) {
  const number = (value) => (value === '' ? null : Number(value))
  return {
    ...form,
    latitude: number(form.latitude),
    longitude: number(form.longitude),
    durationMinutes: number(form.durationMinutes),
    defaultCapacity: number(form.defaultCapacity),
    defaultPrice: number(form.defaultPrice),
    minBookingMinutes: number(form.minBookingMinutes),
    maxBookingMinutes: number(form.maxBookingMinutes),
    cancellationMinutes: number(form.cancellationMinutes),
    refundPercentage: number(form.refundPercentage),
  }
}

export default ActivityFormPage

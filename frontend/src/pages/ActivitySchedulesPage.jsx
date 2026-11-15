import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { api, formatDay, formatMoney, formatTime } from '../api.js'
import Field from '../components/Field.jsx'

// CU-11 and CU-12: the provider schedules the activity on concrete dates and can cancel a date.
function ActivitySchedulesPage() {
  const { id } = useParams()
  const [activity, setActivity] = useState(null)
  const [schedules, setSchedules] = useState([])
  const [form, setForm] = useState({ date: '', startTime: '', capacity: '', price: '' })
  const [errors, setErrors] = useState({})
  const [notice, setNotice] = useState(null)
  const [cancelling, setCancelling] = useState(null)
  // The moment the page was opened, to tell past dates from future ones.
  const [now] = useState(() => new Date())

  useEffect(() => {
    Promise.all([api.getOwnActivity(id), api.listOwnSchedules(id)])
      .then(([own, list]) => {
        setActivity(own)
        setSchedules(list)
        // CU-11 step 2: the form proposes the activity's default capacity and price.
        setForm((current) => ({ ...current, capacity: String(own.defaultCapacity), price: String(own.defaultPrice) }))
      })
      .catch((e) => setNotice({ type: 'error', text: e.message }))
  }, [id])

  function update(field) {
    return (event) => {
      setForm({ ...form, [field]: event.target.value })
      setErrors({ ...errors, [field]: undefined })
    }
  }

  async function handleCreate(event) {
    event.preventDefault()
    setNotice(null)
    try {
      await api.createOwnSchedule(id, {
        date: form.date || null,
        startTime: form.startTime || null,
        capacity: form.capacity === '' ? null : Number(form.capacity),
        price: form.price === '' ? null : Number(form.price),
      })
      setSchedules(await api.listOwnSchedules(id))
      setForm({ ...form, date: '', startTime: '' })
      setErrors({})
      setNotice({ type: 'success', text: 'Fecha agregada. Ya se puede reservar desde el portal.' })
    } catch (e) {
      setErrors(e.fieldErrors)
      setNotice({ type: 'error', text: e.message })
    }
  }

  async function handleCancel(schedule) {
    try {
      await api.cancelOwnSchedule(schedule.id)
      setSchedules(await api.listOwnSchedules(id))
      setNotice({
        type: 'info',
        text: 'Fecha cancelada. A los turistas con reserva se les avisó por correo y a los que habían pagado se les devolvió el 100 %.',
      })
    } catch (e) {
      setNotice({ type: 'error', text: e.message })
    }
    setCancelling(null)
  }

  if (!activity) {
    return (
      <div className="page">
        {notice ? <p className="alert error">{notice.text}</p> : <p className="muted">Cargando…</p>}
      </div>
    )
  }

  const today = now.toISOString().slice(0, 10)

  return (
    <div className="page">
      <p className="small"><Link to="/prestador/actividades">← Mis actividades</Link></p>
      <h1>Fechas de {activity.name}</h1>
      <p className="lead">
        Cada fecha tiene su cupo y su precio; por ejemplo, podés cobrar menos entre semana que el fin de semana.
      </p>

      {notice && <p className={`alert ${notice.type}`}>{notice.text}</p>}

      <div className="admin-layout">
        <section className="list">
          {schedules.length === 0 && <p className="muted">Todavía no hay fechas programadas.</p>}
          {schedules.length > 0 && (
            <table>
              <thead>
                <tr>
                  <th>Fecha</th>
                  <th>Hora</th>
                  <th>Lugares</th>
                  <th>Precio</th>
                  <th>Estado</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {schedules.map((schedule) => {
                  const cancelled = schedule.status === 'CANCELLED'
                  const past = new Date(`${schedule.date}T${schedule.startTime}`) < now
                  return (
                    <tr key={schedule.id} className="plain">
                      <td>{formatDay(schedule.date)}</td>
                      <td>{formatTime(schedule.startTime)}</td>
                      <td>{schedule.availableCapacity} libres de {schedule.totalCapacity}</td>
                      <td>{formatMoney(schedule.price)}</td>
                      <td>
                        <span className={`badge ${cancelled ? 'rejected' : past ? 'pending' : 'approved'}`}>
                          {cancelled ? 'Cancelada' : past ? 'Realizada' : 'Programada'}
                        </span>
                      </td>
                      <td>
                        {cancelling !== schedule.id && (
                          <Link to={`/prestador/fechas/${schedule.id}/reservas`} className="button small secondary">
                            Reservas
                          </Link>
                        )}{' '}
                        {!cancelled && !past && cancelling !== schedule.id && (
                          <button className="button small secondary" onClick={() => setCancelling(schedule.id)}>
                            Cancelar
                          </button>
                        )}
                        {cancelling === schedule.id && (
                          <span className="row-actions">
                            <button className="button small danger" onClick={() => handleCancel(schedule)}>
                              Sí, cancelar ({schedule.totalCapacity - schedule.availableCapacity} lugares reservados)
                            </button>
                            <button className="button small secondary" onClick={() => setCancelling(null)}>No</button>
                          </span>
                        )}
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          )}
        </section>

        <aside className="detail">
          <h2>Agregar una fecha</h2>
          <form onSubmit={handleCreate} noValidate>
            <Field label="Fecha" error={errors.date}>
              <input value={form.date} onChange={update('date')} type="date" min={today} />
            </Field>
            <Field label="Hora de inicio" error={errors.startTime}>
              <input value={form.startTime} onChange={update('startTime')} type="time" />
            </Field>
            <Field label="Cupo" error={errors.capacity}>
              <input value={form.capacity} onChange={update('capacity')} type="number" min="1" />
            </Field>
            <Field label="Precio por persona ($)" error={errors.price}>
              <input value={form.price} onChange={update('price')} type="number" min="0" step="any" />
            </Field>
            <button className="button" type="submit">Agregar fecha</button>
          </form>
          <p className="note">
            Para posponer una fecha, cancelala y agregá la nueva: los turistas reciben el aviso y la
            devolución, y pueden reservar la fecha nueva.
          </p>
        </aside>
      </div>
    </div>
  )
}

export default ActivitySchedulesPage

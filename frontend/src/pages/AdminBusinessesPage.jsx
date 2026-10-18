import { useEffect, useState } from 'react'
import { api, formatDate, PERSON_TYPE_LABELS, STATUS_LABELS } from '../api.js'

const TABS = ['PENDING', 'APPROVED', 'REJECTED']
const TAB_LABELS = { PENDING: 'Pendientes', APPROVED: 'Aprobados', REJECTED: 'Rechazados' }

// CU-02: the administrator reviews new businesses and approves or rejects them.
function AdminBusinessesPage() {
  const [status, setStatus] = useState('PENDING')
  const [businesses, setBusinesses] = useState([])
  const [loading, setLoading] = useState(true)
  const [selected, setSelected] = useState(null)
  const [notice, setNotice] = useState(null)
  const [error, setError] = useState('')

  // Changing reloadKey makes the effect fetch the list again (after approving or rejecting).
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    let cancelled = false
    api.listBusinesses(status)
      .then((list) => {
        if (!cancelled) setBusinesses(list)
      })
      .catch((e) => {
        if (!cancelled) setError(e.message)
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [status, reloadKey])

  function reloadList() {
    setLoading(true)
    setError('')
    setReloadKey((key) => key + 1)
  }

  function changeTab(tab) {
    setStatus(tab)
    setSelected(null)
    setNotice(null)
    reloadList()
  }

  async function openDetail(id) {
    setNotice(null)
    try {
      setSelected(await api.getBusiness(id))
    } catch (e) {
      setError(e.message)
    }
  }

  async function review(action) {
    try {
      const updated = action === 'approve'
        ? await api.approveBusiness(selected.id)
        : await api.rejectBusiness(selected.id)
      setNotice({
        type: action === 'approve' ? 'success' : 'info',
        text: action === 'approve'
          ? `Aprobaste "${updated.legalName}". Ya puede iniciar sesión y se le envió un correo a ${updated.email}.`
          : `Rechazaste "${updated.legalName}". Se le envió un correo a ${updated.email}.`,
      })
      setSelected(null)
      reloadList()
    } catch (e) {
      setError(e.message)
    }
  }

  return (
    <div className="page">
      <h1>Emprendimientos</h1>
      <p className="lead">Revisá los datos de cada emprendimiento nuevo antes de aceptarlo o rechazarlo.</p>

      <div className="tabs" role="tablist">
        {TABS.map((tab) => (
          <button key={tab} role="tab" aria-selected={status === tab}
            className={status === tab ? 'active' : ''} onClick={() => changeTab(tab)}>
            {TAB_LABELS[tab]}
          </button>
        ))}
      </div>

      {notice && <p className={`alert ${notice.type}`}>{notice.text}</p>}
      {error && <p className="alert error">{error}</p>}

      <div className="admin-layout">
        <section className="list">
          {loading && <p className="muted">Cargando…</p>}
          {!loading && businesses.length === 0 && (
            <p className="muted">No hay emprendimientos {TAB_LABELS[status].toLowerCase()}.</p>
          )}
          {!loading && businesses.length > 0 && (
            <table>
              <thead>
                <tr>
                  <th>Razón social</th>
                  <th>Tipo</th>
                  <th>Departamento</th>
                  <th>Registrado</th>
                </tr>
              </thead>
              <tbody>
                {businesses.map((b) => (
                  <tr key={b.id} onClick={() => openDetail(b.id)}
                    className={selected?.id === b.id ? 'selected' : ''}>
                    <td><strong>{b.legalName}</strong></td>
                    <td>{PERSON_TYPE_LABELS[b.personType]}</td>
                    <td>{b.department}</td>
                    <td>{formatDate(b.createdAt)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </section>

        <aside className="detail">
          {selected
            ? <BusinessDetail key={selected.id} business={selected} onReview={review} />
            : <p className="muted">Elegí un emprendimiento de la lista para ver su detalle.</p>}
        </aside>
      </div>
    </div>
  )
}

function BusinessDetail({ business, onReview }) {
  const [confirming, setConfirming] = useState(null)
  const isCompany = business.personType === 'COMPANY'

  const rows = [
    ['Tipo', PERSON_TYPE_LABELS[business.personType]],
    [isCompany ? 'RUT' : 'CI', business.document],
    isCompany && ['Representante', business.representativeName],
    ['Teléfono', business.phone],
    ['Correo', business.email],
    ['Departamento', business.department],
    ['Localidad', business.locality],
    ['Dirección', business.address || '—'],
    ['Registrado', formatDate(business.createdAt)],
    business.reviewedAt && ['Revisado', formatDate(business.reviewedAt)],
  ].filter(Boolean)

  return (
    <>
      <div className="detail-header">
        <h2>{business.legalName}</h2>
        <span className={`badge ${business.status.toLowerCase()}`}>{STATUS_LABELS[business.status]}</span>
      </div>

      <dl>
        {rows.map(([label, value]) => (
          <div key={label}>
            <dt>{label}</dt>
            <dd>{value}</dd>
          </div>
        ))}
      </dl>

      <h3>Descripción de los servicios</h3>
      <p className="description">{business.description}</p>

      {business.status === 'PENDING' && (
        <div className="actions">
          {confirming === null && (
            <>
              <button className="button" onClick={() => setConfirming('approve')}>Aceptar</button>
              <button className="button danger" onClick={() => setConfirming('reject')}>Rechazar</button>
            </>
          )}
          {confirming !== null && (
            <div className="confirm">
              <p>
                {confirming === 'approve'
                  ? '¿Aceptar este emprendimiento? El prestador va a poder iniciar sesión.'
                  : '¿Rechazar este emprendimiento? El prestador no va a poder iniciar sesión.'}
              </p>
              <button className={`button${confirming === 'reject' ? ' danger' : ''}`}
                onClick={() => onReview(confirming)}>
                Sí, {confirming === 'approve' ? 'aceptar' : 'rechazar'}
              </button>
              <button className="button secondary" onClick={() => setConfirming(null)}>Cancelar</button>
            </div>
          )}
        </div>
      )}
    </>
  )
}

export default AdminBusinessesPage

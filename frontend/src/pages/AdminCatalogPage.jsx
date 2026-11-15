import { useEffect, useState } from 'react'
import { api } from '../api.js'

// CU-08 and CU-09: the administrator keeps the lists of tags and equipment types.
// Both lists work the same, so one component (CatalogList) is used twice.
function AdminCatalogPage() {
  return (
    <div className="page">
      <h1>Etiquetas y equipamiento</h1>
      <p className="lead">
        Estas listas son las mismas para toda la plataforma: los prestadores eligen de acá al
        cargar sus actividades.
      </p>

      <div className="catalog-layout">
        <CatalogList
          title="Etiquetas"
          description="Tipos de actividad, por ejemplo trekking o cabalgata."
          placeholder="Nueva etiqueta"
          actions={{ list: api.listTags, create: api.createTag, rename: api.renameTag, remove: api.deleteTag }}
        />
        <CatalogList
          title="Equipamiento"
          description="Por ejemplo casco, chaleco salvavidas o bicicleta."
          placeholder="Nuevo equipamiento"
          actions={{
            list: api.listEquipment,
            create: api.createEquipment,
            rename: api.renameEquipment,
            remove: api.deleteEquipment,
          }}
        />
      </div>
    </div>
  )
}

function CatalogList({ title, description, placeholder, actions }) {
  const [items, setItems] = useState(null)
  const [newName, setNewName] = useState('')
  const [editing, setEditing] = useState(null) // { id, name } of the row being renamed
  const [deleting, setDeleting] = useState(null) // id of the row waiting for "are you sure?"
  const [error, setError] = useState('')

  useEffect(() => {
    actions.list().then(setItems).catch((e) => setError(e.message))
    // The actions never change: load the list once.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  // Runs a change and, if it worked, loads the list again so it stays in order.
  async function run(change) {
    setError('')
    try {
      await change()
      setItems(await actions.list())
      return true
    } catch (e) {
      setError(e.fieldErrors?.name ?? e.message)
      return false
    }
  }

  async function handleCreate(event) {
    event.preventDefault()
    if (await run(() => actions.create(newName))) setNewName('')
  }

  async function handleRename(event) {
    event.preventDefault()
    if (await run(() => actions.rename(editing.id, editing.name))) setEditing(null)
  }

  async function handleDelete(id) {
    await run(() => actions.remove(id))
    setDeleting(null)
  }

  return (
    <section className="list catalog">
      <h2>{title}</h2>
      <p className="muted small">{description}</p>

      <form className="inline-form" onSubmit={handleCreate}>
        <input value={newName} onChange={(e) => setNewName(e.target.value)} placeholder={placeholder}
          aria-label={placeholder} maxLength={60} />
        <button className="button small" type="submit">Agregar</button>
      </form>

      {error && <p className="alert error">{error}</p>}
      {items === null && !error && <p className="muted">Cargando…</p>}
      {items?.length === 0 && <p className="muted">Todavía no hay ninguno.</p>}

      <ul className="catalog-items">
        {items?.map((item) => (
          <li key={item.id}>
            {editing?.id === item.id ? (
              <form className="inline-form" onSubmit={handleRename}>
                <input value={editing.name} onChange={(e) => setEditing({ ...editing, name: e.target.value })}
                  aria-label="Nombre" maxLength={60} autoFocus />
                <button className="button small" type="submit">Guardar</button>
                <button className="button small secondary" type="button" onClick={() => setEditing(null)}>
                  Cancelar
                </button>
              </form>
            ) : deleting === item.id ? (
              <>
                <span>¿Eliminar <strong>{item.name}</strong>?</span>
                <span className="row-actions">
                  <button className="button small danger" onClick={() => handleDelete(item.id)}>Sí, eliminar</button>
                  <button className="button small secondary" onClick={() => setDeleting(null)}>Cancelar</button>
                </span>
              </>
            ) : (
              <>
                <span>{item.name}</span>
                <span className="row-actions">
                  <button className="button small secondary"
                    onClick={() => { setEditing({ id: item.id, name: item.name }); setDeleting(null); setError('') }}>
                    Modificar
                  </button>
                  <button className="button small secondary"
                    onClick={() => { setDeleting(item.id); setEditing(null); setError('') }}>
                    Eliminar
                  </button>
                </span>
              </>
            )}
          </li>
        ))}
      </ul>
    </section>
  )
}

export default AdminCatalogPage

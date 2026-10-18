import { useEffect, useState } from 'react'

function App() {
  const [health, setHealth] = useState(null)
  const [error, setError] = useState(false)

  useEffect(() => {
    fetch('/api/health')
      .then((response) => {
        if (!response.ok) throw new Error(`HTTP ${response.status}`)
        return response.json()
      })
      .then(setHealth)
      .catch(() => setError(true))
  }, [])

  return (
    <main className="page">
      <h1>Uruguay Mnatural</h1>
      <p className="subtitle">Turismo en todo el país, en un solo lugar.</p>

      <section className="status">
        <h2>Estado del sistema</h2>
        {error && <p className="down">No se pudo conectar con el backend.</p>}
        {!error && !health && <p>Consultando…</p>}
        {health && (
          <ul>
            <li>
              Backend: <strong className="up">conectado</strong>
            </li>
            <li>
              Base de datos:{' '}
              {health.database === 'UP'
                ? <strong className="up">conectada</strong>
                : <strong className="down">sin conexión</strong>}
            </li>
          </ul>
        )}
      </section>
    </main>
  )
}

export default App

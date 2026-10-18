import { NavLink, Route, Routes } from 'react-router-dom'
import HomePage from './pages/HomePage.jsx'
import RegisterBusinessPage from './pages/RegisterBusinessPage.jsx'
import AdminBusinessesPage from './pages/AdminBusinessesPage.jsx'

function App() {
  return (
    <>
      <header className="topbar">
        <div className="topbar-inner">
          <NavLink to="/" className="brand">
            Uruguay <span>Mnatural</span>
          </NavLink>
          <nav className="nav">
            <NavLink to="/" end>Inicio</NavLink>
            <NavLink to="/registro-prestador">Registrar emprendimiento</NavLink>
            <NavLink to="/admin/emprendimientos">Administración</NavLink>
          </nav>
        </div>
      </header>

      <main>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/registro-prestador" element={<RegisterBusinessPage />} />
          <Route path="/admin/emprendimientos" element={<AdminBusinessesPage />} />
          <Route path="*" element={<p className="page">Página no encontrada.</p>} />
        </Routes>
      </main>
    </>
  )
}

export default App

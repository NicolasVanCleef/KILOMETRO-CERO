import { useState } from 'react'
import { NavLink, Route, Routes, useNavigate } from 'react-router-dom'
import { loadRole, ROLES, saveRole } from './roles.js'
import ChooseRolePage from './pages/ChooseRolePage.jsx'
import HomePage from './pages/HomePage.jsx'
import RegisterBusinessPage from './pages/RegisterBusinessPage.jsx'
import AdminBusinessesPage from './pages/AdminBusinessesPage.jsx'

function App() {
  const [role, setRole] = useState(loadRole)
  const navigate = useNavigate()

  function chooseRole(newRole) {
    saveRole(newRole)
    setRole(newRole)
    navigate('/')
  }

  if (!role) {
    return <ChooseRolePage onChoose={chooseRole} />
  }

  return (
    <>
      <header className="topbar">
        <div className="topbar-inner">
          <NavLink to="/" className="brand">
            Uruguay <span>Mnatural</span>
          </NavLink>
          <nav className="nav">
            <NavLink to="/" end>Inicio</NavLink>
            {ROLES[role].links.map((link) => (
              <NavLink key={link.to} to={link.to}>{link.label}</NavLink>
            ))}
          </nav>
          <div className="current-role">
            <span>{ROLES[role].label}</span>
            <button onClick={() => chooseRole(null)}>Cambiar usuario</button>
          </div>
        </div>
      </header>

      <main>
        <Routes>
          <Route path="/" element={<HomePage role={role} />} />
          <Route path="/registro-prestador"
            element={<OnlyFor role={role} allowed="PROVIDER"><RegisterBusinessPage /></OnlyFor>} />
          <Route path="/admin/emprendimientos"
            element={<OnlyFor role={role} allowed="ADMIN"><AdminBusinessesPage /></OnlyFor>} />
          <Route path="*" element={<p className="page">Página no encontrada.</p>} />
        </Routes>
      </main>
    </>
  )
}

// Shows the page only to the role it belongs to (RNF-16: each user sees only their functions).
function OnlyFor({ role, allowed, children }) {
  if (role === allowed) return children
  return (
    <div className="page narrow">
      <div className="alert info">
        Esta sección es para el perfil <strong>{ROLES[allowed].label}</strong>. Cambiá de usuario
        para verla.
      </div>
    </div>
  )
}

export default App

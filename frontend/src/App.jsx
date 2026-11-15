import { useEffect, useState } from 'react'
import { Link, NavLink, Route, Routes, useNavigate } from 'react-router-dom'
import { setSessionExpiredHandler } from './api.js'
import { ROLES } from './roles.js'
import { loadSession, saveSession } from './session.js'
import HomePage from './pages/HomePage.jsx'
import LoginPage from './pages/LoginPage.jsx'
import RegisterTouristPage from './pages/RegisterTouristPage.jsx'
import ForgotPasswordPage from './pages/ForgotPasswordPage.jsx'
import ResetPasswordPage from './pages/ResetPasswordPage.jsx'
import TouristProfilePage from './pages/TouristProfilePage.jsx'
import BusinessProfilePage from './pages/BusinessProfilePage.jsx'
import RegisterBusinessPage from './pages/RegisterBusinessPage.jsx'
import AdminBusinessesPage from './pages/AdminBusinessesPage.jsx'
import PaymentPage from './pages/PaymentPage.jsx'

function App() {
  const [session, setSession] = useState(loadSession)
  const navigate = useNavigate()

  // Someone who has not logged in is a visitor.
  const role = session?.user.role ?? 'VISITOR'

  function login(newSession) {
    saveSession(newSession)
    setSession(newSession)
    navigate('/')
  }

  // CU-07: logging out forgets the token and goes back to the public page.
  function logout() {
    saveSession(null)
    setSession(null)
    navigate('/')
  }

  // The name in the header changes when the user edits their profile.
  function updateDisplayName(displayName) {
    const updated = { ...session, user: { ...session.user, displayName } }
    saveSession(updated)
    setSession(updated)
  }

  // If the backend answers that the token is no longer valid, close the session here too.
  useEffect(() => {
    setSessionExpiredHandler(() => {
      saveSession(null)
      setSession(null)
      navigate('/ingresar')
    })
  }, [navigate])

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
          {session ? (
            <div className="current-role">
              <span title={session.user.email}>{session.user.displayName} · {ROLES[role].label}</span>
              <button onClick={logout}>Cerrar sesión</button>
            </div>
          ) : (
            <nav className="nav">
              <NavLink to="/ingresar">Iniciar sesión</NavLink>
              <NavLink to="/registro">Registrarse</NavLink>
            </nav>
          )}
        </div>
      </header>

      <main>
        <Routes>
          <Route path="/" element={<HomePage role={role} />} />
          <Route path="/ingresar" element={<LoginPage onLogin={login} />} />
          <Route path="/registro" element={<RegisterTouristPage />} />
          <Route path="/registro-prestador" element={<RegisterBusinessPage />} />
          <Route path="/recuperar" element={<ForgotPasswordPage />} />
          <Route path="/restablecer" element={<ResetPasswordPage />} />
          <Route path="/perfil"
            element={<OnlyFor role={role} allowed="TOURIST"><TouristProfilePage onNameChange={updateDisplayName} /></OnlyFor>} />
          <Route path="/pagos"
            element={<OnlyFor role={role} allowed="TOURIST"><PaymentPage /></OnlyFor>} />
          <Route path="/prestador/emprendimiento"
            element={<OnlyFor role={role} allowed="PROVIDER"><BusinessProfilePage /></OnlyFor>} />
          <Route path="/admin/emprendimientos"
            element={<OnlyFor role={role} allowed="ADMIN"><AdminBusinessesPage /></OnlyFor>} />
          <Route path="*" element={<p className="page">Página no encontrada.</p>} />
        </Routes>
      </main>
    </>
  )
}

// Shows the page only to the role it belongs to (RNF-16: each user sees only their functions).
// The backend checks the role again on every request; this is only so the screen makes sense.
function OnlyFor({ role, allowed, children }) {
  if (role === allowed) return children
  return (
    <div className="page narrow">
      <div className="alert info">
        Esta sección es para usuarios con perfil <strong>{ROLES[allowed].label}</strong>.{' '}
        {role === 'VISITOR' && <Link to="/ingresar">Iniciá sesión</Link>}
      </div>
    </div>
  )
}

export default App

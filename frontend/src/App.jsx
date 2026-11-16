import { useEffect, useState } from 'react'
import { Link, NavLink, Route, Routes, useNavigate } from 'react-router-dom'
import { setSessionExpiredHandler } from './api.js'
import { ROLES } from './roles.js'
import { loadSession, saveSession } from './session.js'
import HomePage from './pages/HomePage.jsx'
import ExplorePage from './pages/ExplorePage.jsx'
import ActivityDetailPage from './pages/ActivityDetailPage.jsx'
import LoginPage from './pages/LoginPage.jsx'
import RegisterTouristPage from './pages/RegisterTouristPage.jsx'
import ForgotPasswordPage from './pages/ForgotPasswordPage.jsx'
import ResetPasswordPage from './pages/ResetPasswordPage.jsx'
import TouristProfilePage from './pages/TouristProfilePage.jsx'
import BusinessProfilePage from './pages/BusinessProfilePage.jsx'
import RegisterBusinessPage from './pages/RegisterBusinessPage.jsx'
import AdminBusinessesPage from './pages/AdminBusinessesPage.jsx'
import AdminActivitiesPage from './pages/AdminActivitiesPage.jsx'
import AdminCatalogPage from './pages/AdminCatalogPage.jsx'
import ProviderActivitiesPage from './pages/ProviderActivitiesPage.jsx'
import ActivityFormPage from './pages/ActivityFormPage.jsx'
import ActivitySchedulesPage from './pages/ActivitySchedulesPage.jsx'
import BookingPage from './pages/BookingPage.jsx'
import MyBookingsPage from './pages/MyBookingsPage.jsx'
import ProviderBookingsPage from './pages/ProviderBookingsPage.jsx'
import ProviderReviewsPage from './pages/ProviderReviewsPage.jsx'
import PayBookingPage from './pages/PayBookingPage.jsx'

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
          <Link to="/" className="brand">
            <img src="/logos/icon.png" alt="" />
            <span className="brand-name">
              <strong>Uruguay Mnatural</strong>
              <small>por Kilómetro Cero</small>
            </span>
          </Link>
          <nav className="nav main" aria-label="Principal">
            <NavLink to="/actividades" end>Actividades</NavLink>
            {ROLES[role].links.map((link) => (
              <NavLink key={link.to} to={link.to}>{link.label}</NavLink>
            ))}
          </nav>
          {session ? (
            <div className="account">
              <span className="user-chip" title={session.user.email}>
                <span className="avatar" aria-hidden="true">{session.user.displayName.charAt(0).toUpperCase()}</span>
                <span>
                  <strong>{session.user.displayName}</strong>
                  {/* The administrator's name is already "Administrador": do not repeat it. */}
                  {session.user.displayName !== ROLES[role].label && <small>{ROLES[role].label}</small>}
                </span>
              </span>
              <button className="button secondary small" onClick={logout}>Cerrar sesión</button>
            </div>
          ) : (
            <div className="account">
              <Link to="/ingresar" className="button secondary">Iniciar sesión</Link>
              <Link to="/registro" className="button">Crear cuenta</Link>
            </div>
          )}
        </div>
      </header>

      <main>
        <Routes>
          <Route path="/" element={<HomePage role={role} />} />
          <Route path="/actividades" element={<ExplorePage />} />
          <Route path="/actividades/:id" element={<ActivityDetailPage role={role} />} />
          <Route path="/ingresar" element={<LoginPage onLogin={login} />} />
          <Route path="/registro" element={<RegisterTouristPage />} />
          <Route path="/registro-prestador" element={<RegisterBusinessPage />} />
          <Route path="/recuperar" element={<ForgotPasswordPage />} />
          <Route path="/restablecer" element={<ResetPasswordPage />} />
          <Route path="/perfil"
            element={<OnlyFor role={role} allowed="TOURIST"><TouristProfilePage onNameChange={updateDisplayName} /></OnlyFor>} />
          <Route path="/mis-reservas/:id/pagar"
            element={<OnlyFor role={role} allowed="TOURIST"><PayBookingPage /></OnlyFor>} />
          <Route path="/prestador/emprendimiento"
            element={<OnlyFor role={role} allowed="PROVIDER"><BusinessProfilePage /></OnlyFor>} />
          <Route path="/admin/emprendimientos"
            element={<OnlyFor role={role} allowed="ADMIN"><AdminBusinessesPage /></OnlyFor>} />
          <Route path="/admin/actividades"
            element={<OnlyFor role={role} allowed="ADMIN"><AdminActivitiesPage /></OnlyFor>} />
          <Route path="/admin/catalogo"
            element={<OnlyFor role={role} allowed="ADMIN"><AdminCatalogPage /></OnlyFor>} />
          <Route path="/prestador/actividades"
            element={<OnlyFor role={role} allowed="PROVIDER"><ProviderActivitiesPage /></OnlyFor>} />
          <Route path="/prestador/actividades/nueva"
            element={<OnlyFor role={role} allowed="PROVIDER"><ActivityFormPage /></OnlyFor>} />
          <Route path="/prestador/actividades/:id"
            element={<OnlyFor role={role} allowed="PROVIDER"><ActivityFormPage /></OnlyFor>} />
          <Route path="/prestador/actividades/:id/fechas"
            element={<OnlyFor role={role} allowed="PROVIDER"><ActivitySchedulesPage /></OnlyFor>} />
          <Route path="/reservar/:activityId/:scheduleId"
            element={<OnlyFor role={role} allowed="TOURIST"><BookingPage /></OnlyFor>} />
          <Route path="/mis-reservas"
            element={<OnlyFor role={role} allowed="TOURIST"><MyBookingsPage /></OnlyFor>} />
          <Route path="/prestador/fechas/:id/reservas"
            element={<OnlyFor role={role} allowed="PROVIDER"><ProviderBookingsPage /></OnlyFor>} />
          <Route path="/prestador/actividades/:id/valoraciones"
            element={<OnlyFor role={role} allowed="PROVIDER"><ProviderReviewsPage /></OnlyFor>} />
          <Route path="*" element={<p className="page">Página no encontrada.</p>} />
        </Routes>
      </main>

      <footer className="footer">
        <div className="footer-inner">
          <img src="/logos/full-color.png" alt="Kilómetro Cero · Punto de partida" />
          <span>Uruguay Mnatural · Proyecto TIC 2026</span>
        </div>
      </footer>
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

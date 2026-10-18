import { ROLES } from '../roles.js'

// First screen: choose which kind of user to enter as.
function ChooseRolePage({ onChoose }) {
  return (
    <div className="choose-role">
      <div className="choose-role-inner">
        <p className="brand big">
          Uruguay <span>Mnatural</span>
        </p>
        <h1>¿Cómo querés entrar?</h1>
        <p className="lead">Elegí un tipo de usuario para ver la plataforma desde su lugar.</p>

        <div className="role-grid">
          {Object.entries(ROLES).map(([id, role]) => (
            <button key={id} className="role-card" onClick={() => onChoose(id)}>
              <strong>{role.label}</strong>
              <span>{role.description}</span>
            </button>
          ))}
        </div>

        <p className="note">
          Por ahora es una simulación para mostrar cada perfil. El inicio de sesión con correo y
          contraseña se agrega más adelante.
        </p>
      </div>
    </div>
  )
}

export default ChooseRolePage

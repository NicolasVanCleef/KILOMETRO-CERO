import { onlyDigits } from '../digits.js'
import Field from './Field.jsx'

const LANGUAGES = ['Español', 'Inglés', 'Portugués', 'Francés', 'Alemán', 'Italiano', 'Otro']

// The tourist's personal data (RF-07). Used by the sign-up form and by "Mi perfil".
function TouristFields({ form, errors, update }) {
  return (
    <div className="grid">
      <Field label="Nombre" error={errors.firstName}>
        <input value={form.firstName} onChange={update('firstName')} autoComplete="given-name" />
      </Field>
      <Field label="Apellido" error={errors.lastName}>
        <input value={form.lastName} onChange={update('lastName')} autoComplete="family-name" />
      </Field>
      <Field label="País" error={errors.country}>
        <input value={form.country} onChange={update('country')} autoComplete="country-name" />
      </Field>
      <Field label="Fecha de nacimiento" error={errors.birthDate}>
        <input value={form.birthDate} onChange={update('birthDate')} type="date" />
      </Field>
      {/* The language is kept as a profile data, as the client asked; the page itself is only in
          Spanish because the client left translations out for now (RNF-18). */}
      <Field label="Idioma de preferencia" error={errors.preferredLanguage}
        hint="Por ahora la página está solo en español">
        <select value={form.preferredLanguage} onChange={update('preferredLanguage')}>
          <option value="">Elegí…</option>
          {LANGUAGES.map((language) => <option key={language}>{language}</option>)}
        </select>
      </Field>
      <Field label="Cédula" error={errors.document} hint="7 u 8 dígitos, solo números">
        <input value={form.document} inputMode="numeric" maxLength={8}
          onChange={(event) => update('document')({ target: { value: onlyDigits(event.target.value, 8) } })} />
      </Field>
    </div>
  )
}

export default TouristFields

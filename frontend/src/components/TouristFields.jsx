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
      <Field label="Idioma de preferencia" error={errors.preferredLanguage}>
        <select value={form.preferredLanguage} onChange={update('preferredLanguage')}>
          <option value="">Elegí…</option>
          {LANGUAGES.map((language) => <option key={language}>{language}</option>)}
        </select>
      </Field>
      <Field label="Documento" error={errors.document} hint="Cédula o pasaporte">
        <input value={form.document} onChange={update('document')} />
      </Field>
    </div>
  )
}

export default TouristFields

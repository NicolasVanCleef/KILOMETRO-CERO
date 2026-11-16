// Line icon for a type of activity (tag). Tags without their own icon get a map pin.

const PATHS = {
  kayak: (
    <>
      <path d="M2 16c2 0 2-1.5 4-1.5s2 1.5 4 1.5 2-1.5 4-1.5 2 1.5 4 1.5 2-1.5 4-1.5" />
      <path d="M2 20c2 0 2-1.5 4-1.5s2 1.5 4 1.5 2-1.5 4-1.5 2 1.5 4 1.5 2-1.5 4-1.5" />
      <path d="M5 11l14-6" />
    </>
  ),
  trekking: <path d="M3 20l6-11 4 6 3-4 5 9z" />,
  enoturismo: (
    <>
      <path d="M8 3h8l-1 7a3 3 0 0 1-6 0z" />
      <path d="M12 13v7" />
      <path d="M8 21h8" />
    </>
  ),
  cabalgata: (
    <>
      <path d="M4 18c3-6 6-9 12-10" />
      <path d="M14 6l3 2-2 3" />
      <path d="M6 18h4" />
    </>
  ),
  'turismo rural': (
    <>
      <path d="M3 21h18" />
      <path d="M5 21V10l7-5 7 5v11" />
      <path d="M10 21v-6h4v6" />
    </>
  ),
  'avistamiento de aves': (
    <>
      <path d="M16 7h.01" />
      <path d="M3.4 18H12a8 8 0 0 0 8-8V7a4 4 0 0 0-7.3-2.3L2 20" />
    </>
  ),
}

const PIN = (
  <>
    <path d="M12 21s-7-6.2-7-11a7 7 0 0 1 14 0c0 4.8-7 11-7 11z" />
    <circle cx="12" cy="10" r="2.5" />
  </>
)

// Tags shown with the warm (orange) color instead of green.
const WARM = new Set(['enoturismo', 'cabalgata'])

// The icon inside its colored square.
function TagIcon({ name, size = 22 }) {
  return (
    <span className={`icon-box${WARM.has(name.toLowerCase()) ? ' warm' : ''}`}>
      <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"
        strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
        {PATHS[name.toLowerCase()] ?? PIN}
      </svg>
    </span>
  )
}

export default TagIcon

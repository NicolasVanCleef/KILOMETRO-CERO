// Illustrated band of an activity. Activities have no photos (the MER has no field for them),
// so each one gets a simple landscape drawn from its first tag: water, mountains, vineyards, etc.

// Which scene each tag uses. Tags the administrator adds later fall back to "default".
const SCENE_BY_TAG = {
  kayak: 'water',
  'avistamiento de aves': 'coast',
  trekking: 'mountains',
  cabalgata: 'hills',
  'turismo rural': 'hills',
  enoturismo: 'vineyard',
}

function sceneFor(tags) {
  const names = (tags ?? []).map((tag) => (typeof tag === 'string' ? tag : tag.name).toLowerCase())
  for (const name of names) {
    if (SCENE_BY_TAG[name]) return SCENE_BY_TAG[name]
  }
  return 'default'
}

const SCENES = {
  water: {
    sky: '#106F5E',
    shapes: (
      <>
        <circle cx="300" cy="40" r="18" fill="#F0A57E" />
        <path d="M0 96 C 60 80, 120 110, 180 94 S 300 78, 360 92 V132 H0z" fill="#0F5A4C" />
        <path d="M0 112 C 80 100, 140 124, 220 110 S 320 102, 360 112 V132 H0z" fill="#0F332D" />
      </>
    ),
  },
  coast: {
    sky: '#3D7F8C',
    shapes: (
      <>
        <circle cx="70" cy="38" r="16" fill="#F6CDB6" />
        <path d="M0 92 C 90 76, 160 98, 240 86 S 330 80, 360 84 V132 H0z" fill="#E9D3A8" />
        <path d="M0 112 C 70 104, 150 122, 240 110 S 330 104, 360 108 V132 H0z" fill="#2F6470" />
        <path d="M250 50 q 8 -8 16 0 q 8 -8 16 0" stroke="#0F332D" strokeWidth="3" fill="none" strokeLinecap="round" />
      </>
    ),
  },
  mountains: {
    sky: '#6E8F5A',
    shapes: (
      <>
        <circle cx="72" cy="34" r="14" fill="#F6CDB6" />
        <path d="M0 132 L 90 52 L 150 100 L 230 30 L 360 120 V132z" fill="#4D6B3F" />
        <path d="M0 132 L 120 88 L 200 116 L 290 80 L 360 110 V132z" fill="#344A2B" />
      </>
    ),
  },
  hills: {
    sky: '#B98B4F',
    shapes: (
      <>
        <circle cx="290" cy="36" r="16" fill="#FBE7DC" />
        <path d="M0 98 C 70 70, 150 70, 220 96 S 330 104, 360 90 V132 H0z" fill="#8C6A3A" />
        <path d="M0 116 C 90 100, 190 124, 280 108 S 340 106, 360 110 V132 H0z" fill="#5E4626" />
      </>
    ),
  },
  vineyard: {
    sky: '#7A3E4A',
    shapes: (
      <>
        <path d="M0 100 C 90 70, 200 70, 360 96 V132 H0z" fill="#5E2C37" />
        <path
          d="M20 132 V108 M60 132 V102 M100 132 V98 M140 132 V96 M180 132 V96 M220 132 V97 M260 132 V100 M300 132 V104 M340 132 V108"
          stroke="#8FA36A"
          strokeWidth="6"
          strokeLinecap="round"
        />
      </>
    ),
  },
  default: {
    sky: '#0F332D',
    shapes: (
      <>
        <circle cx="180" cy="66" r="14" fill="#E07038" />
        <path d="M180 66 C 220 40, 260 30, 320 24 M180 66 C 140 40, 100 30, 40 24 M180 66 C 230 90, 280 100, 340 104 M180 66 C 130 90, 80 100, 20 104" stroke="#106F5E" strokeWidth="4" fill="none" strokeLinecap="round" />
      </>
    ),
  },
}

function ActivityArt({ tags, children, className = '' }) {
  const scene = SCENES[sceneFor(tags)]
  return (
    <div className={`activity-art ${className}`} style={{ background: scene.sky }}>
      <svg width="100%" height="100%" viewBox="0 0 360 132" preserveAspectRatio="xMidYMax slice" aria-hidden="true">
        {scene.shapes}
      </svg>
      {children}
    </div>
  )
}

export default ActivityArt

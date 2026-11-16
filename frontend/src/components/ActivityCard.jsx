import { Link } from 'react-router-dom'
import { formatDuration, formatMoney } from '../api.js'
import ActivityArt from './ActivityArt.jsx'

// "2026-11-17" -> "Mar 17 nov"
function shortDay(isoDay) {
  const text = new Date(`${isoDay}T12:00:00`).toLocaleDateString('es-UY', {
    weekday: 'short', day: 'numeric', month: 'short',
  })
  return text.charAt(0).toUpperCase() + text.slice(1).replace(/\./g, '').replace(',', '')
}

// One activity of a list (portal search and home page), as a card that opens its detail.
function ActivityCard({ activity }) {
  const free = Number(activity.priceFrom) === 0
  return (
    <Link to={`/actividades/${activity.id}`} className="card activity-card">
      <ActivityArt tags={activity.tags}>
        <span className="date-pill">
          {activity.nextDate ? shortDay(activity.nextDate) : 'Sin fechas próximas'}
        </span>
      </ActivityArt>
      <div className="activity-body">
        <div className="tag-row">
          {activity.tags.map((tag) => <span key={tag} className="tag">{tag}</span>)}
        </div>
        <h3>{activity.name}</h3>
        <p>
          {activity.locality}, {activity.department} · {formatDuration(activity.durationMinutes)}
        </p>
        <p>{activity.businessName}</p>
        <div className="activity-foot">
          {free
            ? <span className="free">Sin costo</span>
            : <span>Desde <span className="price">{formatMoney(activity.priceFrom)}</span></span>}
          {!free && <span>por persona</span>}
        </div>
      </div>
    </Link>
  )
}

export default ActivityCard

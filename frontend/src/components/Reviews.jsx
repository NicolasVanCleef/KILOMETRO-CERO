import { useEffect, useState } from 'react'
import { api, formatDate } from '../api.js'
import Field from './Field.jsx'

// "★★★★☆" for a rating of 4.
export function Stars({ rating }) {
  const full = Math.round(rating)
  return <span className="stars" aria-label={`${rating} de 5`}>{'★'.repeat(full)}{'☆'.repeat(5 - full)}</span>
}

// One review with the provider's reply under it (RF-05).
export function ReviewItem({ review, children }) {
  return (
    <article className="review">
      <p className="review-head">
        <Stars rating={review.rating} /> <strong>{review.touristName}</strong>
        <span className="muted small"> · {formatDate(review.createdAt)}</span>
      </p>
      <p className="description">{review.comment}</p>
      {review.providerReply && (
        <p className="reply"><strong>Respuesta del prestador:</strong> {review.providerReply}</p>
      )}
      {children}
    </article>
  )
}

// CU-20: the reviews of an activity. A tourist who took part can write one, and edit or
// delete their own. onChange asks the page to load the activity again.
function Reviews({ activityId, reviews, averageRating, role, onChange }) {
  const [mine, setMine] = useState(null) // { participated, review } for a logged-in tourist
  const [editing, setEditing] = useState(false)
  const [form, setForm] = useState({ rating: '5', comment: '' })
  const [errors, setErrors] = useState({})
  const [message, setMessage] = useState('')

  useEffect(() => {
    if (role !== 'TOURIST') return
    api.getOwnReview(activityId).then(setMine).catch(() => {})
  }, [activityId, role, reviews])

  function startEditing() {
    setForm({ rating: String(mine.review.rating), comment: mine.review.comment })
    setEditing(true)
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setMessage('')
    try {
      const data = { rating: Number(form.rating), comment: form.comment }
      if (mine.review) await api.updateReview(mine.review.id, data)
      else await api.createReview(activityId, data)
      setEditing(false)
      setErrors({})
      setForm({ rating: '5', comment: '' })
      onChange()
    } catch (e) {
      setErrors(e.fieldErrors)
      setMessage(e.message)
    }
  }

  async function handleDelete() {
    try {
      await api.deleteReview(mine.review.id)
      onChange()
    } catch (e) {
      setMessage(e.message)
    }
  }

  const showForm = mine?.participated && (!mine.review || editing)

  return (
    <>
      <h3>
        Valoraciones{' '}
        {averageRating !== null && (
          <span className="small muted">
            <Stars rating={averageRating} /> {averageRating} de 5 · {reviews.length}{' '}
            {reviews.length === 1 ? 'valoración' : 'valoraciones'}
          </span>
        )}
      </h3>

      {reviews.length === 0 && <p className="muted">Todavía nadie valoró esta actividad.</p>}

      {reviews.map((review) => (
        <ReviewItem key={review.id} review={review}>
          {mine?.review?.id === review.id && !editing && (
            <div className="actions">
              <button className="button small secondary" onClick={startEditing}>Editar mi valoración</button>
              <button className="button small secondary" onClick={handleDelete}>Borrar</button>
            </div>
          )}
        </ReviewItem>
      ))}

      {showForm && (
        <form className="form review-form" onSubmit={handleSubmit} noValidate>
          <fieldset>
            <legend>{mine.review ? 'Editar mi valoración' : 'Valorar esta actividad'}</legend>
            <Field label="Puntaje" error={errors.rating}>
              <select value={form.rating} onChange={(e) => setForm({ ...form, rating: e.target.value })}>
                {[5, 4, 3, 2, 1].map((n) => <option key={n} value={n}>{'★'.repeat(n)} ({n})</option>)}
              </select>
            </Field>
            <Field label="Comentario" error={errors.comment}>
              <textarea rows="3" value={form.comment} onChange={(e) => setForm({ ...form, comment: e.target.value })} />
            </Field>
            {message && <p className="alert error">{message}</p>}
            <button className="button small" type="submit">Publicar</button>{' '}
            {editing && (
              <button className="button small secondary" type="button" onClick={() => setEditing(false)}>Cancelar</button>
            )}
          </fieldset>
        </form>
      )}
      {!showForm && message && <p className="alert error">{message}</p>}

      {role === 'TOURIST' && mine && !mine.participated && (
        <p className="muted small">Vas a poder valorar esta actividad después de participar de ella.</p>
      )}
    </>
  )
}

export default Reviews

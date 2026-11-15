import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { api } from '../api.js'
import { ReviewItem } from '../components/Reviews.jsx'

// CU-21: the provider reads the reviews of one of their activities and answers them.
function ProviderReviewsPage() {
  const { id } = useParams()
  const [activity, setActivity] = useState(null)
  const [reviews, setReviews] = useState([])
  const [replying, setReplying] = useState(null) // { id, text } of the review being answered
  const [error, setError] = useState('')

  useEffect(() => {
    Promise.all([api.getOwnActivity(id), api.listOwnActivityReviews(id)])
      .then(([own, list]) => {
        setActivity(own)
        setReviews(list)
      })
      .catch((e) => setError(e.message))
  }, [id])

  async function handleReply(event) {
    event.preventDefault()
    setError('')
    try {
      await api.replyReview(replying.id, replying.text)
      setReviews(await api.listOwnActivityReviews(id))
      setReplying(null)
    } catch (e) {
      setError(e.fieldErrors?.reply ?? e.message)
    }
  }

  if (!activity) {
    return (
      <div className="page narrow">
        {error ? <p className="alert error">{error}</p> : <p className="muted">Cargando…</p>}
      </div>
    )
  }

  return (
    <div className="page narrow">
      <p className="small"><Link to="/prestador/actividades">← Mis actividades</Link></p>
      <h1>Valoraciones de {activity.name}</h1>
      <p className="lead">Tu respuesta se muestra debajo del comentario en el portal.</p>

      {error && <p className="alert error">{error}</p>}
      {reviews.length === 0 && <p className="muted">Todavía nadie valoró esta actividad.</p>}

      {reviews.map((review) => (
        <ReviewItem key={review.id} review={review}>
          {replying?.id === review.id ? (
            <form onSubmit={handleReply}>
              <textarea className="reply-box" rows="3" value={replying.text} aria-label="Respuesta"
                onChange={(e) => setReplying({ ...replying, text: e.target.value })} />
              <div className="actions">
                <button className="button small" type="submit">Guardar respuesta</button>
                <button className="button small secondary" type="button" onClick={() => setReplying(null)}>Cancelar</button>
              </div>
            </form>
          ) : (
            <div className="actions">
              <button className="button small secondary"
                onClick={() => setReplying({ id: review.id, text: review.providerReply ?? '' })}>
                {review.providerReply ? 'Cambiar respuesta' : 'Responder'}
              </button>
            </div>
          )}
        </ReviewItem>
      ))}
    </div>
  )
}

export default ProviderReviewsPage

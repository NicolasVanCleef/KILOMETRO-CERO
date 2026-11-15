import { useEffect, useState } from 'react'

// Shows the minutes and seconds left until a moment ("7:42") and calls onEnd when it arrives.
function Countdown({ until, onEnd }) {
  const [secondsLeft, setSecondsLeft] = useState(() => secondsUntil(until))

  useEffect(() => {
    const timer = setInterval(() => {
      const left = secondsUntil(until)
      setSecondsLeft(left)
      if (left <= 0) {
        clearInterval(timer)
        onEnd?.()
      }
    }, 1000)
    return () => clearInterval(timer)
    // onEnd is a new function on every render; the timer only depends on the moment.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [until])

  const minutes = Math.floor(secondsLeft / 60)
  const seconds = String(secondsLeft % 60).padStart(2, '0')
  return <strong>{minutes}:{seconds}</strong>
}

function secondsUntil(isoMoment) {
  return Math.max(0, Math.round((new Date(isoMoment) - Date.now()) / 1000))
}

export default Countdown

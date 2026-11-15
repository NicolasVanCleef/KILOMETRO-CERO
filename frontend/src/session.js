// The logged-in user and their token, kept in the browser so a page reload does not log out.
// session = { token, user: { id, email, role, displayName } }

const STORAGE_KEY = 'uruguay-mnatural-session'

export function loadSession() {
  try {
    const session = JSON.parse(localStorage.getItem(STORAGE_KEY))
    return session?.token && session?.user ? session : null
  } catch {
    return null
  }
}

export function saveSession(session) {
  try {
    if (session) localStorage.setItem(STORAGE_KEY, JSON.stringify(session))
    else localStorage.removeItem(STORAGE_KEY)
  } catch {
    // Without storage the session only lasts until the page is reloaded.
  }
}

// Keeps only the digits of what was typed, up to a maximum length.
// Used by fields that only accept numbers (cédula, RUT, phone): letters, dots and dashes
// are dropped as you type.
export function onlyDigits(value, maxLength) {
  return value.replace(/\D/g, '').slice(0, maxLength)
}

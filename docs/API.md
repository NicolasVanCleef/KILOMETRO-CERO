# API REST — Uruguay Mnatural

Documentación de los endpoints del backend (RNF-07): ruta, método, qué recibe y qué devuelve.
La dirección base en desarrollo es `http://localhost:8080`. Todo se envía y se recibe en JSON.

## Cómo se lee

- **Quién:** *Público* no necesita iniciar sesión. *Turista*, *Prestador* y *Administrador*
  necesitan el token que devuelve el inicio de sesión, en el encabezado
  `Authorization: Bearer <token>`. La primera parte de la ruta dice para quién es:
  `/api/tourist/...`, `/api/provider/...`, `/api/admin/...` (RNF-16).
- **Fechas:** `2026-12-05`. **Horas:** `10:00:00`. **Fecha y hora:** `2026-12-05T10:00:00`.

### Códigos de respuesta

| Código | Cuándo |
|--------|--------|
| 200 | La consulta o el cambio salió bien |
| 201 | Se creó algo (un registro, una reserva, un pago…) |
| 204 | Salió bien y no hay nada que devolver (por ejemplo, al eliminar) |
| 400 | Los datos enviados son incorrectos o falta un parámetro |
| 401 | Hay que iniciar sesión, o el correo o la contraseña no son correctos |
| 403 | El usuario no tiene permiso (otro rol, o un dato que es de otro usuario) |
| 404 | Lo que se pidió no existe |
| 409 | No se puede por una regla de negocio (sin lugares, fuera de plazo, nombre repetido…) |

### Forma de los errores

```json
{
  "message": "Hay datos incorrectos.",
  "fieldErrors": { "email": "El correo no es válido." }
}
```

`fieldErrors` trae un mensaje por cada campo incorrecto del formulario; si no aplica, viene vacío.

---

## 1. API pública para otras plataformas (CU-25)

Es la API que pide el enunciado para que sistemas externos consulten información. No necesita
iniciar sesión y solo consulta. Solo muestra emprendimientos aprobados y sus actividades
publicadas (RN-38).

### Listar emprendimientos (RF-48)

`GET /api/public/businesses`

```json
[
  {
    "id": 2,
    "legalName": "Kayak Rocha",
    "description": "Paseos en kayak",
    "department": "Rocha",
    "locality": "La Paloma",
    "phone": "099123456",
    "email": "contacto@kayakrocha.uy"
  }
]
```

### Actividades de un día en un departamento / localidad (RF-49)

`GET /api/public/activities?date=2026-12-05&department=Rocha&locality=La Paloma`

| Parámetro | Obligatorio | Descripción |
|-----------|-------------|-------------|
| `date` | Sí | Día a consultar |
| `department` | Sí | Departamento (sin distinguir mayúsculas) |
| `locality` | No | Texto contenido en la localidad |

Devuelve una fila por cada horario de ese día:

```json
[
  {
    "activityId": 1,
    "name": "Kayak en la laguna",
    "businessName": "Kayak Rocha",
    "department": "Rocha",
    "locality": "La Paloma",
    "date": "2026-12-05",
    "startTime": "10:00:00",
    "price": 1500.00,
    "availableCapacity": 8
  }
]
```

Sin `date` o sin `department`, o con una fecha mal escrita: **400**. Sin resultados: lista vacía.

### Las 3 actividades mejor puntuadas del mes por departamento (RF-50)

`GET /api/public/activities/top-rated?year=2026&month=11`

Sin `year` y `month` responde por el mes actual. El puntaje de una actividad es el promedio de
las valoraciones escritas ese mes (RN-37). Solo aparecen los departamentos con valoraciones.

```json
[
  {
    "department": "Rocha",
    "activities": [
      { "activityId": 1, "name": "Kayak en la laguna", "businessName": "Kayak Rocha", "averageRating": 4.5, "reviewCount": 2 }
    ]
  }
]
```

### Verificar un emprendimiento (RF-51)

`GET /api/public/businesses/{id}/verification` — por identificador.
`GET /api/public/businesses/verification?document=217208670014` — por CI o RUT.

Dice si el emprendimiento está aprobado por Uruguay Mnatural (RN-36). Siempre responde **200**:
si el emprendimiento no existe, lo indica en la respuesta.

```json
{ "found": true, "approved": true, "status": "APPROVED", "id": 2, "legalName": "Kayak Rocha" }
```

```json
{ "found": false, "approved": false, "status": null, "id": null, "legalName": null }
```

`status` puede ser `PENDING`, `APPROVED` o `REJECTED`.

### Buscar actividades por palabra clave (RF-52)

`GET /api/public/activities/search?q=kayak`

Busca en el nombre y la descripción. Devuelve lo mismo que la búsqueda del portal (sección 2).

---

## 2. Portal público

| Método | Ruta | Qué hace |
|--------|------|----------|
| GET | `/api/activities` | Busca actividades publicadas (CU-13). Filtros opcionales: `q`, `department`, `locality`, `tagId`, `date`, `maxPrice` |
| GET | `/api/activities/{id}` | Detalle de una actividad con sus próximas fechas, valoraciones y promedio (CU-14) |
| GET | `/api/activities/{id}/weather` | Pronóstico del clima para sus próximas fechas (RF-06). Lista vacía si el servicio de clima no responde |
| GET | `/api/tags` | Tipos de actividad |
| GET | `/api/equipment` | Tipos de equipamiento |
| GET | `/api/departments` | Los 19 departamentos |
| GET | `/api/health` | Estado del backend y de la base de datos |

Cada actividad de `GET /api/activities`:

```json
{
  "id": 1,
  "name": "Kayak en la laguna",
  "businessName": "Kayak Rocha",
  "department": "Rocha",
  "locality": "La Paloma",
  "durationMinutes": 120,
  "tags": ["Kayak"],
  "priceFrom": 1500.00,
  "nextDate": "2026-12-05"
}
```

## 3. Cuentas

| Método | Ruta | Quién | Qué hace |
|--------|------|-------|----------|
| POST | `/api/tourists` | Público | Registra un turista (CU-03). **201** |
| POST | `/api/businesses` | Público | Registra un emprendimiento, que queda pendiente (CU-01). **201** |
| POST | `/api/auth/login` | Público | Inicia sesión (CU-04). Recibe `email` y `password`; devuelve `token` y `user`. **401** si no son correctos, **403** si el emprendimiento no está aprobado |
| GET | `/api/account/me` | Cualquier usuario | Quién inició sesión |
| POST | `/api/auth/password-reset` | Público | Manda el correo para cambiar la contraseña (CU-05). Recibe `email`. **204**, o **404** si no hay cuenta |
| POST | `/api/auth/password-reset/confirm` | Público | Guarda la contraseña nueva. Recibe `token` y `password`. **204**, o **409** si el enlace venció |
| GET / PUT | `/api/tourist/profile` | Turista | Ver / modificar su perfil (CU-06) |
| GET / PUT | `/api/provider/business` | Prestador | Ver / modificar los datos de su emprendimiento (CU-06) |

Cerrar sesión (CU-07) no tiene endpoint: el frontend descarta el token.

## 4. Administrador

| Método | Ruta | Qué hace |
|--------|------|----------|
| GET | `/api/admin/businesses?status=PENDING` | Emprendimientos por estado (`PENDING`, `APPROVED`, `REJECTED`) |
| GET | `/api/admin/businesses/{id}` | Detalle de un emprendimiento |
| POST | `/api/admin/businesses/{id}/approve` | Aprueba un emprendimiento pendiente (CU-02) |
| POST | `/api/admin/businesses/{id}/reject` | Rechaza un emprendimiento pendiente (CU-02) |
| DELETE | `/api/admin/businesses/{id}` | Elimina a un prestador (CU-23). **204** |
| GET | `/api/admin/activities` | Todas las actividades publicadas |
| DELETE | `/api/admin/activities/{id}` | Elimina una actividad inapropiada (CU-24). **204** |
| POST | `/api/admin/tags` | Crea una etiqueta (CU-08). Recibe `name`. **201** |
| PUT | `/api/admin/tags/{id}` | Cambia el nombre de una etiqueta |
| DELETE | `/api/admin/tags/{id}` | Elimina una etiqueta. **204**, o **409** si la usa una actividad publicada |
| POST / PUT / DELETE | `/api/admin/equipment`, `/api/admin/equipment/{id}` | Lo mismo para el equipamiento (CU-09) |

## 5. Prestador

| Método | Ruta | Qué hace |
|--------|------|----------|
| GET | `/api/provider/activities` | Sus actividades |
| POST | `/api/provider/activities` | Crea una actividad (CU-10). **201** |
| GET | `/api/provider/activities/{id}` | Una actividad suya |
| PUT | `/api/provider/activities/{id}` | Modifica una actividad |
| DELETE | `/api/provider/activities/{id}` | Elimina una actividad: cancela sus fechas y devuelve el 100 % de las reservas pagadas. **204** |
| GET | `/api/provider/activities/{id}/schedules` | Fechas de una actividad |
| POST | `/api/provider/activities/{id}/schedules` | Agrega una fecha (CU-11). Recibe `date`, `startTime` y, opcionales, `capacity` y `price`. **201** |
| POST | `/api/provider/schedules/{id}/cancel` | Cancela una fecha (CU-12): cancela sus reservas, avisa a los turistas y devuelve el 100 % |
| GET | `/api/provider/schedules/{id}/bookings` | Reservas y participantes de una fecha (CU-22) |
| GET | `/api/provider/activities/{id}/reviews` | Valoraciones de una actividad suya |
| PUT | `/api/provider/reviews/{id}/reply` | Responde un comentario (CU-21). Recibe `reply` |

Lo que recibe `POST` / `PUT` de una actividad:

```json
{
  "name": "Kayak en la laguna",
  "description": "Paseo guiado en kayak",
  "requirements": "Mayores de 10 años",
  "observations": null,
  "department": "Rocha",
  "locality": "La Paloma",
  "address": null,
  "latitude": -34.66,
  "longitude": -54.16,
  "durationMinutes": 120,
  "defaultCapacity": 8,
  "defaultPrice": 1500,
  "minBookingMinutes": 60,
  "maxBookingMinutes": null,
  "cancellable": true,
  "cancellationMinutes": 1440,
  "refundable": true,
  "refundPercentage": 50,
  "tagIds": [3],
  "requiredEquipmentIds": [5],
  "providedEquipmentIds": [2]
}
```

## 6. Turista: reservas, pagos y valoraciones

| Método | Ruta | Qué hace |
|--------|------|----------|
| POST | `/api/tourist/bookings` | Retiene lugares por 10 minutos (CU-15). Recibe `scheduleId` y `participantCount`. **201**, o **409** si no hay lugares o está fuera de plazo |
| PUT | `/api/tourist/bookings/{id}/participants` | Carga los datos de cada participante. Si la actividad no tiene costo, la reserva queda confirmada |
| GET | `/api/tourist/bookings` | Sus reservas (CU-18) |
| GET | `/api/tourist/bookings/{id}` | Una reserva suya |
| POST | `/api/tourist/bookings/{id}/cancel` | Cancela una reserva (CU-19). **409** si la actividad no es cancelable o pasó el plazo |
| POST | `/api/tourist/bookings/{id}/payments` | Paga una reserva pendiente (CU-16). **201** |
| GET | `/api/tourist/cards` | Sus tarjetas guardadas (solo los últimos 4 dígitos) |
| GET | `/api/tourist/activities/{id}/review` | Si puede valorar la actividad y su valoración, si ya la escribió |
| POST | `/api/tourist/activities/{id}/reviews` | Valora una actividad (CU-20). Recibe `rating` (1 a 5) y `comment`. **201**, o **409** si no participó |
| PUT | `/api/tourist/reviews/{id}` | Edita su valoración |
| DELETE | `/api/tourist/reviews/{id}` | Borra su valoración. **204** |

### Pagar una reserva

`POST /api/tourist/bookings/{id}/payments` recibe una tarjeta nueva:

```json
{ "cardNumber": "4111 1111 1111 1111", "holder": "Sofía Martínez", "expiry": "12/29", "cvc": "123" }
```

o una tarjeta guardada, con su código de seguridad (se pide cada vez):

```json
{ "cardId": 1, "cvc": "123" }
```

Responde **201** con el intento de pago y cómo quedó la reserva. La reserva solo queda
`CONFIRMED` si el pago fue `APPROVED`; si fue `REJECTED` o `PENDING` (el servicio de pagos
falló o no respondió), sigue `PENDING_PAYMENT` por 24 horas y se puede volver a intentar.

```json
{
  "payment": {
    "id": 7,
    "amount": 3000.00,
    "status": "APPROVED",
    "cardLast4": "1111",
    "externalReference": "sbx_5bd3fa7b5608",
    "message": null,
    "createdAt": "2026-11-15T20:43:09"
  },
  "booking": { "id": 4, "code": "UM-86A6BAE0", "status": "CONFIRMED", "total": 3000.00 }
}
```

Estados de una reserva: `PENDING_PAYMENT`, `CONFIRMED`, `EXPIRED`, `CANCELLED`.

| GET | `/api/tourist/payment-provider` | Qué servicio de pagos está conectado (`mercadopago` o `sandbox`), para mostrar sus tarjetas de prueba |

## 7. Servicio de pagos externo

Uruguay Mnatural no procesa los pagos (RNF-10): el backend le pide el cobro a un servicio
externo. Cuál se usa se elige en `application.properties` con `payments.provider`.

### Mercado Pago (`payments.provider=mercadopago`, por defecto)

El backend consume la API REST de Mercado Pago (`https://api.mercadopago.com`) con
credenciales de **prueba**, que funcionan como sandbox: no se mueve plata de verdad. No se usa
su checkout ni sus componentes para la página: el formulario de la tarjeta es el nuestro.

| Llamada a Mercado Pago | Para qué |
|------------------------|----------|
| `POST /v1/card_tokens` | Convierte los datos de la tarjeta en un token de un solo uso |
| `GET /v1/payment_methods/installments` | Averigua el tipo de tarjeta (visa, master…) por sus primeros 6 dígitos |
| `POST /v1/payments` | Cobra el token. La reserva se confirma solo si responde `approved` |
| `PUT /v1/payments/{id}` | Anula un pago que quedó "en revisión", para que no se cobre después |
| `POST /v1/payments/{id}/refunds` | Devuelve todo o parte de un pago aprobado |
| `GET /v1/customers/search`, `POST /v1/customers`, `POST /v1/customers/{id}/cards` | Guarda la tarjeta para la próxima vez |

El Access Token de prueba va en `backend/secrets.properties` (ver
`backend/secrets.properties.example`), que no se sube a git.

Con la tarjeta de prueba `5031 7557 3453 0604` (vencimiento `11/30`, código `123`) el
resultado depende del **nombre del titular**:

| Titular | Resultado |
|---------|-----------|
| `APRO` | Pago aprobado |
| `OTHE` | Pago rechazado |
| `FUND` | Rechazado por fondos insuficientes |
| `CONT` | Queda en revisión: se anula y la reserva sigue pendiente |

### Proveedor simulado (`payments.provider=sandbox`)

Simula a un proveedor de pagos y vive en `/sandbox/payments` dentro de este proyecto. No
necesita cuenta ni claves, y además permite probar que el servicio falle o no responda. El
backend lo llama por HTTP igual que a Mercado Pago. El resultado depende del **número**:

| Tarjeta | Resultado |
|---------|-----------|
| `4111 1111 1111 1111` (o cualquier otra válida) | Pago aprobado |
| `4000 0000 0000 0002` | Pago rechazado |
| `4000 0000 0000 0500` | El servicio devuelve un error |
| `4000 0000 0000 0408` | El servicio no responde a tiempo |

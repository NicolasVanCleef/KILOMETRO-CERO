# Diagramas de secuencia — Uruguay Mnatural

Muestran, en orden, los mensajes que se mandan el usuario, el frontend y las clases del backend
(controlador → servicio → repositorio) en los casos de uso principales. Los nombres de clases,
métodos y rutas son los del código.

| # | Diagrama | Caso de uso (ERS) |
|---|---|---|
| 1 | [Iniciar sesión](#1-iniciar-sesión) | CU-04 |
| 2 | [Reservar una actividad](#2-reservar-una-actividad) | CU-15 |
| 3 | [Pagar una reserva](#3-pagar-una-reserva) | CU-16 |
| 4 | [Registrar y aprobar un emprendimiento](#4-registrar-y-aprobar-un-emprendimiento) | CU-01, CU-02 |
| 5 | [El prestador cancela una fecha](#5-el-prestador-cancela-una-fecha) | CU-12 |
| 6 | [Vencimiento automático de reservas](#6-vencimiento-automático-de-reservas) | CU-17 |

## 1. Iniciar sesión

La misma pantalla sirve para los tres roles. El backend devuelve un token JWT que el frontend
manda en cada pedido; `AuthInterceptor` controla el rol según el comienzo de la ruta.

![Secuencia: iniciar sesión](img/Secuencia_1_iniciar_sesion.png)

## 2. Reservar una actividad

La reserva funciona "como la de un avión": al elegir la cantidad de personas los lugares se
retienen 10 minutos. La fecha se lee con `SELECT ... FOR UPDATE`, así dos turistas que piden los
últimos lugares al mismo tiempo no pueden llevarse los mismos.

![Secuencia: reservar](img/Secuencia_2_reservar.png)

## 3. Pagar una reserva

El llamado a Mercado Pago puede demorar hasta un minuto, así que se hace **fuera de las
transacciones de la base**: una transacción corta antes (validar y extender el plazo a 24 horas)
y otra después (registrar el resultado y confirmar). Si mientras tanto la reserva dejó de estar
pendiente (por ejemplo, el prestador canceló la fecha), el pago aprobado se devuelve entero.

![Secuencia: pagar](img/Secuencia_3_pagar.png)

## 4. Registrar y aprobar un emprendimiento

El prestador se registra con el usuario deshabilitado. Solo puede iniciar sesión cuando el
administrador lo acepta.

![Secuencia: aprobar emprendimiento](img/Secuencia_4_aprobar_emprendimiento.png)

## 5. El prestador cancela una fecha

Se cancelan todas las reservas de la fecha, se avisa a cada turista por correo y a los que habían
pagado se les devuelve el 100 %. Eliminar una actividad (prestador o administrador) o eliminar un
prestador (administrador) hace lo mismo con cada fecha futura.

![Secuencia: cancelar fecha](img/Secuencia_5_cancelar_fecha.png)

## 6. Vencimiento automático de reservas

Una tarea programada (`@Scheduled`) revisa cada minuto las reservas pendientes cuyo plazo venció,
las pasa a EXPIRED y devuelve los lugares a la fecha.

![Secuencia: vencimiento](img/Secuencia_6_vencimiento.png)

Las imágenes vectoriales (SVG) están en [`img/`](img/), con el mismo nombre.

<details>
<summary>Fuente de los diagramas (Mermaid, para editar)</summary>

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuario
    participant F as Frontend (LoginPage)
    participant C as AuthController
    participant S as AuthService
    participant R as UserRepository
    participant J as JwtService

    U->>F: Escribe correo y contraseña y toca "Entrar"
    F->>C: POST /api/auth/login {email, password}
    C->>S: login(datos)
    S->>R: findByEmailIgnoreCase(email)
    R-->>S: User
    alt correo o contraseña incorrectos
        S-->>C: UnauthorizedException
        C-->>F: 401 "El correo o la contraseña no son correctos."
        F-->>U: Muestra el error
    else usuario deshabilitado (emprendimiento pendiente, rechazado o eliminado)
        S-->>C: ForbiddenException
        C-->>F: 403 con el motivo
        F-->>U: Muestra el motivo
    else datos correctos
        S->>J: createToken(user)
        J-->>S: token JWT (vence en 8 horas)
        S-->>C: SessionDto {token, user}
        C-->>F: 200 SessionDto
        F->>F: Guarda la sesión en localStorage
        F-->>U: Muestra el menú de su rol
    end

    Note over F,C: En los pedidos siguientes el frontend manda "Authorization: Bearer token".<br/>AuthInterceptor controla el rol según la ruta: /api/admin, /api/provider, /api/tourist.
```

```mermaid
sequenceDiagram
    autonumber
    actor T as Turista
    participant F as Frontend (BookingPage)
    participant C as BookingController
    participant S as BookingService
    participant SR as ActivityScheduleRepository
    participant BR as BookingRepository
    participant E as EmailService

    T->>F: Elige fecha y cantidad de personas y toca "Reservar lugares"
    F->>C: POST /api/tourist/bookings {scheduleId, participantCount}
    C->>S: hold(userId, datos)
    S->>SR: findByIdForUpdate(scheduleId)
    Note right of SR: SELECT ... FOR UPDATE bloquea la fila de la fecha.<br/>Si otro turista pide los mismos lugares, espera.
    SR-->>S: ActivitySchedule
    alt cancelada, fuera de plazo o sin lugares suficientes
        S-->>C: BusinessRuleException
        C-->>F: 409 con el motivo
        F-->>T: Muestra el motivo
    else hay lugares
        S->>S: schedule.takePlaces(n)
        S->>BR: save(Booking PENDING_PAYMENT, vence en 10 minutos)
        S-->>C: BookingDto
        C-->>F: 201 reserva con paymentDeadline
        F-->>T: Formulario de participantes y cuenta regresiva
    end

    T->>F: Carga los datos de cada persona y toca "Siguiente"
    F->>C: PUT /api/tourist/bookings/{id}/participants
    C->>S: saveParticipants(userId, id, participantes)
    alt actividad sin costo
        S->>S: booking.confirm()
        S->>E: send("Tu reserva está confirmada", código)
        S-->>F: reserva CONFIRMED
        F-->>T: "¡Reserva confirmada!" con el código
    else actividad con costo
        S-->>F: reserva PENDING_PAYMENT
        F-->>T: "Falta el pago" con el botón "Ir a pagar"
    end
```

```mermaid
sequenceDiagram
    autonumber
    actor T as Turista
    participant F as Frontend (PayBookingPage)
    participant C as PaymentController
    participant S as PaymentService
    participant B as BookingService
    participant G as MercadoPagoGateway
    participant MP as API de Mercado Pago
    participant DB as PostgreSQL

    T->>F: Completa la tarjeta y toca "Pagar"
    F->>C: POST /api/tourist/bookings/{id}/payments
    C->>S: payBooking(userId, id, datos)

    rect rgb(232, 243, 238)
    Note over S,DB: Transacción 1, corta
    S->>B: findOwn y requirePending
    S->>S: Valida la tarjeta y que haya participantes
    S->>DB: paymentDeadline = ahora + 24 horas
    end

    Note over S,MP: Fuera de toda transacción: puede demorar hasta 1 minuto
    S->>G: charge(tarjeta, pedido)
    G->>MP: POST /v1/card_tokens
    MP-->>G: token de la tarjeta
    G->>MP: POST /v1/payments
    MP-->>G: approved, rejected, in_process o error
    G-->>S: Outcome (nunca lanza una excepción)

    rect rgb(232, 243, 238)
    Note over S,DB: Transacción 2, corta
    S->>DB: save(Payment con el resultado y los últimos 4 dígitos)
    alt aprobado y la reserva sigue pendiente
        S->>B: confirm(booking): correo con el código
        S->>DB: save(Card: solo el token y los últimos 4 dígitos)
    else aprobado, pero la reserva ya no estaba pendiente
        S->>MP: Reembolso del 100 % (RefundService)
    else rechazado, error o sin respuesta
        Note over S,DB: No se confirma: la reserva queda pendiente 24 horas para reintentar
    end
    end

    S-->>C: BookingPaymentDto
    C-->>F: 200 pago y reserva
    F-->>T: "¡Reserva confirmada!" o el motivo del rechazo
```

```mermaid
sequenceDiagram
    autonumber
    actor P as Prestador
    actor A as Administrador
    participant F as Frontend
    participant BC as BusinessController
    participant AC as AdminBusinessController
    participant S as BusinessService
    participant R as Repositorios
    participant E as EmailService

    P->>F: Completa "Registrar emprendimiento" y toca "Enviar solicitud"
    F->>BC: POST /api/businesses
    BC->>S: register(datos)
    S->>R: save(User deshabilitado y Business PENDING)
    S-->>F: 201 emprendimiento
    F-->>P: "¡Listo! Recibimos tu solicitud"

    A->>F: Emprendimientos, pestaña Pendientes
    F->>AC: GET /api/admin/businesses?status=PENDING
    AC-->>F: lista
    A->>F: Toca el emprendimiento
    F->>AC: GET /api/admin/businesses/{id}
    AC-->>F: detalle
    A->>F: "Aceptar" y después "Sí, aceptar"
    F->>AC: POST /api/admin/businesses/{id}/approve
    AC->>S: approve(id)
    S->>S: business.approve(): estado APPROVED y usuario habilitado
    S->>E: send("Tu emprendimiento fue aprobado")
    S-->>AC: BusinessDetailDto
    AC-->>F: 200
    F-->>A: "Aprobaste ... Ya puede iniciar sesión"

    Note over A,E: Rechazar es igual con POST .../reject: estado REJECTED, usuario deshabilitado y correo.
```

```mermaid
sequenceDiagram
    autonumber
    actor P as Prestador
    participant F as Frontend (ActivitySchedulesPage)
    participant C as ProviderActivityController
    participant S as ScheduleService
    participant B as BookingService
    participant RF as RefundService
    participant G as PaymentGateway
    participant E as EmailService

    P->>F: "Cancelar" y después "Sí, cancelar"
    F->>C: POST /api/provider/schedules/{id}/cancel
    C->>S: cancel(userId, id)
    S->>S: Controla que la fecha sea suya, esté programada y no haya pasado
    S->>S: schedule.cancel()
    S->>B: cancelAllOf(schedule, motivo)
    loop cada reserva pendiente o confirmada de la fecha
        B->>B: booking.cancel()
        opt la reserva estaba pagada
            B->>RF: refund(booking, 100 %)
            RF->>G: refund(referencia, monto)
            G-->>RF: resultado
        end
        B->>E: send("Se canceló tu reserva")
    end
    S-->>C: ScheduleDto CANCELLED
    C-->>F: 200
    F-->>P: "Fecha cancelada. A los turistas con reserva se les avisó por correo..."
```

```mermaid
sequenceDiagram
    autonumber
    participant J as BookingExpirationJob
    participant S as BookingService
    participant BR as BookingRepository
    participant SC as ActivitySchedule

    loop cada 1 minuto (@Scheduled)
        J->>S: expireOverdue()
        S->>BR: findByStatusAndPaymentDeadlineBefore(PENDING_PAYMENT, ahora)
        BR-->>S: reservas vencidas
        loop cada reserva vencida
            S->>S: booking.expire(): pasa a EXPIRED
            S->>SC: releasePlaces(n)
        end
        S-->>J: cantidad liberada
        J->>J: log "Reservas vencidas liberadas: n"
    end
```

</details>

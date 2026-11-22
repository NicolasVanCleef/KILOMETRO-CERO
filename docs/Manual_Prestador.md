# Manual de usuario — Prestador

**Uruguay Mnatural · por Kilómetro Cero** · Proyecto TIC 2026

Este manual es para los **clientes prestatarios de servicio**: los emprendimientos que ofrecen
actividades turísticas (una persona o una empresa). Explica cómo registrarse, cargar las
actividades, programar las fechas y atender las reservas y las valoraciones.

---

## 1. Cómo funciona

1. Registrás tu emprendimiento. Queda **pendiente**.
2. Un administrador de Uruguay Mnatural revisa los datos y lo **acepta** o lo **rechaza**. Te
   avisamos por correo.
3. Cuando está aceptado, iniciás sesión y cargás tus **actividades** (qué ofrecés).
4. A cada actividad le agregás **fechas** (cuándo), cada una con su cupo y su precio.
5. Los turistas reservan y pagan en la página. Vos ves las reservas de cada fecha.
6. Después de la actividad, los turistas la valoran y vos podés responder.

Cuando iniciás sesión, en el menú de arriba aparecen **Mis actividades** y **Mi
emprendimiento**, y a la derecha tu nombre y el botón **Cerrar sesión**.

---

## 2. Registrar tu emprendimiento

1. Tocá **Crear cuenta** y después **Registrá tu emprendimiento**. (Desde el inicio también está
   el botón **Registrar mi emprendimiento**.)
2. Elegí **¿Quién ofrece las actividades?**
   - **Persona física:** un particular. Se identifica con **CI** (7 u 8 dígitos, sin puntos ni
     guion).
   - **Persona jurídica:** una empresa. Se identifica con **RUT** (12 dígitos) y pide el nombre
     del **representante**.
3. Completá la **razón social**.
4. Completá **Contacto y ubicación:**
   - Teléfono (9 dígitos, por ejemplo 099123456).
   - Correo electrónico: **es con el que vas a iniciar sesión.**
   - Departamento, localidad y dirección (la dirección es opcional).
   - **Descripción de los servicios:** qué actividades ofrecés, dónde y para quién.
5. Elegí una contraseña de al menos 8 caracteres y repetila.
6. Tocá **Enviar solicitud**.

Aparece "¡Listo! Recibimos tu solicitud". **Hasta que el emprendimiento esté aprobado no vas a
poder iniciar sesión.** Cuando el administrador lo revise, te llega un correo con la respuesta.

> En esta versión los correos son simulados: se muestran en la consola del servidor.

---

## 3. Iniciar sesión y contraseña

- **Iniciar sesión:** tocá **Iniciar sesión**, escribí tu correo y tu contraseña, y tocá
  **Entrar**.
- **Me olvidé la contraseña:** en la misma pantalla, tocá **Me olvidé la contraseña**, escribí
  tu correo y tocá **Enviar enlace**. Abrí el enlace del correo (vale por 30 minutos), escribí
  la contraseña nueva dos veces y tocá **Guardar contraseña**.
- **Cerrar sesión:** botón **Cerrar sesión**, arriba a la derecha.

---

## 4. Mi emprendimiento

En **Mi emprendimiento** podés cambiar:

- El representante (solo persona jurídica).
- El teléfono.
- El departamento, la localidad y la dirección.
- La descripción de los servicios.

Tocá **Guardar cambios**.

La **identificación** (tipo, CI o RUT, razón social y correo) **no se puede cambiar**, porque es
la que aprobó Uruguay Mnatural.

---

## 5. Mis actividades

**Mis actividades** muestra una tarjeta por cada actividad, con sus etiquetas, el lugar, la
duración, el precio y el cupo. Cada tarjeta tiene cuatro botones:

| Botón | Para qué |
|---|---|
| **Fechas** | Programar la actividad en días y horas, y ver las reservas |
| **Valoraciones** | Leer y responder los comentarios de los turistas |
| **Modificar** | Cambiar los datos de la actividad |
| **Eliminar** | Borrar la actividad |

### 5.1 Crear o modificar una actividad

Tocá **Nueva actividad** (o **Modificar** en una existente) y completá el formulario:

**La actividad**

- **Nombre** y **descripción**.
- **Requisitos** (opcional), por ejemplo edad mínima.
- **Observaciones** (opcional).
- **Duración aproximada** en minutos.

**Etiquetas:** elegí **al menos una** (por ejemplo trekking o cabalgata). Son los tipos de
actividad con los que los turistas filtran. Las carga el administrador: si falta una, pedísela.

**Ubicación**

- Departamento, localidad y dirección (opcional).
- Latitud y longitud (opcional), por ejemplo -34.65 y -54.16.

**Precio, cupo y reservas**

- **Precio por persona ($):** 0 si no tiene costo.
- **Cupo:** cuántas personas por fecha.
- **Se puede reservar hasta (minutos antes):** 0 = hasta la hora de inicio. Por ejemplo, 120 =
  hasta dos horas antes.
- **Se puede reservar desde (minutos antes, opcional):** vacío = sin límite.

> El precio y el cupo son los **valores por defecto**: cada fecha puede tener los suyos.

**Cancelación**

- Marcá **El turista puede cancelar su reserva** si querés permitirlo. Después indicá **hasta
  cuántos minutos antes del inicio** (por ejemplo 1440 = un día antes).
- Marcá **La cancelación tiene reembolso** si se devuelve plata, y el **porcentaje que se
  devuelve** (de 0 a 100).

Esta política se le muestra al turista en el detalle de la actividad antes de reservar.

**Equipamiento**

- **Lo tiene que llevar el turista:** por ejemplo calzado de trekking.
- **Lo da el prestador:** por ejemplo casco o chaleco salvavidas.

Un mismo equipamiento no puede estar en las dos listas. La lista de equipamiento también la
carga el administrador.

Tocá **Guardar actividad**. La actividad aparece en el portal en el momento, pero los turistas
solo pueden reservarla cuando le agregues **fechas** (sección 6).

### 5.2 Eliminar una actividad

Tocá **Eliminar** y confirmá con **Sí, eliminar**. **Se cancelan todas sus fechas** y a los
turistas que habían pagado **se les devuelve el 100 %**.

---

## 6. Fechas de una actividad

Tocá **Fechas** en la actividad. A la izquierda está la tabla de fechas y a la derecha el
formulario **Agregar una fecha**.

### 6.1 Agregar una fecha

1. Elegí la **fecha** (de hoy en adelante) y la **hora de inicio**.
2. Escribí el **cupo** y el **precio por persona**. Si los dejás vacíos, se usan los de la
   actividad. Podés, por ejemplo, cobrar menos entre semana que el fin de semana.
3. Tocá **Agregar fecha**.

### 6.2 La tabla de fechas

Para cada fecha ves la hora, los **lugares libres de** el total, el precio y el estado:

| Estado | Qué significa |
|---|---|
| Programada | Se puede reservar (si quedan lugares y está dentro del plazo) |
| Realizada | La fecha ya pasó |
| Cancelada | La cancelaste vos |

### 6.3 Cancelar una fecha

Tocá **Cancelar** y confirmá con **Sí, cancelar** (el botón te dice cuántos lugares están
reservados). A cada turista con reserva **se le avisa por correo** y a los que pagaron **se les
devuelve el 100 %**. Usalo, por ejemplo, si hay mal tiempo.

### 6.4 Posponer una fecha

No hay un botón para mover una fecha. **Cancelala y agregá la nueva:** los turistas reciben el
aviso y la devolución, y pueden reservar la fecha nueva.

---

## 7. Reservas de una fecha

En la tabla de fechas, tocá **Reservas**. Arriba ves cuántas personas están **confirmadas** y
cuántos lugares quedan. Debajo, una tarjeta por reserva con:

- El nombre y el correo del turista.
- La cantidad de personas, el total y el **código de reserva**.
- El **estado:** Pendiente de pago, Confirmada, Expirada o Cancelada.
- La tabla de **participantes:** nombre, apellido, fecha de nacimiento y cédula. Si todavía no
  la cargó, dice "Todavía no cargó los datos de los participantes."

Con el código y la lista de participantes podés controlar quién llega el día de la actividad.

> Los lugares de una reserva **pendiente de pago** quedan guardados para ese turista: 10 minutos
> mientras reserva y, si el pago falla, hasta 24 horas para reintentar. Si no paga, la reserva
> vence y los lugares vuelven a estar libres solos.

---

## 8. Valoraciones

Tocá **Valoraciones** en la actividad. Ves cada comentario con su puntaje (de 1 a 5 estrellas),
el nombre del turista y la fecha.

1. Tocá **Responder** debajo del comentario.
2. Escribí tu respuesta y tocá **Guardar respuesta**.

La respuesta se publica **debajo del comentario** en el detalle de la actividad. Podés cambiarla
después con **Cambiar respuesta**.

Las valoraciones las escriben solo los turistas que participaron, y se publican sin moderación.

---

## 9. Mensajes frecuentes

| Mensaje | Qué hacer |
|---|---|
| "Tu emprendimiento todavía no fue aprobado..." | Todavía está pendiente. Esperá el correo del administrador. |
| "Tu emprendimiento no fue aprobado, así que no podés iniciar sesión." | El administrador rechazó la solicitud. |
| "Uruguay Mnatural dio de baja tu emprendimiento..." | El administrador eliminó el emprendimiento. |
| "El administrador todavía no cargó etiquetas." | Pedile al administrador que cargue los tipos de actividad. |
| "Esta sección es para usuarios con perfil Prestador." | Iniciá sesión con la cuenta del emprendimiento. |
| En mi actividad dice "Por ahora no hay fechas próximas." | Agregale fechas desde **Fechas** (sección 6). |

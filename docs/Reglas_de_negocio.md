# Reglas de negocio — Uruguay Mnatural

Las reglas de negocio son las condiciones y políticas de Uruguay Mnatural que el sistema
tiene que respetar. No dicen qué funciones tiene el sistema (eso está en los requerimientos
funcionales de la ERS), sino bajo qué condiciones y con qué límites funciona el negocio:
cuándo se puede reservar, cuánto se devuelve si se cancela, quién puede valorar, etc.

El enunciado dice que el cliente "no ha definido en detalle todos los procesos, reglas de
negocio, estados ni excepciones" y que la empresa desarrolladora tiene que analizarlos. Acá
juntamos las que salieron del enunciado, de la reunión con el cliente y de lo que decidimos
nosotros. Al lado de cada una ponemos de dónde salió: **[E]** enunciado, **[A]** acta de
reunión con el cliente del 14/09/2026, **[D]** indicación del docente en clase, e **[I]**
interpretación nuestra (hay que confirmarla con el cliente). En "Se aplica en" están los
requerimientos (RF / RNF) y casos de uso (CU) de la ERS donde se usa cada regla.

### 1. Emprendimientos y prestadores

| ID | Regla | Fuente | Se aplica en |
|----|-------|--------|--------------|
| RN-01 | Un emprendimiento nuevo queda pendiente hasta que un administrador lo aprueba. Mientras no esté aprobado, el prestador no puede iniciar sesión ni publicar actividades. Si se rechaza, tampoco. | [A] | RF-30, RF-31, RF-45, CU-01, CU-02, CU-04 |
| RN-02 | Un emprendimiento puede ser de una persona física o de una persona jurídica. La persona física se identifica con CI y la jurídica con RUT, y la jurídica además tiene que indicar quién es su representante. | [A] [I: CI para física, RUT para jurídica] | RF-29, CU-01 |
| RN-03 | Una persona física puede tener un solo emprendimiento por usuario. Si quiere otro, tiene que registrarlo con otro usuario o cargarlo como otra actividad del mismo emprendimiento. | [A] | RF-32, CU-01 |
| RN-04 | Al aceptar o rechazar un emprendimiento, siempre se le avisa al prestador por correo. | [A] | RF-45, CU-02 |

### 2. Actividades y fechas

| ID | Regla | Fuente | Se aplica en |
|----|-------|--------|--------------|
| RN-05 | Toda actividad tiene que tener al menos una etiqueta (por ejemplo, trekking). | [A] | RF-35, CU-10 |
| RN-06 | Las etiquetas y los tipos de equipamiento los da de alta solo el administrador, para que haya criterios iguales en toda la plataforma. El prestador elige de esas listas. | [E] | RF-42, RF-43, CU-08, CU-09, CU-10 |
| RN-07 | Cada equipamiento de una actividad es **requerido** (lo tiene que llevar el turista) o **provisto** (lo da el prestador). | [E] | RF-35, CU-10 |
| RN-08 | La actividad tiene un precio y un cupo por defecto. Cada fecha programada puede tener su propio precio y su propio cupo (por ejemplo, más barato entre semana que el fin de semana). | [A] | RF-38, CU-11 |
| RN-09 | Una actividad con precio 0 no tiene costo: la reserva se confirma sin pasar por el pago. | [A] [I] | CU-15 |
| RN-10 | Cada actividad define hasta cuántos minutos antes del inicio se puede reservar. Si son 0 minutos, se puede reservar hasta la hora de inicio. | [E] | RF-13, RF-36, CU-15 |
| RN-11 | El administrador no maneja las actividades de los prestadores. Solo puede eliminar una actividad si la considera inapropiada. | [E] [A] | RF-47, CU-24 |

### 3. Reservas

| ID | Regla | Fuente | Se aplica en |
|----|-------|--------|--------------|
| RN-12 | Para reservar hay que tener cuenta de turista e iniciar sesión. Para mirar las actividades no. | [E] | RF-01, RF-09, CU-13, CU-15 |
| RN-13 | La cantidad máxima de participantes de una reserva es el cupo disponible de esa fecha. No hay límite de reservas por turista. | [A] | RF-10, RF-16, CU-15 |
| RN-14 | Nunca se pueden reservar más lugares que el cupo de una fecha, y el cupo disponible nunca puede quedar negativo. | [E] | RF-12, RNF-12, CU-15 |
| RN-15 | Cuando el turista elige los lugares, se le retienen solo a él durante 10 minutos, como en las reservas de avión. Si en ese tiempo no da a pagar, los lugares se liberan. | [A] [I: 10 minutos] | RF-11, RNF-12, CU-15, CU-17 |
| RN-16 | En cada reserva hay que cargar nombre, apellido, fecha de nacimiento y cédula de cada participante. | [A] | RF-14, CU-15 |
| RN-17 | Una reserva no se puede modificar (ni la fecha, ni el horario, ni la cantidad). Para cambiarla hay que cancelarla y reservar de nuevo, con las condiciones de cancelación de la actividad. | [A] | RF-19, CU-19 |
| RN-18 | Si el turista no se presenta a la actividad (no-show), se toma como si hubiera ido: la reserva sigue confirmada y no hay reembolso. | [A] | — |

#### Estados de una reserva

| Estado | Cuándo | Qué pasa con los lugares |
|--------|--------|--------------------------|
| Pendiente de pago | El turista dio a pagar y el pago no salió, o el servicio de pagos no respondió | Siguen retenidos hasta 24 horas |
| Confirmada | El pago fue aprobado, o la actividad no tiene costo | Ocupados |
| Expirada | Pasaron 24 horas en pendiente de pago sin que se pagara (es una cancelación automática) | Se liberan |
| Cancelada | La canceló el turista, o el prestador o el administrador cancelaron la fecha o la actividad | Se liberan |

### 4. Pagos

| ID | Regla | Fuente | Se aplica en |
|----|-------|--------|--------------|
| RN-19 | Una reserva con costo solo se confirma si el servicio de pagos aprueba el pago. Uruguay Mnatural no procesa los pagos. | [E] [A] | RF-20, RF-22, RNF-10, CU-16 |
| RN-20 | El sistema espera como máximo 1 minuto la respuesta del servicio de pagos. Si no responde, se trata como un pago no completado. | [I] | RNF-11, CU-16 |
| RN-21 | Si el pago no se completa, la reserva queda pendiente de pago durante 24 horas (1440 minutos). Mientras tanto, el turista puede volver a intentar el pago. | [A] [I: reintentar] | RF-23, CU-16, CU-18 |
| RN-22 | Si pasan las 24 horas sin un pago aprobado, la reserva expira y los lugares se liberan. | [A] | RF-24, RNF-13, CU-17 |
| RN-23 | Al confirmarse una reserva, se le manda al turista un correo con los datos y un código de reserva único. | [A] | RF-22, CU-16 |
| RN-24 | De las tarjetas guardadas no se guarda el número completo, sino el identificador (token) que devuelve el servicio de pagos y los últimos 4 dígitos. | [A] [I: token] | RF-21, RNF-17, CU-16 |

### 5. Cancelaciones y reembolsos

| ID | Regla | Fuente | Se aplica en |
|----|-------|--------|--------------|
| RN-25 | Cada actividad define si se puede cancelar. Si se puede, define también hasta cuántos minutos antes del inicio se puede cancelar y si la cancelación tiene reembolso. Si tiene, define qué porcentaje (de 0 a 100) se le devuelve al turista. | [A] | RF-37, CU-10 |
| RN-26 | El turista solo puede cancelar si la actividad es cancelable y todavía está dentro del plazo. Se le devuelve el porcentaje definido; si la actividad no tiene reembolso, no se le devuelve nada. | [A] | RF-18, CU-19 |
| RN-27 | Si el prestador cancela una fecha o una actividad que tiene reservas pagadas, se le devuelve el 100 % a cada turista y se le avisa de la cancelación. | [A] | RF-39, RF-40, CU-10, CU-12 |
| RN-28 | Si el administrador elimina una actividad con reservas pagadas, también se les devuelve el 100 % a los turistas. | [A] | RF-47, CU-24 |
| RN-29 | Si el prestador pospone una fecha y el turista no puede ir en la nueva fecha, se trata como una cancelación del prestador: 100 % de reembolso. | [I] | RF-39, CU-12 |
| RN-30 | Si el administrador elimina a un prestador que tiene reservas pagadas, se les devuelve el 100 % a los turistas. | [I] | RF-46, CU-23 |

### 6. Valoraciones y comentarios

| ID | Regla | Fuente | Se aplica en |
|----|-------|--------|--------------|
| RN-31 | Solo puede valorar una actividad el turista que participó de ella, o sea, que tiene una reserva confirmada de una fecha que ya pasó. | [E] [D] | RF-25, CU-20 |
| RN-32 | La valoración va sobre la actividad (no sobre una fecha concreta ni sobre el emprendimiento). | [D] | RF-25, CU-20 |
| RN-33 | La valoración tiene un puntaje entero de 1 a 5 y un comentario. | [E] | RF-25, CU-20 |
| RN-34 | Las valoraciones se publican sin moderación. Solo el turista que la escribió puede editarla o borrarla. | [A] | RF-26, RF-28, CU-20 |
| RN-35 | El prestador puede responder los comentarios de sus propias actividades, con una respuesta por comentario. | [A] [I: una respuesta] | RF-27, CU-21 |

### 7. API pública

| ID | Regla | Fuente | Se aplica en |
|----|-------|--------|--------------|
| RN-36 | "Verificar emprendimiento" significa consultar si el emprendimiento está aprobado por Uruguay Mnatural. | [A] [I] | RF-51, CU-25 |
| RN-37 | El top 3 de actividades del mes por departamento se calcula con el promedio de los puntajes de las valoraciones de ese mes. | [I] | RF-50, CU-25 |
| RN-38 | Los listados y búsquedas de la API pública solo muestran emprendimientos aprobados y sus actividades publicadas (la verificación sí responde por cualquier emprendimiento, para decir si está aprobado o no). | [I] | RF-48, RF-49, RF-52, CU-25 |

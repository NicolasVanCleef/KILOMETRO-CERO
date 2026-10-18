# Especificación de Requerimientos de Software (ERS) — Uruguay Mnatural

Documento de especificación de requerimientos con casos de uso, entregable del proyecto
(`Proyecto TIC 2026.pdf`).

> **Cómo se construye este documento:** el contenido lo dicta el equipo, prompt a prompt,
> y se redacta tal como fue indicado. Cada prompt queda registrado textualmente en el
> [Anexo A](#anexo-a--registro-de-prompts), con el número de la sección que generó o modificó.
> Fuentes: **[E]** = enunciado (`Proyecto TIC 2026.pdf`), **[A]** = acta de reunión con el
> cliente (14/09/2026).

## 1. Introducción

### 1.1 Propósito

*Pendiente.*

### 1.2 Alcance

*Pendiente.*

### 1.3 Definiciones y glosario

*Pendiente.*

### 1.4 Referencias

*Pendiente.*

## 2. Descripción general

### 2.1 Actores

*Pendiente.*

### 2.2 Restricciones y supuestos

*Pendiente.*

## 3. Requerimientos funcionales

Acá van las cosas que el sistema tiene que hacer. Las sacamos del enunciado y de lo que
nos contestó el cliente en la reunión del 14/09/2026. Al lado de cada una ponemos de dónde
salió: **[E]** si está en el enunciado, **[A]** si nos lo dijo el cliente en el acta, e
**[I]** si es algo que interpretamos nosotros porque no está dicho tal cual (esas las
tenemos que confirmar con el cliente).

### 3.1 Portal público (visitante sin registrarse)

Cualquiera tiene que poder entrar y mirar la oferta sin crearse una cuenta.

| ID | Requerimiento | Fuente |
|----|---------------|--------|
| RF-01 | Cualquier visitante puede ver las actividades publicadas sin registrarse. | [E] |
| RF-02 | El visitante puede buscar y filtrar actividades por destino (departamento / localidad), tipo de actividad (etiqueta), fecha y precio. | [E] |
| RF-03 | Al entrar a una actividad se ve su detalle: qué emprendimiento la ofrece, dónde es, la descripción, cuánto dura más o menos, el precio, los requisitos (por ejemplo la edad), las observaciones, las etiquetas, el equipamiento que tiene que llevar el turista y el que le da el prestador. | [E] [A] |
| RF-04 | En el detalle también se ven las próximas fechas y horarios en que se puede hacer la actividad, con los lugares que quedan en cada una. | [E] |
| RF-05 | En el detalle se ven las valoraciones de otros turistas (puntaje de 1 a 5 y comentario) y lo que les respondió el prestador. | [E] [A] |
| RF-06 | Estaría bueno mostrar el pronóstico del clima para la fecha y el lugar de la actividad, usando alguna API pública de clima. El enunciado lo pone como "sería conveniente", así que no es obligatorio. | [E] |

### 3.2 Turistas: cuenta y reservas

Para reservar sí hace falta cuenta. El flujo de la reserva nos lo explicó el cliente en la
reunión, "estilo página de avión": elegís cuántos lugares, se te guardan, cargás los datos
de los participantes y pagás.

| ID | Requerimiento | Fuente |
|----|---------------|--------|
| RF-07 | El turista se puede registrar con nombre, apellido, país, fecha de nacimiento, idioma de preferencia y documento. Le agregamos correo y contraseña para que pueda iniciar sesión (el acta no los nombra, pero sin eso no hay login). | [A] [I: correo y contraseña] |
| RF-08 | El turista puede iniciar y cerrar sesión. | [E] |
| RF-09 | Para reservar el turista tiene que haber iniciado sesión. | [E] |
| RF-10 | El turista elige una fecha y horario de la actividad y cuántas personas van. El máximo de personas lo da el cupo que queda; no hay otro límite por reserva. | [E] [A] |
| RF-11 | Cuando el turista elige la cantidad de lugares, esos lugares quedan bloqueados para él durante 10 minutos mientras termina la reserva, así nadie más se los puede sacar. Si en esos 10 minutos no llega a dar a pagar, los lugares se liberan. | [A] [I: 10 minutos] |
| RF-12 | Si no hay lugares suficientes en esa fecha, no se puede reservar. | [E] |
| RF-13 | No se puede reservar si faltan menos minutos para que empiece la actividad que los que puso el emprendimiento. Si puso 0 minutos, se puede reservar hasta la hora de inicio. | [E] |
| RF-14 | En la reserva se piden los datos de cada participante: nombre, apellido, fecha de nacimiento y cédula. | [A] |
| RF-15 | Si la actividad tiene costo, se muestra cuánto hay que pagar antes de pagar. | [A] |
| RF-16 | Un turista puede tener todas las reservas que quiera, no hay límite. | [A] |
| RF-17 | El turista puede ver sus reservas y en qué estado está cada una. | [E] |
| RF-18 | El turista puede cancelar una reserva solo si la actividad se marcó como cancelable y si todavía está dentro de los minutos que puso el prestador. Si la actividad tiene reembolso, se le devuelve el porcentaje que definió el prestador; si no tiene, no se le devuelve nada. | [A] |
| RF-19 | Una reserva no se puede cambiar de fecha u horario. Si el turista quiere otra fecha, cancela y vuelve a reservar. | [A] |

### 3.3 Pagos (integración externa)

El pago no lo hace Uruguay Mnatural, lo hace un servicio externo. Para el curso el cliente
nos dijo que usemos un sandbox que devuelve si la tarjeta es válida o no.

| ID | Requerimiento | Fuente |
|----|---------------|--------|
| RF-20 | El turista paga la reserva como un paso más de la reserva. El pago se hace llamando por API a un servicio de pagos externo (sandbox). | [E] [A] |
| RF-21 | El turista puede cargar una tarjeta nueva o elegir una que ya guardó antes. La tarjeta queda guardada en el sistema para la próxima. | [A] |
| RF-22 | Si el pago sale bien, la reserva queda confirmada, se muestra así en pantalla y se le manda al turista un correo con los datos y el código de la reserva. | [A] |
| RF-23 | Si el pago sale mal, o el servicio de pagos no responde o está caído, la reserva **no** se confirma: queda pendiente de pago por 24 horas (1440 minutos). | [E] [A] |
| RF-24 | Si pasan las 24 horas y la reserva sigue pendiente, se cancela sola y los lugares que tenía bloqueados vuelven a estar libres. | [A] |

### 3.4 Valoraciones y comentarios

| ID | Requerimiento | Fuente |
|----|---------------|--------|
| RF-25 | El turista puede dejar un comentario y un puntaje de 1 a 5 en una actividad, pero solo si participó de ella. La valoración va sobre la actividad (así nos lo marcó también el docente para el MER). | [E] |
| RF-26 | El turista puede editar y borrar sus valoraciones. | [A] |
| RF-27 | El prestador puede responder los comentarios de sus actividades. | [A] |
| RF-28 | Los comentarios se publican directo, sin que el administrador los revise antes (por ahora no hay moderación). | [A] |

### 3.5 Prestadores: emprendimiento

El cliente nos dijo que este es el primer caso de uso: el registro del emprendimiento y su
aprobación.

| ID | Requerimiento | Fuente |
|----|---------------|--------|
| RF-29 | Un prestador puede registrar su emprendimiento como persona física o jurídica, con: CI o RUT, razón social, teléfono, correo, ubicación y una descripción de lo que ofrece. Si es persona jurídica, también el representante. | [E] [A] |
| RF-30 | Cuando se registra, el emprendimiento queda guardado como pendiente hasta que lo revise un administrador. | [A] |
| RF-31 | Mientras el administrador no lo apruebe, el prestador no puede iniciar sesión. | [A] |
| RF-32 | Una persona física puede tener un solo emprendimiento por usuario. Si quiere otro, tiene que usar otro usuario (o cargarlo como otra actividad del mismo emprendimiento). | [A] |
| RF-33 | Cada prestador tiene su propio espacio dentro del sistema para manejar los datos de su emprendimiento. | [E] |

### 3.6 Prestadores: actividades y fechas

En la reunión quedó claro que una cosa es la **actividad** (lo que se ofrece, con su
precio por defecto) y otra es **calendarizarla**: cada fecha concreta tiene su cupo y
puede tener otro precio, por ejemplo más barato entre semana que el fin de semana.

| ID | Requerimiento | Fuente |
|----|---------------|--------|
| RF-34 | El prestador puede crear, ver, modificar y eliminar las actividades de su emprendimiento. | [E] [A] |
| RF-35 | De cada actividad se guarda: nombre, descripción, requisitos, observaciones, ubicación, duración aproximada, precio por defecto, cupo, al menos una etiqueta (por ejemplo trekking), y el equipamiento que se necesita y el que da el prestador. | [E] [A] |
| RF-36 | El prestador define hasta cuántos minutos antes del inicio se puede reservar (0 = hasta la hora de inicio). | [E] |
| RF-37 | Al crear la actividad el prestador marca si se puede cancelar. Si se puede, pone el plazo en minutos para cancelar y si la cancelación tiene reembolso; si tiene, marca qué porcentaje se le devuelve al turista. | [A] |
| RF-38 | El prestador arma la agenda de la actividad: la puede programar en distintas fechas y horarios, cada una con su cupo y su precio. | [E] [A] |
| RF-39 | El prestador puede cancelar o posponer una fecha aunque ya tenga reservas (por ejemplo, si llueve y la actividad no se puede hacer con lluvia). | [A] |
| RF-40 | Si el prestador cancela una fecha o una actividad que tiene reservas pagadas, se le devuelve el 100 % a cada turista y se les avisa que se canceló. | [A] |
| RF-41 | El prestador puede ver las reservas de sus actividades con lo que necesita para organizarse: la fecha, cuántas personas van y los datos de cada participante. | [E] [A] |

### 3.7 Administración

El administrador no maneja las actividades de los prestadores; se encarga de los datos
comunes de toda la plataforma y de controlar a los prestadores.

| ID | Requerimiento | Fuente |
|----|---------------|--------|
| RF-42 | El administrador puede dar de alta, ver, modificar y eliminar los tipos de actividad (etiquetas). | [E] |
| RF-43 | El administrador puede dar de alta, ver, modificar y eliminar los tipos de equipamiento. | [E] |
| RF-44 | El administrador ve una lista de "emprendimientos pendientes" y puede entrar al detalle de cada uno. | [A] |
| RF-45 | El administrador acepta o rechaza cada emprendimiento pendiente. Si lo rechaza, se le manda un correo al prestador avisándole. Si lo acepta, se le manda un correo avisándole y ya puede iniciar sesión con la contraseña que puso al registrarse. | [A] |
| RF-46 | El administrador puede ver el detalle de los prestadores y eliminarlos (por ejemplo, si no cumplen). | [A] |
| RF-47 | El administrador puede eliminar una actividad si le parece inapropiada. Si tenía reservas pagadas, se les devuelve el 100 % a los turistas. | [A] |

### 3.8 API pública

El sistema también tiene que tener su propia API REST para que otras plataformas se
puedan conectar. El enunciado pide estas operaciones:

| ID | Requerimiento | Fuente |
|----|---------------|--------|
| RF-48 | Listar todos los emprendimientos. | [E] |
| RF-49 | Listar todas las actividades de un día en un departamento / localidad. | [E] |
| RF-50 | Dar las 3 actividades con mejor puntaje del mes en cada departamento. | [E] |
| RF-51 | Verificar un emprendimiento, o sea, decir si está aprobado por Uruguay Mnatural. Qué datos devuelve lo decidimos nosotros. | [E] [A] [I: qué devuelve] |
| RF-52 | Buscar actividades por palabra clave. | [E] |

### 3.9 Cuenta de usuario

Estas no las dice el enunciado ni el acta tal cual, pero las agregamos porque cualquier
sistema con cuentas las necesita.

| ID | Requerimiento | Fuente |
|----|---------------|--------|
| RF-53 | Cualquier usuario que se olvidó la contraseña la puede recuperar: el sistema le manda un correo con un enlace para poner una nueva. | [I] |
| RF-54 | El turista y el prestador pueden modificar los datos de su perfil. En el caso del prestador son los datos de su emprendimiento, que el enunciado dice que maneja desde su propio espacio. | [E: prestador] [I: turista] |

## 4. Requerimientos no funcionales

Acá van las condiciones que tiene que cumplir el sistema: con qué está hecho, cómo se
organiza el código, qué tan seguro y confiable tiene que ser, etc. Usamos las mismas
fuentes que en los funcionales: **[E]** enunciado, **[A]** acta e **[I]** interpretación
nuestra (lo que agregamos porque nos parece necesario aunque la letra no lo diga).

### 4.1 Tecnologías

El enunciado (página 4) pone una lista de tecnologías obligatorias que impuso el cliente.

| ID | Requerimiento | Fuente |
|----|---------------|--------|
| RNF-01 | El backend se hace en Java con Spring / Spring Boot y Maven. | [E] |
| RNF-02 | El frontend se hace con React. El enunciado deja elegir entre HTML, CSS, Thymeleaf, JavaScript, React o cualquier otro framework; elegimos React. | [E] [I: React] |
| RNF-03 | La base de datos es SQL. El enunciado deja elegir entre PostgreSQL y MySQL; elegimos PostgreSQL. | [E] [I: PostgreSQL] |
| RNF-04 | La base de datos corre de forma local. El enunciado también permite Google Cloud, AWS o Docker. | [E] [I: local] |
| RNF-05 | El código se versiona con Git. | [E] |

### 4.2 Arquitectura y código

| ID | Requerimiento | Fuente |
|----|---------------|--------|
| RNF-06 | El sistema sigue el patrón MVC (Modelo-Vista-Controlador) en capas. En el backend las capas son: controladores (reciben las peticiones), servicios (las reglas de negocio), repositorios (el acceso a la base) y modelo (las entidades). | [E] [I: capas] |
| RNF-07 | El backend expone una API REST. Cada endpoint tiene que estar documentado: ruta, método, qué recibe y qué devuelve. | [E] |
| RNF-08 | En el código, las variables, clases y métodos se nombran en inglés y con las convenciones de Java (por ejemplo `camelCase` para variables y métodos, `PascalCase` para clases). La documentación del proyecto queda en español. | [E] |
| RNF-09 | Hay tests unitarios que cubren los principales casos de uso. | [E] |

### 4.3 Integraciones y confiabilidad

El enunciado insiste en que un servicio externo puede fallar o no estar disponible y que el
sistema lo tiene que manejar "de manera coherente".

| ID | Requerimiento | Fuente |
|----|---------------|--------|
| RNF-10 | Los pagos se hacen a través de un servicio de pagos externo accedido por API (en el curso, un sandbox). Uruguay Mnatural no procesa los pagos. | [E] [A] |
| RNF-11 | Cuando el turista da a pagar, el sistema espera como máximo 1 minuto la respuesta del servicio de pagos. Si en ese minuto no responde, o si rechaza el pago o devuelve un error, el sistema nunca confirma la reserva: la deja pendiente de pago (y ahí empiezan a correr las 24 horas para pagarla, ver RF-23). | [E] [A] [I: 1 minuto] |
| RNF-12 | Los lugares funcionan como en las reservas de avión, como pidió el cliente: cuando el turista elige cuántos lugares quiere, el sistema se los retiene solo a él durante 10 minutos mientras completa los datos y da a pagar, y para los demás dejan de aparecer como disponibles. Si pasan los 10 minutos sin que dé a pagar, los lugares se liberan; si da a pagar y el pago no sale, la reserva queda pendiente y los lugares siguen retenidos las 24 horas (RF-23). Si dos turistas piden los últimos lugares al mismo tiempo, se los queda el primero y al segundo el sistema le muestra los lugares que quedan de verdad. Así nunca se venden más lugares que el cupo, y el cupo disponible de una fecha nunca puede quedar negativo. | [E] [A] [I: reservas simultáneas, 10 minutos] |
| RNF-13 | El sistema revisa automáticamente, cada cierto tiempo, las reservas pendientes de pago para cancelar las que pasaron las 24 horas. El cliente mencionó hacerlo más adelante con un scheduler cada 10 minutos. | [A] |
| RNF-14 | Si el servicio de clima no responde, el detalle de la actividad se muestra igual, sin el pronóstico. | [E] [I] |

### 4.4 Seguridad

| ID | Requerimiento | Fuente |
|----|---------------|--------|
| RNF-15 | Las contraseñas se guardan encriptadas (con hash), nunca en texto plano. | [I] |
| RNF-16 | Cada usuario solo puede usar las funciones de su rol (turista, prestador o administrador). Un prestador solo ve y modifica sus propias actividades y las reservas de ellas; un turista solo ve sus propias reservas. | [E] [I] |
| RNF-17 | De las tarjetas no se guarda el número completo: se guarda el identificador (token) que devuelve el servicio de pagos y los últimos 4 dígitos para mostrarle al turista cuál es. El acta dice que la tarjeta "se guarda en el sistema"; lo interpretamos así para no guardar datos sensibles. | [A] [I: token] |

### 4.5 Usabilidad

| ID | Requerimiento | Fuente |
|----|---------------|--------|
| RNF-18 | La interfaz está en español. El cliente dijo que varios idiomas (i18n) no son prioridad, así que por ahora queda afuera. | [A] |
| RNF-19 | El diseño, los colores y la usabilidad los elegimos nosotros, pero el cliente los tiene que aceptar antes. | [E] |
| RNF-20 | La página se puede usar bien desde el celular (diseño adaptable). | [I] |

### 4.6 Rendimiento

| ID | Requerimiento | Fuente |
|----|---------------|--------|
| RNF-21 | La búsqueda de actividades y el detalle de una actividad cargan en menos de 3 segundos en condiciones normales. | [I] |

### 4.7 Documentación

| ID | Requerimiento | Fuente |
|----|---------------|--------|
| RNF-22 | Hay manuales de usuario para los tres tipos de usuario: turista (usuario final), prestador y administrador. | [E] |

## 5. Casos de uso

Los casos de uso describen paso a paso cómo cada actor usa el sistema. Cada uno indica qué
requerimientos funcionales cubre. Donde un paso es una interpretación nuestra (no lo dice el
enunciado ni el acta), lo aclaramos en el texto.

### 5.1 Actores

| Actor | Descripción |
|-------|-------------|
| Visitante | Cualquier persona que entra al portal sin iniciar sesión. Puede mirar y buscar actividades. |
| Turista | Visitante que se registró e inició sesión. Puede reservar, pagar, ver sus reservas y valorar actividades. |
| Prestador | Empresa o persona particular que ofrece actividades a través de su emprendimiento. Solo puede entrar cuando un administrador aprobó su emprendimiento. |
| Administrador | Usuario de Uruguay Mnatural. Mantiene los datos comunes (etiquetas, equipamiento), aprueba emprendimientos y controla prestadores y actividades. |
| Servicio de pagos | Sistema externo (sandbox) que recibe los datos de la tarjeta y devuelve si el pago es válido o no. |
| Servicio de clima | API pública externa que devuelve el pronóstico para un lugar y una fecha. |
| Sistema externo | Otra plataforma que consulta la API pública de Uruguay Mnatural. |
| Reloj del sistema | Tarea automática que revisa cada cierto tiempo las reservas pendientes de pago. |

### 5.2 Lista de casos de uso

| ID | Caso de uso | Actores |
|----|-------------|---------|
| CU-01 | Registrar emprendimiento | Prestador |
| CU-02 | Aprobar o rechazar emprendimiento | Administrador |
| CU-03 | Registrarse como turista | Visitante |
| CU-04 | Iniciar sesión | Turista, Prestador, Administrador |
| CU-05 | Recuperar contraseña | Turista, Prestador, Administrador |
| CU-06 | Editar perfil | Turista, Prestador |
| CU-07 | Cerrar sesión | Turista, Prestador, Administrador |
| CU-08 | Gestionar etiquetas | Administrador |
| CU-09 | Gestionar equipamiento | Administrador |
| CU-10 | Gestionar actividades | Prestador |
| CU-11 | Programar fechas de una actividad | Prestador |
| CU-12 | Cancelar o posponer una fecha | Prestador |
| CU-13 | Buscar actividades | Visitante (también Turista) |
| CU-14 | Ver detalle de una actividad | Visitante (también Turista), Servicio de clima |
| CU-15 | Reservar actividad | Turista |
| CU-16 | Pagar reserva | Turista, Servicio de pagos |
| CU-17 | Cancelar reservas pendientes vencidas | Reloj del sistema |
| CU-18 | Consultar mis reservas | Turista |
| CU-19 | Cancelar reserva | Turista |
| CU-20 | Valorar actividad | Turista |
| CU-21 | Responder comentario | Prestador |
| CU-22 | Consultar reservas de mis actividades | Prestador |
| CU-23 | Gestionar prestadores | Administrador |
| CU-24 | Eliminar actividad inapropiada | Administrador |
| CU-25 | Consultar la API pública | Sistema externo |

### 5.3 Descripción de los casos de uso

#### CU-01 — Registrar emprendimiento

- **Actores:** Prestador
- **Descripción:** El prestador da de alta su emprendimiento para poder ofrecer actividades. El cliente nos dijo que este es el primer caso de uso.
- **Precondiciones:**
  - El prestador no tiene un usuario en el sistema.
- **Flujo principal:**
  1. El prestador entra a la opción de registrar emprendimiento.
  2. El sistema le pide si es persona física o jurídica.
  3. El prestador carga CI o RUT, razón social, teléfono, correo, ubicación, una descripción de lo que ofrece y la contraseña con la que va a entrar. Si es persona jurídica, también carga el representante.
  4. El prestador confirma el registro.
  5. El sistema guarda el emprendimiento como pendiente de aprobación y le avisa que un administrador lo va a revisar.
- **Flujos alternativos:**
  - 3a. Falta un dato obligatorio o tiene formato incorrecto: el sistema marca el error y no guarda.
  - 3b. Es persona física y ya existe un emprendimiento con ese usuario: el sistema no deja registrar otro (tiene que usar otro usuario).
- **Postcondiciones:**
  - El emprendimiento queda pendiente y el prestador todavía no puede iniciar sesión.
- **Requerimientos:** RF-29, RF-30, RF-31, RF-32

#### CU-02 — Aprobar o rechazar emprendimiento

- **Actores:** Administrador
- **Descripción:** El administrador revisa los emprendimientos nuevos y decide si se publican.
- **Precondiciones:**
  - El administrador inició sesión.
  - Hay al menos un emprendimiento pendiente.
- **Flujo principal:**
  1. El administrador entra a "Emprendimientos pendientes".
  2. El sistema muestra la lista de emprendimientos pendientes.
  3. El administrador elige uno y ve su detalle.
  4. El administrador lo acepta.
  5. El sistema lo marca como aprobado, habilita al prestador a iniciar sesión con la contraseña que puso y le manda un correo avisándole.
- **Flujos alternativos:**
  - 4a. El administrador lo rechaza: el sistema lo marca como rechazado y le manda un correo al prestador avisándole.
- **Postcondiciones:**
  - El emprendimiento queda aprobado (el prestador ya puede entrar) o rechazado.
- **Requerimientos:** RF-30, RF-31, RF-44, RF-45

#### CU-03 — Registrarse como turista

- **Actores:** Visitante
- **Descripción:** El visitante se crea una cuenta para poder reservar.
- **Precondiciones:**
  - El visitante no tiene cuenta.
- **Flujo principal:**
  1. El visitante entra a la opción de registrarse.
  2. El sistema pide nombre, apellido, país, fecha de nacimiento, idioma de preferencia, documento, correo y contraseña.
  3. El visitante completa los datos y confirma.
  4. El sistema crea la cuenta de turista.
- **Flujos alternativos:**
  - 3a. Falta un dato obligatorio: el sistema marca el error y no crea la cuenta.
  - 3b. Ya existe una cuenta con ese correo: el sistema avisa y no crea la cuenta.
- **Postcondiciones:**
  - El turista tiene cuenta y puede iniciar sesión.
- **Requerimientos:** RF-07

#### CU-04 — Iniciar sesión

- **Actores:** Turista, Prestador, Administrador
- **Descripción:** El usuario entra al sistema con su correo y contraseña.
- **Precondiciones:**
  - El usuario tiene cuenta.
- **Flujo principal:**
  1. El usuario ingresa su correo y contraseña.
  2. El sistema verifica los datos.
  3. El sistema le abre el espacio que le corresponde según su rol (turista, prestador o administrador).
- **Flujos alternativos:**
  - 2a. El correo o la contraseña no son correctos: el sistema muestra un error.
  - 2b. Es un prestador cuyo emprendimiento todavía no fue aprobado (o fue rechazado): el sistema no lo deja entrar y le avisa.
- **Postcondiciones:**
  - El usuario queda con la sesión iniciada.
- **Requerimientos:** RF-08, RF-31

#### CU-05 — Recuperar contraseña

- **Actores:** Turista, Prestador, Administrador
- **Descripción:** El usuario que se olvidó la contraseña la cambia por una nueva usando su correo.
- **Precondiciones:**
  - El usuario tiene cuenta.
- **Flujo principal:**
  1. En la pantalla de inicio de sesión, el usuario elige "Recuperar contraseña".
  2. El sistema pide el correo de la cuenta.
  3. El usuario ingresa el correo.
  4. El sistema le manda un correo con un enlace para cambiar la contraseña.
  5. El usuario entra al enlace e ingresa la contraseña nueva.
  6. El sistema guarda la contraseña nueva.
- **Flujos alternativos:**
  - 3a. No hay una cuenta con ese correo: el sistema avisa y no manda nada.
  - 5a. El enlace ya venció o ya se usó: el sistema avisa y le pide que vuelva a empezar.
- **Postcondiciones:**
  - El usuario puede iniciar sesión con la contraseña nueva.
- **Requerimientos:** RF-53

#### CU-06 — Editar perfil

- **Actores:** Turista, Prestador
- **Descripción:** El usuario cambia sus datos. En el caso del prestador, son los datos de su emprendimiento.
- **Precondiciones:**
  - El usuario inició sesión.
- **Flujo principal:**
  1. El usuario entra a su perfil.
  2. El sistema muestra sus datos e indica cuáles puede modificar.
  3. El usuario hace los cambios y da a guardar.
  4. El sistema valida los datos y actualiza el perfil.
- **Flujos alternativos:**
  - 4a. Algún dato no es válido: el sistema marca el error y pide corregirlo.
- **Postcondiciones:**
  - El perfil queda actualizado.
- **Requerimientos:** RF-33, RF-54

#### CU-07 — Cerrar sesión

- **Actores:** Turista, Prestador, Administrador
- **Descripción:** El usuario sale del sistema.
- **Precondiciones:**
  - El usuario inició sesión.
- **Flujo principal:**
  1. El usuario elige "Cerrar sesión".
  2. El sistema termina la sesión.
  3. El sistema muestra la página pública.
- **Flujos alternativos:** ninguno.
- **Postcondiciones:**
  - Para usar las funciones de su cuenta tiene que volver a iniciar sesión.
- **Requerimientos:** RF-08

#### CU-08 — Gestionar etiquetas

- **Actores:** Administrador
- **Descripción:** El administrador mantiene los tipos de actividad (etiquetas), por ejemplo trekking, cabalgata o enoturismo.
- **Precondiciones:**
  - El administrador inició sesión.
- **Flujo principal:**
  1. El administrador entra a "Etiquetas".
  2. El sistema muestra la lista de etiquetas.
  3. El administrador da de alta una etiqueta nueva con su nombre.
  4. El sistema la guarda y la muestra en la lista.
- **Flujos alternativos:**
  - 3a. El administrador modifica una etiqueta existente: el sistema guarda el cambio.
  - 3b. El administrador elimina una etiqueta: el sistema la borra de la lista.
  - 3c. Ya existe una etiqueta con ese nombre: el sistema avisa y no la guarda.
- **Postcondiciones:**
  - La lista de etiquetas queda actualizada para todos los prestadores.
- **Requerimientos:** RF-42

#### CU-09 — Gestionar equipamiento

- **Actores:** Administrador
- **Descripción:** El administrador mantiene los tipos de equipamiento, por ejemplo casco, chaleco salvavidas o bicicleta.
- **Precondiciones:**
  - El administrador inició sesión.
- **Flujo principal:**
  1. El administrador entra a "Equipamiento".
  2. El sistema muestra la lista de equipamiento.
  3. El administrador da de alta un equipamiento nuevo con su nombre.
  4. El sistema lo guarda y lo muestra en la lista.
- **Flujos alternativos:**
  - 3a. El administrador modifica un equipamiento existente: el sistema guarda el cambio.
  - 3b. El administrador elimina un equipamiento: el sistema lo borra de la lista.
  - 3c. Ya existe un equipamiento con ese nombre: el sistema avisa y no lo guarda.
- **Postcondiciones:**
  - La lista de equipamiento queda actualizada para todos los prestadores.
- **Requerimientos:** RF-43

#### CU-10 — Gestionar actividades

- **Actores:** Prestador
- **Descripción:** El prestador crea, ve, modifica y elimina las actividades de su emprendimiento.
- **Precondiciones:**
  - El prestador inició sesión (su emprendimiento está aprobado).
- **Flujo principal:**
  1. El prestador entra a "Mis actividades" y elige crear una actividad.
  2. El sistema pide nombre, descripción, requisitos, observaciones, ubicación, duración aproximada, precio por defecto, cupo, etiquetas, equipamiento requerido y provisto, y los minutos mínimos antes del inicio para reservar.
  3. El sistema pregunta si la actividad se puede cancelar. Si el prestador dice que sí, pide el plazo en minutos y si tiene reembolso; si tiene, pide el porcentaje.
  4. El prestador completa los datos y confirma.
  5. El sistema guarda la actividad.
- **Flujos alternativos:**
  - 1a. El prestador modifica una actividad existente: el sistema muestra los datos cargados, el prestador los cambia y el sistema guarda.
  - 1b. El prestador elimina una actividad: si tiene reservas pagadas, el sistema les devuelve el 100 % a los turistas y les avisa de la cancelación.
  - 4a. No eligió ninguna etiqueta: el sistema avisa que tiene que tener al menos una y no guarda.
  - 4b. Marcó reembolso y el porcentaje no está entre 0 y 100: el sistema marca el error.
- **Postcondiciones:**
  - La actividad queda guardada y se puede programar en fechas (CU-11).
- **Requerimientos:** RF-34, RF-35, RF-36, RF-37, RF-40

#### CU-11 — Programar fechas de una actividad

- **Actores:** Prestador
- **Descripción:** El prestador arma la agenda: en qué fechas y horarios se hace la actividad, con qué cupo y a qué precio.
- **Precondiciones:**
  - El prestador inició sesión.
  - La actividad existe.
- **Flujo principal:**
  1. El prestador entra a la actividad y elige agregar una fecha.
  2. El sistema propone el cupo y el precio por defecto de la actividad.
  3. El prestador indica fecha y hora de inicio, y si quiere cambia el cupo y el precio (por ejemplo, más barato entre semana).
  4. El sistema guarda la fecha y la muestra en el portal con sus lugares disponibles.
- **Flujos alternativos:**
  - 3a. La fecha ya pasó: el sistema avisa y no la guarda.
- **Postcondiciones:**
  - La fecha queda publicada y se puede reservar.
- **Requerimientos:** RF-04, RF-38

#### CU-12 — Cancelar o posponer una fecha

- **Actores:** Prestador
- **Descripción:** El prestador cancela o cambia una fecha programada, por ejemplo porque llueve y la actividad no se puede hacer con lluvia.
- **Precondiciones:**
  - El prestador inició sesión.
  - La fecha está programada.
- **Flujo principal:**
  1. El prestador entra a la fecha y elige cancelarla.
  2. El sistema avisa cuántas reservas tiene y pide confirmación.
  3. El prestador confirma.
  4. El sistema marca la fecha como cancelada, devuelve el 100 % a cada turista con reserva pagada y les avisa de la cancelación.
- **Flujos alternativos:**
  - 1a. El prestador elige posponerla: indica la nueva fecha y hora, el sistema guarda el cambio y les avisa a los turistas con reserva. (Qué pasa con esas reservas si el turista no puede ir en la nueva fecha es interpretación nuestra: tratarlo como una cancelación del prestador, con 100 % de reembolso.)
- **Postcondiciones:**
  - La fecha queda cancelada o con su nueva fecha, y los turistas avisados.
- **Requerimientos:** RF-39, RF-40

#### CU-13 — Buscar actividades

- **Actores:** Visitante (también Turista)
- **Descripción:** El visitante recorre la oferta y busca actividades con filtros, sin necesidad de registrarse.
- **Precondiciones:**
  - Ninguna.
- **Flujo principal:**
  1. El visitante entra al portal.
  2. El sistema muestra las actividades publicadas.
  3. El visitante filtra por destino (departamento / localidad), tipo de actividad, fecha y/o precio.
  4. El sistema muestra las actividades que cumplen los filtros.
- **Flujos alternativos:**
  - 4a. Ninguna actividad cumple los filtros: el sistema muestra un mensaje de que no hay resultados.
- **Postcondiciones:**
  - El visitante puede entrar al detalle de una actividad (CU-14).
- **Requerimientos:** RF-01, RF-02

#### CU-14 — Ver detalle de una actividad

- **Actores:** Visitante (también Turista), Servicio de clima
- **Descripción:** El visitante ve toda la información de una actividad para decidir si la quiere hacer.
- **Precondiciones:**
  - La actividad está publicada.
- **Flujo principal:**
  1. El visitante elige una actividad.
  2. El sistema muestra el emprendimiento, la ubicación, la descripción, la duración, el precio, los requisitos, las observaciones, las etiquetas y el equipamiento requerido y provisto.
  3. El sistema muestra las próximas fechas y horarios con los lugares disponibles.
  4. El sistema muestra las valoraciones de otros turistas con las respuestas del prestador.
- **Flujos alternativos:**
  - 3a. (Opcional) El sistema consulta al servicio de clima y muestra el pronóstico para cada fecha y lugar. Si el servicio no responde, la página se muestra igual, sin el clima.
  - 3b. La actividad no tiene fechas próximas: el sistema lo indica.
- **Postcondiciones:**
  - Si es turista, puede reservar una fecha (CU-15).
- **Requerimientos:** RF-03, RF-04, RF-05, RF-06

#### CU-15 — Reservar actividad

- **Actores:** Turista
- **Descripción:** El turista reserva lugares en una fecha de una actividad. El cliente lo explicó "estilo página de avión": elegís cuántos lugares, se te guardan, cargás los datos y pagás.
- **Precondiciones:**
  - El turista inició sesión.
  - La fecha elegida tiene lugares disponibles.
- **Flujo principal:**
  1. En el detalle de la actividad, el turista elige una fecha y horario y da a reservar.
  2. El turista indica la cantidad de participantes.
  3. El sistema verifica que haya lugares suficientes y que no se pasó el límite de minutos antes del inicio, y le retiene esos lugares durante 10 minutos (como en las reservas de avión).
  4. El sistema pide nombre, apellido, fecha de nacimiento y cédula de cada participante.
  5. El turista completa los datos y da siguiente.
  6. El sistema muestra el monto a pagar.
  7. Se realiza el pago (CU-16).
- **Flujos alternativos:**
  - 1a. El turista no inició sesión: el sistema le pide que inicie sesión o se registre.
  - 3a. No hay lugares suficientes: el sistema avisa cuántos quedan y no deja seguir.
  - 3b. Faltan menos minutos para el inicio que los que definió el prestador: el sistema no deja reservar.
  - 5a. Pasan los 10 minutos y el turista no llegó a dar a pagar: el sistema libera los lugares y le avisa que tiene que empezar la reserva de nuevo.
  - 6a. La actividad no tiene costo: el sistema confirma la reserva sin pasar por el pago, la muestra confirmada y manda el correo con el código.
- **Postcondiciones:**
  - La reserva queda confirmada (si se pagó o no tenía costo) o pendiente de pago.
- **Requerimientos:** RF-09, RF-10, RF-11, RF-12, RF-13, RF-14, RF-15, RF-16

#### CU-16 — Pagar reserva

- **Actores:** Turista, Servicio de pagos
- **Descripción:** El turista paga la reserva con tarjeta. El pago lo valida un servicio externo (sandbox), no Uruguay Mnatural.
- **Precondiciones:**
  - Hay una reserva pendiente de pago del turista.
- **Flujo principal:**
  1. El sistema muestra las tarjetas guardadas del turista y la opción de agregar una nueva.
  2. El turista elige una tarjeta o carga una nueva, que queda guardada en el sistema.
  3. El turista da a pagar.
  4. El sistema le manda el pago al servicio de pagos.
  5. El servicio de pagos responde que el pago es válido.
  6. El sistema confirma la reserva, la muestra como confirmada en pantalla y le manda al turista un correo con los datos y el código de reserva.
- **Flujos alternativos:**
  - 5a. El servicio responde que el pago no es válido: el sistema no confirma la reserva, que queda pendiente de pago por 24 horas con los lugares bloqueados, y le avisa al turista.
  - 5b. El servicio no responde en 1 minuto o no está disponible: igual que 5a, la reserva no se confirma y queda pendiente.
  - 5c. Mientras la reserva está pendiente, el turista puede volver a intentar el pago desde sus reservas (interpretación nuestra).
- **Postcondiciones:**
  - La reserva queda confirmada o pendiente de pago.
- **Requerimientos:** RF-20, RF-21, RF-22, RF-23

#### CU-17 — Cancelar reservas pendientes vencidas

- **Actores:** Reloj del sistema
- **Descripción:** Las reservas que no se pagaron en 24 horas se cancelan solas para liberar el cupo. También se liberan los lugares retenidos por más de 10 minutos de reservas en las que el turista nunca llegó a dar a pagar.
- **Precondiciones:**
  - Hay reservas pendientes de pago.
- **Flujo principal:**
  1. El sistema revisa cada cierto tiempo las reservas pendientes de pago.
  2. Encuentra una que lleva más de 24 horas (1440 minutos) pendiente.
  3. La cancela y libera los lugares que tenía bloqueados.
- **Flujos alternativos:**
  - 2a. Encuentra lugares retenidos hace más de 10 minutos de una reserva en la que el turista no dio a pagar: los libera.
  - 2b. No hay ninguna vencida: no hace nada hasta la próxima revisión.
- **Postcondiciones:**
  - Los lugares vuelven a estar disponibles para otros turistas.
- **Requerimientos:** RF-11, RF-24

#### CU-18 — Consultar mis reservas

- **Actores:** Turista
- **Descripción:** El turista ve sus reservas y en qué estado está cada una.
- **Precondiciones:**
  - El turista inició sesión.
- **Flujo principal:**
  1. El turista entra a "Mis reservas".
  2. El sistema muestra sus reservas con la actividad, la fecha, los participantes, el código y el estado (pendiente de pago, confirmada o cancelada).
- **Flujos alternativos:**
  - 2a. No tiene reservas: el sistema lo indica.
- **Postcondiciones:**
  - Desde acá puede cancelar una reserva (CU-19), reintentar el pago (CU-16) o valorar una actividad en la que participó (CU-20).
- **Requerimientos:** RF-17

#### CU-19 — Cancelar reserva

- **Actores:** Turista
- **Descripción:** El turista cancela una reserva suya. Para cambiar de fecha también tiene que cancelar y reservar de nuevo.
- **Precondiciones:**
  - El turista inició sesión.
  - La reserva está confirmada o pendiente de pago.
- **Flujo principal:**
  1. El turista elige una reserva en "Mis reservas" y da a cancelar.
  2. El sistema verifica que la actividad sea cancelable y que todavía esté dentro del plazo en minutos que puso el prestador.
  3. El sistema muestra cuánto se le devuelve según la configuración de la actividad y pide confirmación.
  4. El turista confirma.
  5. El sistema cancela la reserva, libera los lugares y, si corresponde, hace el reembolso del porcentaje definido.
- **Flujos alternativos:**
  - 2a. La actividad no es cancelable o ya pasó el plazo: el sistema no deja cancelar.
  - 3a. La actividad no tiene reembolso: el sistema avisa que no se le devuelve nada.
- **Postcondiciones:**
  - La reserva queda cancelada y el cupo liberado.
- **Requerimientos:** RF-18, RF-19

#### CU-20 — Valorar actividad

- **Actores:** Turista
- **Descripción:** El turista deja un comentario y un puntaje sobre una actividad de la que participó. La valoración va sobre la actividad.
- **Precondiciones:**
  - El turista inició sesión.
  - El turista participó de la actividad (tiene una reserva confirmada de una fecha que ya pasó).
- **Flujo principal:**
  1. El turista elige la actividad y da a valorar.
  2. El sistema pide un puntaje de 1 a 5 y un comentario.
  3. El turista completa y confirma.
  4. El sistema publica la valoración en el detalle de la actividad, sin moderación.
- **Flujos alternativos:**
  - 1a. El turista no participó de la actividad: el sistema no le deja valorar.
  - 1b. El turista edita una valoración suya: el sistema guarda el cambio.
  - 1c. El turista borra una valoración suya: el sistema la quita del detalle.
- **Postcondiciones:**
  - La valoración queda pública en la actividad.
- **Requerimientos:** RF-25, RF-26, RF-28

#### CU-21 — Responder comentario

- **Actores:** Prestador
- **Descripción:** El prestador contesta un comentario que dejó un turista en una de sus actividades.
- **Precondiciones:**
  - El prestador inició sesión.
  - La actividad tiene al menos una valoración.
- **Flujo principal:**
  1. El prestador entra a las valoraciones de su actividad.
  2. Elige una y escribe la respuesta.
  3. El sistema guarda la respuesta y la muestra debajo del comentario.
- **Flujos alternativos:** ninguno.
- **Postcondiciones:**
  - La respuesta queda pública junto al comentario.
- **Requerimientos:** RF-27

#### CU-22 — Consultar reservas de mis actividades

- **Actores:** Prestador
- **Descripción:** El prestador ve quiénes reservaron para organizar la actividad.
- **Precondiciones:**
  - El prestador inició sesión.
- **Flujo principal:**
  1. El prestador entra a "Reservas" y elige una actividad y una fecha.
  2. El sistema muestra las reservas de esa fecha con la cantidad de personas, los datos de cada participante y el estado de cada reserva.
- **Flujos alternativos:**
  - 2a. No hay reservas para esa fecha: el sistema lo indica.
- **Postcondiciones:**
  - Ninguna (solo consulta).
- **Requerimientos:** RF-41

#### CU-23 — Gestionar prestadores

- **Actores:** Administrador
- **Descripción:** El administrador controla a los prestadores y puede eliminarlos, por ejemplo si no cumplen.
- **Precondiciones:**
  - El administrador inició sesión.
- **Flujo principal:**
  1. El administrador entra a "Prestadores".
  2. El sistema muestra la lista de prestadores.
  3. El administrador elige uno y ve su detalle.
  4. El administrador lo elimina y confirma.
  5. El sistema elimina al prestador y sus actividades dejan de estar publicadas.
- **Flujos alternativos:**
  - 4a. El prestador tiene reservas pagadas: se les devuelve el 100 % a los turistas y se les avisa (interpretación nuestra, aplicando la regla de que toda cancelación que no es del turista devuelve el 100 %).
- **Postcondiciones:**
  - El prestador ya no puede iniciar sesión ni aparece en el portal.
- **Requerimientos:** RF-46

#### CU-24 — Eliminar actividad inapropiada

- **Actores:** Administrador
- **Descripción:** El administrador saca del portal una actividad que le parece inapropiada.
- **Precondiciones:**
  - El administrador inició sesión.
- **Flujo principal:**
  1. El administrador busca la actividad y ve su detalle.
  2. Elige eliminarla y confirma.
  3. El sistema elimina la actividad del portal.
- **Flujos alternativos:**
  - 3a. La actividad tiene reservas pagadas: el sistema les devuelve el 100 % a los turistas y les avisa de la cancelación.
- **Postcondiciones:**
  - La actividad ya no aparece en el portal.
- **Requerimientos:** RF-47

#### CU-25 — Consultar la API pública

- **Actores:** Sistema externo
- **Descripción:** Otra plataforma consulta información de Uruguay Mnatural mediante la API REST.
- **Precondiciones:**
  - Ninguna.
- **Flujo principal:**
  1. El sistema externo hace una consulta a la API con una de estas operaciones: listar emprendimientos; listar las actividades de un día en un departamento / localidad; top 3 de actividades mejor puntuadas del mes por departamento; verificar un emprendimiento; buscar actividades por palabra clave.
  2. El sistema procesa la consulta.
  3. El sistema devuelve el resultado.
- **Flujos alternativos:**
  - 1a. Faltan parámetros o son incorrectos (por ejemplo, una fecha mal escrita): el sistema devuelve un error.
  - 3a. No hay resultados: el sistema devuelve una lista vacía.
  - 3b. Al verificar, el emprendimiento no existe o no está aprobado: el sistema lo indica en la respuesta.
- **Postcondiciones:**
  - Ninguna (solo consulta).
- **Requerimientos:** RF-48, RF-49, RF-50, RF-51, RF-52

## 6. Reglas de negocio

Las reglas de negocio están en un documento aparte: [Reglas_de_negocio.md](Reglas_de_negocio.md)
(en Word: `Reglas_de_Negocio.docx`). Son 38 (RN-01 a RN-38) y cada una indica en qué
requerimientos y casos de uso de esta ERS se aplica.

---

## Anexo A — Registro de prompts

Cada prompt del equipo, textual y en orden, con la parte del documento que afectó.

| # | Fecha | Prompt (textual) | Secciones afectadas |
|---|-------|------------------|---------------------|
| 1 | 28/09/2026 | mira, tengo que ir haciendo las especificaciones de requerimiento, sabes lo que es? | — (consulta previa) |
| 2 | 28/09/2026 | no tenemos plantilla, hacelo en docs. pero el formato es, escribir los prompts que yo te mando | Estructura del documento y este anexo |
| 3 | 28/09/2026 | pasa que ya te mande bastantes prompts, no me acuerdo los primeros, pero para cada seccion que armaste, hacele el prompt que genero esa parte, por ejemplo: para la parte de usuarios, crea un prompt que yo te hubiera mandado para hacer el mismo y anotalo | Ninguna de la ERS; se creó [Registro_de_prompts.md](Registro_de_prompts.md) con los prompts del resto del proyecto |
| 4 | 18/10/2026 | crea los requerimientos funcionales para el proyecto y agregalos al archivo | 3. Requerimientos funcionales (3.1 a 3.9) |
| 5 | 18/10/2026 | pensa en todo lo que hablamos, en la acta de reunion y escribi como si fuese yo | 3. Requerimientos funcionales (redacción reescrita con el estilo del equipo) y Anexo A.1 |
| 6 | 18/10/2026 | los requerimientos funcionales hacelos en un word junto a los casos de uso. saca la parte que dice las cosas que no sabemos | Se quitó la 3.9; se escribió 5. Casos de uso (CU-01 a CU-22); se generó [ERS_Requerimientos_y_Casos_de_Uso.docx](ERS_Requerimientos_y_Casos_de_Uso.docx) con las secciones 3 y 5 |
| 7 | 18/10/2026 | entonces agrega los que te mande que no estaban | 3.9 Cuenta de usuario (RF-53, RF-54); 5. Casos de uso: nuevos CU-05 Recuperar contraseña, CU-06 Editar perfil y CU-07 Cerrar sesión (los siguientes se renumeraron, ahora CU-01 a CU-25); Word actualizado |
| 8 | 18/10/2026 | si, hacelos en la ers y en el word | 4. Requerimientos no funcionales (4.1 a 4.7, RNF-01 a RNF-22); Word actualizado (ahora: 1 funcionales, 2 no funcionales, 3 casos de uso) |
| 9 | 18/10/2026 | en la 11 ponele 1 min de cuanto dura | RNF-11 (espera máxima de 1 minuto la respuesta del servicio de pagos) y CU-16, flujo alternativo 5b; Word actualizado |
| 10 | 18/10/2026 | en la 12 que funcione como las reservas de avion | RNF-12 (retención de lugares estilo reserva de avión); Word actualizado |
| 11 | 18/10/2026 | si, ponele 10 minutos | RF-11 y RNF-12 (lugares retenidos 10 minutos hasta dar a pagar); CU-15 Reservar actividad (flujo 3 y alternativo 5a) y CU-17 (también libera esas retenciones); Word actualizado |
| 12 | 18/10/2026 | si, hacelas en la ers y en el word<br>para, hacelas pero en otro documento | 6. Reglas de negocio: se escribieron en un documento aparte ([Reglas_de_negocio.md](Reglas_de_negocio.md) y `Reglas_de_Negocio.docx`, RN-01 a RN-38); acá queda solo la referencia |

### A.1 Prompts reconstruidos de la sección 3

La sección 3 se generó con un único prompt general (#4). Para que quede claro qué se pidió
en cada parte, abajo está el prompt que el equipo habría mandado para cada subsección.
**Estos prompts se escribieron después (18/10/2026) a partir del acta y de lo conversado;
no fueron enviados así.**

| Sección | Prompt reconstruido |
|---------|---------------------|
| 3.1 Portal público | poné los requerimientos del portal para el visitante que no está registrado: que pueda ver las actividades, buscarlas por destino, tipo, fecha y precio, entrar al detalle con todo (emprendimiento, ubicación, duración, precio, requisitos, equipamiento que lleva y que le dan), ver las próximas fechas con los lugares libres y las valoraciones con la respuesta del prestador. el clima ponelo como opcional porque el enunciado dice "sería conveniente" |
| 3.2 Turistas | ahora los del turista. el registro es con nombre, apellido, pais, fecha de nacimiento, idioma y documento como dijo el cliente, agregale mail y contraseña para el login. para reservar tiene que estar logueado, elige fecha y cuantas personas (el maximo es el cupo, no hay limite de reservas por usuario), los lugares se bloquean como en una pagina de avion, se piden los datos de cada participante y se muestra el costo. tambien que respete los minutos minimos que pone el emprendimiento, que pueda ver sus reservas y cancelarlas solo si la actividad es cancelable, con el reembolso que se haya puesto. cambiar de fecha no, tiene que cancelar y reservar de nuevo |
| 3.3 Pagos | los pagos van con el sandbox. el turista carga la tarjeta o elige una guardada y se guarda en el sistema. si el pago da ok se confirma la reserva, aparece en pantalla y se manda un mail con los datos y el codigo de reserva. si da mal o el servicio no responde no se confirma, queda pendiente 24 horas y despues se cancela y se liberan los lugares |
| 3.4 Valoraciones | valoraciones: comentario y puntaje de 1 a 5, solo si participo, y va sobre la actividad como dijo el profe. el turista la puede editar y borrar, el prestador puede responder y no hay moderacion por ahora |
| 3.5 Emprendimiento | el registro del emprendimiento, que es el primer caso de uso que dijo el cliente: persona fisica o juridica, ci o rut, razon social, telefono, mail, ubicacion, descripcion y el representante si es juridica. queda pendiente y no se puede loguear hasta que el admin lo apruebe. persona fisica un solo emprendimiento por usuario |
| 3.6 Actividades y fechas | las actividades del prestador: ABM, con nombre, descripcion, requisitos, observaciones, ubicacion, duracion, precio por defecto, cupo, minimo una etiqueta y el equipamiento. los minutos minimos para reservar y lo de la cancelacion (si se puede, en cuantos minutos, si tiene reembolso y que porcentaje). separa la actividad de la agenda, cada fecha con su cupo y su precio (entre semana puede ser mas barato). puede cancelar o posponer fechas aunque tengan reservas, por ejemplo si llueve, y ahi se devuelve el 100% y se avisa. tambien que vea las reservas de sus actividades con los datos de los participantes |
| 3.7 Administración | el admin: ABM de etiquetas y de equipamiento, ver los emprendimientos pendientes con el detalle y aceptar o rechazar mandando mail en los dos casos. tambien que vea el detalle de los prestadores y los pueda eliminar, y que borre actividades inapropiadas devolviendo el 100% si tenian reservas |
| 3.8 API pública | la api publica con las 5 operaciones que pide el enunciado: listar emprendimientos, actividades de un dia por departamento/localidad, top 3 del mes por departamento, verificar emprendimiento y buscar por palabra clave |
| 3.9 Puntos sin definir | anota las preguntas del acta que quedaron sin respuesta para preguntarle al cliente, y lo que quedo afuera por ahora (moderacion, idiomas, coordenadas) |

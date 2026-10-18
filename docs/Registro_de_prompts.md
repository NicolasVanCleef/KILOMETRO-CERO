# Registro de prompts — Uruguay Mnatural

Registro del uso de IA (Claude Code) en el proyecto: qué se pidió y qué generó cada pedido.

> Desde el 05/10/2026 el registro principal está en Claude Docs:
> https://claude.ai/code/artifact/66e4060a-fb0a-48a3-979a-45143267dfff. Este archivo se
> mantiene como copia dentro del repositorio.

Hay dos tipos de prompt, siempre identificados:

- **Prompt real:** el mensaje que el equipo mandó, **textual** (con sus errores de tipeo),
  tomado del historial de las sesiones.
- **Prompt reconstruido:** escrito después, el 28/09/2026. **No se mandó así.** Describe con
  precisión el pedido que habría generado esa parte. Se escribió porque muchos prompts reales
  eran muy generales (por ejemplo, "mira los archivos que estan en la carpeta y hace") y
  generaron varias partes a la vez.

Fuentes que se citan: **[E]** = enunciado (`Proyecto TIC 2026.pdf`), **[A]** = acta de
reunión con el cliente (14/09/2026).

Los prompts del documento de requerimientos se registran aparte, en el Anexo A de
[ERS.md](ERS.md).

---

## Parte 1 — Cronología de prompts reales

Solo se listan los mensajes que generaron o cambiaron algo. Se omiten los saludos y los
cierres de sesión.

### Sesión 1 — 14 y 15/09/2026

| # | Fecha | Prompt real (textual) | Qué generó |
|---|-------|-----------------------|------------|
| R1 | 14/09 | mira los archivos que estan en la carpeta y hace | Primera versión del MER (`docs/MER.md`, en inglés) y del backend Spring Boot: proyecto Maven, capas, las 11 entidades JPA y sus repositorios |
| R2 | 14/09 | como lo corro | Instrucciones para correr el backend (en ese momento con una base H2 de prueba) |
| R3 | 14/09 | en donde lo corro, quiero correr una demo ya | Demo del backend con etiquetas de ejemplo (luego eliminada en R4) |
| R4 | 14/09 | bien, borra todas las demos y deja el programa como pide la cosigna. en el pdf que te pase cuales son bien los requierimiento que pide? y que es lo que esta hecho hasta ahora? | Se eliminaron la demo y la base H2; resumen de los requerimientos del enunciado y del avance |
| R5 | 14/09 | pero el mer tiene que ser un dibujo | Diagrama del MER como imagen (`MER.png`, `MER.svg`) |
| R6 | 14/09 | hacelo en espanol | MER traducido al español y tabla de equivalencias con los nombres en inglés del código |
| R7 | 14/09 | para ir armando el proyecto tuviste en cuenta el documento de la entrevista con el cliente | Revisión del MER contra el acta: trazabilidad regla por regla y agregados (departamento/localidad obligatorios, coordenadas opcionales) |
| R8 | 14/09 | bien, vamos a arrancar a crear el frontend, que app recomendas | Recomendación de React + Vite |
| R9 | 14/09 | dale | Frontend React: estructura, cliente HTTP, página de inicio y ABM de etiquetas |
| R10 | 14/09 | sí, instalá postgres | Instalación de PostgreSQL y conexión del backend |
| R11 | 15/09 | hace el portal de actividades | API de emprendimientos (registro y aprobación), de actividades (búsqueda, detalle, alta) y de fechas; páginas de listado y detalle de actividades; datos de ejemplo |
| R12 | 15/09 | lo puedo operar yo? | Instrucciones para levantar backend y frontend |

### Sesión 2 — 28/09/2026

| # | Fecha | Prompt real (textual) | Qué generó |
|---|-------|-----------------------|------------|
| R13 | 28/09 | armame un mer de esto *(con una imagen de ejemplo de notación Chen adjunta)* | MER en notación Chen (`MER_Chen.png`, `MER_Chen.svg`) |

### Sesión 3 — 28/09/2026

| # | Fecha | Prompt real (textual) | Qué generó |
|---|-------|-----------------------|------------|
| R14 | 28/09 | get access to the proyecto tic folder | Acceso a la carpeta del proyecto |
| R15 | 28/09 | the MER is corrrect? | Revisión del MER contra [E] y [A]: lista de faltantes y errores |
| R16 | 28/09 | yes, fix it, but allways check with the letter of the proyect | Correcciones del MER (duración, ubicación del emprendimiento, valoración ligada a la reserva, reembolsos, al menos una etiqueta, `PARTICIPANTE`, `TARJETA`, pagos 1:N); entidades Java actualizadas; ambos diagramas regenerados |
| R17 | 28/09 | does the frontend run? | Revisión del frontend |
| R18 | 28/09 | it doesnt work | Diagnóstico (el backend no estaba corriendo) y arranque de backend y frontend |
| R19 | 28/09 | keep working on it | Duración visible en el portal; página de registro de emprendimiento; página de emprendimientos pendientes (aceptar/rechazar) con validaciones |
| R20 | 28/09 | mira, tengo que ir haciendo las especificaciones de requerimiento, sabes lo que es? | Explicación de la ERS (ver Anexo A de [ERS.md](ERS.md)) |
| R21 | 28/09 | no tenemos plantilla, hacelo en docs. pero el formato es, escribir los prompts que yo te mando | Esqueleto de `docs/ERS.md` con el registro de prompts |
| R22 | 28/09 | pasa que ya te mande bastantes prompts, no me acuerdo los primeros, pero para cada seccion que armaste, hacele el prompt que genero esa parte, por ejemplo: para la parte de usuarios, crea un prompt que yo te hubiera mandado para hacer el mismo y anotalo | Este documento |

### Sesión 4 — 29/09/2026 (nuevo comienzo del código)

A partir de acá la app se rehace de cero, sección por sección y con prompts específicos. El
código anterior quedó archivado en `_referencia/`; la documentación de `docs/` se mantiene.

| # | Fecha | Prompt real (textual) | Qué generó |
|---|-------|-----------------------|------------|
| R23 | 29/09 | necesito ir haciendo la app de a poco, sabiendo mas o menos que es lo que estas haciendo vos de codigo, por lo tanto lo tengo que ir haciendo por secciones. Pero ya esta bastante hecho, y mis prompts no eran especificas, eran solo que continues con la pag. Me gustaria arrancar de cero, haciendo parte por parte y viendo como lo vas haciendo, vos que opinas | Propuesta de trabajo por secciones y orden de las 10 secciones |
| R24 | 29/09 | bien, me parce genial. Archiva el codigo acutal si, y arranquemos de 0 | Código anterior (`backend/`, `frontend/`) movido a `_referencia/` |
| R25 | 29/09 | borra la base vieja | Respaldo de la base vieja en `_referencia/uruguay_mnatural_backup.sql` y borrado de la base `uruguay_mnatural` |

### Sesión 5 — 05/10/2026

| # | Fecha | Prompt real (textual) | Qué generó |
|---|-------|-----------------------|------------|
| R26 | 05/10 | yo quiero que me divivdes la app en partes para ir haciendo por partes y cada vez que yo te digo una prompt para hacer esa parte en especifico la anotes en un documento para ir registrando las prompts. | Parte 3 de este documento: división de la app en 10 partes, con una tabla por parte para registrar sus prompts |
| R27 | 05/10 | fue una correcion *(sobre la nota del profesor "valoracion mer va sobre actividad", de una clase anterior)* | MER corregido: `VALORACION` se relaciona con `ACTIVIDAD` y `TURISTA` (ya no con `RESERVA`); haber participado pasa a ser regla de negocio; diagramas `MER.png/svg` y `MER_Chen.png/svg` regenerados |
| R28 | 05/10 | el registro de prompts lo podes hacer en un docs asi lo voy viendo | Copia de este registro en Claude Docs (https://claude.ai/code/artifact/66e4060a-fb0a-48a3-979a-45143267dfff), que pasa a ser el registro principal |
| R29 | 18/10 | https://claude.ai/artifact/FiLQ3AeY1VhwygNpKU6Wgo   quiero que me hagas algo de este estilo, pero OBVIAMENTE aplicado para nuestro proyecto. Fijate las funcionalidades que tiene | Muestra navegable propia de Kilómetro Cero (visitante, turista, prestador, administrador y API pública, con datos de ejemplo y acciones que funcionan en el navegador), publicada en https://claude.ai/artifact/1vD3zqDnqTbkSUSw7vcZEX. Se tomó solo la idea de la muestra de otro equipo; diseño, contenido y código propios. |

---

## Parte 2 — Prompt por sección

Para cada parte del proyecto: qué prompts reales la generaron y el prompt reconstruido que
la habría generado por sí solo.

### 2.1 MER — `docs/MER.md` y diagramas

#### Estructura general y diagrama (notación pata de gallo)
- **Prompts reales:** R1, R5, R6, R7, R16.
- **Prompt reconstruido:**
  > Leé el enunciado (`Proyecto TIC 2026.pdf`) y el acta de reunión con el cliente, y armá el
  > MER de la base de datos en `docs/MER.md`, en español. Tiene que tener el diagrama como
  > imagen (PNG y SVG) en notación pata de gallo, con tipos, PK, FK y claves únicas, y abajo la
  > justificación de cada entidad citando de dónde sale en el enunciado o en el acta. Todo lo
  > que agregues tiene que estar respaldado por la letra del proyecto.

#### USUARIO
- **Prompts reales:** R1, R6.
- **Prompt reconstruido:**
  > En el MER, creá una entidad USUARIO para el login, compartida por los tres tipos de usuario
  > (turista, prestador y administrador): email único, contraseña, rol, un campo "habilitado"
  > y fecha de creación. El habilitado es porque, según el acta, un prestador no puede
  > loguearse hasta que el admin apruebe su emprendimiento. Los datos propios de cada tipo van
  > en otras tablas, para no repetir email y contraseña.

#### TURISTA
- **Prompts reales:** R1, R6, R7.
- **Prompt reconstruido:**
  > Agregá la entidad TURISTA, vinculada 1 a 1 con USUARIO, con los datos que el acta dice que
  > se piden al registrarse: nombre, apellido, país, fecha de nacimiento, idioma de preferencia
  > y documento.

#### EMPRENDIMIENTO
- **Prompts reales:** R1, R6, R7, R16.
- **Prompt reconstruido:**
  > Agregá la entidad EMPRENDIMIENTO, 1 a 1 con USUARIO, con los datos del primer caso de uso
  > del acta: persona física o jurídica, CI o RUT, razón social, teléfono, correo y
  > representante (solo si es jurídica). Sumale la ubicación (departamento, localidad y
  > dirección, en texto libre porque el cliente no quiere catálogo) y una descripción de los
  > servicios, que pide el enunciado. Poné un estado PENDIENTE / APROBADO / RECHAZADO para el
  > flujo en que el admin acepta o deniega los emprendimientos pendientes. Aclará que una
  > persona física puede tener un solo emprendimiento por usuario.

#### ACTIVIDAD
- **Prompts reales:** R1, R6, R7, R16.
- **Prompt reconstruido:**
  > Agregá la entidad ACTIVIDAD, que pertenece a un EMPRENDIMIENTO. Tiene que tener todo lo que
  > el enunciado pide publicar: nombre, descripción, requisitos (incluida la edad),
  > observaciones, ubicación (departamento y localidad obligatorios, para la API pública que
  > lista actividades por departamento; dirección y coordenadas opcionales) y duración
  > aproximada en minutos. Agregá precio y cupo por defecto (el precio puede ser 0 si no tiene
  > costo), mínimo y máximo de minutos antes del inicio para reservar, y la configuración de
  > cancelación del acta: si se puede cancelar, hasta cuántos minutos antes, si tiene
  > reembolso y qué porcentaje.

#### ETIQUETA y ACTIVIDAD_ETIQUETA
- **Prompts reales:** R1, R6, R16.
- **Prompt reconstruido:**
  > Agregá ETIQUETA como catálogo que administra el admin (tipos de actividad: trekking,
  > gastronomía, etc.), con nombre único. Relacionala N:M con ACTIVIDAD mediante la tabla
  > ACTIVIDAD_ETIQUETA, y que cada actividad tenga al menos una etiqueta, como dijo el cliente
  > ("por lo menos 1 (trekking)").

#### EQUIPAMIENTO y ACTIVIDAD_EQUIPAMIENTO
- **Prompts reales:** R1, R6, R16.
- **Prompt reconstruido:**
  > Agregá EQUIPAMIENTO como catálogo del admin (casco, chaleco salvavidas, etc.) y una entidad
  > ACTIVIDAD_EQUIPAMIENTO que relacione N:M actividad y equipamiento con un tipo de relación:
  > REQUERIDO (lo trae el turista) o PROVISTO (lo da el prestador), como distingue el
  > enunciado. Que no se pueda repetir el mismo equipamiento en una actividad.

#### INSTANCIA_ACTIVIDAD
- **Prompts reales:** R1, R6, R7, R16.
- **Prompt reconstruido:**
  > Agregá INSTANCIA_ACTIVIDAD para las fechas concretas de una actividad (el acta dice que
  > calendarizar es distinto de la actividad): fecha, hora de inicio, cupo total, cupo
  > disponible y precio propio, que puede pisar el precio por defecto (entre semana puede
  > salir más barato que el fin de semana). Poné un estado PROGRAMADA / CANCELADA para que el
  > prestador cancele una fecha (por ejemplo, por lluvia) sin borrarla. Posponer es cancelar y
  > crear otra fecha, porque según el acta los cambios se hacen solo con cancelación y
  > reembolso del 100%.

#### RESERVA
- **Prompts reales:** R1, R6, R7.
- **Prompt reconstruido:**
  > Agregá RESERVA, hecha por un TURISTA sobre una INSTANCIA_ACTIVIDAD, con cantidad de
  > participantes, código de reserva único (el que se manda por correo) y estados
  > PENDIENTE_PAGO, CONFIRMADA, CANCELADA y EXPIRADA. Que tenga un límite de pago, porque según
  > el acta, si el pago no es exitoso, la reserva queda pendiente 24 horas y después se cancela.

#### PARTICIPANTE
- **Prompts reales:** R16.
- **Prompt reconstruido:**
  > En el flujo de reserva del acta, después de elegir la cantidad de cupos se piden nombre,
  > apellido, fecha de nacimiento y CI. Agregá una entidad PARTICIPANTE, con una fila por cada
  > persona de la reserva, con esos datos. Marcalo como interpretación a confirmar con el
  > cliente.

#### TARJETA
- **Prompts reales:** R16.
- **Prompt reconstruido:**
  > El acta dice que el turista agrega o selecciona una tarjeta y queda guardada. Agregá una
  > entidad TARJETA del turista, pero sin guardar el número completo: solo el token que
  > devuelve la pasarela de pagos y los últimos 4 dígitos.

#### PAGO
- **Prompts reales:** R1, R6, R16.
- **Prompt reconstruido:**
  > Agregá PAGO, ligado a la RESERVA y opcionalmente a la TARJETA usada, con monto, estado
  > (PENDIENTE, APROBADO, RECHAZADO, REEMBOLSADO) y la referencia que devuelve el servicio
  > externo de pagos (sandbox). Que una reserva pueda tener varios pagos, porque si uno falla
  > se puede reintentar dentro de las 24 horas, y que solo un pago aprobado confirme la
  > reserva. Agregá monto y fecha de reembolso para las cancelaciones: el porcentaje de la
  > actividad si cancela el turista, y el 100% si cancela el prestador o el admin.

#### VALORACION
- **Prompts reales:** R1, R6, R16.
- **Prompt reconstruido:**
  > Agregá VALORACION con puntuación de 1 a 5, comentario, respuesta del prestador y fecha.
  > Colgala de la RESERVA y que sea única por reserva, porque el enunciado dice que se puede
  > valorar una vez que se participó de la actividad. El turista puede editarla o borrarla y
  > no hay moderación, según el acta.
- **Corrección posterior (R27, 05/10):** por corrección del profesor, la valoración pasó a
  relacionarse con `ACTIVIDAD` y `TURISTA`. Haber participado quedó como regla de negocio.

#### Notas fuera del MER (reglas de negocio)
- **Prompts reales:** R7, R16.
- **Prompt reconstruido:**
  > Al final del MER, listá las reglas de negocio del acta que no se modelan en el esquema:
  > reembolsos al cancelar, qué pasa si el turista no se presenta, que no hay límite de
  > reservas por turista, que el admin puede eliminar actividades, y cómo sale del esquema cada
  > operación de la API pública que pide el enunciado.

#### Tabla de equivalencias español ↔ inglés
- **Prompts reales:** R6.
- **Prompt reconstruido:**
  > Como el enunciado pide nombres en inglés en el código, agregá al final del MER una tabla
  > que relacione cada entidad en español con su clase Java en inglés.

#### Diagrama en notación Chen
- **Prompts reales:** R13, R16.
- **Prompt reconstruido:**
  > Hacé también el MER en notación Chen, como la imagen que te paso: rectángulos para las
  > entidades, óvalos para los atributos (clave primaria subrayada), rombos para las
  > relaciones y cardinalidades 1 / N / M. Sin claves foráneas, porque las reemplazan las
  > relaciones. Guardalo en PNG y SVG en `docs/`.

### 2.2 Backend — `backend/`

#### Proyecto base y arquitectura en capas
- **Prompts reales:** R1, R4, R10.
- **Prompt reconstruido:**
  > Creá el backend con Spring Boot y Maven, como pide el enunciado, conectado a PostgreSQL.
  > Usá el patrón MVC en capas: modelo, repositorios, servicios, controladores REST y DTOs, con
  > nombres en inglés. No dejes datos de demo: tiene que quedar como pide la consigna.

#### Entidades JPA
- **Prompts reales:** R1, R16.
- **Prompt reconstruido:**
  > Implementá cada entidad del MER como clase JPA en inglés (User, Tourist, Business,
  > Activity, etc.), con sus relaciones, claves únicas y enums de estados, y un repositorio por
  > entidad. Si cambia el MER, actualizá las clases para que coincidan.

#### API de etiquetas
- **Prompts reales:** R1, R3.
- **Prompt reconstruido:**
  > Hacé la API REST del catálogo de etiquetas, con alta, consulta, modificación y eliminación,
  > que según el enunciado mantiene el administrador.

#### API de emprendimientos (registro y aprobación)
- **Prompts reales:** R11, R19.
- **Prompt reconstruido:**
  > Hacé la API del primer caso de uso del acta: registrar un emprendimiento (queda pendiente y
  > su usuario no puede entrar), listar los pendientes, y aprobar o rechazar (al aprobar se
  > habilita el usuario). Validá que no se repita el email y que una persona jurídica tenga
  > representante. Esto cubre también la operación "verificar emprendimiento" de la API
  > pública.

#### API de actividades y fechas
- **Prompts reales:** R11, R19.
- **Prompt reconstruido:**
  > Hacé la API de actividades: búsqueda pública por palabra clave, departamento y etiqueta;
  > detalle con las próximas fechas disponibles; alta de actividad por el prestador (con al
  > menos una etiqueta y la duración); y alta y consulta de fechas concretas de cada actividad.

### 2.3 Frontend — `frontend/`

#### Proyecto base y ABM de etiquetas
- **Prompts reales:** R8, R9.
- **Prompt reconstruido:**
  > Creá el frontend con React y Vite, separado del backend, que consuma la API REST. Armá una
  > página de inicio con navegación y una página de administración de etiquetas para dar de
  > alta, editar y eliminar.

#### Portal de actividades
- **Prompts reales:** R11, R19.
- **Prompt reconstruido:**
  > Hacé el portal de actividades para turistas, sin necesidad de registrarse: un listado con
  > buscador por nombre y departamento, y un detalle con el emprendimiento, la ubicación, la
  > descripción, la duración aproximada, el precio, los requisitos, las condiciones de
  > cancelación y las próximas fechas con sus cupos.

#### Registro de emprendimiento y emprendimientos pendientes
- **Prompts reales:** R19.
- **Prompt reconstruido:**
  > Hacé la pantalla de registro de emprendimiento con los datos del acta: tipo de persona, CI
  > o RUT, razón social, teléfono, correo y representante solo si es jurídica, más la
  > ubicación y la descripción que pide el enunciado. Al enviar, que avise que queda pendiente
  > de aprobación. Hacé también la pantalla del admin con los emprendimientos pendientes y su
  > detalle, con botones para aceptar o rechazar.

### 2.4 Documento de requerimientos — `docs/ERS.md`

- **Prompts reales:** R20, R21. Los siguientes se registran en el Anexo A de [ERS.md](ERS.md).
- No lleva prompt reconstruido: se construye solo con los prompts reales que se vayan
  mandando.

---

## Parte 3 — Reconstrucción por partes (desde el 29/09/2026)

La app se rehace de cero en 10 partes, en este orden. Cada parte se hace solo cuando el
equipo manda un prompt específico para ella. Cada prompt se anota acá **textual**, en la
tabla de su parte, numerado por parte (P1.1, P1.2, …, P2.1, …).

| Parte | Qué incluye | Estado |
|-------|-------------|--------|
| 1 | Base: proyecto Spring Boot (backend), proyecto React (frontend), repositorio git y base PostgreSQL | Terminada |
| 2 | Catálogo de etiquetas y equipamiento: ABM del administrador, del backend al frontend | Pendiente |
| 3 | Registro de emprendimientos y aprobación por el administrador | En curso |
| 4 | Usuarios, inicio de sesión y roles | Pendiente |
| 5 | Actividades y sus fechas y horarios | Pendiente |
| 6 | Portal público: búsqueda y detalle de actividades | Pendiente |
| 7 | Reservas con control de cupo | Pendiente |
| 8 | Pagos en sandbox y manejo de fallos | Pendiente |
| 9 | Valoraciones y respuesta del prestador | Pendiente |
| 10 | API pública y clima | Pendiente |

El alcance exacto de cada parte se define con su prompt y se controla contra [E] y [A].

### Parte 1 — Base

| # | Fecha | Prompt (textual) | Qué generó |
|---|-------|------------------|------------|
| P1.1 | 18/10/2026 | ahora vamos a pasar al codigo y a arrancar a crear la pagina | Plan de la Parte 1 y revisión de lo instalado (Java 25, Node, Git, PostgreSQL 17 y 18). Sin código todavía. |
| P1.2 | 18/10/2026 | okay perfecto, vamos a hacerlo con java 25, no quiero escribir codigo, quiero que vos lo hagas y que luego me lo expliques. ya tengo un repositorio git creado pero despues todos los otros pasos estan perfectos, vamo a darle | Base de datos uruguay_mnatural, backend Spring Boot (Java 25) con /api/health, frontend React (Vite) que muestra la conexión. Primer commit subido a GitHub (NicolasVanCleef/KILOMETRO-CERO, rama main). |

### Parte 2 — Catálogo de etiquetas y equipamiento

| # | Fecha | Prompt (textual) | Qué generó |
|---|-------|------------------|------------|
| P2.1 | 18/10/2026 | dale, arranquemos con la parte 2 | Plan de la Parte 2 (entidades, endpoints, pantallas). Sin código: el equipo priorizó la Parte 3 para la defensa (P3.1). |

### Parte 3 — Registro y aprobación de emprendimientos

| # | Fecha | Prompt (textual) | Qué generó |
|---|-------|------------------|------------|
| P3.1 | 18/10/2026 | preciso que hagas esto, presentar algo al cliente<br>registro del proveedor<br> y administrador de aceptar o denegar. esto es lo que tengo que tener si o si para la defensa de manana | Registro de emprendimiento (persona física o jurídica) y pantalla del administrador para aceptar o rechazar, de punta a punta. |
| P3.2 | 18/10/2026 | antes de entrar a la pagina, da la opcion de entrar como los diferentes usaurios, tipo visitante, admin, proveedor, turista | Pantalla de entrada para elegir el usuario (visitante, turista, prestador, administrador), con menú y accesos según el rol. Es una simulación hasta el login real de la Parte 4. |

### Parte 4 — Usuarios, inicio de sesión y roles

| # | Fecha | Prompt (textual) | Qué generó |
|---|-------|------------------|------------|

### Parte 5 — Actividades y fechas

| # | Fecha | Prompt (textual) | Qué generó |
|---|-------|------------------|------------|
| P5.1 | 18/10/2026 | agrega que el cliente pueda hacer algo y poder agregar una actividad | Nada en la app real: se interrumpió antes de escribir código y se reemplazó por la muestra navegable (R29). |

### Parte 6 — Portal público

| # | Fecha | Prompt (textual) | Qué generó |
|---|-------|------------------|------------|

### Parte 7 — Reservas con cupo

| # | Fecha | Prompt (textual) | Qué generó |
|---|-------|------------------|------------|

### Parte 8 — Pagos en sandbox

| # | Fecha | Prompt (textual) | Qué generó |
|---|-------|------------------|------------|

### Parte 9 — Valoraciones y respuesta del prestador

| # | Fecha | Prompt (textual) | Qué generó |
|---|-------|------------------|------------|

### Parte 10 — API pública y clima

| # | Fecha | Prompt (textual) | Qué generó |
|---|-------|------------------|------------|

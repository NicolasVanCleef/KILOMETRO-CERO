# Manual de usuario — Administrador

**Uruguay Mnatural · por Kilómetro Cero** · Proyecto TIC 2026

Este manual es para los **administradores** de Uruguay Mnatural. El administrador controla
quién puede ofrecer actividades en la plataforma, mantiene las listas de etiquetas y
equipamiento y elimina el contenido inapropiado.

---

## 1. Qué podés hacer

Cuando iniciás sesión como administrador, en el menú de arriba aparecen tres secciones:

| Sección | Para qué |
|---|---|
| **Emprendimientos** | Aceptar o rechazar los emprendimientos nuevos y eliminar prestadores |
| **Moderación** | Eliminar actividades publicadas que sean inapropiadas |
| **Catálogo** | Mantener las etiquetas (tipos de actividad) y el equipamiento |

El administrador **no** carga ni modifica las actividades de los prestadores, y **no** modera
las valoraciones: los turistas las publican directamente.

---

## 2. Iniciar sesión

1. Tocá **Iniciar sesión**.
2. Escribí el correo y la contraseña del administrador.
3. Tocá **Entrar**.

La cuenta del administrador se crea sola al arrancar el sistema por primera vez:

| Correo | Contraseña inicial |
|---|---|
| admin@uruguaymnatural.uy | admin1234 |

> Cambiá la contraseña inicial antes de usar el sistema en serio. Se hace con **Me olvidé la
> contraseña** en la pantalla de inicio de sesión: escribí el correo, abrí el enlace que llega
> (vale por 30 minutos) y elegí la contraseña nueva. En esta versión los correos son simulados:
> el enlace aparece en la consola del servidor.

Para salir, tocá **Cerrar sesión**, arriba a la derecha.

---

## 3. Emprendimientos

### 3.1 La lista

Arriba hay cuatro pestañas, una por estado:

| Pestaña | Qué hay |
|---|---|
| **Pendientes** | Emprendimientos que se registraron y esperan tu revisión. Se abre esta por defecto. |
| **Aprobados** | Los prestadores activos. Pueden iniciar sesión y publicar actividades. |
| **Rechazados** | Solicitudes que rechazaste. No pueden iniciar sesión. |
| **Eliminados** | Prestadores que diste de baja. No pueden iniciar sesión. |

La tabla muestra la razón social, el tipo (persona física o jurídica), el departamento y la fecha
de registro. **Tocá una fila** para ver el detalle a la derecha: CI o RUT, representante,
teléfono, correo, ubicación, fecha de registro, fecha de revisión y la descripción de los
servicios.

### 3.2 Aceptar o rechazar un emprendimiento nuevo

1. En **Pendientes**, tocá el emprendimiento.
2. Revisá los datos: que la CI o el RUT, el contacto y la descripción tengan sentido.
3. Tocá **Aceptar** o **Rechazar**.
4. Confirmá con **Sí, aceptar** o **Sí, rechazar** (o **Cancelar** para volver).

- **Al aceptar:** el prestador ya puede iniciar sesión y se le manda un correo.
- **Al rechazar:** el prestador no puede iniciar sesión y se le manda un correo.

El emprendimiento pasa a la pestaña **Aprobados** o **Rechazados**.

### 3.3 Eliminar un prestador

Si un prestador aprobado no cumple las reglas de la plataforma:

1. En **Aprobados**, tocá el emprendimiento.
2. Tocá **Eliminar prestador** y confirmá con **Sí, eliminar**.

Pasa lo siguiente:

- El prestador **ya no puede iniciar sesión**.
- **Sus actividades dejan de estar publicadas.**
- A los turistas que habían pagado reservas **se les devuelve el 100 %**.

El emprendimiento pasa a la pestaña **Eliminados**.

---

## 4. Moderación de actividades

En **Moderación** está la tabla de **Actividades publicadas**, con el nombre (tocalo para ver la
actividad como la ve un turista), el emprendimiento, el lugar y el precio.

Las actividades las manejan los prestadores. Desde acá **solo podés eliminar una si es
inapropiada:**

1. Tocá **Eliminar** en la fila de la actividad.
2. Confirmá con **Sí, eliminar** (o **No** para volver).

Pasa lo siguiente:

- Se cancela cada fecha de la actividad.
- A los turistas que habían pagado **se les devuelve el 100 %**.
- **Se le avisa al prestador.**

---

## 5. Catálogo: etiquetas y equipamiento

En **Catálogo** hay dos listas. Son las mismas para toda la plataforma: los prestadores eligen de
acá al cargar sus actividades.

- **Etiquetas:** los tipos de actividad, por ejemplo trekking o cabalgata. Los turistas filtran
  por estas etiquetas, y cada actividad tiene que tener al menos una.
- **Equipamiento:** por ejemplo casco, chaleco salvavidas o bicicleta. El prestador indica cuál
  tiene que llevar el turista y cuál da él.

En cada lista podés:

| Acción | Cómo |
|---|---|
| **Agregar** | Escribí el nombre en el campo de arriba (hasta 60 caracteres) y tocá **Agregar**. |
| **Modificar** | Tocá **Modificar**, cambiá el nombre y tocá **Guardar**. El cambio se ve en todas las actividades que lo usan. |
| **Eliminar** | Tocá **Eliminar** y confirmá con **Sí, eliminar**. |

> **No se puede eliminar** una etiqueta o un equipamiento que usa alguna actividad publicada.
> Aparece, por ejemplo: "Hay actividades publicadas con la etiqueta "Trekking". No se puede
> eliminar."

---

## 6. Mensajes frecuentes

| Mensaje | Qué significa |
|---|---|
| "No hay emprendimientos pendientes." | No hay solicitudes nuevas para revisar. |
| "Elegí un emprendimiento de la lista para ver su detalle." | Tocá una fila de la tabla. |
| "Hay actividades publicadas que usan ... No se puede eliminar." | Ese equipamiento está en uso. |
| "Esta sección es para usuarios con perfil Administrador." | Iniciá sesión con la cuenta del administrador. |

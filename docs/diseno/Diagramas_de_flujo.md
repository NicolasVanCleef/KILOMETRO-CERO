# Diagramas de flujo — Uruguay Mnatural

Muestran los pasos y las decisiones de los procesos principales, vistos desde el usuario. Para
ver qué clases intervienen en cada paso, consultá los
[diagramas de secuencia](Diagramas_de_secuencia.md).

Símbolos: los rectángulos redondeados son el inicio y el fin, los rectángulos son pasos, los
rombos son decisiones y los paralelogramos son mensajes que se muestran al usuario.

## 1. Reservar y pagar una actividad

Turista · CU-15 y CU-16 · reglas RN-15 (retención de 10 minutos), RN-19 (solo un pago aprobado
confirma) y RN-21 (24 horas para reintentar el pago).

![Flujo: reservar y pagar](img/Flujo_1_reservar_y_pagar.png)

## 2. Registro y aprobación de un emprendimiento

Prestador y Administrador · CU-01, CU-02, CU-10, CU-11 y CU-23.

![Flujo: emprendimiento](img/Flujo_2_emprendimiento.png)

## 3. Cancelar una reserva

Turista · CU-19, y cancelaciones del prestador o el administrador · CU-12, CU-23 y CU-24 · reglas
RN-26 (política de cancelación de la actividad), RN-27 y RN-28 (100 % de reembolso cuando no
cancela el turista).

![Flujo: cancelar reserva](img/Flujo_3_cancelar_reserva.png)

## 4. Estados de una reserva

Resume los flujos anteriores: por qué estados pasa una reserva (`BookingStatus` en el código) y
qué la hace cambiar.

![Estados de una reserva](img/Flujo_4_estados_reserva.png)

Las imágenes vectoriales (SVG) están en [`img/`](img/), con el mismo nombre.

<details>
<summary>Fuente de los diagramas (Mermaid, para editar)</summary>

```mermaid
flowchart TD
    A([Turista en el detalle de una actividad]) --> B{¿Inició sesión?}
    B -- No --> B1[Inicia sesión o crea su cuenta] --> C
    B -- Sí --> C[Toca Reservar en una fecha]
    C --> D[Indica cuántas personas van]
    D --> E{¿La fecha está programada,<br/>dentro del plazo y con<br/>lugares suficientes?}
    E -- No --> E1[/Mensaje con el motivo/] --> D
    E -- Sí --> F[Se retienen los lugares<br/>10 minutos<br/>Reserva: Pendiente de pago]
    F --> G[Carga los datos de cada participante]
    G --> H{¿Terminó antes de<br/>los 10 minutos?}
    H -- No --> X([Reserva Expirada:<br/>los lugares se liberan])
    H -- Sí --> I{¿La actividad<br/>tiene costo?}
    I -- No --> OK
    I -- Sí --> J[Elige tarjeta nueva o guardada y paga]
    J --> K[El servicio de pagos responde<br/>espera máxima: 1 minuto]
    K --> L{¿Pago aprobado?}
    L -- Sí --> OK([Reserva Confirmada<br/>correo con el código de reserva])
    L -- No --> M[No se cobra nada<br/>lugares retenidos 24 horas]
    M --> N{¿Reintenta antes<br/>de las 24 horas?}
    N -- Sí --> J
    N -- No --> X
```

```mermaid
flowchart TD
    A([Prestador entra a Registrar emprendimiento]) --> B{¿Persona física<br/>o jurídica?}
    B -- Física --> C1[Ingresa CI de 7 u 8 dígitos]
    B -- Jurídica --> C2[Ingresa RUT de 12 dígitos<br/>y representante]
    C1 --> D[Razón social, contacto,<br/>ubicación, descripción y contraseña]
    C2 --> D
    D --> E{¿Datos válidos, correo sin cuenta<br/>y la CI sin otro emprendimiento?}
    E -- No --> E1[/Mensaje en cada campo con error/] --> D
    E -- Sí --> F[Emprendimiento Pendiente<br/>el usuario no puede iniciar sesión]
    F --> G[Administrador revisa el detalle<br/>en Emprendimientos → Pendientes]
    G --> H{¿Acepta?}
    H -- Sí --> I[Aprobado<br/>correo al prestador]
    H -- No --> J([Rechazado<br/>correo al prestador<br/>no puede iniciar sesión])
    I --> K[El prestador inicia sesión]
    K --> L[Carga sus actividades<br/>con al menos una etiqueta]
    L --> M[Agrega fechas con cupo y precio]
    M --> N([Los turistas pueden reservar])
    I -. si no cumple las reglas .-> R([Administrador lo elimina:<br/>actividades despublicadas<br/>y reembolso del 100 %])
```

```mermaid
flowchart TD
    A([Turista en Mis reservas]) --> B[Toca Cancelar reserva]
    B --> C{¿Estado de la reserva?}
    C -- Pendiente de pago --> D([Se cancela siempre<br/>no había nada pagado<br/>los lugares se liberan])
    C -- Confirmada --> E{¿La actividad permite<br/>cancelar?}
    E -- No --> E1([No se muestra el botón<br/>la reserva sigue confirmada])
    E -- Sí --> F{¿Falta más que el plazo<br/>de cancelación para el inicio?}
    F -- No --> E1
    F -- Sí --> G[Confirma: Sí, cancelar]
    G --> H[Reserva Cancelada<br/>los lugares se liberan]
    H --> I{¿La actividad tiene<br/>reembolso?}
    I -- No --> J([Sin devolución])
    I -- Sí --> K([Se devuelve el porcentaje<br/>que fijó el prestador<br/>a la misma tarjeta])

    P([El prestador cancela la fecha<br/>o el administrador elimina<br/>la actividad o el prestador]) --> Q[Todas las reservas de esas<br/>fechas pasan a Cancelada]
    Q --> R([Correo a cada turista<br/>y reembolso del 100 % a los que pagaron])
```

```mermaid
stateDiagram-v2
    direction LR
    [*] --> PendienteDePago: el turista elige la cantidad de personas<br/>(lugares retenidos 10 minutos)
    PendienteDePago --> Confirmada: pago aprobado<br/>o actividad sin costo
    PendienteDePago --> PendienteDePago: pago rechazado o sin respuesta<br/>(plazo pasa a 24 horas)
    PendienteDePago --> Expirada: vence el plazo<br/>(tarea automática cada minuto)
    PendienteDePago --> Cancelada: la deja el turista,<br/>o cancelan la fecha
    Confirmada --> Cancelada: cancela el turista según la política,<br/>o el prestador / administrador
    Expirada --> [*]
    Cancelada --> [*]
    Confirmada --> [*]: se realiza la actividad

    PendienteDePago: Pendiente de pago
```

</details>

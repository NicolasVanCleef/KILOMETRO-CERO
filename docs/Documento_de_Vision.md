# Documento de Visión — Uruguay Mnatural

**Kilómetro Cero** · Proyectos TIC 2026 · Cliente: Uruguay Mnatural (UM)

## 1. Introducción

### 1.1 Propósito

Este documento reúne, de forma ordenada, las necesidades y las características de alto nivel
de la plataforma Uruguay Mnatural: qué necesitan los interesados y los usuarios, por qué lo
necesitan y qué ofrece el producto para resolverlo. El detalle de cómo lo resuelve está en la
Especificación de Requerimientos con Casos de Uso (ERS) y en los documentos de diseño del
sistema.

### 1.2 Alcance

El documento aplica a la plataforma web Uruguay Mnatural, que reúne en un solo lugar la oferta
de actividades turísticas de todo el país para tres tipos de usuario: turistas, prestadores
turísticos y administradores. La desarrolla el equipo Kilómetro Cero en el curso Proyectos TIC
2026; el docente del curso actúa como representante del cliente, Uruguay Mnatural.

### 1.3 Definiciones, acrónimos y abreviaturas

| Término | Definición |
|---|---|
| UM | Uruguay Mnatural, organización vinculada al Ministerio de Turismo; cliente del proyecto. |
| Kilómetro Cero | Empresa desarrolladora (el equipo de estudiantes). |
| Visitante | Persona que navega el portal sin haber iniciado sesión. |
| Turista | Usuario registrado que reserva, paga y valora actividades. |
| Prestador | Empresa (persona jurídica, con RUT) o particular (persona física, con CI) que ofrece actividades. |
| Emprendimiento | Datos de identificación, contacto, ubicación y descripción de un prestador. Un administrador lo aprueba antes de que pueda operar. |
| Actividad | Experiencia turística que ofrece un emprendimiento, por ejemplo trekking, cabalgata o enoturismo. |
| Fecha (instancia) | Día y hora concretos en que se realiza una actividad, con su cupo y su precio. |
| Reserva | Lugares que toma un turista en una fecha. Se confirma solo cuando se aprueba el pago (o si la actividad no tiene costo). |
| Retención de lugares | Los lugares elegidos quedan guardados 10 minutos para el turista mientras completa la reserva. |
| Etiqueta | Tipo de actividad del catálogo común, con el que se clasifican y filtran las actividades. |
| API REST | Interfaz de programación sobre HTTP con el estilo REST e intercambio en JSON. |
| MVC | Patrón Modelo-Vista-Controlador. |
| MER | Modelo entidad-relación de la base de datos. |
| Sandbox | Entorno de prueba de un servicio externo, donde no se mueve dinero real. |

### 1.4 Referencias

- Proyectos TIC 2026 — "Proyecto Uruguay Mnatural". Enunciado del curso.
- Kilómetro Cero. Acta de fundación.
- Actas de reunión con el cliente.
- Especificación de Requerimientos con Casos de Uso (ERS) y Reglas de negocio.
- Documentos de diseño del sistema: MER, arquitectura, diagrama de clases, diagramas de secuencia, diagramas de flujo y mockups.
- Manuales de usuario: turista, prestador y administrador.
- Documentación de la API (`docs/API.md`).

## 2. Visión

### 2.1 La visión de Kilómetro Cero

El acta de fundación de Kilómetro Cero define su visión así:

> Aspiramos a ser la empresa de referencia en el desarrollo de soluciones digitales para el
> sector turístico uruguayo, reconocidos por construir plataformas confiables y centradas en el
> usuario, que impulsen la digitalización del turismo nacional y fortalezcan la conexión entre
> el territorio y quienes lo visitan.

Uruguay Mnatural es el primer proyecto en el que esa visión se lleva a un producto concreto.

### 2.2 La visión del producto

Uruguay Mnatural será el punto de partida para recorrer Uruguay: el lugar donde un turista
encuentra, reserva y paga cualquier actividad del país, y donde cualquier emprendimiento,
grande o chico, publica su oferta y gestiona sus reservas sin necesitar infraestructura propia.

### 2.3 Cómo el producto cumple la visión

Cada parte de la visión de la empresa se traduce en decisiones concretas del producto:

| La visión dice… | En Uruguay Mnatural significa… |
|---|---|
| **Soluciones digitales para el sector turístico uruguayo** | Una plataforma pensada para el turismo del país: búsqueda por los 19 departamentos y por localidad, emprendimientos identificados con CI o RUT, y una API pública para que otras plataformas del ecosistema turístico consulten la oferta. |
| **Plataformas confiables** | Una reserva se confirma solo si el pago se aprueba. Los lugares retenidos no se pueden vender dos veces, aunque muchos turistas reserven al mismo tiempo. Si el servicio de pagos falla o no responde, no se cobra ni se confirma, y la reserva queda pendiente 24 horas para reintentar. Si se cancela una fecha, el dinero se devuelve solo. |
| **Centradas en el usuario** | Cada rol ve solo lo suyo, con palabras de todos los días. El turista ve el clima de cada fecha, la política de cancelación antes de reservar y un reloj con el tiempo que le queda. El prestador carga su oferta sin conocimientos técnicos. Los errores explican qué pasó y cómo seguir. |
| **Impulsar la digitalización del turismo nacional** | Los emprendimientos pequeños, que hoy venden por redes sociales o por teléfono, tienen un canal de venta en línea con reserva, cobro y valoraciones, sin desarrollar nada propio. |
| **Conexión entre el territorio y quienes lo visitan** | El turista descubre actividades por destino y por tipo, conoce quién las ofrece, lee las valoraciones de otros turistas y ve la respuesta del prestador. El prestador conoce de antemano quién viene a cada fecha. |

## 3. Posicionamiento

### 3.1 Oportunidad de negocio

La oferta turística uruguaya está formada por muchas empresas, emprendimientos y particulares
repartidos en todos los departamentos. Hoy esa oferta está dispersa entre sitios web propios,
redes sociales y contactos individuales: el turista tiene que buscar, comparar y contactar cada
propuesta por separado, y los prestadores más chicos quedan con poca visibilidad.

Uruguay Mnatural ve la oportunidad de reunir la oferta nacional en una única plataforma que
facilite descubrir, reservar y pagar actividades, y que les dé a los prestadores, incluso a los
de menor escala, un canal de venta profesional.

### 3.2 Definición del problema

| Elemento | Descripción |
|---|---|
| El problema de | la dispersión de la oferta de actividades turísticas en sitios web, redes sociales y canales de contacto independientes |
| afecta a | los turistas que planifican su estadía en Uruguay, a los prestadores turísticos y a Uruguay Mnatural en su rol de promotor del turismo nacional |
| cuyo impacto es | la dificultad para descubrir y comparar actividades desde un solo lugar, oportunidades de reserva perdidas, poca visibilidad de los prestadores pequeños y la falta de un canal común de pago y valoración |
| una solución exitosa sería | una plataforma web que reúna la publicación, la búsqueda, la reserva, el pago y la valoración de actividades de todo el país, con espacios propios para turistas, prestadores y administradores, conectada con un servicio externo de pagos y con un servicio de clima |

### 3.3 Sentencia de posición del producto

| Elemento | Descripción |
|---|---|
| Para | turistas nacionales y extranjeros que visitan o planean visitar Uruguay |
| que | necesitan descubrir, comparar y reservar actividades confiables en cualquier punto del país |
| Uruguay Mnatural | es una plataforma web de intermediación turística |
| que | reúne la oferta de muchos prestadores, permite buscar por destino, tipo, fecha y precio, reservar lugares concretos, pagar en línea y valorar la experiencia |
| a diferencia de | la búsqueda dispersa en los sitios de cada prestador, las redes sociales o los contactos informales |
| nuestro producto | ofrece un único punto de acceso a la oferta nacional, con disponibilidad real, pago integrado y un espacio de gestión propio para cada prestador, respaldado por Uruguay Mnatural |

## 4. Interesados y usuarios

### 4.1 Interesados

| Interesado | Descripción | Responsabilidades |
|---|---|---|
| Uruguay Mnatural (UM) | Organización vinculada al Ministerio de Turismo; cliente del proyecto | Definir las necesidades, validar los entregables y aprobar el diseño y la usabilidad |
| Docente del curso | Representa a UM durante el curso | Actuar como cliente en las reuniones de relevamiento y evaluar el proyecto en la defensa |
| Kilómetro Cero | Empresa desarrolladora | Relevar, especificar, diseñar, construir, probar y documentar la solución, y presentarla |
| Prestadores turísticos | Empresas y particulares del sector | Registrar su emprendimiento, publicar actividades y fechas, y atender las reservas |
| Turistas | Público general, nacional o extranjero | Buscar, reservar, pagar y valorar actividades |

### 4.2 Usuarios

| Usuario | Descripción | Qué hace en la plataforma |
|---|---|---|
| Visitante | Persona que no inició sesión | Busca y filtra actividades, ve su detalle, sus fechas, el clima y las valoraciones; crea su cuenta o registra un emprendimiento |
| Turista | Usuario registrado | Reserva lugares, carga los datos de los participantes, paga, consulta y cancela sus reservas, y valora las actividades en las que participó |
| Prestador | Usuario de un emprendimiento aprobado | Mantiene los datos de su emprendimiento, publica actividades, programa fechas con cupo y precio, cancela fechas, consulta las reservas y responde las valoraciones |
| Administrador | Personal de Uruguay Mnatural | Acepta o rechaza los emprendimientos nuevos, elimina prestadores y actividades inapropiadas, y mantiene las etiquetas y el equipamiento |
| Sistema externo | Otra plataforma del ecosistema turístico | Consulta la API pública: emprendimientos, actividades de un día, las mejor valoradas del mes, verificación de un emprendimiento y búsqueda |

### 4.3 Entorno de los usuarios

Los turistas entran desde el celular o la computadora, en consultas breves mientras planifican
el viaje o durante la estadía, y en momentos puntuales de reserva. Los prestadores usan la
plataforma con frecuencia para mantener sus fechas y revisar las reservas, sobre todo desde una
computadora. Los administradores entran menos seguido, para revisar emprendimientos nuevos y
mantener los catálogos. Todos acceden con un navegador web, sin instalar nada.

### 4.4 Necesidades principales

| Necesidad | Prioridad | Cómo la resuelve el producto |
|---|---|---|
| Descubrir actividades de todo el país en un solo lugar | Alta | Portal con búsqueda por palabra, departamento, localidad, tipo de actividad, fecha y precio máximo, sin necesidad de cuenta |
| Decidir con información completa | Alta | Detalle con descripción, requisitos, equipamiento, ubicación, duración, próximas fechas con lugares libres, pronóstico del clima, política de cancelación y valoraciones |
| Reservar sin riesgo de quedarse sin lugar | Alta | Retención de los lugares durante 10 minutos, con control de cupo que impide vender dos veces el mismo lugar |
| Pagar en línea con confianza | Alta | Pago con tarjeta a través de un servicio externo; la reserva se confirma solo si el pago se aprueba, y se envía el código de reserva |
| Publicar y gestionar la oferta sin conocimientos técnicos | Alta | Espacio del prestador para su emprendimiento, sus actividades, sus fechas y sus reservas |
| Saber quién viene a cada fecha | Alta | Listado de reservas por fecha con los datos de cada participante |
| Cancelar con reglas claras | Media | Política de cancelación definida por el prestador (plazo y porcentaje de reembolso), visible antes de reservar; devolución del 100 % si cancela el prestador |
| Confiar en los prestadores | Media | Aprobación de cada emprendimiento por un administrador antes de que pueda publicar |
| Conocer la experiencia de otros turistas | Media | Valoraciones de 1 a 5 con comentario, solo de quienes participaron, y respuesta del prestador |
| Mantener catálogos homogéneos | Media | Etiquetas y equipamiento mantenidos por el administrador y elegidos de una lista por los prestadores |
| Integrarse con otras plataformas | Media | API pública documentada |

## 5. Vista general del producto

### 5.1 Perspectiva del producto

Uruguay Mnatural es un sistema nuevo e independiente. Tiene un frontend web con un espacio para
cada rol, y un backend común que expone una API REST y guarda los datos en una base PostgreSQL.
El backend se conecta con dos servicios externos: un servicio de pagos (Mercado Pago, con
credenciales de prueba, o un simulador propio intercambiable por configuración) y un servicio de
pronóstico del clima (Open-Meteo). A su vez, ofrece una API pública para que otras plataformas
consulten la oferta.

### 5.2 Resumen de capacidades

| Beneficio | Capacidades que lo soportan |
|---|---|
| Encontrar actividades rápido | Buscador en la página de inicio, búsqueda con seis filtros combinables, exploración por tipo de actividad, próximas salidas |
| Reservar con confianza | Fechas concretas con cupo, retención de lugares por 10 minutos con reloj visible, plazos de reserva fijados por el prestador |
| Pagar de forma segura | Pago con tarjeta nueva o guardada (solo se guarda un token y los últimos 4 dígitos); 24 horas para reintentar si el pago no se aprueba |
| Gestionar la oferta de forma autónoma | Alta y modificación de actividades con etiquetas, equipamiento, precio, cupo y política de cancelación; fechas con su propio cupo y precio |
| Una plataforma confiable | Aprobación de emprendimientos, moderación de contenido inapropiado, reembolsos automáticos |
| Interoperar con otros sistemas | API pública con cinco consultas, sin necesidad de iniciar sesión |

### 5.3 Suposiciones y dependencias

- El servicio de pagos se usa en su entorno de prueba: no se mueve dinero real.
- El pronóstico del clima depende de un servicio público gratuito; si no responde, la plataforma funciona igual, sin mostrar el pronóstico.
- Los correos (confirmación de reserva, aprobación de emprendimientos, recuperación de contraseña) se simulan: se registran en el backend en lugar de enviarse.
- La plataforma está en español; el idioma de preferencia del turista se guarda en su perfil para una versión futura.
- El sistema se ejecuta en un entorno local (frontend, backend y base de datos).
- El proyecto es académico: el alcance y los plazos siguen el calendario del curso.

### 5.4 Costos y precios

Al ser un proyecto académico, no se define un modelo comercial. La presentación comercial
plantea modelos posibles de sostenibilidad, como una comisión por reserva o una suscripción de
los prestadores, a validar con el cliente.

## 6. Características del producto

Cada característica se detalla en uno o más casos de uso de la ERS. Todas están implementadas.

| ID | Área | Característica |
|---|---|---|
| F-01 | Portal | Buscar y filtrar actividades sin cuenta, por palabra, departamento, localidad, tipo, fecha y precio |
| F-02 | Portal | Ver el detalle de una actividad: emprendimiento, ubicación, descripción, requisitos, equipamiento, duración, precio y política de cancelación |
| F-03 | Portal | Ver las próximas fechas con su precio, sus lugares libres y el pronóstico del clima |
| F-04 | Portal | Ver las valoraciones de cada actividad con la respuesta del prestador |
| F-05 | Cuentas | Registro de turistas, inicio y cierre de sesión, recuperación de contraseña y edición del perfil |
| F-06 | Turista | Reservar lugares en una fecha, con retención de 10 minutos y control de cupo |
| F-07 | Turista | Cargar los datos de cada participante |
| F-08 | Turista | Pagar con tarjeta nueva o guardada; reintentar durante 24 horas si el pago no se aprueba |
| F-09 | Turista | Consultar sus reservas y cancelarlas según la política de la actividad |
| F-10 | Turista | Valorar (1 a 5, con comentario) las actividades en las que participó |
| F-11 | Prestador | Registrar el emprendimiento como persona física o jurídica, sujeto a aprobación |
| F-12 | Prestador | Mantener los datos de contacto, ubicación y descripción del emprendimiento |
| F-13 | Prestador | Publicar, modificar y eliminar actividades |
| F-14 | Prestador | Programar fechas con cupo y precio propios; cancelar o posponer una fecha |
| F-15 | Prestador | Consultar las reservas de cada fecha con los datos de los participantes |
| F-16 | Prestador | Responder las valoraciones |
| F-17 | Administración | Aceptar o rechazar emprendimientos nuevos |
| F-18 | Administración | Eliminar prestadores y actividades inapropiadas, con reembolso a los turistas |
| F-19 | Administración | Mantener etiquetas (tipos de actividad) y equipamiento |
| F-20 | Integraciones | Procesar pagos y reembolsos con un servicio externo, sin confirmar la reserva si el pago falla o no hay respuesta |
| F-21 | Integraciones | Mostrar el pronóstico del clima de cada fecha |
| F-22 | API pública | Listar los emprendimientos |
| F-23 | API pública | Listar las actividades de un día por departamento o localidad |
| F-24 | API pública | Listar las 3 actividades mejor valoradas del mes por departamento |
| F-25 | API pública | Verificar un emprendimiento |
| F-26 | API pública | Buscar actividades por palabra clave |

## 7. Restricciones

El cliente impuso las siguientes restricciones, y la solución las cumple:

| Restricción | Cómo se cumple |
|---|---|
| Backend en Java, Spring / Spring Boot y Maven | Spring Boot 4 con Java 25 y Maven |
| Frontend a elección del equipo | React 19 con Vite |
| Patrón MVC en capas | Controladores, servicios, repositorios y modelo; la vista es el frontend |
| API REST documentada | Todos los endpoints documentados en `docs/API.md` |
| Base de datos SQL modelada con MER | PostgreSQL 17, con su MER |
| Tests unitarios de los principales casos de uso | 141 tests automáticos (JUnit 5 y Mockito) |
| Nombres en inglés en el código | Código en inglés; la documentación, en español |
| Gestión de versiones con Git | Repositorio en GitHub, un commit por cada parte del sistema |
| Infraestructura de la base en la nube, Docker o local | Local |
| Diseño visual a elección del equipo, con aprobación del cliente | Identidad visual de Kilómetro Cero |

## 8. Atributos de calidad

| Atributo | Objetivo | Cómo se logra |
|---|---|---|
| Usabilidad | Un turista sin experiencia reserva sin ayuda; un prestador publica sin capacitación | Cada rol ve solo sus opciones; formularios con ayudas y mensajes de error por campo |
| Confiabilidad | Nunca confirmar una reserva sin pago aprobado | La confirmación depende de la respuesta del servicio de pagos; ante error o falta de respuesta, la reserva queda pendiente |
| Consistencia de datos | No vender dos veces el mismo lugar | Bloqueo de la fecha en la base de datos durante la reserva |
| Disponibilidad | Que una falla externa no frene el sistema | Esperas máximas a los servicios externos (1 minuto el pago, 5 segundos el clima) |
| Seguridad | Proteger cuentas y pagos | Contraseñas con hash BCrypt, sesión con token JWT, permisos por rol, la tarjeta nunca se guarda completa |
| Mantenibilidad | Poder extender el sistema | Capas separadas y servicios externos detrás de interfaces intercambiables |

## 9. Precedencia y prioridad

Siguiendo la indicación del cliente de priorizar el funcionamiento de los requerimientos
principales, el sistema se construyó en diez partes, en este orden:

1. Base del proyecto: backend, frontend, base de datos y Git.
2. Catálogo de etiquetas y equipamiento.
3. Registro de emprendimientos y aprobación del administrador.
4. Usuarios, inicio de sesión y roles.
5. Actividades y fechas.
6. Portal público.
7. Reservas con cupo y retención de lugares.
8. Pagos con el servicio externo.
9. Valoraciones y respuesta del prestador.
10. API pública y clima.

## 10. Documentación relacionada

Este Documento de Visión es la base de los demás entregables del proyecto:

- Acta de fundación de Kilómetro Cero.
- Actas de reunión con el cliente.
- Especificación de Requerimientos con Casos de Uso y Reglas de negocio.
- Documentos de diseño del sistema: MER, arquitectura, diagrama de clases, diagramas de secuencia, diagramas de flujo y mockups.
- Manuales de usuario: turista, prestador y administrador.
- Presentación comercial y presentación técnica.

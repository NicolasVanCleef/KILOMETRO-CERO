# Diseño de la arquitectura — Uruguay Mnatural

## 1. Resumen

Uruguay Mnatural es una aplicación web con **frontend y backend separados** que se comunican por
una **API REST con JSON**:

- **Frontend:** una aplicación de una sola página (SPA) en React. Es lo que ve el usuario en el
  navegador.
- **Backend:** una aplicación Spring Boot que tiene toda la lógica de negocio, controla los
  permisos, guarda los datos en PostgreSQL y se conecta con los servicios externos.
- **Servicios externos:** Mercado Pago (pagos, en modo de prueba) y Open-Meteo (pronóstico del
  clima). Otros sistemas pueden consultar la API pública del backend.

## 2. Componentes

![Componentes del sistema](img/Arquitectura_componentes.png)

| Componente | Tecnología | Responsabilidad |
|---|---|---|
| Frontend | React 19, React Router 7, Vite | Pantallas de cada rol, validación inicial de formularios, guarda la sesión (token) en el navegador |
| Backend | Java 25, Spring Boot 4.1, Maven | API REST, reglas de negocio, seguridad, transacciones |
| Base de datos | PostgreSQL 17 | Datos persistentes; el esquema lo genera Hibernate/JPA a partir del modelo |
| Pagos | API REST de Mercado Pago | Tokenizar la tarjeta, cobrar y reembolsar. Uruguay Mnatural nunca guarda el número completo de la tarjeta |
| Clima | API REST de Open-Meteo | Pronóstico de cada fecha de una actividad |
| Correo | `EmailService` | En esta versión es simulado: escribe el correo en el log del backend |

## 3. Patrón MVC en capas

El backend sigue el patrón **MVC en capas** que pide la consigna. Cada capa solo usa a la de
abajo:

![Capas](img/Arquitectura_capas.png)

| Capa | Paquete | Qué hace | Ejemplo |
|---|---|---|---|
| Vista | frontend React | Muestra los datos y captura lo que hace el usuario | `BookingPage.jsx` |
| Controlador | `controller/` | Recibe el pedido HTTP, convierte el JSON en un DTO y llama al servicio. No tiene reglas de negocio | `BookingController` |
| Servicio | `service/` | Reglas de negocio y transacciones (`@Transactional`), correos e integraciones externas | `BookingService.hold()` |
| Modelo | `model/` | Entidades JPA con su estado y su comportamiento | `ActivitySchedule.takePlaces()` |
| Repositorio | `repository/` | Acceso a la base con Spring Data JPA | `ActivityScheduleRepository.findByIdForUpdate()` |
| DTO | `dto/` | Objetos que viajan en el JSON, separados de las entidades para no exponer datos internos (por ejemplo, la contraseña) | `BookingDto` |

Paquetes transversales:

| Paquete | Contenido |
|---|---|
| `security/` | `JwtService` (crea y valida los tokens) y `AuthInterceptor` (exige sesión y controla el rol de cada ruta) |
| `exception/` | Excepciones de negocio y `ApiExceptionHandler`, que las convierte en respuestas HTTP con un mensaje en español |
| `config/` | Configuración: contraseñas con BCrypt, registro del interceptor, tareas programadas y el administrador inicial |
| `sandbox/` | Servicio de pagos simulado, alternativa a Mercado Pago para trabajar sin conexión |

El detalle de las clases del modelo está en el [diagrama de clases](Diagrama_de_clases.md), y el
de cómo colaboran las capas en los [diagramas de secuencia](Diagramas_de_secuencia.md).

## 4. Seguridad y permisos

- **Contraseñas:** se guardan con hash BCrypt, nunca en texto.
- **Sesión:** al iniciar sesión el backend devuelve un **token JWT** firmado que vence en 8 horas.
  El frontend lo manda en cada pedido en el encabezado `Authorization: Bearer ...`.
- **Roles por ruta:** `AuthInterceptor` revisa cada pedido:

| Ruta | Quién puede |
|---|---|
| Las demás: portal (`/api/activities`, `/api/tags`, `/api/equipment`), `/api/public/...`, `/api/auth/...` y los registros | Cualquiera, sin sesión |
| `/api/account/...` | Cualquier usuario con sesión |
| `/api/tourist/...` | Solo turistas |
| `/api/provider/...` | Solo prestadores |
| `/api/admin/...` | Solo administradores |

- **Datos propios:** además del rol, los servicios controlan que cada uno vea y cambie solo lo
  suyo (por ejemplo, un prestador solo ve las reservas de sus actividades).
- **Tarjetas:** solo se guardan el token que da Mercado Pago y los últimos 4 dígitos.

## 5. Decisiones de diseño importantes

| Decisión | Por qué |
|---|---|
| Retención de lugares con bloqueo de fila (`SELECT ... FOR UPDATE`) | Dos turistas que reservan los últimos lugares al mismo tiempo no pueden sobrepasar el cupo |
| Tarea programada cada 1 minuto (`BookingExpirationJob`) | Libera los lugares de las reservas pendientes que vencieron, sin intervención de nadie |
| Llamado al servicio de pagos fuera de las transacciones | El pago puede demorar hasta 1 minuto; así no se bloquea la base mientras tanto |
| Interfaz `PaymentGateway` con dos implementaciones | Se puede cambiar Mercado Pago por el sandbox propio con una propiedad (`payments.provider`), sin tocar los servicios |
| El clima se pide aparte del detalle | Si Open-Meteo no responde, la página de la actividad se muestra igual (sin el pronóstico) |
| Baja lógica de actividades y prestadores | Se conserva el historial de reservas y pagos |

## 6. Ejecución y despliegue

En esta versión todo corre en una sola máquina:

| Pieza | Cómo se ejecuta | Dirección |
|---|---|---|
| Frontend | `npm run dev` en `frontend/` | http://localhost:5173 |
| Backend | `mvnw spring-boot:run` en `backend/` | http://localhost:8080 |
| Base de datos | PostgreSQL 17 instalado localmente | localhost:5432, base `uruguay_mnatural` |

En desarrollo, Vite redirige al backend todo pedido que empieza con `/api`, así el navegador ve
un único origen. Las credenciales de prueba de Mercado Pago van en `backend/secrets.properties`,
que no se sube a Git. Sin ese archivo se puede usar el sandbox propio con
`PAYMENTS_PROVIDER=sandbox`.

La API REST completa está documentada en [`../API.md`](../API.md).

<details>
<summary>Fuente de los diagramas (Mermaid, para editar)</summary>

```mermaid
flowchart LR
    subgraph USR["Usuarios"]
        V["Visitante / Turista"]
        P["Prestador"]
        A["Administrador"]
    end
    EXT["Sistemas externos<br/>(consumen la API pública)"]

    subgraph FE["Frontend — React 19 + Vite (puerto 5173)"]
        R["Páginas y componentes<br/>pages/ · components/"]
        AJ["api.js<br/>fetch + token JWT"]
        SS["session.js<br/>sesión en localStorage"]
        R --> AJ
        R --> SS
    end

    subgraph BE["Backend — Spring Boot 4 + Java 25 (puerto 8080)"]
        INT["AuthInterceptor<br/>valida el JWT y el rol por ruta"]
        CTL["Controladores REST<br/>/api/... · /api/public/..."]
        SRV["Servicios<br/>reglas de negocio"]
        REP["Repositorios<br/>Spring Data JPA"]
        JOB["BookingExpirationJob<br/>cada 1 minuto"]
        GW["PaymentGateway<br/>MercadoPagoGateway · SandboxPaymentGateway"]
        WC["WeatherClient"]
        EM["EmailService<br/>simulado: escribe en el log"]
        INT --> CTL --> SRV --> REP
        JOB --> SRV
        SRV --> GW
        SRV --> WC
        SRV --> EM
    end

    DB[("PostgreSQL 17<br/>base uruguay_mnatural")]
    MP["API de Mercado Pago<br/>credenciales de prueba"]
    OM["API de Open-Meteo<br/>pronóstico del clima"]

    V & P & A -->|navegador| R
    AJ -->|"HTTP + JSON<br/>proxy /api"| INT
    EXT -->|"HTTP + JSON<br/>/api/public"| INT
    REP -->|JDBC| DB
    GW -->|HTTPS REST| MP
    WC -->|HTTPS REST| OM
```

```mermaid
flowchart TB
    VISTA["<b>Vista</b> — frontend React<br/>las pantallas de cada rol"]
    CTRL["<b>Controlador</b> — controller/<br/>recibe el pedido HTTP, valida el formato y devuelve JSON (dto/)"]
    SERV["<b>Servicio</b> — service/<br/>reglas de negocio, transacciones, correos e integraciones"]
    MOD["<b>Modelo</b> — model/<br/>entidades JPA con su comportamiento: approve, takePlaces, confirm…"]
    REPO["<b>Repositorio</b> — repository/<br/>consultas a la base con Spring Data JPA"]
    BD[("<b>Base de datos</b><br/>PostgreSQL")]

    VISTA -->|"HTTP + JSON"| CTRL
    CTRL --> SERV
    SERV --> MOD
    SERV --> REPO
    REPO --> MOD
    REPO --> BD

    TRANS["<b>Transversales</b><br/>security/ · exception/ · config/"]
    TRANS -.-> CTRL
    TRANS -.-> SERV
```

</details>

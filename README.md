# 🧵 Stock3D — Gestión de stock de filamento 3D

Proyecto personal full-stack para gestionar el inventario de filamento de
impresión 3D de cada usuario: qué tiene en stock, qué rollos tiene abiertos
y cuánto ha consumido o vendido, con autenticación, roles y un histórico
de movimientos auditable.

Construido para consolidar y demostrar conocimientos prácticos de
**Spring Boot** en el backend y **Angular** en el frontend, siguiendo el
flujo habitual de la industria: primero se cierra y se prueba la API REST,
después se construye la interfaz que la consume.

---

## 🎯 Qué resuelve

Cada usuario lleva su propio inventario de filamento (no es un stock
global): cuántos rollos cerrados tiene de cada producto, qué rollos tiene
abiertos y con cuántos gramos restantes, y un registro inmutable de cada
entrada, venta o consumo — pensado para poder responder preguntas como
"¿cuánto he vendido este mes?" o "¿qué le pasó a este rollo?" sin recalcular
ni fiarse de un campo que alguien pudo olvidar actualizar.

---

## 🛠️ Stack tecnológico

**Backend**
- Java 25 · Spring Boot 4
- Spring Data JPA (Hibernate) · PostgreSQL
- Spring Security + JWT (`io.jsonwebtoken`) · BCrypt
- Bean Validation (`@Valid`)
- Maven

**Frontend** *(en desarrollo)*
- Angular · TypeScript

**Herramientas**
- Docker (PostgreSQL en contenedor) · Postman

---

## ✅ Funcionalidades

- **Autenticación JWT** con roles (`ADMIN` / `USER`) y endpoints protegidos
  por rol y por ruta.
- **Contraseñas cifradas con BCrypt** — nunca se guarda ni se compara texto
  plano.
- **Catálogo de productos** (CRUD) con control de acceso: cualquier
  autenticado puede consultar, solo `ADMIN` puede crear/editar/borrar.
- **Stock personal por usuario**: altas de rollos cerrados, apertura de
  rollos, consumo parcial por gramos y venta de rollos enteros a precio
  libre.
- **Histórico de movimientos inmutable** (entradas, ventas, consumos) para
  auditoría y estadísticas, separado del estado mutable actual.
- **Paginación y filtrado** en los listados (`Page`/`Pageable`, filtro
  opcional por categoría).
- **Manejo global de errores** (`@RestControllerAdvice`): toda excepción de
  negocio o de validación responde con un formato de error consistente.
- **Protección contra IDOR**: cualquier operación sobre un recurso
  identificado por id comprueba que ese recurso pertenece de verdad al
  usuario autenticado, no al que el cliente diga ser.
- **DTOs de entrada y salida**: el cliente nunca puede enviar campos que no
  debería decidir (id, rol, u otro usuario), y las respuestas nunca filtran
  datos sensibles (el hash de la contraseña jamás viaja en un JSON).

---

## 🏗️ Arquitectura

```
Controller (HTTP)  →  Service (lógica de negocio)  →  Repository (datos)  →  PostgreSQL
```

Separación estricta de responsabilidades: el Controller traduce HTTP ↔ Java
y no conoce reglas de negocio; el Service las aplica sin saber nada de HTTP
(nada de `ResponseEntity` ahí dentro); el Repository solo lee/escribe.

**Decisiones de diseño destacadas:**
- El catálogo compartido (`Producto`) está separado del stock por usuario
  (`Inventario`, `EnUso`): el mismo producto puede tener cantidades
  distintas para cada usuario.
- Estado mutable (`Inventario`, `EnUso` — "cómo estamos ahora") y
  registro inmutable (`Movimiento` — "qué pasó") se modelan como cosas
  distintas: nada se sobreescribe, nada se recalcula a partir de un dato
  que podría desincronizarse.
- `BigDecimal` para dinero y para gramos, evitando los errores de
  redondeo de la coma flotante binaria.
- Nada de datos derivados guardados "por si acaso" (p. ej. si un rollo
  está agotado se consulta, no se almacena un booleano aparte).

---

## 🚀 Cómo ejecutarlo en local

**Requisitos:** JDK 21+, PostgreSQL 16 (o Docker), Maven (incluido el
wrapper `mvnw`, no hace falta instalarlo aparte).

```bash
# 1. Levantar PostgreSQL (opción con Docker)
docker run --name postgres-stock -e POSTGRES_PASSWORD=<tu_password> -p 5432:5432 -d postgres:16

# 2. Crear la base de datos
docker exec -it postgres-stock psql -U postgres -c "CREATE DATABASE stockdb;"

# 3. Variables de entorno necesarias
#    DB_USERNAME, DB_PASSWORD  -> credenciales de PostgreSQL
#    JWT_SECRET                -> cadena de 32+ caracteres para firmar los JWT

# 4. Arrancar la API (desde /backend)
./mvnw spring-boot:run
```

La API queda disponible en `http://localhost:8080`. `POST /registrar` y
`POST /login` son los únicos endpoints públicos; el resto exige un JWT en
la cabecera `Authorization: Bearer <token>`.

---

## 📋 Organización del trabajo: Sprints

El desarrollo se ha organizado en sprints autogestionados, con un objetivo
concreto y entregable por sprint — misma disciplina que un Scrum real,
adaptada a un equipo de una persona.

### Backend — completado

| Sprint | Objetivo | Entregado |
|---|---|---|
| **1** | Primer proyecto Spring Boot funcionando | Estructura del proyecto, primer endpoint, primer commit |
| **2** | API REST de verdad | CRUD de productos en memoria, verbos HTTP, códigos de estado |
| **3** | Persistencia | Primera entidad JPA, `JpaRepository`, base de datos en memoria (H2) |
| **4** | Base de datos real | PostgreSQL, configuración por variables de entorno |
| **5** | Arquitectura en capas | Capa de Service, DTOs, validación con `@Valid`, manejo global de errores |
| **6** | Usuarios y seguridad | Entidad `Usuario`, BCrypt, Spring Security, registro/login, JWT, roles |
| **7** | Dominio de stock completo | `Inventario`, `EnUso`, `Movimiento`; entradas, apertura de rollo, consumo, venta; paginación y filtros |

### Backlog

| Sprint | Objetivo |
|---|---|
| **8** | Tests (JUnit/Mockito, `@SpringBootTest`), migraciones con Flyway, documentación con Swagger/OpenAPI, perfiles `dev`/`prod`, despliegue |
| **9** | Frontend Angular: proyecto base, componentes, consumo de la API con `HttpClient` |
| **10** | Routing y formularios reactivos (alta/edición de productos) |
| **11** | Autenticación en el frontend: login, interceptors, guards por rol |
| **12** | Interfaz completa de inventario y movimientos de stock |

---

## 📄 Licencia

Proyecto personal con fines de aprendizaje y demostración de habilidades.

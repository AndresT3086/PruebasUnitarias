# PRUEBA TÉCNICA DE DOMINIO PROFUNDO - INGENIERO DE SOFTWARE BACKEND

## Introducción

Bienvenido a nuestro desafío de diseño y arquitectura de software. Esta prueba evalúa tu habilidad para modelar un dominio complejo, aplicar principios de ingeniería de software y diseñar sistemas robustos y escalables.

El ejercicio está diseñado para ser completado en aproximadamente **8 horas**. Eres libre de usar el lenguaje de backend de tu elección (**Java, C#, Python, Go**, etc.) y sus ecosistemas correspondientes.

El uso de IA es permitido como herramienta de apoyo, pero la originalidad en el diseño, la justificación de tus decisiones y la calidad del código final son los principales focos de evaluación.

## El Dominio: "LogiTrack" - Sistema de Trazabilidad de Paquetes

Debes diseñar el núcleo de un sistema de logística para el seguimiento de paquetes. Este dominio presenta desafíos en el modelado de entidades, la gestión de estados y la coordinación de procesos.

---

### Fase 1: Núcleo del Dominio (Dev Essential)

Objetivo: Demostrar competencias del nivel Dev Essential mediante un modelo de dominio puro, enfatizando: POO básica, uso adecuado de estructuras de datos, buenas prácticas de Clean Code/SOLID a pequeña escala, CRUD básico y pruebas unitarias. Se permite el consumo mínimo de una API REST externa o el uso de mocks/stubs HTTP para demostrar integración básica y manejo de errores.

Requerimientos (alineados a Essential):

1.  POO básica: Modelado de Entidades
    - Diseña la entidad `Package` con:
      - ID de seguimiento (ej. "LT-123456789").
      - Información del destinatario (nombre, dirección).
      - Dimensiones (alto, ancho, profundidad) y peso.
      - Estado actual.
      - Historial de ubicaciones (`LocationHistory`) compuesto por registros con ubicación (ciudad/país) y fecha/hora.
    - Encapsula los datos y provee comportamientos claros (por ejemplo, `addLocation(...)`, `changeStatus(...)` o equivalente) que operen sobre el estado del objeto.

2.  Estructuras de datos adecuadas
    - Implementa `LocationHistory` utilizando una colección apropiada del lenguaje (lista, arreglo dinámico, etc.).
    - Asegura la manipulación correcta: añadir nuevos registros en orden cronológico, exponer lecturas sin permitir modificaciones indebidas al estado interno (encapsulación/defensiva).

3.  CRUD básico
    - Implementa operaciones de Crear, Leer, Actualizar y Borrar para `Package` siguiendo un diseño estándar (servicio/repositorio) sin restringir la implementación a memoria.
    - Define generación/validación de ID únicos, reglas de actualización (qué campos pueden cambiar) y manejo de errores coherente (excepciones controladas o resultados explícitos).
    - Expón contratos claros a través de servicios o puertos de aplicación; no se requiere framework web.

4.  Consumo de APIs REST
    - Implementa un cliente simple que consuma una API REST externa relacionada (ej. servicio de países, geocodificación) o simúlalo con mocks/stubs de HTTP.
    - Debe contemplar manejo de errores (timeouts, 4xx/5xx), parseo/validación de la respuesta y una mínima resiliencia (reintento simple u opción de degradación razonable).
    - La integración debe estar cubierta por pruebas unitarias con mocking del cliente HTTP.

5.  Gestión del ciclo de vida (máquina de estados)
    - Estados: `CREATED`, `IN_TRANSIT`, `OUT_FOR_DELIVERY`, `DELIVERED`, `DELIVERY_FAILED`, `RETURNED`.
    - Define las transiciones válidas (p. ej., solo pasar a `OUT_FOR_DELIVERY` desde `IN_TRANSIT`).
    - Evita `if/else` o `switch` anidados y extensos. Prefiere un diseño escalable (tabla de transiciones, polimorfismo, patrón State simple) que proteja a `Package` de transiciones inválidas.

6.  Creación robusta de objetos
    - Permite crear `Package` de forma flexible y legible sin constructores con demasiados parámetros (Builder/Factory u otro enfoque equivalente).
    - Valida entradas básicas (ej. no permitir pesos negativos). Garantiza que el objeto nace en un estado consistente.

7.  Buenas prácticas y organización (Essential 2)
    - Estructura del proyecto clara (por ejemplo, carpeta/paquete de dominio y carpeta/paquete de tests).
    - Convenciones de estilo del lenguaje seguidas y nombres descriptivos.
    - Uso del gestor de dependencias del lenguaje (Maven/Gradle/npm/etc.).
    - Historial de Git con commits atómicos y mensajes claros que muestren el progreso.

8.  Pruebas unitarias (Essential 2–3)
    - Cubre caminos felices de transiciones válidas y la creación correcta del objeto.
    - Incluye casos de error y escenarios borde (ej. transiciones inválidas; datos inválidos como peso negativo si se valida).
    - Añade pruebas para CRUD y para el cliente REST usando dobles de prueba (mocks/stubs).
    - Nombres de pruebas descriptivos y aislados del entorno (sin dependencias externas).

Entregables

- Módulo de código con el modelo de dominio puro.
- Implementación de CRUD básico para `Package` (servicio/repositorio) con pruebas.
- Cliente REST (o mocks/stubs) con pruebas que demuestren consumo y manejo de errores.
- Suite de pruebas unitarias que cubra transiciones válidas e inválidas y la creación de objetos.
- README básico con instrucciones para ejecutar el proyecto y las pruebas, y ejemplos de uso (comandos curl/Postman o pseudocódigo) para CRUD y consumo REST.
- Historial de commits que evidencie trabajo incremental.

Criterios de evaluación Essential y evidencias esperadas

- Essential 1: POO básica y estructuras de datos. Evidencia: clase `Package` con atributos y métodos coherentes; `LocationHistory` implementado con colección adecuada y manipulación correcta.
- Essential 1: CRUD básico y consumo de APIs REST. Evidencia: servicio/repositorio con operaciones Crear/Leer/Actualizar/Borrar y cliente REST con manejo de errores (timeouts, 4xx/5xx) cubierto por pruebas o ejemplos.
- Essential 2: Documentación y buenas prácticas. Evidencia: README básico, estructura de carpetas clara, convenciones de estilo y commits atómicos.
- Essential 3: Clean Code/SOLID y testing más elaborado. Evidencia: máquina de estados sin condicionales extensos (enfoque escalable), encapsulación adecuada y pruebas que cubren casos felices y de error, incluyendo CRUD y cliente REST con mocks.

---

### Fase 2: Aplicación y Patrones de Diseño (Nivel: Dev Expert)

Objetivo: Construir una aplicación funcional sobre el dominio de LogiTrack aplicando Clean Architecture y principios SOLID, con una API REST, persistencia relacional, pruebas de integración y contenerización. Además, incorporar capacidades de desacoplamiento asíncrono y escalabilidad de lecturas propias de un ecosistema moderno. Justifica el uso de patrones de diseño para creación de objetos y gestión del ciclo de vida, documentando decisiones y trade-offs en EVALUATION.md.

Requerimientos (alineados a Expert):

1.  Arquitectura y Capas (Clean Architecture)
    - Separa claramente:
      - Dominio: modelos y lógica pura (máquina de estados, reglas, Builder/Factory) sin dependencias de frameworks.
      - Aplicación: casos de uso/servicios orquestadores que dependen de puertos (interfaces) del dominio.
      - Infraestructura: adaptadores de entrada (controladores REST) y salida (repositorios/JPA/ORM, mapeadores, cliente HTTP si aplica).
    - Aplica Inversión de Dependencias: la aplicación depende de interfaces, no de implementaciones concretas.

2.  API REST (endpoints mínimos)
    - Implementa endpoints para:
      - POST /packages: crear paquete.
      - GET /packages/{id}: obtener paquete por id con estado actual.
      - GET /packages/{id}/locations: obtener historial de ubicaciones.
      - PATCH /packages/{id}/status: cambiar estado (validado por la máquina de estados).
    - Manejo de errores consistente (400/404/409/500) y mensajes claros.
    - Documentación de API: incluye especificación OpenAPI/Swagger o una colección Postman/Insomnia con ejemplos de request/response.
    - Validación de DTOs en la capa API (Infraestructura): Usa un framework de validación propio del stack (p. ej., javax.validation/Spring Validation en Java; Pydantic/FastAPI validators en Python; FluentValidation en .NET) para validar la entrada antes de invocar la capa de aplicación o el dominio. Reglas mínimas de ejemplo: recipient_name no vacío, dirección con longitud/estructura mínima, dimensiones y peso positivos. Responder 400 con detalles de campos inválidos.

3.  Persistencia Relacional
    - Modela un esquema normalizado para Package y LocationHistory (1:N):
      - Tabla packages con identificador, datos del destinatario, dimensiones, peso, estado, timestamps.
      - Tabla package_locations con clave foránea a packages, ciudad/país, timestamp y orden cronológico.
    - Implementa repositorios/DAOs e integra un ORM si lo prefieres. Incluye migraciones opcionales (Flyway/Liquibase).

4.  Patrones de Diseño (obligatorios y justificados)
    - Creación de objetos: Builder o Factory para construir Package evitando constructores telescópicos y garantizando invarianzas.
    - Ciclo de vida/estados: State, Strategy o tabla de transiciones para validar y ejecutar cambios de estado sin if/switch extensos.
    - En EVALUATION.md, justifica: problema, alternativa(s) evaluadas y razones de la elección.

5.  Testing (robusto)
    - Pruebas de integración que validen el flujo API→Aplicación→DB (crear, consultar, cambiar estado, consultar historial).
    - Mocks/dobles de prueba para aislar dominio/casos de uso de infraestructura cuando corresponda.
    - Evidencia de TDD básica inferible por historial de commits es un plus.

6.  Contenerización y Ejecución
    - Provee un docker-compose.yml que levante la aplicación y la base de datos requerida.
    - Variables de entorno adecuadas y README con instrucciones de ejecución local y vía Docker.

7.  Seguridad básica implementada + Operabilidad (conceptual)
    - Implementa una verificación de API Key simple (cabecera `X-API-KEY`) mediante un middleware/interceptor/filtro que proteja al menos el endpoint `POST /packages`. La clave puede configurarse por variable de entorno o configuración.
    - Respuestas esperadas: 401/403 cuando falta o es inválida la API Key; 2xx cuando es válida.
    - Operabilidad (conceptual): En EVALUATION.md, describe cómo evolucionarías esto a JWT/OAuth y cómo añadirías logging/trazabilidad (Correlation ID) para producción.

8.  Desacoplamiento y Asincronismo (orientado a eventos)
    - Publica un evento de dominio cuando cambie el estado de un paquete (ej. PackageStateChanged).
    - Procesamiento asíncrono: la API debe responder sin bloquearse por tareas secundarias (p. ej., envío de notificaciones simulado por un consumidor).
    - Se acepta una implementación simple (cola en memoria, mecanismo de publicación/suscripción, o stub) y se valora justificar patrones como Outbox en EVALUATION.md.

9.  Escalabilidad de Lecturas (CQRS / Read Model)
    - Propón e implementa una vista de lectura optimizada para consultar rápidamente el estado actual e historial (puede ser una tabla/materialized view en la misma BD u otro almacén).
    - La API de consulta puede leer desde este modelo para reducir carga en la escritura transaccional.
    - Justifica la elección (relacional vs no relacional/cache) en EVALUATION.md.

10. Visión Estratégica
    - En EVALUATION.md, responde: "Como arquitecto principal de LogiTrack, presenta al CTO una propuesta para la evolución de la plataforma. Describe dos iniciativas técnicas clave para los próximos 12 meses, explicando problema de negocio, riesgos técnicos e impacto esperado." 

11.  Concurrencia: Optimistic Locking (control de versiones)
    - Añade un campo `version` a la entidad/recurso `Package` para implementar bloqueo optimista.
    - Requiere que las actualizaciones (incluido `PATCH /packages/{id}/status`) validen la versión recibida y rechacen con `409 Conflict` cuando sea obsoleta.
    - Evidencia: prueba de integración que simule dos actualizaciones concurrentes, donde la segunda falle por versión desactualizada.

Entregables específicos de Fase 2
- Código con capas separadas (dominio, aplicación, infraestructura) y patrones implementados.
- API REST funcional con endpoints definidos y documentación (OpenAPI o colección Postman/Insomnia).
- Validación de DTOs implementada en capa API: DTOs con anotaciones/esquemas de validación, mapeo de errores a 400 y evidencia mediante pruebas o colección mostrando rechazos por entradas inválidas.
- Seguridad básica implementada: middleware/interceptor/filtro que valide `X-API-KEY` protegiendo POST /packages; configuración de la clave (env/config) y evidencia (tests o colección mostrando 401/403 y 2xx).
- Esquema relacional implementado y persistencia operativa.
- Modelo de eventos y publicación asíncrona con al menos un consumidor simulado/documentado.
- Read model/CQRS básico para consultas (misma BD u otro almacén) con integración en endpoints de lectura.
- Pruebas de integración y unitarias/mocks relevantes (incluyendo eventos y lecturas).
- docker-compose.yml funcional para app + DB.
- EVALUATION.md con justificaciones de patrones, decisiones arquitectónicas, seguridad (implementación API Key y evolución a JWT/OAuth), modelo de eventos/CQRS y trade-offs.

---

### Fase 3: Escala y Arquitectura Avanzada (Nivel: Dev Crack)

Objetivo: Evolucionar LogiTrack desde una aplicación monolítica/por capas hacia un ecosistema orientado a eventos y preparado para escala, observabilidad y estándares de ingeniería a nivel equipo. Esta fase se alinea con los criterios Dev Crack (Crack 1–3) y consolida capacidades de microservicios, contratos entre servicios, monitoreo/observabilidad, y prácticas de calidad/DevOps.

Requerimientos (alineados a Crack):

1. Arquitectura Orientada a Eventos y Microservicios
   - Desacopla al menos un servicio adicional fuera del servicio principal (p. ej., NotificationService) que consuma el evento `PackageStateChanged`.
   - Define el esquema del evento como contrato estable (nombre del evento, versión, payload con IDs, nuevo estado, timestamp, Correlation ID).
   - Emplea un broker de mensajes (simulado o real). Se acepta: in-memory broker, Redis Streams, RabbitMQ, Kafka o equivalente en docker-compose.
   - Consistencia obligatoria (Transactional Outbox): Implementa el patrón Outbox transaccional para asegurar entrega de eventos cuando la escritura en la BD sea exitosa. Debe registrarse el evento en una tabla outbox dentro de la misma transacción y existir un publicador/poller que lo envíe de forma confiable (con reintentos e idempotencia básica).

2. Contratos y Compatibilidad entre Servicios
   - Establece contratos entre productor y consumidor del evento. Recomendado: manifest JSON o esquema (ej. AsyncAPI/Avro/JSON Schema) versionado en el repo.
   - Testing de contrato: implementa contract tests básicos o, en su defecto, tests automatizados que validen el formato y campos críticos del evento en ambos lados (productor/consumidor).

3. Observabilidad y Trazabilidad End-to-End
   - Logs estructurados (JSON) en los servicios involucrados. Incluye nivel, timestamp, service, Correlation ID, message y campos de dominio relevantes.
   - Genera y propaga un Correlation ID desde la entrada HTTP hasta el evento y su consumo en el servicio suscriptor.
   - Métricas/health checks: agrega endpoints o mecanismos mínimos de salud/ready (aunque sean simulados) y documenta métricas clave propuestas en EVALUATION.md.

4. Orquestación con Docker Compose y Consideraciones de Kubernetes
   - docker-compose.yml debe levantar: servicio principal, base de datos, broker de mensajes y NotificationService (o equivalente). Usa variables de entorno y redes.
   - En EVALUATION.md, describe cómo migrarías esta composición a Kubernetes (Deployments, Services, ConfigMaps/Secrets, readiness/liveness probes).

5. CQRS/read model endurecido para lecturas
   - Lleva el read model de Fase 2 a un estado listo para consumo intensivo. Opciones: materialized view, tabla desnormalizada, caché (Redis) o almacén no relacional.
   - Justifica consistencia eventual y sincronización del read model (ej. a partir de eventos o procesos de proyección) en EVALUATION.md.

6. Calidad de Código y Puertas de Calidad (Quality Gates)
   - Agrega herramientas o configuración para calidad: linters/checkers y cobertura de pruebas (ej. Checkstyle/ESLint, JaCoCo/Coverage). Puedes incluir reportes o configuración mínima y documentar su uso.
   - Define objetivos de calidad en EVALUATION.md (ej. cobertura en nuevo código > 80%, sin code smells críticos) y cómo los integrarías a un pipeline de CI/CD.

7. Testing Avanzado
   - Contratos: como en el punto 2. Si no usas una herramienta dedicada (Pact/SC Contract), crea pruebas automatizadas que serialicen/deserialicen el evento y validen su compatibilidad.
   - E2E conceptual: documenta en EVALUATION.md tu estrategia e2e (escenarios, herramientas, datos) para validar desde API hasta efectos en el consumidor.

8. Paradigmas y Reactividad (opcional pero valorado)
   - Implementa una API no bloqueante/reactiva o justifica por qué no es necesaria. Si la implementas, asegúrate de que la cadena I/O→DB→Broker sea asíncrona.

9. Propuesta Estratégica al CTO y Gestión del Cambio
   - En EVALUATION.md, presenta dos iniciativas técnicas con estimaciones de alto nivel, riesgos y métricas de éxito (ej. Outbox, API Gateway, observabilidad centralizada, migración a K8s). Incluye un plan por trimestres.

10. Autorización (RBAC) para operaciones sensibles
   - Implementa una regla de autorización donde solo un rol admin (obtenido de un JWT simulado o derivado del API Key) pueda ejecutar la transición de estado a `RETURNED`.
   - Debe existir evidencia mediante pruebas o colección de API que muestre denegación para no-admin y éxito para admin.

Entregables específicos de Fase 3
- Servicio principal + NotificationService (o equivalente) funcionando y comunicándose vía eventos.
- Esquema/contrato del evento versionado y pruebas de contrato o validaciones automatizadas.
- Logs estructurados JSON y Correlation ID propagado extremo a extremo.
- docker-compose.yml con app, DB, broker y servicio consumidor.
- Transactional Outbox implementado: tabla outbox, transacción con escritura de dominio + outbox, publicador/poller con reintentos e idempotencia básica; evidencia (tests o simulación de fallo de broker y posterior entrega).
- Read model/CQRS fortalecido e integrado a consultas de la API donde aplique.
- Autorización (RBAC) implementada: regla admin-only para transición a RETURNED con evidencia (tests o colección demostrando 403 para no-admin y 2xx para admin).
- Configuración de calidad (linters/cobertura) y documentación de quality gates y CI/CD conceptual.
- EVALUATION.md ampliado con observabilidad, CQRS/eventos, K8s conceptual, estrategia e2e y propuesta al CTO con estimaciones.

---


## Entregables

1.  **Código Fuente:** Un enlace a un repositorio Git público con un `README.md` que detalle la arquitectura y las instrucciones de ejecución.
2.  **Autoevaluación y Documentación de Diseño (EVALUATION.md):** Un archivo que responda a las justificaciones y preguntas planteadas en cada fase. Este documento es **crítico** para la evaluación.
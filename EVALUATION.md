# Autoevaluación de la Prueba Técnica v3.0

Este documento te guía para autoevaluarte de forma alineada con los requisitos y evidencias de cada fase descritas en docs/prueba_tecnica.md y con los criterios de evaluación (Essential, Expert y Crack).

---

## 1) Resumen de Cumplimiento por Fase
- Fase 1 (Dev Essential): [Sí/No] Comentarios breves.
- Fase 2 (Dev Expert): [Sí/No] Comentarios breves.
- Fase 3 (Dev Crack): [Sí/No] Comentarios breves.

---

## 2) Fase 1 – Núcleo del Dominio (Dev Essential)
Marca Sí/No y aporta breve evidencia (archivo/clase/test) y justificación.

1. POO básica y estructura del dominio
   - Clase Package con atributos requeridos y métodos coherentes (addLocation, changeStatus). [Sí/No]
   - Encapsulación y protección del estado interno. [Sí/No]
   Evidencia: ruta de archivo/clase.

2. Estructuras de datos adecuadas
   - Implementación de LocationHistory con colección apropiada y orden cronológico. [Sí/No]
   - Exposición de lecturas sin mutación indebida (copias defensivas u opciones equivalentes). [Sí/No]
   Evidencia: métodos y pruebas asociadas.

3. CRUD básico de Package
   - Operaciones Crear/Leer/Actualizar/Borrar mediante servicio/repositorio. [Sí/No]
   - Reglas de actualización y manejo de errores definidos. [Sí/No]
   Evidencia: servicio/repositorio y pruebas.

4. Consumo de APIs REST externas
   - Cliente REST (o mocks/stubs) con manejo de errores (timeouts, 4xx/5xx) y validación de respuesta. [Sí/No]
   - Pruebas unitarias que mockean la capa HTTP. [Sí/No]
   Evidencia: cliente/pruebas.

5. Gestión del ciclo de vida (máquina de estados)
   - Estados definidos y transiciones válidas. [Sí/No]
   - Diseño sin if/else o switch extensos (State/tabla de transiciones/polimorfismo). [Sí/No]
   Evidencia: diseño y tests de transiciones válidas/ inválidas.

6. Creación robusta de objetos
   - Builder/Factory (u enfoque equivalente) que evita constructores telescópicos. [Sí/No]
   - Validaciones básicas (ej. peso no negativo) e invariantes al crear. [Sí/No]
   Evidencia: patrón utilizado y pruebas.

7. Buenas prácticas y organización
   - Estructura de carpetas clara y convenciones de estilo. [Sí/No]
   - README básico y uso de gestor de dependencias (Gradle/Maven/etc.). [Sí/No]
   - Commits atómicos con mensajes claros. [Sí/No]
   Evidencia: README, build file, historial Git.

8. Pruebas unitarias
   - Caminos felices y escenarios borde (incluye CRUD y cliente REST con dobles). [Sí/No]
   - Nombres descriptivos y aislamiento (sin dependencias externas). [Sí/No]
   Evidencia: rutas de tests y breve resumen de casos.

Notas/Comentarios Fase 1:
- Decisiones clave de diseño del dominio y motivos.

---

## 3) Fase 2 – Aplicación y Patrones (Dev Expert)
Completa cada punto con Sí/No y breve justificación. Cuando aplique, referencia a EVALUATION.md esta misma sección para ampliar.

1. Arquitectura por capas (Clean Architecture)
   - Separación en dominio, aplicación (casos de uso) e infraestructura (controladores REST, persistencia, HTTP). [Sí/No]
   - Inversión de dependencias: la aplicación depende de puertos/ interfaces. [Sí/No]
   Evidencia: estructura de paquetes/módulos.

2. API REST (endpoints mínimos)
   - POST /packages, GET /packages/{id}, GET /packages/{id}/locations, PATCH /packages/{id}/status implementados. [Sí/No]
   - Manejo de errores 400/404/409/500 consistente y documentación (OpenAPI o Postman). [Sí/No]
   Evidencia: controladores, spec/colección.

   2.a. Validación de DTOs en la capa API (Infraestructura)
   - Uso de framework de validación (javax.validation/Pydantic/FluentValidation, etc.) para validar requests antes de llegar a aplicación/dominio; 400 con errores de campo claros. [Sí/No]
   - Reglas mínimas: recipient_name no vacío, dirección con formato/longitud mínima, dimensiones/peso positivos. [Sí/No]
   Evidencia: DTOs/anotaciones, handler de errores y tests o colección que muestre 400 ante entradas inválidas.

3. Persistencia relacional
   - Esquema Package (1) – LocationHistory (N) normalizado. [Sí/No]
   - Repositorios/ORM operativos (migraciones opcionales). [Sí/No]
   Evidencia: entidades, repos, scripts.

4. Patrones de diseño (obligatorios y justificados)
   - Creación: Builder/Factory aplicado. [Sí/No]
   - Estados: State/Estrategia/tabla de transiciones sin condicionales extensos. [Sí/No]
   - Justificación: problema, alternativas y razones de elección. [Sí/No]
   Explicación breve aquí (ampliar si es necesario):

5. Testing robusto
   - Pruebas de integración API→Aplicación→DB para crear/consultar/cambiar estado/consultar historial. [Sí/No]
   - Mocks adecuados para aislar dominio/casos de uso. [Sí/No]
   Evidencia: clases de test y cómo ejecutarlas.

6. Contenerización y ejecución
   - docker-compose.yml para app + DB y variables de entorno. [Sí/No]
   - Instrucciones en README para local y Docker. [Sí/No]
   Evidencia: archivos y comandos.

7. Seguridad básica implementada + operabilidad
   - Implementar validación de API Key (cabecera X-API-KEY) que proteja al menos POST /packages; devolver 401/403 ante ausencia/clave inválida. [Sí/No]
   - Operabilidad (conceptual): describir evolución a JWT/OAuth y logging/trazabilidad (Correlation ID). [Sí/No]
   Evidencia: código del middleware/interceptor/filtro, configuración de la clave y tests o colección mostrando 401/403 y 2xx.

8. Desacoplamiento y asincronismo (eventos)
   - Publicación de PackageStateChanged y consumidor simulado/no bloqueante. [Sí/No]
   - Justificación de patrón Outbox u opciones en esta sección. [Sí/No]
   Evidencia: publisher/handler y pruebas.

9. Escalabilidad de lecturas (CQRS / read model)
   - Vista de lectura optimizada e integrada en consultas. [Sí/No]
   - Justificación de la tecnología elegida. [Sí/No]
   Evidencia: esquema/tabla/materialized view o almacén alterno.

10. Visión estratégica (para el CTO)
   - Dos iniciativas técnicas para 12 meses: problema de negocio, riesgos e impacto. [Sí/No]
   Respuesta:

11. Control de concurrencia (Optimistic Locking)
   - Campo version en Package y validación de versión en actualizaciones; 409 en conflicto. [Sí/No]
   - Prueba de integración que demuestre fallo al intentar actualizar con versión obsoleta. [Sí/No]
   Evidencia: entidad/esquema, endpoint/servicio que verifica versión y test.

Notas/Comentarios Fase 2:
- Trade-offs técnicos y decisiones clave.

---

## 4) Fase 3 – Escala y Arquitectura Avanzada (Dev Crack)
Indica Sí/No, referencia a artefactos y explica brevemente.

1. Arquitectura orientada a eventos y microservicios
   - Servicio adicional (ej. NotificationService) que consume PackageStateChanged. [Sí/No]
   - Broker (simulado o real) operativo. [Sí/No]
   Evidencia: código/compose.

2. Contratos y compatibilidad entre servicios
   - Contrato del evento versionado (AsyncAPI/JSON Schema/Avro). [Sí/No]
   - Pruebas de contrato o validaciones automatizadas. [Sí/No]
   Evidencia: esquemas/tests.

3. Observabilidad y trazabilidad end-to-end
   - Logs JSON estructurados con Correlation ID propagado productor→consumidor. [Sí/No]
   - Health/ready y métricas clave documentadas. [Sí/No]
   Evidencia: configuración/logs/README.

4. Orquestación con Docker Compose (+ consideraciones K8s)
   - compose para app, DB, broker y NotificationService. [Sí/No]
   - Descripción de migración a K8s (Deployments, Services, ConfigMaps/Secrets, probes). [Sí/No]
   Evidencia: compose y sección conceptual.

5. CQRS/read model endurecido
   - Materialized view/tabla desnormalizada/cache o almacén no relacional para altas lecturas. [Sí/No]
   - Justificación de consistencia eventual y proyección. [Sí/No]
   Evidencia: esquema/código/README.

6. Calidad de código y quality gates
   - Linters/checkers y cobertura (JaCoCo u otro). [Sí/No]
   - Objetivos de calidad definidos e integración CI/CD conceptual. [Sí/No]
   Evidencia: configs/reportes y explicación.

7. Testing avanzado
   - Contratos implementados o validados automáticamente. [Sí/No]
   - Estrategia e2e documentada (escenarios, herramientas, datos). [Sí/No]
   Evidencia: tests/documento.

8. API reactiva (opcional)
   - Cadena no bloqueante I/O→DB→Broker o justificación de no aplicarlo. [Sí/No]
   Evidencia: framework/config o explicación.

9. Propuesta estratégica al CTO (estimaciones y plan)
   - Dos iniciativas con estimaciones, riesgos, métricas y plan por trimestres. [Sí/No]
   Resumen:

10. Autorización (RBAC) para operaciones sensibles
   - Solo un rol admin (obtenido de un JWT simulado o derivado del API Key) puede ejecutar la transición de estado a RETURNED. [Sí/No]
   Evidencia: regla de autorización en la capa de aplicación/controlador, pruebas o colección con ejemplo que muestre denegación para rol no-admin y éxito para admin.

11. Consistencia atómica (Transactional Outbox implementado)
   - Outbox transaccional: registro del evento en la misma transacción que la escritura de dominio; publicador/poller que envía al broker con reintentos e idempotencia. [Sí/No]
   - Demostración de resiliencia: simular fallo de broker y confirmar envío posterior desde outbox. [Sí/No]
   Evidencia: esquema/tabla outbox, código del poller/publicador y pruebas o pasos reproducibles.

Notas/Comentarios Fase 3:
- Riesgos y mitigaciones clave.

---

## 5) Uso de IA (todas las fases)
- ¿Cómo y para qué la usaste? ¿Qué aceptaste o descartaste y por qué? Ejemplos concretos. 
- Casos avanzados (exploración de patrones, refactor inmutable, generación de variantes y comparación de trade-offs).

---

## 6) Trade-Offs y Mejoras Futuras
- Compromisos por límite de tiempo y su impacto.
- Mejoras priorizadas (validaciones, outbox, observabilidad, seguridad, performance, CI/CD).

---

## 7) Matriz rápida de cumplimiento (marcar Sí/No)
- F1: Dominio (POO/estructuras) | CRUD | Cliente REST | Máquina de estados | Builder/Factory | Tests | README/Commits.
- F2: Clean Architecture | API endpoints + errores + doc | Persistencia relacional | Patrones justificados | Integración API→DB | Docker Compose | Eventos | Read model | Visión estratégica | Seguridad/Operabilidad.
- F3: Microservicio + broker | Contratos + contract testing | Logs JSON + Correlation ID | Compose multi-servicio | Transactional Outbox | CQRS endurecido | Linters/Cobertura | Testing avanzado | API reactiva (opt) | Propuesta CTO.
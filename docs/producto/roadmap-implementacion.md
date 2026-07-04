# Roadmap de Implementacion de GameKit

Este roadmap define el orden recomendado para construir GameKit sin perder tiempo en modulos aislados o features que dependen de bases todavia inexistentes.

La estrategia es **cross-server-first**: GameKit debe nacer pensando en la topologia real de produccion, con lobby server, arena server, Redis, admissions y Velocity central desde el inicio. Los tests pueden usar fakes, pero los contratos no deben asumir que todo corre en un solo servidor.

## Estado Actual

Segun el avance actual del codebase, el desarrollo funcional esta completado hasta la **Fase 7: Queue Casual**.

Ademas, el layout Gradle ya fue alineado con la arquitectura final de runtimes:

- `gamekit-paper` como base Paper compartida.
- `gamekit-lobby-paper` como runtime Paper de lobby.
- `gamekit-arena-paper` como runtime Paper de arena/match.
- `gamekit-admin-paper` como superficie Paper de administracion transversal para cualquier parte donde se quiera utilizar.
- `gamekit-velocity-plugin` como unico modulo instalable del plano Velocity.

Esto no significa que los runtimes Paper ya tengan funcionalidad completa. Significa que la estructura donde se implementaran las siguientes fases ya esta preparada.

## Principios de Orden

- Construir por verticales verificables, no por modulos sueltos.
- Validar temprano Redis, routing, admissions y Velocity para no descubrir tarde errores de arquitectura.
- Mantener dominio sin Paper/Bukkit y probarlo con JUnit puro.
- Mantener Paper separado por runtime: lobby, arena/match y admin transversal.
- No crear modulos por cada dependencia tecnica. Cloud, BoostedYAML, zMenu, scoreboard-library, NetworkPlayerSettings, PlaceholderAPI y FAWE viven dentro del runtime que realmente los usa.
- Crear fakes desde el inicio para testear sin servidores reales, pero modelando conceptos reales: `serverId`, admission, routing, clock, registry y stores.
- Cada fase debe cerrar con tests o una prueba ejecutable que demuestre comportamiento real.

## Fase 1: Fundacion de Dominio

**Estado:** completada.

**Objetivo:** crear el lenguaje comun que todos los modulos van a usar.

Incluye:

- `gamekit-core`.
- IDs publicos: `GameId`, `VariantId`, `QueueId`, `ArenaId`, `MatchId`, `ServerId`.
- Results, decisions y reject reasons.
- Clock abstraction y fake clock.
- Eventos base y exceptions base.
- `GameDefinition`, `MatchVariant`, `TeamSpec`, `MatchKind`.

**Por que primero:** si los IDs, results y definitions no quedan claros al inicio, cada modulo inventa su propio lenguaje y despues el refactor es caro.

## Fase 2: Testkit 

**Estado:** completada.

**Objetivo:** permitir tests rapidos y deterministas desde el primer modulo.

Incluye:

- Fake clock.
- Fake event bus.
- Fake repositories/stores.
- Fake server registry.
- Fake transfer gateway.
- Fixtures base.

**Por que ahora:** GameKit va a tener countdowns, TTLs, reconnect windows, cooldowns y expiraciones. Sin fakes desde el inicio, los tests terminan usando sleeps o infraestructura real innecesaria.

## Fase 3: Session y Active Play

**Estado:** completada.

**Objetivo:** saber que esta haciendo cada jugador dentro de una modalidad.

Incluye:

- `GameSession`.
- Estados de sesion.
- Validacion de transiciones.
- `ActivePlaySession`.
- Resolver base de intencion de juego.

**Por que antes de queue:** ningun jugador debe entrar a queue, waiting room, match o spectator si ya tiene actividad activa incompatible.

## Fase 4: Redis Runtime 

**Estado:** completada.

**Objetivo:** tener la base runtime distribuida desde temprano.

Incluye:

- Integracion CraftKit Redis.
- Redis key conventions.
- Serializacion simple.
- Leases/locks s.
- Stores runtime para estado temporal.

**Por que aqui:** routing, admissions, arena reservations y active play distribuido dependen de Redis desde el inicio.

## Fase 5: Network Core

**Estado:** completada.

**Objetivo:** modelar servidores, destinos y admisiones cross-server.

Incluye:

- Server registry.
- Server roles y heartbeats.
- Admission requests con TTL.
- Match location registry.
- Routing contracts.
- Fallback contracts.

**Por que antes de arena:** una arena vive en un server concreto. No se puede asignar ni transferir jugadores correctamente si todavia no existe registry, admission y routing.

## Fase 6: Arena Registry y Allocation

**Estado:** completada.

**Objetivo:** reservar donde se juega.

Incluye:

- `ArenaDefinition`.
- `ArenaSlot`.
- Estados de arena.
- Tags y requirements.
- Reserva distribuida con Redis.

**Por que antes de matchmaking real:** queue/matchmaking solo produce candidatos; arena allocation decide donde se juega.

## Fase 7: Queue Casual

**Estado:** completada.

**Objetivo:** convertir una intencion de jugar en un flujo casual resoluble.

Incluye:

- `QueueDefinition`.
- `QueueTicket`.
- Solo player tickets iniciales.
- Casual queue simple.
- Resolver: reject, join queue, route existing room, reserve arena.

**Por que casual primero:** ranked depende de seasons, rating, integrity y penalties. Casual permite validar el flujo sin cargar complejidad competitiva todavia.

## Fase 8: Paper Runtime Foundation

**Estado:** siguiente bloque funcional.

**Objetivo:** conectar dominio con Paper respetando los boundaries actuales de runtime.

Incluye:

- `gamekit-paper` como base Paper compartida .
- `gamekit-lobby-paper` como runtime de lobby.
- `gamekit-arena-paper` como runtime de arena/match.
- Scheduler adapter.
- Listener registration handles.
- Player/world operations comunes.
- Runtime resources cerrables.
- Configuracion comun de runtime con BoostedYAML donde corresponda.
- Integracion inicial con dependencias oficiales dentro del runtime que las usa.
- Publicacion Maven Local validada.

**Por que aqui:** ya existe dominio suficiente para que Paper sea adaptador real, no lugar donde se improvisa gameplay. Esta fase prepara la integracion real sin mezclar lobby, arena/match y admin transversal.

**Criterio de cierre:** los plugins consumidores pueden componer explicitamente los runtimes Paper base sin cargar responsabilidades que no usan, y el build/publicacion local sigue pasando.

## Fase 9: Velocity Central 

**Objetivo:** ejecutar transferencias reales entre lobby server y arena server.

Incluye:

- `gamekit-velocity-plugin` como unico modulo Velocity instalable.
- Main class y metadata Velocity.
- Wiring de lifecycle Velocity.
- Admission lookup.
- Transfer executor.
- Fallback basico.
- Coordinacion con `gamekit-network` y stores runtime.
- Jar final en `target/`.

**Por que temprano:** GameKit es cross-server-first. Si Velocity se deja para el final, podemos disenar APIs que funcionan localmente pero fallan en produccion.

**Criterio de cierre:** una transferencia cross-server puede ser ejecutada por el plugin Velocity central usando una admission verificable, sin que Paper dependa de APIs Velocity.

## Fase 10: Waiting Room

**Objetivo:** admitir jugadores en el arena server y preparar el match.

Incluye:

- Admission consumption desde `gamekit-arena-paper`.
- Waiting room states.
- Min/max players.
- Countdown.
- Cancelacion de countdown.
- Ready-to-start event.
- Cleanup de room.
- Registro runtime de rooms activas.

**Por que despues de Velocity:** waiting room debe validar por que llego el jugador. El arena server nunca debe adivinar la razon de entrada.

**Criterio de cierre:** un jugador transferido con admission valida entra a waiting room; una admission ausente, expirada o incompatible se rechaza de forma auditable.

## Fase 11: Match Lifecycle 

**Objetivo:** crear, iniciar, terminar y limpiar una partida.

Incluye:

- `Match`.
- Estados de match.
- Participants.
- Teams basicos.
- `MatchScope`.
- `endMatch` idempotente.
- Resultado simple.
- Cleanup de recursos temporales.

**Por que antes de experience avanzada:** primero debe existir lifecycle confiable. Visuales y tasks sin scope seguro generan leaks.

**Criterio de cierre:** una partida puede iniciar, terminar una sola vez aunque reciba señales duplicadas y cerrar sus recursos temporales.

## Fase 12: Primera Vertical Cross-Server

**Objetivo:** demostrar que GameKit sirve para una modalidad real.

Flujo esperado:

1. jugador en lobby de modalidad;
2. intencion casual de jugar;
3. active play resolver;
4. queue casual;
5. arena reservation;
6. admission request;
7. Velocity transfer;
8. arena server consume admission;
9. waiting room;
10. countdown;
11. match starting;
12. match running;
13. end simple;
14. cleanup scope;
15. return lobby.

**Por que es el primer gran hito:** valida la cadena completa antes de invertir tiempo en ranked, leaderboards, admin o spectator avanzado.

**Criterio de cierre:** el flujo completo se puede demostrar con fakes y, cuando aplique, con una prueba ejecutable de integracion local.

## Fase 13: Experience  en Runtimes Paper

**Objetivo:** dar UX comun sin sobrecargar el sistema ni crear un modulo de experience prematuro.

Incluye:

- Experience scopes dentro de `gamekit-lobby-paper` y `gamekit-arena-paper`.
- Feedback integration.
- Scoreboard-library donde el runtime lo necesite.
- Titles/actionbars basicos.
- Recursos visuales dentro de scopes cerrables.
- Politicas de refresh que eviten updates excesivos.

**Por que despues de la vertical:** asi se disena experience contra necesidades reales, no contra suposiciones. Si mas adelante aparecen contratos visuales verdaderamente transversales, se puede extraer un modulo dedicado.

**Criterio de cierre:** los recursos visuales se crean, actualizan y cierran desde el scope correcto sin leaks ni refresh innecesario.

## Fase 14: Persistence e Idempotencia

**Objetivo:** cerrar matches sin perder verdad durable.

Incluye:

- DB contracts.
- Processing ledger.
- Projection outbox.
- Match record persistence.
- Idempotency tests.
- Retryable failures.

**Por que antes de stats/rating:** stats, rewards, SR y leaderboards necesitan una base durable confiable.

**Criterio de cierre:** un match terminado produce un registro durable idempotente, y los fallos posteriores pueden quedar retryable sin bloquear retorno de jugadores.

## Fase 15: Arena Reset Real con FAWE

**Objetivo:** reutilizar arenas de forma segura en produccion.

Incluye:

- Template reset.
- Integracion FAWE dentro de `gamekit-arena-paper`.
- Estrategia de fallo a `Disabled`.
- Observability de reset.
- Proteccion contra reutilizar arenas no limpias.

**Por que aqui:** el reset real depende de lifecycle, cleanup e idempotencia. `gamekit-arena` mantiene contratos limpios; FAWE pertenece al runtime Paper de arena.

**Criterio de cierre:** una arena pasa de Playing a Resetting y vuelve a Available solo si el reset termina correctamente; si falla, queda Disabled y observable.

## Fase 16: Stats y Rewards

**Objetivo:** agregar progresion casual confiable.

Incluye:

- `gamekit-progression`.
- Stat buffers.
- Stats por match.
- Totales por jugador/scope.
- Reward ledger.
- Grants idempotentes.

**Por que antes de ranked:** aplica a mas flujos y tiene menos riesgo competitivo que rating/SR.

**Criterio de cierre:** stats y rewards casuales se procesan de forma idempotente y no contaminan el match lifecycle.

## Fase 17: Season Context, Ranked y Rating

**Objetivo:** habilitar competitivo correctamente.

Incluye:

- Season context sin modulo Gradle dedicado inicialmente.
- Active season resolver cuando ranked/progression lo requiera.
- Ranked requires active season.
- Ranked queue constraints.
- Rating groups.
- SR ledger.

**Por que despues de persistence:** ranked sin ledger, season context y persistencia idempotente seria fragil e injusto.

**Criterio de cierre:** ranked no puede operar sin contexto competitivo valido, y cada cambio de SR queda auditado con ledger.

## Fase 18: Leaderboards e Integrity

**Objetivo:** hacer visible y protegible el competitivo.

Incluye:

- Redis live projections.
- DB historical snapshots.
- Leaderboard eligibility.
- Dodge/abandon.
- Cooldowns.
- Penalties.
- Flags y audit competitivo.

**Por que aqui:** leaderboards e integrity necesitan datos reales de ranked, SR, season context y match history.

**Criterio de cierre:** un jugador puede tener progression/rating sin ser necesariamente elegible para leaderboards, y las decisiones sensibles quedan auditadas.

## Fase 19: Admin y Operacion

**Objetivo:** operar GameKit sin tocar archivos ni base de datos manualmente.

Incluye:

- `gamekit-admin-paper` para administracion transversal.
- Operaciones locales en `gamekit-lobby-paper` y `gamekit-arena-paper` cuando pertenecen a un runtime concreto.
- Arena drafts.
- Validate/publish.
- Server status.
- Queue status.
- Active matches.
- Processing retry.
- Season operations cuando el contexto competitivo ya exista.
- Integrity review.
- Cloud commands y menus zMenu dentro del runtime que los usa.

**Por que tarde:** admin debe administrar sistemas reales, no pantallas sobre funcionalidades incompletas. Separar runtime-local de transversal evita convertir admin en un contenedor generico de comandos con permisos.

**Criterio de cierre:** las operaciones administrativas criticas son auditables, tienen permisos claros y no editan runtime como si fuera configuracion durable.

## Fase 20: Spectator Avanzado y Hardening

**Objetivo:** cerrar produccion.

Incluye:

- Spectator externo.
- Staff spectator.
- Eliminated player spectator.
- Ranked spectator restrictions.
- Visibility/chat scopes.
- Observability completa.
- Contract tests.
- Docs de integracion.
- Examples de consumo.

**Por que al final:** spectator toca match, teams, visibility, chat, ranked integrity y experience. Es transversal y conviene implementarlo con las bases ya estables.

**Criterio de cierre:** spectator respeta restricciones competitivas, visibility/chat scopes y cleanup seguro.

## Hitos de Entrega

| Hito | Resultado |
| --- | --- |
| 1 | Dominio base + testkit . |
| 2 | Session + active play testeable. |
| 3 | Redis + network + admissions. |
| 4 | Arena allocation + queue casual. |
| 5 | Paper runtime foundation + Velocity . |
| 6 | Waiting room + match lifecycle. |
| 7 | Primera vertical cross-server completa. |
| 8 | Experience  en runtimes Paper. |
| 9 | Persistence + FAWE arena reset real. |
| 10 | Stats + rewards. |
| 11 | Ranked + SR + leaderboards + integrity. |
| 12 | Admin + spectator avanzado + hardening. |

## Regla Final

Cada fase debe cerrar con tests o una prueba ejecutable que demuestre comportamiento real. Si una fase no puede verificarse, todavia no esta terminada.

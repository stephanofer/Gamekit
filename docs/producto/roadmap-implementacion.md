    # Roadmap de Implementacion de GameKit

Este roadmap define el orden recomendado para construir GameKit sin perder tiempo en modulos aislados o features que dependen de bases todavia inexistentes.

La estrategia es **cross-server-first**: GameKit debe nacer pensando en la topologia real de produccion, con lobby server, arena server, Redis, admissions y Velocity central desde el inicio. Los tests pueden usar fakes, pero los contratos no deben asumir que todo corre en un solo servidor.

## Principios de Orden

- Construir por verticales verificables, no por modulos sueltos.
- Validar temprano Redis, routing, admissions y Velocity para no descubrir tarde errores de arquitectura.
- Mantener dominio sin Paper/Bukkit y probarlo con JUnit puro.
- Crear fakes desde el inicio para testear sin servidores reales, pero modelando conceptos reales: `serverId`, admission, routing, clock y registry.

## Fase 1: Fundacion de Dominio

**Objetivo:** crear el lenguaje comun que todos los modulos van a usar.

Incluye:

- `gamekit-core`.
- IDs publicos: `GameId`, `VariantId`, `QueueId`, `ArenaId`, `MatchId`, `ServerId`.
- Results, decisions y reject reasons.
- Clock abstraction y fake clock.
- Eventos base y exceptions base.
- `GameDefinition`, `MatchVariant`, `TeamSpec`, `MatchKind`.

**Por que primero:** si los IDs, results y definitions no quedan claros al inicio, cada modulo inventa su propio lenguaje y despues el refactor es caro.

## Fase 2: Testkit Minimo

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

**Objetivo:** saber que esta haciendo cada jugador dentro de una modalidad.

Incluye:

- `GameSession`.
- Estados de sesion.
- Validacion de transiciones.
- `ActivePlaySession`.
- Resolver base de intencion de juego.

**Por que antes de queue:** ningun jugador debe entrar a queue, waiting room, match o spectator si ya tiene actividad activa incompatible.

## Fase 4: Redis Runtime Minimo

**Objetivo:** tener la base runtime distribuida desde temprano.

Incluye:

- Integracion CraftKit Redis.
- Redis key conventions.
- Serializacion simple.
- Leases/locks minimos.
- Stores runtime para estado temporal.

**Por que aqui:** routing, admissions, arena reservations y active play distribuido dependen de Redis desde el inicio.

## Fase 5: Network Core

**Objetivo:** modelar servidores, destinos y admisiones cross-server.

I arena vive en un server concreto. No se puede asignar ni transferir jugadores correctamente si todavia no existe registry/admission/routing.ncluye:

- Server registry.
- Server roles y heartbeats.
- Admission requests con TTL.
- Match location registry.
- Routing contracts.
- Fallback contracts.

**Por que antes de arena:** una

## Fase 6: Arena Registry y Allocation

**Objetivo:** reservar donde se juega.

Incluye:

- `ArenaDefinition`.
- `ArenaSlot`.
- Estados de arena.
- Tags y requirements.
- Reserva distribuida con Redis.

**Por que antes de matchmaking real:** queue/matchmaking solo produce candidatos; arena allocation decide donde se juega.

## Fase 7: Queue Casual Basica

**Objetivo:** convertir una intencion de jugar en un flujo casual resoluble.

Incluye:

- `QueueDefinition`.
- `QueueTicket`.
- Solo player tickets iniciales.
- Casual queue simple.
- Resolver: reject, join queue, route existing room, reserve arena.

**Por que casual primero:** ranked depende de seasons, rating, integrity y penalties. Casual permite validar el flujo sin cargar complejidad competitiva todavia.

## Fase 8: Paper Adapter Minimo

**Objetivo:** conectar dominio con Paper sin meter logica de negocio en Paper.

Incluye:

- Scheduler adapter.
- Listener registration handles.
- Player/world operations minimas.
- Logica comun de lobby para lobby servers de modalidad.
- Metadata `paper-plugin.yml` para ejemplos consumidores.
- Publicacion Maven Local validada.

**Por que aqui:** ya existe dominio suficiente para que Paper sea adaptador real, no lugar donde se improvisa gameplay.

## Fase 9: Velocity Central Minimo

**Objetivo:** ejecutar transferencias reales entre lobby server y arena server.

Incluye:

- `gamekit-velocity`.
- `gamekit-velocity-plugin`.
- Main class y metadata Velocity.
- Admission lookup.
- Transfer executor.
- Fallback basico.
- Jar final en `target/`.

**Por que temprano:** GameKit es cross-server-first. Si Velocity se deja para el final, podemos disenar APIs que funcionan localmente pero fallan en produccion.

## Fase 10: Waiting Room

**Objetivo:** admitir jugadores en el arena server y preparar el match.

Incluye:

- Admission consumption.
- Waiting room states.
- Min/max players.
- Countdown.
- Cancelacion de countdown.
- Ready-to-start event.
- Cleanup de room.

**Por que despues de Velocity:** waiting room debe validar por que llego el jugador. El arena server nunca debe adivinar la razon de entrada.

## Fase 11: Match Lifecycle Minimo

**Objetivo:** crear, iniciar, terminar y limpiar una partida.

Incluye:

- `Match`.
- Estados de match.
- Participants.
- Teams basicos.
- `MatchScope`.
- `endMatch` idempotente.
- Resultado simple.

**Por que antes de experience avanzada:** primero debe existir lifecycle confiable. Visuales y tasks sin scope seguro generan leaks.

## Fase 12: Primera Vertical Cross-Server

**Objetivo:** demostrar que GameKit sirve para una modalidad real.

Flujo esperado:

1. jugador en lobby de modalidad;
2. click casual;
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

## Fase 13: Experience Minima

**Objetivo:** dar UX comun sin sobrecargar el sistema.

Incluye:

- Experience scopes.
- Feedback integration.
- Scoreboard contracts.
- Scoreboard-library adapter.
- Titles/actionbars basicos.
- Recursos visuales dentro de scopes.

**Por que despues de la vertical:** asi se disena experience contra necesidades reales, no contra suposiciones.

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

## Fase 15: Arena Reset Real

**Objetivo:** reutilizar arenas de forma segura en produccion.

Incluye:

- Template reset.
- Estrategia de fallo a `Disabled`.
- Observability de reset.
- Adapter WorldEdit/FAWE solo si hace falta.

**Por que aqui:** el reset real depende de lifecycle, cleanup e idempotencia..

## Fase 16: Stats y Rewards

**Objetivo:** agregar progresion casual confiable.

Incluye:

- Stat buffers.
- Stats por match.
- Totales por jugador/scope.
- Reward ledger.
- Grants idempotentes.

**Por que antes de ranked:** aplica a mas flujos y tiene menos riesgo competitivo que rating/SR.

## Fase 17: Seasons, Ranked y Rating

**Objetivo:** habilitar competitivo correctamente.

Incluye:

- Seasons manuales.
- Active season resolver.
- Ranked requires active season.
- Ranked queue constraints.
- Rating groups.
- SR ledger.

**Por que despues de persistence:** ranked sin ledger, season y persistencia idempotente seria fragil e injusto.

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

**Por que aqui:** leaderboards e integrity necesitan datos reales de ranked, SR, seasons y match history.

## Fase 19: Admin y Operacion

**Objetivo:** operar GameKit sin tocar archivos ni base de datos manualmente.

Incluye:

- Arena drafts.
- Validate/publish.
- Server status.
- Queue status.
- Active matches.
- Processing retry.
- Season management.
- Integrity review.
- Cloud commands y menus zMenu donde aporten.

**Por que tarde:** admin debe administrar sistemas reales, no pantallas sobre funcionalidades incompletas.

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

## Hitos de Entrega

| Hito | Resultado |
| --- | --- |
| 1 | Dominio base + testkit minimo. |
| 2 | Session + active play testeable. |
| 3 | Redis + network + admissions. |
| 4 | Arena allocation + queue casual. |
| 5 | Paper + Velocity minimo. |
| 6 | Waiting room + match lifecycle. |
| 7 | Primera vertical cross-server completa. |
| 8 | Experience minima. |
| 9 | Persistence + arena reset real. |
| 10 | Stats + rewards. |
| 11 | Ranked + SR + leaderboards + integrity. |
| 12 | Admin + spectator avanzado + hardening. |

## Regla Final

Cada fase debe cerrar con tests o una prueba ejecutable que demuestre comportamiento real. Si una fase no puede verificarse, todavia no esta terminada.

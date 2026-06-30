# PRD Técnico — GameKit para HERA Network

**Estado:** diseño base validado, sujeto a decisiones de implementación  
**Proyecto:** HERA Network / GameKit  
**Formato:** documento de producto técnico y arquitectura funcional  
**Audiencia:** equipo de desarrollo, arquitectura, administración técnica y diseño de modalidades

---

## 0. Decisión arquitectónica inicial: Manual Context Assembly + Explicit Runtime Handles

GameKit adoptará el enfoque **Manual Context Assembly + Explicit Runtime Handles** para la integración con plugins consumidores.

Esto significa que el plugin de modalidad construye explícitamente el contexto que necesita, decide qué módulos usa, pasa sus dependencias de forma visible y cierra los recursos que recibió. GameKit no debe actuar como una caja negra que instala todo, descubre todo o controla silenciosamente el lifecycle del plugin consumidor.

La decisión se toma para proteger tres objetivos principales:

- mantener una DX clara y auditable;
- evitar magia de lifecycle difícil de depurar;
- permitir que cada modalidad componga solo las piezas que necesita sin cargar módulos innecesarios.

### 0.1 Qué no queremos

GameKit no debe convertirse en:

- un bootstrap gigante tipo `GameKit.bootstrap(plugin).everything().build()`;
- una API donde cada módulo requiera llamadas opacas tipo `install()` para modificar el plugin por detrás;
- un sistema que registre listeners, comandos, tasks, scoreboards, Redis subscriptions o recursos temporales sin que el consumidor entienda qué se creó;
- un plugin externo instalado en `/plugins` con lifecycle propio separado de la modalidad;
- una capa que envuelva innecesariamente APIs potentes como Adventure, zMenu, cloud, CraftKit o scoreboard-library cuando exponerlas directamente sea más claro y útil.

Estas formas parecen cómodas al principio, pero vuelven el sistema difícil de auditar, difícil de testear y peligroso ante fallos de cierre, reloads, dependencias opcionales o integración con varios plugins de modalidad.

### 0.2 Enfoque esperado

El plugin consumidor debe ser dueño de su lifecycle.

GameKit debe entregar:

- módulos componibles;
- contratos explícitos;
- servicios sin estado global innecesario;
- handles cerrables para recursos runtime;
- scopes para recursos temporales;
- adaptadores Paper/Velocity delgados;
- errores claros cuando falta una dependencia obligatoria.

El consumidor debe decidir:

- qué módulos construye;
- en qué orden inicializa;
- qué dependencias runtime exige;
- cuándo registra listeners/comandos/menús;
- cuándo cierra recursos.

### 0.3 Ejemplo conceptual

El siguiente ejemplo no define una API final. Solo ilustra la estrategia de integración esperada:

```java
public final class ExampleModePlugin extends JavaPlugin {

    private RuntimeResources resources;

    @Override
    public void onEnable() {
        this.resources = RuntimeResources.create();

        PlatformAdapter platform = PaperPlatformAdapter.create(this);
        RuntimeStore store = resources.add(RuntimeStore.create(loadRuntimeConfig()));
        DurableStore durableStore = resources.add(DurableStore.create(loadDatabaseConfig()));

        PlayerSessions sessions = PlayerSessions.create(platform.clock(), platform.events());
        QueueModule queues = QueueModule.create(sessions, store);
        ExperienceModule experience = ExperienceModule.create(platform, sessions);

        resources.add(platform.registerListeners(sessions, queues, experience));
    }

    @Override
    public void onDisable() {
        if (this.resources != null) {
            this.resources.close();
        }
    }
}
```

La idea central es que la construcción sea explícita, legible y fácil de revisar. Si un recurso se crea, debe quedar claro quién lo posee y quién lo cierra.

### 0.4 Regla de ownership

Todo recurso runtime que GameKit cree o registre debe tener una política de ownership clara.

Ejemplos:

- si GameKit crea una conexión, debe devolver un handle cerrable;
- si GameKit registra listeners, debe devolver un registro cerrable o estar asociado a un scope;
- si GameKit crea scoreboards, bossbars, tasks o subscriptions, deben quedar dentro de un scope cerrable;
- si el consumidor pasa un recurso externo ya creado, GameKit no debe cerrarlo salvo que el contrato lo diga explícitamente.

Esta regla evita leaks y mantiene el control en manos del plugin consumidor sin renunciar a seguridad operativa.

## 1. Propósito del documento

Este PRD formaliza el diseño completo de **GameKit**, la librería interna que servirá como base común para todas las modalidades competitivas que se desarrollen unica y exclusivamente de HERA Network.

El objetivo es que el equipo pueda iniciar desarrollo con una referencia única, clara y accionable sobre:

- responsabilidades de GameKit;
- módulos funcionales;
- flujos principales de jugador;
- reglas de cola, waiting room, match, reconnect, spectator, stats, SR, rewards y leaderboards;
- administración in-game;
- Redis runtime;
- persistencia durable;
- observabilidad;
- testing;
- naming conventions;
- política de errores y custom exceptions.

Este documento evita bajar a implementación clase por clase. La intención es dejar validada la **base conceptual, funcional y arquitectónica** para que luego cada módulo se desarrolle con detalle técnico propio.

---

## 2. Visión de GameKit

GameKit es una librería interna para construir modalidades competitivas de Minecraft dentro de HERA Network.

HERA busca revivir modalidades altamente populares y adictivas —BedWars, SkyWars, Hunger Games, Pillars y otras— llevándolas a versiones modernas de Minecraft, con enfoque competitivo, progresión, estadísticas, tiers, leaderboards y experiencia profesional.

GameKit existe para que cada modalidad no tenga que reinventar:

- sesión de jugador dentro de modalidad;
- lobby de modalidad;
- queue;
- waiting room;
- matchmaking;
- arena allocation;
- match lifecycle;
- equipos;
- spectator;
- respawn/eliminación;
- finalización/post-game;
- stats;
- rating/SR;
- rewards;
- leaderboards;
- seasons;
- routing entre servidores;
- integridad competitiva;
- observabilidad;
- testing común.

La modalidad mantiene su identidad, reglas y gameplay. GameKit provee el marco operativo profesional.

---

## 3. Principios no negociables

### 3.1 GameKit administra estructura; la modalidad decide gameplay

GameKit no debe saber cómo se juega BedWars, SkyWars o cualquier modalidad concreta.

GameKit administra:

- estados;
- flujos;
- lifecycle;
- participantes;
- arenas;
- colas;
- waiting rooms;
- equipos;
- spectating;
- persistencia del resultado;
- coordinación runtime;
- integridad competitiva.

La modalidad decide:

- reglas de victoria;
- reglas de muerte;
- reglas de respawn;
- objetivos;
- kits;
- upgrades;
- generadores;
- cosméticos;
- textos concretos;
- menús concretos;
- scoreboard concreto;
- recompensas específicas;
- comportamiento visual final.

### 3.2 GameKit no es un plugin suelto en `/plugins`

GameKit será una librería interna consumida por plugins de modalidad mediante Gradle y Shadow.

El plugin de modalidad debe sentir que GameKit es parte de su propio código, no un plugin externo genérico.

### 3.3 Redis desde el inicio

Aunque la topología inicial tenga un lobby server y un arena server por modalidad, GameKit debe usar Redis desde el inicio para coordinación runtime.

Redis se usa para:

- server registry;
- heartbeats;
- arena snapshots;
- distributed arena reservations;
- pending admission requests;
- match location registry;
- active play sessions;
- reconnect routing;
- cooldowns rápidos;
- ventanas recientes de integridad competitiva;
- leaderboards vivos como proyección.

Redis no es la fuente de verdad histórica.

### 3.4 Database es la verdad durable

La base de datos guarda lo que no se puede perder:

- stats oficiales;
- rating/SR;
- rewards ledger;
- audit log;
- competitive records;
- season snapshots;
- configs publicadas si aplica;
- historial de rating;
- historial de rewards;
- historia de partidas
- processing ledger.
- y muchisimas cosas mas 

Redis coordina y acelera. Database decide la verdad.

### 3.5 No Folia

GameKit no tendrá soporte Folia.

La compatibilidad objetivo es Paper, con módulos de dominio Java puros y adaptadores Paper delgados. Se descarta cualquier compatbilidad con Folia


### 3.6 Dominio sin Paper

Los módulos de dominio no deben depender de Paper/Bukkit.

Paper vive en adaptadores:

- listeners;
- player operations;
- inventories;
- chat;
- scheduler;
- visibility;
- titles/sounds/actionbars;
- teleport;
- plugin messaging;
- rendering visual.

Esto permite testear la mayor parte de GameKit con JUnit puro.

---

## 4. Alcance

### 4.1 Incluido

GameKit debe cubrir:

- sesión local de jugador dentro de modalidad;
- lobby de modalidad;
- queue;
- party handling;
- casual matchmaking;
- ranked matchmaking base;
- arena allocation;
- waiting room;
- match lifecycle;
- teams;
- spectator;
- respawn/eliminación;
- reconnect;
- abandon ranked/casual;
- finalización/post-game;
- arena reset;
- stats;
- rating/SR;
- rewards;
- leaderboards;
- seasons simples/manuales;
- competitive integrity;
- routing cross-server;
- server registry;
- Redis runtime;
- persistence/idempotency;
- config/management/admin in-game;
- experience scopes;
- observability;
- testkit JUnit;
- naming conventions;
- custom exceptions/result policy.

---

## 5. Topología inicial y escalabilidad

### 5.1 Topología inicial por modalidad

La topología productiva inicial será:

- 1 lobby server por modalidad;
- 1 arena server por modalidad;
- Redis compartido para coordinación runtime;
- database compartida para datos durables.

Osea lo que se tienen pensando esque al principio de la network tenemos que dejar todo listo por ejemplo tenedremos un lobby server por cada modalidad imaginemos Bedwars entonces tenemos el Lobby de bedwarss donde se seleccionara los difrentes modos entonces luego tenemops otro servidor aparte donde estara los watigin rooms y la arena como tal pero luego tiene que estar todo listo para ir agrengaod mas servidores con mas wating rooms y mas arenas ya me entiedns

Ejemplo:

- `bedwars-lobby-01`;
- `bedwars-arena-01`;
- `skywars-lobby-01`;
- `skywars-arena-01`.

### 5.2 Escalabilidad esperada

El diseño debe permitir crecer a:

- múltiples lobby servers por modalidad;
- múltiples arena servers por modalidad;
- routing distribuido;
- reconnect cross-server;
- spectator cross-server;
- server draining;
- arena allocation distribuida;
- expansión de leaderboards y ranked.

### 5.3 Regla de ubicación

Waiting room, match starting, match running y spectating viven normalmente en el mismo server donde vive la arena.

El lobby resuelve, reserva y transfiere. El arena server ejecuta.

---

## 6. Modelo conceptual principal

### 6.1 Jerarquía funcional

La jerarquía base es:

1. **GameDefinition**: modalidad conceptual, por ejemplo BedWars.
2. **MatchVariant**: forma jugable dentro de la modalidad, por ejemplo Ranked 2v2.
3. **QueueDefinition**: entrada concreta de cola, por ejemplo `bedwars:ranked_2v2`.
4. **Match**: partida concreta en ejecución.

Ejemplo:

- GameDefinition: `bedwars`;
- MatchVariant: `ranked_2v2`;
- QueueDefinition: `bedwars:ranked_2v2`;
- Match: instancia UUID concreta.

### 6.2 MatchKind

GameKit reconocerá estos tipos de match:

- Casual;
- Ranked;
- Event.

La terminología oficial será **Casual**, no “Unranked”, para mantener claridad y consistencia.

### 6.3 TeamSpec

Cada variant define:

- cantidad de equipos;
- jugadores por equipo;
- máximo de jugadores.

Todo match se modela con equipos, incluso modalidades solo/1v1.

### 6.4 ArenaRequirements

Las variants declaran requirements por tags.

Ejemplo conceptual:

- `bedwars`;
- `2v2`;
- `ranked_ready`.

Una arena es compatible si contiene todos los tags requeridos por la variant. esto es una idea inicial pero obviasmten pueden aver mejores ya lo iremos si es posibel mejorando talvez existgna otroso enfoqeus mjeores 

---

## 7. Estados de jugador y active play

### 7.1 GameSession

GameSession representa la sesión operacional local de un jugador dentro de una modalidad.

No es:

- perfil persistente;
- stats;
- cosméticos;
- economía;
- settings globales;
- wrapper de Bukkit Player.

Estados principales:

- Lobby;
- Queue;
- Waiting Room;
- Playing;
- Spectating;
- Ending.

GameKit valida transiciones básicas y emite eventos. La modalidad reacciona.

### 7.2 ActivePlaySession

Antes de entrar a una queue, room o match, GameKit debe verificar si el jugador ya tiene actividad activa.

Actividad activa incluye:

- queue;
- waiting room;
- match starting/pre-game;
- match running;
- spectating;
- match reconnectable.

Esto evita que un jugador esté en dos flujos al mismo tiempo.

### 7.3 Play request resolution

El botón de una modalidad o variant no entra directo a queue. Pasa por un resolver.

El resolver decide:

- join queue;
- join waiting room existente;
- reconnect to match;
- show active match prompt;
- show ranked conflict prompt;
- reject.

### 7.4 Reconnect y abandon

Casual:

- disconnect en waiting room remueve rápido al jugador;
- click en misma mode durante match starting puede reconectar automáticamente;
- click durante match running debe mostrar prompt para reconectar o abandonar.

Ranked:

- si el jugador tiene ranked activa y pide otra acción, se muestra prompt;
- puede reconectar;
- puede abandonar ranked y continuar;
- abandonar ranked aplica penalización, cooldown y auditoría;
- no se abandona ranked automáticamente por un click.

---

## 8. Lobby de modalidad

GameKit maneja el lobby propio de cada modalidad, no el lobby global de network.

Responsabilidades:

- aplicar loadouts;
- limpiar inventario al entrar;
- proteger inventario;
- bloquear drop/move/offhand/pickup según policy;
- registrar acciones de items;
- refrescar items según estado/contexto.

La modalidad define:

- qué items existen;
- condiciones de visibilidad;
- acciones;
- menús concretos;
- textos;
- permisos;
- integración con party, queue, locale y feedback.

---

## 9. Queue, parties y matchmaking

### 9.1 Separación de responsabilidades

Queue:

- entradas;
- salidas;
- tickets;
- estado de cola.

Matchmaking:

- decide quién juega con quién;
- forma candidatos de match.

Arena allocation:

- decide dónde se juega;
- filtra y reserva arenas.

GameKit arma la mesa. La modalidad juega la partida.

### 9.2 QueueTicket

Un ticket puede representar:

- jugador solo;
- party completa.

Una party entra completa o no entra.

### 9.3 Party split

Party split significa separar miembros de una party en distintos equipos dentro del mismo match, no separar la party en matches distintos.

Casual puede permitir split.

Ranked no debe permitir split por defecto.

### 9.4 PartyJoinMode

Modos de party:

- Solo only;
- Same team;
- Allow split.

### 9.5 Casual

Casual prioriza:

- actividad;
- velocidad;
- diversión;
- retención;
- experiencia social.

Reglas:

- puede permitir party split;
- party size puede ser mayor que playersPerTeam si maxPlayers lo permite;
- se busca waiting room compatible antes de crear una nueva;
- se evita fragmentar rooms;
- si hay arena disponible, el jugador no debe quedar innecesariamente esperando en lobby.

### 9.6 Ranked

Ranked prioriza:

- integridad;
- justicia competitiva;
- evitar boosting;
- evitar win trading;
- evitar abuso de party.

Reglas:

- no split por defecto;
- parties deben entrar en condiciones competitivas válidas;
- ranked 1v1 no acepta party de 2 como oponentes;
- matchmaking espera candidato válido antes de reservar arena.

### 9.7 Matchmaking coordinator

GameKit tendrá coordinadores de matchmaking por estrategia.

Estrategias iniciales:

- CasualMatchmakingStrategy;
- RankedMatchmakingStrategy.

El coordinator corre periódicamente, no cada tick.

---

## 10. Routing y server registry

### 10.1 Server registry

GameKit debe conocer:

- servers online;
- server role;
- capacidad;
- estado;
- heartbeats;
- arenas reportadas;
- waiting rooms activas;
- matches activos.

Estados de server:

- Online;
- Draining;
- Full;
- Offline;
- Unknown.

Nuevas partidas no deben asignarse a servers Draining, Full, Offline o Unknown.

Reconnect puede permitirse a Draining si el match sigue vivo allí.

Oviamente los servidores deben poder registrarse correctmaente porque no podemos usar el name del servidor que viene de la API de papermc obviasmtne 

### 10.2 Routing service

Routing decide destino.

Transfer ejecuta traslado.

Admission admite al jugador en el server destino.

El server destino nunca debe “adivinar” por qué llegó un jugador.

### 10.3 Admission requests

Cada transferencia cross-server debe crear una admission request con:

- jugador;
- tipo de admisión;
- gameId;
- variantId si aplica;
- arenaId si aplica;
- waitingRoomId si aplica;
- matchId si aplica;
- expiración.

Tipos:

- join waiting room;
- reconnect match;
- spectate match;
- return lobby.

### 10.4 Match location registry

Cada match activo debe registrar:

- matchId;
- gameId;
- variantId;
- serverId;
- arenaId;
- estado.

Esto habilita reconnect y spectator cross-server.

### 10.5 Flujos

Casual:

1. jugador click variant casual;
2. resolver verifica active play;
3. se busca waiting room compatible;
4. si existe, se crea admission y se transfiere;
5. si no existe, se reserva arena y se crea waiting room;
6. arena server admite al jugador.

Ranked:

1. jugador entra a queue ranked;
2. matchmaking forma candidato competitivo válido;
3. se reserva arena;
4. se crean admissions para todos;
5. todos son transferidos al arena server;
6. se inicia pre-game/match starting.

### 10.6 Plugin Velocity central de GameKit

GameKit tendrá un único plugin Velocity central para el plano network.

Este plugin será generado desde el propio proyecto GameKit como un `.jar` final instalable directamente en Velocity.

Responsabilidades del plugin Velocity central:

- ejecutar transferencias entre servidores;
- manejar fallos de conexión;
- aplicar fallback controlado;
- registrar canales de comunicación necesarios;
- coordinarse con Redis/admissions;
- validar que un traslado tenga intención explícita;
- observar estado de servidores cuando aplique;
- mantener routing cross-server consistente y auditable.

El plugin Velocity central no debe contener lógica específica de modalidades.

No debe saber cómo funciona BedWars, SkyWars, Pillars o cualquier otra modalidad. Su responsabilidad es transporte, coordinación y seguridad operacional del movimiento entre servidores.

Las modalidades declaran intención desde sus plugins Paper mediante admissions, queue/routing requests o contratos equivalentes. Velocity ejecuta el traslado de forma genérica.

Flujo conceptual:

1. plugin Paper crea una admission request con TTL;
2. plugin Paper solicita o dispara el traslado;
3. plugin Velocity valida destino e intención;
4. Velocity conecta al jugador al server destino;
5. plugin Paper destino consume la admission request;
6. el jugador entra al flujo correspondiente.

Redis debe contener la intención verificable. Plugin messaging o Pub/Sub pueden actuar como señal, pero no deben ser la única fuente de verdad para una transferencia crítica.

---

## 11. Arena y arena reset

### 11.1 ArenaSlot

La unidad runtime es ArenaSlot.

Un ArenaSlot representa una arena operativa específica en un server concreto.

Debe incluir:

- arenaId;
- templateId;
- serverId;
- worldName;
- state;
- tags;
- maxPlayers.

### 11.2 Arena states

Estados:

- Available;
- Reserved;
- Waiting Room;
- Playing;
- Resetting;
- Disabled.

Reglas:

- solo Available se puede reservar;
- Reserved puede ir a Waiting Room o Playing;
- Playing termina en Resetting;
- Resetting vuelve a Available o Disabled;
- Disabled no recibe partidas.

### 11.3 Reserva distribuida

Las reservas de arena deben ser atómicas cuando haya más de un lobby/server intentando asignar.

Redis debe proveer lock/CAS de corta duración.

Si dos procesos intentan reservar la misma arena, solo uno gana.

### 11.4 Arena reset

Al terminar un match:

1. arena pasa a Resetting;
2. se ejecuta estrategia de reset;
3. si funciona, vuelve a Available;
4. si falla, pasa a Disabled y se alerta.

Estrategias:

- NoOp para tests/desarrollo controlado;
- World template reset para arenas completas;
- schematic/FAWE adapter para casos específicos;
- rollback de cambios no es estrategia primaria inicial.

Core no depende directamente de WorldEdit/FAWE.

---

## 12. Waiting room

Waiting room existe en el mismo server de la arena.

Responsabilidades:

- admitir tickets completos;
- buscar room compatible;
- evitar fragmentación;
- iniciar countdown al llegar al mínimo;
- cancelar countdown si baja del mínimo;
- acelerar si se llena;
- cerrar si queda vacía;
- emitir ready-to-start.

Estados:

- Waiting for players;
- Countdown;
- Starting;
- Closed.

WaitingRoomService no crea el match directamente. Emite evento de ready-to-start y el coordinador correspondiente inicia match.

---

## 13. Match lifecycle

GameKit administra la partida; la modalidad define cómo se gana.

Estados:

- Created;
- Preparing;
- Starting;
- Running;
- Ending;
- Cleanup;
- Closed;
- Cancelled.

Flujo normal:

1. Created;
2. Preparing;
3. Starting;
4. Running;
5. Ending;
6. Cleanup;
7. Closed.

Cancelación:

- Created/Preparing/Starting/Running pueden ir a Cancelled;
- luego Cleanup;
- luego Closed.

### 13.1 Match context

Cada match debe tener contexto suficiente:

- matchId;
- gameId;
- variantId;
- arenaId;
- serverId;
- teams;
- participants;
- scope;
- season context.

### 13.2 MatchScope

Todo recurso creado para un match debe quedar scopeado.

Incluye:

- tasks;
- listeners;
- bossbars;
- scoreboard resources;
- tab/chat scopes;
- temporary entities;
- visual resources;
- scheduled callbacks.

Al cerrar el match, MatchScope debe cerrarse siempre.

### 13.3 Idempotencia de endMatch

Finalizar un match debe ser idempotente.

Si dos señales intentan cerrar el mismo match, solo una debe procesar resultado, stats, rating y rewards.

---

## 14. Teams

Todo match usa equipos, incluso modos solo.

GameKit debe:

- crear equipos finales;
- validar asignaciones;
- evitar duplicados;
- evitar sobrellenado;
- mantener teamId en participantes eliminados/desconectados/abandonados.

La modalidad decide:

- nombres visibles;
- colores;
- cuándo un team queda eliminado;
- lógica de victoria.

No hay rebalancing silencioso al iniciar match.

---

## 15. Death, respawn y eliminación

GameKit no asume que muerte equivale a eliminación.

La modalidad resuelve la muerte y devuelve una decisión conceptual:

- respawn con delay;
- eliminate;
- ignore.

Ejemplos:

- BedWars con cama viva: respawn;
- BedWars sin cama: eliminación;
- SkyWars: eliminación;
- muerte durante starting: ignore;
- muerte mientras respawning: ignore.

GameKit administra:

- estado del participante;
- respawn session;
- bloqueo temporal;
- eventos;
- transición a eliminado.

La modalidad ejecuta:

- teleport final;
- gear;
- invulnerability;
- textos;
- reglas específicas.

---

## 16. Spectator

GameKit distingue:

- spectator externo;
- jugador eliminado;
- staff spectator;
- party member spectator;
- jugador en respawn cooldown.

Respawning no es spectator real.

### 16.1 Estrategia visual

Default recomendado:

- Adventure fly con UX custom.

Vanilla spectator puede existir para staff o casos específicos.

### 16.2 Responsabilidades

GameKit maneja:

- admission policy;
- spectator state;
- visibility;
- basic loadout;
- teleport targets;
- chat scope;
- salida limpia;
- bloqueo de interacción.

La modalidad define:

- menús concretos;
- mensajes;
- scoreboards;
- permisos concretos;
- comportamiento específico por modalidad.

### 16.3 Ranked

Ranked debe deshabilitar spectator externo por defecto para evitar ghosting.

Staff bypass debe ser controlado y auditable.

---

## 17. Finalización, post-game y cleanup

La modalidad decide cuándo termina el match y con qué resultado.

GameKit ejecuta el cierre ordenado:

1. validar que el match puede terminar;
2. pasar a Ending;
3. emitir MatchEndedEvent;
4. ejecutar hooks de modalidad;
5. procesar stats/rating/rewards/leaderboards mediante pipeline idempotente;
6. mostrar post-game;
7. devolver jugadores;
8. limpiar recursos;
9. arena a Resetting;
10. cerrar MatchScope;
11. cerrar match.

Ending es experiencia de jugador. Cleanup es limpieza técnica.

Los jugadores no deben quedar atrapados si falla una proyección o un proceso durable. El fallo queda registrado para retry/alerta.

---

## 18. Experience

GameKit tendrá un sistema de experience scopes para coordinar visuales y comunicación.

Canales:

- scoreboard;
- tablist;
- bossbars;
- actionbars;
- titles/subtitles;
- sounds;
- chat scopes;
- system messages.

Regla:

> GameKit gestiona scopes. La modalidad define contenido. CraftKit/render libraries renderizan.

### 18.1 Experience scopes

Scopes principales:

- Lobby;
- Queue;
- Waiting Room;
- Match Starting;
- Match Running;
- Respawning;
- Spectating;
- Ending.

Estos scopes no son idénticos a GameSessionState.

Ejemplo: un jugador puede seguir en Playing pero tener experience scope Respawning.

### 18.2 Performance

Reglas obligatorias:

- no scoreboard refresh cada tick;
- no full tablist refresh si cambió una entrada;
- no bossbar sin owner/scope;
- no visual tasks fuera de MatchScope;
- no leaks de spectator chat a vivos;
- no cálculos pesados en main thread para post-game summary.

---

## 19. Stats, rating, rewards y leaderboards

### 19.1 Separación por módulos

- Match publica resultado confiable.
- Stats procesa acumuladores y totales.
- Rating calcula SR.
- Rewards entrega recompensas.
- Achievements escucha eventos si aplica.
- Leaderboard proyecta vistas rápidas.

Match no calcula stats, SR ni rewards.

### 19.2 Stats

Stats se acumulan durante match en buffer y se persisten al completar match válido según policy.

Se guardan:

- stats por match;
- totales agregados por jugador/scope.

KDR y ratios son derivados, no contadores primarios.

### 19.3 Rating/SR

SR se calcula:

- solo en ranked válida;
- al final del match;
- no por kills durante match;
- con rating group por modalidad/variant/season.

Cada cambio de SR requiere ledger con:

- before;
- after;
- delta;
- reason;
- matchId;
- ratingGroupId.

### 19.4 Rewards

Rewards son independientes de stats y SR.

Boosters afectan rewards, no SR ni stats.

Cada grant debe tener unique rewardGrantId para evitar duplicados.

### 19.5 Leaderboards

Leaderboards vivos pueden proyectarse a Redis.

Leaderboards históricos se guardan en DB como snapshots inmutables.

Redis se puede reconstruir desde DB.

---

## 20. Seasons

Seasons serán simples, manuales e históricas.

No hay resets automáticos.

No hay rewards automáticas de cierre de season en este diseño.

### 20.1 Propósito

Season define el contexto competitivo/histórico donde ocurre una partida.

Stats, rating y leaderboards deben quedar asociados a seasonId.

### 20.2 Scope recomendado

Season por modalidad.

Ejemplo:

- BedWars Season 1;
- SkyWars Season 1.

Dentro de una season pueden existir leaderboards por variant.

### 20.3 Estados

- Draft;
- Active;
- Closing;
- Closed;
- Archived.

### 20.4 Captura por match

Un match captura el seasonId activo al crearse.

Si la season cambia antes de que el match termine, el match conserva su seasonId original.

### 20.5 Ranked requiere season activa

No se permite ranked sin season activa.

Para casual, la modalidad productiva también debería operar con season activa para mantener contexto histórico consistente.

---

## 21. Competitive integrity

GameKit no es anticheat.

GameKit sí protege integridad competitiva estructural.

### 21.1 Problemas cubiertos

- ranked dodge;
- ranked abandon;
- cooldowns;
- penalties progresivas;
- repeated opponents;
- win trading signals;
- queue sniping signals;
- stat farming signals;
- leaderboard eligibility;
- competitive audit;
- manual review.

### 21.2 Penalty, flag, eligibility y audit

- Penalty: castigo automático por acción clara.
- Flag: señal de sospecha.
- Eligibility: visibilidad o acceso competitivo.
- Audit: evidencia durable.

### 21.3 Dodge vs abandon

Dodge:

- ranked encontrado/pre-game;
- jugador se va antes de running real;
- penaliza menos.

Abandon:

- ranked ya empezó;
- penaliza más;
- puede contar como derrota;
- genera cooldown.

Server error no penaliza jugador.

### 21.4 Leaderboard eligibility

Un jugador puede tener SR pero no ser elegible para aparecer en leaderboard si:

- tiene pocas partidas;
- tiene sanción;
- tiene flags severas;
- fue ocultado manualmente por staff.

Minimum matches played es obligatorio para ranked leaderboards.

---

## 22. Config, management y admin in-game

### 22.1 Separación conceptual

- Definition: lo conceptual que existe.
- Configuration: lo persistido/editable.
- Runtime: lo cargado y activo.

Ejemplo:

- ArenaDefinition: configuración publicada;
- ArenaDraft: edición en progreso;
- ArenaSlot: estado runtime.

No se edita runtime como config.

### 22.2 Draft/validate/publish

Flujo administrativo:

1. create draft;
2. marcar posiciones/tags/spawns/regions;
3. validar;
4. publicar;
5. actualizar runtime registry;
6. auditar.

Nada se publica parcialmente.

### 22.3 Admin in-game

Todo lo que tenga sentido operar in-game debe tener comandos/menús:

- arenas;
- variants;
- seasons;
- leaderboards;
- active matches;
- queues;
- server status;
- health;
- processing;
- integrity review.

Comandos para precisión. Menús para UX.

### 22.4 Arena operational state

Además de ArenaState runtime, existe estado operativo administrativo:

- Enabled;
- Draining;
- Disabled.

Draining no acepta nuevas partidas, pero permite terminar lo activo.

---

## 23. Observability

GameKit debe ser observable por diseño.

Debe emitir:

- logs estructurados;
- métricas;
- health snapshots;
- operational events;
- audit entries;
- alerts.

### 23.1 Trace context

Flujos importantes deben portar contexto:

- traceId;
- playerId;
- matchId;
- arenaId;
- serverId;
- gameId;
- variantId.

### 23.2 Métricas clave

Deben existir métricas para:

- queue;
- waiting room;
- match;
- arena;
- routing/admission;
- Redis runtime;
- ranked/rating;
- stats/rewards;
- processing;
- arena reset.

### 23.3 Auditoría

Auditar:

- acciones admin críticas;
- cierre/activación de seasons;
- publish de configs;
- ranked penalties;
- reward grants;
- ocultar jugador de leaderboard;
- retry manual de processing;
- cambios competitivos sensibles.

Logs operativos no reemplazan auditoría durable.

---

## 24. Persistencia e idempotencia

### 24.1 Regla principal

Todo procesamiento durable crítico debe ser idempotente.

Aplica a:

- stats por matchId;
- rating por matchId/ratingGroup;
- rewards por matchId/playerId/rewardType;
- competitive records por matchId;
- season snapshots por snapshotId;
- leaderboard projections por taskId;
- audit crítica cuando corresponda.

### 24.2 Processing ledger

Debe existir un ledger de procesamiento con estados:

- Processing;
- Processed;
- Failed;
- Retryable Failed.

Esto permite retry sin duplicar datos.

### 24.3 Projection outbox

Las proyecciones a Redis deben poder reintentarse.

No se debe mezclar transacción SQL y Redis como si fueran una sola operación atómica.

Flujo correcto:

1. persistir source of truth en DB;
2. confirmar transacción;
3. proyectar a Redis;
4. si Redis falla, encolar retry.

### 24.4 Fallos después del match

Si falla persistencia/proyección después de terminar match:

- el match puede cerrar;
- los jugadores pueden volver al lobby;
- la tarea queda retryable/failed;
- observability alerta;
- admin puede inspeccionar/reintentar.

---

## 25. Paper adapters

`gamekit-paper` contiene integración con Paper.

Debe ser delgado.

Responsabilidades:

- escuchar eventos Paper;
- traducir a servicios de dominio;
- aplicar operaciones de Player/World/Inventory;
- renderizar experience;
- adaptar scheduler;
- manejar admission en join;
- proteger inventario;
- adaptar chat scope;
- manejar visibility.

No debe contener lógica de negocio compleja.

La lógica vive en dominio y se prueba con JUnit.

### 25.1 Metadata recomendada para plugins consumidores Paper

Los plugins consumidores de modalidad deben usar `paper-plugin.yml` como metadata principal.

La razón es que GameKit está orientado a Paper, no a Bukkit genérico, y Paper ofrece un modelo más explícito para dependencias, orden de carga y aislamiento de classloader.

`plugin.yml` solo debe considerarse si existe una necesidad concreta de compatibilidad con tooling antiguo o con un plugin que todavía lo requiera. No debe ser el camino principal.

Regla:

- plugins de modalidad Paper usan `paper-plugin.yml`;
- plugins Velocity usan la metadata/anotación propia de Velocity;
- GameKit como librería shadeada no puede declarar metadata por el consumidor;
- cada plugin final debe declarar sus dependencias runtime reales.

Dependencias como NetworkPlayerSettings, zMenu, PlaceholderAPI, ViaVersion o PacketEvents no quedan declaradas automáticamente por incluir GameKit en Gradle. El plugin final debe declarar `required`, `load`, `join-classpath` o equivalentes según corresponda.

Ejemplo conceptual:

```yaml
name: ExampleMode
version: 1.0.0
main: network.hera.example.ExampleModePlugin
api-version: "1.21"

dependencies:
  server:
    NetworkPlayerSettings:
      load: BEFORE
      required: true
      join-classpath: true
    zMenu:
      load: BEFORE
      required: false
      join-classpath: true
    PlaceholderAPI:
      load: BEFORE
      required: false
      join-classpath: true
```

El ejemplo no fija dependencias obligatorias para todas las modalidades. Solo ilustra que cada plugin consumidor debe declarar explícitamente lo que realmente usa.

---

## 26. Testing

### 26.1 Estrategia

GameKit se testea con:

- JUnit;
- fakes propios;
- builders;
- fixtures;
- manual scheduler;
- fake clock;
- in-memory repositories.

No se usa MockBukkit como base.

No se usa Mockito como base.

### 26.2 Niveles

- Unit tests;
- Flow tests;
- Contract tests;
- Idempotency tests;
- Concurrency/race tests puntuales.

### 26.3 Casos críticos

Deben cubrirse:

- session transitions;
- queue join/leave;
- party split casual;
- ranked party restrictions;
- waiting room countdown;
- match finalization idempotente;
- respawn/elimination;
- spectator admission;
- stats processing;
- rating processing;
- rewards idempotentes;
- Redis admission expiration;
- arena reservation race;
- season capture;
- ranked abandon penalties;
- leaderboard eligibility;
- projection retry.

---

## 27. Estructura de módulos Gradle

GameKit debe usar una estructura modular, pero no excesivamente fragmentada.

La regla principal es:

> Un módulo Gradle debe representar una responsabilidad grande y estable del sistema, no una clase conceptual ni una subfeature aislada.

La estructura objetivo será:

| Módulo | Responsabilidad |
| --- | --- |
| `gamekit-bom` | Version alignment para consumidores Gradle. |
| `gamekit-core` | Tipos base, IDs, clocks, results, events, contracts comunes y errores base. |
| `gamekit-session` | Sesión operacional de jugador dentro de modalidad y active play. |
| `gamekit-lobby` | Lobby de modalidad, loadouts, protections y acciones de entrada. |
| `gamekit-queue` | Queue tickets, queue state, matchmaking contracts y coordinadores base. |
| `gamekit-network` | Server registry, routing, admission requests, match location y contratos cross-server. |
| `gamekit-arena` | Arena definitions, arena slots, allocation, reservation y reset contracts. |
| `gamekit-match` | Match lifecycle, participants, teams, scopes, finalization y cleanup. |
| `gamekit-experience` | Experience scopes y contratos de render para scoreboard, tab, bossbars, titles, sounds y chat scopes. |
| `gamekit-progression` | Stats, rewards, rating/SR y leaderboards cuando compartan pipeline de progresión. |
| `gamekit-season` | Seasons manuales, season state y captura de season por match. |
| `gamekit-competitive-integrity` | Penalties, flags, eligibility, audit competitivo y señales anti-abuse estructurales. |
| `gamekit-admin` | Contratos y servicios para management/admin in-game. |
| `gamekit-infra-craftkit` | Adaptadores hacia CraftKit database, Redis y feedback cuando corresponda. |
| `gamekit-paper` | Adaptadores Paper delgados: listeners, scheduler, player/world operations, admissions, inventory protection y visibility. |
| `gamekit-scoreboard-paper` | Integración Paper con scoreboard-library para experience/scoreboards. |
| `gamekit-zmenu-paper` | Integración opcional con zMenu/CraftKit zMenu para menús y dialogs. |
| `gamekit-cloud-paper` | Integración opcional con cloud-minecraft para comandos Paper. |
| `gamekit-velocity` | Contratos y adapters comunes para el plano Velocity/network. |
| `gamekit-velocity-plugin` | Plugin Velocity central generado como `.jar` instalable en Velocity. |
| `gamekit-paper-worldedit` | Integración opcional para reset/imports con WorldEdit/FAWE si se decide usar. |
| `gamekit-testkit` | Fakes, fixtures, manual scheduler, fake clock y utilidades de flow tests. |
| `gamekit-examples` | Ejemplos mínimos de integración y vertical slices de referencia. |

Esta estructura es objetivo, no obligación de crear todos los módulos vacíos desde el primer commit. La implementación debe iniciar por la vertical funcional mínima y agregar módulos cuando exista responsabilidad real.

Dominio puro incluye principalmente:

- core;
- session;
- variant;
- queue;
- matchmaking;
- routing contracts;
- server-registry contracts;
- arena;
- waiting-room;
- match;
- team;
- spectator;
- stats;
- rating;
- rewards;
- leaderboard;
- season;
- competitive-integrity;
- persistence contracts;
- observability contracts.

Los módulos Paper, Velocity, zMenu, scoreboard, cloud, CraftKit y WorldEdit son adaptadores o integraciones. No deben empujar lógica de gameplay ni reglas de modalidad hacia el dominio de GameKit.

---

## 28. Organización de paquetes y carpetas

Regla:

> Gradle module = gran responsabilidad del sistema. Dentro del módulo,los directories deben estar organizados y ordenados con una arquitectura de directorios y archviso de  feature-first. Dentro de feature, responsabilidad cuando haga falta.

Evitar estructura obligatoria `domain/application/infrastructure` en todos los módulos. Solo usar separación hexagonal donde aporte claridad real, especialmente en adapters/storage.

Evitar:

- utils;
- common;
- helper;
- manager;
- misc;
- base.

Usar nombres semánticos:

- lifecycle;
- participant;
- respawn;
- result;
- finalization;
- scope;
- registry;
- admission;
- penalty;
- leaderboard;
- storage;
- event;
- policy;
- strategy;
- internal.

---

## 29. Política de errores, results y custom exceptions

### 29.1 Regla principal

Errores esperados del flujo se modelan con Result, Decision o RejectReason.

Errores técnicos, invariantes rotas o infraestructura se modelan con custom runtime exceptions.

### 29.2 Result/Decision/RejectReason

Usar para fallos esperados como:

- player already in queue;
- player already in match;
- ranked cooldown active;
- no active season;
- party not allowed;
- no available arena;
- spectator not allowed;
- leaderboard ineligible.

Estos resultados son user-facing mediante feedback/localización.

### 29.3 Custom exceptions

Usar runtime custom exceptions para:

- estado corrupto;
- transición imposible interna;
- dependencia obligatoria ausente;
- configuración crítica inválida;
- fallo DB;
- fallo Redis;
- timeout;
- serialización;
- uso incorrecto de API.

No usar checked exceptions en APIs públicas.

### 29.4 Error codes

Cada exception debe tener:

- error code;
- mensaje técnico;
- atributos estructurados;
- causa opcional.

No crear un enum global gigante. Cada módulo puede definir sus propios códigos.

### 29.5 Métodos por intención

Convenciones:

- `findX`: devuelve Optional o equivalente conceptual cuando puede no existir.
- `requireX`: falla si no existe; se usa cuando la ausencia indica bug/corrupción.
- `tryX`: intenta operación que puede fallar normalmente.
- `validateX`: devuelve reporte, no exception.

Las exceptions no se envían como mensaje directo al jugador.

---

## 30. Naming conventions

### 30.1 Módulos Gradle

Formato: kebab-case.

Ejemplos:

- gamekit-core;
- gamekit-match;
- gamekit-server-registry;
- gamekit-competitive-integrity.

### 30.2 Packages Java

Formato: lowercase sin guiones.

Ejemplo:

- `network.hera.gamekit.match`;
- `network.hera.gamekit.spectator.admission`;
- `network.hera.gamekit.integrity.penalty`.

### 30.3 Clases, records, enums

Formato: PascalCase.

Ejemplos:

- MatchService;
- MatchState;
- ArenaReservation;
- SpectatorAdmissionPolicy.

### 30.4 Métodos y variables

Formato: lowerCamelCase.

Ejemplos:

- joinQueue;
- findMatch;
- tryEndMatch;
- calculateRating;
- grantRewards.

### 30.5 Constantes

Formato: UPPER_SNAKE_CASE.

### 30.6 Interfaces e implementaciones

No usar prefijo `I`.

No usar sufijo `Impl`.

Usar nombres por responsabilidad:

- DefaultX;
- InMemoryX;
- RedisX;
- CraftKitX;
- PaperX;
- NoOpX;
- FakeX.

### 30.7 Eventos

Eventos en pasado cuando algo ya ocurrió:

- QueueJoinedEvent;
- MatchStartedEvent;
- MatchCompletedEvent;
- ArenaResetCompletedEvent;
- RewardGrantedEvent;
- RatingChangedEvent;
- SeasonActivatedEvent.

Usar Requested cuando algo fue pedido pero no ocurrió todavía.

### 30.8 Sufijos semánticos

Usar:

- Service;
- Registry;
- Repository;
- Store;
- Policy;
- Strategy;
- Resolver;
- Provider;
- Processor;
- Projector;
- Coordinator;
- Renderer;
- Adapter;
- Listener;
- Factory;
- Builder;
- Validator.

Evitar salvo excepción aprobada:

- Manager;
- Helper;
- Utils;
- Common;
- Base;
- Misc;
- Data;
- Info;
- Thing;
- Stuff;
- Impl.

### 30.9 IDs públicos

Formato para IDs de dominio: lowercase snake_case.

Ejemplos:

- gameId: `bedwars`;
- variantId: `ranked_2v2`;
- arenaId: `lighthouse_01`;
- templateId: `lighthouse`;
- seasonId: `bedwars_s1`;
- ratingGroupId: `bedwars_ranked_2v2`;
- leaderboardId: `bedwars_s1_ranked_2v2_sr`.

Server IDs pueden usar kebab-case:

- `bedwars-lobby-01`;
- `bedwars-arena-01`.

### 30.10 Redis keys

Formato:

`gamekit:<domain>:<scope>:<id>`

Ejemplos:

- `gamekit:server:bedwars-arena-01`;
- `gamekit:arena:bedwars:lighthouse_01`;
- `gamekit:arena-lock:lighthouse_01`;
- `gamekit:admission:<requestId>`;
- `gamekit:match-location:<matchId>`;
- `gamekit:active-player:<playerId>`;
- `gamekit:leaderboard:bedwars_s1_ranked_2v2_sr`.

### 30.11 DB naming

Tablas y columnas en snake_case.

Ejemplos de tablas:

- player_stat_totals;
- player_match_stats;
- player_rating;
- rating_change_ledger;
- reward_ledger;
- season_snapshots;
- audit_log;
- competitive_match_records;
- processing_ledger;
- projection_outbox.

Constraints únicos con nombres claros.

### 30.12 Permisos

Formato:

`gamekit.<area>.<action>`

Ejemplos:

- gamekit.admin.arena.create;
- gamekit.admin.arena.publish;
- gamekit.admin.variant.edit;
- gamekit.admin.season.close;
- gamekit.admin.rating.modify;
- gamekit.admin.integrity.review;
- gamekit.admin.processing.retry;
- gamekit.admin.leaderboard.rebuild.

---

## 31. Secuencia recomendada de construcción

La construcción debe iniciar por una vertical funcional completa, no por sistemas aislados sin integración.

Secuencia recomendada:

1. core + convenciones base;
2. testkit mínimo desde el inicio: fakes, fake clock y fixtures base;
3. session;
4. variant;
5. queue básica;
6. Redis runtime mínimo;
7. server registry;
8. routing/admission;
9. arena registry/allocation con reset `NoOp` para desarrollo;
10. waiting room;
11. match lifecycle;
12. team;
13. Paper adapters mínimos;
14. experience mínima;
15. persistence/idempotency base;
16. arena reset real: template reset y adapters opcionales;
17. stats;
18. rewards;
19. seasons;
20. rating/SR;
21. leaderboards;
22. competitive integrity;
23. observability completa;
24. admin/management in-game;
25. spectator avanzado;
26. testkit expandido por contratos.

El reset `NoOp` permite validar el primer flujo sin bloquear el desarrollo por infraestructura de mundos. El reset real debe llegar después de tener lifecycle, cleanup e idempotencia base, porque depende de saber cuándo una arena terminó, qué recursos se cerraron y cómo se reporta un fallo operacional.

El testkit no debe esperar al final. Desde el primer módulo deben existir fakes mínimos para clock, eventos, repositorios y schedulers. Al final se expande con contract tests y fixtures más completas.

### 31.1 Primer flujo funcional a validar

El primer flujo completo debería demostrar:

1. jugador en lobby de modalidad;
2. click casual 2v2;
3. resolución de active play;
4. routing por Redis al arena server;
5. admission request;
6. ingreso a waiting room;
7. countdown;
8. match starting;
9. match running;
10. finalización simple;
11. post-game;
12. arena reset;
13. return lobby;
14. limpieza de scope;
15. métricas/logs básicos;
16. tests del flujo.

Luego se agregan ranked, SR, leaderboards e integrity sobre una base ya funcional.

---

## 32. Criterios de listo para iniciar desarrollo

El diseño se considera listo para desarrollo cuando el equipo acepta:

- GameKit como librería Gradle interna;
- dependencia directa hacia CraftKit;
- dominio sin Paper;
- Redis desde el inicio;
- database como fuente durable;
- separación clara GameKit vs modalidad;
- módulos Gradle definidos;
- private matches fuera del alcance actual;
- no soporte Folia;
- seasons manuales;
- admin in-game para management;
- testing con JUnit + fakes;
- custom runtime exceptions;
- Result/Decision para errores esperados;
- naming conventions oficiales;
- pipeline durable idempotente;
- competitive integrity como módulo propio;
- observability como requisito de diseño.
---

## 33. Glosario breve

**GameKit:** librería interna común para modalidades competitivas.  
**CraftKit:** librería técnica base para DB, Redis, feedback, integración zMenu, utilidades Paper/Adventure y otras piezas comunes. CraftKit no maneja scoreboards en el diseño actual; scoreboards se integrarán mediante la librería dedicada definida para ese propósito.  
**GameDefinition:** modalidad conceptual.  
**MatchVariant:** variante jugable de una modalidad.  
**QueueTicket:** entrada de jugador/party en cola.  
**Waiting Room:** sala previa en el server de arena.  
**Match:** partida concreta.  
**ArenaSlot:** arena runtime concreta en un server.  
**MatchScope:** contenedor de recursos temporales de match.  
**SR:** rating visible competitivo.  
**RatingGroup:** grupo de rating por game/variant/season.  
**Season:** contexto competitivo e histórico.  
**AdmissionRequest:** intención explícita para admitir jugador en server destino.  
**ActivePlaySession:** actividad actual del jugador dentro de modalidad.  
**ProcessingLedger:** registro durable para idempotencia.  
**ProjectionOutbox:** cola durable para reproyectar datos a Redis u otras vistas.  
**Competitive Integrity:** protección estructural contra abuso competitivo.

---

## 34. Cierre

GameKit debe nacer como una base profesional para modalidades competitivas, no como una colección de utilidades.

El diseño prioriza:

- claridad de responsabilidades;
- escalabilidad progresiva;
- Redis runtime desde el inicio;
- persistencia confiable;
- idempotencia;
- experiencia de jugador;
- integridad competitiva;
- administración in-game;
- testabilidad;
- naming consistente;
- separación entre infraestructura y gameplay.

Con este PRD, el equipo puede iniciar el diseño técnico e implementación con una base coherente y preparada para crecer sin romper sus fundamentos.

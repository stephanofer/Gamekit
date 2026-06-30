## No negociables

- Quiero que el proyecto tenga la menor sobreingeniería posible. Queremos algo liviano, funcional, simple, escalable, fácil de mantener, auditable y depurable; es decir, no deberíamos introducir complejidad innecesaria que nos vaya a causar problemas más adelante. Dicho eso, esto no significa que vayamos a hacer las cosas mal; todo debe tener el mejor rendimiento y eficiencia, cada componente debe cumplir correctamente con sus responsabilidades, y queremos la menor cantidad posible de bugs y errores. Nada de problemas de rendimiento, nada de ineficiencias; queremos algo ultra-performante.'
- Recordá que si necesitás información sobre cualquier cosa, tenemos un directorio `docs/` donde vas a encontrar documentación para todo lo que puedas necesitar respecto de PaperMC, Adventure, zMenu; eso es lo que está disponible por ahora. Así que si necesitás cualquier información para mejorar tu resultado y hacer las cosas correctamente siguiendo la documentación, eso sería ideal. Siempre que necesites hacer algo y no estés seguro de cómo manejarlo al nivel de estas dependencias, andá a `docs/` y vas a encontrar lo que buscás. Además, todo está fragmentado; esto es muy importante: NO leas un archivo entero de una sola vez, está fragmentado para que no desperdicies tokens ni contexto leyendo cosas que no necesitás para aquello en lo que estás trabajando actualmente.

## Estrategia oficial de dependencias

- GameKit estandariza intencionalmente sobre las dependencias listadas abajo. Ya pasaron la evaluación del equipo y se espera que sean usadas por GameKit y por los plugins consumidores de modalidades. No diseñes como si estas dependencias fueran a ser reemplazadas casualmente.
- El acoplamiento a estas dependencias es aceptable cuando mejora la claridad, la DX, el rendimiento o el acceso a sus capacidades reales. Esto no significa escribir código descuidado; las integraciones igual deben ser limpias, explícitas, eficientes, auditables y fáciles de depurar.
- Es aceptable que las APIs de GameKit expongan tipos de estas dependencias oficiales cuando el módulo esté integrándose específicamente con esa dependencia o cuando ocultar el tipo empeoraría la API.
- No crees wrappers innecesarios alrededor de APIs, métodos o tipos de librerías solo para evitar acoplamiento. Los wrappers desperdician tiempo, ocultan funcionalidad útil y hacen más débil al sistema salvo que agreguen un contrato real de GameKit, una regla de ownership, un límite de ciclo de vida, una policy o una abstracción multiplataforma.
- Antes de implementar comportamiento de integración, revisá la documentación local y el código fuente disponible en este repositorio. Preferí evidencia de la documentación/fuente real de la dependencia por encima de suposiciones.

### Mapa de dependencias

| Área | Dependencia oficial | Usala para | Referencia local |
| --- | --- | --- | --- |
| Archivos YAML/config | BoostedYAML | Archivos de configuración, carga/guardado de YAML, actualizaciones y migraciones cuando se necesite configuración YAML. | fuente en `boosted-yaml/`. |
| Caché local | Caffeine | Cachés locales en memoria cuando se necesite caching eficiente dentro de un proceso. | dependencia Maven oficial; revisar docs públicas si hace falta. |
| Comandos | cloud-minecraft | Registro de comandos Paper/Velocity y UX de comandos. | fuente en `cloud-minecraft/`. |
| Texto/componentes | Adventure | Components, MiniMessage, audiences, serialización de texto y mensajería moderna de Minecraft. | fuente en `adventure/` y `docs/Adventure/`. |
| Infra compartida | CraftKit | Base de datos, Redis, feedback, helpers de Paper y utilidades de integración con zMenu ya estandarizadas por HERA. | `docs/CraftKit/`. |
| Ajustes/locales de jugador | NetworkPlayerSettings | Idioma del jugador, salida adaptada al locale, ajustes/preferencias de jugador y servicios relacionados. | `docs/NetworkPlayerSettings/`. |
| Paquetes | PacketEvents | Comportamiento a nivel de paquetes solo cuando exista una necesidad real que las APIs de Paper/Velocity no cubran. | `docs/PacketEvents/`. |
| Plataforma Paper | PaperMC | APIs de plugins de Paper, ciclo de vida, scheduler, eventos, metadata e integración con el servidor. | `docs/Papermc/`. |
| Placeholders | PlaceholderAPI | Parseo de placeholders y expansiones internas cuando se necesiten placeholders. | `docs/PlaceholderAPI/`. |
| Proxy/red | Velocity | Plugin proxy central, transfers, fallback, plugin messaging y cuestiones de transporte en red. | `docs/Velocity/`. |
| Scoreboards | scoreboard-library | Sidebars, teams, objectives y renderizado de scoreboards. | fuente en `scoreboard-library/`. |
| Menús/diálogos | zMenu | Menús, diálogos y UX guiada por GUI. Usá zMenu en lugar de inventar un motor de menús. | fuente en `zMenu/` y documentación relacionada cuando esté disponible. |

### Guía de integración

- Si implementás menús o diálogos, usá zMenu e inspeccioná `zMenu/` más la documentación antes de integrar.
- Si implementás caché local en memoria, usá Caffeine en lugar de estructuras caseras con mapas, timers o limpieza manual.
- Si implementás comandos, usá cloud-minecraft en lugar de inventar un framework de comandos.
- Si implementás scoreboards, usá scoreboard-library en lugar de crear lógica personalizada de paquetes/sidebar.
- Si implementás configuración respaldada por YAML, usá BoostedYAML salvo que un módulo tenga una razón más fuerte para no hacerlo.
- Si implementás comportamiento de feedback, idioma o preferencias del jugador, integralo con NetworkPlayerSettings y los patrones de feedback de CraftKit.
- Si implementás Redis, BD, feedback, helpers de zMenu o utilidades de Paper ya cubiertas por CraftKit, preferí CraftKit en lugar de duplicar infraestructura.
- Si implementás comportamiento de transferencia/enrutamiento del proxy, usá la documentación de Velocity y mantené a Velocity como infraestructura de transporte genérica, no como lógica de gameplay.
- Si implementás comportamiento a nivel de paquetes, primero verificá que las APIs de Paper/Velocity sean insuficientes; después consultá la documentación/fuente de PacketEvents y mantené la integración aislada.

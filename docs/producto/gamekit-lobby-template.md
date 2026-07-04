# GameKit Lobby YAML Template

`gamekit-lobby.yml` is owned by each modality plugin. GameKit does not copy, load, or save this file automatically. The modality plugin should include this file in its own resources, load it with BoostedYAML, then pass the `YamlDocument` to GameKit parsers.

```yaml
config-version: 1

runtime:
  enabled: true
  default-language: en

spawn:
  world: bedwars_lobby
  x: 0.5
  y: 80.0
  z: 0.5
  yaw: 180
  pitch: 0

join:
  clear-inventory:
    enabled: true

  reset-player:
    enabled: true
    health: true
    food: true
    saturation: true
    fire: true
    freeze: true
    fall-distance: true
    remaining-air: true
    potion-effects: true
    exp: false

  teleport-spawn:
    enabled: true

  set-gamemode:
    enabled: true
    value: adventure

  apply-loadout:
    enabled: true

  held-slot:
    enabled: true
    slot: 0

protections:
  inventory: true
  drop: true
  pickup: true
  offhand: true
  craft: true
  item-damage: true
  block-place: true
  block-break: true
  block-interact: true
  pvp: true
  fall-damage: true
  fire-damage: true
  drowning: true
  hunger: true
  void-teleport: true
  mob-spawn: true
  mob-drops: true
  death-messages: true
  weather-change: true
  fire-spread: true
  leaf-decay: true
  item-frames: true

world:
  fixed-time:
    enabled: true
    value: 6000

  clear-weather:
    enabled: true

staff-mode:
  enabled: true
  permission: gamekit.lobby.staff
  gamemode: creative
  clear-inventory-on-enable: true
  restore-lobby-on-disable: true

commands:
  enabled: true
  root: gamekit
  permission-prefix: gamekit.lobby

items:
  play_casual_2v2:
    slot: 0
    material: COMPASS
    text:
      en:
        name: "<green>Play Casual 2v2"
        lore:
          - "<gray>Right click to join."
      es:
        name: "<green>Jugar Casual 2v2"
        lore:
          - "<gray>Click derecho para entrar."
    actions:
      right:
        type: join_casual_queue
        queue: bedwars:casual_2v2
```

Consumer loading shape:

```java
YamlDocument lobbyYaml = YamlDocument.create(
    new File(getDataFolder(), "gamekit-lobby.yml"),
    getResource("gamekit-lobby.yml")
);

LobbyRuntimeConfig config = LobbyRuntimeConfigParser.parseConfig(lobbyYaml);
LobbyLoadout loadout = LobbyRuntimeConfigParser.parseLoadout(lobbyYaml);
```

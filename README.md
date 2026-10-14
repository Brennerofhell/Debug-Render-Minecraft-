# Support Blocks

Client-Mod für **Minecraft 1.21.1** mit **NeoForge**. Sie markiert den Block, auf dem
Spieler und Mobs stehen, mit einem farbigen Rahmen, auch wenn sie unsichtbar sind.
Das ist ein Nachbau des Debug-Renderers `visualize_entity_supporting_block` aus 1.21.11.

## Installation
1. Die `.jar` aus `build/libs/` (oder aus dem Artifact `supportblocks-jar` der
   GitHub-Actions-Builds) nach `.minecraft/mods` kopieren.
2. Minecraft mit dem NeoForge-Profil (21.1.x) starten. Auf dem Server ist die Mod nicht nötig.

## Bedienung
- **F8** schaltet die Rahmen ein und aus (änderbar unter Optionen → Steuerung).
- **Einstellungen**: Mods → Support Blocks → Konfiguration, oder die Datei
  `config/supportblocks-client.toml`:
  - `enabledOnStartup`: Overlay beim Start aktiv
  - `onlyPlayers`: nur Spieler statt aller Entities markieren
  - `radius`: maximale Entfernung (1 - 256 Blöcke)
  - `red` / `green` / `blue` / `alpha`: Rahmenfarbe (0.0 - 1.0)

## Selbst bauen
Benötigt JDK 21:
```
./gradlew build
```

## Lizenz
GNU General Public License v3.0 oder neuer, siehe [LICENSE](LICENSE).

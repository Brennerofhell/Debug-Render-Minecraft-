# Changelog

Alle wichtigen Änderungen an Support Blocks.

## [1.0.0] – unveröffentlicht

### Funktionen
- Markiert den Block, auf dem Spieler und Mobs stehen, auch bei Unsichtbarkeit.
  Nachbau des Debug-Renderers `visualize_entity_supporting_block` aus 1.21.11.
- **F8** schaltet ein und aus (änderbar), mit Meldung „Support-Blöcke: AN/AUS“ in der Actionbar.
- Block-Erkennung wie in Vanilla, auch an Blockkanten.
- Umrandet die echte Blockform (Stufen, Treppen, Zäune, ...).
- Optional durch Wände sichtbar und mit halbtransparenter Füllung.
- Eigene Farben für Spieler, feindliche Mobs, friedliche Mobs und sonstige Entities,
  als Hex mit optionaler Deckkraft.
- Filter: nur Spieler, Spieler und Mobs, oder alle Entities. Dazu Radius und
  optional der eigene Spieler in der Third-Person-Ansicht.
- Konfiguration in `supportblocks-client.toml` und im Spiel über die Mod-Liste,
  auf Deutsch und Englisch.
- Nur Client: auf dem Server nicht nötig.

### Behoben (während der Entwicklung)
- Rahmen folgen der Kamera-Drehung jetzt in jedem Fall (Model-View-Matrix wird explizit gesetzt).
- Kein Zittern mehr weit weg vom Spawn (Koordinaten relativ zur Kamera).
- Veraltete `mainSupportingBlockPos`-Werte werden ignoriert, wenn die Entity nicht am Boden ist.
- Mehrere Entities auf demselben Block werden nur einmal gezeichnet.

### Projekt
- NeoForge 21.1.258 für Minecraft 1.21.1, Java 21, ModDevGradle.
- GitHub Actions: Build mit Unit-Tests bei jedem Push, automatische Releases beim Push auf `main`.
- Lizenz: GPL-3.0-or-later.

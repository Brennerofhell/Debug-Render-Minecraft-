<div align="center">

# Support Blocks

**Zeigt, auf welchem Block Spieler und Mobs stehen, auch wenn sie unsichtbar sind.**

Client-Mod für Minecraft 1.21.1 · NeoForge

[![Download](https://img.shields.io/badge/Download-neueste%20Version-2ea44f?style=for-the-badge&logo=github)](https://github.com/Brennerofhell/Debug-Render-Minecraft-/releases/latest)
[![Alle Versionen](https://img.shields.io/badge/Alle%20Versionen-Releases-555?style=for-the-badge&logo=github)](https://github.com/Brennerofhell/Debug-Render-Minecraft-/releases)

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-62B47A?logo=minecraft&logoColor=white)](https://www.minecraft.net)
[![NeoForge](https://img.shields.io/badge/NeoForge-21.1.x-E57E25)](https://neoforged.net)
[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)](https://adoptium.net)
[![Lizenz: GPL v3](https://img.shields.io/badge/Lizenz-GPL%20v3-blue)](LICENSE)
[![Build](https://img.shields.io/github/actions/workflow/status/Brennerofhell/Debug-Render-Minecraft-/build.yml?label=Build&logo=githubactions&logoColor=white)](https://github.com/Brennerofhell/Debug-Render-Minecraft-/actions/workflows/build.yml)

</div>

---

## Was macht die Mod?

Sie baut den Debug-Renderer `visualize_entity_supporting_block` aus Minecraft 1.21.11 nach.
Unter jedem Spieler und Mob wird der Block, auf dem er gerade steht, mit einem
**grünen Rahmen** markiert. Das funktioniert auch bei **Unsichtbarkeit**.

- Ein- und Ausschalten per Taste (**F8**, änderbar)
- Rückmeldung in der Actionbar: *„Support-Blöcke: AN/AUS“*
- Radius, Farbe und „nur Spieler“ einstellbar
- **Nur Client:** Auf dem Server nicht nötig, funktioniert auch auf fremden Servern

## Installation

1. **[Neueste Version herunterladen](https://github.com/Brennerofhell/Debug-Render-Minecraft-/releases/latest)**: die Datei `supportblocks-x.y.z.jar`.
2. [NeoForge für 1.21.1](https://neoforged.net) installieren (Version 21.1.x), falls noch nicht vorhanden.
3. Die `.jar` in den Ordner `.minecraft/mods` kopieren.
   - Windows: `%appdata%\.minecraft\mods`
   - macOS: `~/Library/Application Support/minecraft/mods`
   - Linux: `~/.minecraft/mods`
4. Minecraft mit dem **NeoForge-Profil** starten.

## Bedienung

| Aktion | Wie |
|---|---|
| Rahmen ein/aus | **F8** |
| Taste ändern | Optionen → Steuerung → Tastenbelegung → *Support Blocks* |
| Einstellungen | Mods → Support Blocks → **Konfiguration** |

### Einstellungen

Im Spiel über den Konfigurations-Knopf in der Mod-Liste änderbar, oder direkt in
`.minecraft/config/supportblocks-client.toml`. Änderungen wirken sofort.

| Option | Standard | Beschreibung |
|---|---|---|
| `enabledOnStartup` | `false` | Rahmen schon beim Spielstart anzeigen |
| `onlyPlayers` | `false` | Nur Spieler markieren statt aller Entities |
| `radius` | `64` | Maximale Entfernung in Blöcken (1 - 256) |
| `red` / `green` / `blue` | `0` / `1` / `0` | Rahmenfarbe (0.0 - 1.0) |
| `alpha` | `1` | Deckkraft (0.0 - 1.0) |

## Ausprobieren

1. Einzelspielerwelt öffnen (Cheats an) und ein paar Mobs spawnen.
2. Alle unsichtbar machen:
   ```
   /effect give @e minecraft:invisibility 999 0 true
   ```
3. **F8** drücken: Unter jedem unsichtbaren Mob erscheint ein grüner Rahmen.

## Selbst bauen

Benötigt **JDK 21**:

```bash
git clone https://github.com/Brennerofhell/Debug-Render-Minecraft-.git
cd Debug-Render-Minecraft-
./gradlew build        # Windows: gradlew.bat build
```

Die fertige Mod liegt danach in `build/libs/`.

### Neue Version veröffentlichen

1. `mod_version` in `gradle.properties` erhöhen (z. B. `1.0.1`).
2. Auf `main` pushen bzw. mergen.

GitHub Actions baut die Mod und erstellt automatisch das Release `v1.0.1` mit der `.jar` zum Download.
Alternativ geht das auch per Tag (`git tag v1.0.1 && git push origin v1.0.1`) oder über
*Actions → Release → Run workflow*.

## Lizenz

[GNU General Public License v3.0 oder neuer](LICENSE)

# Passive Skill Tree for NeoForge 1.21.1 (unofficial port)

This is an **unofficial** port of [Passive Skill Tree](https://github.com/Daripher/Passive-Skill-Tree) by **Daripher**
from Minecraft 1.20.1 Forge to Minecraft 1.21.1 NeoForge. All credit for the mod's design, code, art and data goes to Daripher.
Please do not report bugs in this port to the original author. Report them in this repository's issues.

- Mod id: `skilltree`. The skill and skill tree JSON formats are unchanged.
- Minecraft 1.21.1, NeoForge 21.1.x, Java 21
- Required: [Curios API](https://modrinth.com/mod/curios) 9.x and [AttributeFix](https://modrinth.com/mod/attributefix) 21.1.x (which needs Bookshelf and Prickle)
- Optional: JEI, Apothic Attributes, Easy Magic
- Not supported in this port: Apotheosis and Tetra integration (see `PORTING_NOTES.md`)

ItemProductionLib (also by Daripher) has no 1.21.1 version, so the parts of it this mod uses are included in this jar.
The socket tooltip renderer is adapted from Apotheosis by Shadows_of_Fire.

## Source code and changes

Source code: https://github.com/Horizzon9/Passive-Skill-Tree-NeoForge

This work is a modified version of Passive Skill Tree 0.6.14a (Minecraft 1.20.1, Forge) by Daripher.
It was modified on 2026-10-25 to run on Minecraft 1.21.1 with NeoForge. `PORTING_NOTES.md` lists the changes.

## Building

```
./gradlew build
```

The jar is written to `build/libs/PassiveSkillTree-1.21.1-NeoForge-<version>.jar`.
`./gradlew runClient` and `./gradlew runServer` start a development game.

## License

Licensed under the GNU General Public License v3, like the original mod. See `LICENSE`.

    Passive Skill Tree (NeoForge port) Copyright (C) Daripher and contributors.
    This program comes with ABSOLUTELY NO WARRANTY. This is free software, and you are welcome
    to redistribute it under the conditions of the GPL v3.
`PORTING_NOTES.md` lists every change made for 1.21.1.

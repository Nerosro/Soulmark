# Soulmark

A shared player framework mod for **Minecraft 26.1.2** built on **NeoForge**.

Soulmark provides the core player data systems that multiple job mods — such as **Elemancy** — can build on top of. It stores and exposes baseline player stats while letting job mods decide what those values mean in gameplay.

## Features

### Mana System
- **Mana pool** and **mana regen rate** are rolled once on first spawn and inversely tied — higher pool means lower regen and vice versa.
- Mana regeneration resumes after a configurable delay following each cast.
- Mana is personal, internal, and cannot be bypassed — no free casting.
- Pool and regen can be modified at runtime by traits, gear, trinkets, and skills.

### Affinity
- A core magical leaning randomly assigned at character creation.
- Job mods interpret affinity in their own way (e.g. elemental attunement, alignment triggers).

### Trait System
- Each player receives **3 traits** on first spawn: one Boost, one Neutral, and one Penalty.
- Traits are selected via **weighted random rolls** with rarity tiers: Common, Uncommon, Rare, Legendary, and Exotic.
- Constraints enforced:
  - Maximum **1 Legendary+** trait per player.
  - At least **1 Rare+** among the three traits.
  - The exclusive **Markless** trait cancels all other traits.
- Trait content is provided by job mods; Soulmark owns the framework and rolling logic. Job mods may register multiple variants of a trait at different weight tiers.

### Skill Tree
- A generic unlock framework for skill nodes.
- Tracks unlocked nodes and points spent per player.
- Supports configurable point caps.
- Job mods define the actual tree structure and node content.

### Shared Stat Framework
| Stat | Description |
|------|-------------|
| Mana Pool | Total available mana capacity |
| Mana Regen | Baseline mana recovery rate |
| Mana Regen Delay | Delay before recovery resumes after casting |
| Affinity | Natural magical resonance |
| Traits | Rolled trait set (Boost / Neutral / Penalty) |

## Design Philosophy

- **Soulmark stores the truth** — job mods present it through their own class-specific tools (Tome, adventurer's license, orb, etc.).
- **No universal stat screen** — each job mod filters for what matters to its players.
- **Cross-mod interaction is intentional** — combined installs should feel deeper, not isolated.
- **Futureproof early, implement incrementally** — data structures anticipate real extension points, but complexity is added in small steps.

## Dependencies

- **Minecraft** 26.1.2
- **NeoForge** 26.1.2.48-beta
- **Curios API** 15.0.0-beta.2

## License

MIT License

# Kits

A lightweight, modern, and fully configurable Minecraft kit management plugin designed for Spigot/Paper 1.8 servers.

---

## Features

- **Interactive Kit Selector (`/kit`):** Fully customizable GUI with border panes, filler items, and status indicators.
- **In-Game Visual Kit Editor (`/kit edit`):** 
  - 36-slot interactive inventory contents editor.
  - Dedicated armor slot configuration (Helmet, Chestplate, Leggings, Boots).
  - Quick-import buttons to copy your player's items, armor, or potion effects.
  - Change kit icons on the fly by holding any item.
  - Auto-saving on inventory close.
- **Dynamic Color Formatting:** Clean `&` color code translations across all items, lores, and configurations.
- **YAML Storage:** Human-readable `.yml` files per kit in `plugins/Kits/kits/` with automatic default kit unpacking (`archer.yml`).
- **Permission Support:** Per-kit permissions with distinct available vs. locked GUI display states.

---

## Commands & Permissions

| Command | Permission | Description |
|---|---|---|
| `/kit` | `kit.command` | Opens the kit selector GUI. |
| `/kit <name>` | `kit.command` | Equips the specified kit directly. |
| `/kit list` | `kit.command` | Lists all available kits in chat. |
| `/kit edit [kit]` | `kit.admin` | Opens the interactive visual kit editor. |
| `/kit create <name>` | `kit.admin` | Creates a new kit from your current inventory. |
| `/kit delete <name>` | `kit.admin` | Deletes an existing kit from memory and disk. |
| `/kit give <player> <kit>` | `kit.admin` | Gives a kit to another player. |
| `/kit reload` | `kit.admin` | Reloads all kit and menu configurations. |
| `/kit help` | `kit.command` | Displays the help menu with all available commands. |

---

## Configuration

### `menus.yml`
Controls the layout, size, and styling of the `/kit` selector interface:
- **`size`:** 9, 18, 27, 36, 45, or 54 slots.
- **`border`:** Enable/disable decorative glass pane borders.
- **`filler`:** Fill empty background slots.
- **`kit-slots`:** Assign specific kits to exact slot positions (unassigned kits will automatically fill open slots).
- **`kit-display`:** Customize the item name and lore formats for both available and locked kits.

### `kits/<name>.yml`
Each kit is stored in its own YAML file with:
- `name` & `displayName`
- `permission`
- `description`
- `icon` (serialized item data)
- `armor` (helmet, chestplate, leggings, boots)
- `contents` (slots 0–35)
- `potionEffects` (type, amplifier, duration)

---

## Building from Source

Requirements:
- Java 8 or higher
- Maven 3.6+
- Commando

```bash
mvn clean package

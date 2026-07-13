
# PointBlank Recipe

PointBlank Recipe is a Minecraft mod designed to replace the simple crafting system of Vic's Point Blank with a more complex, modular approach. Instead of crafting weapons using vanilla materials directly, this mod introduces specialized weapon components that must be assembled in a workbench, providing a more balanced and immersive experience.

## Key Features
*   **Modular Crafting:** Requires the creation of specific base parts and mechanical components before assembling the final weapon.
*   **Balanced Progression:** Crafts are designed to reflect the physical complexity and shape of the corresponding weapon model.
*   **Expansion Support:** Designed to work with various external weapon packs.
*   **Configuration and Datapacks:** Highly customizable system to disable or modify recipes via configuration files or data packs.

## Configuration and Customization

You can control which recipes are active using two distinct methods, depending on your needs.

### 1. The Config File (Broad Control)
The configuration file is the quickest way to disable entire categories of content at once. This is ideal if you want to remove specific weapon packs (like S.T.A.L.K.E.R. or Metro 2033) or disable all recipes globally.

*   **File location:** `config/pointblank_recipe.json`
*   **Behavior:** Changes require a full game or server restart to take effect.

**Example configuration:**
```json
{
  "disabledModules": [
    "metro",
    "base"
  ]
}
```
*Available default modules: base, trauma, metro, stalker, handguns_galore, cyberpunk.*

### 2. Datapacks (Dynamic and Precise Control)
If you require surgical control, such as changing specific ingredients for a single item, deleting individual recipes, or overriding module states dynamically without a restart, use a standard Minecraft datapack.

*   **Structure:** Create a folder in your world's `datapacks` directory (e.g., `your_pack_name/data/pointblank_recipe/pointblank_modules/base.json`).
*   **Behavior:** Supports dynamic reloading. You can use the `/reload` command in-game to apply changes instantly.

**Example module file (`base.json`):**
```json
{
  "enabled": false
}
```

### KubeJS
See documentation here: [kubejs.docs.md](https://github.com/Samiker69/PointBlankRecipe/blob/main/kubejs.docs.md)

### Comparison
| Method | Best For | Requirement |
| :--- | :--- | :--- |
| **Config File** | Disabling entire categories | Requires Game/Server Restart |
| **Datapacks** | Fine-tuning recipes, per-world changes | Supports `/reload` command |

## Dependencies
*   **Vic's Point Blank (-jelly):** Required dependency.
*   **JEI (Just Enough Items):** Highly recommended for viewing recipes.
*   **Polymorph:** Highly recommended if you are using external weapon packs to avoid recipe conflicts.

## Build Instructions

This project is built using Gradle. Depending on which version you are compiling, you must have the corresponding Java Development Kit (JDK) installed:

| Minecraft Version | Required Java Version |
| :--- | :--- |
| 1.20.1 | Java 17 |
| 1.21.1 | Java 21 |
| 1.21.11 | Java 25 |

To compile the mod, run the following command in the terminal from the root directory:

```bash
./gradlew build
```

The build process will produce the artifacts for each version in their respective subdirectories.

## Q&A

**Q: Can I use this mod in a modpack?**
A: Yes, you are free to include this mod in any modpack.

**Q: Why do I see errors in my logs?**
A: You see recipe-related errors because you are missing the specific weapon add-ons that the recipes reference. These errors are non-critical and can be safely ignored.

**Q: Is this mod created by the developer of Vic's Point Blank?**
A: No, this is an independent project. Please do not contact the original Vic's Point Blank developers for support regarding this mod.

## Contribution and Licensing

This project is licensed under the **MIT License**.

### Contribution Policy
We welcome contributions from the community to help improve the mod. If you have improvements, bug fixes, or new features, please submit a Pull Request.

### Distribution Policy
While you are encouraged to contribute to the codebase, you are **strictly prohibited** from re-uploading, redistributing, or claiming this project as your own on any platform. Respect the work put into this development.

For support, bug reporting, or to discuss development ideas, please join the official [Discord server](https://discord.gg/EC6jfnnjX4).
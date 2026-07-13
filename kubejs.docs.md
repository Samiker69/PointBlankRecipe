# KubeJS Recipe Integration

This document guides modpack developers and players on how to customize crafting recipes for PointBlank Recipe items using KubeJS, integrating components from mods like Create and Mekanism.

## Introduction

PointBlank Recipe introduces a modular crafting system for firearms. While the mod provides a default set of recipes, KubeJS allows for advanced customization, enabling you to integrate specific components from other technical mods (e.g., Create, Mekanism) into the crafting progression. This provides greater control over balancing and item acquisition within your modpack.

## Prerequisites

Before you begin, ensure you have the following installed:

*   **PointBlank Recipe** (and its dependencies)
*   **KubeJS** 
*   **Create** (if you plan to use Create components in recipes)
*   **Mekanism** (if you plan to use Mekanism components in recipes)
*   A text editor for writing KubeJS scripts

## Find PointBlank Recipe Item IDs

To modify a recipe, you need the exact item ID of the PointBlank Recipe item.

1.  Launch Minecraft.
2.  Enable advanced tooltips by pressing `F3 + H`.
3.  Hover over any PointBlank Recipe item in your inventory or JEI.
4.  The item ID will be displayed (e.g., `pointblank_recipe:triggermechanism`).

Or go to [github](https://github.com/Samiker69/PointBlankRecipe) to see all recipes

## KubeJS Script Examples

Create your KubeJS scripts in the `kubejs/server_scripts/` directory of your Minecraft instance. For example, you might create a file named `custom_recipes.js`.

### Example 
You can find it here: [pbr_kjs_example.js](https://github.com/Samiker69/PointBlankRecipe/blob/main/kubejs/server_scripts/pbr_kjs_example.js)

## Remove Existing PointBlank Recipe Recipes

To replace a default recipe with a KubeJS custom recipe, first remove the original.

```js
// kubejs/server_scripts/custom_recipes.js
ServerEvents.recipes(event => {
    // Remove a specific recipe by its output item
    event.remove({ output: 'pointblank_recipe:riflemagazine' })

    // You can also remove all recipes for a specific mod
    // event.remove({ mod: 'pointblank_recipe' })
})
```

## Additional Tips

*   **Reloading KubeJS:** After making changes to your KubeJS scripts, use the command `/kubejs reload server_scripts` in-game to apply them without restarting Minecraft.
*   **JEI Integration:** KubeJS recipes automatically show up in Just Enough Items (JEI), allowing players to easily view your custom recipes.
*   **Recipe Types:** Explore the KubeJS documentation to discover all available recipe types (e.g., `create.mixing`, `mekanism.enriching`).
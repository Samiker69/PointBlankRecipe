package xyz.samiker.pointblank_recipe.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import xyz.samiker.pointblank_recipe.modular.ModularLayout;
import xyz.samiker.pointblank_recipe.modular.ModularTemplate;
import xyz.samiker.pointblank_recipe.screen.ModularWorkbenchMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static xyz.samiker.pointblank_recipe.screen.ModularWorkbenchMenu.*;

// Drawn with plain fills. Empty slots show the parts the shown gun still needs as faded ghost items.
public class ModularWorkbenchScreen extends AbstractContainerScreen<ModularWorkbenchMenu> {
    private static final int PANEL = 0xFFC6C6C6;
    private static final int SHADOW = 0xFF373737;
    private static final int LIGHT = 0xFFFFFFFF;
    private static final int SLOT = 0xFF8B8B8B;
    private static final int TEXT = 0xFF404040;
    // Slot color drawn over a ghost item so it reads as a hint, not as a real item.
    private static final int GHOST_FADE = 0xA08B8B8B;

    private final List<Button> resultButtons = new ArrayList<>();

    public ModularWorkbenchScreen(ModularWorkbenchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, ModularLayout.WIDTH, ModularLayout.HEIGHT);
        inventoryLabelY = ModularLayout.INVENTORY_Y - 11;
    }

    @Override
    protected void init() {
        super.init();
        addButton("<", BUTTON_PREVIOUS_TEMPLATE, 6, 4);
        addButton(">", BUTTON_NEXT_TEMPLATE, ModularLayout.WIDTH - 18, 4);
        resultButtons.clear();
        resultButtons.add(addButton("<", BUTTON_PREVIOUS_RESULT, ModularLayout.RESULT_X - 5, ModularLayout.RESULT_Y + 22));
        resultButtons.add(addButton(">", BUTTON_NEXT_RESULT, ModularLayout.RESULT_X + 9, ModularLayout.RESULT_Y + 22));
    }

    private Button addButton(String label, int id, int left, int top) {
        return addRenderableWidget(Button.builder(Component.literal(label), button -> {
            if (minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
            }
        }).bounds(leftPos + left, topPos + top, 12, 12).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        // Arrows only make sense when the parts can become more than one gun.
        resultButtons.forEach(button -> button.visible = menu.matches() > 1);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int width = ModularLayout.WIDTH;
        int height = ModularLayout.HEIGHT;
        graphics.fill(leftPos, topPos, leftPos + width, topPos + height, SHADOW);
        graphics.fill(leftPos + 1, topPos + 1, leftPos + width - 1, topPos + height - 1, PANEL);

        ModularTemplate template = menu.template();
        if (template != null) {
            for (String slot : template.slots()) {
                int[] position = ModularLayout.slotPosition(slot);
                drawSlot(graphics, position[0], position[1], 18);
            }
        }
        drawSlot(graphics, ModularLayout.RESULT_X - 4, ModularLayout.RESULT_Y - 4, 26);
        for (int i = 0; i < 36; i++) {
            int row = i / 9;
            drawSlot(graphics, 8 + i % 9 * 18, row < 3 ? ModularLayout.INVENTORY_Y + row * 18 : ModularLayout.INVENTORY_Y + 58, 18);
        }

        for (Slot slot : menu.slots) {
            ItemStack ghost = menu.ghost(slot);
            if (slot.isActive() && !slot.hasItem() && !ghost.isEmpty()) {
                int x = leftPos + slot.x;
                int y = topPos + slot.y;
                graphics.fakeItem(ghost, x, y);
                graphics.itemDecorations(font, ghost, x, y);
                graphics.fill(x, y, x + 16, y + 16, GHOST_FADE);
            }
        }
    }

    // A vanilla-style sunken slot whose item area starts at slotX, slotY.
    private void drawSlot(GuiGraphicsExtractor graphics, int slotX, int slotY, int size) {
        int left = leftPos + slotX - 1;
        int top = topPos + slotY - 1;
        graphics.fill(left, top, left + size, top + size, SHADOW);
        graphics.fill(left + 1, top + 1, left + size, top + size, LIGHT);
        graphics.fill(left + 1, top + 1, left + size - 1, top + size - 1, SLOT);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, TEXT, false);

        Component template = Component.translatable("template.pointblank_recipe." + (menu.template() == null ? "none" : menu.template().name()));
        graphics.text(font, template, (ModularLayout.WIDTH - font.width(template)) / 2, 6, TEXT, false);

        if (menu.matches() > 1) {
            String counter = (menu.selected() + 1) + "/" + menu.matches();
            graphics.text(font, counter, ModularLayout.RESULT_X + 8 - font.width(counter) / 2, ModularLayout.RESULT_Y - 15, TEXT, false);
        }
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        Slot slot = hoveredSlot;
        if (slot != null && !slot.hasItem() && menu.getCarried().isEmpty()) {
            List<Component> lines = new ArrayList<>();
            String name = ModularWorkbenchMenu.slotName(slot);
            if (name != null) {
                lines.add(Component.translatable("slot.pointblank_recipe." + name));
            }
            ItemStack ghost = menu.ghost(slot);
            if (!ghost.isEmpty()) {
                lines.addAll(getTooltipFromItem(minecraft, ghost));
            }
            if (!lines.isEmpty()) {
                graphics.setTooltipForNextFrame(font, lines, Optional.empty(), mouseX, mouseY);
                return;
            }
        }
        super.extractTooltip(graphics, mouseX, mouseY);
    }
}

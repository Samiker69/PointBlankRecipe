package xyz.samiker.pointblank_recipe.client;

import net.minecraft.client.gui.GuiGraphics;
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
        super(menu, inventory, title);
        imageWidth = ModularLayout.WIDTH;
        imageHeight = ModularLayout.HEIGHT;
        inventoryLabelY = ModularLayout.INVENTORY_Y - 11;
    }

    @Override
    protected void init() {
        super.init();
        addButton("<", BUTTON_PREVIOUS_TEMPLATE, 6, 4);
        addButton(">", BUTTON_NEXT_TEMPLATE, imageWidth - 18, 4);
        resultButtons.clear();
        resultButtons.add(addButton("<", BUTTON_PREVIOUS_RESULT, ModularLayout.RESULT_X - 5, ModularLayout.RESULT_Y + 22));
        resultButtons.add(addButton(">", BUTTON_NEXT_RESULT, ModularLayout.RESULT_X + 9, ModularLayout.RESULT_Y + 22));
    }

    private Button addButton(String label, int id, int left, int top) {
        return addRenderableWidget(Button.builder(Component.literal(label), button -> {
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
            }
        }).bounds(leftPos + left, topPos + top, 12, 12).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Arrows only make sense when the parts can become more than one gun.
        resultButtons.forEach(button -> button.visible = menu.matches() > 1);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, SHADOW);
        graphics.fill(leftPos + 1, topPos + 1, leftPos + imageWidth - 1, topPos + imageHeight - 1, PANEL);

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
                graphics.renderFakeItem(ghost, x, y);
                graphics.renderItemDecorations(font, ghost, x, y);
                graphics.fill(x, y, x + 16, y + 16, GHOST_FADE);
            }
        }
    }

    // A vanilla-style sunken slot whose item area starts at slotX, slotY.
    private void drawSlot(GuiGraphics graphics, int slotX, int slotY, int size) {
        int left = leftPos + slotX - 1;
        int top = topPos + slotY - 1;
        graphics.fill(left, top, left + size, top + size, SHADOW);
        graphics.fill(left + 1, top + 1, left + size, top + size, LIGHT);
        graphics.fill(left + 1, top + 1, left + size - 1, top + size - 1, SLOT);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, TEXT, false);

        Component template = Component.translatable("template.pointblank_recipe." + (menu.template() == null ? "none" : menu.template().name()));
        graphics.drawString(font, template, (imageWidth - font.width(template)) / 2, 6, TEXT, false);

        if (menu.matches() > 1) {
            String counter = (menu.selected() + 1) + "/" + menu.matches();
            graphics.drawString(font, counter, ModularLayout.RESULT_X + 8 - font.width(counter) / 2,
                    ModularLayout.RESULT_Y - 15, TEXT, false);
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
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
        super.renderTooltip(graphics, mouseX, mouseY);
    }
}

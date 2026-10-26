package xyz.samiker.pointblank_recipe.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import xyz.samiker.pointblank_recipe.modular.ModularLayout;
import xyz.samiker.pointblank_recipe.modular.ModularTemplate;
import xyz.samiker.pointblank_recipe.screen.ModularWorkbenchMenu;

import java.util.ArrayList;
import java.util.List;

import static xyz.samiker.pointblank_recipe.screen.ModularWorkbenchMenu.*;

// Drawn with plain fills: the slots sit on a gun silhouette built from the parts of the selected template.
public class ModularWorkbenchScreen extends AbstractContainerScreen<ModularWorkbenchMenu> {
    private static final int PANEL = 0xFFC6C6C6;
    private static final int SHADOW = 0xFF373737;
    private static final int LIGHT = 0xFFFFFFFF;
    private static final int SLOT = 0xFF8B8B8B;
    private static final int TEXT = 0xFF404040;

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
        // Arrows only make sense when several guns use the same parts.
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
        for (ModularLayout.Rect rect : ModularLayout.silhouette(template)) {
            graphics.fill(leftPos + rect.x1() - 1, topPos + rect.y1() - 1, leftPos + rect.x2() + 1, topPos + rect.y2() + 1, ModularLayout.SILHOUETTE_OUTLINE);
        }
        for (ModularLayout.Rect rect : ModularLayout.silhouette(template)) {
            graphics.fill(leftPos + rect.x1(), topPos + rect.y1(), leftPos + rect.x2(), topPos + rect.y2(), ModularLayout.SILHOUETTE_FILL);
        }
        for (String slot : template.slots()) {
            int[] position = ModularLayout.slotPosition(slot);
            drawSlot(graphics, position[0], position[1], 18);
        }
        drawSlot(graphics, ModularLayout.RESULT_X - 4, ModularLayout.RESULT_Y - 4, 26);
        for (int i = 0; i < 36; i++) {
            int row = i / 9;
            drawSlot(graphics, 8 + i % 9 * 18, row < 3 ? ModularLayout.INVENTORY_Y + row * 18 : ModularLayout.INVENTORY_Y + 58, 18);
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

        Component template = Component.translatable("template.pointblank_recipe." + menu.template().name());
        graphics.text(font, template, (ModularLayout.WIDTH - font.width(template)) / 2, 6, TEXT, false);

        if (menu.matches() > 1) {
            String counter = (menu.selected() + 1) + "/" + menu.matches();
            graphics.text(font, counter, ModularLayout.RESULT_X + 8 - font.width(counter) / 2, ModularLayout.RESULT_Y - 15, TEXT, false);
        }
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        Slot slot = hoveredSlot;
        String name = slot == null ? null : ModularWorkbenchMenu.slotName(slot);
        if (name != null && !slot.hasItem() && menu.getCarried().isEmpty()) {
            graphics.setTooltipForNextFrame(font, Component.translatable("slot.pointblank_recipe." + name), mouseX, mouseY);
            return;
        }
        super.extractTooltip(graphics, mouseX, mouseY);
    }
}

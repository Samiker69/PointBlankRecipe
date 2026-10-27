package xyz.samiker.pointblank_recipe.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import xyz.samiker.pointblank_recipe.modular.ModularLayout;
import xyz.samiker.pointblank_recipe.modular.ModularTemplate;
import xyz.samiker.pointblank_recipe.screen.ModularWorkbenchScreenHandler;

import java.util.ArrayList;
import java.util.List;

import static xyz.samiker.pointblank_recipe.screen.ModularWorkbenchScreenHandler.*;

// Drawn with plain fills: the slots sit on a gun silhouette built from the parts of the selected template.
public class ModularWorkbenchScreen extends HandledScreen<ModularWorkbenchScreenHandler> {
    private static final int PANEL = 0xFFC6C6C6;
    private static final int SHADOW = 0xFF373737;
    private static final int LIGHT = 0xFFFFFFFF;
    private static final int SLOT = 0xFF8B8B8B;
    private static final int TEXT = 0xFF404040;

    private final List<ButtonWidget> resultButtons = new ArrayList<>();

    public ModularWorkbenchScreen(ModularWorkbenchScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        backgroundWidth = ModularLayout.WIDTH;
        backgroundHeight = ModularLayout.HEIGHT;
        playerInventoryTitleY = ModularLayout.INVENTORY_Y - 11;
    }

    @Override
    protected void init() {
        super.init();
        addButton("<", BUTTON_PREVIOUS_TEMPLATE, 6, 4);
        addButton(">", BUTTON_NEXT_TEMPLATE, backgroundWidth - 18, 4);
        resultButtons.clear();
        resultButtons.add(addButton("<", BUTTON_PREVIOUS_RESULT, ModularLayout.RESULT_X - 5, ModularLayout.RESULT_Y + 22));
        resultButtons.add(addButton(">", BUTTON_NEXT_RESULT, ModularLayout.RESULT_X + 9, ModularLayout.RESULT_Y + 22));
    }

    private ButtonWidget addButton(String label, int id, int left, int top) {
        return addDrawableChild(ButtonWidget.builder(Text.literal(label), button -> {
            if (client != null && client.interactionManager != null) {
                client.interactionManager.clickButton(handler.syncId, id);
            }
        }).dimensions(x + left, y + top, 12, 12).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Arrows only make sense when several guns use the same parts.
        resultButtons.forEach(button -> button.visible = handler.matches() > 1);
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.fill(x, y, x + backgroundWidth, y + backgroundHeight, SHADOW);
        context.fill(x + 1, y + 1, x + backgroundWidth - 1, y + backgroundHeight - 1, PANEL);

        ModularTemplate template = handler.template();
        for (ModularLayout.Rect rect : ModularLayout.silhouette(template)) {
            context.fill(x + rect.x1() - 1, y + rect.y1() - 1, x + rect.x2() + 1, y + rect.y2() + 1, ModularLayout.SILHOUETTE_OUTLINE);
        }
        for (ModularLayout.Rect rect : ModularLayout.silhouette(template)) {
            context.fill(x + rect.x1(), y + rect.y1(), x + rect.x2(), y + rect.y2(), ModularLayout.SILHOUETTE_FILL);
        }
        for (String slot : template.slots()) {
            int[] position = ModularLayout.slotPosition(slot);
            drawSlot(context, position[0], position[1], 18);
        }
        drawSlot(context, ModularLayout.RESULT_X - 4, ModularLayout.RESULT_Y - 4, 26);
        for (int i = 0; i < 36; i++) {
            int row = i / 9;
            drawSlot(context, 8 + i % 9 * 18, row < 3 ? ModularLayout.INVENTORY_Y + row * 18 : ModularLayout.INVENTORY_Y + 58, 18);
        }
    }

    // A vanilla-style sunken slot whose item area starts at slotX, slotY.
    private void drawSlot(DrawContext context, int slotX, int slotY, int size) {
        int left = x + slotX - 1;
        int top = y + slotY - 1;
        context.fill(left, top, left + size, top + size, SHADOW);
        context.fill(left + 1, top + 1, left + size, top + size, LIGHT);
        context.fill(left + 1, top + 1, left + size - 1, top + size - 1, SLOT);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(textRenderer, playerInventoryTitle, playerInventoryTitleX, playerInventoryTitleY, TEXT, false);

        Text template = Text.translatable("template.pointblank_recipe." + handler.template().name());
        context.drawText(textRenderer, template, (backgroundWidth - textRenderer.getWidth(template)) / 2, 6, TEXT, false);

        if (handler.matches() > 1) {
            String counter = (handler.selected() + 1) + "/" + handler.matches();
            context.drawText(textRenderer, counter, ModularLayout.RESULT_X + 8 - textRenderer.getWidth(counter) / 2,
                    ModularLayout.RESULT_Y - 15, TEXT, false);
        }
    }

    @Override
    protected void drawMouseoverTooltip(DrawContext context, int mouseX, int mouseY) {
        Slot slot = focusedSlot;
        String name = slot == null ? null : ModularWorkbenchScreenHandler.slotName(slot);
        if (name != null && !slot.hasStack() && handler.getCursorStack().isEmpty()) {
            context.drawTooltip(textRenderer, Text.translatable("slot.pointblank_recipe." + name), mouseX, mouseY);
            return;
        }
        super.drawMouseoverTooltip(context, mouseX, mouseY);
    }
}

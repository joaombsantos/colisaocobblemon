package me.marcronte.colisaocobblemon.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import me.marcronte.colisaocobblemon.features.eventblock.PokemonBlockadeMenu;
import me.marcronte.colisaocobblemon.network.EventNetwork;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

public class PokemonBlockadeScreen extends AbstractContainerScreen<PokemonBlockadeMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "textures/gui/event_block.png");

    private static final int TEXTURE_WIDTH = 176;
    private static final int TEXTURE_HEIGHT = 186;

    private EditBox pokemonPropsField;
    private EditBox eventIdField;
    private EditBox checkMessageField;
    private EditBox wakeMessageField;
    private EditBox hitboxSizeField;
    private Checkbox catchableBox;

    public PokemonBlockadeScreen(PokemonBlockadeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = TEXTURE_WIDTH;
        this.imageHeight = TEXTURE_HEIGHT;
        this.inventoryLabelY = this.imageHeight - 94;
        this.titleLabelY = 5;
    }

    @Override
    protected void init() {
        super.init();

        // 1. Props
        this.pokemonPropsField = new EditBox(this.font, this.leftPos + 25, this.topPos + 16, 125, 12, Component.literal("Props"));
        this.pokemonPropsField.setMaxLength(256);
        this.addRenderableWidget(this.pokemonPropsField);

        // 2. ID
        this.eventIdField = new EditBox(this.font, this.leftPos + 25, this.topPos + 30, 80, 12, Component.literal("ID"));
        this.eventIdField.setMaxLength(32);
        this.addRenderableWidget(this.eventIdField);

        // 3. Checkbox
        this.catchableBox = Checkbox.builder(Component.translatable("message.colisao-cobblemon.capturable"), this.font)
                .pos(this.leftPos + 110, this.topPos + 30)
                .selected(true)
                .build();
        this.addRenderableWidget(this.catchableBox);

        // 4. Check Message
        this.checkMessageField = new EditBox(this.font, this.leftPos + 25, this.topPos + 44, 125, 12, Component.literal("Msg1"));
        this.checkMessageField.setMaxLength(256);
        this.addRenderableWidget(this.checkMessageField);

        // 5. Wake Message
        this.wakeMessageField = new EditBox(this.font, this.leftPos + 25, this.topPos + 58, 125, 12, Component.literal("Msg2"));
        this.wakeMessageField.setMaxLength(256);
        this.addRenderableWidget(this.wakeMessageField);

        // 6. Hitbox Size
        this.hitboxSizeField = new EditBox(this.font, this.leftPos + 25, this.topPos + 72, 15, 14, Component.literal("Size"));
        this.hitboxSizeField.setMaxLength(1);
        this.addRenderableWidget(this.hitboxSizeField);

        // 7. Save
        this.addRenderableWidget(Button.builder(Component.translatable("message.colisao-cobblemon.save"), button -> save())
                .pos(this.leftPos + 110, this.topPos + 72)
                .size(40, 14)
                .build());

        // Load Data
        this.pokemonPropsField.setValue(this.menu.loadedProps);
        this.eventIdField.setValue(this.menu.loadedEventId);
        this.checkMessageField.setValue(this.menu.loadedCheckMessage);
        this.wakeMessageField.setValue(this.menu.loadedWakeMessage);
        this.hitboxSizeField.setValue(String.valueOf(this.menu.loadedHitboxSize));

        if (this.catchableBox != null && this.menu.loadedCatchable != this.catchableBox.selected()) {
            this.catchableBox.onPress();
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }
        if (this.pokemonPropsField.isFocused() || this.eventIdField.isFocused() ||
                this.checkMessageField.isFocused() || this.wakeMessageField.isFocused() ||
                this.hitboxSizeField.isFocused()) {
            if (this.minecraft != null && this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
                return true;
            }
            if (this.pokemonPropsField.keyPressed(keyCode, scanCode, modifiers) ||
                    this.eventIdField.keyPressed(keyCode, scanCode, modifiers) ||
                    this.checkMessageField.keyPressed(keyCode, scanCode, modifiers) ||
                    this.wakeMessageField.keyPressed(keyCode, scanCode, modifiers) ||
                    this.hitboxSizeField.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void save() {
        if (this.menu.pos != null) {
            int size = 1;
            try {
                size = Integer.parseInt(hitboxSizeField.getValue());
            } catch (NumberFormatException ignored) {}

            ClientPlayNetworking.send(new EventNetwork.SaveBlockadePayload(
                    this.menu.pos,
                    this.pokemonPropsField.getValue(),
                    this.eventIdField.getValue(),
                    this.catchableBox.selected(),
                    this.checkMessageField.getValue(),
                    this.wakeMessageField.getValue(),
                    size
            ));
        }
        this.onClose();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        // Labels
        guiGraphics.drawString(this.font, "Pk:", this.leftPos + 5, this.topPos + 18, 0x404040, false);
        guiGraphics.drawString(this.font, "ID:", this.leftPos + 5, this.topPos + 32, 0x404040, false);
        guiGraphics.drawString(this.font, "M1:", this.leftPos + 5, this.topPos + 46, 0x404040, false);
        guiGraphics.drawString(this.font, "M2:", this.leftPos + 5, this.topPos + 60, 0x404040, false);
        guiGraphics.drawString(this.font, "Box:", this.leftPos + 5, this.topPos + 75, 0x404040, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
    }
}
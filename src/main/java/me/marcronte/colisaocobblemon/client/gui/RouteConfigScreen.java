package me.marcronte.colisaocobblemon.client.gui;

import me.marcronte.colisaocobblemon.network.payloads.SaveRoutePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class RouteConfigScreen extends Screen {

    private EditBox nameBox;

    public RouteConfigScreen() {
        super(Component.translatable("message.colisao-cobblemon.route_settings"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.nameBox = new EditBox(this.font, centerX - 100, centerY - 20, 200, 20, Component.translatable("message.colisao-cobblemon.route_name"));
        this.nameBox.setMaxLength(32);
        this.addRenderableWidget(this.nameBox);

        this.setInitialFocus(this.nameBox);

        this.addRenderableWidget(Button.builder(Component.translatable("message.colisao-cobblemon.save"), button -> save())
                .bounds(centerX - 105, centerY + 10, 100, 20)
                .build());

        this.addRenderableWidget(Button.builder(Component.translatable("message.colisao-cobblemon.cancel"), button -> this.onClose())
                .bounds(centerX + 5, centerY + 10, 100, 20)
                .build());
    }

    private void save() {
        String name = this.nameBox.getValue();
        if (name == null || name.isBlank()) return;

        ClientPlayNetworking.send(new SaveRoutePayload(name));

        this.onClose();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.drawCenteredString(this.font, "Definir Nome da Rota", this.width / 2, this.height / 2 - 40, 0xFFFFFF);

        guiGraphics.drawCenteredString(this.font, "(Deve ser igual ao nome no routes.json)", this.width / 2, this.height / 2 - 50, 0xAAAAAA);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            save();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
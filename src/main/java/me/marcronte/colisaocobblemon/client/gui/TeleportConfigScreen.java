package me.marcronte.colisaocobblemon.client.gui;

import me.marcronte.colisaocobblemon.network.TeleportNetwork;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class TeleportConfigScreen extends Screen {
    private final BlockPos blockPos;
    private EditBox xBox;
    private EditBox yBox;
    private EditBox zBox;
    private EditBox yawBox;

    public TeleportConfigScreen(BlockPos blockPos) {
        super(Component.translatable("message.colisao-cobblemon.teleport_configs"));
        this.blockPos = blockPos;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // X
        this.xBox = new EditBox(this.font, centerX - 100, centerY - 60, 200, 20, Component.literal("X"));
        this.xBox.setHint(Component.literal("x"));
        this.addRenderableWidget(this.xBox);

        // Y
        this.yBox = new EditBox(this.font, centerX - 100, centerY - 35, 200, 20, Component.literal("Y"));
        this.yBox.setHint(Component.literal("y"));
        this.addRenderableWidget(this.yBox);

        // Z
        this.zBox = new EditBox(this.font, centerX - 100, centerY - 10, 200, 20, Component.literal("Z"));
        this.zBox.setHint(Component.literal("z"));
        this.addRenderableWidget(this.zBox);

        // --- Rotation ---
        this.yawBox = new EditBox(this.font, centerX - 100, centerY + 15, 200, 20, Component.literal("Yaw"));
        this.yawBox.setHint(Component.literal("(0=Sul, 90=Oeste, 180=Norte, -90=Leste)"));
        //this.yawBox.setValue("0");
        this.addRenderableWidget(this.yawBox);

        // Buttons
        this.addRenderableWidget(Button.builder(Component.translatable("message.colisao-cobblemon.save"), button -> save())
                .bounds(centerX - 102, centerY + 45, 100, 20)
                .build());

        this.addRenderableWidget(Button.builder(Component.translatable("message.colisao-cobblemon.cancel"), button -> this.onClose())
                .bounds(centerX + 2, centerY + 45, 100, 20)
                .build());
    }

    private void save() {
        try {
            int x = Integer.parseInt(this.xBox.getValue());
            int y = Integer.parseInt(this.yBox.getValue());
            int z = Integer.parseInt(this.zBox.getValue());

            float yaw = 0f;
            try {
                yaw = Float.parseFloat(this.yawBox.getValue());
            } catch (Exception e) {
                yaw = 0f;
            }

            ClientPlayNetworking.send(new TeleportNetwork.SetTeleportPayload(this.blockPos, x, y, z, yaw));
            this.onClose();
        } catch (NumberFormatException ignored) {
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);

        guiGraphics.drawString(this.font, "X:", this.width / 2 - 115, this.height / 2 - 45, 0xFFFFFF);
        guiGraphics.drawString(this.font, "Y:", this.width / 2 - 115, this.height / 2 - 20, 0xFFFFFF);
        guiGraphics.drawString(this.font, "Z:", this.width / 2 - 115, this.height / 2 + 5, 0xFFFFFF);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}
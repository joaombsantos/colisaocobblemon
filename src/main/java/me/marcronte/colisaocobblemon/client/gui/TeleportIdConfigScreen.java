package me.marcronte.colisaocobblemon.client.gui;

import me.marcronte.colisaocobblemon.features.teleportblock.TeleportType;
import me.marcronte.colisaocobblemon.network.TeleportNetwork;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class TeleportIdConfigScreen extends Screen {
    private final BlockPos blockPos;
    private final TeleportType type;
    private EditBox idBox;

    public TeleportIdConfigScreen(BlockPos blockPos, TeleportType type) {
        super(Component.translatable("message.colisao-cobblemon.teleport_configs"));
        this.blockPos = blockPos;
        this.type = type;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.idBox = new EditBox(this.font, centerX - 100, centerY - 20, 200, 20, Component.literal("ID"));
        this.idBox.setHint(Component.literal("Digite o ID (Ex: ginasio_fogo)"));
        this.addRenderableWidget(this.idBox);

        this.addRenderableWidget(Button.builder(Component.translatable("message.colisao-cobblemon.save"), button -> save())
                .bounds(centerX - 102, centerY + 20, 100, 20)
                .build());

        this.addRenderableWidget(Button.builder(Component.translatable("message.colisao-cobblemon.cancel"), button -> this.onClose())
                .bounds(centerX + 2, centerY + 20, 100, 20)
                .build());
    }

    private void save() {
        String linkId = this.idBox.getValue().trim();
        if (!linkId.isEmpty()) {
            ClientPlayNetworking.send(new TeleportNetwork.SetLinkIdPayload(this.blockPos, linkId));
            this.onClose();
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        String titleType = type == TeleportType.HUB ? "Configurar HUB Central" : "Configurar Conexão (Spoke)";
        guiGraphics.drawCenteredString(this.font, titleType, this.width / 2, this.height / 2 - 50, 0xFFFFFF);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}
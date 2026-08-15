package me.marcronte.colisaocobblemon.client.gui;

import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class PokeLurerScreen extends AbstractContainerScreen<PokeLurerMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "textures/gui/poke_lurer_gui.png");

    public PokeLurerScreen(PokeLurerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = 10000;
        this.inventoryLabelX = 10000;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        super.render(graphics, mouseX, mouseY, delta);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, 176, 166);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        int ticks = this.menu.getFuelTicks();
        String timeString = "00:00";

        if (ticks > 0) {
            int totalSeconds = ticks / 20;
            int minutes = totalSeconds / 60;
            int seconds = totalSeconds % 60;
            timeString = String.format("%02d:%02d", minutes, seconds);
        }

        graphics.drawCenteredString(this.font, "Tempo", 42, 20, 0xFFFFFF);
        graphics.drawCenteredString(this.font, "Restante", 42, 20 + this.font.lineHeight, 0xFFFFFF);

        graphics.drawCenteredString(this.font, timeString, 42, 44, 0x55FF55);

        String currentType = this.menu.getCurrentType();

        graphics.drawCenteredString(this.font, "Atraindo:", 138, 28, 0xFFFFFF);

        int typeColor = 0xFFD700;
        if (currentType.equals("Fogo")) typeColor = 0xFF5555;
        if (currentType.equals("Água")) typeColor = 0x5555FF;
        if (currentType.equals("Planta")) typeColor = 0x55FF55;
        if (currentType.equals("Elétrico")) typeColor = 0xFFFF55;
        if (currentType.equals("Gelo")) typeColor = 0x55FFFF;
        if (currentType.equals("Lutador")) typeColor = 0xFF7700;
        if (currentType.equals("Venenoso")) typeColor = 0xAA55CC;
        if (currentType.equals("Terra")) typeColor = 0xDD9955;
        if (currentType.equals("Voador")) typeColor = 0x8899FF;
        if (currentType.equals("Psíquico")) typeColor = 0xFF55AA;
        if (currentType.equals("Inseto")) typeColor = 0xAACC22;
        if (currentType.equals("Pedra")) typeColor = 0xCCAA66;
        if (currentType.equals("Fantasma")) typeColor = 0x6666AA;
        if (currentType.equals("Dragão")) typeColor = 0x7755EE;
        if (currentType.equals("Sombrio")) typeColor = 0x555555;
        if (currentType.equals("Metálico")) typeColor = 0xAAAAAA;
        if (currentType.equals("Fada")) typeColor = 0xFFAAFF;
        if (currentType.equals("Normal")) typeColor = 0xDDDDDD;

        graphics.drawCenteredString(this.font, currentType, 138, 42, typeColor);
    }
}
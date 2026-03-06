package me.marcronte.colisaocobblemon.client.gui;

import me.marcronte.colisaocobblemon.network.payloads.PlantationPayloads;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class PlantationScreen extends Screen {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("colisao-cobblemon:textures/gui/plantation_gui.png");
    private static final long GROW_TIME_MS = 30 * 60 * 1000L;

    private final PlantationPayloads.SyncPayload payload;

    private int selectingForSlot = -1;
    private int berryPage = 0;
    private static final int ITEMS_PER_PAGE = 10;

    public PlantationScreen(PlantationPayloads.SyncPayload payload) {
        super(Component.translatable("gui.colisao-cobblemon.plantation"));
        this.payload = payload;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        int imgWidth = 256;
        int imgHeight = 128;
        int startX = (this.width - imgWidth) / 2;
        int startY = (this.height - imgHeight) / 2;

        graphics.blit(TEXTURE, startX, startY, 0, 0, imgWidth, imgHeight, 256, 128);

        graphics.drawCenteredString(this.font, "§aPlantação de Berries", startX + imgWidth / 2, startY + 15, 0xFFFFFF);

        long currentTime = System.currentTimeMillis();

        for (int i = 0; i < 5; i++) {
            int slotX = startX + 17 + (i * 51);
            int slotY = startY + 47;

            if (i >= payload.unlockedSlots()) {
                graphics.fill(slotX, slotY, slotX + 18, slotY + 18, 0xAA000000); // Fundo escurecido
                graphics.drawCenteredString(this.font, "§c🔒", slotX + 9, slotY + 5, 0xFFFFFF);
                continue;
            }

            PlantationPayloads.SyncPayload.SlotData slotData = getSlotData(i);
            String berryId = slotData != null ? slotData.berryId() : "empty";

            if ("empty".equals(berryId)) {
                if (isHovering(slotX, slotY, mouseX, mouseY)) {
                    graphics.fill(slotX, slotY, slotX + 18, slotY + 18, 0x4400FF00);
                }
            } else {
                Item berryItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(berryId));
                graphics.renderFakeItem(new ItemStack(berryItem), slotX + 1, slotY + 1);

                long plantTime = slotData.plantTime();
                long timePassed = currentTime - plantTime;
                long timeLeft = GROW_TIME_MS - timePassed;

                if (timeLeft > 0) {
                    int minutes = (int) (timeLeft / 60000);
                    int seconds = (int) ((timeLeft % 60000) / 1000);
                    String timeStr = String.format("%02d:%02d", minutes, seconds);

                    graphics.drawCenteredString(this.font, "§e" + timeStr, slotX + 9, slotY - 12, 0xFFFFFF);
                } else {
                    graphics.fill(slotX, slotY, slotX + 18, slotY + 18, 0x5500FF00);
                    graphics.drawCenteredString(this.font, "§aPronto!", slotX + 9, slotY - 12, 0xFFFFFF);
                }
            }
        }

        if (selectingForSlot != -1) {
            renderBerrySelector(graphics, startX, startY, mouseX, mouseY);
        }
    }

    private void renderBerrySelector(GuiGraphics graphics, int startX, int startY, int mouseX, int mouseY) {
        List<String> berries = payload.availableBerries();
        int maxPages = (int) Math.ceil(berries.size() / (double) ITEMS_PER_PAGE);

        int boxWidth = 240;
        int boxHeight = 40;
        int boxX = startX + (256 - boxWidth) / 2;
        int boxY = startY + 80;

        graphics.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0xDD000000);
        graphics.drawCenteredString(this.font, "§fEscolha o que plantar (" + (berryPage + 1) + "/" + maxPages + "):", boxX + boxWidth / 2, boxY + 5, 0xFFFFFF);

        if (berryPage > 0) {
            int arrowLX = boxX + 5;
            boolean hoverL = mouseX >= arrowLX && mouseX <= arrowLX + 15 && mouseY >= boxY + 18 && mouseY <= boxY + 34;
            graphics.drawString(this.font, "<", arrowLX + 4, boxY + 22, hoverL ? 0x00FF00 : 0xFFFFFF, false);
        }

        if (berryPage < maxPages - 1) {
            int arrowRX = boxX + boxWidth - 15;
            boolean hoverR = mouseX >= arrowRX && mouseX <= arrowRX + 15 && mouseY >= boxY + 18 && mouseY <= boxY + 34;
            graphics.drawString(this.font, ">", arrowRX + 4, boxY + 22, hoverR ? 0x00FF00 : 0xFFFFFF, false);
        }

        int startIndex = berryPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, berries.size());

        for (int i = startIndex; i < endIndex; i++) {
            int bx = boxX + 22 + ((i - startIndex) * 20);
            int by = boxY + 18;

            Item berryItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(berries.get(i)));
            graphics.renderFakeItem(new ItemStack(berryItem), bx, by);

            if (mouseX >= bx && mouseX <= bx + 16 && mouseY >= by && mouseY <= by + 16) {
                graphics.fill(bx, by, bx + 16, by + 16, 0x55FFFFFF);
                graphics.renderTooltip(this.font, berryItem.getDescription(), mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int startX = (this.width - 256) / 2;
        int startY = (this.height - 128) / 2;

        if (selectingForSlot != -1) {
            List<String> berries = payload.availableBerries();
            int maxPages = (int) Math.ceil(berries.size() / (double) ITEMS_PER_PAGE);
            int boxWidth = 240;
            int boxX = startX + (256 - boxWidth) / 2;
            int boxY = startY + 80;

            if (berryPage > 0 && mouseX >= boxX + 5 && mouseX <= boxX + 20 && mouseY >= boxY + 18 && mouseY <= boxY + 34) {
                berryPage--;
                return true;
            }

            if (berryPage < maxPages - 1 && mouseX >= boxX + boxWidth - 15 && mouseX <= boxX + boxWidth && mouseY >= boxY + 18 && mouseY <= boxY + 34) {
                berryPage++;
                return true;
            }

            int startIndex = berryPage * ITEMS_PER_PAGE;
            int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, berries.size());

            for (int i = startIndex; i < endIndex; i++) {
                int bx = boxX + 22 + ((i - startIndex) * 20);
                int by = boxY + 18;

                if (mouseX >= bx && mouseX <= bx + 16 && mouseY >= by && mouseY <= by + 16) {
                    ClientPlayNetworking.send(new PlantationPayloads.ActionPayload(selectingForSlot, "plant", berries.get(i)));
                    selectingForSlot = -1;
                    return true;
                }
            }

            selectingForSlot = -1;
            return true;
        }

        long currentTime = System.currentTimeMillis();

        for (int i = 0; i < 5; i++) {
            if (i >= payload.unlockedSlots()) continue;

            int slotX = startX + 18 + (i * 51);
            int slotY = startY + 48;

            if (isHovering(slotX, slotY, (int) mouseX, (int) mouseY)) {
                PlantationPayloads.SyncPayload.SlotData slotData = getSlotData(i);
                String berryId = slotData != null ? slotData.berryId() : "empty";

                if ("empty".equals(berryId)) {
                    selectingForSlot = i;
                    berryPage = 0;
                } else {
                    long timeLeft = GROW_TIME_MS - (currentTime - slotData.plantTime());
                    if (timeLeft <= 0) {
                        ClientPlayNetworking.send(new PlantationPayloads.ActionPayload(i, "harvest", ""));
                    }
                }
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private PlantationPayloads.SyncPayload.SlotData getSlotData(int index) {
        for (PlantationPayloads.SyncPayload.SlotData s : payload.slots()) {
            if (s.index() == index) return s;
        }
        return null;
    }

    private boolean isHovering(int slotX, int slotY, int mouseX, int mouseY) {
        return mouseX >= slotX && mouseX <= slotX + 18 && mouseY >= slotY && mouseY <= slotY + 18;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
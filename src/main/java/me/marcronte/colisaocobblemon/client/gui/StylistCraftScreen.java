package me.marcronte.colisaocobblemon.client.gui;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.marcronte.colisaocobblemon.network.payloads.StylistPayloads;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

public class StylistCraftScreen extends Screen {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("colisao-cobblemon:textures/gui/stylist_craft.png");

    private final String category;
    private final List<Map<String, Object>> recipes;
    private final List<String> partySpecies;

    private int selectedRecipe = -1;
    private int selectedPartySlot = -1;
    private int recipeScrollOffset = 0;
    private int ingredientScrollOffset = 0;

    public StylistCraftScreen(StylistPayloads.OpenCraftPayload payload) {
        super(Component.literal("Crafting do Estilista"));
        this.category = payload.category();

        Gson gson = new Gson();
        this.recipes = gson.fromJson(payload.recipesJson(), new TypeToken<List<Map<String, Object>>>(){}.getType());
        this.partySpecies = gson.fromJson(payload.partyJson(), new TypeToken<List<String>>(){}.getType());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        int imgWidth = 300;
        int imgHeight = 166;
        int startX = (this.width - imgWidth) / 2;
        int startY = (this.height - imgHeight) / 2;

        graphics.blit(TEXTURE, startX, startY, 0, 0, imgWidth, imgHeight, 300, 166);

        int maxRecipeScroll = Math.max(0, recipes.size() - 7);
        drawScrollbar(graphics, startX + 111, startY + 16, 5, 140, recipeScrollOffset, maxRecipeScroll);

        for (int i = 0; i < 7; i++) {
            int index = i + recipeScrollOffset;
            if (index >= recipes.size()) break;

            Map<String, Object> recipe = recipes.get(index);
            String species = (String) recipe.get("species");

            int btnX = startX + 5;
            int btnY = startY + 16 + (i * 20);
            boolean isHovered = mouseX >= btnX && mouseX <= btnX + 104 && mouseY >= btnY && mouseY <= btnY + 20;

            if (selectedRecipe == index) graphics.fill(btnX, btnY, btnX + 104, btnY + 20, 0x55FF88FF);
            else if (isHovered) graphics.fill(btnX, btnY, btnX + 104, btnY + 20, 0x33FFFFFF);

            String formattedName = species.substring(0, 1).toUpperCase() + species.substring(1);
            drawScrollingText(graphics, "Roupa: " + formattedName, btnX + 5, btnY + 6, 95, isHovered);
        }

        if (selectedRecipe != -1) {
            Map<String, Object> recipe = recipes.get(selectedRecipe);

            List<String> ingredientsList = new java.util.ArrayList<>();
            Object ingObj = recipe.get("ingredients");
            if (ingObj instanceof List<?> tempList) {
                for (Object item : tempList) {
                    ingredientsList.add(String.valueOf(item));
                }
            }

            String targetSpecies = (String) recipe.get("species");

            int maxIngScroll = Math.max(0, ingredientsList.size() - 3);
            drawScrollbar(graphics, startX + 267, startY + 18, 7, 54, ingredientScrollOffset, maxIngScroll);

            for (int i = 0; i < 3; i++) {
                int index = i + ingredientScrollOffset;
                if (index >= ingredientsList.size()) break;

                String[] parts = ingredientsList.get(index).split("_", 2);
                int amount = Integer.parseInt(parts[0].trim());
                Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(parts[1].trim()));

                int ingX = startX + 186;
                int ingY = startY + 19 + (i * 19);
                boolean isHovered = mouseX >= ingX && mouseX <= ingX + 81 && mouseY >= ingY && mouseY <= ingY + 18;

                graphics.renderFakeItem(new ItemStack(item), ingX, ingY);

                String itemName = item.getDescription().getString();
                drawScrollingText(graphics, amount + "x " + itemName, ingX + 20, ingY + 5, 60, isHovered);
            }

            int partyStartX = startX + 145;
            int partyStartY = startY + 90;

            for (int i = 0; i < 6; i++) {
                int col = i % 3;
                int row = i / 3;
                int slotX = partyStartX + (col * 45);
                int slotY = partyStartY + (row * 30);

                String pSpecies = partySpecies.get(i);
                boolean isHovered = mouseX >= slotX && mouseX <= slotX + 40 && mouseY >= slotY && mouseY <= slotY + 25;

                if (pSpecies.equals("empty")) {
                    graphics.fill(slotX, slotY, slotX + 40, slotY + 25, 0x44000000);
                    continue;
                }

                boolean match = pSpecies.equalsIgnoreCase(targetSpecies);

                int color = match ? (isHovered ? 0x5500FF00 : 0x4400AA00) : 0x55FF0000;
                if (selectedPartySlot == i) color = 0xAA00FF00;

                graphics.fill(slotX, slotY, slotX + 40, slotY + 25, color);

                String fName = pSpecies.substring(0, 1).toUpperCase() + pSpecies.substring(1);
                graphics.drawCenteredString(this.font, fName, slotX + 20, slotY + 8, 0xFFFFFF);
            }

            int applyBtnX = startX + 149;
            int applyBtnY = startY + 37;
            boolean canApply = selectedPartySlot != -1;
            boolean hoverApply = mouseX >= applyBtnX && mouseX <= applyBtnX + 24 && mouseY >= applyBtnY && mouseY <= applyBtnY + 24;

            graphics.fill(applyBtnX - 2, applyBtnY - 2, applyBtnX + 18, applyBtnY + 18, canApply ? (hoverApply ? 0xAAFF88FF : 0x55FF88FF) : 0x44000000);
            graphics.drawCenteredString(this.font, "OK", applyBtnX + 8, applyBtnY + 4, 0xFFFFFF);
        }
    }

    private void drawScrollingText(GuiGraphics graphics, String text, int x, int y, int maxWidth, boolean isHovered) {
        int textWidth = this.font.width(text);
        if (textWidth <= maxWidth) {
            graphics.drawString(this.font, text, x, y, 0xFFFFFF, false);
        } else if (isHovered) {
            int extraChars = text.length() - this.font.plainSubstrByWidth(text, maxWidth).length();
            int scrollIndex = (int) ((Util.getMillis() / 250L) % (extraChars + 4));
            if (scrollIndex > extraChars) scrollIndex = extraChars;
            String visible = this.font.plainSubstrByWidth(text.substring(scrollIndex), maxWidth);
            graphics.drawString(this.font, visible, x, y, 0xFFFF88FF, false);
        } else {
            String truncated = this.font.plainSubstrByWidth(text, maxWidth - 12) + "...";
            graphics.drawString(this.font, truncated, x, y, 0xFFFFFF, false);
        }
    }

    private void drawScrollbar(GuiGraphics graphics, int x, int y, int width, int height, int currentOffset, int maxOffset) {
        graphics.fill(x, y, x + width, y + height, 0x55000000);
        if (maxOffset > 0) {
            int handleHeight = Math.max(10, height / (maxOffset + 1));
            int handleY = y + (int) (((float) currentOffset / maxOffset) * (height - handleHeight));
            graphics.fill(x, handleY, x + width, handleY + handleHeight, 0xFF888888);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int startX = (this.width - 300) / 2;
        int startY = (this.height - 166) / 2;

        if (mouseX >= startX + 5 && mouseX <= startX + 116 && mouseY >= startY + 16 && mouseY <= startY + 156) {
            int maxRecipeScroll = Math.max(0, recipes.size() - 7);
            if (scrollY > 0) recipeScrollOffset = Math.max(0, recipeScrollOffset - 1);
            if (scrollY < 0) recipeScrollOffset = Math.min(maxRecipeScroll, recipeScrollOffset + 1);
            return true;
        }

        if (selectedRecipe != -1) {
            Map<String, Object> recipe = recipes.get(selectedRecipe);
            Object matObj = recipe.get("ingredients");
            int matSize = 0;
            if (matObj instanceof String) matSize = ((String) matObj).split(",").length;
            else if (matObj instanceof List) matSize = ((List<?>) matObj).size();

            if (mouseX >= startX + 185 && mouseX <= startX + 274 && mouseY >= startY + 18 && mouseY <= startY + 72) {
                int maxIngScroll = Math.max(0, matSize - 3);
                if (scrollY > 0) ingredientScrollOffset = Math.max(0, ingredientScrollOffset - 1);
                if (scrollY < 0) ingredientScrollOffset = Math.min(maxIngScroll, ingredientScrollOffset + 1);
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int startX = (this.width - 300) / 2;
        int startY = (this.height - 166) / 2;

        for (int i = 0; i < 7; i++) {
            int index = i + recipeScrollOffset;
            if (index >= recipes.size()) break;

            int btnX = startX + 5;
            int btnY = startY + 16 + (i * 20);
            if (mouseX >= btnX && mouseX <= btnX + 104 && mouseY >= btnY && mouseY <= btnY + 20) {
                selectedRecipe = index;
                selectedPartySlot = -1;
                return true;
            }
        }

        if (selectedRecipe != -1) {
            Map<String, Object> recipe = recipes.get(selectedRecipe);
            String targetSpecies = (String) recipe.get("species");

            int partyStartX = startX + 145;
            int partyStartY = startY + 90;

            for (int i = 0; i < 6; i++) {
                int col = i % 3;
                int row = i / 3;
                int slotX = partyStartX + (col * 45);
                int slotY = partyStartY + (row * 30);

                if (mouseX >= slotX && mouseX <= slotX + 40 && mouseY >= slotY && mouseY <= slotY + 25) {
                    if (partySpecies.get(i).equalsIgnoreCase(targetSpecies)) {
                        selectedPartySlot = i;
                    }
                    return true;
                }
            }

            int applyBtnX = startX + 149;
            int applyBtnY = startY + 37;
            if (selectedPartySlot != -1 && mouseX >= applyBtnX && mouseX <= applyBtnX + 16 && mouseY >= applyBtnY && mouseY <= applyBtnY + 16) {
                ClientPlayNetworking.send(new StylistPayloads.PerformApplyPayload(category, selectedRecipe, selectedPartySlot));
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
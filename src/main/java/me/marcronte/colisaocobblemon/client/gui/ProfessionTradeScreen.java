package me.marcronte.colisaocobblemon.client.gui;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.marcronte.colisaocobblemon.config.ProfessionsCraftsConfig;
import me.marcronte.colisaocobblemon.network.payloads.ProfessionCraftPayloads;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class ProfessionTradeScreen extends Screen {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("colisao-cobblemon:textures/gui/profession_craft.png");
    private static final ResourceLocation XP_BAR_BG = ResourceLocation.parse("hud/experience_bar_background");
    private static final ResourceLocation XP_BAR_PROGRESS = ResourceLocation.parse("hud/experience_bar_progress");

    private final String profession;
    private final String rank;
    private int currentExp;
    private final List<ProfessionsCraftsConfig.CraftData> recipes;

    private int selectedRecipe = -1;

    private int recipeScrollOffset = 0;
    private int ingredientScrollOffset = 0;

    public ProfessionTradeScreen(String profession, String rank, int currentExp, String recipesJson) {
        super(Component.literal("Fabricação"));
        this.profession = profession;
        this.rank = rank;
        this.currentExp = currentExp;

        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, ProfessionsCraftsConfig.RankData>>(){}.getType();
        Map<String, ProfessionsCraftsConfig.RankData> allRanks = gson.fromJson(recipesJson, type);

        if (allRanks != null && allRanks.containsKey(rank)) {
            this.recipes = allRanks.get(rank).craft_list;
        } else {
            this.recipes = java.util.List.of();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        int imgWidth = 300;
        int imgHeight = 166;
        int startX = (this.width - imgWidth) / 2;
        int startY = (this.height - imgHeight) / 2;

        graphics.blit(TEXTURE, startX, startY, 0, 0, imgWidth, imgHeight, 300, 166);

        int barX = startX + 155;
        int barY = startY + 7;
        graphics.fill(barX, barY, barX + 100, barY + 5, 0xFF555555);
        int fillWidth = (int) ((Math.min(100, currentExp) / 100.0f) * 100);
        graphics.fill(barX, barY, barX + fillWidth, barY + 5, 0xFF00AA00);
        graphics.blitSprite(XP_BAR_BG, barX, barY - 1, 102, 7);
        graphics.blitSprite(XP_BAR_PROGRESS, 100, 7, 0, 0, barX, barY - 1, fillWidth, 7);
        if (mouseX >= barX && mouseX <= barX + 100 && mouseY >= barY && mouseY <= barY + 5) {
            graphics.renderTooltip(this.font, Component.literal("§e" + Math.min(100, currentExp) + " / 100 XP"), mouseX, mouseY);
        }

        int maxRecipeScroll = Math.max(0, recipes.size() - 7);
        drawScrollbar(graphics, startX + 111, startY + 16, 5, 140, recipeScrollOffset, maxRecipeScroll);

        for (int i = 0; i < 7; i++) {
            int index = i + recipeScrollOffset;
            if (index >= recipes.size()) break;

            ProfessionsCraftsConfig.CraftData recipe = recipes.get(index);
            int btnX = startX + 5;
            int btnY = startY + 16 + (i * 20);
            boolean isHovered = mouseX >= btnX && mouseX <= btnX + 104 && mouseY >= btnY && mouseY <= btnY + 20;

            if (selectedRecipe == index) graphics.fill(btnX, btnY, btnX + 104, btnY + 20, 0x5500FF00);
            else if (isHovered) graphics.fill(btnX, btnY, btnX + 104, btnY + 20, 0x33FFFFFF);

            Item resultItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(recipe.result_item));
            graphics.renderFakeItem(new ItemStack(resultItem), btnX + 2, btnY + 2);

            String text = currentExp < recipe.exp_needed ? "§c" + recipe.exp_needed + " XP" : resultItem.getDescription().getString();
            drawScrollingText(graphics, text, btnX + 22, btnY + 6, 80, isHovered);
        }

        if (selectedRecipe != -1 && selectedRecipe < recipes.size()) {
            ProfessionsCraftsConfig.CraftData recipe = recipes.get(selectedRecipe);

            int craftBtnX = startX + 149;
            int craftBtnY = startY + 37;
            Item resultItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(recipe.result_item));

            boolean canCraft = currentExp >= recipe.exp_needed;
            boolean hoverCraft = mouseX >= craftBtnX && mouseX <= craftBtnX + 24 && mouseY >= craftBtnY && mouseY <= craftBtnY + 24;

            if (canCraft && hoverCraft) {
                graphics.fill(craftBtnX - 2, craftBtnY - 2, craftBtnX + 18, craftBtnY + 18, 0x5500FF00);
                graphics.renderTooltip(this.font, Component.translatable("gui.colisao-cobblemon.click_to_fabric"), mouseX, mouseY);
            } else if (!canCraft && hoverCraft) {
                graphics.fill(craftBtnX - 2, craftBtnY - 2, craftBtnX + 18, craftBtnY + 18, 0x55FF0000);
            }

            graphics.renderFakeItem(new ItemStack(resultItem), craftBtnX, craftBtnY);

            int xpTextY = craftBtnY + 22;
            if (currentExp < recipe.limit_exp) {
                graphics.drawCenteredString(this.font, "§b+" + recipe.exp_reward + " XP", craftBtnX + 8, xpTextY, 0xFFFFFF);
            } else {
                graphics.drawCenteredString(this.font, "§7Max XP", craftBtnX + 8, xpTextY, 0xFFFFFF);
            }

            int maxIngScroll = Math.max(0, recipe.ingredients.size() - 3);
            drawScrollbar(graphics, startX + 267, startY + 18, 7, 54, ingredientScrollOffset, maxIngScroll);

            for (int i = 0; i < 3; i++) {
                int index = i + ingredientScrollOffset;
                if (index >= recipe.ingredients.size()) break;

                String[] parts = recipe.ingredients.get(index).split("_", 2);
                int amount = Integer.parseInt(parts[0]);
                Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(parts[1]));

                int ingX = startX + 185;
                int ingY = startY + 18 + (i * 18);
                boolean isHovered = mouseX >= ingX && mouseX <= ingX + 81 && mouseY >= ingY && mouseY <= ingY + 18;

                graphics.renderFakeItem(new ItemStack(item), ingX, ingY);
                drawScrollingText(graphics, amount + "x " + item.getDescription().getString(), ingX + 20, ingY + 5, 60, isHovered);
            }
        }

        Player player = Minecraft.getInstance().player;
        if (player != null) {
            int invStartX = startX + 132;
            int invStartY = startY + 84;

            for (int i = 0; i < 36; i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (!stack.isEmpty()) {
                    int visualRow = (i < 9) ? 3 : (i - 9) / 9;
                    int visualCol = (i < 9) ? i : (i - 9) % 9;

                    int slotX = invStartX + (visualCol * 18);
                    int slotY = invStartY + (visualRow * 18);

                    if (visualRow == 3) slotY += 4;

                    graphics.renderItem(stack, slotX, slotY);
                    graphics.renderItemDecorations(this.font, stack, slotX, slotY);

                    if (mouseX >= slotX && mouseX <= slotX + 18 && mouseY >= slotY && mouseY <= slotY + 18) {
                        graphics.renderTooltip(this.font, stack, mouseX, mouseY);
                    }
                }
            }
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
            String visible = text.substring(scrollIndex);
            visible = this.font.plainSubstrByWidth(visible, maxWidth);

            graphics.drawString(this.font, visible, x, y, 0xFFFF55, false);
        } else {
            String truncated = this.font.plainSubstrByWidth(text, maxWidth - this.font.width("...")) + "...";
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
            if (mouseX >= startX + 185 && mouseX <= startX + 274 && mouseY >= startY + 18 && mouseY <= startY + 72) {
                int maxIngScroll = Math.max(0, recipes.get(selectedRecipe).ingredients.size() - 3);
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
                ingredientScrollOffset = 0;
                return true;
            }
        }

        if (selectedRecipe != -1) {
            int craftBtnX = startX + 149;
            int craftBtnY = startY + 37;

            if (mouseX >= craftBtnX && mouseX <= craftBtnX + 16 && mouseY >= craftBtnY && mouseY <= craftBtnY + 16) {
                ProfessionsCraftsConfig.CraftData recipe = recipes.get(selectedRecipe);

                if (currentExp >= recipe.exp_needed) {
                    ClientPlayNetworking.send(new ProfessionCraftPayloads.PerformCraftPayload(this.rank, selectedRecipe));
                }
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void updateExp(int newExp) {
        this.currentExp = newExp;
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
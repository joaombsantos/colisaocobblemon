package me.marcronte.colisaocobblemon.network.payloads;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class StylistPayloads {

    public record OpenMenuPayload(String categoriesJson) implements CustomPacketPayload {
        public static final Type<OpenMenuPayload> ID = new Type<>(ResourceLocation.parse("colisao-cobblemon:stylist_menu"));
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenMenuPayload> CODEC = StreamCodec.of(
                (buf, p) -> buf.writeUtf(p.categoriesJson()), buf -> new OpenMenuPayload(buf.readUtf())
        );
        @Override public Type<? extends CustomPacketPayload> type() { return ID; }
    }

    public record SelectCategoryPayload(String categoryName) implements CustomPacketPayload {
        public static final Type<SelectCategoryPayload> ID = new Type<>(ResourceLocation.parse("colisao-cobblemon:stylist_select_cat"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SelectCategoryPayload> CODEC = StreamCodec.of(
                (buf, p) -> buf.writeUtf(p.categoryName()), buf -> new SelectCategoryPayload(buf.readUtf())
        );
        @Override public Type<? extends CustomPacketPayload> type() { return ID; }
    }

    public record OpenCraftPayload(String category, String recipesJson, String partyJson) implements CustomPacketPayload {
        public static final Type<OpenCraftPayload> ID = new Type<>(ResourceLocation.parse("colisao-cobblemon:stylist_craft"));
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenCraftPayload> CODEC = StreamCodec.of(
                (buf, p) -> { buf.writeUtf(p.category()); buf.writeUtf(p.recipesJson()); buf.writeUtf(p.partyJson()); },
                buf -> new OpenCraftPayload(buf.readUtf(), buf.readUtf(), buf.readUtf())
        );
        @Override public Type<? extends CustomPacketPayload> type() { return ID; }
    }

    public record PerformApplyPayload(String category, int recipeIndex, int partySlot) implements CustomPacketPayload {
        public static final Type<PerformApplyPayload> ID = new Type<>(ResourceLocation.parse("colisao-cobblemon:stylist_apply"));
        public static final StreamCodec<RegistryFriendlyByteBuf, PerformApplyPayload> CODEC = StreamCodec.of(
                (buf, p) -> { buf.writeUtf(p.category()); buf.writeInt(p.recipeIndex()); buf.writeInt(p.partySlot()); },
                buf -> new PerformApplyPayload(buf.readUtf(), buf.readInt(), buf.readInt())
        );
        @Override public Type<? extends CustomPacketPayload> type() { return ID; }
    }
}
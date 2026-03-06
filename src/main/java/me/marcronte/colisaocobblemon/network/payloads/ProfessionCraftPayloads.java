package me.marcronte.colisaocobblemon.network.payloads;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ProfessionCraftPayloads {

    // S2C
    public record OpenMenuPayload(String profession, String currentRank, int currentExp, String recipesJson) implements CustomPacketPayload {
        public static final Type<OpenMenuPayload> ID = new Type<>(ResourceLocation.parse("colisao-cobblemon:prof_open_menu"));

        public static final StreamCodec<RegistryFriendlyByteBuf, OpenMenuPayload> CODEC = StreamCodec.of(
                (buf, payload) -> payload.write(buf), OpenMenuPayload::new
        );

        private OpenMenuPayload(RegistryFriendlyByteBuf buf) {
            this(buf.readUtf(), buf.readUtf(), buf.readInt(), buf.readUtf());
        }

        private void write(RegistryFriendlyByteBuf buf) {
            buf.writeUtf(profession);
            buf.writeUtf(currentRank);
            buf.writeInt(currentExp);
            buf.writeUtf(recipesJson);
        }

        @Override public @NotNull Type<? extends CustomPacketPayload> type() { return ID; }
    }

    // C2S
    public record PerformCraftPayload(String rank, int recipeIndex) implements CustomPacketPayload {
        public static final Type<PerformCraftPayload> ID = new Type<>(ResourceLocation.parse("colisao-cobblemon:prof_perform_craft"));

        public static final StreamCodec<RegistryFriendlyByteBuf, PerformCraftPayload> CODEC = StreamCodec.of(
                (buf, payload) -> payload.write(buf), PerformCraftPayload::new
        );

        private PerformCraftPayload(RegistryFriendlyByteBuf buf) {
            this(buf.readUtf(), buf.readInt());
        }

        private void write(RegistryFriendlyByteBuf buf) {
            buf.writeUtf(rank);
            buf.writeInt(recipeIndex);
        }

        @Override public @NotNull Type<? extends CustomPacketPayload> type() { return ID; }
    }

    public record SyncExpPayload(int newExp) implements CustomPacketPayload {
        public static final Type<SyncExpPayload> ID = new Type<>(ResourceLocation.parse("colisao-cobblemon:prof_sync_exp"));

        public static final StreamCodec<RegistryFriendlyByteBuf, SyncExpPayload> CODEC = StreamCodec.of(
                (buf, payload) -> payload.write(buf), SyncExpPayload::new
        );

        private SyncExpPayload(RegistryFriendlyByteBuf buf) {
            this(buf.readInt());
        }

        private void write(RegistryFriendlyByteBuf buf) {
            buf.writeInt(newExp);
        }

        @Override public @NotNull Type<? extends CustomPacketPayload> type() { return ID; }
    }
}
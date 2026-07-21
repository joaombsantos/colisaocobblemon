package me.marcronte.colisaocobblemon;

import me.marcronte.colisaocobblemon.features.blocks.ChairBlock;
import me.marcronte.colisaocobblemon.features.blocks.DecorativeBlock;
import me.marcronte.colisaocobblemon.features.breeding.habitat.BreedingHabitatBlock;
import me.marcronte.colisaocobblemon.features.breeding.habitat.BreedingHabitatBlockEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ModBlocks {

    public static final Block BREEDING_HABITAT = registerBlock("breeding_habitat",
            new BreedingHabitatBlock(BlockBehaviour.Properties.of()
                    .strength(2.5F, 3.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.WOOD)
                    .noOcclusion()
            )
    );

    public static final BlockEntityType<BreedingHabitatBlockEntity> BREEDING_HABITAT_BE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "breeding_habitat_be"),
            BlockEntityType.Builder.of(
                    BreedingHabitatBlockEntity::new,
                    BREEDING_HABITAT
            ).build(null)
    );

    public static final Block PIKACHU_POKEDOLL = registerBlock("pikachu_pokedoll",
            new DecorativeBlock(BlockBehaviour.Properties.of()
                    .strength(0.5F)
                    .sound(SoundType.WOOL)
                    .noOcclusion(),

                    Block.box(3.0, 0.0, 3.0, 13.0, 12.0, 13.0)
            )
    );

    public static final Block SQUIRTLE_POKEDOLL = registerBlock("squirtle_pokedoll",
            new DecorativeBlock(BlockBehaviour.Properties.of()
                    .strength(0.5F)
                    .sound(SoundType.WOOL)
                    .noOcclusion(),

                    Block.box(3.0, 0.0, 3.0, 13.0, 12.0, 13.0)
            )
    );

    public static final Block BULBASAUR_POKEDOLL = registerBlock("bulbasaur_pokedoll",
            new DecorativeBlock(BlockBehaviour.Properties.of()
                    .strength(0.5F)
                    .sound(SoundType.WOOL)
                    .noOcclusion(),

                    Block.box(3.0, 0.0, 3.0, 13.0, 12.0, 13.0)
            )
    );

    public static final Block CHARMANDER_POKEDOLL = registerBlock("charmander_pokedoll",
            new DecorativeBlock(BlockBehaviour.Properties.of()
                    .strength(0.5F)
                    .sound(SoundType.WOOL)
                    .noOcclusion(),

                    Block.box(3.0, 0.0, 3.0, 13.0, 12.0, 13.0)
            )
    );

    public static final Block FREEZER = registerBlock("freezer",
            new DecorativeBlock(BlockBehaviour.Properties.of()
                    .strength(1.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion(),

                    Block.box(0.0, 0.0, 0.0, 16.0, 32.0, 13.0)
            )
    );

    public static final Block TABLE = registerBlock("table",
            new DecorativeBlock(BlockBehaviour.Properties.of()
                    .strength(1.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion(),

                    Block.box(0.0, 0.0, 0.0, 16.0, 13.0, 16.0)
            )
    );

    public static final Block CHAIR = registerBlock("chair",
            new ChairBlock(BlockBehaviour.Properties.of()
                    .strength(1.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion(),

                    Block.box(3.0, 0.0, 3.0, 13.0, 9.0, 13.0)
            )
    );

    public static final Block MAILBOX = registerBlock("mailbox",
            new DecorativeBlock(BlockBehaviour.Properties.of()
                    .strength(1.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion(),

                    Block.box(0, 0.0, 4.5, 12.0, 19.0, 11.5)
            )
    );

    private static Block registerBlock(String name, Block block) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, name);

        Registry.register(BuiltInRegistries.ITEM, id, new BlockItem(block, new Item.Properties()));

        return Registry.register(BuiltInRegistries.BLOCK, id, block);
    }

    public static void registerModBlocks() {
        ColisaoCobblemon.LOGGER.info("Registrando Blocos para " + ColisaoCobblemon.MOD_ID);
    }
}
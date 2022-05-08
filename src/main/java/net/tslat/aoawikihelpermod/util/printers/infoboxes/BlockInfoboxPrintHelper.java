package net.tslat.aoawikihelpermod.util.printers.infoboxes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.printers.PrintHelper;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

public class BlockInfoboxPrintHelper extends PrintHelper {
    private static final String HEAD = "{{BlockInfo";
    private static final String END = "}}";

    private static final String BLOCK_IMAGE_SIZE = "150px";

    private static final ResourceLocation NEEDS_STONE = new ResourceLocation("minecraft", "needs_stone_tool");
    private static final ResourceLocation NEEDS_IRON = new ResourceLocation("minecraft", "needs_iron_tool");
    private static final ResourceLocation NEEDS_DIAMOND = new ResourceLocation("minecraft", "needs_diamond_tool");

    private static final ResourceLocation MINEABLE_AXE = new ResourceLocation("minecraft", "mineable/axe");
    private static final ResourceLocation MINEABLE_HOE = new ResourceLocation("minecraft", "mineable/hoe");
    private static final ResourceLocation MINEABLE_PICKAXE = new ResourceLocation("minecraft", "mineable/pickaxe");
    private static final ResourceLocation MINEABLE_SHOVEL = new ResourceLocation("minecraft", "mineable/shovel");

    protected BlockInfoboxPrintHelper(String fileName) throws IOException {
        super(fileName);
    }

    @Nullable
    public static BlockInfoboxPrintHelper open(String fileName) {
        try {
            return new BlockInfoboxPrintHelper(fileName);
        }
        catch (IOException ex) {
            return null;
        }
    }

    @Nullable
    private List<ResourceLocation> getBlockTags(Block block) {
        if (block.builtInRegistryHolder().tags().toList().isEmpty())
            return null;

        return block.builtInRegistryHolder().tags().map(TagKey::location).toList();
    }

    private static String determineLightLevel(Block block) {
        int lightLevel = 0;
        int numberOfLightValues = 0;
        for (BlockState state: block.getStateDefinition().getPossibleStates()) {
            int stateLightLevel = block.getLightEmission(state, null, null);
            lightLevel = Math.max(lightLevel, stateLightLevel);

            if (stateLightLevel > 0) {
                numberOfLightValues++;
            }
        }

        if (numberOfLightValues > 1) {
            return "Up to " + lightLevel;
        } else if (numberOfLightValues == 1) {
            return lightLevel + "";
        } else {
            return "None";
        }
    }

    private static String determineHarvestLevel(Block block, List<ResourceLocation> tags) {
        if (block.defaultDestroyTime() < 0) {
            return "N/A";
        } else if (tags == null) {
            return "Any";
        }

        for (ResourceLocation tag: tags) {
            if(tag.equals(NEEDS_STONE)) {
                return "Stone";
            } else if (tag.equals(NEEDS_IRON)) {
                return "Iron";
            } else if (tag.equals(NEEDS_DIAMOND)) {
                return "Diamond";
            }
        }

        return "Any";
    }

    private static String determineToolType(Block block, List<ResourceLocation> tags) {
        if (block.defaultDestroyTime() < 0) {
            return "N/A";
        } else if (tags == null) {
            return "Any";
        }

        for (ResourceLocation tag: tags) {
            if(tag.equals(MINEABLE_PICKAXE)) {
                return "Pickaxe";
            } else if (tag.equals(MINEABLE_AXE)) {
                return "Axe";
            } else if (tag.equals(MINEABLE_HOE)) {
                return "Hoe";
            } else if (tag.equals(MINEABLE_SHOVEL)) {
                return "Shovel";
            }
        }

        return "Any";
    }

    private static String convertRarityColor(Rarity rarity) {
        switch (rarity) {
            case COMMON: return "Common";
            case UNCOMMON: return "Uncommon";
            case EPIC: return "Epic";
            case RARE: return "Rare";
            default: return "Couldn't get rarity color";
        }
    }

    public void printBlockInfobox(Block block) {
        String displayName = ObjectHelper.getBlockName(block);
        List<ResourceLocation> tags = getBlockTags(block);
        ItemStack itemStack = new ItemStack(block);
        int stackSize = itemStack.getItem().getMaxStackSize();
        BlockState defaultBlockState = block.defaultBlockState();

        write(HEAD);
        write("|name=" + displayName);
        write("|image=" + displayName + ".png");
        write("|imgsize=" + BLOCK_IMAGE_SIZE);
        write("|hardness=" + block.defaultDestroyTime());
        write("|blastresistance=" + block.getExplosionResistance());
        write("|transparent=" + (defaultBlockState.isViewBlocking(null, null) ? "No" : "Yes"));
        write("|luminance=" + determineLightLevel(block));
        write("|flammable=" + (defaultBlockState.getFlammability(null, null, null) > 0 ? "Yes" : "No"));
        write("|stackable=" + (stackSize == 1 ? "No" : "Yes (" + stackSize + ")"));
        write("|tool=" + determineToolType(block, tags));
        write("|harvestlevel=" + determineHarvestLevel(block, tags));
        write("|raritycolor=" + convertRarityColor(itemStack.getRarity()));
        write("|id=" + block.getRegistryName());
        write("|versionadded=");
        write(END);
    }
}

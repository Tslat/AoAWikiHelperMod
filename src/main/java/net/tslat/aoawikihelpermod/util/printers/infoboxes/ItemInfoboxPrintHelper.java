package net.tslat.aoawikihelpermod.util.printers.infoboxes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.printers.PrintHelper;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

public class ItemInfoboxPrintHelper extends PrintHelper {
	private static final String HEAD = "{{ItemInfo";
	private static final String END = "}}";

	private static final String ITEM_IMAGE_SIZE = "150px";

	protected ItemInfoboxPrintHelper(String fileName) throws IOException {
		super(fileName);
	}

	@Nullable
	public static ItemInfoboxPrintHelper open(String fileName) {
		try {
			return new ItemInfoboxPrintHelper(fileName);
		} catch (IOException ex) {
			return null;
		}
	}

	@Nullable
	private List<ResourceLocation> getItemTags(Item Item) {
		if (Item.builtInRegistryHolder().tags().toList().isEmpty())
			return null;

		return Item.builtInRegistryHolder().tags().map(TagKey::location).toList();
	}

	private static String convertRarityColor(Rarity rarity) {
		switch (rarity) {
			case COMMON:
				return "Common";
			case UNCOMMON:
				return "Uncommon";
			case EPIC:
				return "Epic";
			case RARE:
				return "Rare";
			default:
				return "Couldn't get rarity color";
		}
	}

	private static String getHunger(ItemStack itemStack, Entity entity) {
		if (itemStack.getFoodProperties((LivingEntity) entity) == null) {
			return "";
		}
		return "" + itemStack.getFoodProperties((LivingEntity) entity).getNutrition();
	}

	private static String getSaturation(ItemStack itemStack, Entity entity) {
		if (itemStack.getFoodProperties((LivingEntity) entity) == null) {
			return "";
		}
		return "" + itemStack.getFoodProperties((LivingEntity) entity).getSaturationModifier();
	}

	private static String getRange(ItemStack itemStack, Entity entity) {
		double range = 0;
		AABB box = itemStack.getSweepHitBox((Player) entity, entity);
		if (box.getXsize() > range) range = box.getXsize();
		if (box.getYsize() > range) range = box.getYsize();
		if (box.getZsize() > range) range = box.getZsize();
		if (range == 0) {
			return "";
		}
		return "" + range;
	}

	private <T extends Number> String noStringIfZero(T input) {
		if (input.equals(0)) return "";
		return "" + input;
	}

	public void printItemInfobox(Item Item, Entity player) {
		String displayName = ObjectHelper.getItemName(Item);
		List<ResourceLocation> tags = getItemTags(Item);
		ItemStack itemStack = new ItemStack(Item);
		int stackSize = itemStack.getItem().getMaxStackSize();

		write(HEAD);
		write("|name=" + displayName);
		write("|image=" + displayName + ".png");
		write("|imgsize=" + ITEM_IMAGE_SIZE);
		write("|imglist=");
		write("|itemimage=");
		write("|armorimage=");
		write("|armorimageold=");
		write("|damage=");//+ noStringIfZero(itemStack.getAttributeModifiers()));
		write("|specialdamage=");
		write("|attackspeed=");
		write("|knockback=");
		write("|armor=");
		write("|armortoughness=");
		write("|durability=" + noStringIfZero(itemStack.getMaxDamage()));
		write("|ammo=");
		write("|ammunition=");
		write("|drawspeed=");
		write("|firerate=");
		write("|hunger=" + getHunger(itemStack, player));
		write("|saturation=" + getSaturation(itemStack, player));
		write("|efficiency=");
		write("|harvestlevel=");
		write("|radius=" + getRange(itemStack, player));
		write("|penetration=");
		write("|effect=");
		write("|skillreq=");
		write("|tooltip=");
		write("|stackable=" + (stackSize == 1 ? "No" : "Yes (" + stackSize + ")"));
		write("|raritycolor=" + convertRarityColor(itemStack.getRarity()));
		write("|id=" + ForgeRegistries.ITEMS.getKey(Item));
		write("|versionadded=");
		write(END);
	}
}

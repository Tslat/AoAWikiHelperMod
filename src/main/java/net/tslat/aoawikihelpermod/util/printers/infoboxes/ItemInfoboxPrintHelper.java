package net.tslat.aoawikihelpermod.util.printers.infoboxes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.aoa3.content.item.weapon.bow.BaseBow;
import net.tslat.aoa3.content.item.weapon.gun.BaseGun;
import net.tslat.aoa3.util.NumberUtil;
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

	private static String getAttribute(ItemStack itemStack, Attribute attribute, double offset, String suffix) {
		double value = (double) ObjectHelper.getAttributeFromItem(itemStack.getItem(), attribute) + offset;
		if (value > 0) return NumberUtil.roundToNthDecimalPlace((float) value, 2) + suffix;
		return "";
	}

	private static String getHarvestLevel(ItemStack itemStack) {
		Item item = itemStack.getItem();
		if (item instanceof TieredItem) {
			Tier tier = ((TieredItem) item).getTier();
			int level = tier.getLevel();
			if (level > 0) return "" + level;
		}
		return "";
	}

	private static String getEfficiency(ItemStack itemStack) {
		Item item = itemStack.getItem();
		if (item instanceof TieredItem) {
			Tier tier = ((TieredItem) item).getTier();
			float speed = tier.getSpeed();
			if (speed > 0) return NumberUtil.roundToNthDecimalPlace(speed, 2);
		}
		return "";
	}

	private static String getDrawSpeed(ItemStack itemStack) {
		Item item = itemStack.getItem();
		if (item instanceof BaseBow) {
			BaseBow bow = (BaseBow) itemStack.getItem();
			double speed = bow.getDrawSpeedMultiplier();
			if (speed > 0) return NumberUtil.roundToNthDecimalPlace((float) (1 / speed), 2) + "s";
		}
		return "";
	}

	private static String getFireRate(ItemStack itemStack) {
		Item item = itemStack.getItem();
		if (item instanceof BaseGun) {
			BaseGun gun = (BaseGun) itemStack.getItem();
			double fireRate = gun.getFiringDelay();
			if (fireRate > 0) return NumberUtil.roundToNthDecimalPlace((float) (20 / fireRate), 2) + "/sec";
		}
		return "";
	}

	private static String getAmmoType(ItemStack itemStack) {
		Item item = itemStack.getItem();
		if (item instanceof BaseGun) {
			BaseGun gun = (BaseGun) itemStack.getItem();
			Item ammo = gun.getAmmoItem();
			return ammo.getName(new ItemStack(ammo)).getString();
		}
		return "";
	}

	private boolean isGun(ItemStack itemStack) {
		if (itemStack.getItem() instanceof BaseGun) {
			return true;
		}
		return false;
	}

	private <T extends Number> String noStringIfZero(T input) {
		if (input.equals(0)) return "";
		return "" + input;
	}

	private void writeIfExists(String name, String value) {
		if (value.length() == 0) return;
		write(name, value);
	}

	public void printItemInfobox(Item Item, Entity player) {
		String displayName = ObjectHelper.getItemName(Item);
		List<ResourceLocation> tags = getItemTags(Item);
		ItemStack itemStack = new ItemStack(Item);
		int stackSize = itemStack.getItem().getMaxStackSize();

		write(HEAD);
		writeIfExists("|name=", displayName);
		writeIfExists("|image=", displayName + ".png");
		//write("|imgsize=" + ITEM_IMAGE_SIZE);
		write("|imglist=");
		write("|itemimage=");
		write("|armorimage=");
		write("|armorimageold=");
		writeIfExists("|damage=", getAttribute(itemStack, Attributes.ATTACK_DAMAGE, 0, ""));
		write("|specialdamage="); // manual input
		writeIfExists("|" +
				(isGun(itemStack) ? "unholstertime" : "attackspeed") + "=", getAttribute(itemStack, Attributes.ATTACK_SPEED, 4, "/sec"))
		;
		writeIfExists("|knockback=", getAttribute(itemStack, Attributes.ATTACK_KNOCKBACK, 0, ""));
		writeIfExists("|armor=", getAttribute(itemStack, Attributes.ARMOR, 0, ""));
		writeIfExists("|armortoughness=", getAttribute(itemStack, Attributes.ARMOR_TOUGHNESS, 0, ""));
		writeIfExists("|durability=", noStringIfZero(itemStack.getMaxDamage()));
		writeIfExists("|ammo=", getAmmoType(itemStack));
		write("|ammunition="); // can be omitted
		writeIfExists("|drawspeed=", getDrawSpeed(itemStack));
		writeIfExists("|firerate=", getFireRate(itemStack));
		writeIfExists("|hunger=", getHunger(itemStack, player));
		writeIfExists("|saturation=", getSaturation(itemStack, player));
		writeIfExists("|efficiency=", getEfficiency(itemStack));
		writeIfExists("|harvestlevel=", getHarvestLevel(itemStack));
		//write("|radius=" );
		//write("|penetration=");
		//write("|range=" + getAttribute(itemStack, (Attribute) ForgeMod.ATTACK_RANGE.get(), 0, ""));
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

package net.tslat.aoawikihelpermod.util.printers.infoboxes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.aoa3.content.item.weapon.blaster.BaseBlaster;
import net.tslat.aoa3.content.item.weapon.bow.BaseBow;
import net.tslat.aoa3.content.item.weapon.gun.BaseGun;
import net.tslat.aoa3.content.item.weapon.staff.BaseStaff;
import net.tslat.aoa3.content.item.weapon.thrown.BaseThrownWeapon;
import net.tslat.aoa3.util.NumberUtil;
import net.tslat.aoa3.util.StringUtil;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.printers.PrintHelper;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class ItemInfoboxPrintHelper extends PrintHelper {
	private static final String HEAD = "{{ItemInfo";
	private static final String END = "}}";


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
		return StringUtil.toTitleCase(rarity.name());
	}

	private static String getHunger(ItemStack itemStack, LivingEntity player) {
		if (itemStack.getFoodProperties( player) == null) {
			return "";
		}
		return "" + itemStack.getFoodProperties( player).getNutrition();
	}

	private static String getSaturation(ItemStack itemStack, LivingEntity player) {
		if (itemStack.getFoodProperties(player) == null) {
			return "";
		}
		return "" + itemStack.getFoodProperties(player).getSaturationModifier();
	}

	private static String getAttribute(ItemStack itemStack, Attribute attribute, double offset, String suffix) {
		double value = (double) ObjectHelper.getAttributeFromItem(itemStack.getItem(), attribute);
		if (value > 0) {
			return NumberUtil.roundToNthDecimalPlace((float) (value + offset), 2) + suffix;
		}
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

	private static String getAttackSpeed(ItemStack itemStack) {
		if (!(itemStack.getItem() instanceof BaseGun)) {
			return getAttribute(itemStack, Attributes.ATTACK_SPEED, 4, "s");
		}
		return "";
	}

	private static String getUnholsterTime(ItemStack itemStack) {
		Item item = itemStack.getItem();
		if (item instanceof BaseGun || item instanceof BaseBlaster) {
			double speed = ObjectHelper.getAttributeFromItem(item, Attributes.ATTACK_SPEED) + 4;
			return NumberUtil.roundToNthDecimalPlace((float) (1.0 / speed), 2) + "s";
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
		if (item instanceof BaseBlaster) {
			BaseBlaster gun = (BaseBlaster) itemStack.getItem();
			double fireRate = gun.getFiringDelay();
			if (fireRate > 0) return NumberUtil.roundToNthDecimalPlace((float) (20 / fireRate), 2) + "/sec";
		}
		return "";
	}

	private static String getFireType(ItemStack itemStack) {
		Item item = itemStack.getItem();
		if (item instanceof BaseGun) {
			BaseGun gun = (BaseGun) itemStack.getItem();
			if (gun.isFullAutomatic()) return "Fully-Automatic";
			return "Semi-Automatic";
		}
		if (item instanceof BaseBlaster) {
			BaseBlaster gun = (BaseBlaster) itemStack.getItem();
			return "Fully-Automatic";
		}
		return "";
	}

	private static String getAmmoType(ItemStack itemStack) {
		Item item = itemStack.getItem();
		if (item instanceof BaseGun && !(item instanceof BaseThrownWeapon)) {
			BaseGun gun = (BaseGun) itemStack.getItem();
			Item ammo = gun.getAmmoItem();
			return ammo.getName(new ItemStack(ammo)).getString();
		}
		if (item instanceof ProjectileWeaponItem) {
			ProjectileWeaponItem bow = (ProjectileWeaponItem) itemStack.getItem();
			String s = "";
			for (Item i : ForgeRegistries.ITEMS.getValues()) {
				ItemStack stack = new ItemStack(i);
				if (bow.getAllSupportedProjectiles().test(stack)) {
					s += ObjectHelper.getItemName(i);
				}
			}
			return s;
		}
		if (item instanceof BaseStaff<?>) {
			BaseStaff staff = (BaseStaff) itemStack.getItem();
			String s = "";
			HashMap<Item, Integer> runes = staff.getRunes();
			for (Item i : runes.keySet()) {
				s += runes.get(i) + " " + ObjectHelper.getItemName(i) + ",";
			}
			return s;
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
		write(name + value);
	}

	public void printItemInfobox(Item Item, LivingEntity player) {
		String displayName = ObjectHelper.getItemName(Item);
		List<ResourceLocation> tags = getItemTags(Item);
		ItemStack itemStack = new ItemStack(Item);
		int stackSize = itemStack.getItem().getMaxStackSize();

		write(HEAD);
		write("|name=" + displayName);
		write("|image=" + displayName + ".png");
		writeIfExists("|damage=", getAttribute(itemStack, Attributes.ATTACK_DAMAGE, 0, ""));
		write("|specialdamage="); // manual input
		writeIfExists("|attackspeed=", getAttackSpeed(itemStack));
		writeIfExists("|unholstertime=", getUnholsterTime(itemStack));
		writeIfExists("|knockback=", getAttribute(itemStack, Attributes.ATTACK_KNOCKBACK, 0, ""));
		writeIfExists("|armor=", getAttribute(itemStack, Attributes.ARMOR, 0, ""));
		writeIfExists("|armortoughness=", getAttribute(itemStack, Attributes.ARMOR_TOUGHNESS, 0, ""));
		writeIfExists("|durability=", noStringIfZero(itemStack.getMaxDamage()));
		writeIfExists("|ammo=", getAmmoType(itemStack));
		writeIfExists("|drawspeed=", getDrawSpeed(itemStack));
		writeIfExists("|firerate=", getFireRate(itemStack));
		writeIfExists("|firetype=", getFireType(itemStack));
		writeIfExists("|hunger=", getHunger(itemStack, player));
		writeIfExists("|saturation=", getSaturation(itemStack, player));
		writeIfExists("|efficiency=", getEfficiency(itemStack));
		writeIfExists("|harvestlevel=", getHarvestLevel(itemStack));
		write("|effect=");
		write("|tooltip=");
		write("|stackable=" + (stackSize == 1 ? "No" : "Yes (" + stackSize + ")"));
		write("|raritycolor=" + convertRarityColor(itemStack.getRarity()));
		write("|id=" + ForgeRegistries.ITEMS.getKey(Item));
		write("|versionadded=");
		write(END);
	}
}

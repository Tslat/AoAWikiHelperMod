package net.tslat.aoawikihelpermod.dataprintouts;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.tslat.aoa3.advent.AdventOfAscension;
import net.tslat.aoa3.item.misc.RuneItem;
import net.tslat.aoa3.item.weapon.AdventWeapon;
import net.tslat.aoa3.item.weapon.archergun.BaseArchergun;
import net.tslat.aoa3.item.weapon.blaster.BaseBlaster;
import net.tslat.aoa3.item.weapon.bow.BaseBow;
import net.tslat.aoa3.item.weapon.cannon.BaseCannon;
import net.tslat.aoa3.item.weapon.greatblade.BaseGreatblade;
import net.tslat.aoa3.item.weapon.gun.BaseGun;
import net.tslat.aoa3.item.weapon.maul.BaseMaul;
import net.tslat.aoa3.item.weapon.shotgun.BaseShotgun;
import net.tslat.aoa3.item.weapon.sniper.BaseSniper;
import net.tslat.aoa3.item.weapon.staff.BaseStaff;
import net.tslat.aoa3.item.weapon.sword.BaseSword;
import net.tslat.aoa3.item.weapon.thrown.BaseThrownWeapon;
import net.tslat.aoa3.item.weapon.vulcane.BaseVulcane;
import net.tslat.aoa3.library.misc.AoAAttributes;
import net.tslat.aoa3.utils.ItemUtil;
import net.tslat.aoa3.utils.StringUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class CommandPrintWeaponsData extends CommandBase {
	@Override
	public String getName() {
		return "printweaponsdata";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/printweaponsdata [clipboard] - Prints out all AoA weapons data to file. Optionally copy contents to clipboard.";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
		World world = sender.getEntityWorld();

		if (!world.isRemote) {
			boolean copyToClipboard = args.length > 0 && args[0].equalsIgnoreCase("clipboard");

			if (copyToClipboard && server.isDedicatedServer()) {
				sender.sendMessage(new TextComponentString("Can't copy contents of file to clipboard on dedicated servers, skipping."));
				copyToClipboard = false;
			}
			
			List<String> data = new ArrayList<String>();

			data.add("AoA v" + AdventOfAscension.version + " weapons data printout below: ");
			data.add("");
			data.add("---~~~---~~~---~~~");

			HashSet<BaseSword> swords = new HashSet<BaseSword>();
			HashSet<BaseGreatblade> greatblades = new HashSet<BaseGreatblade>();
			HashSet<BaseMaul> mauls = new HashSet<BaseMaul>();
			HashSet<BaseShotgun> shotguns = new HashSet<BaseShotgun>();
			HashSet<BaseSniper> snipers = new HashSet<BaseSniper>();
			HashSet<BaseCannon> cannons = new HashSet<BaseCannon>();
			HashSet<BaseBlaster> blasters = new HashSet<BaseBlaster>();
			HashSet<BaseArchergun> archerguns = new HashSet<BaseArchergun>();
			HashSet<BaseThrownWeapon> thrownWeapons = new HashSet<BaseThrownWeapon>();
			HashSet<BaseGun> guns = new HashSet<BaseGun>();
			HashSet<BaseVulcane> vulcanes = new HashSet<BaseVulcane>();
			HashSet<BaseBow> bows = new HashSet<BaseBow>();
			HashSet<BaseStaff> staves = new HashSet<BaseStaff>();

			for (Item item : ForgeRegistries.ITEMS) {
				if (item instanceof AdventWeapon) {
					if (item instanceof BaseSword) {
						swords.add((BaseSword)item);
					}
					else if (item instanceof BaseGreatblade) {
						greatblades.add((BaseGreatblade)item);
					}
					else if (item instanceof BaseMaul) {
						mauls.add((BaseMaul)item);
					}
					else if (item instanceof BaseShotgun) {
						shotguns.add((BaseShotgun)item);
					}
					else if (item instanceof BaseSniper) {
						snipers.add((BaseSniper)item);
					}
					else if (item instanceof BaseCannon) {
						cannons.add((BaseCannon)item);
					}
					else if (item instanceof BaseBlaster) {
						blasters.add((BaseBlaster)item);
					}
					else if (item instanceof BaseArchergun) {
						archerguns.add((BaseArchergun)item);
					}
					else if (item instanceof BaseThrownWeapon) {
						thrownWeapons.add((BaseThrownWeapon)item);
					}
					else if (item instanceof BaseGun) {
						guns.add((BaseGun)item);
					}
					else if (item instanceof BaseVulcane) {
						vulcanes.add((BaseVulcane)item);
					}
					else if (item instanceof BaseBow) {
						bows.add((BaseBow)item);
					}
					else if (item instanceof BaseStaff) {
						staves.add((BaseStaff)item);
					}
				}
			}

			data.add("Swords: ---~~~---~~~---~~~");

			for (BaseSword sword : swords) {
				data.add(new ItemStack(sword).getDisplayName());
				data.add("ID: " + sword.getRegistryName().toString());
				data.add("Stats: ");
				data.add("    Damage: " + sword.getAttackDamage());
				data.add("    Durability: " + sword.getMaxDamage());
				data.add("    Attack Speed: " + StringUtil.roundToNthDecimalPlace((float)ItemUtil.getStackAttributeValue(new ItemStack(sword), SharedMonsterAttributes.ATTACK_SPEED, (EntityPlayer)sender, EntityEquipmentSlot.MAINHAND, AoAAttributes.VANILLA_ATTACK_SPEED), 2));
				data.add("---~~~---~~~---~~~");
			}

			data.add("Greatblades: ---~~~---~~~---~~~");

			for (BaseGreatblade greatblade : greatblades) {
				data.add(new ItemStack(greatblade).getDisplayName());
				data.add("ID: " + greatblade.getRegistryName().toString());
				data.add("Stats: ");
				data.add("    Damage: " + greatblade.getDamage());
				data.add("    Durability: " + greatblade.getMaxDamage());
				data.add("    Attack Speed: " + StringUtil.roundToNthDecimalPlace((float)ItemUtil.getStackAttributeValue(new ItemStack(greatblade), SharedMonsterAttributes.ATTACK_SPEED, (EntityPlayer)sender, EntityEquipmentSlot.MAINHAND, AoAAttributes.VANILLA_ATTACK_SPEED), 2));
				data.add("    Reach: " + ((int)greatblade.getReach()) + " blocks");
				data.add("---~~~---~~~---~~~");
			}

			data.add("Mauls: ---~~~---~~~---~~~");

			for (BaseMaul maul : mauls) {
				data.add(new ItemStack(maul).getDisplayName());
				data.add("ID: " + maul.getRegistryName().toString());
				data.add("Stats: ");
				data.add("    Damage: " + maul.getDamage());
				data.add("    Durability: " + maul.getMaxDamage());
				data.add("    Attack Speed: " + StringUtil.roundToNthDecimalPlace((float)ItemUtil.getStackAttributeValue(new ItemStack(maul), SharedMonsterAttributes.ATTACK_SPEED, (EntityPlayer)sender, EntityEquipmentSlot.MAINHAND, AoAAttributes.VANILLA_ATTACK_SPEED), 2));
				data.add("    Knockback: " + (maul.getBaseKnockback()));
				data.add("---~~~---~~~---~~~");
			}

			data.add("Shotguns: ---~~~---~~~---~~~");

			for (BaseShotgun shotgun : shotguns) {
				data.add(new ItemStack(shotgun).getDisplayName());
				data.add("ID: " + shotgun.getRegistryName().toString());
				data.add("Stats: ");
				data.add("    Damage per pellet: " + shotgun.getDamage());
				data.add("    Pellets: " + shotgun.getPelletCount());
				data.add("    Durability: " + shotgun.getMaxDamage());
				data.add("    Firing Speed: " + (2000 / shotgun.getFiringDelay()) / (double)100 + "/sec");
				data.add("    Recoil: " + shotgun.getRecoil());

				data.add("    Unholster Time: " + StringUtil.roundToNthDecimalPlace(1 / (((int)(400 * (1 - (-ItemUtil.getStackAttributeValue(new ItemStack(shotgun), SharedMonsterAttributes.ATTACK_SPEED, (EntityPlayer)sender, EntityEquipmentSlot.MAINHAND, AoAAttributes.ATTACK_SPEED_MAINHAND)) / 100f))) / 100f), 2) + "s");
				data.add("---~~~---~~~---~~~");
			}

			data.add("Snipers: ---~~~---~~~---~~~");

			for (BaseSniper sniper : snipers) {
				data.add(new ItemStack(sniper).getDisplayName());
				data.add("ID: " + sniper.getRegistryName().toString());
				data.add("Stats: ");
				data.add("    Damage per shot: " + sniper.getDamage());
				data.add("    Durability: " + sniper.getMaxDamage());
				data.add("    Firing Speed: " + (2000 / sniper.getFiringDelay()) / (double)100 + "/sec");
				data.add("    Recoil: " + sniper.getRecoil());
				data.add("    Unholster Time: " + StringUtil.roundToNthDecimalPlace(1 / (((int)(400 * (1 - (-ItemUtil.getStackAttributeValue(new ItemStack(sniper), SharedMonsterAttributes.ATTACK_SPEED, (EntityPlayer)sender, EntityEquipmentSlot.MAINHAND, AoAAttributes.ATTACK_SPEED_MAINHAND)) / 100f))) / 100f), 2) + "s");
				data.add("---~~~---~~~---~~~");
			}

			data.add("Guns: ---~~~---~~~---~~~");

			for (BaseGun gun : guns) {
				data.add(new ItemStack(gun).getDisplayName());
				data.add("ID: " + gun.getRegistryName().toString());
				data.add("Stats: ");
				data.add("    Damage per shot: " + gun.getDamage());
				data.add("    Durability: " + gun.getMaxDamage());
				data.add("    Firing Speed: " + (2000 / gun.getFiringDelay()) / (double)100 + "/sec");
				data.add("    Recoil: " + gun.getRecoil());
				data.add("    Unholster Time: " + StringUtil.roundToNthDecimalPlace(1 / (((int)(400 * (1 - (-ItemUtil.getStackAttributeValue(new ItemStack(gun), SharedMonsterAttributes.ATTACK_SPEED, (EntityPlayer)sender, EntityEquipmentSlot.MAINHAND, AoAAttributes.ATTACK_SPEED_MAINHAND)) / 100f))) / 100f), 2) + "s");
				data.add("---~~~---~~~---~~~");
			}

			data.add("Cannons: ---~~~---~~~---~~~");

			for (BaseCannon cannon : cannons) {
				data.add(new ItemStack(cannon).getDisplayName());
				data.add("ID: " + cannon.getRegistryName().toString());
				data.add("Stats: ");
				data.add("    Damage per shot: " + cannon.getDamage());
				data.add("    Durability: " + cannon.getMaxDamage());
				data.add("    Firing Speed: " + (2000 / cannon.getFiringDelay()) / (double)100 + "/sec");
				data.add("    Recoil: " + cannon.getRecoil());
				data.add("    Unholster Time: " + StringUtil.roundToNthDecimalPlace(1 / (((int)(400 * (1 - (-ItemUtil.getStackAttributeValue(new ItemStack(cannon), SharedMonsterAttributes.ATTACK_SPEED, (EntityPlayer)sender, EntityEquipmentSlot.MAINHAND, AoAAttributes.ATTACK_SPEED_MAINHAND)) / 100f))) / 100f), 2) + "s");
				data.add("---~~~---~~~---~~~");
			}

			data.add("Blasters: ---~~~---~~~---~~~");

			for (BaseBlaster blaster : blasters) {
				data.add(new ItemStack(blaster).getDisplayName());
				data.add("ID: " + blaster.getRegistryName().toString());
				data.add("Stats: ");
				data.add("    Damage: " + blaster.getDamage());
				data.add("    Durability: " + blaster.getMaxDamage());
				data.add("    Firing Speed: " + (2000 / blaster.getFiringDelay()) / (double)100 + "/sec");
				data.add("    Energy Cost: " + blaster.getEnergyCost());
				data.add("    Unholster Time: " + StringUtil.roundToNthDecimalPlace(1 / (float)(ItemUtil.getStackAttributeValue(new ItemStack(blaster), SharedMonsterAttributes.ATTACK_SPEED, (EntityPlayer)sender, EntityEquipmentSlot.MAINHAND, AoAAttributes.VANILLA_ATTACK_SPEED)), 2) + "s");
				data.add("---~~~---~~~---~~~");
			}

			data.add("Archerguns: ---~~~---~~~---~~~");

			for (BaseArchergun archergun : archerguns) {
				data.add(new ItemStack(archergun).getDisplayName());
				data.add("ID: " + archergun.getRegistryName().toString());
				data.add("Stats: ");
				data.add("    Damage per shot: " + archergun.getDamage());
				data.add("    Durability: " + archergun.getMaxDamage());
				data.add("    Firing Speed: " + (2000 / archergun.getFiringDelay()) / (double)100 + "/sec");
				data.add("    Recoil: " + archergun.getRecoil());
				data.add("    Unholster Time: " + StringUtil.roundToNthDecimalPlace(1 / (((int)(400 * (1 - (-ItemUtil.getStackAttributeValue(new ItemStack(archergun), SharedMonsterAttributes.ATTACK_SPEED, (EntityPlayer)sender, EntityEquipmentSlot.MAINHAND, AoAAttributes.ATTACK_SPEED_MAINHAND)) / 100f))) / 100f), 2) + "s");
				data.add("---~~~---~~~---~~~");
			}

			data.add("Bows: ---~~~---~~~---~~~");

			for (BaseBow bow : bows) {
				data.add(new ItemStack(bow).getDisplayName());
				data.add("ID: " + bow.getRegistryName().toString());
				data.add("Stats: ");
				data.add("    Damage per arrow: " + bow.getDamage());
				data.add("    Durability: " + bow.getMaxDamage());
				data.add("    Draw Time: " + (((int)(72000 / bow.getDrawSpeedMultiplier()) / 720) / (double)100) + "s");
				data.add("---~~~---~~~---~~~");
			}

			data.add("Thrown Weapons: ---~~~---~~~---~~~");

			for (BaseThrownWeapon throwable : thrownWeapons) {
				data.add(new ItemStack(throwable).getDisplayName());
				data.add("ID: " + throwable.getRegistryName().toString());
				data.add("Stats: ");
				data.add("    Damage: " + throwable.getDamage());
				data.add("    Throw Speed: " + (2000 / throwable.getFiringDelay()) / (double)100 + "/sec");
				data.add("---~~~---~~~---~~~");
			}

			data.add("Vulcanes: ---~~~---~~~---~~~");

			for (BaseVulcane vulcane : vulcanes) {
				data.add(new ItemStack(vulcane).getDisplayName());
				data.add("ID: " + vulcane.getRegistryName().toString());
				data.add("Stats: ");
				data.add("    Damage: " + vulcane.getDamage());
				data.add("    Durability: " + vulcane.getMaxDamage());
				data.add("---~~~---~~~---~~~");
			}

			data.add("Staves: ---~~~---~~~---~~~");

			for (BaseStaff staff : staves) {
				data.add(new ItemStack(staff).getDisplayName());
				data.add("ID: " + staff.getRegistryName().toString());
				data.add("Stats: ");
				data.add("    Durability: " + staff.getMaxDamage());
				data.add("    Runes: ");

				for (Map.Entry<RuneItem, Integer> runeEntry : staff.getRunes().entrySet()) {
					data.add("        " + runeEntry.getValue() + "x " + new ItemStack(runeEntry.getKey()).getDisplayName());
				}

				data.add("---~~~---~~~---~~~");
			}


			data.add("Weapons printout complete, stats: ");
			data.add("    Swords: " + swords.size());
			data.add("    Greatblades: " + greatblades.size());
			data.add("    Mauls: " + mauls.size());
			data.add("    Shotguns: " + shotguns.size());
			data.add("    Snipers: " + snipers.size());
			data.add("    Cannons: " + cannons.size());
			data.add("    Blasters: " + blasters.size());
			data.add("    Archerguns: " + archerguns.size());
			data.add("    Thrown Weapons: " + thrownWeapons.size());
			data.add("    Guns: " + guns.size());
			data.add("    Vulcanes: " + vulcanes.size());
			data.add("    Bows: " + bows.size());
			data.add("    Staves: " + staves.size());
			data.add("");
			data.add("Total: " + (swords.size() + greatblades.size() + mauls.size() + shotguns.size() + snipers.size() + cannons.size() + blasters.size() + archerguns.size() + thrownWeapons.size() + guns.size() + vulcanes.size() + bows.size() + staves.size()));
			data.add("---~~~---~~~---~~~");

			DataPrintoutWriter.writeData("Weapons", data, sender, copyToClipboard);
		}
	}
}

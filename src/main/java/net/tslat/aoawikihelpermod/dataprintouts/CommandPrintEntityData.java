package net.tslat.aoawikihelpermod.dataprintouts;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.tslat.aoa3.advent.AdventOfAscension;
import net.tslat.aoa3.entity.base.*;
import net.tslat.aoa3.entity.minions.AoAMinion;
import net.tslat.aoa3.entity.mobs.creeponia.EntityCreeponiaCreeper;
import net.tslat.aoa3.entity.properties.BossEntity;
import net.tslat.aoa3.entity.properties.SpecialPropertyEntity;
import net.tslat.aoa3.library.Enums;
import net.tslat.aoa3.utils.StringUtil;
import net.tslat.aoa3.utils.skills.HunterUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandPrintEntityData extends CommandBase {
	@Override
	public String getName() {
		return "printentitydata";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/printentitydata [clipboard] - Prints out all AoA entity data to file. Optionally copy contents to clipboard.";
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

			data.add("AoA v" + AdventOfAscension.version + " entity data printout below: ");
			data.add("");
			data.add("---~~~---~~~---~~~");
			HashMap<String, Integer> mobCounts = new HashMap<String, Integer>();

			for (EntityEntry entry : ForgeRegistries.ENTITIES) {
				if (!entry.getName().contains("aoa3"))
					continue;

				Entity entity = entry.newInstance(((EntityPlayer)sender).world);

				data.add("Entity: " + entity.getName());
				data.add("Id: " + entry.getName().replace("aoa3.", "aoa3:"));

				String type = "Other";

				if (entity instanceof BossEntity) {
					type = "Boss";
					mobCounts.merge("Boss", 1, Integer::sum);
				}
				else if (entity instanceof AoAMeleeMob) {
					if (entity instanceof AoARangedAttacker) {
						type = "Hybrid Melee/Ranged Mob";
						mobCounts.merge("Hybrid Melee/Ranged Mob", 1, Integer::sum);
					}
					else {
						type = "Melee Mob";
						mobCounts.merge("Melee Mob", 1, Integer::sum);
					}
				}
				else if (entity instanceof AoARangedMob) {
					type = "Ranged Mob";
					mobCounts.merge("Ranged Mob", 1, Integer::sum);
				}
				else if (entity instanceof AoAFlyingMeleeMob) {
					type = "Flying Melee Mob";
					mobCounts.merge("Flying Melee Mob", 1, Integer::sum);
				}
				else if (entity instanceof AoAFlyingRangedMob) {
					type = "Flying Ranged Mob";
					mobCounts.merge("Flying Ranged Mob", 1, Integer::sum);
				}
				else if (entity instanceof AoAAmbientNPC) {
					type = "NPC";
					mobCounts.merge("NPC", 1, Integer::sum);
				}
				else if (entity instanceof AoAMinion) {
					type = "Minion";
					mobCounts.merge("Minion", 1, Integer::sum);
				}
				else if (entity instanceof EntityAnimal) {
					type = "Animal";
					mobCounts.merge("Animal", 1, Integer::sum);
				}
				else if (entity instanceof AoATrader) {
					type = "Trader";
					mobCounts.merge("Trader", 1, Integer::sum);
				}
				else if (entity instanceof IProjectile) {
					type = "Projectile";
					mobCounts.merge("Projectile", 1, Integer::sum);
				}
				else {
					if (entity instanceof EntityCreeponiaCreeper)
						type = "Creepoid";

					mobCounts.merge("Other", 1, Integer::sum);
				}

				data.add("Type: " + type);

				if (entity instanceof EntityLivingBase) {
					EntityLivingBase livingEntity = (EntityLivingBase)entity;

					String creatureType = "None";

					switch (((EntityLivingBase)entity).getCreatureAttribute()) {
						case ARTHROPOD:
							creatureType = "Arthropod";
							break;
						case ILLAGER:
							creatureType = "Illager";
							break;
						case UNDEAD:
							creatureType = "Undead";
							break;
						default:
							break;
					}

					data.add("Creature Type: " + creatureType + (entity instanceof BossEntity ? " (Boss)" : ""));
					data.add("Size: " + entity.width + "W x " + entity.height + "H");
					data.add("Stats:");
					data.add("    Health: " + livingEntity.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue());
					data.add("    Armour: " + livingEntity.getEntityAttribute(SharedMonsterAttributes.ARMOR).getBaseValue());
					data.add("    Knockback Resistance: " + StringUtil.roundToNthDecimalPlace((float)livingEntity.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getBaseValue(), 2));
					data.add("    Movement Speed: " + StringUtil.roundToNthDecimalPlace((float)livingEntity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getBaseValue(), 3));

					if (livingEntity.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) != null)
						data.add("    Strength: " + livingEntity.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue());

					if (livingEntity instanceof EntityCreeponiaCreeper)
						data.add("    Explosion Strength: " + ((EntityCreeponiaCreeper)livingEntity).getExplosionStrength());

					if (HunterUtil.isHunterCreature(livingEntity)) {
						data.add("    Hunter Level: " + HunterUtil.getHunterLevel(livingEntity));
						data.add("    Hunter Xp: " + HunterUtil.getHunterXp(livingEntity));
					}

					if (livingEntity instanceof SpecialPropertyEntity) {
						data.add("    Immunities: ");

						for (Enums.MobProperties property : ((SpecialPropertyEntity)livingEntity).getMobProperties()) {
							data.add("        " + StringUtil.capitaliseFirstLetter(property.toString()));
						}
					}
				}
				else {
					data.add("Size: " + entity.width + "W x " + entity.height + "H");
				}

				data.add("---~~~---~~~---~~~");
			}

			data.add("Entity printout complete, stats: ");

			int total = 0;

			for (Map.Entry<String, Integer> entry : mobCounts.entrySet()) {
				data.add("    " + entry.getKey() + ": " + entry.getValue());
				total += entry.getValue();
			}

			data.add("    Total: " + total);

			DataPrintoutWriter.writeData("Entity", data, sender, copyToClipboard);
		}
	}
}

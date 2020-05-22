package net.tslat.aoawikihelpermod.weaponcategories;

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
import net.tslat.aoa3.item.weapon.maul.BaseMaul;
import net.tslat.aoa3.library.misc.AoAAttributes;
import net.tslat.aoa3.utils.ItemUtil;
import net.tslat.aoa3.utils.StringUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CommandPrintMaulsOverview extends CommandBase {
	@Override
	public String getName() {
		return "printmaulsoverview";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/printmaulsoverview [clipboard] - Prints out all AoA mauls data to file. Optionally copy contents to clipboard.";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
		World world = sender.getEntityWorld();

		if (!(sender instanceof EntityPlayer)) {
			sender.sendMessage(new TextComponentString("This command can only be done ingame for accuracy."));

			return;
		}

		if (!world.isRemote) {
			boolean copyToClipboard = args.length > 0 && args[0].equalsIgnoreCase("clipboard");

			if (copyToClipboard && server.isDedicatedServer()) {
				sender.sendMessage(new TextComponentString("Can't copy contents of file to clipboard on dedicated servers, skipping."));
				copyToClipboard = false;
			}

			List<String> data = new ArrayList<String>();
			List<BaseMaul> mauls = new ArrayList<BaseMaul>();

			for (Item item : ForgeRegistries.ITEMS.getValuesCollection()) {
				if (item instanceof BaseMaul)
					mauls.add((BaseMaul)item);
			}

			mauls = mauls.stream().sorted(Comparator.comparing(maul -> maul.getItemStackDisplayName(new ItemStack(maul)))).collect(Collectors.toList());

			data.add("{| cellpadding=\"5\" class=\"sortable\" width=\"100%\" cellspacing=\"0\" border=\"1\" style=\"text-align:center\"");
			data.add("|- style=\"background-color:#eee\"");
			data.add("! Name !! data-sort-type=number | Damage !! Attack speed !! Knockback !! Durability !! Effects");
			data.add("|-");

			for (BaseMaul maul : mauls) {
				ItemStack maulStack = new ItemStack(maul);
				String name = maul.getItemStackDisplayName(maulStack);
				String attackSpeed = StringUtil.roundToNthDecimalPlace((float)ItemUtil.getStackAttributeValue(maulStack, SharedMonsterAttributes.ATTACK_SPEED, (EntityPlayer)sender, EntityEquipmentSlot.MAINHAND, AoAAttributes.VANILLA_ATTACK_SPEED), 2);

				data.add("| [[File:" + name + ".png|64px|link=]] '''[[" + name + "]]''' || {{hp|" + StringUtil.roundToNthDecimalPlace(maul.getDamage() + 1, 1) + "}} || " + attackSpeed + " || " + StringUtil.roundToNthDecimalPlace((float)maul.getBaseKnockback(), 2) + " || " + maul.getMaxDamage(maulStack) + " || ");
				data.add("|-");
			}

			data.add("|}");
			CategoryTableWriter.writeData("Mauls", data, sender, copyToClipboard);
		}
	}
}

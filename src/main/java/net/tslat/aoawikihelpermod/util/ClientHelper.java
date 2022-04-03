package net.tslat.aoawikihelpermod.util;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public final class ClientHelper {
	public static void collectTooltipLines(Item item, List<Component> baseList, boolean advanced) {
		item.appendHoverText(new ItemStack(item), null, baseList, advanced ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
	}
}

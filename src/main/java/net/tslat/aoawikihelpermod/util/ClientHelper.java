package net.tslat.aoawikihelpermod.util;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public final class ClientHelper {
	public static void collectTooltipLines(Item item, List<ITextComponent> baseList, boolean advanced) {
		item.appendHoverText(new ItemStack(item), null, baseList, advanced ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
	}
}

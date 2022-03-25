package net.tslat.aoawikihelpermod.util.loottable.condition;

import net.tslat.aoa3.content.loottable.condition.PlayerHasResource;
import net.tslat.aoa3.util.NumberUtil;
import net.tslat.aoawikihelpermod.util.FormattingHelper;

import javax.annotation.Nonnull;

public class PlayerHasResourceConditionHelper extends LootConditionHelper<PlayerHasResource> {
	@Nonnull
	@Override
	public String getDescription(PlayerHasResource condition) {
		return "if the player has at least " + NumberUtil.roundToNthDecimalPlace(condition.getAmount(), 2) + " " + FormattingHelper.createLinkableText(condition.getResource().getName().getString(), false, false, true);
	}
}

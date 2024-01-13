package net.tslat.aoawikihelpermod.util.loottable.condition;

import net.tslat.aoa3.content.loottable.condition.PlayerHasResource;
import net.tslat.aoa3.util.NumberUtil;
import net.tslat.aoawikihelpermod.util.FormattingHelper;

import javax.annotation.Nonnull;

public class PlayerHasResourceConditionHelper extends LootConditionHelper<PlayerHasResource> {
	@Nonnull
	@Override
	public String getDescription(PlayerHasResource condition) {
		return "if the player has at least " + NumberUtil.roundToNthDecimalPlace(condition.amount(), 2) + " " + FormattingHelper.createLinkableText(condition.resource().getName().getString(), false, true);
	}
}

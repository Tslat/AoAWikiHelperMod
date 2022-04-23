package net.tslat.aoawikihelpermod.util.loottable.function;

import net.tslat.aoa3.content.loottable.function.GrantSkillXp;
import net.tslat.aoa3.util.NumberUtil;
import net.tslat.aoawikihelpermod.util.FormattingHelper;

import javax.annotation.Nonnull;

public class GrantSkillXpFunctionHelper extends LootFunctionHelper<GrantSkillXp> {
	@Nonnull
	@Override
	public String getDescription(GrantSkillXp function) {
		return "will additionally grant " + NumberUtil.roundToNthDecimalPlace(function.getXp(), 2) + " " + FormattingHelper.createLinkableText(function.getSkill().getName().getString(), false, true) + " xp";
	}
}

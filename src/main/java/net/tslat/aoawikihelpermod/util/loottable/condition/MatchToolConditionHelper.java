package net.tslat.aoawikihelpermod.util.loottable.condition;

import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.loot.conditions.MatchTool;
import net.minecraft.tags.ITag;
import net.tslat.aoawikihelpermod.util.FormattingHelper;

import javax.annotation.Nonnull;

public class MatchToolConditionHelper extends LootConditionHelper<MatchTool> {
	@Nonnull
	@Override
	public String getDescription(MatchTool condition) {
		ItemPredicate predicate = condition.predicate;

		String heldItemParticle2;

		if (predicate.item != null) {
			heldItemParticle2 = "is " + FormattingHelper.createLinkableItem(predicate.item, false, true);
		}
		else if (predicate.tag instanceof ITag.INamedTag) {
			heldItemParticle2 = "is anything tagged as " + FormattingHelper.createLinkableTag(((ITag.INamedTag<?>)predicate.tag).getName().toString());
		}
		else {
			heldItemParticle2 = "meets certain conditions";
		}

		return "if the tool used " + heldItemParticle2;
	}
}

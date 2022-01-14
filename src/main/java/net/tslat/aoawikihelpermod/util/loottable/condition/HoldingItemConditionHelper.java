package net.tslat.aoawikihelpermod.util.loottable.condition;

import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.loot.LootContext;
import net.minecraft.tags.ITag;
import net.minecraft.util.Hand;
import net.tslat.aoa3.object.loottable.condition.HoldingItem;
import net.tslat.aoawikihelpermod.util.FormattingHelper;

import javax.annotation.Nonnull;

public class HoldingItemConditionHelper extends LootConditionHelper<HoldingItem> {
	@Nonnull
	@Override
	public String getDescription(HoldingItem condition) {
		ItemPredicate predicate = condition.getPredicate();
		LootContext.EntityTarget entityTarget = condition.getTarget();
		Hand hand = condition.getHand();

		String handParticle = hand == null ? "held item" : (hand == Hand.MAIN_HAND ? "mainhand item" : "offhand item");
		String heldItemParticle;

		if (predicate.item != null) {
			heldItemParticle = "is " + FormattingHelper.createLinkableItem(predicate.item, false, true);
		}
		else if (predicate.tag instanceof ITag.INamedTag) {
			heldItemParticle = "is anything tagged as " + FormattingHelper.createLinkableTag(((ITag.INamedTag<?>)predicate.tag).getName().toString());
		}
		else {
			heldItemParticle = "meets certain conditions";
		}

		switch (entityTarget) {
			case THIS:
				return "if the target entity's " + handParticle + " " + heldItemParticle;
			case KILLER:
				return "if the attacking entity's " + handParticle + " " + heldItemParticle;
			case DIRECT_KILLER:
				return "if the directly killing entity's " + handParticle + " " + heldItemParticle;
			case KILLER_PLAYER:
				return "if the killer is a player, and if their " + handParticle + " " + heldItemParticle;
			default:
				return "";
		}
	}
}

package net.tslat.aoawikihelpermod.util.loottable.condition;

import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootContext;
import net.tslat.aoa3.content.loottable.condition.HoldingItem;
import net.tslat.aoawikihelpermod.util.FormattingHelper;

import javax.annotation.Nonnull;
import java.util.Iterator;

public class HoldingItemConditionHelper extends LootConditionHelper<HoldingItem> {
	@Nonnull
	@Override
	public String getDescription(HoldingItem condition) {
		ItemPredicate predicate = condition.getPredicate();
		LootContext.EntityTarget entityTarget = condition.getTarget();
		InteractionHand hand = condition.getHand();

		String handParticle = hand == null ? "held item" : (hand == InteractionHand.MAIN_HAND ? "mainhand item" : "offhand item");
		StringBuilder heldItemParticle;

		if (predicate.items != null) {
			if (predicate.items.size() == 1) {
				heldItemParticle = new StringBuilder("is " + FormattingHelper.createLinkableItem(predicate.items.stream().findFirst().get(), false, true));
			}
			else {
				Iterator<Item> iterator = predicate.items.iterator();
				heldItemParticle = new StringBuilder("is " + FormattingHelper.createLinkableItem(iterator.next(), false, true));

				while (iterator.hasNext()) {
					Item item = iterator.next();

					if (iterator.hasNext()) {
						heldItemParticle.append(", ");
					}
					else {
						heldItemParticle.append(", or ");
					}

					heldItemParticle.append(FormattingHelper.createLinkableItem(item, false, true));
				}
			}
		}
		else if (predicate.tag != null) {
			heldItemParticle = new StringBuilder("is anything tagged as " + FormattingHelper.createLinkableTag(predicate.tag.location().toString()));
		}
		else {
			heldItemParticle = new StringBuilder("meets certain conditions");
		}

		return switch (entityTarget) {
			case THIS -> "if the target entity's " + handParticle + " " + heldItemParticle;
			case KILLER -> "if the attacking entity's " + handParticle + " " + heldItemParticle;
			case DIRECT_KILLER -> "if the directly killing entity's " + handParticle + " " + heldItemParticle;
			case KILLER_PLAYER -> "if the killer is a player, and if their " + handParticle + " " + heldItemParticle;
		};
	}
}

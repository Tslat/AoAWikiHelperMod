package net.tslat.aoawikihelpermod.util.loottable.condition;

import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.tslat.aoawikihelpermod.util.FormattingHelper;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.Optional;

public class MatchToolConditionHelper extends LootConditionHelper<MatchTool> {
	@Nonnull
	@Override
	public String getDescription(MatchTool condition) {
		Optional<ItemPredicate> optionalPredicate = condition.predicate();

		if (optionalPredicate.isEmpty())
			return "";

		ItemPredicate predicate = optionalPredicate.get();
		StringBuilder heldItemParticle;

		if (predicate.items().isPresent()) {
			if (predicate.items().get().size() == 1) {
				heldItemParticle = new StringBuilder("is " + FormattingHelper.createLinkableItem(predicate.items().get().stream().findFirst().get().value(), false, true));
			}
			else {
				Iterator<Holder<Item>> iterator = predicate.items().get().iterator();
				heldItemParticle = new StringBuilder("is " + FormattingHelper.createLinkableItem(iterator.next().value(), false, true));

				while (iterator.hasNext()) {
					Item item = iterator.next().value();

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
		else if (predicate.components() != DataComponentPredicate.EMPTY) {
			heldItemParticle = new StringBuilder("matches specific item component values");
		}
		else {
			heldItemParticle = new StringBuilder("meets certain conditions");
		}

		return "if the tool used " + heldItemParticle;
	}
}

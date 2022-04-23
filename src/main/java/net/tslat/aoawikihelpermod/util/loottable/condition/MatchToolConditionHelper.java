package net.tslat.aoawikihelpermod.util.loottable.condition;

import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.tslat.aoawikihelpermod.util.FormattingHelper;

import javax.annotation.Nonnull;
import java.util.Iterator;

public class MatchToolConditionHelper extends LootConditionHelper<MatchTool> {
	@Nonnull
	@Override
	public String getDescription(MatchTool condition) {
		ItemPredicate predicate = condition.predicate;

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
			heldItemParticle = new StringBuilder("is anything tagged as " + FormattingHelper.createLinkableTag(predicate.tag.location().toString(), Items.STONE));
		}
		else {
			heldItemParticle = new StringBuilder("meets certain conditions");
		}

		return "if the tool used " + heldItemParticle;
	}
}

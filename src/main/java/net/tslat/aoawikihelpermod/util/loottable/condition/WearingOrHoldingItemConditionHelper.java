package net.tslat.aoawikihelpermod.util.loottable.condition;

import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootContext;
import net.tslat.aoa3.content.loottable.condition.WearingOrHoldingItem;
import net.tslat.aoawikihelpermod.util.FormattingHelper;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.Optional;

public class WearingOrHoldingItemConditionHelper extends LootConditionHelper<WearingOrHoldingItem> {
	@Nonnull
	@Override
	public String getDescription(WearingOrHoldingItem condition) {
		ItemPredicate predicate = condition.predicate();
		LootContext.EntityTarget entityTarget = condition.target();
		Optional<EquipmentSlot> slot = condition.slot();

		String slotParticle = slot.map(equipmentSlot -> switch (equipmentSlot) {
            case MAINHAND -> "mainhand item";
            case OFFHAND -> "offhand item";
            case FEET -> "boots";
            case LEGS -> "leggings";
            case CHEST -> "chestplate";
            case HEAD -> "helmet";
            case BODY -> "body";
        }).orElse("equipment");

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

		return switch (entityTarget) {
			case THIS -> "if the target entity's " + slotParticle + " " + heldItemParticle;
			case ATTACKER -> "if the attacking entity's " + slotParticle + " " + heldItemParticle;
			case DIRECT_ATTACKER -> "if the directly killing entity's " + slotParticle + " " + heldItemParticle;
			case ATTACKING_PLAYER -> "if the killer is a player, and if their " + slotParticle + " " + heldItemParticle;
		};
	}
}

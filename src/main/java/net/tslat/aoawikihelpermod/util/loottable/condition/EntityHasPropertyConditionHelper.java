package net.tslat.aoawikihelpermod.util.loottable.condition;

import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;

import javax.annotation.Nonnull;

public class EntityHasPropertyConditionHelper extends LootConditionHelper<LootItemEntityPropertyCondition> {
	@Nonnull
	@Override
	public String getDescription(LootItemEntityPropertyCondition condition) {
		return switch (condition.entityTarget()) {
			case THIS -> "if the target entity meets certain conditions";
			case ATTACKER -> "if the attacking entity meets certain conditions";
			case DIRECT_ATTACKER -> "if the directly killing entity meets certain conditions";
			case ATTACKING_PLAYER -> "if the killer is a player, and meets certain conditions";
		};
	}
}

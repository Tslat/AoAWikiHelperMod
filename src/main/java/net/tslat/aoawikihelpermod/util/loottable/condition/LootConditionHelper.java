package net.tslat.aoawikihelpermod.util.loottable.condition;

import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import javax.annotation.Nonnull;

public abstract class LootConditionHelper<T extends LootItemCondition> {
	public final String getDescriptor(LootItemCondition condition) {
		return getDescription((T)condition);
	}

	/**
	 * Returns the description of the condition based on the provided instance. May return an empty string if not applicable or has null-values
	 *
	 * @param condition Type-casted LootItemCondition
	 * @return The description string for the condition.
	 */
	@Nonnull
	public abstract String getDescription(T condition);
}

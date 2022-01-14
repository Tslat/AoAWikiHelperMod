package net.tslat.aoawikihelpermod.util.loottable.condition;

import net.minecraft.loot.conditions.ILootCondition;

import javax.annotation.Nonnull;

public abstract class LootConditionHelper<T extends ILootCondition> {
	public final String getDescriptor(ILootCondition condition) {
		return getDescription((T)condition);
	}

	/**
	 * Returns the description of the condition based on the provided instance. May return an empty string if not applicable or has null-values
	 *
	 * @param condition Type-casted ILootCondition
	 * @return The description string for the condition.
	 */
	@Nonnull
	public abstract String getDescription(T condition);
}

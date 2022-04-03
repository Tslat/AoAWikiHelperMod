package net.tslat.aoawikihelpermod.util.loottable.function;

import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import javax.annotation.Nonnull;

public abstract class LootFunctionHelper<T extends LootItemFunction> {
	public final String getDescriptor(LootItemFunction function) {
		return getDescription((T)function);
	}

	/**
	 * Returns the description of the function based on the provided instance. May return an empty string if not applicable or has null-values
	 *
	 * @param function Type-casted LootItemFunction
	 * @return The description string for the function. Should always start with 'if ' for consistency
	 */
	@Nonnull
	public abstract String getDescription(T function);
}

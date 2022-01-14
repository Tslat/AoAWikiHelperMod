package net.tslat.aoawikihelpermod.util.loottable.function;

import net.minecraft.loot.functions.ILootFunction;

import javax.annotation.Nonnull;

public abstract class LootFunctionHelper<T extends ILootFunction> {
	public final String getDescriptor(ILootFunction function) {
		return getDescription((T)function);
	}

	/**
	 * Returns the description of the function based on the provided instance. May return an empty string if not applicable or has null-values
	 *
	 * @param function Type-casted ILootFunction
	 * @return The description string for the function. Should always start with 'if ' for consistency
	 */
	@Nonnull
	public abstract String getDescription(T function);
}

package net.tslat.aoawikihelpermod.util.loottable.condition;

import net.minecraft.loot.conditions.DamageSourceProperties;

import javax.annotation.Nonnull;

public class DamageSourcePropertiesConditionHelper extends LootConditionHelper<DamageSourceProperties> {
	@Nonnull
	@Override
	public String getDescription(DamageSourceProperties condition) {
		return "if the damage source meets certain conditions";
	}
}

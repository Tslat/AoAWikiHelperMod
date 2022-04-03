package net.tslat.aoawikihelpermod.util.loottable.condition;

import net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition;

import javax.annotation.Nonnull;

public class DamageSourcePropertiesConditionHelper extends LootConditionHelper<DamageSourceCondition> {
	@Nonnull
	@Override
	public String getDescription(DamageSourceCondition condition) {
		return "if the damage source meets certain conditions";
	}
}

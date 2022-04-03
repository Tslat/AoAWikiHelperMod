package net.tslat.aoawikihelpermod.util.loottable.condition;

import net.minecraft.world.level.storage.loot.predicates.LocationCheck;

import javax.annotation.Nonnull;

public class LocationCheckConditionHelper extends LootConditionHelper<LocationCheck> {
	@Nonnull
	@Override
	public String getDescription(LocationCheck condition) {
		return "if the source of the drops is from a specific location";
	}
}

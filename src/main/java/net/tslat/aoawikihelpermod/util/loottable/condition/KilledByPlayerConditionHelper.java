package net.tslat.aoawikihelpermod.util.loottable.condition;

import net.minecraft.loot.conditions.KilledByPlayer;

import javax.annotation.Nonnull;

public class KilledByPlayerConditionHelper extends LootConditionHelper<KilledByPlayer> {
	@Nonnull
	@Override
	public String getDescription(KilledByPlayer condition) {
		return "if the target was killed by a player";
	}
}

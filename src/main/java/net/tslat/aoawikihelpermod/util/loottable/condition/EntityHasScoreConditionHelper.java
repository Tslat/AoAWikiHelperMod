package net.tslat.aoawikihelpermod.util.loottable.condition;

import net.minecraft.loot.conditions.EntityHasScore;

import javax.annotation.Nonnull;

public class EntityHasScoreConditionHelper extends LootConditionHelper<EntityHasScore> {
	@Nonnull
	@Override
	public String getDescription(EntityHasScore condition) {
		switch (condition.entityTarget) {
			case THIS:
				return "if the target entity meets certain conditions";
			case KILLER:
				return "if the attacking entity meets certain conditions";
			case DIRECT_KILLER:
				return "if the directly killing entity meets certain conditions";
			case KILLER_PLAYER:
				return "if the killer is a player, and meets certain conditions";
			default:
				return "";
		}
	}
}

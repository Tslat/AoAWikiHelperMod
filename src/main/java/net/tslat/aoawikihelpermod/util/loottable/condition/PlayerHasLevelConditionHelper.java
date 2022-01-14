package net.tslat.aoawikihelpermod.util.loottable.condition;

import net.tslat.aoa3.object.loottable.condition.PlayerHasLevel;

import javax.annotation.Nonnull;

public class PlayerHasLevelConditionHelper extends LootConditionHelper<PlayerHasLevel> {
	@Nonnull
	@Override
	public String getDescription(PlayerHasLevel condition) {
		return "if the player has at least level " + condition.getLevel() + " " + condition.getSkill().getName().getString();
	}
}

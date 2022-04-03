package net.tslat.aoawikihelpermod.util.loottable.condition;

import net.minecraft.world.level.storage.loot.predicates.WeatherCheck;

import javax.annotation.Nonnull;

public class WeatherCheckConditionHelper extends LootConditionHelper<WeatherCheck> {
	@Nonnull
	@Override
	public String getDescription(WeatherCheck condition) {
		Boolean isRaining = condition.isRaining;
		Boolean isThundering = condition.isThundering;


		if (isRaining != null || isThundering != null) {
			StringBuilder stringBuilder = new StringBuilder("if ");
			if (isRaining != null) {
				if (isRaining) {
					stringBuilder.append("it is raining");
				}
				else {
					stringBuilder.append("it isn't raining");
				}
			}

			if (isThundering != null) {
				if (isThundering) {
					if (isRaining != null) {
						stringBuilder.append(", and it is thundering");
					}
					else {
						stringBuilder.append("is it thundering");
					}
				}
				else {
					if (isRaining != null) {
						stringBuilder.append(", and it isn't thundering");
					}
					else {
						stringBuilder.append("it isn't thundering");
					}
				}
			}

			return stringBuilder.toString();
		}

		return "";
	}
}

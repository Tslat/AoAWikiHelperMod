package net.tslat.aoawikihelpermod.util.loottable.condition;

import net.minecraft.world.level.storage.loot.predicates.WeatherCheck;

import javax.annotation.Nonnull;
import java.util.Optional;

public class WeatherCheckConditionHelper extends LootConditionHelper<WeatherCheck> {
	@Nonnull
	@Override
	public String getDescription(WeatherCheck condition) {
		Optional<Boolean> isRaining = condition.isRaining();
		Optional<Boolean> isThundering = condition.isThundering();


		if (isRaining.isPresent() || isThundering.isPresent()) {
			StringBuilder stringBuilder = new StringBuilder("if ");
			if (isRaining.isPresent()) {
				if (isRaining.get()) {
					stringBuilder.append("it is raining");
				}
				else {
					stringBuilder.append("it isn't raining");
				}
			}

			if (isThundering.isPresent()) {
				if (isThundering.get()) {
					if (isRaining.isPresent()) {
						stringBuilder.append(", and it is thundering");
					}
					else {
						stringBuilder.append("is it thundering");
					}
				}
				else {
					if (isRaining.isPresent()) {
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

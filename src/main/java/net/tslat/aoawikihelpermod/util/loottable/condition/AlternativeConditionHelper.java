package net.tslat.aoawikihelpermod.util.loottable.condition;

import net.minecraft.loot.conditions.Alternative;
import net.tslat.aoawikihelpermod.util.LootTableHelper;

import javax.annotation.Nonnull;

public class AlternativeConditionHelper extends LootConditionHelper<Alternative> {
	@Nonnull
	@Override
	public String getDescription(Alternative condition) {
		if (condition.terms.length == 0) {
			return "";
		}
		else if (condition.terms.length == 1) {
			String description = LootTableHelper.getConditionDescription(condition.terms[0]);

			if (description.isEmpty())
				return "";

			return "if the following is true: <br/>  " + description.substring(3);
		}
		else {
			StringBuilder builder = new StringBuilder("if any of the following is true: \n");

			for (int i = 0; i < condition.terms.length; i++) {
				if (i > 0) {
					builder.append(",");

					if (i == condition.terms.length - 1)
						builder.append(" or");

					builder.append("\n");
				}

				String description = LootTableHelper.getConditionDescription(condition.terms[i]);

				if (description.isEmpty())
					continue;

				builder.append(":").append(description.substring(3).replaceAll("\n:", "\n::"));
			}

			builder.append("\n");

			return builder.toString();
		}
	}
}

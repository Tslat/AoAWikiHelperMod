package net.tslat.aoawikihelpermod.util.printers.infoboxes;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.printers.PrintHelper;

import javax.annotation.Nullable;
import java.io.IOException;

public class EntityInfoboxPrintHelper extends PrintHelper {
	private static final String HEAD = "{{EntityInfo";
	private static final String END = "}}";


	protected EntityInfoboxPrintHelper(String fileName) throws IOException {
		super(fileName);
	}

	@Nullable
	public static EntityInfoboxPrintHelper open(String fileName) {
		try {
			return new EntityInfoboxPrintHelper(fileName);
		} catch (IOException ex) {
			return null;
		}
	}

	private <T extends Number> String noStringIfZero(T input) {
		if (input.equals(0)) return "";
		return "" + input;
	}

	private void writeIfExists(String name, String value) {
		if (value.length() == 0) return;
		write(name + value);
	}

	public void printEntityInfobox(EntityType<?> entity, Entity player) {
		String displayName = ObjectHelper.getEntityName(entity);

		write(HEAD);
		write("|name=" + displayName);
		write("|image=" + displayName + ".png");
		write("|noimage=");
		write("|image2=");
		writeIfExists("|health=", "");
		writeIfExists("|specialhealth=", "");
		writeIfExists("|size=", "");
		writeIfExists("|damage=", "");
		writeIfExists("|specialdamage=", "");
		writeIfExists("|armor=", "");
		writeIfExists("|armortoughness=", "");
		writeIfExists("|environment =", "");
		writeIfExists("|hostility =", "");
		writeIfExists("|classification =", "");
		writeIfExists("|xp=", "");
		writeIfExists("|knockbackresist=", "");
		write("|id=");
		write("|id=" + ForgeRegistries.ENTITY_TYPES.getKey(entity));
		write("|versionadded=");
		write(END);
	}
}

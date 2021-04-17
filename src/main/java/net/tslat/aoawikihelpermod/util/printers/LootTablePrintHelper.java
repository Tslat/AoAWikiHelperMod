package net.tslat.aoawikihelpermod.util.printers;

import net.minecraft.loot.LootEntry;

import javax.annotation.Nullable;
import java.io.IOException;

public class LootTablePrintHelper extends PrintHelper {
	protected LootTablePrintHelper(String fileName) throws IOException {
		super(fileName);
	}

	@Nullable
	public static LootTablePrintHelper open(String fileName) {
		try {
			return new LootTablePrintHelper(fileName);
		}
		catch (IOException ex) {
			return null;
		}
	}

	public void entry(LootEntry entry) {

	}
}

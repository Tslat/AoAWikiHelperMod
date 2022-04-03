package net.tslat.aoawikihelpermod.util.printers;

import net.minecraft.resources.ResourceLocation;
import net.tslat.aoawikihelpermod.util.printers.handlers.HaulingTablePrintHandler;

import javax.annotation.Nullable;
import java.io.IOException;

public class HaulingTablePrintHelper extends PrintHelper {
	private static final String HEAD = "{{LootTable";
	private static final String END = "}}";

	private int tableCount = 0;
	private final boolean singleTable;

	protected HaulingTablePrintHelper(String fileName, boolean singleTable) throws IOException {
		super(fileName);

		this.singleTable = singleTable;
	}

	@Nullable
	public static HaulingTablePrintHelper open(String fileName, boolean singleTable) {
		try {
			return new HaulingTablePrintHelper(fileName, singleTable);
		}
		catch (IOException ex) {
			return null;
		}
	}

	public void printTable(ResourceLocation id, HaulingTablePrintHandler printHandler) {
		if (tableCount > 0)
			write("");

		if (!singleTable)
			write(id.toString() + ":");

		write(HEAD);
		write("|type=hauling");

		write("|pool1=");

		for (String entryLine : printHandler.getEntries()) {
			write(entryLine);
		}

		if (printHandler.getTableNotes() != null)
			write("|notes=" + printHandler.getTableNotes());

		write(END);

		tableCount++;
	}
}

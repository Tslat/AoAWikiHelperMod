package net.tslat.aoawikihelpermod.util.printers;

import net.minecraft.util.ResourceLocation;
import net.tslat.aoawikihelpermod.util.printers.handlers.LootTablePrintHandler;

import javax.annotation.Nullable;
import java.io.IOException;

public class LootTablePrintHelper extends PrintHelper {
	private static final String HEAD = "{{LootTable";
	private static final String END = "}}";

	private int tableCount = 0;
	private final boolean singleTable;

	protected LootTablePrintHelper(String fileName, boolean singleTable) throws IOException {
		super(fileName);

		this.singleTable = singleTable;
	}

	@Nullable
	public static LootTablePrintHelper open(String fileName, boolean singleTable) {
		try {
			return new LootTablePrintHelper(fileName, singleTable);
		}
		catch (IOException ex) {
			return null;
		}
	}

	public void printTable(ResourceLocation id, LootTablePrintHandler printHandler) {
		if (tableCount > 0)
			write("");

		if (!singleTable)
			write(id.toString() + ":");

		write(HEAD);
		write("|type=" + printHandler.getType());

		for (LootTablePrintHandler.PoolPrintData poolData : printHandler.getPools()) {
			int index = poolData.getPoolIndex();

			write("|pool" + index + "=");

			for (String entryLine : poolData.getEntries()) {
				write(entryLine);
			}

			if (poolData.getRollsDescription() != null)
				write("|rolls" + index + "=" + poolData.getRollsDescription());

			if (poolData.getBonusRollsDescription() != null)
				write("|bonusrolls" + index + "=" + poolData.getBonusRollsDescription());

			if (poolData.getPoolNotes() != null)
				write("|notes" + index + "=" + poolData.getPoolNotes());
		}

		if (printHandler.getTableNotes() != null)
			write("|notes=" + printHandler.getTableNotes());

		write(END);

		tableCount++;
	}

	@Override
	public void close() {
		super.close();
	}
}

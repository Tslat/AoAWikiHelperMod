package net.tslat.aoawikihelpermod.util;

import net.tslat.aoawikihelpermod.AoAWikiHelperMod;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class PrintHelper implements AutoCloseable {
	private static File configDir = null;

	private final PrintWriter writer;
	private final File outputFile;

	private String indents = "";

	private PrintHelper(String fileName) throws IOException {
		this.outputFile = new File(configDir, fileName);

		try {
			if (this.outputFile.exists())
				this.outputFile.delete();

			this.outputFile.createNewFile();
			this.writer = new PrintWriter(outputFile);
		}
		catch (Exception ex) {
			AoAWikiHelperMod.LOGGER.error("Failed to create new PrintHelper {}", fileName);

			throw ex;
		}
	}

	public void write(String line) {
		this.writer.write(indents + line);
	}

	public void indent() {
		this.indents += "  ";
	}

	public void unindent() {
		if (indents.length() > 2)
			this.indents = this.indents.substring(2);
	}

	public static void init(File configPath) {
		configDir = configPath;
	}

	@Nullable
	public static PrintHelper open(String fileName) {
		try {
			return new PrintHelper(fileName);
		}
		catch (IOException ex) {
			return null;
		}
	}

	@Override
	public void close() {
		this.writer.close();
	}
}

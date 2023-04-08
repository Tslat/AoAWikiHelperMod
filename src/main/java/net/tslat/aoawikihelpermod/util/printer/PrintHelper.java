package net.tslat.aoawikihelpermod.util.printer;

import net.tslat.aoa3.library.object.MutableSupplier;
import net.tslat.aoawikihelpermod.AoAWikiHelperMod;
import net.tslat.aoawikihelpermod.util.FormattingHelper;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.function.Function;

public class PrintHelper implements AutoCloseable {
	public static File configDir = null;

	private final PrintWriter writer;
	private final File outputFile;
	protected final String name;

	private String indents = "";

	private final ArrayList<String> lines = new ArrayList<String>();
	private MutableSupplier<String> clipboardPrinter = null;

	protected PrintHelper(String fileName) throws IOException {
		this.name = fileName;
		this.outputFile = new File(configDir, formatFileName(fileName));

		try {
			if (this.outputFile.exists())
				this.outputFile.delete();

			this.outputFile.createNewFile();
			this.writer = new PrintWriter(outputFile);
		}
		catch (Exception ex) {
			AoAWikiHelperMod.LOGGER.error("Failed to create new PrintHelper {}", name);

			throw ex;
		}
	}

	public PrintHelper withClipboardOutput(MutableSupplier<String> clipboardSupplier) {
		this.clipboardPrinter = clipboardSupplier;

		return this;
	}

	private static String formatFileName(String fileName) {
		return fileName.replaceAll("[\\\\\\/\\:\\*\\?\\\"\\<\\>\\|]", ".") + " Printout " + AoAWikiHelperMod.getAoAVersion() + ".txt";
	}

	public File getOutputFile() {
		return this.outputFile;
	}

	public void write(String line) {
		if (line != null && !line.isEmpty())
			this.lines.add(this.indents + line);
	}

	public void edit(int lineNumber, Function<String, String> lineEditor) {
		if (this.lines.size() <= lineNumber)
			return;

		this.lines.set(lineNumber, lineEditor.apply(this.lines.get(lineNumber)));
	}

	public void insert(int lineNumber, String value) {
		if (this.lines.size() <= lineNumber) {
			write(value);

			return;
		}

		this.lines.add(lineNumber, value);
	}

	public void undo() {
		if (this.lines.isEmpty())
			return;

		this.lines.remove(this.lines.size() - 1);
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

	public static PrintHelper open(String fileName) throws IOException {
		return new PrintHelper(fileName);
	}

	@Override
	public void close() {
		for (String line : lines) {
			this.writer.println(line);
		}

		if (this.clipboardPrinter != null)
			this.clipboardPrinter.update(() -> FormattingHelper.listToString(lines, true));

		this.writer.close();
	}
}

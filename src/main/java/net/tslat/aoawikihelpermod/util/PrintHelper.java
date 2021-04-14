package net.tslat.aoawikihelpermod.util;

import net.tslat.aoawikihelpermod.AoAWikiHelperMod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class PrintHelper implements AutoCloseable {
	private static File configDir = null;

	private final PrintWriter writer;
	private final File outputFile;
	protected final String name;

	private String indents = "";

	private final ArrayList<String> lines = new ArrayList<String>();

	private PrintHelper(String fileName) throws IOException {
		this.name = fileName;
		this.outputFile = new File(configDir, name);

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

	public void write(String line) {
		this.lines.add(indents + line);
	}

	public void edit(int lineNumber, Function<String, String> lineEditor) {
		if (this.lines.size() <= lineNumber)
			return;

		this.lines.add(lineNumber, lineEditor.apply(this.lines.get(lineNumber)));
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
		for (String line : lines) {
			this.writer.write(line);
		}

		this.writer.close();
	}

	public static class TablePrintHelper extends PrintHelper {
		private static final String HEAD = "{|";
		private static final String GAP = "|-";

		private final String[] columns;
		private final HashMap<String, String> tableProperties = new HashMap<String, String>();
		private final HashMap<String, String> styles = new HashMap<String, String>();

		public TablePrintHelper(String fileName, String... columns) throws IOException {
			super(fileName);

			if (columns == null || columns.length <= 0)
				throw new IllegalArgumentException("Invalid table format, must provide at least one column (" + this.name + ")");

			this.columns = columns;

			withProperty("cellpadding", "5");
			withProperty("width", "100%");
			withProperty("cellspacing", "0");
			withProperty("border", "1");
			withStyle("text-align", "center");
			write(HEAD);
			write(GAP);
			write("!");
		}

		@Nullable
		public static TablePrintHelper open(String fileName, String... columns) {
			try {
				return new TablePrintHelper(fileName, columns);
			}
			catch (IOException ex) {
				return null;
			}
		}

		public TablePrintHelper withProperty(String property, String value) {
			this.tableProperties.put(property, value);

			return this;
		}

		public TablePrintHelper withStyle(String style, String value) {
			this.styles.put(style, value);

			return this;
		}

		public void entry(@Nonnull String... values) {
			if (values.length != columns.length)
				throw new IllegalArgumentException("Provided invalid number of values for table entry (" + this.name + "). Values: " + Arrays.toString(values) + ", Columns: " + Arrays.toString(columns));

			StringBuilder builder = new StringBuilder("| ");

			for (String value : values) {
				builder.append(value);
				builder.append(" || ");
			}

			write(builder.toString());
			write(GAP);
		}

		@Override
		public void close() {
			StringBuilder propertiesBuilder = new StringBuilder(HEAD);
			StringBuilder stylesBuilder = new StringBuilder("style=\"");
			StringBuilder headerBuilder = new StringBuilder(GAP);
			StringBuilder columnsBuilder = new StringBuilder("! ");
			boolean sortable = false;

			for (Map.Entry<String, String> property : tableProperties.entrySet()) {
				if (property.getKey().equals("class") && property.getValue().equals("sortable"))
					sortable = true;

				propertiesBuilder.append(" ");
				propertiesBuilder.append(property.getKey());
				propertiesBuilder.append("=\"");
				propertiesBuilder.append(property.getValue());
				propertiesBuilder.append("\"");
			}

			for (Map.Entry<String, String> style : this.styles.entrySet()) {
				if (stylesBuilder.length() > 7)
					stylesBuilder.append(" ");

				stylesBuilder.append(style.getKey());
				stylesBuilder.append(":");
				stylesBuilder.append(style.getValue());
				stylesBuilder.append(";");
			}

			stylesBuilder.append("\"");
			propertiesBuilder.append(stylesBuilder.toString());
			headerBuilder.append(" style=background-color:#eee |");

			if (sortable)
				headerBuilder.append(" data-sort-type=number |");

			for (String column : this.columns) {
				if (columnsBuilder.length() > 2)
					columnsBuilder.append(" !! ");

				columnsBuilder.append(column);
			}

			edit(0, line -> propertiesBuilder.toString());
			edit(1, line -> headerBuilder.toString());
			edit(2, line -> columnsBuilder.toString());
			write("|}");

			super.close();
		}
	}
}

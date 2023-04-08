package net.tslat.aoawikihelpermod.util.printer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TablePrintHelper extends PrintHelper {
	private static final String HEAD = "{|";
	private static final String GAP = "|-";

	private final String[] columns;
	private final HashMap<String, String> tableProperties = new HashMap<String, String>();
	private final HashMap<String, String> styles = new HashMap<String, String>();

	protected TablePrintHelper(String fileName, String... columns) throws IOException {
		super(fileName);

		if (columns == null || columns.length <= 0)
			throw new IllegalArgumentException("Invalid table format, must provide at least one column (" + this.name + ")");

		this.columns = columns;

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
			ex.printStackTrace();

			return null;
		}
	}

	public static String combineLines(String... lines) {
		return combineLines(Arrays.asList(lines));
	}

	public static String combineLines(List<String> lines) {
		StringBuilder builder = new StringBuilder();

		for (String ln : lines) {
			if (builder.length() > 0)
				builder.append(System.lineSeparator());

			builder.append(ln);
		}

		return builder.toString();
	}

	public TablePrintHelper defaultFullPageTableProperties() {
		withProperty("cellpadding", "5");
		withProperty("width", "100%");
		withProperty("cellspacing", "0");
		withProperty("border", "1");
		withStyle("text-align", "center");

		return this;
	}

	public TablePrintHelper withProperty(String property, String value) {
		this.tableProperties.put(property, value);

		return this;
	}

	public TablePrintHelper withStyle(String style, String value) {
		this.styles.put(style, value);

		return this;
	}

	public void forceEntry(@Nonnull String... values) {
		StringBuilder builder = new StringBuilder("| ");

		for (String value : values) {
			if (builder.length() > 2)
				builder.append(" || ");

			if (value.contains(System.lineSeparator())) {
				write(builder.toString());

				for (String line : value.split(System.lineSeparator())) {
					write(line);
				}

				write(GAP);

				builder = new StringBuilder("");
			}
			else {
				builder.append(value);
			}
		}

		if (builder.length() > 0) {
			write(builder.toString());
			write(GAP);
		}
	}

	public void rowId(@Nonnull String id) {
		write(GAP + " id=\"" + id + "\"");
	}

	public void entry(@Nonnull String... values) {
		if (values.length != columns.length)
			throw new IllegalArgumentException("Provided invalid number of values for table entry (" + this.name + "). Values: " + Arrays.toString(values) + ", Columns: " + Arrays.toString(columns));

		forceEntry(values);
	}

	@Override
	public void close() {
		StringBuilder propertiesBuilder = new StringBuilder(HEAD);
		StringBuilder classesBuilder = new StringBuilder(" class=\"");
		StringBuilder stylesBuilder = new StringBuilder(" style=\"");
		StringBuilder headerBuilder = new StringBuilder(GAP);
		StringBuilder columnsBuilder = new StringBuilder("! ");
		boolean sortable = false;

		for (Map.Entry<String, String> property : tableProperties.entrySet()) {
			if (property.getKey().equals("class")) {
				if (property.getValue().equals("sortable"))
					sortable = true;

				if (classesBuilder.length() > 8)
					classesBuilder.append(" ");

				classesBuilder.append(property.getValue());
				continue;
			}

			propertiesBuilder.append(" ");
			propertiesBuilder.append(property.getKey());
			propertiesBuilder.append("=\"");
			propertiesBuilder.append(property.getValue());
			propertiesBuilder.append("\"");
		}

		classesBuilder.append("\"");

		if (classesBuilder.length() > 9)
			propertiesBuilder.append(classesBuilder);

		for (Map.Entry<String, String> style : this.styles.entrySet()) {
			if (stylesBuilder.length() > 7)
				stylesBuilder.append(" ");

			stylesBuilder.append(style.getKey());
			stylesBuilder.append(":");
			stylesBuilder.append(style.getValue());
			stylesBuilder.append(";");
		}

		stylesBuilder.append("\"");

		if (this.styles.size() > 0)
			propertiesBuilder.append(stylesBuilder);

		for (String column : this.columns) {
			if (columnsBuilder.length() > 2)
				columnsBuilder.append(" !! ");

			columnsBuilder.append(column);
		}

		if (sortable)
			headerBuilder.append(" data-sort-type=number |");

		edit(0, line -> propertiesBuilder.toString());
		edit(1, line -> headerBuilder.toString());
		edit(2, line -> columnsBuilder.toString());
		insert(3, GAP);
		write("|}");

		super.close();
	}
}

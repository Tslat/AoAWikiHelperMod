package net.tslat.aoawikihelpermod.util.printer;

import net.tslat.aoa3.library.object.MutableSupplier;
import net.tslat.aoawikihelpermod.util.printer.handler.RecipePrintHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.function.Function;

public class RecipePrintHelper implements AutoCloseable {
	private final PrintHelper printHelper;

	private RecipePrintHelper(@Nonnull PrintHelper printHelper) {
		this.printHelper = printHelper;
	}

	@Nullable
	public static RecipePrintHelper open(String fileName, RecipePrintHandler recipePrintHandler) {
		try {
			if (recipePrintHandler.isPlainTextPrintout())
				return new RecipePrintHelper(new PrintHelper(fileName));

			return new RecipePrintHelper(new TablePrintHelper(fileName, recipePrintHandler.getColumnTitles()));
		}
		catch (IOException ex) {
			return null;
		}
	}

	public PrintHelper withClipboardOutput(MutableSupplier<String> clipboardSupplier) {
		return this.printHelper.withClipboardOutput(clipboardSupplier);
	}

	public File getOutputFile() {
		return this.printHelper.getOutputFile();
	}

	public void write(String line) {
		this.printHelper.write(line);
	}

	public void edit(int lineNumber, Function<String, String> lineEditor) {
		this.printHelper.edit(lineNumber, lineEditor);
	}

	public void insert(int lineNumber, String value) {
		this.printHelper.insert(lineNumber, value);
	}

	public void undo() {
		this.printHelper.undo();
	}

	public void indent() {
		this.printHelper.indent();
	}

	public void unindent() {
		this.printHelper.unindent();
	}

	public TablePrintHelper defaultFullPageTableProperties() {
		if (this.printHelper instanceof TablePrintHelper)
			return ((TablePrintHelper)this.printHelper).defaultFullPageTableProperties();

		return null;
	}

	public TablePrintHelper withProperty(String property, String value) {
		if (this.printHelper instanceof TablePrintHelper)
			return ((TablePrintHelper)this.printHelper).withProperty(property, value);

		return null;
	}

	public TablePrintHelper withStyle(String style, String value) {
		if (this.printHelper instanceof TablePrintHelper)
			return ((TablePrintHelper)this.printHelper).withStyle(style, value);

		return null;
	}

	public void entry(@Nonnull String... values) {
		if (this.printHelper instanceof TablePrintHelper) {
			((TablePrintHelper)this.printHelper).entry(values);
		}
		else {
			for (String line : values) {
				this.printHelper.write(line);
			}
		}
	}

	@Override
	public void close() {
		this.printHelper.close();
	}
}

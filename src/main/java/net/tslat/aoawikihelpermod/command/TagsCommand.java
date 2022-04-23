package net.tslat.aoawikihelpermod.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.resources.ResourceLocation;
import net.tslat.aoa3.util.StringUtil;
import net.tslat.aoawikihelpermod.AoAWikiHelperMod;
import net.tslat.aoawikihelpermod.dataskimmers.TagDataSkimmer;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.printers.TablePrintHelper;
import net.tslat.aoawikihelpermod.util.printers.handlers.TagCategoryPrintHandler;

import java.io.File;
import java.util.Arrays;

public class TagsCommand implements Command<CommandSourceStack> {
	private static final TagsCommand CMD = new TagsCommand();
	private static final SuggestionProvider<CommandSourceStack> SUGGESTION_PROVIDER = SuggestionProviders.register(new ResourceLocation(AoAWikiHelperMod.MOD_ID, "tag_types"), (context, builder) -> SharedSuggestionProvider.suggestResource(TagDataSkimmer.tagTypes().stream(), builder));

	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("tags").executes(CMD);

		builder.then(Commands.argument("tag_type", ResourceLocationArgument.id())
				.suggests(SUGGESTION_PROVIDER)
				.executes(TagsCommand::printTags));

		return builder;
	}

	protected String commandName() {
		return "Tags";
	}

	@Override
	public int run(CommandContext<CommandSourceStack> context) {
		WikiHelperCommand.info(context.getSource(), commandName(), "Print out overview data on object tags.");

		return 1;
	}

	private static int printTags(CommandContext<CommandSourceStack> cmd) {
		ResourceLocation tagType = ResourceLocationArgument.getId(cmd, "tag_type");
		TagCategoryPrintHandler<?> tagCategoryPrintHandler = TagDataSkimmer.get(tagType);

		if (tagCategoryPrintHandler == null) {
			WikiHelperCommand.error(cmd.getSource(), "Tags", "Unable to find tags for registry type: " + tagType);

			return 1;
		}

		for (String namespace : tagCategoryPrintHandler.getNameSpaces()) {
			File outputFile;
			String fileName = "Tags Overview - " + tagType.getPath() + " - " + StringUtil.toTitleCase(namespace);

			try (TablePrintHelper printHelper = TablePrintHelper.open(fileName, "Tag", "Default Contents")) {
				printHelper.defaultFullPageTableProperties();
				printHelper.withProperty("class", "sortable mw-collapsible");

				for (String[] entry : tagCategoryPrintHandler.getCategoryPrintout(namespace)) {
					printHelper.rowId(entry[0]);
					printHelper.entry(Arrays.copyOfRange(entry, 1, 3));
				}

				outputFile = printHelper.getOutputFile();
			}

			WikiHelperCommand.success(cmd.getSource(), "Tags", FormattingHelper.generateResultMessage(outputFile, fileName, null));
		}

		return 1;
	}
}

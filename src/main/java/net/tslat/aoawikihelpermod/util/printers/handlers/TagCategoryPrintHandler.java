package net.tslat.aoawikihelpermod.util.printers.handlers;

import com.google.common.collect.HashMultimap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;

import java.util.HashMap;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class TagCategoryPrintHandler {
	private final ResourceLocation registryId;
	private final Supplier<Stream<TagKey<?>>> tags;
	private final Function<TagKey<?>, Stream<?>> tagContentsRetriever;

	private HashMultimap<String, TagKey<?>> namespacedTags = null;
	private HashMap<String, String[][]> cachedPrintouts = null;
	private String[] tagNamespaces = null;
	private Function<Object, String> namingFunction = null;

	public TagCategoryPrintHandler(ResourceLocation registryId, Supplier<Stream<TagKey<?>>> tags, Function<TagKey<?>, Stream<?>> tagContentsRetriever) {
		this.registryId = registryId;
		this.tags = tags;
		this.tagContentsRetriever = tagContentsRetriever;
	}

	private void prepTags() {
		if (namespacedTags != null)
			return;

		namespacedTags = HashMultimap.create();
		tags.get().forEachOrdered(tagKey -> namespacedTags.put(tagKey.location().getNamespace(), tagKey));
	}

	public String[] getNameSpaces() {
		if (tagNamespaces != null)
			return tagNamespaces;

		prepTags();

		tagNamespaces = namespacedTags.keySet().toArray(new String[0]);

		return tagNamespaces;
	}

	public String[][] getCategoryPrintout(String namespace) {
		if (cachedPrintouts == null)
			cachedPrintouts = new HashMap<>();

		if (cachedPrintouts.containsKey(namespace))
			return cachedPrintouts.get(namespace);

		prepTags();

		Set<TagKey<?>> tags = namespacedTags.get(namespace);
		String[][] printout = new String[tags.size()][3];
		int index = 0;

		for (TagKey tagKey : tags) {
			String[] contents = new String[3];
			String tagName = tagKey.location().toString();

			contents[0] = registryId + ":" + tagName;
			contents[1] = "<code>" + tagName + "</code>";
			contents[2] = this.tagContentsRetriever.apply(tagKey).map(entry -> {
				if (namingFunction != null)
					return namingFunction.apply(entry);

				namingFunction = ObjectHelper.getNameFunctionForUnknownObject(entry).andThen(name -> FormattingHelper.createLinkableText(name, false, true));

				return namingFunction.apply(entry);
			}).reduce((line, newEntry) -> line += ", " + newEntry).orElse("");
			printout[index++] = contents;
		}

		cachedPrintouts.put(namespace, printout);

		return cachedPrintouts.get(namespace);
	}
}

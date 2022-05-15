package net.tslat.aoawikihelpermod.util.printers.handlers;

import com.google.common.collect.HashMultimap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.tags.ITagManager;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;

import java.util.HashMap;
import java.util.Set;
import java.util.function.Function;

public class TagCategoryPrintHandler<T extends IForgeRegistryEntry<T>> {
	private final ResourceLocation registryId;
	private final ITagManager<T> tagManager;

	private HashMultimap<String, TagKey<T>> namespacedTags = null;
	private HashMap<String, String[][]> cachedPrintouts = null;
	private String[] tagNamespaces = null;
	private Function<IForgeRegistryEntry<?>, String> namingFunction = null;

	public TagCategoryPrintHandler(ResourceLocation registryId, ITagManager<T> tagManager) {
		this.registryId = registryId;
		this.tagManager = tagManager;
	}

	private void prepTags() {
		if (namespacedTags != null)
			return;

		namespacedTags = HashMultimap.create();
		tagManager.getTagNames().forEachOrdered(tagKey -> namespacedTags.put(tagKey.location().getNamespace(), tagKey));
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

		Set<TagKey<T>> tags = namespacedTags.get(namespace);
		String[][] printout = new String[tags.size()][3];
		int index = 0;

		for (TagKey<T> tagKey : tags) {
			String[] contents = new String[3];
			String tagName = tagKey.location().toString();

			contents[0] = registryId + ":" + tagName;
			contents[1] = "<code>" + tagName + "</code>";
			contents[2] = tagManager.getTag(tagKey).stream().map(entry -> {
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
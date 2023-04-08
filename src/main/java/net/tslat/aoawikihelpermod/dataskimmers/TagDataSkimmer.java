package net.tslat.aoawikihelpermod.dataskimmers;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.printer.handler.TagCategoryPrintHandler;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Stream;

public class TagDataSkimmer {
	private static final HashMap<ResourceLocation, TagCategoryPrintHandler> DATA_BY_TAG_CATEGORY = new HashMap<>(4);

	public static void init(MinecraftServer server) {
		ObjectHelper.getAllRegistries().object2BooleanEntrySet().forEach(key -> {
			ResourceLocation id = key.getKey().location();
			ObjectHelper.getRegistry(key.getKey())
					.ifLeft(registry -> {
						if (registry.getTags().findAny().isPresent())
							DATA_BY_TAG_CATEGORY.put(id, new TagCategoryPrintHandler(id, () -> (Stream)registry.getTagNames(), tagKey -> ((HolderSet.Named)registry.getTag((TagKey)tagKey).get()).stream().map(holder -> ((Holder)holder).value())));
					})
					.ifRight(forgeRegistry -> {
						if (forgeRegistry.tags() != null && forgeRegistry.tags().stream().findAny().isPresent())
							DATA_BY_TAG_CATEGORY.put(id, new TagCategoryPrintHandler(id, () -> (Stream)forgeRegistry.tags().getTagNames(), tagKey -> forgeRegistry.tags().getTag((TagKey)tagKey).stream()));
					});
		});
	}

	@Nullable
	public static TagCategoryPrintHandler get(ResourceLocation registryId) {
		return DATA_BY_TAG_CATEGORY.get(registryId);
	}

	public static Set<ResourceLocation> tagTypes() {
		return DATA_BY_TAG_CATEGORY.keySet();
	}
}

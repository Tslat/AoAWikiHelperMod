package net.tslat.aoawikihelpermod.dataskimmers;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import net.minecraftforge.registries.tags.ITagManager;
import net.tslat.aoawikihelpermod.util.printers.handlers.TagCategoryPrintHandler;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Set;

public class TagDataSkimmer {
	private static final HashMap<ResourceLocation, TagCategoryPrintHandler<?>> DATA_BY_TAG_CATEGORY = new HashMap<>(4);

	public static void init() {
		for (ResourceLocation id : RegistryManager.getRegistryNamesForSyncToClient()) {
			ForgeRegistry<?> registry = RegistryManager.ACTIVE.getRegistry(id);

			if (registry == null)
				continue;

			ITagManager<?> tagManager = registry.tags();

			if (tagManager != null)
				DATA_BY_TAG_CATEGORY.put(id, new TagCategoryPrintHandler<>(id, tagManager));
		}
	}

	@Nullable
	public static TagCategoryPrintHandler<?> get(ResourceLocation registryId) {
		return DATA_BY_TAG_CATEGORY.get(registryId);
	}

	public static Set<ResourceLocation> tagTypes() {
		return DATA_BY_TAG_CATEGORY.keySet();
	}
}

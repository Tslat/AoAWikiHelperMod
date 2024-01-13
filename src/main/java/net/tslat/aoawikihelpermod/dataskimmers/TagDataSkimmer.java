package net.tslat.aoawikihelpermod.dataskimmers;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.tslat.aoa3.util.TagUtil;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.printer.handler.TagCategoryPrintHandler;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Set;

public class TagDataSkimmer {
	private static final HashMap<ResourceLocation, TagCategoryPrintHandler> DATA_BY_TAG_CATEGORY = new HashMap<>(4);

	public static void init(MinecraftServer server) {
		ObjectHelper.getAllRegistries().forEach(registryEntry -> {
			final ResourceLocation registryId = registryEntry.key().location();

			DATA_BY_TAG_CATEGORY.put(registryId, new TagCategoryPrintHandler(registryId, () -> registryEntry.value().getTags().map(Pair::getFirst), tagKey -> TagUtil.getTagContents(tagKey, server.overworld()).stream().mapMulti((holderSet, adder) -> holderSet.forEach(holder -> adder.accept(holder.value())))));
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

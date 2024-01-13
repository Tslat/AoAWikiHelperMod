package net.tslat.aoawikihelpermod.dataskimmers;

import com.google.common.base.Suppliers;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.tslat.aoa3.util.WorldUtil;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class StructureTemplateSkimmer extends SimplePreparableReloadListener<Map<ResourceLocation, Supplier<StructureTemplate>>> {
	private static final HashMap<ResourceLocation, Supplier<StructureTemplate>> templates = new HashMap<>();

	@Override
	protected Map<ResourceLocation, Supplier<StructureTemplate>> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
		Map<ResourceLocation, Supplier<StructureTemplate>> collection = new Object2ObjectOpenHashMap<>();
		templates.clear();

		for (ResourceLocation file : resourceManager.listResources("structures", fileName -> fileName.getPath().endsWith(".nbt")).keySet()) {
			String filePath = file.getPath();
			ResourceLocation resourcePath = new ResourceLocation(file.getNamespace(), filePath.substring(11, filePath.length() - 4));

			collection.put(resourcePath, Suppliers.memoize(() -> WorldUtil.getServer().getStructureManager().get(resourcePath).get()));
		}

		return collection;
	}

	@Override
	protected void apply(Map<ResourceLocation, Supplier<StructureTemplate>> collection, ResourceManager resourceManager, ProfilerFiller profiler) {
		templates.putAll(collection);
	}

	public static List<ResourceLocation> getTemplateList() {
		return templates.keySet().stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());
	}

	@Nullable
	public static StructureTemplate getTemplate(ResourceLocation path) {
		if (templates.containsKey(path))
			return templates.get(path).get();

		return null;
	}
}

package net.tslat.aoawikihelpermod.dataskimmers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.common.util.Lazy;
import net.tslat.aoa3.util.WorldUtil;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class StructureTemplateSkimmer extends SimplePreparableReloadListener<HashMap<ResourceLocation, Lazy<StructureTemplate>>> {
	private static final HashMap<ResourceLocation, Lazy<StructureTemplate>> templates = new HashMap<>();

	@Override
	protected HashMap<ResourceLocation, Lazy<StructureTemplate>> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
		HashMap<ResourceLocation, Lazy<StructureTemplate>> collection = new HashMap<>();
		templates.clear();

		for (ResourceLocation file : resourceManager.listResources("structures", fileName -> fileName.endsWith(".nbt"))) {
			String filePath = file.getPath();
			ResourceLocation resourcePath = new ResourceLocation(file.getNamespace(), filePath.substring(11, filePath.length() - 4));

			collection.put(resourcePath, () -> WorldUtil.getServer().getStructureManager().get(resourcePath).get());
		}

		return collection;
	}

	@Override
	protected void apply(HashMap<ResourceLocation, Lazy<StructureTemplate>> collection, ResourceManager resourceManager, ProfilerFiller profiler) {
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

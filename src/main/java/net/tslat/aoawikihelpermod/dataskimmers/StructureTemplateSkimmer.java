package net.tslat.aoawikihelpermod.dataskimmers;

import net.minecraft.client.resources.ReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class StructureTemplateSkimmer extends ReloadListener<HashMap<ResourceLocation, Lazy<Template>>> {
	private static final HashMap<ResourceLocation, Lazy<Template>> templates = new HashMap<>();
	private static final Lazy<MinecraftServer> currentServer = () -> (MinecraftServer)LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);

	@Override
	protected HashMap<ResourceLocation, Lazy<Template>> prepare(IResourceManager resourceManager, IProfiler profiler) {
		HashMap<ResourceLocation, Lazy<Template>> collection = new HashMap<>();
		templates.clear();

		for (ResourceLocation file : resourceManager.listResources("structures", fileName -> fileName.endsWith(".nbt"))) {
			String filePath = file.getPath();
			ResourceLocation resourcePath = new ResourceLocation(file.getNamespace(), filePath.substring(11, filePath.length() - 4));

			collection.put(resourcePath, () -> currentServer.get().getStructureManager().get(resourcePath));
		}

		return collection;
	}

	@Override
	protected void apply(HashMap<ResourceLocation, Lazy<Template>> collection, IResourceManager resourceManager, IProfiler profiler) {
		templates.putAll(collection);
	}

	public static List<ResourceLocation> getTemplateList() {
		return templates.keySet().stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());
	}

	@Nullable
	public static Template getTemplate(ResourceLocation path) {
		if (templates.containsKey(path))
			return templates.get(path).get();

		return null;
	}
}

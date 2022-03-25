package net.tslat.aoawikihelpermod.util.fakeworld;

import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeContainer;

public class FakeBiomeProvider extends BiomeContainer {
	public FakeBiomeProvider(World level) {
		super(level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY), new Biome[] {FakeWorld.PLAINS_BIOME.get()});
	}

	@Override
	public Biome getNoiseBiome(int x, int y, int z) {
		return FakeWorld.PLAINS_BIOME.get();
	}
}

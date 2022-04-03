package net.tslat.aoawikihelpermod.util.fakeworld;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

import java.util.List;

public class FakeBiomeProvider extends BiomeSource {
	public static final Codec<FakeBiomeProvider> CODEC = Biome.CODEC.fieldOf("biome").xmap(FakeBiomeProvider::new, provider -> provider.biome).stable().codec();
	private final Holder<Biome> biome;

	public FakeBiomeProvider(Holder<Biome> biome) {
		super(List.of(FakeWorld.PLAINS_BIOME.get()));

		this.biome = biome;
	}

	@Override
	protected Codec<? extends BiomeSource> codec() {
		return CODEC;
	}

	@Override
	public BiomeSource withSeed(long seed) {
		return this;
	}

	@Override
	public Holder<Biome> getNoiseBiome(int p_204238_, int p_204239_, int p_204240_, Climate.Sampler p_204241_) {
		return this.biome;
	}
}

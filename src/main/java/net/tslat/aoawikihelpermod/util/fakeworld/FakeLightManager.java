package net.tslat.aoawikihelpermod.util.fakeworld;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.IChunkLightProvider;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.lighting.IWorldLightListener;
import net.minecraft.world.lighting.WorldLightManager;

import javax.annotation.Nullable;

public class FakeLightManager extends WorldLightManager {
	public FakeLightManager(IChunkLightProvider chunkProvider) {
		super(chunkProvider, true, true);
	}

	@Override
	public void checkBlock(BlockPos pos) {}

	@Override
	public boolean hasLightWork() {
		return false;
	}

	@Override
	public int runUpdates(int updateCount, boolean updateSkylight, boolean updateBlockLight) {
		return 0;
	}

	@Override
	public void onBlockEmissionIncrease(BlockPos pos, int difference) {}

	@Override
	public void updateSectionStatus(SectionPos pos, boolean isEmpty) {}

	@Override
	public void enableLightSources(ChunkPos chunkPos, boolean enableSkyLight) {}

	@Override
	public void queueSectionData(LightType type, SectionPos pos, @Nullable NibbleArray array, boolean skipUntrusted) {}

	@Override
	public IWorldLightListener getLayerListener(LightType type) {
		return new IWorldLightListener() {
			@Nullable
			@Override
			public NibbleArray getDataLayerData(SectionPos sectionPos) {
				return null;
			}

			@Override
			public int getLightValue(BlockPos pos) {
				return 15;
			}

			@Override
			public void updateSectionStatus(SectionPos pos, boolean isEmpty) {}
		};
	}

	@Override
	public void retainData(ChunkPos pos, boolean retain) {}

	@Override
	public int getRawBrightness(BlockPos pos, int amount) {
		return 15;
	}
}

package net.tslat.aoawikihelpermod.util.fakeworld;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import net.minecraft.world.level.lighting.LevelLightEngine;

import javax.annotation.Nullable;

public class FakeLightManager extends LevelLightEngine {
	public FakeLightManager(LightChunkGetter chunkProvider) {
		super(chunkProvider, true, true);
	}

	@Override
	public void checkBlock(BlockPos pos) {}

	@Override
	public boolean hasLightWork() {
		return false;
	}

	@Override
	public void updateSectionStatus(SectionPos pos, boolean isEmpty) {}

	@Override
	public LayerLightEventListener getLayerListener(LightLayer type) {
		return new LayerLightEventListener() {
			@Nullable
			public DataLayer getDataLayerData(SectionPos pos) {
				return null;
			}

			public int getLightValue(BlockPos pos) {
				return 15;
			}

			public void checkBlock(BlockPos pos) {}

			public void onBlockEmissionIncrease(BlockPos pos, int offset) {}

			public boolean hasLightWork() {
				return false;
			}

			@Override
			public int runLightUpdates() {
				return 0;
			}

			public int runUpdates(int toUpdate, boolean skyLight, boolean blockLight) {
				return toUpdate;
			}

			public void updateSectionStatus(SectionPos pos, boolean pIsEmpty) {}

			@Override
			public void setLightEnabled(ChunkPos pChunkPos, boolean pLightEnabled) {

			}

			@Override
			public void propagateLightSources(ChunkPos pChunkPos) {

			}

			public void enableLightSources(ChunkPos pos, boolean doSkyLight) {}
		};
	}

	@Override
	public void retainData(ChunkPos pos, boolean retain) {}

	@Override
	public int getRawBrightness(BlockPos pos, int amount) {
		return 15;
	}
}

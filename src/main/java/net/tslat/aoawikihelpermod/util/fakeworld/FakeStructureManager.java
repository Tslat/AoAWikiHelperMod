package net.tslat.aoawikihelpermod.util.fakeworld;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.chunk.FeatureAccess;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import javax.annotation.Nullable;
import java.util.List;

public class FakeStructureManager extends StructureFeatureManager {
	public FakeStructureManager() {
		super(null, null, null);
	}

	@Override
	public StructureFeatureManager forWorldGenRegion(WorldGenRegion region) {
		return this;
	}

	@Override
	public List<StructureStart> startsForFeature(SectionPos pos, ConfiguredStructureFeature structure) {
		return List.of();
	}

	@Nullable
	@Override
	public StructureStart getStartForFeature(SectionPos p_207803_, ConfiguredStructureFeature<?, ?> p_207804_, FeatureAccess p_207805_) {
		return null;
	}

	@Override
	public void setStartForFeature(SectionPos sectionPos, ConfiguredStructureFeature structure, StructureStart start, FeatureAccess reader) {}

	@Override
	public void addReferenceForFeature(SectionPos sectionPos, ConfiguredStructureFeature structure, long chunkValue, FeatureAccess reader) {}

	@Override
	public boolean shouldGenerateFeatures() {
		return false;
	}

	@Override
	public StructureStart getStructureAt(BlockPos pos, ConfiguredStructureFeature structure) {
		return StructureStart.INVALID_START;
	}
}

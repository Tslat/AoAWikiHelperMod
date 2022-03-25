package net.tslat.aoawikihelpermod.util.fakeworld;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.IStructureReader;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.structure.StructureStart;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public class FakeStructureManager extends StructureManager {
	public FakeStructureManager() {
		super(null, null);
	}

	@Override
	public StructureManager forWorldGenRegion(WorldGenRegion region) {
		return this;
	}

	@Override
	public Stream<? extends StructureStart<?>> startsForFeature(SectionPos pos, Structure<?> structure) {
		return Stream.empty();
	}

	@Nullable
	@Override
	public StructureStart<?> getStartForFeature(SectionPos sectionPos, Structure<?> structure, IStructureReader reader) {
		return null;
	}

	@Override
	public void setStartForFeature(SectionPos sectionPos, Structure<?> structure, StructureStart<?> start, IStructureReader reader) {}

	@Override
	public void addReferenceForFeature(SectionPos sectionPos, Structure<?> structure, long chunkValue, IStructureReader reader) {}

	@Override
	public boolean shouldGenerateFeatures() {
		return false;
	}

	@Override
	public StructureStart<?> getStructureAt(BlockPos pos, boolean p_235010_2_, Structure<?> structure) {
		return StructureStart.INVALID_START;
	}
}

package net.tslat.aoawikihelpermod.util.fakeworld;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.StructureAccess;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import javax.annotation.Nullable;
import java.util.List;

public class FakeStructureManager extends StructureManager {
	public FakeStructureManager() {
		super(null, null, null);
	}

	@Override
	public FakeStructureManager forWorldGenRegion(WorldGenRegion region) {
		return this;
	}

	@Override
	public List<StructureStart> startsForStructure(SectionPos pos, Structure structure) {
		return List.of();
	}

	@Nullable
	@Override
	public StructureStart getStartForStructure(SectionPos p_207803_, Structure p_207804_, StructureAccess p_207805_) {
		return null;
	}

	@Override
	public void setStartForStructure(SectionPos sectionPos, Structure structure, StructureStart start, StructureAccess reader) {}

	@Override
	public void addReferenceForStructure(SectionPos sectionPos, Structure structure, long chunkValue, StructureAccess reader) {}

	@Override
	public boolean shouldGenerateStructures() {
		return false;
	}

	@Override
	public StructureStart getStructureAt(BlockPos pos, Structure structure) {
		return StructureStart.INVALID_START;
	}
}

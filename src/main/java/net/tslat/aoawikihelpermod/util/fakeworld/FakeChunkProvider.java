package net.tslat.aoawikihelpermod.util.fakeworld;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.lighting.LevelLightEngine;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BooleanSupplier;

public class FakeChunkProvider extends ChunkSource {
	private final HashMap<ChunkPos, FakeChunk> chunkMap;
	private final LevelLightEngine lightManager;

	protected FakeChunkProvider() {
		this.chunkMap = new HashMap<>();
		this.lightManager = new FakeLightManager(this);
	}

	@Nullable
	@Override
	public ChunkAccess getChunk(int chunkX, int chunkZ, ChunkStatus requiredStatus, boolean load) {
		return this.chunkMap.computeIfAbsent(new ChunkPos(chunkX, chunkZ), pos -> new FakeChunk(getLevel(), pos));
	}

	@Override
	public void tick(BooleanSupplier aheadOfSchedule, boolean tickChunks) {}

	@Override
	public String gatherStats() {
		return "";
	}

	@Override
	public int getLoadedChunksCount() {
		return 0;
	}

	@Nonnull
	@Override
	public LevelLightEngine getLightEngine() {
		return this.lightManager;
	}

	@Nonnull
	@Override
	public FakeWorld getLevel() {
		return FakeWorld.INSTANCE.get();
	}

	public void reset() {
		this.chunkMap.clear();
	}

	@Nullable
	public Pair<Vec3i, List<StructureTemplate.StructureBlockInfo>> compileBlocksIntoList() {
		if (chunkMap.isEmpty())
			return null;

		ArrayList<StructureTemplate.StructureBlockInfo> blocks = new ArrayList<>();
		int minX = 0;
		int minY = 0;
		int minZ = 0;
		int maxX = 0;
		int maxY = 0;
		int maxZ = 0;

		for (FakeChunk chunk : this.chunkMap.values()) {
			for (Map.Entry<BlockPos, BlockState> block : chunk.getAllFilledBlocks().entrySet()) {
				BlockPos pos = block.getKey();

				blocks.add(new StructureTemplate.StructureBlockInfo(pos, block.getValue(), null));

				if (!block.getValue().isAir()) {
					minX = Math.min(pos.getX(), minX);
					minY = Math.min(pos.getY(), minY);
					minZ = Math.min(pos.getZ(), minZ);
					maxX = Math.max(pos.getX(), maxX);
					maxY = Math.max(pos.getY(), maxY);
					maxZ = Math.max(pos.getZ(), maxZ);
				}
			}
		}

		blocks.sort(Comparator.<StructureTemplate.StructureBlockInfo>comparingInt(info -> info.pos.getY()).thenComparingInt(info -> info.pos.getZ()).thenComparingInt(info -> info.pos.getX()));

		BlockPos minPos = blocks.get(0).pos;
		BlockPos maxPos = blocks.get(blocks.size() - 1).pos;
		Vec3i totalSize = new Vec3i(Math.abs(maxX - minX) + 1, Math.abs(maxY - minY) + 1, Math.abs(maxZ - minZ) + 1);

		return Pair.of(totalSize, blocks);
	}
}

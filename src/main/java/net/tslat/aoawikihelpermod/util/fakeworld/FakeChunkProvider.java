package net.tslat.aoawikihelpermod.util.fakeworld;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.lighting.WorldLightManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class FakeChunkProvider extends AbstractChunkProvider {
	private final HashMap<ChunkPos, FakeChunk> chunkMap;
	private final WorldLightManager lightManager;

	protected FakeChunkProvider() {
		this.chunkMap = new HashMap<>();
		this.lightManager = new FakeLightManager(this);
	}

	@Nullable
	@Override
	public IChunk getChunk(int chunkX, int chunkZ, ChunkStatus requiredStatus, boolean load) {
		return this.chunkMap.computeIfAbsent(new ChunkPos(chunkX, chunkZ), pos -> new FakeChunk(getLevel(), pos));
	}

	@Override
	public String gatherStats() {
		return "";
	}

	@Nonnull
	@Override
	public WorldLightManager getLightEngine() {
		return this.lightManager;
	}

	@Nonnull
	@Override
	public FakeWorld getLevel() {
		return FakeWorld.INSTANCE;
	}

	public void reset() {
		this.chunkMap.clear();
	}

	@Nullable
	public Pair<Vector3i, List<Template.BlockInfo>> compileBlocksIntoList() {
		if (chunkMap.isEmpty())
			return null;

		ArrayList<Template.BlockInfo> blocks = new ArrayList<>();
		int minX = 0;
		int minY = 0;
		int minZ = 0;
		int maxX = 0;
		int maxY = 0;
		int maxZ = 0;

		for (FakeChunk chunk : this.chunkMap.values()) {
			for (Map.Entry<BlockPos, BlockState> block : chunk.getAllFilledBlocks().entrySet()) {
				BlockPos pos = block.getKey();

				blocks.add(new Template.BlockInfo(pos, block.getValue(), null));

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

		blocks.sort(Comparator.<Template.BlockInfo>comparingInt(info -> info.pos.getY()).thenComparingInt(info -> info.pos.getZ()).thenComparingInt(info -> info.pos.getX()));

		BlockPos minPos = blocks.get(0).pos;
		BlockPos maxPos = blocks.get(blocks.size() - 1).pos;
		Vector3i totalSize = new Vector3i(Math.abs(maxX - minX) + 1, Math.abs(maxY - minY) + 1, Math.abs(maxZ - minZ) + 1);

		return Pair.of(totalSize, blocks);
	}
}

package net.tslat.aoawikihelpermod.util.fakeworld;

import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.palette.UpgradeData;
import net.minecraft.world.ITickList;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureStart;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class FakeChunk implements IChunk {
	private final World level;
	private final ChunkPos pos;

	private final HashMap<BlockPos, BlockState> blocks = new HashMap<BlockPos, BlockState>() {
		@Override
		public BlockState get(Object key) {
			return getOrDefault(key, Blocks.AIR.defaultBlockState());
		}
	};
	private final HashMap<BlockPos, TileEntity> tileEntities = new HashMap<>();
	private final HashMap<Heightmap.Type, Heightmap> heightmaps = new HashMap<>(Heightmap.Type.values().length);

	private final FakeBiomeProvider biomeProvider;

	public FakeChunk(World level, ChunkPos pos) {
		this.level = level;
		this.pos = pos;
		this.biomeProvider = new FakeBiomeProvider(level);
	}

	private int posToInt(BlockPos pos) {
		int x = pos.getX() & 15;
		int y = pos.getY() & 255;
		int z = pos.getZ() & 15;

		return x << 8 | z << 4 | y;
	}

	@Nullable
	@Override
	public TileEntity getBlockEntity(BlockPos pos) {
		return this.tileEntities.get(pos);
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		return this.blocks.get(pos);
	}

	@Override
	public FluidState getFluidState(BlockPos pos) {
		if (!this.blocks.containsKey(pos))
			return Fluids.EMPTY.defaultFluidState();

		return this.blocks.get(pos).getFluidState();
	}

	@Nullable
	@Override
	public BlockState setBlockState(BlockPos pos, BlockState state, boolean isMoving) {
		BlockState existingState = this.blocks.get(pos);

		if (existingState == state)
			return null;

		if (state == Blocks.AIR.defaultBlockState()) {
			this.blocks.remove(pos);
		}
		else {
			this.blocks.put(pos, state);
		}

		for (Heightmap.Type mapType : Heightmap.Type.values()) {
			getOrCreateHeightmapUnprimed(mapType).update(pos.getX() & 15, pos.getY(), pos.getZ() & 15, state);
		}

		//if (!this.level.isClientSide())
		//	state.onPlace(this.level, pos, state, isMoving);

		if (state.hasTileEntity()) {
			TileEntity tileEntity = getBlockEntity(pos);

			if (tileEntity == null) {
				this.level.setBlockEntity(pos, state.createTileEntity(this.level));
			}
			else {
				tileEntity.clearCache();
			}
		}

		return existingState;
	}

	@Override
	public void setBlockEntity(BlockPos pos, TileEntity tileEntity) {
		if (!getBlockState(pos).hasTileEntity())
			return;

		tileEntity.setLevelAndPosition(this.level, pos);
		tileEntity.clearRemoved();

		TileEntity oldTileEntity = this.tileEntities.put(pos.immutable(), tileEntity);

		if (oldTileEntity != null && oldTileEntity != tileEntity)
			oldTileEntity.setRemoved();
	}

	public Map<BlockPos, BlockState> getAllFilledBlocks() {
		return this.blocks;
	}

	@Override
	public void addEntity(Entity entity) {}

	@Override
	public Set<BlockPos> getBlockEntitiesPos() {
		return Sets.newHashSet(this.tileEntities.keySet());
	}

	@Override
	public ChunkSection[] getSections() {
		return new ChunkSection[0];
	}

	@Override
	public Collection<Map.Entry<Heightmap.Type, Heightmap>> getHeightmaps() {
		return Collections.unmodifiableSet(this.heightmaps.entrySet());
	}

	@Override
	public void setHeightmap(Heightmap.Type type, long[] data) {
		getOrCreateHeightmapUnprimed(type).setRawData(data);
	}

	@Override
	public Heightmap getOrCreateHeightmapUnprimed(Heightmap.Type type) {
		return this.heightmaps.computeIfAbsent(type, mapType -> new Heightmap(this, mapType));
	}

	@Override
	public int getHeight(Heightmap.Type heightmapType, int x, int z) {
		return getOrCreateHeightmapUnprimed(heightmapType).getFirstAvailable(x & 15, z & 15) - 1;
	}

	@Override
	public ChunkPos getPos() {
		return this.pos;
	}

	@Override
	public void setLastSaveTime(long time) {}

	@Override
	public Map<Structure<?>, StructureStart<?>> getAllStarts() {
		return Collections.emptyMap();
	}

	@Override
	public void setAllStarts(Map<Structure<?>, StructureStart<?>> structureStarts) {}

	@Nullable
	@Override
	public BiomeContainer getBiomes() {
		return this.biomeProvider;
	}

	@Override
	public void setUnsaved(boolean modified) {}

	@Override
	public boolean isUnsaved() {
		return false;
	}

	@Override
	public ChunkStatus getStatus() {
		return ChunkStatus.FULL;
	}

	@Override
	public void removeBlockEntity(BlockPos pos) {
		TileEntity tileEntity = this.tileEntities.remove(pos);

		if (tileEntity != null)
			tileEntity.setRemoved();
	}

	@Override
	public ShortList[] getPostProcessing() {
		return new ShortList[0];
	}

	@Nullable
	@Override
	public CompoundNBT getBlockEntityNbt(BlockPos pos) {
		return null;
	}

	@Nullable
	@Override
	public CompoundNBT getBlockEntityNbtForSaving(BlockPos pos) {
		return null;
	}

	@Override
	public Stream<BlockPos> getLights() {
		return StreamSupport.stream(BlockPos.betweenClosed(this.pos.getMinBlockX(), 0, this.pos.getMinBlockZ(), this.pos.getMaxBlockX(), 255, this.pos.getMaxBlockZ()).spliterator(), false).filter(pos -> this.getBlockState(pos).getLightValue(this.level, pos) != 0);
	}

	@Override
	public ITickList<Block> getBlockTicks() {
		return this.level.getBlockTicks();
	}

	@Override
	public ITickList<Fluid> getLiquidTicks() {
		return this.level.getLiquidTicks();
	}

	@Override
	public UpgradeData getUpgradeData() {
		return UpgradeData.EMPTY;
	}

	@Override
	public void setInhabitedTime(long newInhabitedTime) {}

	@Override
	public long getInhabitedTime() {
		return 0;
	}

	@Override
	public boolean isLightCorrect() {
		return true;
	}

	@Override
	public void setLightCorrect(boolean lightCorrect) {}

	@Nullable
	@Override
	public StructureStart<?> getStartForFeature(Structure<?> structure) {
		return null;
	}

	@Override
	public void setStartForFeature(Structure<?> structure, StructureStart<?> start) {}

	@Override
	public LongSet getReferencesForFeature(Structure<?> structure) {
		return new LongOpenHashSet(0);
	}

	@Override
	public void addReferenceForFeature(Structure<?> structure, long chunkValue) {}

	@Override
	public Map<Structure<?>, LongSet> getAllReferences() {
		return Collections.emptyMap();
	}

	@Override
	public void setAllReferences(Map<Structure<?>, LongSet> structureReferences) {}
}

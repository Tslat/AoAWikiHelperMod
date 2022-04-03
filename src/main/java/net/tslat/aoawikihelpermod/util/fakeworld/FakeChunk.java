package net.tslat.aoawikihelpermod.util.fakeworld;

import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraft.world.ticks.TickContainerAccess;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class FakeChunk extends ChunkAccess {
	private final Level level;

	private final HashMap<BlockPos, BlockState> blocks = new HashMap<>() {
		@Override
		public BlockState get(Object key) {
			return getOrDefault(key, Blocks.AIR.defaultBlockState());
		}
	};
	private final HashMap<BlockPos, BlockEntity> tileEntities = new HashMap<>();
	private final HashMap<Heightmap.Types, Heightmap> heightmaps = new HashMap<>(Heightmap.Types.values().length);

	private final FakeBiomeProvider biomeProvider;

	public FakeChunk(Level level, ChunkPos pos) {
		super(pos, UpgradeData.EMPTY, level, level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY), 0, null, null);
		this.level = level;
		this.biomeProvider = new FakeBiomeProvider(FakeWorld.PLAINS_BIOME.get());
	}

	private int posToInt(BlockPos pos) {
		int x = pos.getX() & 15;
		int y = pos.getY() & 255;
		int z = pos.getZ() & 15;

		return x << 8 | z << 4 | y;
	}

	@Nullable
	@Override
	public BlockEntity getBlockEntity(BlockPos pos) {
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

		for (Heightmap.Types mapType : Heightmap.Types.values()) {
			getOrCreateHeightmapUnprimed(mapType).update(pos.getX() & 15, pos.getY(), pos.getZ() & 15, state);
		}

		if (state.hasBlockEntity()) {
			BlockEntity tileEntity = getBlockEntity(pos, LevelChunk.EntityCreationType.CHECK);

			if (tileEntity == null) {
				BlockEntity blockEntity = ((EntityBlock)state.getBlock()).newBlockEntity(pos, state);

				if (blockEntity != null)
					addAndRegisterBlockEntity(blockEntity);
			}
			else {
				tileEntity.setBlockState(state);
			}
		}

		return existingState;
	}

	@Nullable
	public BlockEntity getBlockEntity(BlockPos pPos, LevelChunk.EntityCreationType pCreationType) {
		BlockEntity blockEntity = this.blockEntities.get(pPos);

		if (blockEntity != null && blockEntity.isRemoved()) {
			blockEntities.remove(pPos);

			blockEntity = null;
		}

		if (blockEntity == null) {
			CompoundTag nbt = this.pendingBlockEntities.remove(pPos);

			if (nbt != null) {
				BlockEntity blockentity1 = this.promotePendingBlockEntity(pPos, nbt);
				if (blockentity1 != null) {
					return blockentity1;
				}
			}
		}

		if (blockEntity == null) {
			if (pCreationType == LevelChunk.EntityCreationType.IMMEDIATE) {
				blockEntity = this.createBlockEntity(pPos);

				if (blockEntity != null)
					this.addAndRegisterBlockEntity(blockEntity);
			}
		}

		return blockEntity;
	}

	@Nullable
	private BlockEntity promotePendingBlockEntity(BlockPos pPos, CompoundTag pTag) {
		BlockState blockState = this.getBlockState(pPos);
		BlockEntity blockEntity;

		if ("DUMMY".equals(pTag.getString("id"))) {
			if (blockState.hasBlockEntity()) {
				blockEntity = ((EntityBlock)blockState.getBlock()).newBlockEntity(pPos, blockState);
			}
			else {
				blockEntity = null;
			}
		}
		else {
			blockEntity = BlockEntity.loadStatic(pPos, blockState, pTag);
		}

		if (blockEntity != null) {
			blockEntity.setLevel(this.level);
			this.addAndRegisterBlockEntity(blockEntity);
		}

		return blockEntity;
	}

	@Nullable
	private BlockEntity createBlockEntity(BlockPos pos) {
		BlockState blockState = this.getBlockState(pos);

		return !blockState.hasBlockEntity() ? null : ((EntityBlock)blockState.getBlock()).newBlockEntity(pos, blockState);
	}

	public void addAndRegisterBlockEntity(BlockEntity blockEntity) {
		this.setBlockEntity(blockEntity);
	}

	@Override
	public void setBlockEntity(BlockEntity tileEntity) {
		BlockPos pos = tileEntity.getBlockPos();

		if (!getBlockState(pos).hasBlockEntity())
			return;

		tileEntity.setLevel(this.level);
		tileEntity.clearRemoved();

		BlockEntity oldBlockEntity = this.tileEntities.put(pos.immutable(), tileEntity);

		if (oldBlockEntity != null && oldBlockEntity != tileEntity)
			oldBlockEntity.setRemoved();
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
	public LevelChunkSection[] getSections() {
		return new LevelChunkSection[0];
	}

	@Override
	public Collection<Map.Entry<Heightmap.Types, Heightmap>> getHeightmaps() {
		return Collections.unmodifiableSet(this.heightmaps.entrySet());
	}

	@Override
	public void setHeightmap(Heightmap.Types type, long[] data) {
		getOrCreateHeightmapUnprimed(type).setRawData(this, type, data);
	}

	@Override
	public Heightmap getOrCreateHeightmapUnprimed(Heightmap.Types type) {
		return this.heightmaps.computeIfAbsent(type, mapType -> new Heightmap(this, mapType));
	}

	@Override
	public int getHeight(Heightmap.Types heightmapType, int x, int z) {
		return getOrCreateHeightmapUnprimed(heightmapType).getFirstAvailable(x & 15, z & 15) - 1;
	}

	@Override
	public ChunkPos getPos() {
		return this.chunkPos;
	}

	@Override
	public Map<ConfiguredStructureFeature<?, ?>, StructureStart> getAllStarts() {
		return Collections.emptyMap();
	}

	@Override
	public void setAllStarts(Map<ConfiguredStructureFeature<?, ?>, StructureStart> structureStarts) {}

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
		BlockEntity tileEntity = this.tileEntities.remove(pos);

		if (tileEntity != null)
			tileEntity.setRemoved();
	}

	@Override
	public ShortList[] getPostProcessing() {
		return new ShortList[0];
	}

	@Nullable
	@Override
	public CompoundTag getBlockEntityNbt(BlockPos pos) {
		return null;
	}

	@Nullable
	@Override
	public CompoundTag getBlockEntityNbtForSaving(BlockPos pos) {
		return null;
	}

	@Override
	public Stream<BlockPos> getLights() {
		return StreamSupport.stream(BlockPos.betweenClosed(this.chunkPos.getMinBlockX(), 0, this.chunkPos.getMinBlockZ(), this.chunkPos.getMaxBlockX(), 255, this.chunkPos.getMaxBlockZ()).spliterator(), false).filter(pos -> this.getBlockState(pos).getLightEmission(this.level, pos) != 0);
	}

	@Override
	public TickContainerAccess<Block> getBlockTicks() {
		return new TickContainerAccess<>() {
			@Override
			public void schedule(ScheduledTick<Block> p_193428_) {
			}

			@Override
			public boolean hasScheduledTick(BlockPos p_193429_, Block p_193430_) {
				return false;
			}

			@Override
			public int count() {
				return 0;
			}
		};
	}

	@Override
	public TickContainerAccess<Fluid> getFluidTicks() {
		return new TickContainerAccess<>() {
			@Override
			public void schedule(ScheduledTick<Fluid> p_193428_) {
			}

			@Override
			public boolean hasScheduledTick(BlockPos p_193429_, Fluid p_193430_) {
				return false;
			}

			@Override
			public int count() {
				return 0;
			}
		};
	}

	@Override
	public TicksToSave getTicksForSerialization() {
		return new ChunkAccess.TicksToSave((time, blockMapper) -> new ListTag(), (time, blockMapper) -> new ListTag());
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
	public StructureStart getStartForFeature(ConfiguredStructureFeature<?, ?> structure) {
		return null;
	}

	@Override
	public void setStartForFeature(ConfiguredStructureFeature<?, ?> structure, StructureStart start) {}

	@Override
	public LongSet getReferencesForFeature(ConfiguredStructureFeature<?, ?> structure) {
		return new LongOpenHashSet(0);
	}

	@Override
	public void addReferenceForFeature(ConfiguredStructureFeature<?, ?> structure, long chunkValue) {}

	@Override
	public Map<ConfiguredStructureFeature<?, ?>, LongSet> getAllReferences() {
		return Collections.emptyMap();
	}

	@Override
	public void setAllReferences(Map<ConfiguredStructureFeature<?, ?>, LongSet> structureReferences) {}
}

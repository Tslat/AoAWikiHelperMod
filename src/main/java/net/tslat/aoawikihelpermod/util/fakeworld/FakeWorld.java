package net.tslat.aoawikihelpermod.util.fakeworld;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.profiler.EmptyProfiler;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.tags.ITagCollectionSupplier;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.aoa3.common.registration.AoADimensions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Stream;

public class FakeWorld extends World implements ISeedReader {
	public static final FakeWorld INSTANCE = new FakeWorld();

	private static final Scoreboard scoreboard = new Scoreboard();
	private static final RecipeManager recipeManager = new RecipeManager();
	private static final FakeChunkProvider chunkProvider = new FakeChunkProvider();
	private static final FakeStructureManager structureManager = new FakeStructureManager();

	public static final Lazy<Biome> PLAINS_BIOME = Lazy.of(() -> ForgeRegistries.BIOMES.getValue(Biomes.PLAINS.location()));

	public static void init() {}

	protected FakeWorld() {
		super(new FakeSpawnInfo(), World.OVERWORLD, AoADimensions.OVERWORLD.getWorld().dimensionType(), () -> EmptyProfiler.INSTANCE, true, false, 0);
	}

	@Override
	public void sendBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, int flags) {}

	@Override
	public void playSound(@Nullable PlayerEntity player, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch) {}

	@Override
	public void playSound(@Nullable PlayerEntity player, Entity entity, SoundEvent event, SoundCategory category, float volume, float pitch) {}

	@Nullable
	@Override
	public Entity getEntity(int id) {
		return null;
	}

	@Nullable
	@Override
	public MapData getMapData(String mapName) {
		return null;
	}

	@Override
	public void setMapData(MapData mapData) {}

	@Override
	public int getFreeMapId() {
		return 0;
	}

	@Override
	public void destroyBlockProgress(int breakerId, BlockPos pos, int progress) {}

	@Nonnull
	@Override
	public Scoreboard getScoreboard() {
		return scoreboard;
	}

	@Override
	public RecipeManager getRecipeManager() {
		return recipeManager;
	}

	@Nonnull
	@Override
	public ITagCollectionSupplier getTagManager() {
		return ITagCollectionSupplier.EMPTY;
	}

	@Override
	public ITickList<Block> getBlockTicks() {
		return EmptyTickList.empty();
	}

	@Override
	public ITickList<Fluid> getLiquidTicks() {
		return EmptyTickList.empty();
	}

	@Nonnull
	@Override
	public FakeChunkProvider getChunkSource() {
		return chunkProvider;
	}

	public FakeStructureManager getStructureManager() {
		return structureManager;
	}

	@Override
	public void levelEvent(@Nullable PlayerEntity player, int type, BlockPos pos, int data) {}

	@Nonnull
	@Override
	public DynamicRegistries registryAccess() {
		return Minecraft.getInstance().level != null ? Minecraft.getInstance().level.registryAccess() : DynamicRegistries.builtin();
	}

	@Override
	public float getShade(Direction side, boolean doSideShading) {
		boolean constantAmbientLight = false;

		if (!doSideShading)
			return constantAmbientLight ? 0.9f : 1f;

		switch(side) {
			case DOWN:
				return constantAmbientLight ? 0.9f : 0.5f;
			case UP:
				return constantAmbientLight ? 0.9f : 1f;
			case NORTH:
			case SOUTH:
				return 0.8f;
			case WEST:
			case EAST:
				return 0.6f;
			default:
				return 1f;
		}
	}

	@Override
	public Biome getBiome(BlockPos pos) {
		return PLAINS_BIOME.get();
	}

	@Nonnull
	@Override
	public List<? extends PlayerEntity> players() {
		if (Minecraft.getInstance() != null && Minecraft.getInstance().level != null)
			return ImmutableList.of(Minecraft.getInstance().player);

		return ImmutableList.of();
	}

	@Nonnull
	@Override
	public Biome getUncachedNoiseBiome(int x, int y, int z) {
		return registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getOrThrow(Biomes.PLAINS);
	}

	@Override
	public DifficultyInstance getCurrentDifficultyAt(BlockPos pos) {
		return new DifficultyInstance(Difficulty.PEACEFUL, FakeWorld.INSTANCE.getGameTime(), 0, 0);
	}

	@Override
	public void blockEntityChanged(BlockPos p_175646_1_, TileEntity p_175646_2_) {}

	@Override
	public FluidState getFluidState(BlockPos pos) {
		if (isOutsideBuildHeight(pos))
			return Fluids.EMPTY.defaultFluidState();

		return getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL).getFluidState(pos);
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		if (isOutsideBuildHeight(pos))
			return Blocks.VOID_AIR.defaultBlockState();

		return ((FakeChunk)getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, true)).getBlockState(pos);
	}

	@Override
	public void setBlockEntity(BlockPos pos, @Nullable TileEntity blockEntity) {
		if (isOutsideBuildHeight(pos))
			return;

		if (blockEntity == null || blockEntity.isRemoved())
			return;

		FakeChunk chunk = (FakeChunk)getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL);

		chunk.setBlockEntity(pos, blockEntity);
		addBlockEntity(blockEntity);
	}

	@Override
	public boolean addBlockEntity(TileEntity blockEntity) {
		return true;
	}

	@Override
	public boolean setBlock(BlockPos pos, BlockState state, int flags, int recursionLeft) {
		if (isOutsideBuildHeight(pos))
			return false;

		FakeChunk chunk = (FakeChunk)getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, true);
		Block block = state.getBlock();
		BlockState oldState = getBlockState(pos);
		int oldLight = oldState.getLightValue(this, pos);
		int oldOpacity = oldState.getLightBlock(this, pos);
		BlockState replacedState = chunk.setBlockState(pos, state, (flags & 64) != 0);

		if (replacedState == null)
			return false;

		BlockState newState = getBlockState(pos);

		if ((flags & 128) == 0 && newState != oldState && (newState.getLightBlock(this, pos) != oldOpacity || newState.getLightValue(this, pos) != oldLight || newState.useShapeForLightOcclusion() || oldState.useShapeForLightOcclusion()))
			getChunkSource().getLightEngine().checkBlock(pos);

		return true;
	}

	@Nullable
	@Override
	public TileEntity getBlockEntity(BlockPos pos) {
		if (isOutsideBuildHeight(pos))
			return null;

		return getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, true).getBlockEntity(pos);
	}

	@Override
	public int getHeight(Heightmap.Type heightmapType, int x, int z) {
		if (x >= -30000000 && z >= -30000000 && x < 30000000 && z < 30000000) {
			if (hasChunk(x >> 4, z >> 4))
				return getChunk(x >> 4, z >> 4, ChunkStatus.FULL, true).getHeight(heightmapType, x & 15, z & 15) + 1;

			return 0;
		}

		return 1;
	}

	public void reset() {
		this.getChunkSource().reset();
	}

	@Override
	public long getSeed() {
		return 0;
	}

	@Override
	public Stream<? extends StructureStart<?>> startsForFeature(SectionPos pos, Structure<?> structure) {
		return Stream.empty();
	}

	@Override
	public ServerWorld getLevel() {
		return AoADimensions.OVERWORLD.getWorld();
	}
}

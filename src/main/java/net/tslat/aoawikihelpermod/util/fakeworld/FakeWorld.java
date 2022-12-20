package net.tslat.aoawikihelpermod.util.fakeworld;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.BlackholeTickAccess;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.tslat.aoa3.common.registration.worldgen.AoADimensions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class FakeWorld extends Level implements WorldGenLevel {
	public static final Lazy<FakeWorld> INSTANCE = FakeWorld::new;

	private static final Scoreboard scoreboard = new Scoreboard();
	private static final RecipeManager recipeManager = new RecipeManager();
	private static final FakeChunkProvider chunkProvider = new FakeChunkProvider();
	private static final FakeStructureManager structureManager = new FakeStructureManager();

	public static final Lazy<Holder<Biome>> PLAINS_BIOME = Lazy.of(() -> new Holder.Direct<>(ForgeRegistries.BIOMES.getValue(Biomes.PLAINS.location())));

	public static void init() {}

	protected FakeWorld() {
		super(new FakeSpawnInfo(), Level.OVERWORLD, AoADimensions.OVERWORLD.getWorld().dimensionTypeRegistration(), () -> InactiveProfiler.INSTANCE, true, false, 0, 0);
	}

	@Override
	public void sendBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, int flags) {}

	@Override
	public void playSound(@Nullable Player player, double x, double y, double z, SoundEvent sound, SoundSource category, float volume, float pitch) {}

	@Override
	public void playSound(@Nullable Player player, Entity entity, SoundEvent event, SoundSource category, float volume, float pitch) {}

	@Override
	public String gatherChunkSourceStats() {
		return "Chunks[C] W: " + chunkProvider.gatherStats() + " E: " + 0;
	}

	@Nullable
	@Override
	public Entity getEntity(int id) {
		return null;
	}

	@Nullable
	@Override
	public MapItemSavedData getMapData(String mapName) {
		return null;
	}

	@Override
	public void setMapData(String mapId, MapItemSavedData data) {}

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

	@Override
	protected LevelEntityGetter<Entity> getEntities() {
		return new LevelEntityGetter<>() {
			@Nullable
			@Override
			public Entity get(int id) {
				return null;
			}

			@Nullable
			@Override
			public Entity get(UUID uuid) {
				return null;
			}

			@Override
			public Iterable<Entity> getAll() {
				return ImmutableList.of();
			}

			@Override
			public <U extends Entity> void get(EntityTypeTest<Entity, U> test, AbortableIterationConsumer<U> consumer) {}

			@Override
			public void get(AABB boundingBox, Consumer<Entity> consumer) {}

			@Override
			public <U extends Entity> void get(EntityTypeTest<Entity, U> test, AABB bounds, AbortableIterationConsumer<U> consumer) {}
		};
	}

	@Override
	public LevelTickAccess<Block> getBlockTicks() {
		return  BlackholeTickAccess.emptyLevelList();
	}

	@Override
	public LevelTickAccess<Fluid> getFluidTicks() {
		return BlackholeTickAccess.emptyLevelList();
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
	public void levelEvent(@Nullable Player player, int type, BlockPos pos, int data) {}

	@Override
	public void gameEvent(GameEvent event, Vec3 position, GameEvent.Context context) {}

	@Override
	public void gameEvent(@Nullable Entity entity, GameEvent event, BlockPos pos) {}

	@Nonnull
	@Override
	public RegistryAccess registryAccess() {
		return Minecraft.getInstance().level != null ? Minecraft.getInstance().level.registryAccess() : ServerLifecycleHooks.getCurrentServer().registryAccess();
	}

	@Override
	public FeatureFlagSet enabledFeatures() {
		return Minecraft.getInstance().level.enabledFeatures();
	}

	@Override
	public float getShade(Direction side, boolean doSideShading) {
		boolean constantAmbientLight = false;

		if (!doSideShading)
			return constantAmbientLight ? 0.9f : 1f;

		return switch (side) {
			case DOWN -> constantAmbientLight ? 0.9f : 0.5f;
			case UP -> constantAmbientLight ? 0.9f : 1f;
			case NORTH, SOUTH -> 0.8f;
			case WEST, EAST -> 0.6f;
			default -> 1f;
		};
	}

	@Override
	public Holder<Biome> getBiome(BlockPos pos) {
		return PLAINS_BIOME.get();
	}

	@Nonnull
	@Override
	public List<? extends Player> players() {
		if (Minecraft.getInstance() != null && Minecraft.getInstance().level != null)
			return ImmutableList.of(Minecraft.getInstance().player);

		return ImmutableList.of();
	}

	@Nonnull
	@Override
	public Holder<Biome> getUncachedNoiseBiome(int x, int y, int z) {
		return Holder.direct(registryAccess().registryOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS));
	}

	@Override
	public DifficultyInstance getCurrentDifficultyAt(BlockPos pos) {
		return new DifficultyInstance(Difficulty.PEACEFUL, FakeWorld.INSTANCE.get().getGameTime(), 0, 0);
	}

	@Override
	public void blockEntityChanged(BlockPos pos) {}

	@Override
	public FluidState getFluidState(BlockPos pos) {
		if (isOutsideBuildHeight(pos))
			return Fluids.EMPTY.defaultFluidState();

		return getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL).getFluidState(pos);
	}

	@Override
	public void playSeededSound(@org.jetbrains.annotations.Nullable Player player, double x, double y, double z, Holder<SoundEvent> sound, SoundSource source, float volume, float pitch, long seed) {}

	@Override
	public void playSeededSound(@Nullable Player player, double posX, double posY, double posZ, SoundEvent sound, SoundSource source, float volume, float pitch, long seed) {}

	@Override
	public void playSeededSound(@org.jetbrains.annotations.Nullable Player player, Entity entity, Holder<SoundEvent> sound, SoundSource source, float volume, float pitch, long seed) {}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		if (isOutsideBuildHeight(pos))
			return Blocks.VOID_AIR.defaultBlockState();

		return ((FakeChunk)getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, true)).getBlockState(pos);
	}

	@Override
	public void setBlockEntity(BlockEntity blockEntity) {
		BlockPos pos = blockEntity.getBlockPos();

		if (blockEntity.isRemoved())
			return;

		if (isOutsideBuildHeight(pos))
			return;

		this.getChunkAt(pos).addAndRegisterBlockEntity(blockEntity);
	}

	@Override
	public boolean addFreshEntity(Entity entity) {
		return false;
	}

	@Override
	public boolean setBlock(BlockPos pos, BlockState state, int flags, int recursionLeft) {
		if (isOutsideBuildHeight(pos))
			return false;

		FakeChunk chunk = (FakeChunk)getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, true);
		Block block = state.getBlock();
		BlockState oldState = getBlockState(pos);
		int oldLight = oldState.getLightEmission(this, pos);
		int oldOpacity = oldState.getLightBlock(this, pos);
		BlockState replacedState = chunk.setBlockState(pos, state, (flags & 64) != 0);

		if (replacedState == null)
			return false;

		BlockState newState = getBlockState(pos);

		if ((flags & 128) == 0 && newState != oldState && (newState.getLightBlock(this, pos) != oldOpacity || newState.getLightEmission(this, pos) != oldLight || newState.useShapeForLightOcclusion() || oldState.useShapeForLightOcclusion()))
			getChunkSource().getLightEngine().checkBlock(pos);

		return true;
	}

	@Nullable
	@Override
	public BlockEntity getBlockEntity(BlockPos pos) {
		if (isOutsideBuildHeight(pos))
			return null;

		return getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, true).getBlockEntity(pos);
	}

	@Override
	public int getHeight(Heightmap.Types heightmapType, int x, int z) {
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
	public ServerLevel getLevel() {
		return AoADimensions.OVERWORLD.getWorld();
	}
}

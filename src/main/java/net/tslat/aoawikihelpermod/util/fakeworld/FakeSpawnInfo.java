package net.tslat.aoawikihelpermod.util.fakeworld;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.storage.WritableLevelData;

import javax.annotation.Nonnull;

public class FakeSpawnInfo implements WritableLevelData {
	private static final GameRules gameRules = new GameRules();

	@Override
	public BlockPos getSpawnPos() {
		return null;
	}

	@Override
	public float getSpawnAngle() {
		return 0;
	}

	@Override
	public long getGameTime() {
		return 0;
	}

	@Override
	public long getDayTime() {
		return 0;
	}

	@Override
	public boolean isThundering() {
		return false;
	}

	@Override
	public boolean isRaining() {
		return false;
	}

	@Override
	public void setRaining(boolean isRaining) {}

	@Override
	public boolean isHardcore() {
		return false;
	}

	@Nonnull
	@Override
	public GameRules getGameRules() {
		return gameRules;
	}

	@Nonnull
	@Override
	public Difficulty getDifficulty() {
		return Difficulty.EASY;
	}

	@Override
	public boolean isDifficultyLocked() {
		return false;
	}

	@Override
	public void setSpawn(BlockPos spawnPoint, float spawnAngle) {

	}
}

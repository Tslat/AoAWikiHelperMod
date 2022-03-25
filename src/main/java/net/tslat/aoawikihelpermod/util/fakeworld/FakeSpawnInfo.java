package net.tslat.aoawikihelpermod.util.fakeworld;

import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.storage.ISpawnWorldInfo;

import javax.annotation.Nonnull;

public class FakeSpawnInfo implements ISpawnWorldInfo {
	private static final GameRules gameRules = new GameRules();

	@Override
	public void setXSpawn(int x) {}

	@Override
	public void setYSpawn(int y) {}

	@Override
	public void setZSpawn(int z) {}

	@Override
	public void setSpawnAngle(float angle) {}

	@Override
	public int getXSpawn() {
		return 0;
	}

	@Override
	public int getYSpawn() {
		return 0;
	}

	@Override
	public int getZSpawn() {
		return 0;
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
}

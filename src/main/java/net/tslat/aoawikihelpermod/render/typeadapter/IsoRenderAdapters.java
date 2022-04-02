package net.tslat.aoawikihelpermod.render.typeadapter;

import net.minecraft.block.*;
import net.minecraft.entity.passive.fish.AbstractFishEntity;
import net.minecraft.entity.passive.fish.PufferfishEntity;
import net.minecraft.util.math.vector.Vector3f;
import net.tslat.aoa3.content.entity.base.AbstractLavaFishEntity;
import net.tslat.aoa3.content.entity.projectile.gun.BaseBullet;
import net.tslat.aoawikihelpermod.render.IsometricPrinterScreen;
import net.tslat.aoawikihelpermod.render.typeadapter.block.DoublePlantBlockRenderAdapter;

public final class IsoRenderAdapters {
	public static void init() {
		IsometricPrinterScreen.registerEntityAdapter(new SimpleRotationRenderAdapter<>(entity ->
				entity instanceof AbstractFishEntity ||
				entity instanceof AbstractLavaFishEntity,
				Vector3f.ZP, 90f));
		IsometricPrinterScreen.registerBlockAdapter(new SimpleRotationRenderAdapter<>(block ->
				block.getBlock() instanceof SaplingBlock ||
				block.getBlock() instanceof WebBlock ||
				block.getBlock() instanceof SugarCaneBlock ||
				block.getBlock() instanceof FlowerBlock ||
				block.getBlock() instanceof TallGrassBlock,
				Vector3f.YP, -45f));
		IsometricPrinterScreen.registerBlockAdapter(new SimpleRotationRenderAdapter<>(block ->
				block.getBlock() instanceof BedBlock,
				Vector3f.YP, -180f));
		IsometricPrinterScreen.registerBlockAdapter(new DoublePlantBlockRenderAdapter());
		IsometricPrinterScreen.registerEntityAdapter(new SimpleScaleRenderAdapter<>(entity ->
				entity instanceof BaseBullet,
				5f));
		IsometricPrinterScreen.registerEntityAdapter(new SimpleScaleAndRotateRenderAdapter<>(entity ->
				entity instanceof PufferfishEntity,
				5f,
				Vector3f.ZP,
				-90f));
	}
}

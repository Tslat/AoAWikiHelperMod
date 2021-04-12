package net.tslat.aoawikihelpermod.util;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ObjectHelper {
	public static List<Item> scrapeRegistryForItems(Predicate<Item> filter) {
		return ForgeRegistries.ITEMS.getValues().stream().filter(filter).collect(Collectors.toList());
	}

	public static List<Block> scrapeRegistryForBlocks(Predicate<Block> filter) {
		return ForgeRegistries.BLOCKS.getValues().stream().filter(filter).collect(Collectors.toList());
	}

	public static List<EntityType<?>> scrapeRegistryForEntities(Predicate<EntityType<?>> filter) {
		return ForgeRegistries.ENTITIES.getValues().stream().filter(filter).collect(Collectors.toList());
	}
}

package net.tslat.aoawikihelpermod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.tslat.aoawikihelpermod.recipes.RecipeWriter;

@Mod.EventBusSubscriber
@SideOnly(Side.CLIENT)
public class KeyBindings {
	public static KeyBinding keyUsagesPrintout;
	public static KeyBinding keyRecipePrintout;

	public static void init() {
		ClientRegistry.registerKeyBinding(keyUsagesPrintout = new KeyBinding("Print Recipe Usages", 209, "AoAWikiHelper"));
		ClientRegistry.registerKeyBinding(keyRecipePrintout = new KeyBinding("Print Item Recipe", 201, "AoAWikiHelper"));
	}

	@SubscribeEvent
	public static void onKeyDown(final InputEvent.KeyInputEvent ev) {
		if (keyUsagesPrintout.isPressed())
			RecipeWriter.printItemRecipeUsages(Minecraft.getMinecraft().player.getHeldItemMainhand());

		if (keyRecipePrintout.isPressed())
			RecipeWriter.printItemRecipes(Minecraft.getMinecraft().player.getHeldItemMainhand());
	}
}

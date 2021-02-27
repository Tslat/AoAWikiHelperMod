package net.tslat.aoawikihelpermod;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Map;
import java.util.UUID;

public class AttributeHandler {
    public static final UUID ATTACK_SPEED_MAINHAND = UUID.fromString("99fdc256-279e-4c8e-b1c6-9209571f134e");

    public static double getStackAttributeValue(ItemStack stack, IAttribute baseAttribute, PlayerEntity player, EquipmentSlotType equipmentSlot, UUID attributeUUID) {
        for (Map.Entry<String, AttributeModifier> entry : stack.getItem().getAttributeModifiers(equipmentSlot, stack).entries()) {
            AttributeModifier mod = entry.getValue();

            if (mod.getID().equals(attributeUUID)) {
                double value = mod.getAmount();

                if (mod.getID().equals(Item.ATTACK_SPEED_MODIFIER))
                    value += player.getAttribute(baseAttribute).getBaseValue();

                return mod.getOperation() != AttributeModifier.Operation.MULTIPLY_BASE && mod.getOperation() != AttributeModifier.Operation.MULTIPLY_TOTAL ? value : value * 100;
            }
        }

        return 0;
    }
}

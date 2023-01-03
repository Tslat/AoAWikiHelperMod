package net.tslat.aoawikihelpermod.util.printers.infoboxes;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.aoa3.util.NumberUtil;
import net.tslat.aoa3.util.StringUtil;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.fakeworld.FakeWorld;
import net.tslat.aoawikihelpermod.util.printers.PrintHelper;

import javax.annotation.Nullable;
import java.io.IOException;

public class EntityInfoboxPrintHelper extends PrintHelper {
	private static final String HEAD = "{{EntityInfo";
	private static final String END = "}}";


	protected EntityInfoboxPrintHelper(String fileName) throws IOException {
		super(fileName);
	}

	@Nullable
	public static EntityInfoboxPrintHelper open(String fileName) {
		try {
			return new EntityInfoboxPrintHelper(fileName);
		} catch (IOException ex) {
			return null;
		}
	}

	private String getEntitySize(EntityType<?> entity){
		return "'''Width''': =" + entity.getWidth() + " blocks <br> '''Height''': " + entity.getHeight() + " blocks";
	}

	private String getHealth(LivingEntity entity){
		return NumberUtil.roundToNthDecimalPlace(entity.getMaxHealth(), 2);
	}

	private String getDamage(LivingEntity entity){
		double damage = entity.getAttribute(Attributes.ATTACK_DAMAGE).getValue();
		if(damage > 0){
			return NumberUtil.roundToNthDecimalPlace((float)damage, 2);
		}
		return "";
	}

	private String getHostility(LivingEntity entity){
		if(entity instanceof Monster){
			return "Aggressive";
		}
		return "Passive";
	}

	private String getXP(LivingEntity entity){
		return "" + entity.getExperienceReward();
	}

	private <T extends Number> String noStringIfZero(T input) {
		if (input.equals(0)) return "";
		return "" + input;
	}

	private void writeIfExists(String name, String value) {
		if (value.length() == 0) return;
		write(name + value);
	}

	public void printEntityInfobox(EntityType<?> entity, LivingEntity player) {
		String displayName = ObjectHelper.getEntityName(entity);
		LivingEntity createdEntity = (LivingEntity) entity.create(FakeWorld.INSTANCE.get());

		write(HEAD);
		write("|name=" + displayName);
		write("|image=" + displayName + ".png");
		write("|noimage=");
		write("|image2=");
		writeIfExists("|health=", getHealth(createdEntity));
		write("|specialhealth=");
		writeIfExists("|size=", getEntitySize(entity));
		writeIfExists("|damage=", getDamage(createdEntity));
		writeIfExists("|specialdamage=", "");
		writeIfExists("|armor=", "" + createdEntity.getArmorValue());
		writeIfExists("|armortoughness=", ""+ createdEntity.getAbsorptionAmount());
		writeIfExists("|environment=", "");
		writeIfExists("|hostility=", getHostility(createdEntity));
		writeIfExists("|classification=", StringUtil.toTitleCase(entity.getCategory().getName()));
		writeIfExists("|xp=", getXP(createdEntity));
		writeIfExists("|knockbackresist=", "");
		write("|id=" + ForgeRegistries.ENTITY_TYPES.getKey(entity));
		write("|versionadded=");
		write(END);
	}
}

package am2.buffs;

import java.util.UUID;

import am2.defs.PotionEffectsDefs;
import am2.utils.EntityUtils;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class BuffEffectCharmed extends BuffEffect{


	public static final int CHARM_TO_PLAYER = 1;
	public static final int CHARM_TO_MONSTER = 2;

	private EntityLivingBase charmer;
	
	public BuffEffectCharmed(int duration, int amplifier){
		super(PotionEffectsDefs.charme, duration, amplifier);
	}

	public void setCharmer(EntityLivingBase entity){
		charmer = entity;
	}

	@Override
	public void applyEffect(EntityLivingBase entityliving) {
		if (getAmplifier() == CHARM_TO_PLAYER && entityliving instanceof EntityCreature && charmer instanceof EntityPlayer){
			EntityUtils.makeSummon_PlayerFaction((EntityCreature)entityliving, (EntityPlayer)charmer, true);
		}else if (getAmplifier() == CHARM_TO_MONSTER && entityliving instanceof EntityCreature){
			EntityUtils.makeSummon_MonsterFaction((EntityCreature)entityliving, true);
		}
		EntityUtils.setOwner(entityliving, charmer);
		EntityUtils.setSummonDuration(entityliving, -1);
	}

	@Override
	public void stopEffect(EntityLivingBase entityliving){
		if (entityliving instanceof EntityCreature){
			EntityUtils.revertAI((EntityCreature)entityliving);
		}
	}
	
	@Override
	public void writeCustomNBT(NBTTagCompound nbt) {
		nbt.setString("Charmer", charmer.getUniqueID().toString());
	}
	
	@Override
	public void readCustomNBT(NBTTagCompound nbt) {
		try {
			charmer = (EntityLivingBase) FMLCommonHandler.instance().getMinecraftServerInstance().worlds[0].getEntityFromUuid(UUID.fromString(nbt.getString("Charmer")));
		} catch (Throwable t) {
		}
	}
	
	@Override
	protected String spellBuffName(){
		return "Charmed";
	}

}

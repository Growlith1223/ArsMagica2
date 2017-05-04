package am2.buffs;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import am2.defs.PotionEffectsDefs;

public class BuffEffectFlight extends BuffEffect{

	public BuffEffectFlight(int duration, int amplifier){
		super(PotionEffectsDefs.flight, duration, amplifier);
	}

	@Override
	public void applyEffect(EntityLivingBase entityliving){

	}

	@Override
	public void performEffect(EntityLivingBase entityliving){
		if (entityliving instanceof EntityPlayerMP){
			EntityPlayer player = (EntityPlayer)entityliving;
			player.capabilities.allowFlying = true;
			player.sendPlayerAbilities();		
		} 
	}

	@Override
	public void stopEffect(EntityLivingBase entityliving){
		if (entityliving instanceof EntityPlayerMP){
			EntityPlayer player = (EntityPlayer)entityliving;
			if (!player.capabilities.isCreativeMode){
				player.capabilities.allowFlying = false;
				player.capabilities.isFlying = false;
				player.fallDistance = 0f;
				player.sendPlayerAbilities();
			}
		}
	}

	@Override
	protected String spellBuffName(){
		return "Flight";
	}

}

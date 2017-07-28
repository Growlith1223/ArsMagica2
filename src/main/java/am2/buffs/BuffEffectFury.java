package am2.buffs;

import am2.defs.PotionEffectsDefs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;

public class BuffEffectFury extends BuffEffect{

	public BuffEffectFury(int duration, int amplifier){
		super(PotionEffectsDefs.fury, duration, amplifier);
	}

	@Override
	public void applyEffect(EntityLivingBase entityliving){
	}

	@Override
	public void stopEffect(EntityLivingBase entityliving){
		if (!entityliving.world.isRemote){
			entityliving.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 200, 1));
			entityliving.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 200, 1));
		}
	}

	@Override
	public void combine(PotionEffect potioneffect){
	}

	@Override
	protected String spellBuffName(){
		return "Fury";
	}

}

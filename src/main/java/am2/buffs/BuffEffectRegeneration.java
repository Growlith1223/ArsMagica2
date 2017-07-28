package am2.buffs;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import am2.defs.PotionEffectsDefs;

public class BuffEffectRegeneration extends BuffEffect{

	public BuffEffectRegeneration(int duration, int amplifier){
		super(PotionEffectsDefs.regeneration, duration, amplifier);
	}

	@Override
	public void applyEffect(EntityLivingBase entityliving){
	}

	@Override
	public void stopEffect(EntityLivingBase entityliving){

	}

	public boolean onUpdate(EntityLivingBase entityliving){

		World world = entityliving.world;
		double ticks = 80 / Math.pow(2, this.getAmplifier());

		if (getDuration() != 0 && (getDuration() % ticks) == 0){
			if (!world.isRemote){
				entityliving.heal(1);
			}
		}

		return super.onUpdate(entityliving);
	}

	@Override
	protected String spellBuffName(){
		return null;
	}

}

package am2.bosses.ai;

import am2.bosses.BossActions;
import am2.bosses.EntityEnderGuardian;
import am2.bosses.IArsMagicaBoss;
import am2.utils.NPCSpells;
import am2.utils.SpellUtils;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.SoundCategory;
import thehippomaster.AnimationAPI.AIAnimation;
import thehippomaster.AnimationAPI.IAnimatedEntity;

public class EntityAIEnderbolt extends AIAnimation{
	private int cooldownTicks = 0;

	public EntityAIEnderbolt(IAnimatedEntity entity){
		super(entity);
	}

	@Override
	public int getAnimID(){
		return BossActions.STRIKE.ordinal();
	}

	@Override
	public boolean isAutomatic(){
		return false;
	}

	@Override
	public int getDuration(){
		return 30;
	}

	@Override
	public boolean shouldAnimate(){
		//accessor method in AIAnimation that gives access to the entity
		EntityLiving living = getEntity();

		//must have an attack target
		if (living.getAttackTarget() == null) return false;

		return cooldownTicks-- <= 0;
	}

	@Override
	public void resetTask(){
		cooldownTicks = 20;
	}

	@Override
	public void updateTask(){
		EntityEnderGuardian guardian = getEntity();
		if (guardian.getAttackTarget() != null){
			guardian.getLookHelper().setLookPositionWithEntity(guardian.getAttackTarget(), 30, 30);
			if (guardian.getTicksInCurrentAction() == 7){
				guardian.faceEntity(guardian.getAttackTarget(), 180, 180);
				guardian.world.playSound(guardian.posX, guardian.posY, guardian.posZ, ((IArsMagicaBoss)guardian).getAttackSound(), SoundCategory.HOSTILE, 1.0f, 1.0f, false);
				SpellUtils.applyStackStage(NPCSpells.instance.enderGuardian_enderBolt, guardian, guardian, guardian.posX, guardian.posY + 0.5f, guardian.posZ, null, guardian.world, false, false, 0);
			}else{
				guardian.faceEntity(guardian.getAttackTarget(), 180, 180);
			}
		}
	}
}

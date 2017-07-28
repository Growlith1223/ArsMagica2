package am2.bosses.ai;

import am2.bosses.BossActions;
import am2.bosses.IArsMagicaBoss;
import am2.utils.NPCSpells;
import am2.utils.SpellUtils;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.SoundCategory;

public class EntityAIDispel extends EntityAIBase{
	private final EntityLiving host;
	private int cooldownTicks = 0;
	private boolean hasCasted = false;
	private int boltTicks = 0;

	public EntityAIDispel(IArsMagicaBoss host){
		this.host = (EntityLiving)host;
		this.setMutexBits(3);
	}

	@Override
	public boolean shouldExecute(){
		cooldownTicks--;
		boolean execute = ((IArsMagicaBoss)host).getCurrentAction() == BossActions.IDLE && host.getActivePotionEffects().size() > 0 && cooldownTicks <= 0;
		if (execute) hasCasted = false;
		return execute;
	}

	@Override
	public boolean continueExecuting(){
		return !hasCasted && host.getAttackTarget() != null && !host.getAttackTarget().isDead;
	}

	@Override
	public void resetTask(){
		((IArsMagicaBoss)host).setCurrentAction(BossActions.IDLE);
		cooldownTicks = 50;
		hasCasted = true;
		boltTicks = 0;
	}

	@Override
	public void updateTask(){
		if (((IArsMagicaBoss)host).getCurrentAction() != BossActions.CASTING)
			((IArsMagicaBoss)host).setCurrentAction(BossActions.CASTING);

		boltTicks++;
		if (boltTicks == 16){
			if (!host.world.isRemote)
				host.world.playSound(host.posX, host.posY, host.posZ, ((IArsMagicaBoss)host).getAttackSound(), SoundCategory.HOSTILE, 1.0f, 1.0f, false);
			SpellUtils.applyStackStage(NPCSpells.instance.dispel, host, host, host.posX, host.posY, host.posZ, null, host.world, false, false, 0);
		}
		if (boltTicks >= 23){
			resetTask();
		}
	}
}

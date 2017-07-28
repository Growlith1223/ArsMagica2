package am2.bosses.ai;

import am2.bosses.BossActions;
import am2.bosses.IArsMagicaBoss;
import am2.utils.SpellUtils;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;

public class EntityAICastSpell<T extends EntityLiving & IArsMagicaBoss> extends EntityAIBase{

	private final T host;
	private int cooldownTicks = 0;
	private boolean hasCasted = false;
	private int castTicks = 0;

	private ItemStack stack;
	private int castPoint;
	private int duration;
	private int cooldown;
	private BossActions activeAction;
	private ISpellCastCallback<T> callback;

	public EntityAICastSpell(T host, ItemStack spell, int castPoint, int duration, int cooldown, BossActions activeAction){
		this.host = (T)host;
		this.stack = spell;
		this.castPoint = castPoint;
		this.duration = duration;
		this.cooldown = cooldown;
		this.activeAction = activeAction;
		this.callback = null;
		this.setMutexBits(3);
	}

	public EntityAICastSpell(T host, ItemStack spell, int castPoint, int duration, int cooldown, BossActions activeAction, ISpellCastCallback<T> callback){
		this.host = (T)host;
		this.stack = spell;
		this.castPoint = castPoint;
		this.duration = duration;
		this.cooldown = cooldown;
		this.activeAction = activeAction;
		this.callback = callback;
	}

	@Override
	public boolean shouldExecute(){
		cooldownTicks--;
		boolean execute = host.getCurrentAction() == BossActions.IDLE && host.getAttackTarget() != null && cooldownTicks <= 0;
		if (execute){
			if (callback == null || callback.shouldCast(host, stack))
				hasCasted = false;
			else
				execute = false;
		}
		return execute;
	}

	@Override
	public boolean continueExecuting(){
		return !hasCasted && host.getAttackTarget() != null && !host.getAttackTarget().isDead;
	}

	@Override
	public void resetTask(){
		((IArsMagicaBoss)host).setCurrentAction(BossActions.IDLE);
		cooldownTicks = cooldown;
		hasCasted = true;
		castTicks = 0;
	}

	@Override
	public void updateTask(){
		host.getLookHelper().setLookPositionWithEntity(host.getAttackTarget(), 30, 30);
		if (host.getDistanceSqToEntity(host.getAttackTarget()) > 64){

			double deltaZ = host.getAttackTarget().posZ - host.posZ;
			double deltaX = host.getAttackTarget().posX - host.posX;

			double angle = -Math.atan2(deltaZ, deltaX);

			double newX = host.getAttackTarget().posX + (Math.cos(angle) * 6);
			double newZ = host.getAttackTarget().posZ + (Math.sin(angle) * 6);

			host.getNavigator().tryMoveToXYZ(newX, host.getAttackTarget().posY, newZ, 0.5f);
		}else if (!host.canEntityBeSeen(host.getAttackTarget())){
			host.getNavigator().tryMoveToEntityLiving(host.getAttackTarget(), 0.5f);
		}else{
			if (((IArsMagicaBoss)host).getCurrentAction() != activeAction)
				((IArsMagicaBoss)host).setCurrentAction(activeAction);

			castTicks++;
			if (castTicks == castPoint){
				if (!host.world.isRemote)
					host.world.playSound(host.posX, host.posY, host.posZ, ((IArsMagicaBoss)host).getAttackSound(), SoundCategory.HOSTILE, 1.0f, 1.0f, false);
				host.faceEntity(host.getAttackTarget(), 180, 180);
				SpellUtils.applyStackStage(stack, host, host.getAttackTarget(), host.posX, host.posY, host.posZ, null, host.world, false, false, 0);
			}
		}
		if (castTicks >= duration){
			resetTask();
		}
	}
}

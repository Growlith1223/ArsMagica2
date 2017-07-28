package am2.bosses.ai;

import java.lang.reflect.Constructor;

import am2.bosses.BossActions;
import am2.bosses.EntityLifeGuardian;
import am2.bosses.IArsMagicaBoss;
import am2.buffs.BuffEffectMagicShield;
import am2.buffs.BuffEffectShrink;
import am2.defs.AMSounds;
import am2.utils.EntityUtils;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

public class EntityAISummonAllies extends EntityAIBase{
	private final EntityLifeGuardian host;
	private int cooldownTicks = 0;
	private boolean hasCasted = false;
	private int actionTicks = 0;
	private Class<? extends EntityCreature>[] mobs;

	@SafeVarargs
	public EntityAISummonAllies(EntityLifeGuardian host, Class<? extends EntityCreature>... summons){
		this.host = host;
		this.setMutexBits(1);
		mobs = summons;
	}

	@Override
	public boolean shouldExecute(){
		cooldownTicks--;
		boolean execute = ((IArsMagicaBoss)host).getCurrentAction() != BossActions.CASTING && cooldownTicks <= 0;
		if (execute) hasCasted = false;
		return execute;
	}

	@Override
	public boolean continueExecuting(){
		return !hasCasted;
	}

	@Override
	public void resetTask(){
		((IArsMagicaBoss)host).setCurrentAction(BossActions.IDLE);
		cooldownTicks = 200;
		hasCasted = true;
		actionTicks = 0;
	}

	@Override
	public void updateTask(){
		if (((IArsMagicaBoss)host).getCurrentAction() != BossActions.CASTING)
			((IArsMagicaBoss)host).setCurrentAction(BossActions.CASTING);

		actionTicks++;
		if (actionTicks == 16){
			if (!host.world.isRemote)
				host.world.playSound(host.posX, host.posY, host.posZ, AMSounds.LIFE_GUARDIAN_SUMMON, SoundCategory.HOSTILE, 1.0f, host.getRNG().nextFloat() * 0.5f + 0.5f, false);
			int numAllies = 3;
			for (int i = 0; i < numAllies; ++i){
				Class<? extends EntityCreature> summon = mobs[host.world.rand.nextInt(mobs.length)];
				try{
					Constructor<? extends EntityCreature> ctor = summon.getConstructor(World.class);
					EntityCreature mob = (EntityCreature)ctor.newInstance(host.world);
					mob.setPosition(host.posX + host.world.rand.nextDouble() * 2 - 1, host.posY, host.posZ + host.world.rand.nextDouble() * 2 - 1);
					mob.addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("speed"), 99999, 1));
					mob.addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("strength"), 99999, 1));
					mob.addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("regeneration"), 99999, 1));
					mob.addPotionEffect(new BuffEffectMagicShield(99999, 1));
					if (host.getHealth() < host.getMaxHealth() / 2){
						mob.addPotionEffect(new BuffEffectShrink(99999, 1));
					}
					EntityUtils.makeSummon_MonsterFaction(mob, false);
					EntityUtils.setOwner(mob, host);
					EntityUtils.setSummonDuration(mob, 1800);
					host.world.spawnEntity(mob);

					host.queued_minions.add(mob);
				}catch (Throwable e){
					e.printStackTrace();
					return;
				}
			}
		}
		if (actionTicks >= 23){
			resetTask();
		}
	}

}

package am2.bosses.ai;

import java.util.ArrayList;

import com.google.common.collect.Lists;

import am2.api.ArsMagicaAPI;
import am2.api.spell.AbstractSpellPart;
import am2.bosses.BossActions;
import am2.bosses.EntityWaterGuardian;
import am2.utils.SpellUtils;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class EntityAIChaosWaterBolt extends EntityAIBase{
	private final EntityWaterGuardian host;
	private static final ItemStack castStack = createDummyStack();
	private static AbstractSpellPart WateryGrave() {return ArsMagicaAPI.getSpellRegistry().getObject(new ResourceLocation("arsmagica2", "watery_grave"));}
	private static AbstractSpellPart Projectile() {return ArsMagicaAPI.getSpellRegistry().getObject(new ResourceLocation("arsmagica2", "projectile"));}
	private static AbstractSpellPart MagicDamage() {return ArsMagicaAPI.getSpellRegistry().getObject(new ResourceLocation("arsmagica2", "magic_damage"));}
	private static AbstractSpellPart Knockback() {return ArsMagicaAPI.getSpellRegistry().getObject(new ResourceLocation("arsmagica2", "knockback"));}

	private static ItemStack createDummyStack(){
		ItemStack stack = SpellUtils.createSpellStack(new ArrayList<>(), Lists.newArrayList(Projectile(), WateryGrave(), MagicDamage(), Knockback()), new NBTTagCompound());
		return stack;
	}

	public EntityAIChaosWaterBolt(EntityWaterGuardian host){
		this.host = host;
		this.setMutexBits(1);
	}

	@Override
	public boolean shouldExecute(){
		if (host.getCurrentAction() == BossActions.IDLE && host.isActionValid(BossActions.CASTING)) return true;
		return false;
	}

	@Override
	public boolean continueExecuting(){
		if (host.getCurrentAction() == BossActions.CASTING && host.getTicksInCurrentAction() > 100){
			host.setCurrentAction(BossActions.IDLE);
			return false;
		}
		return true;
	}

	@Override
	public void updateTask(){
		if (host.getCurrentAction() != BossActions.CASTING)
			host.setCurrentAction(BossActions.CASTING);

		if (!host.world.isRemote && host.getCurrentAction() == BossActions.CASTING){
			float yaw = host.world.rand.nextFloat() * 360;
			host.rotationYaw = yaw;
			host.prevRotationYaw = yaw;
			SpellUtils.applyStackStage(castStack, host, host, host.posX, host.posY, host.posZ, null, host.world, false, false, 0);
		}
	}
}

package am2.affinity.abilities;

import am2.api.affinity.AbstractAffinityAbility;
import am2.api.affinity.Affinity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class AbilityFirePunch extends AbstractAffinityAbility {

	public AbilityFirePunch() {
		super(new ResourceLocation("arsmagica2", "firepunch"));
	}

	@Override
	public float getMinimumDepth() {
		return 0.8f;
	}

	@Override
	public Affinity getAffinity() {
		return Affinity.FIRE;
	}
	
	@Override
	public void applyHurt(EntityPlayer player, LivingHurtEvent event, boolean isAttacker) {
		if (isAttacker && !player.world.isRemote && player.getHeldItemMainhand()== null) {
			event.getEntityLiving().setFire(4);
			event.setAmount(event.getAmount() + 3);
		}
	}
}

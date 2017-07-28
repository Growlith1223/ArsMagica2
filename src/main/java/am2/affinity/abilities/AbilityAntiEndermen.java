package am2.affinity.abilities;

import am2.api.affinity.AbstractAffinityAbility;
import am2.api.affinity.Affinity;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class AbilityAntiEndermen extends AbstractAffinityAbility {

	public AbilityAntiEndermen() {
		super(new ResourceLocation("arsmagica2", "antiendermen"));
	}

	@Override
	public float getMinimumDepth() {
		return 0.9f;
	}

	@Override
	public Affinity getAffinity() {
		return Affinity.WATER;
	}
	
	@Override
	public void applyHurt(EntityPlayer player, LivingHurtEvent event, boolean isAttacker) {
		if (!isAttacker && event.getSource().getSourceOfDamage() instanceof EntityEnderman){
			event.getSource().getSourceOfDamage().attackEntityFrom(DamageSource.DROWN, 2);
		}
	}

}

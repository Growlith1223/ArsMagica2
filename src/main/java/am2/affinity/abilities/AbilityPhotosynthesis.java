package am2.affinity.abilities;

import am2.api.affinity.AbstractAffinityAbility;
import am2.api.affinity.Affinity;
import am2.extensions.AffinityData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class AbilityPhotosynthesis extends AbstractAffinityAbility {

	public AbilityPhotosynthesis() {
		super(new ResourceLocation("arsmagica2", "photosynthesis"));
	}

	@Override
	public float getMinimumDepth() {
		return 1f;
	}

	@Override
	public Affinity getAffinity() {
		return Affinity.NATURE;
	}
	
	@Override
	public void applyTick(EntityPlayer player) {
		AffinityData affinityData = AffinityData.For(player);
		if (player.world.isRemote) return;
		
		if (player.world.canBlockSeeSky(player.getPosition()) && player.world.isDaytime()){
			affinityData.accumulatedHungerRegen += 0.02f;
			if (affinityData.accumulatedHungerRegen > 1.0f){
				((EntityPlayer)player).getFoodStats().addStats(1, 0.025f);
				affinityData.accumulatedHungerRegen -= 1;
			}
		}else{
			((EntityPlayer)player).addExhaustion(0.025f);
		}
	}

}

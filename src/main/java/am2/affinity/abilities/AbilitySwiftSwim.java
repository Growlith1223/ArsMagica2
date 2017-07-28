package am2.affinity.abilities;

import am2.api.affinity.AbstractAffinityAbility;
import am2.api.affinity.Affinity;
import am2.buffs.BuffEffectSwiftSwim;
import am2.defs.PotionEffectsDefs;
import am2.extensions.AffinityData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class AbilitySwiftSwim extends AbstractAffinityAbility {

	public AbilitySwiftSwim() {
		super(new ResourceLocation("arsmagica2", "swiftswim"));
	}

	@Override
	public float getMinimumDepth() {
		return 0.5f;
	}

	@Override
	public Affinity getAffinity() {
		return Affinity.WATER;
	}

	@Override
	public void applyTick(EntityPlayer player) {
		if (player.isInWater()) {
			if (!player.world.isRemote && (!player.isPotionActive(PotionEffectsDefs.swiftSwim) || player.getActivePotionEffect(PotionEffectsDefs.swiftSwim).getDuration() < 10)){
				player.addPotionEffect(new BuffEffectSwiftSwim(100, AffinityData.For(player).getAffinityDepth(getAffinity()) > 0.75f ? 1 : 0));
			}
		}
	}

}

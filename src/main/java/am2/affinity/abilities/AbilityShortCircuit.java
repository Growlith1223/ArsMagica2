package am2.affinity.abilities;

import am2.ArsMagica2;
import am2.api.affinity.AbstractAffinityAbility;
import am2.api.affinity.Affinity;
import am2.extensions.EntityExtension;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class AbilityShortCircuit extends AbstractAffinityAbility {

	public AbilityShortCircuit() {
		super(new ResourceLocation("arsmagica2", "shortcircuit"));
	}

	@Override
	public float getMinimumDepth() {
		return 0.25f;
	}
	@Override
	public Affinity getAffinity() {
		return Affinity.LIGHTNING;
	}

	@Override
	public void applyTick(EntityPlayer player) {
		if (player.isWet() && !player.world.isRemote){
			if (player.getRNG().nextFloat() < 0.04f) {
				EntityExtension.For(player).deductMana(100);
				if (player.world.isRemote){
					ArsMagica2.proxy.particleManager.BoltFromEntityToPoint(player.world, player, player.posX - 2 + player.getRNG().nextDouble() * 4, player.posY + player.getEyeHeight() - 2 + player.getRNG().nextDouble() * 4, player.posZ - 2 + player.getRNG().nextDouble() * 4);
				}
			}
//			else{
//				if (player.getRNG().nextDouble() < 0.4f)
//					player.world.playSoundAtEntity(player, "arsmagica2:misc.event.mana_shield_block", 1.0f, player.world.rand.nextFloat() + 0.5f);
//			}
		}
	}
}

package am2.affinity.abilities;

import am2.api.affinity.AbstractAffinityAbility;
import am2.api.affinity.Affinity;
import am2.api.event.SpellCastEvent.Pre;
import am2.buffs.BuffEffectClarity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class AbilityClearCaster extends AbstractAffinityAbility {

	public AbilityClearCaster() {
		super(new ResourceLocation("arsmagica2", "clearcaster"));
	}

	@Override
	public float getMinimumDepth() {
		return 0.4f;
	}

	@Override
	public Affinity getAffinity() {
		return Affinity.ARCANE;
	}
	
	@Override
	public void applyPreSpellCast(EntityPlayer player, Pre event) {
		if (event.entityLiving.world.rand.nextInt(100) < 5 && !event.entityLiving.world.isRemote){
			event.entityLiving.addPotionEffect(new BuffEffectClarity(140, 0));
		}
	}
}

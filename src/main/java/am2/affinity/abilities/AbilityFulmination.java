package am2.affinity.abilities;

import java.util.List;

import am2.ArsMagica2;
import am2.api.affinity.AbstractAffinityAbility;
import am2.api.affinity.Affinity;
import am2.extensions.AffinityData;
import net.minecraft.block.BlockTNT;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class AbilityFulmination extends AbstractAffinityAbility {

	public AbilityFulmination() {
		super(new ResourceLocation("arsmagica2", "fulmination"));
	}

	@Override
	public float getMinimumDepth() {
		return 0.5f;
	}
	
	@Override
	public float getMaximumDepth() {
		return 0.95F;
	}

	@Override
	public Affinity getAffinity() {
		return Affinity.LIGHTNING;
	}

	@Override
	public void applyTick(EntityPlayer player) {
		applyFulmintion(player, AffinityData.For(player).getAffinityDepth(getAffinity()));
	}
	
	@SuppressWarnings("unchecked")
	private void applyFulmintion(EntityPlayer ent, double lightningDepth){
		//chance to light nearby TNT
		if (!ent.world.isRemote){
			if (lightningDepth <= 0.8f){
				BlockPos offsetPos = new BlockPos(ent.posX - 5 + ent.getRNG().nextInt(11), ent.posY - 5 + ent.getRNG().nextInt(11), ent.posZ - 5 + ent.getRNG().nextInt(11));
				IBlockState block = ent.world.getBlockState(offsetPos);
				if (block.getBlock() == Blocks.TNT){
					ent.world.setBlockToAir(offsetPos);
					((BlockTNT)Blocks.TNT).explode(ent.world, offsetPos, block.withProperty(BlockTNT.EXPLODE, true), ent);
				}
			}
			//chance to supercharge nearby creepers
			if (lightningDepth >= 0.7f && ent.getRNG().nextDouble() < 0.05f){
				List<EntityCreeper> creepers = ent.world.getEntitiesWithinAABB(EntityCreeper.class, ent.getEntityBoundingBox().expand(5, 5, 5));
				for (EntityCreeper creeper : creepers){
					try {
						creeper.getDataManager().set((DataParameter<Boolean>)ReflectionHelper.findField(EntityCreeper.class, "POWERED", "field_184714_b").get(creeper), true);
					} catch (IllegalArgumentException | IllegalAccessException e) {
					}
					ArsMagica2.proxy.particleManager.BoltFromEntityToEntity(ent.world, ent, ent, creeper, 0, 1, -1);
				}
			}
		}
	}
}

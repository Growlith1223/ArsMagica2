package am2.blocks.tileentity;

import java.util.List;

import com.google.common.collect.Lists;

import am2.ArsMagica2;
import am2.api.DamageSources;
import am2.api.IMultiblockStructureController;
import am2.api.blocks.MultiblockGroup;
import am2.api.blocks.MultiblockStructureDefinition;
import am2.blocks.BlockArsMagicaBlock;
import am2.blocks.BlockArsMagicaBlock.EnumBlockType;
import am2.defs.BlockDefs;
import am2.extensions.EntityExtension;
import am2.particles.AMParticle;
import am2.particles.ParticleFloatUpward;
import am2.power.PowerNodeRegistry;
import am2.power.PowerTypes;
import net.minecraft.block.BlockQuartz;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class TileEntityManaDrain extends TileEntityAMPower implements IMultiblockStructureController {
	
	private MultiblockStructureDefinition multiblock = new MultiblockStructureDefinition("mana_drain");
	
	public TileEntityManaDrain() {
		super(10000);
		setupMultiblock();
	}

	private void setupMultiblock() {
		MultiblockGroup walls = new MultiblockGroup("walls", Lists.newArrayList(BlockDefs.magicWall.getDefaultState()), false);
		walls.addBlock(new BlockPos(-1, 0, -1));
		walls.addBlock(new BlockPos(-1, 0, 0));
		walls.addBlock(new BlockPos(-1, 0, 1));
		walls.addBlock(new BlockPos(0, 0, -1));
		walls.addBlock(new BlockPos(0, 0, 1));
		walls.addBlock(new BlockPos(1, 0, -1));
		walls.addBlock(new BlockPos(1, 0, 0));
		walls.addBlock(new BlockPos(1, 0, 1));
		
		MultiblockGroup pillars = new MultiblockGroup("pillars", Lists.newArrayList(Blocks.QUARTZ_BLOCK.getDefaultState().withProperty(BlockQuartz.VARIANT, BlockQuartz.EnumType.LINES_Y)), false);
		pillars.addBlock(new BlockPos (-2, 1, 0));
		pillars.addBlock(new BlockPos (2, 1, 0));
		pillars.addBlock(new BlockPos (0, 1, -2));
		pillars.addBlock(new BlockPos (0, 1, 2));
		pillars.addBlock(new BlockPos (-2, 2, 0));
		pillars.addBlock(new BlockPos (2, 2, 0));
		pillars.addBlock(new BlockPos (0, 2, -2));
		pillars.addBlock(new BlockPos (0, 2, 2));
		
		MultiblockGroup caps = new MultiblockGroup("caps", Lists.newArrayList(BlockDefs.blocks.getDefaultState().withProperty(BlockArsMagicaBlock.BLOCK_TYPE, EnumBlockType.BLUETOPAZ)), false);
		caps.addBlock(new BlockPos (-2, 3, 0));
		caps.addBlock(new BlockPos (2, 3, 0));
		caps.addBlock(new BlockPos (0, 3, -2));
		caps.addBlock(new BlockPos (0, 3, 2));
		
		MultiblockGroup drain = new MultiblockGroup("drain", Lists.newArrayList(BlockDefs.manaDrain.getDefaultState()), true);
		
		drain.addBlock(BlockPos.ORIGIN);
		
		multiblock.addGroup(walls);
		multiblock.addGroup(pillars);
		multiblock.addGroup(caps);
		multiblock.addGroup(drain);
	}

	@Override
	public boolean canRelayPower(PowerTypes type) {
		return false;
	}

	@Override
	public int getChargeRate() {
		return world.isBlockIndirectlyGettingPowered(pos) == 0 ? 0 : 500;
	}
	
	@Override
	public void update() {
		super.update();
		if (!isMultiblockComplete()) return;
		if (PowerNodeRegistry.For(world).getPower(this, PowerTypes.NEUTRAL) == capacity) return;
		List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(pos.add(-1, 1, -1), pos.add(2, 4, 2)));
		boolean isWorking = false;
		for (EntityLivingBase entity : entities) {
			EntityExtension ext = EntityExtension.For(entity);
			if (ext == null) continue;
			if (world.isBlockIndirectlyGettingPowered(pos) == 0) {
				isWorking = true;
				if (!world.isRemote) {
					float drain = ext.getMaxMana() / 100f;
					if (drain < 1)
						drain = 1;
					if (ext.getCurrentMana() >= drain) {
						PowerNodeRegistry.For(world).setPower(this, PowerTypes.NEUTRAL, PowerNodeRegistry.For(world).getPower(this, PowerTypes.NEUTRAL) + drain * 10);
						ext.setCurrentMana(ext.getCurrentMana() - drain);
					}else if (entity.ticksExisted % 20 == 0) {
						entity.attackEntityFrom(DamageSources.causeHolyDamage(null), 1);
						PowerNodeRegistry.For(world).setPower(this, PowerTypes.NEUTRAL, PowerNodeRegistry.For(world).getPower(this, PowerTypes.NEUTRAL) + 40);
					}
				}
			} else {
				if (!(entity instanceof EntityPlayer)) continue;
				isWorking = true;
				if (!world.isRemote) {
					float toConsume = PowerNodeRegistry.For(world).getPower(this, PowerTypes.NEUTRAL) / 10;
					if (toConsume > 10)
						toConsume = 10;
					if (toConsume + ext.getCurrentMana() > ext.getMaxMana())
						toConsume = ext.getMaxMana() - ext.getCurrentMana();
					ext.setCurrentMana(ext.getCurrentMana() + toConsume);
					PowerNodeRegistry.For(world).consumePower(this, PowerTypes.NEUTRAL, toConsume * 10);
				}
			}
		}
		if (world.isRemote && isWorking) {
			AMParticle particle = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, "sparkle2", pos.getX() - 1 + world.rand.nextDouble() * 3, pos.getY() + 2, pos.getZ() - 1 + world.rand.nextDouble() * 3);
			if (particle != null) {
				particle.setRGBColorI(0x00AAFF);
				particle.AddParticleController(new ParticleFloatUpward(particle, 0.1f, world.isBlockIndirectlyGettingPowered(pos) == 0 ? -0.05f : 0.05f, 1, false));
			}
		}
	}
	
	private boolean isMultiblockComplete() {
		return multiblock.matches(world, pos);
	}
	
	@Override
	public boolean canProvidePower(PowerTypes type) {
		return world.isBlockIndirectlyGettingPowered(pos) == 0 && type == PowerTypes.NEUTRAL;
	}
	
	@Override
	public List<PowerTypes> getValidPowerTypes() {
		return Lists.newArrayList(PowerTypes.NEUTRAL);
	}

	@Override
	public MultiblockStructureDefinition getDefinition() {
		return multiblock;
	}
}

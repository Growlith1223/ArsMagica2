package am2.armor;

import java.util.List;

import am2.defs.ItemDefs;
import am2.proxy.gui.ModelLibrary;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.PotionTypes;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SuppressWarnings("deprecation")
public class ItemWaterGuardianOrbs extends AMArmor{

	public ItemWaterGuardianOrbs(ArmorMaterial inheritFrom, ArsMagicaArmorMaterial enumarmormaterial, int par3, EntityEquipmentSlot par4){
		super(inheritFrom, enumarmormaterial, par3, par4);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot,
			ModelBiped _default){
		return ModelLibrary.instance.waterOrbs;
	}

	@Override
	public int getArmorDisplay(EntityPlayer player, ItemStack armor, int slot){
		return 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List<String> par3List, boolean par4){
		par3List.add(I18n.translateToLocal("am2.tooltip.water_orbs"));
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type){
		return "arsmagica2:textures/mobs/bosses/water_guardian.png";
	}

	@SideOnly(Side.CLIENT)
	public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, NonNullList<ItemStack> par3List){
		par3List.add(ItemDefs.waterOrbsEnchanted.copy());
	}

	@Override
	public void onArmorTick(World world, EntityPlayer plr, ItemStack stack) {
		super.onArmorTick(world, plr, stack);
		if (!plr.getActivePotionEffects().contains(PotionTypes.WATER_BREATHING))
			plr.addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation(new ResourceLocation("water_breathing").toString())));

		if (plr.isInWater());
			reverseMaterialAcceleration(world, plr);
	}

	public void reverseMaterialAcceleration(World world, EntityPlayer entityIn)
	{
		AxisAlignedBB bb = entityIn.getEntityBoundingBox();

		int minX = MathHelper.floor(bb.minX);
		int maxX = MathHelper.ceil(bb.maxX);
		int minY = MathHelper.floor(bb.minY);
		int maxY = MathHelper.ceil(bb.maxY);
		int minZ = MathHelper.floor(bb.minZ);
		int maxZ = MathHelper.ceil(bb.maxZ);

		Vec3d vec3d = Vec3d.ZERO;
		BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();

		for (int k1 = minX; k1 < maxX; ++k1)
			for (int l1 = minY; l1 < maxY; ++l1)
				for (int i2 = minZ; i2 < maxZ; ++i2)
				{
					blockpos$pooledmutableblockpos.setPos(k1, l1, i2);
					IBlockState iblockstate = world.getBlockState(blockpos$pooledmutableblockpos);
					Block block = iblockstate.getBlock();

					Boolean result = block.isEntityInsideMaterial(world, blockpos$pooledmutableblockpos, iblockstate, entityIn, (double)maxY, Material.WATER, false);
					if (result != null && result == true)
					{
						vec3d = block.modifyAcceleration(world, blockpos$pooledmutableblockpos, entityIn, vec3d);
						continue;
					}
					else if (result != null && result == false) continue;
					if (iblockstate.getMaterial() == Material.WATER)
					{
						double d0 = (double)((float)(l1 + 1) - BlockLiquid.getLiquidHeightPercent(((Integer)iblockstate.getValue(BlockLiquid.LEVEL)).intValue()));
						if ((double)maxY >= d0)
							vec3d = block.modifyAcceleration(world, blockpos$pooledmutableblockpos, entityIn, vec3d);
					}
				}
		blockpos$pooledmutableblockpos.release();
		if (vec3d.lengthVector() > 0.0D && entityIn.isPushedByWater())
		{
			vec3d = vec3d.normalize();
			double d1 = 0.014D;
			entityIn.motionX -= vec3d.xCoord * d1;
			entityIn.motionY -= vec3d.yCoord * d1;
			entityIn.motionZ -= vec3d.zCoord * d1;
		}

	}
}

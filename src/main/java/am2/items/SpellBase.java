package am2.items;

import java.util.List;

import am2.ArsMagica2;
import am2.api.spell.SpellModifiers;
import am2.api.spell.SpellShape;
import am2.defs.IDDefs;
import am2.extensions.EntityExtension;
import am2.extensions.SkillData;
import am2.utils.EntityUtils;
import am2.utils.SpellUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class SpellBase extends ItemSpellBase{
	public SpellBase(){
		super();
		this.setMaxDamage(0);
	}
	
	@Override
	public SpellBase registerAndName(String name) {
		return (SpellBase) super.registerAndName(name);
	}
	
	@Override
	public EnumAction getItemUseAction(ItemStack par1ItemStack){
		return EnumAction.NONE;
	}

	@Override
	public boolean getShareTag(){
		return true;
	}

	@Override
	public String getItemStackDisplayName(ItemStack par1ItemStack){
		if (par1ItemStack.getTagCompound() == null) return "\247bMalformed Spell";
		return "Unnamed Spell";
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean par4){
		if (!stack.hasTagCompound()) return;
		
		float manaCost = SpellUtils.getManaCost(stack, player);
		manaCost *= 1F + (float)((float)EntityExtension.For(player).getCurrentBurnout() / (float)EntityExtension.For(player).getMaxBurnout());

		list.add("Mana Cost : " + manaCost);
	}

	@Override
	public int getMaxItemUseDuration(ItemStack par1ItemStack){
		return 72000;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer caster, EnumHand hand){
		if (!stack.hasTagCompound()) return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
		if (!stack.hasDisplayName()){
			if (!world.isRemote)
				FMLNetworkHandler.openGui(caster, ArsMagica2.instance, IDDefs.GUI_SPELL_CUSTOMIZATION, world, (int)caster.posX, (int)caster.posY, (int)caster.posZ);
		} else {
			caster.setActiveHand(hand);
		}
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
	}

	@Override
	public boolean hasEffect(ItemStack par1ItemStack){
		return par1ItemStack.getTagCompound() != null && par1ItemStack.getTagCompound().getBoolean("HasEffect");
	}
	
	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World world,
			EntityLivingBase player, int timeLeft) {
		SpellShape shape = SpellUtils.getShapeForStage(stack, 0);
		if (!stack.hasTagCompound()) return;
		if (shape != null){
			if (!shape.isChanneled())
				SpellUtils.applyStackStage(stack, player, null, player.posX, player.posY, player.posZ, EnumFacing.UP, world, true, true, 0);
			if (world.isRemote && shape.isChanneled()){
				//SoundHelper.instance.stopSound(shape.getSoundForAffinity(SpellUtils.instance.mainAffinityFor(stack), stack, null));
			}
		}
	}
	
	@Override
	public void onUsingTick(ItemStack stack, EntityLivingBase caster, int count) {
		SpellShape shape = SpellUtils.getShapeForStage(stack, 0);
		if (shape.isChanneled())
			SpellUtils.applyStackStage(stack, caster, null, caster.posX, caster.posY, caster.posZ, EnumFacing.UP, caster.worldObj, true, true, count - 1);
		super.onUsingTick(stack, caster, count);
	}
	
	@Override
	public RayTraceResult getMovingObjectPosition(EntityLivingBase caster, World world, double range, boolean includeEntities, boolean targetWater){
		RayTraceResult entityPos = null;
		if (includeEntities){
			Entity pointedEntity = EntityUtils.getPointedEntity(world, caster, range, 1.0f, false, targetWater);
			if (pointedEntity != null){
				entityPos = new RayTraceResult(pointedEntity);
			}
		}

		float factor = 1.0F;
		float interpPitch = caster.prevRotationPitch + (caster.rotationPitch - caster.prevRotationPitch) * factor;
		float interpYaw = caster.prevRotationYaw + (caster.rotationYaw - caster.prevRotationYaw) * factor;
		double interpPosX = caster.prevPosX + (caster.posX - caster.prevPosX) * factor;
		double interpPosY = caster.prevPosY + (caster.posY - caster.prevPosY) * factor + caster.getEyeHeight();
		double interpPosZ = caster.prevPosZ + (caster.posZ - caster.prevPosZ) * factor;
		Vec3d vec3 = new Vec3d(interpPosX, interpPosY, interpPosZ);
		float offsetYawCos = MathHelper.cos(-interpYaw * 0.017453292F - (float)Math.PI);
		float offsetYawSin = MathHelper.sin(-interpYaw * 0.017453292F - (float)Math.PI);
		float offsetPitchCos = -MathHelper.cos(-interpPitch * 0.017453292F);
		float offsetPitchSin = MathHelper.sin(-interpPitch * 0.017453292F);
		float finalXOffset = offsetYawSin * offsetPitchCos;
		float finalZOffset = offsetYawCos * offsetPitchCos;
		Vec3d targetVector = vec3.addVector(finalXOffset * range, offsetPitchSin * range, finalZOffset * range);
		RayTraceResult mop = world.rayTraceBlocks(vec3, targetVector, targetWater, !targetWater, false);

		if (entityPos != null && mop != null){
			if (mop.hitVec.distanceTo(new RayTraceResult(caster).hitVec) < entityPos.hitVec.distanceTo(new RayTraceResult(caster).hitVec)){
				return mop;
			}else{
				return entityPos;
			}
		}

		return entityPos != null ? entityPos : mop;

	}
	
	@Override
	public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onUpdate(ItemStack stack, World world, Entity entity, int par4, boolean par5){
		super.onUpdate(stack, world, entity, par4, par5);
		if (entity instanceof EntityPlayerSP && ((EntityPlayerSP)entity).getActiveHand() != null){
			EntityPlayerSP player = (EntityPlayerSP)entity;
			ItemStack usingItem = player.getActiveItemStack();
			if (usingItem != null && usingItem.getItem() == this){
				if (SkillData.For(player).hasSkill("spellMovement")){
					player.movementInput.moveForward *= 2.5F;
					player.movementInput.moveStrafe *= 2.5F;
				}
			}
		}
	}

	@Override
	public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, EntityPlayer player) {
	    player.worldObj.destroyBlock(pos, player.canHarvestBlock(player.worldObj.getBlockState(pos)));
	    return true;
	}

	@Override
	public int getHarvestLevel(ItemStack stack, String toolClass, @Nullable EntityPlayer player, @Nullable IBlockState blockState) {
	    return SpellUtils.getModifiedInt_Add(2, stack, (EntityLivingBase)player, (EntityLivingBase)player, player.getEntityWorld(), SpellModifiers.MINING_POWER);
	}
}

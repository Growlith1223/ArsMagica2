package am2.items;

import java.util.List;

import am2.api.power.IPowerNode;
import am2.api.spell.SpellShape;
import am2.defs.ItemDefs;
import am2.defs.SkillDefs;
import am2.extensions.SkillData;
import am2.packet.AMNetHandler;
import am2.power.PowerNodeRegistry;
import am2.power.PowerTypes;
import am2.spell.SpellCastResult;
import am2.utils.SpellUtils;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SuppressWarnings("deprecation")
public class ItemSpellStaff extends ItemArsMagica{

	private final int castingMode;
	private final int maxCharge;

	private static final String NBT_CHARGE = "current_charge";
	private static final String NBT_SPELL = "spell_to_cast";
	private static final String NBT_SPELL_NAME = "spell_name";

	public ItemSpellStaff(int charge, int castingMode){
		super();
		this.setMaxDamage(charge);
		this.maxCharge = charge;
		this.maxStackSize = 1;
		this.castingMode = castingMode;
	}

	/**
	 * Sets the displayed head of the staff.
	 *
	 * @param index The index.  Valid values: 1 (lesser), 2 (standard), 3 (greater)
	 * @return
	 */
	public ItemSpellStaff setStaffHeadIndex(int index){
		if (index > 3 || index < 0){
			index = 0;
		}
		return this;
	}

	@Override
	public boolean hasEffect(ItemStack par1ItemStack){
		return isMagiTechStaff() || getSpellStack(par1ItemStack) != null;
	}

	public boolean isMagiTechStaff(){
		return this.castingMode == -1;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack par1ItemStack){
		if (isMagiTechStaff())
			return EnumAction.NONE;
		return EnumAction.BLOCK;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldRotateAroundWhenRendering(){
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isFull3D(){
		return true;
	}

	public void setSpellScroll(ItemStack stack, ItemStack spell){
		if (isMagiTechStaff()) return;
		if (stack.getTagCompound() == null){
			stack.setTagCompound(new NBTTagCompound());
		}
		NBTTagCompound compound = stack.getTagCompound();
		NBTTagCompound spellCompound = spell.writeToNBT(new NBTTagCompound());
		compound.setTag(NBT_SPELL, spellCompound);
		compound.setString(NBT_SPELL_NAME, spell.getDisplayName());
		if (!compound.hasKey(NBT_CHARGE))
			compound.setFloat(NBT_CHARGE, maxCharge);
	}

	public void copyChargeFrom(ItemStack my_stack, ItemStack stack){
		if (isMagiTechStaff()) return;
		if (stack.getItem() instanceof ItemSpellStaff && stack.getTagCompound() != null && stack.getTagCompound().hasKey("current_charge")){
			if (my_stack.getTagCompound() == null){
				my_stack.setTagCompound(new NBTTagCompound());
			}
			my_stack.getTagCompound().setFloat("current_charge", stack.getTagCompound().getFloat("current_charge"));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer par2EntityPlayer, List<String> par3List, boolean par4){
		if (stack.getTagCompound() == null || isMagiTechStaff()) return;
		float chargeCost = 1;

		ItemStack spell = getSpellStack(stack);

		if (spell != null){
			chargeCost = SpellUtils.getManaCost(spell, par2EntityPlayer);
		}
		if (chargeCost == 0)
			chargeCost = 1;

		float chargeRemaining = stack.getTagCompound().getFloat(NBT_CHARGE);
		int chargesRemaining = (int)Math.ceil(chargeRemaining / chargeCost);

		par3List.add(I18n.translateToLocal("am2.tooltip.charge") + ": " + (int)chargeRemaining + " / " + maxCharge);
		par3List.add("" + chargesRemaining + " " + I18n.translateToLocal("am2.tooltip.uses") + ".");
	}

	@Override
	public String getItemStackDisplayName(ItemStack par1ItemStack){
		if (isMagiTechStaff()){
			return I18n.translateToLocal("item.arsmagica2:spell_staff_magitech.name");
		}
		String name = super.getItemStackDisplayName(par1ItemStack);
		if (par1ItemStack.hasTagCompound() && par1ItemStack.getTagCompound().hasKey(NBT_SPELL_NAME))
			name += " (\2479" + par1ItemStack.getTagCompound().getString(NBT_SPELL_NAME) + "\2477)";
		return name;
	}

	@Override
	public boolean getShareTag(){
		return true;
	}
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ){
		if (isMagiTechStaff()){
			return EnumActionResult.SUCCESS;
		}

		return EnumActionResult.PASS;
	}
	
	@Override
	public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand){
		if (isMagiTechStaff()){
			if (!world.isRemote){
				TileEntity te = world.getTileEntity(pos);
				if (te != null && te instanceof IPowerNode){
					if (player.isSneaking()){
						AMNetHandler.INSTANCE.syncPowerPaths((IPowerNode<?>)te, (EntityPlayerMP)player);
					}else{
						List<PowerTypes> types = ((IPowerNode<?>)te).getValidPowerTypes();
						for (PowerTypes type : types){
							float power = PowerNodeRegistry.For(world).getPower((IPowerNode<?>)te, type);
							player.sendMessage(
									new TextComponentString(String.format(I18n.translateToLocal("am2.tooltip.det_eth"),
											type.getChatColor(), type.name(), String.format("%.2f", power))));
						}
					}
					return EnumActionResult.SUCCESS;
				}
			}
		}

		return EnumActionResult.PASS;
	}
	
	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft){
		if (isMagiTechStaff()) return;

		ItemStack spell = getSpellStack(stack);
		if (spell != null){

			SpellShape shape = SpellUtils.getShapeForStage(spell, 0);
			if (shape != null){
				if (!shape.isChanneled())
					if (SpellUtils.applyStackStage(spell, entityLiving, null, entityLiving.posX, entityLiving.posY, entityLiving.posZ, null, worldIn, false, false, timeLeft) == SpellCastResult.SUCCESS)
						consumeStaffCharge(stack, (EntityPlayer) entityLiving);
				if (worldIn.isRemote && shape.isChanneled()){
					//SoundHelper.instance.stopSound(shape.getSoundForAffinity(SpellUtils.instance.mainAffinityFor(spell), spell, null));
				}
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onUpdate(ItemStack stack, World world, Entity entity, int par4, boolean par5){
		super.onUpdate(stack, world, entity, par4, par5);
		if (entity instanceof EntityPlayerSP){
			EntityPlayerSP player = (EntityPlayerSP)entity;
			ItemStack usingItem = player.getActiveItemStack();
			if (usingItem != null && usingItem.getItem() == this){
				if (SkillData.For(player).hasSkill(SkillDefs.SPELL_MOTION.getID())){
					player.movementInput.moveForward *= 2.5F;
					player.movementInput.moveStrafe *= 2.5F;
				}
			}
		}
	}

	@Override
	public int getMaxItemUseDuration(ItemStack itemStack){
		return 2000;
	}

	private ItemStack getSpellStack(ItemStack staffStack){
		if (!staffStack.hasTagCompound() || !staffStack.getTagCompound().hasKey(NBT_SPELL))
			return null;
		ItemStack stack = new ItemStack(ItemDefs.spell);
		//stack.readFromNBT(staffStack.getTagCompound().getCompoundTag(NBT_SPELL));
		return stack;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer entityplayer, EnumHand hand){
		ItemStack itemstack = entityplayer.getHeldItem(hand);
		if (isMagiTechStaff())
			return new ActionResult<ItemStack>(EnumActionResult.PASS, itemstack);

		if (getSpellStack(itemstack) != null)
			entityplayer.setActiveHand(hand);

		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityLivingBase player, int count){
		if (player instanceof EntityPlayer) {
			if (isMagiTechStaff()) return;
			ItemStack spell = getSpellStack(stack);
			if (spell != null){
				if (SpellUtils.applyStackStageOnUsing(spell, player, player, player.posX, player.posY, player.posZ, player.world, false, true, count - 1) == SpellCastResult.SUCCESS)
					consumeStaffCharge(stack, (EntityPlayer)player);
			}
		}
	}

	@Override
	public int getDamage(ItemStack stack){
		if (!stack.hasTagCompound())
			return super.getDamage(stack);
		float chargeRemaining = stack.getTagCompound().getFloat(NBT_CHARGE);
		return maxCharge - (int)Math.floor(chargeRemaining);
	}

	private void consumeStaffCharge(ItemStack staffStack, EntityPlayer caster){
		float chargeCost = 1;

		ItemStack spell = getSpellStack(staffStack);

		if (spell != null){
			chargeCost = SpellUtils.getManaCost(staffStack, caster);
		}
		if (chargeCost == 0)
			chargeCost = 1;

		float chargeRemaining = staffStack.getTagCompound().getFloat(NBT_CHARGE);
		chargeRemaining -= chargeCost;
		staffStack.getTagCompound().setFloat(NBT_CHARGE, chargeRemaining);

		if (chargeRemaining <= 0){
			if (!caster.world.isRemote){
				if (caster.getActiveItemStack() != null) {
					caster.getActiveItemStack().damageItem(9001, caster);
				}
			}
		}

	}
}

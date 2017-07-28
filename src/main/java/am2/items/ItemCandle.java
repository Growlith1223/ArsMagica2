package am2.items;

import java.util.List;

import am2.ArsMagica2;
import am2.blocks.BlockInvisibleUtility;
import am2.defs.BlockDefs;
import am2.particles.AMParticle;
import am2.particles.ParticleHoldPosition;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

@SuppressWarnings("deprecation")
public class ItemCandle extends ItemArsMagica{

	private static final int radius = 10;
	private static final int short_radius = 5;
	private static final float immediate_radius = 2.5f;

	public ItemCandle(){
		super();
		setMaxStackSize(1);
		setMaxDamage(18000); //15 minutes (20 * 60 * 15)
	}
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack stack = playerIn.getHeldItem(hand);
		if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey("search_block")){
			IBlockState block = worldIn.getBlockState(pos);
			if (playerIn.isSneaking() && block != null && block.getBlockHardness(worldIn, pos) > 0f && worldIn.getTileEntity(pos) == null){
				if (!worldIn.isRemote){
					setSearchBlock(block, stack);
					worldIn.setBlockToAir(pos);
				}else{
					AMParticle particle = (AMParticle)ArsMagica2.proxy.particleManager.spawn(worldIn, "radiant", pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
					if (particle != null){
						particle.AddParticleController(new ParticleHoldPosition(particle, 20, 1, false));
						particle.setRGBColorF(0, 0.5f, 1);
					}
				}
				return EnumActionResult.SUCCESS;
			}
		}

		if (!worldIn.isRemote){

			if (stack.hasTagCompound() && stack.getTagCompound().hasKey("search_block")){
				playerIn.sendMessage(new TextComponentString(I18n.translateToLocal("am2.tooltip.candlecantplace")));
				return EnumActionResult.PASS;
			}

			pos = pos.offset(facing);

			IBlockState block = worldIn.getBlockState(pos);
			if (block == null || block.getBlock().isReplaceable(worldIn, pos)){
				worldIn.setBlockState(pos, BlockDefs.wardingCandle.getDefaultState(), 3);
				if (!playerIn.capabilities.isCreativeMode)
					playerIn.inventory.setInventorySlotContents(playerIn.inventory.currentItem, null);
			}
			return EnumActionResult.PASS;
		}
		return EnumActionResult.PASS;
	}
	
	
//	@Override
//	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ){
//
//		if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey("search_block")){
//			Block block = world.getBlock(x, y, z);
//			if (player.isSneaking() && block != null && block.getBlockHardness(world, x, y, z) > 0f && world.getTileEntity(x, y, z) == null){
//				if (!world.isRemote){
//					setSearchBlock(world.getBlock(x, y, z), world.getBlockMetadata(x, y, z), stack);
//					world.setBlockToAir(x, y, z);
//				}else{
//					AMParticle particle = (AMParticle)AMCore.proxy.particleManager.spawn(world, "radiant", x + 0.5, y + 0.5, z + 0.5);
//					if (particle != null){
//						particle.AddParticleController(new ParticleHoldPosition(particle, 20, 1, false));
//						particle.setRGBColorF(0, 0.5f, 1);
//					}
//				}
//				return true;
//			}
//		}
//
//		if (!world.isRemote){
//
//			if (stack.hasTagCompound() && stack.getTagCompound().hasKey("search_block")){
//				player.sendMessage(new ChatComponentText(I18n.translateToLocal("am2.tooltip.candlecantplace")));
//				return false;
//			}
//
//			switch (side){
//			case 0:
//				y--;
//				break;
//			case 1:
//				y++;
//				break;
//			case 2:
//				z--;
//				break;
//			case 3:
//				z++;
//				break;
//			case 4:
//				x--;
//				break;
//			case 5:
//				x++;
//				break;
//			}
//
//			Block block = world.getBlock(x, y, z);
//			if (block == null || block.isReplaceable(world, x, y, z)){
//				int newMeta = (int)Math.ceil(stack.getItemDamage() / 1200);
//				world.setBlock(x, y, z, Blockdef.candle, newMeta, 2);
//				if (!player.capabilities.isCreativeMode)
//					player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
//			}
//			return true;
//		}
//		return false;
//	}

	public void setSearchBlock(IBlockState state, ItemStack item){
		if (!item.hasTagCompound())
			item.setTagCompound(new NBTTagCompound());

		setFlameColor(item, 0, 1, 0);
		item.getTagCompound().setInteger("search_block", Block.getStateId(state));
	}

	private void setFlameColor(ItemStack stack, float r, float g, float b){
		if (!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());

		stack.getTagCompound().setFloat("flame_red", r);
		stack.getTagCompound().setFloat("flame_green", g);
		stack.getTagCompound().setFloat("flame_blue", b);
	}

	public void search(EntityPlayer player, ItemStack stack, World world, BlockPos pos, IBlockState state){
		boolean found = false;
		boolean foundLarge = false;
		boolean foundSmall = false;
		for (int i = -radius; i <= radius; ++i){
			for (int j = -radius; j <= radius; ++j){
				for (int k = -radius; k <= radius; ++k){
					IBlockState f_block = world.getBlockState(pos.add(i, j, k));
					int dist = Math.abs(i) + Math.abs(j) + Math.abs(k);
					if (dist > radius) continue;

					if (state == f_block){
						if (dist <= immediate_radius){// && player.getCurrentArmor(3) != null && ArmorHelper.isInfusionPreset(player.getCurrentArmor(3), GenericImbuement.pinpointOres)){
							setFlameColor(stack, 0, 0, 0);
							return;
						}else if (dist <= short_radius && !foundSmall){
							setFlameColor(stack, 1, 0, 0);
							found = true;
							foundSmall = true;
						}else if (!found && !foundSmall && !foundLarge){
							setFlameColor(stack, 0, 0.5f, 1f);
							found = true;
							foundLarge = true;
						}
					}
				}
			}
		}
		if(!found)
			setFlameColor(stack, 0, 1, 0);
	}

	@Override
	public boolean getShareTag(){
		return true;
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int indexInInventory, boolean isCurrentlyHeld){
		if (entity instanceof EntityPlayer){
			EntityPlayer player = (EntityPlayer)entity;
			isCurrentlyHeld = player.getHeldItemMainhand() == stack;
			isCurrentlyHeld |= player.getHeldItemOffhand() == stack;
			if (isCurrentlyHeld && !world.isRemote && ArsMagica2.config.candlesAreRovingLights() && world.isAirBlock(player.getPosition()) && world.getLightFor(EnumSkyBlock.BLOCK, player.getPosition()) < 14){
				world.setBlockState(player.getPosition(), BlockDefs.invisibleUtility.getDefaultState().withProperty(BlockInvisibleUtility.TYPE, BlockInvisibleUtility.EnumInvisibleType.HIGH_ILLUMINATED), 2);
			}
			if (isCurrentlyHeld) {
				if (!world.isRemote && stack.hasTagCompound() && stack.getItemDamage() % 40 == 0){
					search(player, stack, world, player.getPosition(), Block.getStateById(stack.getTagCompound().getInteger("search_block")));
				}
				
				if (!player.capabilities.isCreativeMode)
					stack.damageItem(1, player);
				if (!world.isRemote && stack.getItemDamage() >= this.getMaxDamage())
					player.setItemStackToSlot((player.getHeldItemOffhand() == stack ? EntityEquipmentSlot.OFFHAND : EntityEquipmentSlot.MAINHAND), null);
			}
		}
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack){
		String name = I18n.translateToLocal("item.arsmagica2:warding_candle.name");
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("search_block")){
			IBlockState state = Block.getStateById(stack.getTagCompound().getInteger("search_block"));
			ItemStack blockStack = new ItemStack(state.getBlock(), 0, state.getBlock().getMetaFromState(state));
			Item tempItem = blockStack.getItem();
			if(tempItem == null){
				name += " (" + stack.getTagCompound().getInteger("search_block") + ":" + stack.getTagCompound().getInteger("search_meta") + ")";
			}
			else{
				name += " (" + blockStack.getDisplayName() + ")";
			}
		}else{
			name += " (" + I18n.translateToLocal("am2.tooltip.unattuned") + ")";
		}

		return name;
	}

	@Override
	public boolean getHasSubtypes(){
		return true;
	}
	
	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		if (oldStack == null) return slotChanged;
		if (newStack == null) return slotChanged;
		if (oldStack.getTagCompound() == null) return slotChanged;
		if (newStack.getTagCompound() == null) return slotChanged;
		if (oldStack.getTagCompound().equals(newStack.getTagCompound())) return false;
		return slotChanged;
	}

	@Override
	public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, NonNullList<ItemStack> par3List){
		ItemStack unattuned = new ItemStack(this, 1, 0);
		par3List.add(unattuned);
	}
}


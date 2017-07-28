package am2.spell.component;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Sets;

import am2.api.affinity.Affinity;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellModifiers;
import am2.defs.ItemDefs;
import am2.utils.InventoryUtilities;
import am2.utils.SpellUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

@SuppressWarnings("deprecation")
public class PlaceBlock extends SpellComponent{

	private static final String KEY_BLOCKID = "PlaceBlockID";
	private static final String KEY_META = "PlaceMeta";

	@Override
	public Object[] getRecipe(){
		return new Object[]{
				Items.STONE_AXE,
				Items.STONE_PICKAXE,
				Items.STONE_SHOVEL,
				Blocks.CHEST
		};
	}

	private IBlockState getPlaceBlock(ItemStack stack){
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey(KEY_BLOCKID)){
			return Block.getBlockById(stack.getTagCompound().getInteger(KEY_BLOCKID)).getStateFromMeta(stack.getTagCompound().getInteger(KEY_META));
		}
		return null;
	}

	private void setPlaceBlock(ItemStack stack, IBlockState state){
		if (!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());

		stack.getTagCompound().setInteger(KEY_BLOCKID, Block.getIdFromBlock(state.getBlock()));
		stack.getTagCompound().setInteger(KEY_META, state.getBlock().getMetaFromState(state));

		//set lore entry so that the stack displays the name of the block to place
		if (!stack.getTagCompound().hasKey("Lore"))
			stack.getTagCompound().setTag("Lore", new NBTTagList());

		ItemStack blockStack = new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));

		NBTTagList tagList = stack.getTagCompound().getTagList("Lore", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < tagList.tagCount(); ++i){
			String str = tagList.getStringTagAt(i);
			if (str.startsWith(String.format(I18n.translateToLocal("am2.tooltip.placeBlockSpell"), ""))){
				tagList.removeTag(i);
			}
		}
		tagList.appendTag(new NBTTagString(String.format(I18n.translateToLocal("am2.tooltip.placeBlockSpell"), blockStack.getDisplayName())));

		stack.getTagCompound().setTag("Lore", tagList);
	}
	
	@Override
	public EnumSet<SpellModifiers> getModifiers() {
		return EnumSet.noneOf(SpellModifiers.class);
	}
	
	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, BlockPos pos, EnumFacing blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster){

		if (!(caster instanceof EntityPlayer))
			return false;

		EntityPlayer player = (EntityPlayer)caster;
		ItemStack spellStack = player.getActiveItemStack();
		if (spellStack == null || spellStack.getItem() != ItemDefs.spell || !SpellUtils.componentIsPresent(spellStack, PlaceBlock.class))
			return false;

		IBlockState bd = getPlaceBlock(spellStack);

		if (bd != null && !caster.isSneaking()){
			if (world.isAirBlock(pos) || !world.getBlockState(pos).isSideSolid(world, pos, blockFace))
				blockFace = null;
			if (blockFace != null){
				pos = pos.add(blockFace.getDirectionVec());
			}
			if (world.isAirBlock(pos) || !world.getBlockState(pos).getMaterial().isSolid()){
				ItemStack searchStack = new ItemStack(bd.getBlock(), 1, bd.getBlock().getMetaFromState(bd));
				if (!world.isRemote && (player.capabilities.isCreativeMode || InventoryUtilities.inventoryHasItem(player.inventory, searchStack, 1))){
					world.setBlockState(pos, bd);
					if (!player.capabilities.isCreativeMode)
						InventoryUtilities.deductFromInventory(player.inventory, searchStack, 1);
				}
				return true;
			}
		}else if (caster.isSneaking()){
			if (!world.isRemote && !world.isAirBlock(pos)){
				setPlaceBlock(spellStack, world.getBlockState(pos));
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target){
		return false;
	}

	@Override
	public float manaCost(EntityLivingBase caster){
		return 5;
	}

	@Override
	public ItemStack[] reagents(EntityLivingBase caster){
		return null;
	}

	@Override
	public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier){
	}

	@Override
	public Set<Affinity> getAffinity(){
		return Sets.newHashSet(Affinity.EARTH, Affinity.ENDER);
	}

	@Override
	public float getAffinityShift(Affinity affinity){
		return 0.05f;
	}

	@Override
	public void encodeBasicData(NBTTagCompound tag, Object[] recipe) {

		
	}

}

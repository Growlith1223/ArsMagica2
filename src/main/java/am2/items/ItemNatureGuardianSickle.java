package am2.items;

import java.util.List;

import com.google.common.collect.Multimap;

import am2.ArsMagica2;
import am2.defs.ItemDefs;
import am2.entity.EntityThrownSickle;
import am2.extensions.EntityExtension;
import am2.utils.DummyEntityPlayer;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

@SuppressWarnings("deprecation")
public class ItemNatureGuardianSickle extends ItemArsMagica{

	public ItemNatureGuardianSickle(){
		super();
		setMaxStackSize(1);
	}

	@Override
	public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot slot){
		Multimap<String, AttributeModifier> multimap = super.getItemAttributeModifiers(slot);
		if (slot.equals(EntityEquipmentSlot.MAINHAND)) {
			multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", 9, 0));
			multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -3, 0));
		}
		return multimap;
	}

	@Override
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List<String> par3List, boolean par4){
		par3List.add(I18n.translateToLocal("am2.tooltip.nature_scythe"));
		super.addInformation(par1ItemStack, par2EntityPlayer, par3List, par4);
	}
	
	@Override
	public boolean onBlockDestroyed(ItemStack stack, World worldIn, IBlockState state, BlockPos pos, EntityLivingBase entityLiving) {
		int radius = 1;
		for (int i = -radius; i <= radius; ++i){
			for (int j = -radius; j <= radius; ++j){
				for (int k = -radius; k <= radius; ++k){

					if (EntityExtension.For(entityLiving).getCurrentMana() < 5f){
						if (worldIn.isRemote)
							ArsMagica2.proxy.flashManaBar();
						return false;
					}

					IBlockState nextBlock = worldIn.getBlockState(pos.add(i, j, k));
					if (nextBlock == null) continue;
					if (nextBlock.getBlock() instanceof BlockLeaves){
						if (ForgeEventFactory.doPlayerHarvestCheck(DummyEntityPlayer.fromEntityLiving(entityLiving), nextBlock, true))
							if (!worldIn.isRemote)
								worldIn.destroyBlock(pos.add(i, j, k), true);
						EntityExtension.For(entityLiving).deductMana(5f);
					}
				}
			}
		}

		return false;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand){
		ItemStack item = player.getHeldItem(hand);
		if (flingSickle(item, world, player)){
			player.setItemStackToSlot(hand == EnumHand.MAIN_HAND ? EntityEquipmentSlot.MAINHAND : EntityEquipmentSlot.OFFHAND, null);//inventory.setInventorySlotContents(par3EntityPlayer.inventory.currentItem, null);
		}
		return new ActionResult<>(EnumActionResult.SUCCESS, item);
	}

	public boolean flingSickle(ItemStack stack, World world, EntityPlayer player){
		if (!EntityExtension.For(player).hasEnoughtMana(250) && !player.capabilities.isCreativeMode){
			if (world.isRemote)
				ArsMagica2.proxy.flashManaBar();
			return false;
		}
		if (!world.isRemote){
			EntityThrownSickle projectile = new EntityThrownSickle(world, player, 1.25f);
			projectile.setSickleNBT(stack);
			projectile.setThrowingEntity(player);
			projectile.setProjectileSpeed(2.0);
			//projectile.setInMotion(1.25);
			world.spawnEntity(projectile);
			EntityExtension.For(player).deductMana(250f);
		}
		return true;
	}

	@Override
	public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, NonNullList<ItemStack> par3List){
		par3List.add(ItemDefs.natureScytheEnchanted.copy());
	}
}

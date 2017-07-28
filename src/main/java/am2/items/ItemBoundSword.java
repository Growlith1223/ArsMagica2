package am2.items;

import am2.api.IBoundItem;
import am2.defs.ItemDefs;
import am2.utils.SpellUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ItemBoundSword extends ItemSword implements IBoundItem {

	public ItemBoundSword() {
		super(ItemDefs.BOUND);
		this.maxStackSize = 1;
		this.setMaxDamage(0);
		this.setCreativeTab(null);
	}

	@SuppressWarnings("deprecation")
	public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
		if (!stack.hasTagCompound())
			return true;
		ItemStack copiedStack = SpellUtils.merge(stack.copy());
		copiedStack.getTagCompound().getCompoundTag("AM2").setInteger("CurrentGroup", SpellUtils.currentStage(stack) + 1);
		ItemStack spell = new ItemStack(ItemDefs.spell);
		spell.setTagCompound(copiedStack.getTagCompound());
		spell.getTagCompound().getCompoundTag("AM2").setInteger("CurrentGroup", SpellUtils.currentStage(stack) + 1);
		copiedStack = spell.copy();
		int hurtResist = target.hurtResistantTime;
		target.hurtResistantTime = 0;
		SpellUtils.applyStackStage(copiedStack, attacker, target, target.posX, target.posY, target.posZ, null, attacker.world, true, true, 0);
		target.hurtResistantTime = hurtResist;
		return true;
	}

	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
		return false;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onDroppedByPlayer(ItemStack item, EntityPlayer player) {
		NBTTagCompound nbt = item.getTagCompound();
		item = new ItemStack(ItemDefs.spell);
		item.setTagCompound(nbt);
		return false;
	}

	@Override
	public float maintainCost(EntityPlayer player, ItemStack stack) {
		return normalMaintain;
	}

	public ItemSword registerAndName(String name) {
		this.setUnlocalizedName(new ResourceLocation("arsmagica2", name).toString());
		GameRegistry.register(this, new ResourceLocation("arsmagica2", name));
		return this;
	}

}

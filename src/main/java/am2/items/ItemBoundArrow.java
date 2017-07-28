package am2.items;

import java.util.List;

import am2.entity.EntityBoundArrow;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ItemBoundArrow extends ItemArrow {
	
	public ItemBoundArrow() {
		setCreativeTab(null);
	}
	
	@Override
	public void getSubItems(Item itemIn, CreativeTabs tab, NonNullList<ItemStack> subItems) {
	}
	
	@Override
	public EntityArrow createArrow(World worldIn, ItemStack stack, EntityLivingBase shooter) {
		EntityBoundArrow arrow = new EntityBoundArrow(worldIn, shooter);
		arrow.setSpellStack(stack);
		return arrow;
	}
	
	public ItemBoundArrow registerAndName(String name) {
		this.setUnlocalizedName(new ResourceLocation("arsmagica2", name).toString());
		GameRegistry.register(this, new ResourceLocation("arsmagica2", name));
		return this;
	}
}

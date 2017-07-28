package am2.armor;

import java.util.List;

import am2.defs.ItemDefs;
import am2.proxy.gui.ModelLibrary;
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
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SuppressWarnings("deprecation")
public class ItemFireGuardianEars extends AMArmor{

	public ItemFireGuardianEars(ArmorMaterial inheritFrom, ArsMagicaArmorMaterial enumarmormaterial, int par3, EntityEquipmentSlot par4){
		super(inheritFrom, enumarmormaterial, par3, par4);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot, ModelBiped _default) {
		return ModelLibrary.instance.fireEars;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getArmorDisplay(EntityPlayer player, ItemStack armor, int slot){
		return 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List<String> par3List, boolean par4){
		par3List.add(I18n.translateToLocal("am2.tooltip.fire_ears"));
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type){
		return "arsmagica2:textures/mobs/bosses/fire_guardian.png";
	}

	public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, NonNullList<ItemStack> par3List){
		par3List.add(ItemDefs.fireEarsEnchanted.copy());
	}

	@Override
	public void onArmorTick(World world, EntityPlayer plr, ItemStack stack) {
		super.onArmorTick(world, plr, stack);
		if (!plr.getActivePotionEffects().contains(PotionTypes.FIRE_RESISTANCE))
			plr.addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation(new ResourceLocation("fire_resistance").toString())));
	}
}

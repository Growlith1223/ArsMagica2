package am2.items;

import java.util.HashMap;
import java.util.List;

import am2.utils.EntityUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityList.EntityEggInfo;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
@SuppressWarnings("deprecation")
public class ItemCrystalPhylactery extends ItemArsMagica{

	public final HashMap<String, Integer> spawnableEntities;

	public static final int META_EMPTY = 0;
	public static final int META_QUARTER = 1;
	public static final int META_HALF = 2;
	public static final int META_FULL = 3;


	public ItemCrystalPhylactery(){
		super();
		spawnableEntities = new HashMap<String, Integer>();
		setMaxDamage(0);
		setMaxStackSize(1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> tip, boolean HIGHLYNECESSARYPARAMETER){
		if (stack.hasTagCompound()){
			int id = stack.getTagCompound().getInteger("id");
			if (id != 0){
				tip.add(String.format("am2.tooltip.phyEss", "entity." + EntityList.getClassFromID(id).getName() + ".name"));
				Float f = stack.getTagCompound().getFloat("PercentFilled");
				float pct = f == null ? 0 : f.floatValue();
				tip.add(String.format(I18n.translateToLocal("am2.tooltip.pctFull"), pct));
			}else{
				tip.add(I18n.translateToLocal("am2.tooltip.empty"));
			}
		}else{
			tip.add(I18n.translateToLocal("am2.tooltip.empty"));
		}
	}
	
	public void addFill(ItemStack stack){
		if (stack.hasTagCompound()){
			int id = stack.getTagCompound().getInteger("id");
			if (id != 0){
				Float f = stack.getTagCompound().getFloat("PercentFilled");
				float pct = f == null ? 0 : f.floatValue();
				pct += itemRand.nextFloat() * 5;
				if (pct > 100) pct = 100;
				stack.getTagCompound().setFloat("PercentFilled", pct);
				if (pct == 100)
					stack.setItemDamage(META_FULL);
				else if (pct > 50)
					stack.setItemDamage(META_HALF);
				else if (pct > 25)
					stack.setItemDamage(META_QUARTER);
				else
					stack.setItemDamage(META_EMPTY);

			}
		}
	}

	public void addFill(ItemStack stack, float amt){
		if (stack.hasTagCompound()){
			int id = stack.getTagCompound().getInteger("id");
			if (id != 0){
				Float f = stack.getTagCompound().getFloat("PercentFilled");
				float pct = f == null ? 0 : f.floatValue();
				pct += amt;
				if (pct > 100) pct = 100;
				stack.getTagCompound().setFloat("PercentFilled", pct);
				if (pct == 100)
					stack.setItemDamage(META_FULL);
				else if (pct > 50)
					stack.setItemDamage(META_HALF);
				else if (pct > 25)
					stack.setItemDamage(META_QUARTER);
				else
					stack.setItemDamage(META_EMPTY);

			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack par1ItemStack){
		return par1ItemStack.getItemDamage() == META_FULL;
	}

	public void setSpawnClass(ItemStack stack, Class<? extends Entity> clazz){

		if (!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		
		int e = EntityList.getID(clazz);
		if (e != -1)
			stack.getTagCompound().setInteger("id", e);
	}

	public boolean canStore(ItemStack stack, EntityLiving entity){
		if (!entity.isNonBoss()) return false;
		if (stack.getItemDamage() == META_FULL)
			return false;
		if (!stack.hasTagCompound())
			return true;
		
		int e = stack.getTagCompound().getInteger("id");
		int s = EntityList.getID(entity.getClass());
		return (e == s);
	}

	public boolean isFull(ItemStack stack){
		return stack.getItemDamage() == META_FULL;
	}

	public int getSpawnClass(ItemStack stack){
		if (!stack.hasTagCompound())
			return 0;
		return stack.getTagCompound().getInteger("id");
	}
	
	public void getSpawnableEntities(World world){
		for (ResourceLocation key : ForgeRegistries.ENTITIES.getKeys()){
			Object clazz = EntityList.getClass(key);
			if (EntityCreature.class.isAssignableFrom((Class<?>)clazz)){
				try{
					EntityCreature temp = (EntityCreature)((Class<?>)clazz).getConstructor(World.class).newInstance(world);
					if (EntityUtils.isAIEnabled(temp) && temp.isNonBoss()){
						int color = 0;
						boolean found = false;
						//look for entity egg
						for (Object info : EntityList.ENTITY_EGGS.values()){
							EntityEggInfo eei = (EntityEggInfo)info;
							Class<?> spawnClass = EntityList.getClass(eei.spawnedID);
							if (spawnClass == (Class<?>)clazz){
								color = eei.primaryColor;
								found = true;
								break;
							}
						}
						if (!found){
							//no spawn egg...pick random color?
							color = world.rand.nextInt();
						}
						spawnableEntities.put(clazz.getClass().getName(), color);
					}
				}catch (Throwable e){
					//e.printStackTrace();
				}
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, NonNullList<ItemStack> par3List){
		par3List.add(new ItemStack(this));
		for (String s : spawnableEntities.keySet()){
			ItemStack stack = new ItemStack(this, 1, META_FULL);
			stack.setTagCompound(new NBTTagCompound());
			stack.getTagCompound().setString("SpawnClassName", s);
			stack.getTagCompound().setFloat("PercentFilled", 100);
			par3List.add(stack);
		}
	}
}

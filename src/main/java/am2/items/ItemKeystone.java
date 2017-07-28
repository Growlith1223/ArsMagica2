package am2.items;

import java.util.List;

import am2.ArsMagica2;
import am2.container.InventoryKeyStone;
import am2.defs.IDDefs;
import am2.defs.ItemDefs;
import am2.utils.KeystoneUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;

@SuppressWarnings("deprecation")
public class ItemKeystone extends ItemArsMagica{

	public static final int KEYSTONE_INVENTORY_SIZE = 3;

	public ItemKeystone(){
		super();
		setMaxStackSize(1);
	}

	public void addCombination(ItemStack stack, String name, int[] metas){
		if (!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());

		int comboID = numCombinations(stack);
		boolean isNew = true;

		for (int i = 0; i < comboID; ++i){
			if (name.equals(stack.getTagCompound().getString("Combination_" + i + "_name"))){
				comboID = i;
				isNew = false;
				break;
			}
		}

		stack.getTagCompound().setString("Combination_" + comboID + "_name", name);
		stack.getTagCompound().setIntArray("Combination_" + comboID + "_metas", metas);

		if (isNew)
			stack.getTagCompound().setInteger("numKeystoneCombinations", comboID + 1);
	}

	public void removeCombination(ItemStack stack, String name){
		int c = numCombinations(stack);
		int removedIndex = -1;
		for (int i = 0; i < c; ++i){
			KeystoneCombination combo = getCombinationAt(stack, i);
			if (combo.name.equals(name)){
				removedIndex = i;
				break;
			}
		}

		if (removedIndex == -1)
			return;

		for (int i = removedIndex + 1; i < c; ++i){
			String tName = stack.getTagCompound().getString("Combination_" + i + "_name");
			int[] tMetas = stack.getTagCompound().getIntArray("Combination_" + i + "_metas");

			stack.getTagCompound().setString("Combination_" + (i - 1) + "_name", tName);
			stack.getTagCompound().setIntArray("Combination_" + (i - 1) + "_metas", tMetas);
		}

		stack.getTagCompound().removeTag("Combination_" + c + "_name");
		stack.getTagCompound().removeTag("Combination_" + c + "_metas");
		stack.getTagCompound().setInteger("numKeystoneCombinations", c - 1);
	}

	public int numCombinations(ItemStack stack){
		if (!stack.hasTagCompound()) return 0;
		return stack.getTagCompound().getInteger("numKeystoneCombinations");
	}

	public KeystoneCombination getCombinationAt(ItemStack stack, int index){
		if (!stack.hasTagCompound()) return null;

		if (numCombinations(stack) <= index) return null;

		String name = stack.getTagCompound().getString("Combination_" + index + "_name");
		int[] metas = stack.getTagCompound().getIntArray("Combination_" + index + "_metas");

		return new KeystoneCombination(name, metas);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand){
		if (player.isSneaking()){
			FMLNetworkHandler.openGui(player, ArsMagica2.instance, IDDefs.GUI_KEYSTONE, world, (int)player.posX, (int)player.posY, (int)player.posZ);
		}

		return new ActionResult<ItemStack>(EnumActionResult.PASS, player.getHeldItem(hand));
	}

	private ItemStack[] getMyInventory(ItemStack itemStack){
		return ReadFromStackTagCompound(itemStack);
	}

	public String getRecipeAsString(ItemStack keystoneStack){
		String s = "Recipe: ";
		for (ItemStack stack : getMyInventory(keystoneStack)){
			s += stack.getDisplayName().replace("Rune ", "") + " ";
		}
		return s;
	}

	public void UpdateStackTagCompound(ItemStack itemStack, ItemStack[] values){
		if (itemStack.getTagCompound() == null){
			itemStack.setTagCompound(new NBTTagCompound());
		}
		for (int i = 0; i < values.length; ++i){
			ItemStack stack = values[i];
			if (stack == null){
				itemStack.getTagCompound().setInteger("keystonemeta" + i, -1);
			}else{
				itemStack.getTagCompound().setInteger("keystonemeta" + i, stack.getItemDamage());
			}
		}
	}

	public ItemStack[] ReadFromStackTagCompound(ItemStack itemStack){
		if (itemStack.getTagCompound() == null){
			return new ItemStack[InventoryKeyStone.inventorySize];
		}
		ItemStack[] items = new ItemStack[InventoryKeyStone.inventorySize];
		for (int i = 0; i < items.length; ++i){
			int meta = 0;
			if (!itemStack.getTagCompound().hasKey("keystonemeta" + i)){
				items[i] = null;
				continue;
			}else if (itemStack.getTagCompound().getInteger("keystonemeta" + i) == -1){
				items[i] = null;
				continue;
			}else{
				meta = itemStack.getTagCompound().getInteger("keystonemeta" + i);
			}

			items[i] = new ItemStack(ItemDefs.rune, 1, meta);
		}
		return items;
	}

	public InventoryKeyStone ConvertToInventory(ItemStack keyStoneStack){
		InventoryKeyStone iks = new InventoryKeyStone();
		iks.SetInventoryContents(getMyInventory(keyStoneStack));
		return iks;
	}

	@Override
	public boolean getShareTag(){
		return true;
	}

	@Override
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List<String> par3List, boolean par4){
		ItemStack[] items = getMyInventory(par1ItemStack);

		String s = I18n.translateToLocal("am2.tooltip.open");
		par3List.add((new StringBuilder()).append("\2477").append(s).toString());

		if (items.length > 0){
			s = I18n.translateToLocal("am2.tooltip.runes") + ": ";
			par3List.add((new StringBuilder()).append("\2477").append(s).toString());
			s = "";
			for (int i = 0; i < KEYSTONE_INVENTORY_SIZE; ++i){
				if (items[i] == null) continue;
				s += items[i].getDisplayName().replace("Rune", "").trim() + " ";
			}
			if (s == "") s = I18n.translateToLocal("am2.tooltip.none");
			par3List.add((new StringBuilder()).append("\2477").append(s).toString());
		}
	}

	public long getKey(ItemStack keystoneStack){
		ItemStack[] inventory = getMyInventory(keystoneStack);
		if (inventory == null) return 0;
		return KeystoneUtilities.instance.getKeyFromRunes(inventory);
	}

	public class KeystoneCombination{
		public int[] metas;
		public String name;

		public KeystoneCombination(String name, int[] metas){
			this.metas = metas;
			this.name = name;
		}

		@Override
		public boolean equals(Object obj){
			if (obj instanceof KeystoneCombination){
				boolean match = ((KeystoneCombination)obj).metas.length == metas.length;
				if (!match) return false;

				for (int i = 0; i < this.metas.length; ++i){
					match &= (this.metas[i] == ((KeystoneCombination)obj).metas[i]);
				}

				return match;
			}
			return false;
		}

		@Override
		public int hashCode(){
			int sum = 0;
			for (int i : metas)
				sum += i;
			return sum;
		}
	}
}

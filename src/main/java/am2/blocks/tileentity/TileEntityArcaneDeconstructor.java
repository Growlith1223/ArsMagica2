package am2.blocks.tileentity;	

import am2.ArsMagica2;
import am2.api.ArsMagicaAPI;
import am2.api.blocks.IKeystoneLockable;
import am2.api.spell.AbstractSpellPart;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellModifier;
import am2.api.spell.SpellShape;
import am2.defs.ItemDefs;
import am2.particles.AMParticle;
import am2.particles.ParticleHoldPosition;
import am2.power.PowerNodeRegistry;
import am2.power.PowerTypes;
import am2.spell.component.Summon;
import am2.spell.shape.Binding;
import am2.utils.InventoryUtilities;
import am2.utils.RecipeUtils;
import am2.utils.SpellUtils;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class TileEntityArcaneDeconstructor extends TileEntityAMPower implements IInventory, ISidedInventory, IKeystoneLockable<TileEntityArcaneDeconstructor>{

	private int particleCounter;
	private static final float DECONSTRUCTION_POWER_COST = 1.25f; //power cost per tick
	private static final int DECONSTRUCTION_TIME = 200; //how long does it take to deconstruct something?
	private int current_deconstruction_time = 0; //how long have we been deconstructing something?

	private static final ArrayList<PowerTypes> validPowerTypes = Lists.newArrayList(PowerTypes.DARK);

	@SideOnly(Side.CLIENT)
	AMParticle radiant;

	private NonNullList<ItemStack> inventory = NonNullList.<ItemStack>withSize(getSizeInventory(), ItemStack.EMPTY);

	private ItemStack[] deconstructionRecipe;

	public TileEntityArcaneDeconstructor(){
		super(500);
	}

	@Override
	public boolean canRelayPower(PowerTypes type){
		return false;
	}

	@Override
	public int getChargeRate(){
		return 250;
	}

	@Override
	public void update(){
		super.update();

		if (world.isRemote){
			if (particleCounter == 0 || particleCounter++ > 1000){
				particleCounter = 1;
				radiant = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, "radiant", pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f);
				if (radiant != null){
					radiant.setMaxAge(1000);
					radiant.setRGBColorF(0.1f, 0.1f, 0.1f);
					radiant.setParticleScale(0.1f);
					radiant.AddParticleController(new ParticleHoldPosition(radiant, 1000, 1, false));
				}
			}
		}else{
			if (!isActive()){
				if (inventory.get(0) != ItemStack.EMPTY){
					current_deconstruction_time = 1;
				}
			}else{
				if (inventory.get(0).isEmpty()){
					current_deconstruction_time = 0;
					deconstructionRecipe = null;
					this.markDirty();
					//world.markAndNotifyBlock(pos, world.getChunkFromBlockCoords(pos), world.getBlockState(pos), world.getBlockState(pos), 2);
				}else{
					if (PowerNodeRegistry.For(world).checkPower(this, PowerTypes.DARK, DECONSTRUCTION_POWER_COST)){
						if (deconstructionRecipe == null){
							if (!getDeconstructionRecipe()){
								transferOrEjectItem(inventory.get(0));
								setInventorySlotContents(0, ItemStack.EMPTY);
							}
						}else{
							if (current_deconstruction_time++ >= DECONSTRUCTION_TIME){
							        if(getDeconstructionRecipe() == true){
									for (ItemStack stack : deconstructionRecipe){
										transferOrEjectItem(stack);
									}
								}
								deconstructionRecipe = null;
								decrStackSize(0, 1);
								current_deconstruction_time = 0;
							}
							if (current_deconstruction_time % 10 == 0)
								this.markDirty();
								//world.markAndNotifyBlock(pos, world.getChunkFromBlockCoords(pos), world.getBlockState(pos), world.getBlockState(pos), 2);
						}
						PowerNodeRegistry.For(world).consumePower(this, PowerTypes.DARK, DECONSTRUCTION_POWER_COST);
					}
				}
			}
		}
	}

	private boolean getDeconstructionRecipe(){
		ItemStack checkStack = getStackInSlot(0);
		ArrayList<ItemStack> recipeItems = new ArrayList<ItemStack>();
		if (checkStack.isEmpty())
			return false;
		if (checkStack.getItem()== ItemDefs.spell){
			int numStages = SpellUtils.numStages(checkStack);

			for (int i = 0; i < numStages; ++i){
				SpellShape shape = SpellUtils.getShapeForStage(checkStack, i);
				Object[] componentParts = shape.getRecipe();
				if (componentParts != null){
					for (Object o : componentParts){
						ItemStack stack = objectToItemStack(o);
						if (stack != ItemStack.EMPTY){
							if (stack.getItem() == ItemDefs.bindingCatalyst){
								stack.setItemDamage(((Binding)ArsMagicaAPI.getSpellRegistry().getObject(new ResourceLocation("arsmagica2", "binding"))).getBindingType(checkStack));
							}
							recipeItems.add(stack.copy());
						}
					}
				}
				ArrayList<SpellComponent> components = SpellUtils.getComponentsForStage(checkStack, i);
				for (SpellComponent component : components){
					componentParts = component.getRecipe();
					if (componentParts != null){
						for (Object o : componentParts){
							ItemStack stack = objectToItemStack(o);
							if (!stack.isEmpty()){
								if (stack.getItem() == ItemDefs.crystalPhylactery){
									ItemDefs.crystalPhylactery.setSpawnClass(stack,((Summon)ArsMagicaAPI.getSpellRegistry().getObject(new ResourceLocation("arsmagica2", "summon"))).getSummonType(checkStack));
									ItemDefs.crystalPhylactery.addFill(stack, 100);
								}
								recipeItems.add(stack.copy());
							}
						}
					}
				}
				ArrayList<SpellModifier> modifiers = SpellUtils.getModifiersForStage(checkStack, i);
				for (SpellModifier modifier : modifiers){
					componentParts = modifier.getRecipe();
					if (componentParts != null){
						for (Object o : componentParts){
							ItemStack stack = objectToItemStack(o);
							if (!stack.isEmpty())
								recipeItems.add(stack.copy());
						}
					}
				}
			}

			int numShapeGroups = SpellUtils.numShapeGroups(checkStack);
			for (int i = 0; i < numShapeGroups; ++i){
				ArrayList<AbstractSpellPart> parts = SpellUtils.getShapeGroupParts(checkStack, i);
				for (AbstractSpellPart entry : parts){
					Object[] componentParts = null;
					if (entry != null)
						componentParts = ((AbstractSpellPart)entry).getRecipe();
					if (componentParts != null){
						for (Object o : componentParts){
							ItemStack stack = objectToItemStack(o);
							if (!stack.isEmpty()){
								if (stack.getItem() == ItemDefs.bindingCatalyst){
									stack.setItemDamage(((Binding)ArsMagicaAPI.getSpellRegistry().getObject(new ResourceLocation("arsmagica2", "binding"))).getBindingType(checkStack));
								}
								recipeItems.add(stack.copy());
							}
						}
					}
				}
			}

			deconstructionRecipe = recipeItems.toArray(new ItemStack[recipeItems.size()]);
			return true;
		}else{
			IRecipe recipe = RecipeUtils.getRecipeFor(checkStack);
			if (recipe == null)
				return false;
			Object[] recipeParts = RecipeUtils.getRecipeItems(recipe);
			if (recipeParts != null && !checkStack.isEmpty() && recipe.getRecipeOutput() != null){
				if (recipe.getRecipeOutput().getItem() == checkStack.getItem() && recipe.getRecipeOutput().getItemDamage() == checkStack.getItemDamage() && recipe.getRecipeOutput().getCount() > 1)
					return false;

				for (Object o : recipeParts){
					ItemStack stack = objectToItemStack(o);
					if (!stack.isEmpty() && !stack.getItem().hasContainerItem(stack)){
						stack.setCount(1);
						recipeItems.add(stack.copy());
					}
				}
			}
			deconstructionRecipe = recipeItems.toArray(new ItemStack[recipeItems.size()]);
			return true;
		}
	}

	@SuppressWarnings({ "rawtypes" })
	private ItemStack objectToItemStack(Object o){
		ItemStack output = ItemStack.EMPTY;
		if (o instanceof ItemStack)
			output = (ItemStack)o;
		else if (o instanceof Item)
			output = new ItemStack((Item)o);
		else if (o instanceof Block)
			output = new ItemStack((Block)o);
		else if (o instanceof List)
			output = objectToItemStack(((List) o).get(0));

		if (!output.isEmpty()){
			if (output.getCount() == 0)
				output.setCount(1);
		}

		return output;
	}

	private void transferOrEjectItem(ItemStack stack){
		if (world.isRemote)
			return;

		for (int i = -1; i <= 1; ++i){
			for (int j = -1; j <= 1; ++j){
				for (int k = -1; k <= 1; ++k){
					if (i == 0 && j == 0 && k == 0)
						continue;
					TileEntity te = world.getTileEntity(pos.add(i, j, k));
					if (te != null && te instanceof IInventory){
						for (EnumFacing side : EnumFacing.values()){
							if (InventoryUtilities.mergeIntoInventory((IInventory)te, stack, stack.getCount(), side))
								return;
						}
					}
				}
			}
		}

		//eject the remainder
		EntityItem item = new EntityItem(world);
		item.setPosition(pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5);
		item.setEntityItemStack(stack);
		world.spawnEntity(item);
	}

	public boolean isActive(){
		return current_deconstruction_time > 0;
	}

	@Override
	public int getSizeInventory(){
		return 16;
	}

	@Override
	public ItemStack getStackInSlot(int var1){
		if (var1 >= inventory.size()){
			return ItemStack.EMPTY;
		}
		return inventory.get(var1);
	}

	@Override
	public ItemStack decrStackSize(int i, int j){
		if (!inventory.get(i).isEmpty()){
			if (inventory.get(i).getCount() <= j){
				ItemStack itemstack = inventory.get(i);
				inventory.set(i, ItemStack.EMPTY);
				return itemstack;
			}
			ItemStack itemstack1 = inventory.get(i).splitStack(j);
			if (inventory.get(i).getCount() == 0){
				inventory.set(i, ItemStack.EMPTY);
			}
			return itemstack1;
		}else{
			return ItemStack.EMPTY;
		}
	}

	@Override
	public ItemStack removeStackFromSlot(int i){
		if (!inventory.get(i).isEmpty()){
			ItemStack itemstack = inventory.get(i);
			inventory.set(i, ItemStack.EMPTY);
			return itemstack;
		}else{
			return ItemStack.EMPTY;
		}
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack){
		inventory.set(i, itemstack);
		if (!itemstack.isEmpty() && itemstack.getCount() > getInventoryStackLimit()){
			itemstack.setCount(getInventoryStackLimit());
		}
	}

	@Override
	public String getName(){
		return "ArcaneDeconstructor";
	}

	@Override
	public boolean hasCustomName(){
		return false;
	}

	@Override
	public int getInventoryStackLimit(){
		return 64;
	}

	@Override
	public void openInventory(EntityPlayer player){
	}

	@Override
	public void closeInventory(EntityPlayer player){
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack){
		return i <= 9;
	}

	@Override
	public int[] getSlotsForFace(EnumFacing var1){
		return new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemstack, EnumFacing j){
		return i == 0;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, EnumFacing j){
		return i >= 1 && i <= 9;
	}

	@Override
	public ItemStack[] getRunesInKey(){
		return new ItemStack[]{
				inventory.get(13),
				inventory.get(14),
				inventory.get(15)
		};
	}

	@Override
	public boolean keystoneMustBeHeld(){
		return false;
	}

	@Override
	public boolean keystoneMustBeInActionBar(){
		return false;
	}

	@Override
	public List<PowerTypes> getValidPowerTypes(){
		return validPowerTypes;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound){
		super.readFromNBT(nbttagcompound);
		NBTTagList nbttaglist = nbttagcompound.getTagList("DeconstructorInventory", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < nbttaglist.tagCount(); i++){
			String tag = String.format("ArrayIndex", i);
			NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbttaglist.getCompoundTagAt(i);
			byte byte0 = nbttagcompound1.getByte(tag);
			if (byte0 >= 0 && byte0 < inventory.size()){
				inventory.set(byte0, new ItemStack(nbttagcompound1));
			}
		}

		this.current_deconstruction_time = nbttagcompound.getInteger("DeconstructionTime");

		if (current_deconstruction_time > 0)
			getDeconstructionRecipe();
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound){
		super.writeToNBT(nbttagcompound);
		NBTTagList nbttaglist = new NBTTagList();
		for (int i = 0; i < inventory.size(); i++){
			if (!inventory.get(i).isEmpty()){
				String tag = String.format("ArrayIndex", i);
				NBTTagCompound nbttagcompound1 = new NBTTagCompound();
				nbttagcompound1.setByte(tag, (byte)i);
				inventory.get(i).writeToNBT(nbttagcompound1);
				nbttaglist.appendTag(nbttagcompound1);
			}
		}

		nbttagcompound.setTag("DeconstructorInventory", nbttaglist);

		nbttagcompound.setInteger("DeconstructionTime", current_deconstruction_time);
		return nbttagcompound;
	}

	public int getProgressScaled(int i){
		return current_deconstruction_time * i / DECONSTRUCTION_TIME;
	}

	@Override
	public ITextComponent getDisplayName() {
		return null;
	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {

		
	}

	@Override
	public int getFieldCount() {

		return 0;
	}

	@Override
	public void clear() {

		
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack itemstack : this.inventory)
        {
            if (!itemstack.isEmpty())
            {
                return false;
            }
        }

        return true;
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		return true;
	}
}

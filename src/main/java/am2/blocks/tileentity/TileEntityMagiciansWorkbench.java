package am2.blocks.tileentity;

import java.util.LinkedList;

import am2.api.blocks.IKeystoneLockable;
import am2.blocks.BlockMagiciansWorkbench;
import am2.packet.AMDataWriter;
import am2.packet.AMNetHandler;
import am2.packet.AMPacketIDs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Constants;

public class TileEntityMagiciansWorkbench extends TileEntity implements ITickable, IKeystoneLockable<TileEntityMagiciansWorkbench>, ISidedInventory{

	private NonNullList<ItemStack> inventory = NonNullList.<ItemStack>withSize(this.getSizeInventory(), ItemStack.EMPTY);
	public IInventory firstCraftResult;
	public IInventory secondCraftResult;

	private final LinkedList<RememberedRecipe> rememberedRecipes;
	private byte upgradeState = 0;
	public static final byte UPG_CRAFT = 0x1;
	public static final byte UPG_ADJ_INV = 0x2;
	private int numPlayersUsing = 0;
	private float drawerOffset = 0;
	private float prevDrawerOffset = 0;
	private static final float drawerIncrement = 0.05f;
	private static final float drawerMax = 0.5f;
	private static final float drawerMin = 0.0f;

	public TileEntityMagiciansWorkbench(){
		inventory = NonNullList.<ItemStack>withSize(this.getSizeInventory(), ItemStack.EMPTY);
		firstCraftResult = new InventoryCraftResult();
		secondCraftResult = new InventoryCraftResult();

		rememberedRecipes = new LinkedList<RememberedRecipe>();
	}

	@Override
	public void update(){
		setPrevDrawerOffset(getDrawerOffset());

		if (numPlayersUsing > 0){
			if (getDrawerOffset() == drawerMin){
				//sound could go here
			}
			if (getDrawerOffset() < drawerMax){
				setDrawerOffset(getDrawerOffset() + drawerIncrement);
			}else{
				setDrawerOffset(drawerMax);
			}
		}else{
			if (getDrawerOffset() == drawerMax){
				this.world.playSound(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D, SoundEvents.BLOCK_CHEST_CLOSE, SoundCategory.BLOCKS, 0.5F, this.world.rand.nextFloat() * 0.1F + 0.9F, true);
			}
			if (getDrawerOffset() - drawerIncrement > drawerMin){
				setDrawerOffset(getDrawerOffset() - drawerIncrement);
			}else{
				setDrawerOffset(drawerMin);
			}
		}
	}

	@Override
	public boolean receiveClientEvent(int par1, int par2){
		if (par1 == 1){
			this.numPlayersUsing = par2;
			return true;
		}else{
			return super.receiveClientEvent(par1, par2);
		}
	}

	public float getPrevDrawerOffset(){
		return prevDrawerOffset;
	}

	public void setPrevDrawerOffset(float prevDrawerOffset){
		this.prevDrawerOffset = prevDrawerOffset;
	}

	public float getDrawerOffset(){
		return drawerOffset;
	}

	public void setDrawerOffset(float drawerOffset){
		this.drawerOffset = drawerOffset;
	}

	@Override
	public void openInventory(EntityPlayer player){
		if (this.numPlayersUsing < 0){
			this.numPlayersUsing = 0;
		}

		++this.numPlayersUsing;
		this.world.addBlockEvent(pos, this.getBlockType(), 1, this.numPlayersUsing);
	}

	@Override
	public void closeInventory(EntityPlayer player){
		if (this.getBlockType() != null && this.getBlockType() instanceof BlockMagiciansWorkbench){
			--this.numPlayersUsing;
			this.world.addBlockEvent(pos, this.getBlockType(), 1, this.numPlayersUsing);
		}
	}

	public boolean getUpgradeStatus(byte flag){
		return (upgradeState & flag) == flag;
	}

	public void setUpgradeStatus(byte flag, boolean set){
		if (set)
			upgradeState |= flag;
		else
			upgradeState &= ~flag;

		if (!world.isRemote)
			world.markAndNotifyBlock(pos, world.getChunkFromBlockCoords(pos), world.getBlockState(pos), world.getBlockState(pos), 2);
	}

	public void rememberRecipe(ItemStack output, ItemStack[] recipeItems, boolean is2x2){
		for (RememberedRecipe recipe : rememberedRecipes){
			if (recipe.output.isItemEqual(output))
				return;
		}
		if (!popRecipe()){
			return;
		}

		for (ItemStack stack : recipeItems)
			if (stack != null)
				stack.setCount(1);

		rememberedRecipes.add(new RememberedRecipe(output, recipeItems, is2x2));

		world.markAndNotifyBlock(pos, world.getChunkFromBlockCoords(pos), world.getBlockState(pos), world.getBlockState(pos), 2);
	}

	private boolean popRecipe(){

		if (rememberedRecipes.size() < 8)
			return true;

		int index = 0;
		while (index < rememberedRecipes.size()){
			if (!rememberedRecipes.get(index).isLocked){
				rememberedRecipes.remove(index);
				return true;
			}
			index++;
		}

		return false;
	}

	public LinkedList<RememberedRecipe> getRememberedRecipeItems(){
		return rememberedRecipes;
	}

	@Override
	public int getSizeInventory(){
		return 48;
	}

	@Override
	public ItemStack getStackInSlot(int i){
		if (i < 0 || i >= getSizeInventory())
			return ItemStack.EMPTY;
		return inventory.get(i);
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
			return null;
		}
	}

	@Override
	public ItemStack removeStackFromSlot(int i){
		if (!inventory.get(i).isEmpty()){
			ItemStack itemstack = inventory.get(i);
			inventory.set(i, ItemStack.EMPTY);
			return itemstack;
		}else{
			return null;
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
		return "Magician's Workbench";
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
	public boolean isUsableByPlayer(EntityPlayer entityplayer){
		if (world.getTileEntity(pos) != this){
			return false;
		}
		return entityplayer.getDistanceSqToCenter(pos) <= 64D;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack){
		if (i > getStorageStart())
			return true;
		return false;
	}

	public class RememberedRecipe{
		public final ItemStack output;
		public final ItemStack[] components;
		private boolean isLocked;
		public final boolean is2x2;

		public RememberedRecipe(ItemStack output, ItemStack[] components, boolean is2x2){
			this.output = output;
			this.components = components;
			this.isLocked = false;
			this.is2x2 = is2x2;
		}

		public void lock(){
			this.isLocked = true;
		}

		public void unlock(){
			this.isLocked = false;
		}

		public boolean isLocked(){
			return isLocked;
		}
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket(){
		NBTTagCompound compound = new NBTTagCompound();
		this.writeToNBT(compound);
		SPacketUpdateTileEntity packet = new SPacketUpdateTileEntity(pos, 0, compound);
		return packet;
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt){
		this.readFromNBT(pkt.getNbtCompound());
	}

	public void setRecipeLocked(int index, boolean locked){
		if (index >= 0 && index < rememberedRecipes.size())
			rememberedRecipes.get(index).isLocked = locked;

		if (world.isRemote){
			AMDataWriter writer = new AMDataWriter();
			writer.add(pos.getX());
			writer.add(pos.getY());
			writer.add(pos.getZ());
			writer.add(index);
			writer.add(locked);
			AMNetHandler.INSTANCE.sendPacketToServer(AMPacketIDs.M_BENCH_LOCK_RECIPE, writer.generate());
		}
	}

	public void toggleRecipeLocked(int index){
		if (index >= 0 && index < rememberedRecipes.size())
			setRecipeLocked(index, !rememberedRecipes.get(index).isLocked);
	}

	public int getStorageStart(){
		return 18;
	}

	public int getStorageSize(){
		return 27;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound){
		super.readFromNBT(nbttagcompound);
		NBTTagList nbttaglist = nbttagcompound.getTagList("ArcaneReconstructorInventory", Constants.NBT.TAG_COMPOUND);
		inventory = NonNullList.<ItemStack>withSize(this.getSizeInventory(), ItemStack.EMPTY);
		for (int i = 0; i < nbttaglist.tagCount(); i++){
			String tag = String.format("ArrayIndex", i);
			NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbttaglist.getCompoundTagAt(i);
			byte byte0 = nbttagcompound1.getByte(tag);
			if (byte0 >= 0 && byte0 < inventory.size()){
				inventory.set(byte0, new ItemStack(nbttagcompound1));
			}
		}

		NBTTagList recall = nbttagcompound.getTagList("rememberedRecipes", Constants.NBT.TAG_COMPOUND);
		rememberedRecipes.clear();
		for (int i = 0; i < recall.tagCount(); ++i){
			NBTTagCompound rememberedRecipe = (NBTTagCompound)recall.getCompoundTagAt(i);
			ItemStack output = new ItemStack(rememberedRecipe);
			boolean is2x2 = rememberedRecipe.getBoolean("is2x2");
			NBTTagList componentNBT = rememberedRecipe.getTagList("components", Constants.NBT.TAG_COMPOUND);
			ItemStack[] components = new ItemStack[componentNBT.tagCount()];
			for (int n = 0; n < componentNBT.tagCount(); ++n){
				NBTTagCompound componentTAG = (NBTTagCompound)componentNBT.getCompoundTagAt(n);
				if (componentTAG.getBoolean("componentExisted")){
					ItemStack component = new ItemStack(componentTAG);
					components[n] = component;
				}else{
					components[n] = null;
				}
			}

			RememberedRecipe rec = new RememberedRecipe(output, components, is2x2);
			rec.isLocked = rememberedRecipe.getBoolean("isLocked");
			rememberedRecipes.add(rec);
		}

		this.upgradeState = nbttagcompound.getByte("upgradestate");
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

		nbttagcompound.setTag("ArcaneReconstructorInventory", nbttaglist);

		//remembered recipes
		NBTTagList recall = new NBTTagList();
		for (RememberedRecipe recipe : rememberedRecipes){
			try{
				NBTTagCompound output = new NBTTagCompound();
				recipe.output.writeToNBT(output);
				output.setBoolean("is2x2", recipe.is2x2);
				NBTTagList components = new NBTTagList();
				for (int i = 0; i < recipe.components.length; ++i){
					NBTTagCompound component = new NBTTagCompound();
					component.setBoolean("componentExisted", recipe.components[i] != null);
					if (recipe.components[i] != null)
						recipe.components[i].writeToNBT(component);
					components.appendTag(component);
				}
				output.setTag("components", components);
				output.setBoolean("isLocked", recipe.isLocked);
				recall.appendTag(output);
			}catch (Throwable t){
				//no log, as this is likely due to a mod being removed and the recipe no longer exists.
			}
		}

		nbttagcompound.setTag("rememberedRecipes", recall);
		nbttagcompound.setByte("upgradestate", upgradeState);
		return nbttagcompound;
	}

	@Override
	public ItemStack[] getRunesInKey(){
		ItemStack[] runes = new ItemStack[3];
		runes[0] = inventory.get(45);
		runes[1] = inventory.get(46);
		runes[2] = inventory.get(47);
		return runes;
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
	public int[] getSlotsForFace(EnumFacing var1){
		int[] slots = new int[getStorageSize()];
		for (int i = 0; i < slots.length; ++i){
			slots[i] = i + getStorageStart();
		}
		return slots;
	}


	@Override
	public boolean canInsertItem(int i, ItemStack itemstack, EnumFacing j){
		if (i > getStorageStart())
			return true;
		return false;
	}


	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, EnumFacing j){
		if (i > getStorageStart())
			return true;
		return false;
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
	public ITextComponent getDisplayName() {

		return null;
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
}

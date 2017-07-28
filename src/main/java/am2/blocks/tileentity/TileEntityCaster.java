package am2.blocks.tileentity;

import am2.entity.EntityDummyCaster;
import am2.extensions.EntityExtension;
import am2.items.ItemSpellBase;
import am2.power.PowerTypes;
import am2.utils.DummyEntityPlayer;
import am2.utils.SpellUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;

public class TileEntityCaster extends TileEntityAMPoweredContainer implements IInventory, ITileEntityAMBase{
	
	private ItemStack spellStack = null;
	private EntityPlayer caster = null;
	private String placedByName = null;

	private int numTriggers = 1;
	private boolean isPermanent = false;
	private boolean dirty;

	public TileEntityCaster(int capacity) {
		super(1000);
	}

	@Override
	public boolean canRelayPower(PowerTypes type) {
		return false;
	}

	@Override
	public int getChargeRate() {
		return 200;
	}


	public void setSpellStack(ItemStack spellStack){
		this.spellStack = spellStack.copy();
	}
	
	public ItemStack getSpellStack() {
		return spellStack;
	}

	public void setNumTriggers(int triggers){
		this.numTriggers = triggers;
	}

	public int getNumTriggers(){
		return this.numTriggers;
	}

	public void setPermanent(boolean permanent){
		this.isPermanent = permanent;
	}

	public boolean getPermanent(){
		return this.isPermanent;
	}

	private void prepForActivate(){
		if (caster == null){
			caster = DummyEntityPlayer.fromEntityLiving(new EntityDummyCaster(world));
			EntityExtension.For(caster).setMagicLevelWithMana(99);
		}
	}
	
	public boolean canApply(EntityLivingBase entity) {
		if (spellStack == null) return false;
		prepForActivate();
		if (entity.getName().equals(placedByName)) return false;
		return true;
	}

	public boolean applySpellEffect(EntityLivingBase target){
		if (spellStack == null) return false;
		if (!canApply(target)) return false;
		prepForActivate();
		SpellUtils.applyStackStage(spellStack, caster, target, target.posX, target.posY, target.posZ, null, world, false, false, 0);
		return true;
	}

	public void setPlacedBy(EntityLivingBase caster){
		if (caster instanceof EntityPlayer) this.placedByName = ((EntityPlayer)caster).getName();
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound){
		if (placedByName != null)
			compound.setString("placedByName", placedByName);
		if (spellStack != null)
			compound.setTag("spellStack", spellStack.writeToNBT(new NBTTagCompound()));
		compound.setInteger("numTrigger", numTriggers);
		compound.setBoolean("permanent", isPermanent);
		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound){
		if (compound.hasKey("placedByName"))
			placedByName = compound.getString("placedByName");
		if (compound.hasKey("spellStack"))
			spellStack = new ItemStack(compound.getCompoundTag("spellStack"));
		numTriggers = compound.getInteger("numTrigger");
		isPermanent = compound.getBoolean("permanent");
	}
	
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(pos, 0, writeToNBT(new NBTTagCompound()));
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		readFromNBT(pkt.getNbtCompound());
	}

	@Override
	public void update() {
		world.markAndNotifyBlock(pos, world.getChunkFromBlockCoords(pos), world.getBlockState(pos), world.getBlockState(pos), 2);
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public int getSizeInventory() {
		return 1;
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return stack.getItem() instanceof ItemSpellBase;
	}

	@Override
	public void markDirty() {
		markForUpdate();
		super.markDirty();
	}

	@Override
	public void markForUpdate() {
		this.dirty = true;
	}

	@Override
	public boolean needsUpdate() {
		return this.dirty;
	}

	@Override
	public void clean() {
		this.dirty = false;
	}

	@Override
	public int[] getSlotsForFace(EnumFacing side) {
		switch(side){
		case UP: return new int[]{0};
		case DOWN: return new int[]{0};
		default: return new int[]{1,2,3};
		}
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemStackIn, EnumFacing direction) {
		return inventory.get(i).isEmpty();
	}

	@Override
	public boolean canExtractItem(int i, ItemStack stack, EnumFacing direction) {
		return !inventory.get(i).isEmpty();
	}

	@Override
	public ItemStack removeStackFromSlot(int i) {
		if (!inventory.get(i).isEmpty()){
			ItemStack itemstack = inventory.get(i);
			inventory.set(i, ItemStack.EMPTY);
			return itemstack;
		}else{
			return ItemStack.EMPTY;
		}
	}

	@Override
	public void openInventory(EntityPlayer player) {
	}

	@Override
	public void closeInventory(EntityPlayer player) {
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


}

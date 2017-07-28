package am2.entity;

import com.google.common.base.Optional;

import am2.ArsMagica2;
import am2.api.math.AMVector3;
import am2.blocks.tileentity.TileEntityCraftingAltar;
import am2.defs.AMSounds;
import am2.entity.ai.EntityAISpellmaking;
import am2.particles.AMParticle;
import am2.particles.ParticleFloatUpward;
import am2.proxy.ShadowSkinHelper;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityShadowHelper extends EntityLiving{

	private static final DataParameter<String> DW_MIMIC_USER = EntityDataManager.createKey(EntityShadowHelper.class, DataSerializers.STRING); //who are we going to mimic (MC skin)?	
	private static final DataParameter<ItemStack> DW_SEARCH_ITEM = EntityDataManager.createKey(EntityShadowHelper.class, DataSerializers.OPTIONAL_ITEM_STACK); //what are we currently looking for?
	private static final DataParameter<Integer> DW_TRANS_LOC_X = EntityDataManager.createKey(EntityShadowHelper.class, DataSerializers.VARINT); //x-coordinate of search
	private static final DataParameter<Integer> DW_TRANS_LOC_Y = EntityDataManager.createKey(EntityShadowHelper.class, DataSerializers.VARINT); //y-coordinate of search
	private static final DataParameter<Integer> DW_TRANS_LOC_Z = EntityDataManager.createKey(EntityShadowHelper.class, DataSerializers.VARINT); //z-coordinate of search
	private static final DataParameter<ItemStack> DW_HELD_ITEM = EntityDataManager.createKey(EntityShadowHelper.class, DataSerializers.OPTIONAL_ITEM_STACK); //current held item
	private static final DataParameter<Integer> DW_DROP_LOC_X = EntityDataManager.createKey(EntityShadowHelper.class, DataSerializers.VARINT); //x-coordinate of search
	private static final DataParameter<Integer> DW_DROP_LOC_Y = EntityDataManager.createKey(EntityShadowHelper.class, DataSerializers.VARINT); //y-coordinate of search
	private static final DataParameter<Integer> DW_DROP_LOC_Z = EntityDataManager.createKey(EntityShadowHelper.class, DataSerializers.VARINT); //z-coordinate of search

	private TileEntityCraftingAltar altarTarget = null;
	private String lastDWString = "";

	@SideOnly(Side.CLIENT)
	private ShadowSkinHelper skinHelper;

	public EntityShadowHelper(World world){
		super(world);
		initAI();
	}

	@Override
	public void onDeath(DamageSource par1DamageSource){
		super.onDeath(par1DamageSource);
		if (world.isRemote){
			spawnParticles();
			world.playSound(posX, posY, posZ, AMSounds.CRAFTING_ALTAR_CREATE_SPELL, SoundCategory.NEUTRAL, 1.0f, 1.0f, true);
		}
	}

	private void spawnParticles(){
		if (world.isRemote){
			for (int i = 0; i < 25 * ArsMagica2.config.getGFXLevel() + 1; ++i){
				AMParticle particle = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, "arcane", posX, posY, posZ);
				if (particle != null){
					particle.addRandomOffset(1, 1, 1);
					particle.AddParticleController(new ParticleFloatUpward(particle, 0, 0.02f + getRNG().nextFloat() * 0.2f, 1, false));
					particle.setIgnoreMaxAge(false);
					particle.setMaxAge(20 + getRNG().nextInt(20));
				}
			}
		}
	}

	@Override
	protected SoundEvent getDeathSound(){
		return null;
	}

	@Override
	protected boolean canDespawn(){
		return false;
	}

	@Override
	protected void onDeathUpdate(){
		this.setDead();
	}

	@Override
	protected void entityInit(){
		super.entityInit();
		this.dataManager.register(DW_MIMIC_USER, "");
		this.dataManager.register(DW_SEARCH_ITEM, new ItemStack(Items.APPLE));
		this.dataManager.register(DW_TRANS_LOC_X, 0);
		this.dataManager.register(DW_TRANS_LOC_Y, 0);
		this.dataManager.register(DW_TRANS_LOC_Z, 0);
		this.dataManager.register(DW_HELD_ITEM, new ItemStack(Items.PAPER));
		this.dataManager.register(DW_DROP_LOC_X, 0);
		this.dataManager.register(DW_DROP_LOC_Y, 0);
		this.dataManager.register(DW_DROP_LOC_Z, 0);
	}

	public void setSearchLocationAndItem(AMVector3 location, ItemStack item){
		if (this.world.isRemote) return;
		this.dataManager.set(DW_SEARCH_ITEM, item);
		this.dataManager.set(DW_TRANS_LOC_X, (int)location.x);
		this.dataManager.set(DW_TRANS_LOC_Y, (int)location.y);
		this.dataManager.set(DW_TRANS_LOC_Z, (int)location.z);
	}

	public void setDropoffLocation(AMVector3 location){
		this.dataManager.set(DW_DROP_LOC_X, (int)location.x);
		this.dataManager.set(DW_DROP_LOC_Y, (int)location.y);
		this.dataManager.set(DW_DROP_LOC_Z, (int)location.z);
	}

	public AMVector3 getSearchLocation(){
		return new AMVector3(this.dataManager.get(DW_TRANS_LOC_X), this.dataManager.get(DW_TRANS_LOC_Y), this.dataManager.get(DW_TRANS_LOC_Z));
	}

	public AMVector3 getDropLocation(){
		return new AMVector3(this.dataManager.get(DW_DROP_LOC_X), this.dataManager.get(DW_DROP_LOC_Y), this.dataManager.get(DW_DROP_LOC_Z));
	}

	public ItemStack getSearchItem(){
		return this.dataManager.get(DW_SEARCH_ITEM);
	}

	public void setHeldItem(ItemStack item){
		this.dataManager.set(DW_HELD_ITEM, (item));
	}

	public void setMimicUser(String userName){
		this.dataManager.set(DW_MIMIC_USER, userName);
	}

	public String getMimicUser(){
		return this.dataManager.get(DW_MIMIC_USER);
	}

	public boolean hasSearchLocation(){
		return !this.getSearchLocation().equals(AMVector3.zero());
	}

	public TileEntityCraftingAltar getAltarTarget(){
		return this.altarTarget;
	}

	public void setAltarTarget(TileEntityCraftingAltar target){
		this.altarTarget = target;
	}

	private void initAI(){
		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(1, new EntityAIOpenDoor(this, true));
		this.tasks.addTask(2, new EntityAISpellmaking(this));
	}

	@Override
	public ItemStack getHeldItemMainhand(){
		return this.dataManager.get(DW_HELD_ITEM);
	}

	@Override
	public void onUpdate(){
		super.onUpdate();
		if (world != null && world.isRemote && skinHelper == null) {
			this.skinHelper = new ShadowSkinHelper();
			spawnParticles();
		}
		
		if (this.world.isRemote){
			if (this.getMimicUser() != lastDWString){
				lastDWString = getMimicUser();
				this.skinHelper.setupCustomSkin(lastDWString);
			}
		}
		if (!world.isRemote && (altarTarget == null || !altarTarget.isCrafting())){
			this.unSummon();
		}
	}

	@Override
	protected SoundEvent getHurtSound(){
		return null;
	}

	public void unSummon(){
		this.attackEntityFrom(DamageSource.GENERIC, 5000);
	}

	public ResourceLocation getLocationSkin(){
		return this.skinHelper.getLocationSkin();
	}

	public ThreadDownloadImageData getTextureSkin(){
		return this.skinHelper.getTextureSkin();
	}
}

package am2.bosses;

import am2.api.ArsMagicaAPI;
import am2.api.affinity.Affinity;
import am2.api.sources.DamageSourceFrost;
import am2.api.sources.DamageSourceLightning;
import am2.bosses.ai.EntityAICastSpell;
import am2.bosses.ai.EntityAIChaosWaterBolt;
import am2.bosses.ai.EntityAICloneSelf;
import am2.bosses.ai.EntityAISpinAttack;
import am2.defs.AMSounds;
import am2.defs.ItemDefs;
import am2.extensions.EntityExtension;
import am2.packet.AMNetHandler;
import am2.utils.NPCSpells;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.BossInfo.Color;
import net.minecraft.world.World;

public class EntityWaterGuardian extends AM2Boss {

	private EntityWaterGuardian master;
	private final EntityWaterGuardian[] clones;
	private float orbitRotation;
	private boolean uberSpinAvailable = false;

	private static final DataParameter<Boolean> IS_CLONE = EntityDataManager.createKey(EntityWaterGuardian.class, DataSerializers.BOOLEAN);

	public float spinRotation = 0;

	public EntityWaterGuardian(World par1World){
		super(par1World);
		currentAction = BossActions.IDLE;
		master = null;
		clones = new EntityWaterGuardian[2];
		this.setSize(1.0f, 2.0f);
		EntityExtension.For(this).setMagicLevelWithMana(10);
	}

	@Override
	protected void applyEntityAttributes(){
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(75D);
	}

	public void setClones(EntityWaterGuardian clone1, EntityWaterGuardian clone2){
		clones[0] = clone1;
		clones[1] = clone2;
	}

	private boolean hasClones(){
		return clones[0] != null || clones[1] != null;
	}

	public void clearClones(){
		if (clones[0] != null){
			clones[0].setDead();
		}
		if (clones[1] != null){
			clones[1].setDead();
		}
		clones[0] = null;
		clones[1] = null;
	}

	private void enableUberAttack(){
		uberSpinAvailable = true;
	}

	public void setMaster(EntityWaterGuardian master){
		dataManager.set(IS_CLONE, true);
		this.master = master;
	}

	public boolean isClone(){
		return dataManager.get(IS_CLONE);
	}

	public void clearMaster(){
		this.master = null;
	}

	@Override
	protected void initSpecificAI(){
		this.tasks.addTask(2, new EntityAIChaosWaterBolt(this));
		this.tasks.addTask(3, new EntityAICloneSelf(this));
		this.tasks.addTask(4, new EntityAICastSpell<EntityWaterGuardian>(this, NPCSpells.instance.waterBolt, 12, 23, 5, BossActions.CASTING));
		this.tasks.addTask(3, new EntityAISpinAttack(this, 0.5f, 4));
	}

	@Override
	public void onUpdate(){

		if (currentAction == BossActions.CASTING){
			uberSpinAvailable = false;
		}

		if (!world.isRemote && uberSpinAvailable && currentAction != BossActions.CASTING && currentAction != BossActions.IDLE){
			setCurrentAction(BossActions.IDLE);
		}

		if (!world.isRemote && isClone() && (master == null || ticksExisted > 400)){
			setDead();
		}

		if (world.isRemote){
			updateRotations();
		}
		super.onUpdate();
	}

	@Override
	protected void entityInit(){
		super.entityInit();

		this.dataManager.register(IS_CLONE, false);
	}

	private void updateRotations(){
		if (!isClone())
			orbitRotation += 2f;
		else
			orbitRotation -= 2f;
		orbitRotation %= 360;

		if (this.getCurrentAction() == BossActions.SPINNING || this.getCurrentAction() == BossActions.CASTING){
			this.spinRotation = (this.spinRotation - 30) % 360;
		}
	}

	public float getOrbitRotation(){
		return orbitRotation;
	}

	@Override
	public void setCurrentAction(BossActions action){
		super.setCurrentAction(action);
		this.spinRotation = 0;

		if (!world.isRemote){
			AMNetHandler.INSTANCE.sendActionUpdateToAllAround(this);
		}
	}

	@Override
	public boolean attackEntityFrom(DamageSource par1DamageSource, float par2){
		if (par1DamageSource.getSourceOfDamage() instanceof EntityWaterGuardian)
			return false;
		if (isClone() && master != null){
			master.enableUberAttack();
			master.clearClones();
		}else if (hasClones()){
			clearClones();
		}

		if (!isClone() && rand.nextInt(10) < 6){
			world.playSound(posX, posY, posZ, getAmbientSound(), SoundCategory.HOSTILE, 1.0f, 0.4f + rand.nextFloat() * 0.6f, false);
			return false;
		}

		return super.attackEntityFrom(par1DamageSource, par2);
	}

	@Override
	protected float modifyDamageAmount(DamageSource source, float damageAmt){
		if (source instanceof DamageSourceLightning)
			damageAmt *= 2.0f;
		if (source.getSourceOfDamage() != null && source.getSourceOfDamage() instanceof EntityWaterGuardian)
			damageAmt = 0;
		if (source instanceof DamageSourceFrost)
			damageAmt = 0;

		return damageAmt;
	}

	@Override
	public boolean isActionValid(BossActions action){
		if (uberSpinAvailable && action != BossActions.CASTING) return false;
		if (action == BossActions.CASTING){
			return uberSpinAvailable;
		}
		if (action == BossActions.CLONE){
			return !isClone();
		}
		return true;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound par1nbtTagCompound){
		super.writeEntityToNBT(par1nbtTagCompound);

		par1nbtTagCompound.setBoolean("isClone", isClone());
	}

	@Override
	public int getTotalArmorValue(){
		return 10;
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound par1nbtTagCompound){
		super.readEntityFromNBT(par1nbtTagCompound);

		dataManager.set(IS_CLONE, par1nbtTagCompound.getBoolean("isClone"));
	}


	@Override
	protected void dropFewItems(boolean par1, int par2){
		if (par1)
			this.entityDropItem(new ItemStack(ItemDefs.infinityOrb, 1, 0), 0.0f);

		int i = rand.nextInt(4);

		for (int j = 0; j < i; j++){
			this.entityDropItem(new ItemStack(ItemDefs.essence, 1, ArsMagicaAPI.getAffinityRegistry().getId(Affinity.WATER)), 0.0f);
		}
		
		i = rand.nextInt(10);

		if (i < 3){
			this.entityDropItem(ItemDefs.waterOrbsEnchanted.copy(), 0.0f);
		}
	}

	@Override
	protected SoundEvent getHurtSound(){
		return AMSounds.WATER_GUARDIAN_HIT;
	}

	@Override
	protected SoundEvent getDeathSound(){
		return AMSounds.WATER_GUARDIAN_DEATH;
	}

	@Override
	protected SoundEvent getAmbientSound(){
		return AMSounds.WATER_GUARDIAN_IDLE;
	}

	@Override
	public SoundEvent getAttackSound(){
		return AMSounds.WATER_GUARDIAN_ATTACK;
	}

	@Override
	protected Color getBarColor() {
		return Color.BLUE;
	}
}

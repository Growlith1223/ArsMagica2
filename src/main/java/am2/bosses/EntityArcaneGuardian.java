package am2.bosses;

import am2.api.ArsMagicaAPI;
import am2.api.affinity.Affinity;
import am2.bosses.ai.EntityAICastSpell;
import am2.bosses.ai.EntityAIDispel;
import am2.bosses.ai.ISpellCastCallback;
import am2.defs.AMSounds;
import am2.defs.ItemDefs;
import am2.packet.AMNetHandler;
import am2.utils.NPCSpells;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BossInfo.Color;
import net.minecraft.world.World;

public class EntityArcaneGuardian extends AM2Boss{

	private float runeRotationZ = 0;
	private float runeRotationY = 0;

	private static final DataParameter<Integer> DW_TARGET_ID = EntityDataManager.createKey(EntityArcaneGuardian.class, DataSerializers.VARINT);

	public EntityArcaneGuardian(World par1World){
		super(par1World);
		this.setSize(1.0f, 3.0f);
	}

	@Override
	protected void entityInit(){
		super.entityInit();
		this.dataManager.register(DW_TARGET_ID, -1);
	}

	@Override
	protected void applyEntityAttributes(){
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(115D);
	}

	@Override
	public void onUpdate(){

		if (this.motionY < 0){
			this.motionY *= 0.7999999f;
		}

		updateRotations();

		if (!world.isRemote){
			int eid = this.dataManager.get(DW_TARGET_ID);
			int tid = -1;
			if (this.getAttackTarget() != null){
				tid = this.getAttackTarget().getEntityId();
			}
			if (eid != tid){
				this.dataManager.set(DW_TARGET_ID, tid);
			}

		}

		super.onUpdate();
	}

	private void updateRotations(){
		runeRotationZ = 0;
		float targetRuneRotationY = 0;
		float runeRotationSpeed = 0.3f;
		if (this.getTarget() != null){
			double deltaX = this.getTarget().posX - this.posX;
			double deltaZ = this.getTarget().posZ - this.posZ;

			double angle = Math.atan2(deltaZ, deltaX);

			angle -= Math.toRadians(MathHelper.wrapDegrees(this.rotationYaw + 90) + 180);

			targetRuneRotationY = (float)angle;
			runeRotationSpeed = 0.085f;
		}

		if (targetRuneRotationY > runeRotationY)
			runeRotationY += runeRotationSpeed;
		else if (targetRuneRotationY < runeRotationY)
			runeRotationY -= runeRotationSpeed;

		if (isWithin(runeRotationY, targetRuneRotationY, 0.25f)){
			runeRotationY = targetRuneRotationY;
		}

	}

	@Override
	public boolean attackEntityFrom(DamageSource par1DamageSource, float par2){
		if (par1DamageSource.getSourceOfDamage() == null){
			return super.attackEntityFrom(par1DamageSource, par2);
		}

		if (checkRuneRetaliation(par1DamageSource)){
			return super.attackEntityFrom(par1DamageSource, par2);
		}
		return super.attackEntityFrom(par1DamageSource, par2 * 0.8F);
	}

	private boolean checkRuneRetaliation(DamageSource damagesource){
		Entity source = damagesource.getSourceOfDamage();
		if (source instanceof EntityArcaneGuardian) {
			return true;
		}

		double deltaX = source.posX - this.posX;
		double deltaZ = source.posZ - this.posZ;

		double angle = Math.atan2(deltaZ, deltaX);

		angle -= Math.toRadians(MathHelper.wrapDegrees(this.rotationYaw + 90) + 180);

		float targetRuneRotationY = (float)angle;

		if (isWithin(runeRotationY, targetRuneRotationY, 0.5f)){
			if (this.getDistanceSqToEntity(source) < 9){
				double speed = 2.5;
				double vertSpeed = 0.325;

				deltaZ = source.posZ - this.posZ;
				deltaX = source.posX - this.posX;
				angle = Math.atan2(deltaZ, deltaX);

				double radians = angle;

				if (source instanceof EntityPlayer){
					AMNetHandler.INSTANCE.sendVelocityAddPacket(source.world, (EntityLivingBase)source, speed * Math.cos(radians), vertSpeed, speed * Math.sin(radians));
				}
				source.motionX = (speed * Math.cos(radians));
				source.motionZ = (speed * Math.sin(radians));
				source.motionY = vertSpeed;

				source.attackEntityFrom(DamageSource.causeMobDamage(this), 2);
				return false;
			}
		}
		return true;
	}

	@Override
	protected float modifyDamageAmount(DamageSource source, float damageAmt){
		return damageAmt;
	}

	private boolean isWithin(float source, float target, float tolerance){
		return source + tolerance > target && source - tolerance < target;
	}

	public Entity getTarget(){
		int eid = this.dataManager.get(DW_TARGET_ID);
		if (eid == -1) return null;
		return this.world.getEntityByID(eid);
	}

	public float getRuneRotationZ(){
		return runeRotationZ;
	}

	public float getRuneRotationY(){
		return runeRotationY;
	}

	@Override
	protected void initSpecificAI(){
		this.tasks.addTask(1, new EntityAIDispel(this));
		this.tasks.addTask(1, new EntityAICastSpell<EntityArcaneGuardian>(this, NPCSpells.instance.healSelf, 16, 23, 60, BossActions.CASTING, new ISpellCastCallback<EntityArcaneGuardian>(){
			@Override
			public boolean shouldCast(EntityArcaneGuardian host, ItemStack spell){
				return host.getHealth() < host.getMaxHealth();
			}
		}));
		this.tasks.addTask(2, new EntityAICastSpell<EntityArcaneGuardian>(this, NPCSpells.instance.blink, 16, 23, 20, BossActions.CASTING));
		this.tasks.addTask(3, new EntityAICastSpell<EntityArcaneGuardian>(this, NPCSpells.instance.arcaneBolt, 12, 23, 5, BossActions.CASTING));
	}

	@Override
	public void setCurrentAction(BossActions action){
		super.setCurrentAction(action);
		if (!world.isRemote){
			AMNetHandler.INSTANCE.sendActionUpdateToAllAround(this);
		}
	}

	@Override
	public int getTotalArmorValue(){
		return 9;
	}

	@Override
	protected void dropFewItems(boolean par1, int par2){
		if (par1)
			this.entityDropItem(new ItemStack(ItemDefs.infinityOrb, 1, 1), 0.0f);

		int i = rand.nextInt(4);

		for (int j = 0; j < i; j++){
			this.entityDropItem(new ItemStack(ItemDefs.essence, 1, ArsMagicaAPI.getAffinityRegistry().getId(Affinity.ARCANE)), 0.0f);
		}

		i = rand.nextInt(10);

		if (i < 3 && par1){
			this.entityDropItem(ItemDefs.arcaneSpellBookEnchanted.copy(), 0.0f);
		}
	}
	
	@Override
	public void fall(float distance, float damageMultiplier) {}

	@Override
	protected SoundEvent getHurtSound(){
		return AMSounds.ARCANE_GUARDIAN_HIT;
	}

	@Override
	protected SoundEvent getDeathSound(){
		return AMSounds.ARCANE_GUARDIAN_DEATH;
	}

	@Override
	protected SoundEvent getAmbientSound(){
		return AMSounds.ARCANE_GUARDIAN_IDLE;
	}

	@Override
	public SoundEvent getAttackSound(){
		return AMSounds.ARCANE_GUARDIAN_SPELL;
	}

	@Override
	protected Color getBarColor() {
		return Color.GREEN;
	}
}

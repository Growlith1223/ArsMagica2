package am2.bosses;

import am2.ArsMagica2;
import am2.api.ArsMagicaAPI;
import am2.api.DamageSources;
import am2.api.affinity.Affinity;
import am2.api.sources.DamageSourceFire;
import am2.api.sources.DamageSourceFrost;
import am2.bosses.ai.EntityAICastSpell;
import am2.bosses.ai.EntityAIPlantGuardianThrowSickle;
import am2.bosses.ai.EntityAIShieldBash;
import am2.bosses.ai.EntityAISpinAttack;
import am2.bosses.ai.EntityAIStrikeAttack;
import am2.bosses.ai.ISpellCastCallback;
import am2.defs.AMSounds;
import am2.defs.ItemDefs;
import am2.packet.AMNetHandler;
import am2.particles.AMParticle;
import am2.particles.ParticleFloatUpward;
import am2.particles.ParticleOrbitEntity;
import am2.utils.NPCSpells;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.BossInfo.Color;
import net.minecraft.world.World;

public class EntityNatureGuardian extends AM2Boss{

	public float tendrilRotation;
	public boolean hasSickle;

	public float last_rotation_z_main = 0;
	public float last_rotation_z_shield = 0;

	public float last_rotation_x_main = 0;
	public float last_rotation_x_shield = 0;

	public float last_rotation_y_main = 0;
	public float last_rotation_y_shield = 0;

	public float spinRotation = 0;

	public EntityNatureGuardian(World par1World){
		super(par1World);
		this.setSize(1.65f, 4.75f);
		hasSickle = true;
	}

	@Override
	protected void applyEntityAttributes(){
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(500D);
	}

	@Override
	protected void initSpecificAI(){
		this.tasks.addTask(1, new EntityAICastSpell<EntityNatureGuardian>(this, NPCSpells.instance.dispel, 16, 23, 50, BossActions.CASTING, new ISpellCastCallback<EntityNatureGuardian>(){
			@Override
			public boolean shouldCast(EntityNatureGuardian host, ItemStack spell){
				return host.getActivePotionEffects().size() > 0;
			}
		}));
		this.tasks.addTask(3, new EntityAIPlantGuardianThrowSickle(this, 0.75f));
		this.tasks.addTask(3, new EntityAISpinAttack(this, 0.5f, 8));
		this.tasks.addTask(4, new EntityAIStrikeAttack(this, 0.75f, 12.0f, DamageSources.DamageSourceTypes.CACTUS));
		this.tasks.addTask(5, new EntityAIShieldBash(this, 0.75f));
	}

	@Override
	public void onUpdate(){
		if (world.isRemote){
			updateMovementAngles();
			spawnParticles();
		}
		super.onUpdate();
	}

	@Override
	public int getTotalArmorValue(){
		return 20;
	}

	private void spawnParticles(){
		AMParticle leaf = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, "leaf", posX + (rand.nextDouble() * 3) - 1.5f, posY + (rand.nextDouble() * 5f), posZ + (rand.nextDouble() * 3) - 1.5f);
		if (leaf != null){
			leaf.setMaxAge(20);
			leaf.setIgnoreMaxAge(false);
			leaf.AddParticleController(new ParticleFloatUpward(leaf, 0.05f, -0.02f, 1, false));
			if (getCurrentAction() == BossActions.SPINNING){
				leaf.AddParticleController(new ParticleOrbitEntity(leaf, this, 0.6f, 1, false));
			}
		}
	}

	@Override
	public void setCurrentAction(BossActions action){
		super.setCurrentAction(action);
		this.spinRotation = 0;

		if (!world.isRemote){
			AMNetHandler.INSTANCE.sendActionUpdateToAllAround(this);
		}
	}

	private void updateMovementAngles(){
		tendrilRotation += 0.2f;
		tendrilRotation %= 360;

		switch (currentAction){
		case IDLE:
			break;
		case SPINNING:
			this.spinRotation = (this.spinRotation - 40) % 360;
			break;
		case STRIKE:
			break;
		case THROWING_SICKLE:
			break;
		default:
			break;

		}
	}

	@Override
	public ItemStack getHeldItem(EnumHand hand){
		return null;
	}
	
	public void setItemStackToSlot(EntityEquipmentSlot slotIn, ItemStack stack) {};

	@Override
	public boolean isActionValid(BossActions action){
		if (action == BossActions.STRIKE || action == BossActions.SPINNING || action == BossActions.THROWING_SICKLE){
			return hasSickle;
		}
		return true;
	}

	@Override
	protected void dropFewItems(boolean par1, int par2){
		if (par1)
			this.entityDropItem(new ItemStack(ItemDefs.infinityOrb, 1, 2), 0.0f);

		int i = rand.nextInt(4);

		for (int j = 0; j < i; j++){
			this.entityDropItem(new ItemStack(ItemDefs.essence, 1, ArsMagicaAPI.getAffinityRegistry().getId(Affinity.NATURE)), 0.0f);
		}
		i = rand.nextInt(10);

		if (i < 3 && par1){
			this.entityDropItem(ItemDefs.natureScytheEnchanted.copy(), 0.0f);
		}
	}

	@Override
	protected float modifyDamageAmount(DamageSource source, float damageAmt){
		if (source instanceof DamageSourceFire || source.isFireDamage()){
			damageAmt *= 2f;
		}else if (source instanceof DamageSourceFrost){
			damageAmt *= 1.5f;
		}
		return damageAmt;
	}

	@Override
	protected SoundEvent getHurtSound(){
		return AMSounds.NATURE_GUARDIAN_HIT;
	}

	@Override
	protected SoundEvent getDeathSound(){
		return AMSounds.NATURE_GUARDIAN_DEATH;
	}

	@Override
	protected SoundEvent getAmbientSound(){
		return AMSounds.NATURE_GUARDIAN_IDLE;
	}

	@Override
	public SoundEvent getAttackSound(){
		return AMSounds.NATURE_GUARDIAN_ATTACK;
	}

	@Override
	protected Color getBarColor() {
		return Color.RED;
	}
}

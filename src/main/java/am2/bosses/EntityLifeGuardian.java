package am2.bosses;

import java.util.ArrayList;
import java.util.Iterator;

import am2.ArsMagica2;
import am2.api.ArsMagicaAPI;
import am2.api.affinity.Affinity;
import am2.bosses.ai.EntityAICastSpell;
import am2.bosses.ai.EntityAIDispel;
import am2.bosses.ai.EntityAISummonAllies;
import am2.bosses.ai.ISpellCastCallback;
import am2.defs.AMSounds;
import am2.defs.ItemDefs;
import am2.entity.EntityDarkling;
import am2.entity.EntityEarthElemental;
import am2.entity.EntityFireElemental;
import am2.entity.EntityManaElemental;
import am2.utils.NPCSpells;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.BossInfo.Color;
import net.minecraft.world.World;

public class EntityLifeGuardian extends AM2Boss{

	private ArrayList<EntityLiving> minions;
	public ArrayList<EntityLiving> queued_minions;

	private static final DataParameter<Integer> DATA_MINION_COUNT = EntityDataManager.createKey(EntityLifeGuardian.class, DataSerializers.VARINT);

	public EntityLifeGuardian(World par1World){
		super(par1World);
		this.setSize(1, 2);
		minions = new ArrayList<EntityLiving>();
		queued_minions = new ArrayList<EntityLiving>();
	}

	@Override
	protected void entityInit(){
		super.entityInit();
		this.dataManager.register(DATA_MINION_COUNT, 0);
	}

	@Override
	protected void initSpecificAI(){
		this.tasks.addTask(1, new EntityAIDispel(this));
		this.tasks.addTask(1, new EntityAICastSpell<EntityLifeGuardian>(this, NPCSpells.instance.healSelf, 16, 23, 100, BossActions.CASTING, new ISpellCastCallback<EntityLifeGuardian>(){
			@Override
			public boolean shouldCast(EntityLifeGuardian host, ItemStack spell){
				return host.getHealth() < host.getMaxHealth();
			}
		}));
		this.tasks.addTask(2, new EntityAICastSpell<EntityLifeGuardian>(this, NPCSpells.instance.nauseate, 16, 23, 20, BossActions.CASTING, new ISpellCastCallback<EntityLifeGuardian>(){
			@Override
			public boolean shouldCast(EntityLifeGuardian host, ItemStack spell){
				return minions.size() == 0;
			}
		}));
		this.tasks.addTask(3, new EntityAISummonAllies(this, EntityEarthElemental.class, EntityFireElemental.class, EntityManaElemental.class, EntityDarkling.class));
	}

	@Override
	public boolean attackEntityFrom(DamageSource par1DamageSource, float par2){
		if (par1DamageSource.getSourceOfDamage() != null && par1DamageSource.getSourceOfDamage() instanceof EntityLivingBase){
			for (EntityLivingBase minion : minions.toArray(new EntityLivingBase[minions.size()])){
				((EntityLiving)minion).setAttackTarget((EntityLivingBase)par1DamageSource.getSourceOfDamage());
			}
		}
		return super.attackEntityFrom(par1DamageSource, par2);
	}

	@Override
	protected float modifyDamageAmount(DamageSource source, float damageAmt){
		if (minions.size() > 0){
			damageAmt = 0;
			minions.get(getRNG().nextInt(minions.size())).attackEntityFrom(source, damageAmt);
		}
		return damageAmt;
	}

	public int getNumMinions(){
		return this.dataManager.get(DATA_MINION_COUNT);
	}

	@Override
	protected void applyEntityAttributes(){
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(200D);
	}

	@Override
	public void onUpdate(){
		//Minion management - add any queued minions to the minion list and prune out any fallen or nonexistant ones
		if (!world.isRemote){
			minions.addAll(queued_minions);
			queued_minions.clear();
			Iterator<EntityLiving> it = minions.iterator();
			while (it.hasNext()){
				EntityLiving minion = it.next();
				if (minion == null || minion.isDead)
					it.remove();
			}

			this.dataManager.set(DATA_MINION_COUNT, minions.size());

			if (this.ticksExisted % 100 == 0){
				for (EntityLivingBase e : minions)
					ArsMagica2.proxy.particleManager.spawn(world, "textures/blocks/oreblocksunstone.png", this, e);
			}
		}

		if (this.ticksExisted % 40 == 0)
			this.heal(2f);

		super.onUpdate();
	}

	@Override
	protected SoundEvent getHurtSound(){
		return AMSounds.LIFE_GUARDIAN_HIT;
	}

	@Override
	protected SoundEvent getDeathSound(){
		return AMSounds.LIFE_GUARDIAN_DEATH;
	}

	@Override
	protected SoundEvent getAmbientSound(){
		return AMSounds.LIFE_GUARDIAN_IDLE;
	}

	@Override
	public SoundEvent getAttackSound(){
		return AMSounds.LIFE_GUARDIAN_HEAL;
	}

	@Override
	protected void dropFewItems(boolean par1, int par2){
		if (par1)
			this.entityDropItem(new ItemStack(ItemDefs.infinityOrb, 1, 1), 0.0f);

		int i = rand.nextInt(4);

		for (int j = 0; j < i; j++){
			this.entityDropItem(new ItemStack(ItemDefs.essence, 1, ArsMagicaAPI.getAffinityRegistry().getId(Affinity.LIFE)), 0.0f);
		}
		i = rand.nextInt(10);

		if (i < 3 && par1){
			this.entityDropItem(ItemDefs.lifeWardEnchanted.copy(), 0.0f);
		}
	}

	@Override
	public float getEyeHeight(){
		return 1.5f;
	}

	@Override
	protected Color getBarColor() {
		return Color.GREEN;
	}
}

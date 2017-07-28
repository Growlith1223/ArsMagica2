package am2.entity;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILeapAtTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityDarkling extends EntityMob{
	
	private static final DataParameter<Boolean> IS_ANGRY = EntityDataManager.createKey(EntityDarkling.class, DataSerializers.BOOLEAN);

	public EntityDarkling(World par1World){
		super(par1World);
		initAI();
		this.setSize(0.5f, 0.5f);
	}

	@Override
	protected void applyEntityAttributes(){
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(7D);
	}

	@Override
	protected void entityInit(){
		super.entityInit();
		this.dataManager.register(IS_ANGRY, false);
	}

	public boolean isAngry(){
		return this.dataManager.get(IS_ANGRY);
	}

	@Override
	public boolean isAIDisabled(){
		return false;
	}

	private void initAI(){
		this.targetTasks.taskEntries.clear();
		this.tasks.taskEntries.clear();

		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, 0, false, true, null));

		this.setPathPriority(PathNodeType.WATER, -1F);
		this.tasks.addTask(1, new EntityAISwimming(this));
		this.tasks.addTask(3, new EntityAILeapAtTarget(this, 0.6F));
		this.tasks.addTask(4, new EntityAIAttackMelee(this, 0.5f, true));
		this.tasks.addTask(7, new EntityAIWander(this, 0.4f));
		this.tasks.addTask(9, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(9, new EntityAILookIdle(this));
	}

	@Override
	public void onUpdate(){
		if (this.getAttackTarget() != null){
			this.dataManager.set(IS_ANGRY, true);
		}else{
			this.dataManager.set(IS_ANGRY, false);
		}
		super.onUpdate();
	}
	
	@Override
	public EnumActionResult applyPlayerInteraction(EntityPlayer player, Vec3d vec, EnumHand hand) {
		return EnumActionResult.PASS;
	}
}

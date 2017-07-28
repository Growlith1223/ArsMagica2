package am2.entity;

import java.util.List;

import am2.defs.ItemDefs;
import am2.defs.LootTablesArsMagica;
import am2.defs.SkillDefs;
import am2.entity.ai.EntityAIAllyManaLink;
import am2.entity.ai.EntityAIRangedAttackSpell;
import am2.entity.ai.selectors.LightMageEntitySelector;
import am2.extensions.EntityExtension;
import am2.extensions.SkillData;
import am2.utils.EntityUtils;
import am2.utils.NPCSpells;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemNameTag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SuppressWarnings("deprecation")
public class EntityLightMage extends EntityCreature{

	int hp;
	private static ItemStack diminishedHeldItem = new ItemStack(ItemDefs.affinityTome, 1, 2);
	private static ItemStack normalHeldItem = new ItemStack(ItemDefs.affinityTome, 1, 7);
	private static ItemStack augmentedHeldItem = new ItemStack(ItemDefs.affinityTome, 1, 5);

	public static final DataParameter<Integer> MAGE_SKIN = EntityDataManager.createKey(EntityLightMage.class, DataSerializers.VARINT);
	public static final DataParameter<Integer> MAGE_BOOK = EntityDataManager.createKey(EntityLightMage.class, DataSerializers.VARINT);

	public EntityLightMage(World world){
		super(world);
		setSize(0.6F, 1.8F);
		hp = rand.nextInt(10) + 12;
		initAI();
	}

	@Override
	protected void entityInit(){
		super.entityInit();
		this.dataManager.register(MAGE_BOOK, 0);
		this.dataManager.register(MAGE_SKIN, rand.nextInt(12) + 1);
	}

	@Override
	protected void applyEntityAttributes(){
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(22D);
	}
	
	@Override
	public ItemStack getHeldItemMainhand() {
		int cm = this.dataManager.get(MAGE_BOOK);
		if (cm == 0)
			return diminishedHeldItem;
		else if (cm == 1)
			return normalHeldItem;
		else
			return augmentedHeldItem;
	}

	private void initAI(){
		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(7, new EntityAILookIdle(this));
		this.tasks.addTask(5, new EntityAIWander(this, MovementSpeed()));
		this.tasks.addTask(1, new EntityAIAvoidEntity<EntityManaVortex>(this, EntityManaVortex.class, 10, MovementSpeed(), ActionSpeed()));

		this.tasks.addTask(3, new EntityAIAllyManaLink(this));

		this.tasks.addTask(4, new EntityAIRangedAttackSpell(this, MovementSpeed(), 20, NPCSpells.instance.lightMage_DiminishedAttack));

		//Retaliation to attacks
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));

		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityDarkMage>(this, EntityDarkMage.class, 0, true, false, null));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntitySlime>(this, EntitySlime.class, 0, true, false, null));
		this.targetTasks.addTask(3, new EntityAINearestAttackableTarget<EntityMob>(this, EntityMob.class, 0, true, false, LightMageEntitySelector.instance));
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound par1nbtTagCompound){
		super.readEntityFromNBT(par1nbtTagCompound);
		this.dataManager.set(MAGE_SKIN, par1nbtTagCompound.getInteger("am2_lm_skin"));
		this.dataManager.set(MAGE_BOOK, par1nbtTagCompound.getInteger("am2_lm_book"));
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound par1nbtTagCompound){
		super.writeEntityToNBT(par1nbtTagCompound);

		par1nbtTagCompound.setInteger("am2_lm_skin", this.dataManager.get(MAGE_SKIN));
		par1nbtTagCompound.setInteger("am2_lm_book", this.dataManager.get(MAGE_BOOK));
	}

	@Override
	public int getTotalArmorValue(){
		return 1;
	}

	protected float MovementSpeed(){
		return 0.4f;
	}

	protected float ActionSpeed(){
		return 0.5f;
	}

	@Override
	public boolean isAIDisabled(){
		return false;
	}

	private int getAverageNearbyPlayerMagicLevel(){
		if (this.world == null) return 0;
		List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().expand(250, 250, 250));
		if (players.size() == 0) return 0;
		int avgLvl = 0;
		for (EntityPlayer player : players){
			avgLvl += EntityExtension.For(player).getCurrentLevel();
		}
		return (int)Math.ceil(avgLvl / players.size());
	}

	@Override
	public boolean getCanSpawnHere(){
		if (!SpawnBlacklists.entityCanSpawnHere(this.getPosition(), world, this))
			return false;
		if (getAverageNearbyPlayerMagicLevel() < 8){
			return false;
		}

		int avgLevel = getAverageNearbyPlayerMagicLevel();
		if (avgLevel == 0){
			EntityExtension.For(this).setMagicLevelWithMana(10);
			if (rand.nextInt(100) < 10){
				this.tasks.addTask(3, new EntityAIRangedAttackSpell(this, MovementSpeed(), 40, NPCSpells.instance.lightMage_NormalAttack));
				this.dataManager.set(MAGE_BOOK, 1);
			}
		}else{
			EntityExtension.For(this).setMagicLevelWithMana(10 + rand.nextInt(avgLevel));
			int levelRand = rand.nextInt(avgLevel * 2);
			if (levelRand > 60){
				this.tasks.addTask(2, new EntityAIRangedAttackSpell(this, MovementSpeed(), 100, NPCSpells.instance.lightMage_AugmentedAttack));
				this.dataManager.set(MAGE_BOOK, 2);
			}
			if (levelRand > 30){
				this.tasks.addTask(3, new EntityAIRangedAttackSpell(this, MovementSpeed(), 40, NPCSpells.instance.lightMage_NormalAttack));
				this.dataManager.set(MAGE_BOOK, 1);
			}
		}
		return isValidLightLevel() && super.getCanSpawnHere();
	}

	protected boolean isValidLightLevel(){

		if (this.world.getLightFor(EnumSkyBlock.SKY, getPosition()) > this.rand.nextInt(32)){
			return false;
		}else{
			int var4 = this.world.getLightFor(EnumSkyBlock.BLOCK, getPosition());

			if (this.world.isThundering()){
				int var5 = this.world.getSkylightSubtracted();
				this.world.setSkylightSubtracted(10);
				var4 = this.world.getLightFor(EnumSkyBlock.BLOCK, getPosition());
				this.world.setSkylightSubtracted(var5);
			}

			return var4 <= this.rand.nextInt(8);
		}
	}
	
	@Override
	protected void dropEquipment(boolean wasRecentlyHit, int lootingModifier) {
	}

//	@Override
//	protected void dropFewItems(boolean par1, int par2){
//		if (par1 && getRNG().nextDouble() < 0.2)
//			for (int j = 0; j < getRNG().nextInt(3); ++j)
//				this.entityDropItem(new ItemStack(ItemDefs.rune, 1, getRNG().nextInt(16)), 0.0f);
//
//		if (par1 && getRNG().nextDouble() < 0.2)
//			this.entityDropItem(new ItemStack(ItemDefs.spellParchment, 1, 0), 0.0f);
//
//		if (par1 && getRNG().nextDouble() < 0.05)
//			this.entityDropItem(new ItemStack(ItemDefs.spellBook, 1, 0), 0.0f);
//	}
//	
	@Override
	protected ResourceLocation getLootTable() {
		return LootTablesArsMagica.LIGHT_MAGE_LOOT;
	}
	@Override
	public EnumActionResult applyPlayerInteraction(EntityPlayer player, Vec3d vec, EnumHand hand){
		ItemStack stack = player.getHeldItem(hand);
		if (world.isRemote)
			return EnumActionResult.PASS;

		if (stack != null && stack.getItem() instanceof ItemNameTag)
			return EnumActionResult.PASS;
		
		if (hand == EnumHand.OFF_HAND) return EnumActionResult.PASS;
		
		if (SkillData.For(player).hasSkill(SkillDefs.MAGE_POSSE_1.getID())){
			if (EntityUtils.isSummon(this)){
				player.sendMessage(new TextComponentString(String.format("\247o%s", I18n.translateToLocal("am2.npc.partyleave"))));
				EntityUtils.revertAI(this);
			}else{
				if (EntityExtension.For(player).getCanHaveMoreSummons()){
					if (EntityExtension.For(player).getCurrentLevel() - 5 >= EntityExtension.For(this).getCurrentLevel()){
						player.sendMessage(new TextComponentString(String.format("\247o%s", I18n.translateToLocal("am2.npc.partyjoin"))));
						EntityUtils.setOwner(this, player);
						EntityUtils.makeSummon_PlayerFaction(this, player, true);
						EntityUtils.setSummonDuration(this, -1);
					}else{
						player.sendMessage(new TextComponentString(String.format("\247o%s", I18n.translateToLocal("am2.npc.partyrefuse"))));
					}
				}else{
					player.sendMessage(new TextComponentString(String.format("\247o%s", I18n.translateToLocal("am2.npc.partyfull"))));
					for (Entity entity : EntityExtension.For(player).getSummonedEntities()){
						player.sendMessage(new TextComponentString(String.format("\247o%s", I18n.translateToLocal(entity.getName() + entity.getPosition().toString()))));
					}
				}
			}
		}else{
			player.sendMessage(new TextComponentString(String.format("\247o%s", I18n.translateToLocal("am2.npc.nopartyskill"))));
		}
		return EnumActionResult.SUCCESS;
	}

	@SideOnly(Side.CLIENT)
	public String getTexture(){
		return String.format("arsmagica2:textures/mobs/light_mages/light_mage_%d.png", this.dataManager.get(MAGE_SKIN).intValue());
	}
}

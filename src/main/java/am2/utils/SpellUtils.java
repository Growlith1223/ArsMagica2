package am2.utils;

import java.util.ArrayList;
import java.util.HashMap;

import javax.annotation.Nullable;

import am2.extensions.AffinityData;
import com.google.common.collect.Lists;

import am2.ArsMagica2;
import am2.api.ArsMagicaAPI;
import am2.api.SpellRegistry;
import am2.api.affinity.Affinity;
import am2.api.event.SpellCastEvent;
import am2.api.extensions.IEntityExtension;
import am2.api.spell.AbstractSpellPart;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellModifier;
import am2.api.spell.SpellModifiers;
import am2.api.spell.SpellShape;
import am2.armor.ArmorHelper;
import am2.armor.ArsMagicaArmorMaterial;
import am2.defs.ItemDefs;
import am2.defs.PotionEffectsDefs;
import am2.defs.SpellDefs;
import am2.enchantments.AMEnchantmentHelper;
import am2.entity.EntityDarkMage;
import am2.entity.EntityLightMage;
import am2.extensions.EntityExtension;
import am2.gui.AMGuiHelper;
import am2.items.ItemSpellBase;
import am2.spell.SpellCastResult;
import am2.spell.modifier.Colour;
import am2.spell.shape.MissingShape;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.oredict.OreDictionary;

@SuppressWarnings("deprecation")
public class SpellUtils {
	
	public static final String TYPE_SHAPE = "Shape";
	public static final String TYPE_COMPONENT = "Component";
	public static final String TYPE_MODIFIER = "Modifier";
	public static final String TYPE = "Type";
	public static final String ID = "ID";
	public static final String SHAPE_GROUP = "ShapeGroup";
	public static final String STAGE = "Stage_";
	public static final String SPELL_DATA = "SpellData";
	
	public static SpellShape getShapeForStage(ItemStack oldIs, int stage){
		if (oldIs == null || !oldIs.hasTagCompound()) return SpellDefs.MISSING_SHAPE;
		ItemStack stack = merge(oldIs.copy());
		NBTTagCompound am2Tag = NBTUtils.getAM2Tag(stack.getTagCompound());
		NBTTagList stageTag = NBTUtils.addCompoundList(am2Tag, STAGE + stage);
		String shapeName = "null";
		for (int i = 0; i < stageTag.tagCount(); i++) {
			if (stageTag.getCompoundTagAt(i).getString(TYPE).equals(TYPE_SHAPE)) {
				shapeName = stageTag.getCompoundTagAt(i).getString(ID);
				break;
			}
		}

		return SpellRegistry.getShapeFromName(shapeName) != null ? SpellRegistry.getShapeFromName(shapeName) : SpellDefs.MISSING_SHAPE;
	}
	
	public static void changeEnchantmentsForShapeGroup(ItemStack stack){
		ItemStack constructed = merge(stack);
		int looting = 0;
		int silkTouch = 0;
		for (int i = 0; i < numStages(constructed); ++i){
			looting += countModifiers(SpellModifiers.FORTUNE_LEVEL, constructed);
			silkTouch += countModifiers(SpellModifiers.SILKTOUCH_LEVEL, constructed);
		}

		AMEnchantmentHelper.fortuneStack(stack, looting);
		AMEnchantmentHelper.lootingStack(stack, looting);
		AMEnchantmentHelper.silkTouchStack(stack, silkTouch);
	}
	
	public static float modifyDamage(EntityLivingBase caster, float damage){
		float factor = (float)(EntityExtension.For(caster).getCurrentLevel() < 20 ?
				0.5 + (0.5 * (EntityExtension.For(caster).getCurrentLevel() / 19)) :
				1.0 + (1.0 * (EntityExtension.For(caster).getCurrentLevel() - 20) / 79));
		return damage * factor;
	}

	
	public static boolean modifierIsPresent (SpellModifiers mod, ItemStack stack) {
		ArrayList<SpellModifier> mods = getModifiersForStage(stack, -1);
		if (mods.isEmpty())
			return false;
		for (SpellModifier m : mods) {
			if (m.getAspectsModified().contains(mod)) 
				return true;
		}
		
		return false;
	}
	
	public static int countModifiers (SpellModifiers mod, ItemStack stack) {
		ArrayList<SpellModifier> mods = getModifiersForStage(stack, -1);
		int i = 0;
		for (SpellModifier m : mods) {
			if (m.getAspectsModified().contains(mod)) 
				i++;
		}
		
		return i;
	}
	
	public static boolean attackTargetSpecial(ItemStack spellStack, Entity target, DamageSource damagesource, float magnitude){

		if (target.world.isRemote)
			return true;

		EntityPlayer dmgSrcPlayer = null;

		if (damagesource.getEntity() != null){
			if (damagesource.getEntity() instanceof EntityLivingBase){
				EntityLivingBase source = (EntityLivingBase)damagesource.getEntity();
				if ((source instanceof EntityLightMage || source instanceof EntityDarkMage) && target.getClass() == EntityCreeper.class){
					return false;
				}else if (source instanceof EntityLightMage && target instanceof EntityLightMage){
					return false;
				}else if (source instanceof EntityDarkMage && target instanceof EntityDarkMage){
					return false;
				}else  if (source instanceof EntityPlayer && target instanceof EntityPlayer && !target.world.isRemote && (!FMLCommonHandler.instance().getMinecraftServerInstance().isPVPEnabled() || ((EntityPlayer)target).capabilities.isCreativeMode)){
					return false;
				}

				if (source.isPotionActive(PotionEffectsDefs.fury))
					magnitude += 4;
			}

			if (damagesource.getEntity() instanceof EntityPlayer){
				dmgSrcPlayer = (EntityPlayer)damagesource.getEntity();
				int armorSet = ArmorHelper.getFullArsMagicaArmorSet(dmgSrcPlayer);
				if (armorSet == ArsMagicaArmorMaterial.MAGE.getMaterialID()){
					magnitude *= 1.05f;
				}else if (armorSet == ArsMagicaArmorMaterial.BATTLEMAGE.getMaterialID()){
					magnitude *= 1.025f;
				}else if (armorSet == ArsMagicaArmorMaterial.ARCHMAGE.getMaterialID()){
					magnitude *= 1.1f;
				}

				ItemStack equipped = dmgSrcPlayer.getActiveItemStack();
				if (equipped != null && equipped.getItem() == ItemDefs.arcaneSpellbook){
					magnitude *= 1.1f;
				}
			}
		}

		if (target instanceof EntityLivingBase){
			if (EntityUtils.isSummon((EntityLivingBase)target) && damagesource.damageType.equals("magic")){
				magnitude *= 3.0f;
			}
		}

		magnitude *= ArsMagica2.config.getDamageMultiplier();

//		ItemStack oldItemStack = null;

		boolean success = false;
		if (target instanceof EntityDragon){
			success = ((EntityDragon)target).attackEntityFromPart(((EntityDragon)target).dragonPartBody, damagesource, magnitude);
		}else{
			success = target.attackEntityFrom(damagesource, magnitude);
		}

		if (dmgSrcPlayer != null){
			if (spellStack != null && target instanceof EntityLivingBase){
				if (!target.world.isRemote &&
						((EntityLivingBase)target).getHealth() <= 0 &&
						modifierIsPresent(SpellModifiers.DISMEMBERING_LEVEL, spellStack)){
					double chance = SpellUtils.getModifiedDouble_Add(0, spellStack, dmgSrcPlayer, target, dmgSrcPlayer.world, SpellModifiers.DISMEMBERING_LEVEL);
					if (dmgSrcPlayer.world.rand.nextDouble() <= chance){
						dropHead(target, dmgSrcPlayer.world);
					}
				}
			}
		}

		return success;
	}
	
	private static void dropHead(Entity target, World world){
		if (target.getClass() == EntitySkeleton.class){
			dropHead_do(world, target.posX, target.posY, target.posZ, 0);
		}
		else if (target.getClass() == EntityWitherSkeleton.class){
			dropHead_do(world, target.posX, target.posY, target.posZ, 1);
		}else if (target.getClass() == EntityZombie.class){
			dropHead_do(world, target.posX, target.posY, target.posZ, 2);
		}else if (target.getClass() == EntityCreeper.class){
			dropHead_do(world, target.posX, target.posY, target.posZ, 4);
		}else if (target instanceof EntityPlayer){
			dropHead_do(world, target.posX, target.posY, target.posZ, 3);
		}
	}
	
	private static void dropHead_do(World world, double x, double y, double z, int type){
		EntityItem item = new EntityItem(world);
		ItemStack stack = new ItemStack(Items.SKULL, 1, type);
		item.setEntityItemStack(stack);
		item.setPosition(x, y, z);
		world.spawnEntity(item);
	}
	
	public static NBTTagCompound encode(KeyValuePair<ArrayList<AbstractSpellPart>, NBTTagCompound> toEncode) {
		NBTTagCompound group = new NBTTagCompound();
		group.setTag(SPELL_DATA, toEncode.value);
		int stage = 0;
		for (AbstractSpellPart part : toEncode.key) {
			NBTTagList stageTag = NBTUtils.addCompoundList(group, STAGE + stage);
			NBTTagCompound tmp = new NBTTagCompound();
			String id = SpellRegistry.getSkillFromPart(part).getID();
			tmp.setString(ID, id);
			String type = "";
			if (part instanceof SpellShape) type = TYPE_SHAPE;
			if (part instanceof SpellModifier) type = TYPE_MODIFIER;
			if (part instanceof SpellComponent) type = TYPE_COMPONENT;
			tmp.setString(TYPE, type);
			if (part instanceof SpellShape) {
				stage++;
			} else {
			}
			stageTag.appendTag(tmp);
		}
		group.setInteger("StageNum", stage);
		return group;
	}
	
	public static KeyValuePair<ArrayList<AbstractSpellPart>, NBTTagCompound> decode(NBTTagCompound toDecode) {
		if (toDecode == null)
			return null;
		try {
			ArrayList<AbstractSpellPart> parts = new ArrayList<>();
			for (int j = 0; j < NBTUtils.getAM2Tag(toDecode).getInteger("StageNum"); j++) { 
				NBTTagList stageTag = NBTUtils.addCompoundList(NBTUtils.getAM2Tag(toDecode), STAGE + j);
				for (int i = 0; i < stageTag.tagCount(); i++) {
					NBTTagCompound tmp = stageTag.getCompoundTagAt(i);
					String type = tmp.getString(TYPE);
					if (type.equalsIgnoreCase(TYPE_COMPONENT)) {
						parts.add(SpellRegistry.getComponentFromName(tmp.getString(ID)));
					}
					if (type.equalsIgnoreCase(TYPE_MODIFIER)) {
						parts.add(SpellRegistry.getModifierFromName(tmp.getString(ID)));
					}
					if (type.equalsIgnoreCase(TYPE_SHAPE)) {
						parts.add(SpellRegistry.getShapeFromName(tmp.getString(ID)));
					}
				}
			}
			return new KeyValuePair<ArrayList<AbstractSpellPart>, NBTTagCompound>(parts, toDecode.getCompoundTag(SPELL_DATA));
		} catch (Exception e) {
			return null;
		}
	}
	
	public static ItemStack createSpellStack(ArrayList<KeyValuePair<ArrayList<AbstractSpellPart>, NBTTagCompound>> shapeGroups, ArrayList<AbstractSpellPart> spellDef, NBTTagCompound encodedData) {
		ItemStack stack = new ItemStack(ItemDefs.spell);
		NBTTagCompound tag = new NBTTagCompound();
		NBTTagCompound am2 = NBTUtils.getAM2Tag(tag);
		am2.setTag(SPELL_DATA, encodedData);
		NBTTagList shapeGroupList = NBTUtils.addCompoundList(am2, "ShapeGroups");
		for (KeyValuePair<ArrayList<AbstractSpellPart>, NBTTagCompound> shapeGroup : shapeGroups) {
			if (shapeGroup.key.isEmpty()) {
				continue;
			}
			NBTTagCompound group = new NBTTagCompound();
			group.setTag(SPELL_DATA, shapeGroup.value);
			int stage = 0;
			boolean lastWasShape = false;
			for (AbstractSpellPart part : shapeGroup.key) {
				NBTTagList stageTag = NBTUtils.addCompoundList(group, STAGE + stage);
				NBTTagCompound tmp = new NBTTagCompound();
				String id = part.getRegistryName().toString();
				tmp.setString(ID, id);
				String type = "";
				if (part instanceof SpellShape) type = TYPE_SHAPE;
				if (part instanceof SpellModifier) type = TYPE_MODIFIER;
				if (part instanceof SpellComponent) type = TYPE_COMPONENT;
				tmp.setString(TYPE, type);
				if (part instanceof SpellShape) {
					stage++;
					lastWasShape = true;
				} else {
					lastWasShape = false;
				}
				stageTag.appendTag(tmp);
			}
			group.setInteger("StageNum", stage);
			group.setBoolean("LastWasShape", lastWasShape);
			group.setInteger("CurrentGroup", 0);
			shapeGroupList.appendTag(group);
		}
		int stage = 0;
		for (AbstractSpellPart part : spellDef) {
			NBTTagList stageTag = NBTUtils.addCompoundList(am2, STAGE + stage);
			NBTTagCompound tmp = new NBTTagCompound();
			String id = SpellRegistry.getSkillFromPart(part).getID();
			tmp.setString(ID, id);
			String type = "";
			if (part instanceof SpellShape) type = TYPE_SHAPE;
			if (part instanceof SpellModifier) type = TYPE_MODIFIER;
			if (part instanceof SpellComponent) type = TYPE_COMPONENT;
			tmp.setString(TYPE, type);
			if (part instanceof SpellShape) stage++;
			stageTag.appendTag(tmp);
		}
		am2.setInteger("StageNum", stage + 1);
		am2.setInteger("NumShapeGroups", shapeGroupList.tagCount());
		am2.setInteger("CurrentShapeGroup", shapeGroupList.tagCount() == 0 ? -1 : 0);
		am2.setInteger("CurrentGroup", 0);
		stack.setTagCompound(tag);
		return stack;
	}
	
	public static ItemStack merge(ItemStack spellIn) {

		if (spellIn.getTagCompound() == null)
			return spellIn;
		if (NBTUtils.getAM2Tag(spellIn.getTagCompound()).getInteger("CurrentShapeGroup") == -1) {
			return spellIn;
		}
		ItemStack newStack = spellIn.copy();
		spellIn = new ItemStack(ItemDefs.spell);
		
		
		NBTTagCompound group = (NBTTagCompound) NBTUtils.addCompoundList(NBTUtils.getAM2Tag(newStack.getTagCompound()), "ShapeGroups").getCompoundTagAt(NBTUtils.getAM2Tag(newStack.getTagCompound()).getInteger("CurrentShapeGroup")).copy();
		int stageNum = numStages(newStack);
		for (int i = 0; i < stageNum; i++) {
			NBTTagList list = (NBTTagList) NBTUtils.addCompoundList(NBTUtils.getAM2Tag(newStack.getTagCompound()), STAGE + i).copy();
			if (i == 0 && !group.getBoolean("LastWasShape")) {
				NBTTagList newList = (NBTTagList) NBTUtils.addCompoundList(group, "Stage_" + group.getInteger("StageNum")).copy();
				for (int j = 0; j < list.tagCount(); j++) {
					newList.appendTag(list.getCompoundTagAt(j));
				}
				list = newList;
			}
			// (group.getBoolean("LastWasShape") ? -1 : 0) +
			group.setTag(STAGE + (i + group.getInteger("StageNum")), list);
		}
		group.setInteger("StageNum", group.getInteger("StageNum") + stageNum);
		group.setInteger("CurrentShapeGroup", -1);
		group.getCompoundTag(SPELL_DATA).merge(NBTUtils.getAM2Tag(newStack.getTagCompound()).getCompoundTag(SPELL_DATA));
		newStack.setTagCompound(NBTUtils.addTag(new NBTTagCompound(), group, "AM2"));
		return newStack;
	}
	
	public static ItemStack popStackStage(ItemStack is) {
		NBTUtils.getAM2Tag(is.getTagCompound()).setInteger("CurrentGroup", NBTUtils.getAM2Tag(is.getTagCompound()).getInteger("CurrentGroup") + 1);
		return is;
	}
	
	public static int numStages(ItemStack stack){
		return NBTUtils.getAM2Tag(stack.getTagCompound()).getInteger("StageNum");
	}
	
	public static float getManaCost(ItemStack stack, Entity caster) {
		if (stack.getTagCompound() == null)
			return 0;
		ItemStack mergedStack = merge(stack);
		AffinityData pAffinity = null;
		Affinity[] affinities = null;
		if (caster instanceof EntityPlayer) {
            pAffinity = AffinityData.For((EntityLivingBase) caster);
            affinities = pAffinity.getHighestAffinities();
        }
		try {
			float cost = 0;
			float modMultiplier = 1.0F;
			for (int j = 0; j < NBTUtils.getAM2Tag(mergedStack.getTagCompound()).getInteger("StageNum"); j++) { 
				NBTTagList stageTag = NBTUtils.addCompoundList(NBTUtils.getAM2Tag(mergedStack.getTagCompound()), STAGE + j);
				for (int i = 0; i < stageTag.tagCount(); i++) {
					NBTTagCompound tmp = stageTag.getCompoundTagAt(i);
					String type = tmp.getString(TYPE);
					if (type.equalsIgnoreCase(TYPE_COMPONENT)) {
						SpellComponent component = SpellRegistry.getComponentFromName(tmp.getString(ID));
						cost += component.manaCost(ArsMagica2.proxy.getLocalPlayer());
						if (caster instanceof EntityPlayer) {
                            for (Affinity aff : affinities) {
                                for (Affinity aff2 : component.getAffinity()) {
                                    if (aff == aff2 && pAffinity.getAffinityDepth(aff) > 0) {
                                        cost = cost - (float) (cost * (0.5f * AffinityData.For((EntityLivingBase) caster).getAffinityDepth(aff)));
                                        break;
                                    } else {
                                        cost = cost + (float) (cost * (0.10f));
                                    }
                                }
                            }
                        }
					}
					if (type.equalsIgnoreCase(TYPE_MODIFIER)) {
						SpellModifier mod = SpellRegistry.getModifierFromName(tmp.getString(ID));
						modMultiplier *= mod.getManaCostMultiplier(mergedStack, j, 1);
					}
					if (type.equalsIgnoreCase(TYPE_SHAPE)) {
						SpellShape shape = SpellRegistry.getShapeFromName(tmp.getString(ID));
						modMultiplier *= shape.manaCostMultiplier(mergedStack);
					}
				}
			}
			cost *= modMultiplier;
			if (caster instanceof EntityPlayer) {
                if (pAffinity.getAffinityDepth(Affinity.ARCANE) > 0.5f) {
                    float reduction = (float) (1 - (0.5 * pAffinity.getAffinityDepth(Affinity.ARCANE)));
                    cost *= reduction;
                }
            }
			return cost;
		} catch (Exception e) {
			return 0;
		}
	}
	
	public static float getBurnoutCost(ItemStack stack) {
		if (stack.getTagCompound() == null)
			return 0;
		ItemStack mergedStack = merge(stack);
		try {
			float cost = 0;
			for (int j = 0; j < NBTUtils.getAM2Tag(mergedStack.getTagCompound()).getInteger("StageNum"); j++) { 
				NBTTagList stageTag = NBTUtils.addCompoundList(NBTUtils.getAM2Tag(mergedStack.getTagCompound()), STAGE + j);
				for (int i = 0; i < stageTag.tagCount(); i++) {
					NBTTagCompound tmp = stageTag.getCompoundTagAt(i);
					String type = tmp.getString(TYPE);
					if (type.equalsIgnoreCase(TYPE_COMPONENT)) {
						SpellComponent component = SpellRegistry.getComponentFromName(tmp.getString(ID));
						cost += component.burnout(ArsMagica2.proxy.getLocalPlayer());
					}
				}
			}
			return cost;
		} catch (Exception e) {
			return 0;
		}
	}
	
	public static SpellCastResult applyStackStage(ItemStack stack, EntityLivingBase caster, EntityLivingBase target, double x, double y, double z, @Nullable EnumFacing side, World world, boolean consumeMBR, boolean giveXP, int ticksUsed) {
		if (caster.isPotionActive(PotionEffectsDefs.silence))
			return SpellCastResult.SILENCED;
		
		IEntityExtension ext = EntityExtension.For(caster);
		int group = NBTUtils.getAM2Tag(stack.getTagCompound()).getInteger("CurrentGroup");
		if (group == 0) {
			stack = merge(stack.copy());
		}
		SpellShape shape = getShapeForStage(stack, group);
		//if (!(caster instanceof EntityPlayer))
		//	return SpellCastResult.EFFECT_FAILED;
		if (shape instanceof MissingShape) {
			return SpellCastResult.MALFORMED_SPELL_STACK;
		}
		float manaCost = getManaCost(stack, caster);
		manaCost *= 1F + (float)((float)EntityExtension.For(caster).getCurrentBurnout() / (float)EntityExtension.For(caster).getMaxBurnout());
		SpellCastEvent.Pre pre = new SpellCastEvent.Pre(caster, stack, manaCost);
		MinecraftForge.EVENT_BUS.post(pre);
		manaCost = pre.manaCost;
		
		if (consumeMBR) {
			if ((!ext.hasEnoughtMana(manaCost) && (caster instanceof EntityPlayer)) && !((EntityPlayer)caster).capabilities.isCreativeMode) {
				if (world.isRemote)
				AMGuiHelper.instance.flashManaBar();
				return SpellCastResult.NOT_ENOUGH_MANA;
			}
			if (!casterHasAllReagents(caster, stack)) {
				if (world.isRemote)
					caster.sendMessage(new TextComponentString(getMissingReagents(caster, stack)));
				return SpellCastResult.REAGENTS_MISSING;
			}
		}
		
		SpellCastResult result = SpellCastResult.EFFECT_FAILED;
		
		ItemStack stack2 = stack.copy();
		NBTUtils.getAM2Tag(stack2.getTagCompound()).setInteger("CurrentGroup", group + 1);
		if (group == 0)
			result = shape.beginStackStage((ItemSpellBase)stack.getItem(), stack2, caster, target, world, x, y, z, side, giveXP, ticksUsed);
		else {
			NBTUtils.getAM2Tag(stack.getTagCompound()).setInteger("CurrentGroup", group + 1);
			result = shape.beginStackStage((ItemSpellBase)stack.getItem(), stack, caster, target, world, x, y, z, side, giveXP, ticksUsed);
		}
		//SUCCESS is the default return
		//SUCCESS_REDUCE_MANA is basically there because i don't know where to
		//MALFORMED_SPELL_STACK means we reached the end of the spell
		if (caster instanceof EntityPlayer) {
			if (consumeMBR && !((EntityPlayer) caster).capabilities.isCreativeMode && (result == SpellCastResult.SUCCESS || result == SpellCastResult.SUCCESS_REDUCE_MANA || result == SpellCastResult.MALFORMED_SPELL_STACK)) {
				ext.deductMana(manaCost);
				ext.setCurrentBurnout(ext.getCurrentBurnout() + getBurnoutCost(stack));
				consumeReagents(caster, stack);
				if (ext.getCurrentBurnout() > ext.getMaxBurnout())
					ext.setCurrentBurnout(ext.getMaxBurnout());
			}
		}
		
		return SpellCastResult.SUCCESS;
	}
	
	public static boolean casterHasAllReagents(EntityLivingBase caster, ItemStack spellStack){
		if (caster instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) caster;
			if (player.capabilities.isCreativeMode) return true;
			for (SpellComponent part : getComponentsForStage(spellStack, -1)) {
				if (part.reagents(caster) == null) continue;
				for (ItemStack stack : part.reagents(caster)) {
					if (stack != null) {
						boolean foundMatch = false;
						for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
							ItemStack is = player.inventory.getStackInSlot(i);
							if (is == null) continue;
							if (is.getItem() == stack.getItem() && (stack.getItemDamage() == OreDictionary.WILDCARD_VALUE || is.getItemDamage() == stack.getItemDamage())) {
								if (is.getCount() >= stack.getCount()) {
									foundMatch = true;
									break;
								}
							}
						}
						if (!foundMatch) return false;
					}
				}
			}
		}
		return true;
	}
	
	public static String getMissingReagents(EntityLivingBase caster, ItemStack spellStack) {
		if (caster instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) caster;
			if (player.capabilities.isCreativeMode) return "";
			StringBuilder string = new StringBuilder(I18n.translateToLocal("am2.tooltip.missingReagents"));
			boolean first = true;
			for (SpellComponent part : getComponentsForStage(spellStack, -1)) {
				if (part.reagents(caster) == null) continue;
				for (ItemStack stack : part.reagents(caster)) {
					if (stack != null) {
						boolean foundMatch = false;
						for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
							ItemStack is = player.inventory.getStackInSlot(i);
							if (is == null) continue;
							if (is.getItem() == stack.getItem() && (stack.getItemDamage() == OreDictionary.WILDCARD_VALUE || is.getItemDamage() == stack.getItemDamage())) {
								if (is.getCount() >= stack.getCount()) {
									foundMatch = true;
									break;
								}
							}
						}
						if (!foundMatch)  {
							if (!first) string.append(", ");
							string.append(stack.getCount()).append("x ").append(stack.getDisplayName());
							first = false;
						}
					}
				}
			}
			return string.toString();
		}
		return "";		
	}
	
	public static void consumeReagents(EntityLivingBase caster, ItemStack spellStack) {
		if (caster instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) caster;
			if (player.capabilities.isCreativeMode) return;
			for (SpellComponent part : getComponentsForStage(spellStack, -1)) {
				if (part.reagents(caster) == null) continue;
				for (ItemStack stack : part.reagents(caster)) {
					if (stack != null) {
						for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
							ItemStack is = player.inventory.getStackInSlot(i);
							if (is == null) continue;
							if (is.getItem() == stack.getItem() && (stack.getItemDamage() == OreDictionary.WILDCARD_VALUE || is.getItemDamage() == stack.getItemDamage())) {
								if (is.getCount() >= stack.getCount()) {
									is.shrink(stack.getCount());
									if (is.getCount() <= 0) {
										player.inventory.setInventorySlotContents(i, null);
									} else {
										player.inventory.setInventorySlotContents(i, is);
									}
									break;
								}
							}
						}
					}
				}
			}
		}	
	}


	public static double getModifiedStat (double defaultValue, int operation, ItemStack stack, EntityLivingBase caster, Entity target, World world, int stage, SpellModifiers modified) {
		double val = defaultValue;
		if (stage != -1) {
			NBTTagList stageTag = NBTUtils.addCompoundList(NBTUtils.getAM2Tag(stack.getTagCompound()), STAGE + stack);
			for (int i = 0; i < stageTag.tagCount(); i++) {
				NBTTagCompound tag = stageTag.getCompoundTagAt(i);
				String tagType = tag.getString(TYPE);
				if (tagType.equalsIgnoreCase(TYPE_MODIFIER)) {
					String tagID = tag.getString(ID);
					SpellModifier mod = SpellRegistry.getModifierFromName(tagID);
					if (mod.getAspectsModified().contains(modified))
						val = makeCalculation(operation, val, mod.getModifier(modified, caster, target, world, stack.getTagCompound()));
				}
			}
		} else {
			for (int j = 0; j < NBTUtils.getAM2Tag(stack.getTagCompound()).getInteger("StageNum"); j++) { 
				NBTTagList stageTag = NBTUtils.addCompoundList(NBTUtils.getAM2Tag(stack.getTagCompound()), STAGE + j);
				for (int i = 0; i < stageTag.tagCount(); i++) {
					NBTTagCompound tag = stageTag.getCompoundTagAt(i);
					String tagType = tag.getString(TYPE);
					if (tagType.equalsIgnoreCase(TYPE_MODIFIER)) {
						String tagID = tag.getString(ID);
						SpellModifier mod = SpellRegistry.getModifierFromName(tagID);
						if (mod.getAspectsModified().contains(modified)) {
							val = makeCalculation(operation, val, mod.getModifier(modified, caster, target, world, stack.getTagCompound()));
						}
					}
				}
			}
		}
		return val;
	}
	
	private static double makeCalculation (int operation, double val, double mod ) {
		if (operation == 0) {
			return val + mod;
		}
		if (operation == 1) {
			return val - mod;
		}
		if (operation == 2) {
			return val * mod;
		}
		if (operation == 3) {
			return val / mod;
		}
		return 0;
	}
	
	/**
	 * 
	 * @param operation 0 add, 1 subtract, 2 multiply, 3 divide
	 * @param stage set to -1 for all;
	 * @param modified
	 * @param stack
	 * @return
	 */
	public static double getModifiedStat (int operation, ItemStack stack, EntityLivingBase caster, Entity target, World world, int stage, SpellModifiers modified) {
		return getModifiedStat(modified.defaultValue, operation, stack, caster, target, world, stage, modified);
	}
	
	public static ArrayList<SpellModifier> getModifiersForStage (ItemStack stack, int stage) {
		ArrayList<SpellModifier> mods = new ArrayList<SpellModifier>();
		if (stack.getTagCompound() == null)
			return mods;
		if (stage != -1) {
			NBTTagList stageTag = NBTUtils.addCompoundList(NBTUtils.getAM2Tag(stack.getTagCompound()), STAGE + stage);
			for (int i = 0; i < stageTag.tagCount(); i++) {
				NBTTagCompound tag = stageTag.getCompoundTagAt(i);
				String tagType = tag.getString(TYPE);
				if (tagType.equalsIgnoreCase(TYPE_MODIFIER)) {
					mods.add(SpellRegistry.getModifierFromName(tag.getString(ID)));
				}
			}
		} else {
			for (int j = 0; j <= NBTUtils.getAM2Tag(stack.getTagCompound()).getInteger("StageNum"); j++) {

				NBTTagList stageTag = NBTUtils.addCompoundList(NBTUtils.getAM2Tag(stack.getTagCompound()), STAGE + j);
				for (int i = 0; i < stageTag.tagCount(); i++) {
					NBTTagCompound tag = stageTag.getCompoundTagAt(i);
					String tagType = tag.getString(TYPE);
					if (tagType.equalsIgnoreCase(TYPE_MODIFIER)) {
						mods.add(SpellRegistry.getModifierFromName(tag.getString(ID)));
					}
				}
			}
		}
		return mods;
	}
	
	public static ArrayList<AbstractSpellPart> getPartsForGroup (ItemStack stack, int group) {
		ArrayList<AbstractSpellPart> mods = new ArrayList<>();
		try {
		NBTTagCompound compound = (NBTTagCompound) NBTUtils.addCompoundList(NBTUtils.getAM2Tag(stack.getTagCompound()), "ShapeGroups").getCompoundTagAt(NBTUtils.getAM2Tag(stack.getTagCompound()).getInteger("CurrentShapeGroup")).copy();
		for (int j = 0; j <= compound.getInteger("StageNum"); j++) { 
			NBTTagList stageTag = NBTUtils.addCompoundList(compound, STAGE + j);
			for (int i = 0; i < stageTag.tagCount(); i++) {
				NBTTagCompound tag = stageTag.getCompoundTagAt(i);
				String tagType = tag.getString(TYPE);
				if (tagType.equalsIgnoreCase(TYPE_MODIFIER)) {
					mods.add(SpellRegistry.getModifierFromName(tag.getString(ID)));
				}
				if (tagType.equalsIgnoreCase(TYPE_SHAPE)) {
					mods.add(SpellRegistry.getShapeFromName(tag.getString(ID)));
				}
			}
		}
		return mods;
		} catch (Exception e) {
			return new ArrayList<>();
		}
	}
	
	public static ArrayList<AbstractSpellPart> getPartsForSpell (ItemStack stack) {
		try {
			ArrayList<AbstractSpellPart> mods = new ArrayList<AbstractSpellPart>();
			for (int j = 0; j <= NBTUtils.getAM2Tag(stack.getTagCompound()).getInteger("StageNum"); j++) { 
				NBTTagList stageTag = NBTUtils.addCompoundList(NBTUtils.getAM2Tag(stack.getTagCompound()), STAGE + j);
				for (int i = 0; i < stageTag.tagCount(); i++) {
					NBTTagCompound tag = stageTag.getCompoundTagAt(i);
					mods.add(ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation(tag.getString(ID))));
				}
			}
			return mods;
		} catch (Exception e) {
			return Lists.newArrayList();
		}
	}
	
	public static ArrayList<SpellComponent> getComponentsForStage (ItemStack stack, int stage) {
		try {
			ArrayList<SpellComponent> mods = new ArrayList<SpellComponent>();
			if (stage != -1) {
				NBTTagList stageTag = NBTUtils.addCompoundList(NBTUtils.getAM2Tag(stack.getTagCompound()), STAGE + stage);
				for (int i = 0; i < stageTag.tagCount(); i++) {
					NBTTagCompound tag = stageTag.getCompoundTagAt(i);
					String tagType = tag.getString(TYPE);
					if (tagType.equalsIgnoreCase(TYPE_COMPONENT)) {
						mods.add(SpellRegistry.getComponentFromName(tag.getString(ID)));
					}
				}
			} else {
				for (int j = 0; j <= NBTUtils.getAM2Tag(stack.getTagCompound()).getInteger("StageNum"); j++) { 
					NBTTagList stageTag = NBTUtils.addCompoundList(NBTUtils.getAM2Tag(stack.getTagCompound()), STAGE + j);
					for (int i = 0; i < stageTag.tagCount(); i++) {
						NBTTagCompound tag = stageTag.getCompoundTagAt(i);
						String tagType = tag.getString(TYPE);
						if (tagType.equalsIgnoreCase(TYPE_COMPONENT)) {
							mods.add(SpellRegistry.getComponentFromName(tag.getString(ID)));
						}
					}
				}
			}
			return mods;
		} catch (Exception e) {
			return Lists.newArrayList();
		}
	}
	
	public static SpellCastResult applyStageToGround(ItemStack stack, EntityLivingBase caster, World world, BlockPos pos, EnumFacing blockFace, double impactX, double impactY, double impactZ, boolean consumeMBR){
		SpellShape stageShape = SpellUtils.getShapeForStage(stack, 0);
		if (stageShape == null || stageShape == SpellDefs.MISSING_SHAPE){
			return SpellCastResult.MALFORMED_SPELL_STACK;
		}
		boolean isPlayer = caster instanceof EntityPlayer;
		int group = NBTUtils.getAM2Tag(stack.getTagCompound()).getInteger("CurrentGroup");
		ArrayList<SpellComponent> components = SpellUtils.getComponentsForStage(stack, group);
		for (SpellComponent component : components){
			if (component.applyEffectBlock(stack, world, pos, blockFace, impactX, impactY, impactZ, caster)){
				if (isPlayer && !world.isRemote) {
					if (component.getAffinity() != null) {
						AffinityShiftUtils.doAffinityShift(caster, component, stageShape);
					}
				}
				if (world.isRemote){
					int color = -1;
					if (modifierIsPresent(SpellModifiers.COLOR, stack)){
						ArrayList<SpellModifier> mods = SpellUtils.getModifiersForStage(stack, -1);
						for (SpellModifier mod : mods){
							if (mod instanceof Colour){
								color = (int)mod.getModifier(SpellModifiers.COLOR, null, null, null, NBTUtils.getAM2Tag(stack.getTagCompound()));
							}
						}
					}
					component.spawnParticles(world, pos.getX(), pos.getY(), pos.getZ(), caster, caster, world.rand, color);
				}
			}
		}

		return SpellCastResult.SUCCESS;
	}

	public static SpellCastResult applyStageToEntity(ItemStack stack, EntityLivingBase caster, World world, Entity target, boolean shiftAffinityAndXP){
		SpellShape stageShape = SpellUtils.getShapeForStage(stack, 0);
		if (stageShape == null) return SpellCastResult.MALFORMED_SPELL_STACK;

//		if ((!AMCore.config.getAllowCreativeTargets()) && target instanceof EntityPlayerMP && ((EntityPlayerMP) target).capabilities.isCreativeMode) {
//			return SpellCastResult.EFFECT_FAILED;
//		}
		int group = NBTUtils.getAM2Tag(stack.getTagCompound()).getInteger("CurrentGroup");
		ArrayList<SpellComponent> components = SpellUtils.getComponentsForStage(stack, group);

		boolean appliedOneComponent = false;
		boolean isPlayer = caster instanceof EntityPlayer;

		for (SpellComponent component : components){

//			if (SkillTreeManager.instance.isSkillDisabled(component))
//				continue;

			if (component.applyEffectEntity(stack, world, caster, target)){
				if (isPlayer && !world.isRemote) {
					if (component.getAffinity() != null) {
						AffinityShiftUtils.doAffinityShift(caster, component, stageShape);
					}
				}
				appliedOneComponent = true;
				if (world.isRemote){
					int color = -1;
					if (SpellUtils.modifierIsPresent(SpellModifiers.COLOR, stack)){
						ArrayList<SpellModifier> mods = SpellUtils.getModifiersForStage(stack, -1);
						for (SpellModifier mod : mods){
							if (mod instanceof Colour){
								color = (int)mod.getModifier(SpellModifiers.COLOR, null, null, null, NBTUtils.getAM2Tag(stack.getTagCompound()));
							}
						}
					}
					component.spawnParticles(world, target.posX, target.posY + target.getEyeHeight(), target.posZ, caster, target, world.rand, color);
				}
				if (caster instanceof EntityPlayer) {
					AffinityShiftUtils.doAffinityShift(caster, component, stageShape);
				}
			}
		}

		if (appliedOneComponent)
			return SpellCastResult.SUCCESS;
		else
			return SpellCastResult.EFFECT_FAILED;
	}

	public static int currentStage(ItemStack spellStack) {
		return NBTUtils.getAM2Tag(spellStack.getTagCompound()).getInteger("CurrentGroup");
	}

	public static boolean componentIsPresent(ItemStack stack, Class<? extends SpellComponent> clazz) {
		for (SpellComponent comp : getComponentsForStage(stack, currentStage(stack)))
			if (clazz.isInstance(comp))
				return true;
		return false;
	}

	public static int getModifiedInt_Mul(int defaultValue, ItemStack stack, EntityLivingBase caster, Entity target, World world, SpellModifiers modified) {
		return (int) getModifiedStat(defaultValue, 2, stack, caster, target, world, -1, modified);
	}
	
	public static double getModifiedDouble_Mul(double defaultValue, ItemStack stack, EntityLivingBase caster, Entity target, World world, SpellModifiers modified) {
		return getModifiedStat(defaultValue, 2, stack, caster, target, world, -1, modified);
	}
	
	public static double getModifiedDouble_Mul(ItemStack stack, EntityLivingBase caster, Entity target, World world, SpellModifiers modified) {
		return getModifiedStat(modified.defaultValue, 2, stack, caster, target, world, -1, modified);
	}
	
	public static int getModifiedInt_Add(int defaultValue, ItemStack stack, EntityLivingBase caster, Entity target, World world, SpellModifiers modified) {
		return (int) getModifiedDouble_Add(defaultValue, stack, caster, target, world, modified);
	}
	
	public static double getModifiedDouble_Add(double defaultValue, ItemStack stack, EntityLivingBase caster, Entity target, World world, SpellModifiers modified) {
		return getModifiedStat(defaultValue, 0, stack, caster, target, world, -1, modified);
	}
	
	public static double getModifiedDouble_Add(ItemStack stack, EntityLivingBase caster, Entity target, World world, SpellModifiers modified) {
		return getModifiedStat(modified.defaultValueInt, 0, stack, caster, target, world, -1, modified);
	}

	public static int getModifiedInt_Add(ItemStack stack, EntityLivingBase caster, EntityLivingBase target, World world, SpellModifiers modified) {
		return getModifiedInt_Add(modified.defaultValueInt, stack, caster, target, world, modified);
	}

	public static String getSpellMetadata(ItemStack stack, String string) {
		return NBTUtils.addTag(NBTUtils.getAM2Tag(stack.getTagCompound()), SPELL_DATA).getString(string);
	}

	public static void setSpellMetadata(ItemStack stack, String string, String s) {
		NBTUtils.addTag(NBTUtils.getAM2Tag(stack.getTagCompound()), SPELL_DATA).setString(string, s);
	}
	
	public static void setSpellMetadata(NBTTagCompound stack, String string, String s) {
		NBTUtils.addTag(NBTUtils.getAM2Tag(stack), SPELL_DATA).setString(string, s);
	}

	public static int numShapeGroups(ItemStack stack) {
		return NBTUtils.getAM2Tag(stack.getTagCompound()).getInteger("NumShapeGroups");
	}

	public static ArrayList<AbstractSpellPart> getShapeGroupParts(ItemStack stack, int shapeGroup) {
		try {
			ArrayList<AbstractSpellPart> mods = new ArrayList<AbstractSpellPart>();
			NBTTagCompound tag = NBTUtils.addCompoundList(NBTUtils.getAM2Tag(stack.getTagCompound()), "ShapeGroups").getCompoundTagAt(shapeGroup);
			for (int j = 0; j <= tag.getInteger("StageNum"); j++) { 
				NBTTagList stageTag = NBTUtils.addCompoundList(tag, STAGE + j);
				for (int i = 0; i < stageTag.tagCount(); i++) {
					NBTTagCompound tmp = stageTag.getCompoundTagAt(i);
					mods.add(SpellRegistry.getComponentFromName(tmp.getString(ID)));
				}
			}
			return mods;
		} catch (Exception e) {
			return Lists.newArrayList();
		}
	}

	public static ItemStack createSpellStack( ArrayList<KeyValuePair<ArrayList<AbstractSpellPart>, NBTTagCompound>> shapeGroupSetup, KeyValuePair<ArrayList<AbstractSpellPart>, NBTTagCompound> curRecipeSetup) {
		return createSpellStack(shapeGroupSetup, curRecipeSetup.key, curRecipeSetup.value);
	}

	public static SpellShape getShapeForStage(ItemStack stack) {
		return getShapeForStage(stack, currentStage(stack));
	}
	
	public static HashMap<Affinity, Float> AffinityFor(ItemStack stack) {
		HashMap<Affinity, Float> customDepthMap = new HashMap<>();
		ArrayList<SpellComponent> components = SpellUtils.getComponentsForStage(stack, -1);
		for (SpellComponent component : components) {
			for (Affinity aff1 : component.getAffinity()) {
				if (customDepthMap.get(aff1) != null) {
					customDepthMap.put(aff1, customDepthMap.get(aff1) + component.getAffinityShift(aff1));
				} else {
					customDepthMap.put(aff1, component.getAffinityShift(aff1));
				}
			}
		}
		return customDepthMap;
	}
	
	public static SpellCastResult applyStackStageOnUsing(ItemStack stack, EntityLivingBase caster, EntityLivingBase target, double x, double y, double z, World world, boolean consumeMBR, boolean giveXP, int ticks){
		if (SpellUtils.numStages(stack) == 0){
			return SpellCastResult.SUCCESS;
		}

		if (!SpellUtils.getShapeForStage(stack).isChanneled()){
			return SpellCastResult.EFFECT_FAILED;
		}

		return applyStackStage(stack, caster, target, x, y, z, null, world, consumeMBR, giveXP, ticks);
	}

	public static int cycleShapeGroup(ItemStack stack) {
		if (!stack.hasTagCompound())
			return 0;
		int current = NBTUtils.getAM2Tag(stack.getTagCompound()).getInteger("CurrentShapeGroup");
		int max = NBTUtils.getAM2Tag(stack.getTagCompound()).getInteger("NumShapeGroups");
		if (max == 0)
			return 0;
		return (current + 1) % max;
	}

	public static void setShapeGroup(ItemStack stack, int newShapeGroupOrdinal) {
		if (stack.hasTagCompound())
			NBTUtils.getAM2Tag(stack.getTagCompound()).setInteger("CurrentShapeGroup", newShapeGroupOrdinal);
	}
	
}

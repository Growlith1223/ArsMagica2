package am2.affinity;

import java.util.UUID;

import net.minecraft.block.material.Material;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class AffinityAbilityModifiers {

	public static AffinityAbilityModifiers instance = new AffinityAbilityModifiers();

	public static final int OPERATION_ADD = 2;
	public static final int OPERATION_MULTIPLY = 0;
	public static final int OPERATION_DIVIDE = 1;

	//Nature Affinity Modifiers

	public static final UUID natureAffinityModifierID = UUID.fromString("d0a02b30-c830-11e3-9c1a-0800200c9a66");
	public static final AttributeModifier natureAffinityRoots = new AttributeModifier(natureAffinityModifierID, "Nature's Roots", -0.1, OPERATION_ADD);

	// Ice Affinity Speed Modifiers

	public static final UUID iceAffinityodifierID = UUID.fromString("d0a02b30-c830-11e3-9c1a-0800200c9a65");
	public static final AttributeModifier iceAffinityColdBlooded = new AttributeModifier(iceAffinityodifierID, "Cold Blooded", -0.1, OPERATION_ADD);

	//Lightning Affinity Speed Modifiers
	public static final UUID lightningAffinityModifierID = UUID.fromString("3b51a94c-8866-470b-8b69-e1d5cb50a61f");
	public static final AttributeModifier lightningAffinitySpeed = new AttributeModifier(lightningAffinityModifierID, "Lightning Reflexes", 1.20, OPERATION_ADD);

	public static final UUID waterWeaknessID = UUID.fromString("3b51a94c-7844-732b-8b69-a1f5cd50a60f");
	public static final AttributeModifier waterWeakness = new AttributeModifier(waterWeaknessID, "Water Weakness", -0.25, OPERATION_ADD);
	public static final UUID fireWeaknessID = UUID.fromString("3b51a94c-7844-732b-8b69-a1f5cd50a60e");
	public static final AttributeModifier fireWeakness = new AttributeModifier(fireWeaknessID, "Fire Weakness", -0.25, OPERATION_ADD);
	public static final UUID sunlightWeaknessID = UUID.fromString("3b51a94c-7844-732b-8b69-a1f5cd50a60d");
	public static final AttributeModifier sunlightWeakness = new AttributeModifier(sunlightWeaknessID, "Sunlight Weakness", -0.25, OPERATION_ADD);

	public void applySpeedModifiersBasedOnDepth(EntityPlayer ent, float natureDepth, float iceDepth, float lightningDepth){
		IAttributeInstance attribute = ent.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);

		applyOrRemoveModifier(attribute, natureAffinityRoots, natureDepth >= 0.5f);
		applyOrRemoveModifier(attribute, lightningAffinitySpeed, lightningDepth >= 0.65f);
		applyOrRemoveModifier(attribute, iceAffinityColdBlooded, iceDepth >= 0.1f && !isOnIce(ent));
	}

	public void applyHealthModifiers(EntityPlayer ent, float enderDepth, float waterDepth, float fireDepth, float lightningDepth){
		IAttributeInstance attribute = ent.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);

		applyOrRemoveModifier(attribute, waterWeakness,
				((fireDepth >= 0.5f && fireDepth <= 0.9f) ||
						(enderDepth >= 0.5f && enderDepth <= 0.9f) ||
						(lightningDepth >= 0.5f && lightningDepth <= 0.9f)) && ent.isWet());
		applyOrRemoveModifier(attribute, fireWeakness, (waterDepth >= 0.5f && waterDepth <= 0.9f) && (ent.isBurning() || ent.world.provider.getDimension() == -1));
		int worldTime = (int)ent.world.getWorldTime() % 24000;
		applyOrRemoveModifier(attribute, sunlightWeakness, (enderDepth > 0.65 && enderDepth <= 0.95f) && ent.world.canBlockSeeSky(ent.getPosition()) && (worldTime > 23000 || worldTime < 12500));
	}

	public void applyOrRemoveModifier(IAttributeInstance attribute, AttributeModifier modifier, boolean tryApply){
		if (tryApply){
			if (attribute.getModifier(modifier.getID()) == null){
				attribute.applyModifier(modifier);
			}
		}else{
			if (attribute.getModifier(modifier.getID()) != null){
				attribute.removeModifier(modifier);
			}
		}
	}

	public boolean isOnIce(EntityPlayer ent){
		AxisAlignedBB par1AxisAlignedBB = ent.getEntityBoundingBox().expand(0.0D, -0.4000000059604645D, 0.0D).contract(0.001D);
		int i = MathHelper.floor(par1AxisAlignedBB.minX);
		int j = MathHelper.floor(par1AxisAlignedBB.maxX + 1.0D);
		int k = MathHelper.floor(par1AxisAlignedBB.minY - 1.0D);
		int l = MathHelper.floor(par1AxisAlignedBB.maxY + 1.0D);
		int i1 = MathHelper.floor(par1AxisAlignedBB.minZ);
		int j1 = MathHelper.floor(par1AxisAlignedBB.maxZ + 1.0D);
		boolean isOnIce = false;
		for (int k1 = i; k1 < j && !isOnIce; ++k1){
			for (int l1 = k; l1 < l && !isOnIce; ++l1){
				for (int i2 = i1; i2 < j1 && !isOnIce; ++i2){
					Material block = ent.world.getBlockState(new BlockPos(k1, l1, i2)).getMaterial();
					if (block == Material.ICE || block == Material.PACKED_ICE){
						return true;
					}
				}
			}
		}
		return false;
	}}

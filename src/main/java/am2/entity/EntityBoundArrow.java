package am2.entity;

import javax.annotation.Nullable;

import com.google.common.base.Optional;

import am2.defs.ItemDefs;
import am2.utils.SpellUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;

public class EntityBoundArrow extends EntityArrow {
	
	private static final DataParameter<ItemStack> SPELL_STACK = EntityDataManager.createKey(EntityBoundArrow.class, DataSerializers.OPTIONAL_ITEM_STACK);
	
	public EntityBoundArrow(World worldIn, EntityLivingBase shooter) {
		super(worldIn, shooter);
	}
	
	public EntityBoundArrow(World worldIn) {
		super(worldIn);
	}
	
	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(SPELL_STACK, new ItemStack(ItemDefs.spell));
	}
	
	public void setSpellStack(@Nullable ItemStack stack) {
		this.dataManager.set(SPELL_STACK, stack);
	}
	
	@Override
	protected void arrowHit(EntityLivingBase living) {
		ItemStack stack = dataManager.get(SPELL_STACK);
		if (stack == null || ! stack.hasTagCompound())
			return;
		SpellUtils.applyStackStage(stack, (EntityLivingBase) shootingEntity, living, living.posX, living.posY, living.posZ, null, world, true, true, this.ticksExisted);
	}
	
	@Override
	protected ItemStack getArrowStack() {
		return new ItemStack(ItemDefs.BoundArrow);
	}

}

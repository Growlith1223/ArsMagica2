package am2.spell.component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Sets;

import am2.ArsMagica2;
import am2.api.affinity.Affinity;
import am2.api.blocks.MultiblockStructureDefinition;
import am2.api.extensions.IEntityExtension;
import am2.api.math.AMVector3;
import am2.api.rituals.IRitualInteraction;
import am2.api.rituals.RitualShapeHelper;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellModifiers;
import am2.defs.BlockDefs;
import am2.defs.ItemDefs;
import am2.defs.PotionEffectsDefs;
import am2.extensions.EntityExtension;
import am2.items.ItemOre;
import am2.particles.AMParticle;
import am2.particles.ParticleExpandingCollapsingRingAtPoint;
import am2.utils.DimensionUtilities;
import am2.utils.KeystoneUtilities;
import am2.utils.SelectionUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

@SuppressWarnings("deprecation")
public class Recall extends SpellComponent implements IRitualInteraction{

	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, BlockPos pos, EnumFacing blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster){
		return false;
	}

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target){

		if (!(target instanceof EntityLivingBase)){
			return false;
		}

		if (caster.isPotionActive(PotionEffectsDefs.astralDistortion) || ((EntityLivingBase)target).isPotionActive(PotionEffectsDefs.astralDistortion)){
			if (caster instanceof EntityPlayer)
				((EntityPlayer)caster).sendMessage(new TextComponentString(I18n.translateToLocal("am2.tooltip.cantTeleport")));
			return false;
		}
		if (RitualShapeHelper.instance.matchesRitual(this, world, target.getPosition())) {
			ItemStack[] ritualRunes = RitualShapeHelper.instance.checkForRitual(this, world, target.getPosition());
			if (ritualRunes != null){
				return handleRitualReagents(ritualRunes, world, target.getPosition(), caster, target);
			}
		}

		IEntityExtension casterProperties = EntityExtension.For(caster);
		if (casterProperties.getMarkDimensionID() == -512){
			if (caster instanceof EntityPlayer && !world.isRemote)
				((EntityPlayer)caster).sendMessage(new TextComponentString(I18n.translateToLocal("am2.tooltip.noMark")));
			return false;
		}else if (casterProperties.getMarkDimensionID() != caster.dimension){
			if (caster instanceof EntityPlayer && !world.isRemote)
				((EntityPlayer)caster).sendMessage(new TextComponentString(I18n.translateToLocal("am2.tooltip.diffDimMark")));
			return false;
		}
		if (!world.isRemote){
			((EntityLivingBase)target).setPositionAndUpdate(casterProperties.getMarkX(), casterProperties.getMarkY(), casterProperties.getMarkZ());
		}
		return true;
	}
	
	@Override
	public EnumSet<SpellModifiers> getModifiers() {
		return EnumSet.noneOf(SpellModifiers.class);
	}
	
	private boolean handleRitualReagents(ItemStack[] ritualRunes, World world, BlockPos pos, EntityLivingBase caster, Entity target){

		boolean hasVinteumDust = false;
		for (ItemStack stack : ritualRunes){
			if (stack.getItem() == ItemDefs.itemOre && stack.getItemDamage() == ItemOre.META_VINTEUM){
				hasVinteumDust = true;
				break;
			}
		}

		if (!hasVinteumDust && ritualRunes.length == 3){
			long key = KeystoneUtilities.instance.getKeyFromRunes(ritualRunes);
			AMVector3 vector = ArsMagica2.proxy.blocks.getNextKeystonePortalLocation(world, pos, false, key);
			if (vector == null || vector.equals(new AMVector3(pos))){
				if (caster instanceof EntityPlayer && !world.isRemote)
					((EntityPlayer)caster).sendMessage(new TextComponentString(I18n.translateToLocal("am2.tooltip.noMatchingGate")));
				return false;
			}else{
				RitualShapeHelper.instance.consumeAllReagents(this, world, pos);
				RitualShapeHelper.instance.consumeShape(this, world, pos);
				((EntityLivingBase)target).setPositionAndUpdate(vector.x, vector.y - target.height, vector.z);
				return true;
			}
		}else if (hasVinteumDust){
			ArrayList<Integer> copy = new ArrayList<Integer>();
			for (ItemStack stack : ritualRunes){
				if (stack.getItem() == ItemDefs.rune && stack.getItemDamage() <= 16){
					copy.add(stack.getItemDamage());
				}
			}
			int[] newRunes = new int[copy.size()];
			for (int i = 0; i < copy.size(); i++) {
				newRunes[i] = copy.get(i);
			}
			EntityPlayer player = SelectionUtils.getPlayersForRuneSet(newRunes);

			if (player == null){
				if (caster instanceof EntityPlayer && !world.isRemote)
					((EntityPlayer)caster).sendMessage(new TextComponentString("am2.tooltip.noMatchingPlayer"));
				return false;
			}
			else if (player == caster){
				if (caster instanceof EntityPlayer && !world.isRemote)
					((EntityPlayer)caster).sendMessage(new TextComponentString("am2.tooltip.cantSummonSelf"));
				return false;
			}else{
				RitualShapeHelper.instance.consumeAllReagents(this, world, pos);
				if (target.world.provider.getDimension() != caster.world.provider.getDimension()){
					DimensionUtilities.doDimensionTransfer(player, caster.world.provider.getDimension());
				}
				((EntityLivingBase)target).setPositionAndUpdate(pos.getX(), pos.getY() + 0.5D, pos.getZ());
				return true;
			}
		}
		return false;
	}

	@Override
	public float manaCost(EntityLivingBase caster){
		return 500;
	}
	@Override
	public ItemStack[] reagents(EntityLivingBase caster){
		return null;
	}

	@Override
	public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier){
		for (int i = 0; i < 25; ++i){
			AMParticle particle = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, "arcane", x, y - 1, z);
			if (particle != null){
				particle.addRandomOffset(1, 0, 1);
				particle.AddParticleController(new ParticleExpandingCollapsingRingAtPoint(particle, x, y - 1, z, 0.1, 3, 0.3, 1, false).setCollapseOnce());
				particle.setMaxAge(20);
				particle.setParticleScale(0.2f);
				if (colorModifier > -1){
					particle.setRGBColorF(((colorModifier >> 16) & 0xFF) / 255.0f, ((colorModifier >> 8) & 0xFF) / 255.0f, (colorModifier & 0xFF) / 255.0f);
				}
			}
		}
	}

	@Override
	public Set<Affinity> getAffinity(){
		return Sets.newHashSet(Affinity.ARCANE);
	}

	@Override
	public Object[] getRecipe(){
		return new Object[]{
				new ItemStack(ItemDefs.rune, 1, EnumDyeColor.ORANGE.getDyeDamage()),
				Items.COMPASS,
				new ItemStack(Items.MAP),
				Items.ENDER_PEARL
		};
	}

	@Override
	public float getAffinityShift(Affinity affinity){
		return 0.1f;
	}

	@Override
	public MultiblockStructureDefinition getRitualShape(){
		return RitualShapeHelper.instance.ringedCross;
	}

	@Override
	public ItemStack[] getReagents(){
		return new ItemStack[]{
				new ItemStack(ItemDefs.rune, 1, OreDictionary.WILDCARD_VALUE),
				new ItemStack(ItemDefs.rune, 1, OreDictionary.WILDCARD_VALUE),
				new ItemStack(ItemDefs.rune, 1, OreDictionary.WILDCARD_VALUE)
		};
	}

	@Override
	public int getReagentSearchRadius(){
		return RitualShapeHelper.instance.ringedCross.getWidth();
	}

	@Override
	public void encodeBasicData(NBTTagCompound tag, Object[] recipe) {}
	
	@Override
	@SideOnly(Side.CLIENT)
	public ItemStack getResult() {
		return new ItemStack(BlockDefs.keystoneRecepticle);
	}
}

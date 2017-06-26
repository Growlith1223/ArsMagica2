package am2.packet;

import java.util.ArrayList;

import am2.ArsMagica2;
import am2.LogHelper;
import am2.api.power.IPowerNode;
import am2.api.spell.SpellModifier;
import am2.api.spell.SpellModifiers;
import am2.blocks.tileentity.TileEntityCraftingAltar;
import am2.blocks.tileentity.TileEntityLectern;
import am2.blocks.tileentity.TileEntityObelisk;
import am2.bosses.BossActions;
import am2.bosses.IArsMagicaBoss;
import am2.extensions.AffinityData;
import am2.extensions.EntityExtension;
import am2.extensions.SkillData;
import am2.gui.AMGuiHelper;
import am2.lore.ArcaneCompendium;
import am2.particles.AMParticle;
import am2.particles.ParticleChangeSize;
import am2.particles.ParticleFadeOut;
import am2.particles.ParticleLeaveParticleTrail;
import am2.particles.ParticleMoveOnHeading;
import am2.power.PowerNodeEntry;
import am2.power.PowerNodeRegistry;
import am2.proxy.tick.ClientTickHandler;
import am2.spell.modifier.Colour;
import am2.utils.MathUtilities;
import am2.utils.SpellUtils;
import io.netty.buffer.ByteBufInputStream;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent;
import net.minecraftforge.fml.relauncher.Side;

public class AMPacketProcessorClient extends AMPacketProcessorServer{

	@SubscribeEvent
	public void onPacketData(ClientCustomPacketEvent event){
		ByteBufInputStream bbis = new ByteBufInputStream(event.getPacket().payload());
		byte packetID = -1;
		try{
			if (event.getPacket().getTarget() != Side.CLIENT){
				return;
			}
			//constant details all packets share:  ID, player, and remaining data
			packetID = bbis.readByte();
			EntityPlayer player = Minecraft.getMinecraft().thePlayer;
			byte[] remaining = new byte[bbis.available()];
			bbis.readFully(remaining);
						
			switch (packetID){
//			case AMPacketIDs.SPELL_CAST:
//				handleSpellCast(remaining);
//				break;
//			case AMPacketIDs.MAGIC_LEVEL_UP:
//				handleMagicLevelUpResponse(remaining);
//				break;
			case AMPacketIDs.PARTICLE_SPAWN_SPECIAL:
				ArsMagica2.proxy.particleManager.handleClientPacketData(Minecraft.getMinecraft().theWorld, remaining);
				break;
//			case AMPacketIDs.PLAYER_VELOCITY_ADD:
//				handleVelocityAdd(remaining);
//				break;
			case AMPacketIDs.FLASH_ARMOR_PIECE:
				handleFlashArmor(remaining);
				break;
//			case AMPacketIDs.REMOVE_BUFF_EFFECT:
//				handleRemoveBuffEffect(remaining);
//				break;
			case AMPacketIDs.SYNC_EXTENDED_PROPS:
				handleSyncProps(remaining, player);
				break;
//			case AMPacketIDs.SYNC_BETA_PARTICLES:
//				handleSyncBetaParticles(remaining);
//				break;
//			case AMPacketIDs.REQUEST_BETA_PARTICLES:
//				handleRequestBetaParticles(remaining);
//				break;
//			case AMPacketIDs.SYNC_AIR_CHANGE:
//				handleSyncAir(remaining);
//				break;
			case AMPacketIDs.SYNC_AFFINITY_DATA:
				handleSyncAffinity(remaining, player);
				break;
//			case AMPacketIDs.SYNC_SPELL_KNOWLEDGE:
//				handleSyncSpellKnowledge(remaining);
//				break;
			case AMPacketIDs.ENTITY_ACTION_UPDATE:
				handleEntityActionUpdate(remaining, player);
				break;
			case AMPacketIDs.PLAYER_LOGIN_DATA:
				handlePlayerLoginData(remaining, player);
				break;
//			case AMPacketIDs.NBT_DUMP:
//				handleNBTDump(player);
//				break;
//			case AMPacketIDs.SET_MAG_WORK_REC:
//				handleSetMagicicansWorkbenchRecipe(player);
//				break;
			case AMPacketIDs.SYNC_WORLD_NAME:
				handleSyncWorldName(remaining);
				break;
			case AMPacketIDs.STAR_FALL:
				handleStarFall(remaining);
				break;
//			case AMPacketIDs.HIDDEN_COMPONENT_UNLOCK:
//				Minecraft.getMinecraft().guiAchievement.func_146256_a(ArcaneCompendium.componentUnlock);
//				break;
//			case AMPacketIDs.SPELL_APPLY_EFFECT:
//				handleSpellApplyEffect(remaining);
//				break;
			case AMPacketIDs.HECATE_DEATH:
				handleHecateDeath(remaining);
				break;
			case AMPacketIDs.REQUEST_PWR_PATHS:
				handleRcvPowerPaths(remaining);
				break;
//			case AMPacketIDs.CAPABILITY_CHANGE:
//				handleCapabilityChange(remaining);
//				break;
			case AMPacketIDs.CRAFTING_ALTAR_DATA:
				handleCraftingAltarData(remaining);
				break;
			case AMPacketIDs.LECTERN_DATA:
				handleLecternData(remaining);
				break;
//			case AMPacketIDs.CALEFACTOR_DATA:
//				handleCalefactorData(remaining);
//				break;
			case AMPacketIDs.OBELISK_DATA:
				handleObeliskData(remaining);
				break;
			case AMPacketIDs.MANA_LINK_UPDATE:
				handleManaLinkUpdate(remaining);
				break;
			case AMPacketIDs.SYNC_COMPENDIUM:
				handleCompendiumUpdate(remaining, player);
				break;
			case AMPacketIDs.SYNC_SKILL_DATA:
				handleSkillDataUpdate(remaining, player);
				break;
//			case AMPacketIDs.SYNC_CLIENT:
//				handleClientUpdate(remaining);
//				break;
			}
		}catch (Throwable t){
			LogHelper.error("Client Packet Failed to Handle!");
			LogHelper.error("Packet Type: " + packetID);
			t.printStackTrace();
		}finally{
			try{
				if (bbis != null)
					bbis.close();
			}catch (Throwable t){
				t.printStackTrace();
			}
		}
	}

//	private void handleClientUpdate(byte[] remaining) {
//		AMDataReader reader = new AMDataReader(remaining, false);
//		if (Minecraft.getMinecraft().theWorld != null) {
//			EntityLivingBase ent = (EntityLivingBase) Minecraft.getMinecraft().theWorld.getEntityByID(reader.getInt());
//			if (ent != null)
//				DataSyncExtension.For(ent).handleUpdatePacket(reader);
//		}
//	}

	private void handleManaLinkUpdate(byte[] remaining) {
		AMDataReader reader = new AMDataReader(remaining, false);
		((EntityExtension)EntityExtension.For((EntityLivingBase) Minecraft.getMinecraft().theWorld.getEntityByID(reader.getInt()))).handleManaLinkUpdate(reader);
	}

	private void handleObeliskData(byte[] remaining){
		AMDataReader rdr = new AMDataReader(remaining, false);
		TileEntity te = Minecraft.getMinecraft().theWorld.getTileEntity(new BlockPos (rdr.getInt(), rdr.getInt(), rdr.getInt()));
		if (te == null || !(te instanceof TileEntityObelisk)) return;
		((TileEntityObelisk)te).handlePacket(rdr.getRemainingBytes());
	}
//
//	private void handleCalefactorData(byte[] remaining){
//		AMDataReader rdr = new AMDataReader(remaining, false);
//		TileEntity te = Minecraft.getMinecraft().theWorld.getTileEntity(rdr.getInt(), rdr.getInt(), rdr.getInt());
//		if (te == null || !(te instanceof TileEntityCalefactor)) return;
//		((TileEntityCalefactor)te).handlePacket(rdr.getRemainingBytes());
//	}
//
	private void handleLecternData(byte[] data){
		AMDataReader rdr = new AMDataReader(data, false);
		TileEntity te = Minecraft.getMinecraft().theWorld.getTileEntity(new BlockPos(rdr.getInt(), rdr.getInt(), rdr.getInt()));
		if (te == null || !(te instanceof TileEntityLectern)) return;
		if (rdr.getBoolean())
			((TileEntityLectern)te).setStack(rdr.getItemStack());
		else
			((TileEntityLectern)te).setStack(null);
	}

	private void handleCraftingAltarData(byte[] data){
		AMDataReader rdr = new AMDataReader(data, false);
		TileEntity te = Minecraft.getMinecraft().theWorld.getTileEntity(new BlockPos(rdr.getInt(), rdr.getInt(), rdr.getInt()));
		if (te == null || !(te instanceof TileEntityCraftingAltar)) return;
		((TileEntityCraftingAltar)te).HandleUpdatePacket(rdr.getRemainingBytes());
	}
//
//	private void handleCapabilityChange(byte[] data){
//		AMDataReader rdr = new AMDataReader(data, false);
//		int capability = rdr.getInt();
//		boolean flag = rdr.getBoolean();
//
//		switch (capability){
//		case 1: //canfly
//			Minecraft.getMinecraft().thePlayer.capabilities.allowFlying = flag;
//			break;
//		case 2: //isflying
//			Minecraft.getMinecraft().thePlayer.capabilities.isFlying = flag;
//			break;
//		}
//	}
//
	private void handleRcvPowerPaths(byte[] data){
		AMDataReader rdr = new AMDataReader(data, false);
		byte bite = rdr.getByte();
		NBTTagCompound compound = rdr.getNBTTagCompound();
		if (bite == 0){
			PowerNodeEntry pnd = PowerNodeRegistry.For(Minecraft.getMinecraft().theWorld).parseFromNBT(compound);
			ArsMagica2.proxy.receivePowerPathVisuals(pnd.getNodePaths());
		}else if (bite == 1){
			int x = rdr.getInt();
			int y = rdr.getInt();
			int z = rdr.getInt();
			ArsMagica2.proxy.setTrackedPowerCompound((NBTTagCompound)compound.copy());
			TileEntity te = Minecraft.getMinecraft().theWorld.getTileEntity(new BlockPos(x, y, z));
			if (te != null && te instanceof IPowerNode)
				PowerNodeRegistry.For(Minecraft.getMinecraft().theWorld).setDataCompoundForNode((IPowerNode<?>)te, compound);
		}
	}

	private void handleHecateDeath(byte[] data){
		AMDataReader rdr = new AMDataReader(data, false);
		double x = rdr.getDouble();
		double y = rdr.getDouble();
		double z = rdr.getDouble();

		World world = Minecraft.getMinecraft().theWorld;

		for (int i = 0; i < 10 * ArsMagica2.config.getGFXLevel(); ++i){
			world.spawnParticle(EnumParticleTypes.FLAME, x, y + 1, z, world.rand.nextDouble() - 0.5, world.rand.nextDouble() - 0.5, world.rand.nextDouble() - 0.5);
		}
	}
//
//	private void handleSpellApplyEffect(byte[] data){
//		AMDataReader rdr = new AMDataReader(data, false);
//		double x = rdr.getDouble();
//		double y = rdr.getDouble();
//		double z = rdr.getDouble();
//		ItemStack spellStack = rdr.getBoolean() ? rdr.getItemStack() : null;
//		if (spellStack == null)
//			return;
//
//		int casterID = rdr.getInt();
//		int targetID = rdr.getInt();
//
//		Entity caster = Minecraft.getMinecraft().theWorld.getEntityByID(casterID);
//		Entity target = Minecraft.getMinecraft().theWorld.getEntityByID(targetID);
//
//		if (caster == null || target == null || !(caster instanceof EntityLivingBase) || !(target instanceof EntityLivingBase))
//			return;
//
//		SpellHelper.instance.applyStackStage(spellStack, (EntityLivingBase)caster, (EntityLivingBase)target, x, y, z, 0, Minecraft.getMinecraft().theWorld, false, false, 0);
//	}
//
	private void handleStarFall(byte[] data){
		AMDataReader rdr = new AMDataReader(data, false);
		double x = rdr.getDouble();
		double y = rdr.getDouble();
		double z = rdr.getDouble();

		ItemStack spellStack = null;
		if (rdr.getBoolean())
			spellStack = rdr.getItemStack();
		
		int color = -1;
		if (spellStack != null){
			if (SpellUtils.modifierIsPresent(SpellModifiers.COLOR, spellStack)){
				ArrayList<SpellModifier> mods = SpellUtils.getModifiersForStage(spellStack, -1);
				for (SpellModifier mod : mods){
					if (mod instanceof Colour){
						color = (int)mod.getModifier(SpellModifiers.COLOR, null, null, null, spellStack.getTagCompound());
					}
				}
			}
		}

		for (int i = 0; i < 360; i += ArsMagica2.config.FullGFX() ? 5 : ArsMagica2.config.LowGFX() ? 10 : 20){
			AMParticle effect = (AMParticle)ArsMagica2.proxy.particleManager.spawn(Minecraft.getMinecraft().theWorld, "sparkle2", x, y + 1.5, z);
			if (effect != null){
				effect.setIgnoreMaxAge(true);
				effect.AddParticleController(new ParticleMoveOnHeading(effect, i, 0, 0.7f, 1, false));
				float clrMod = Minecraft.getMinecraft().theWorld.rand.nextFloat();
				int finalColor = -1;
				if (color == -1)
					finalColor = MathUtilities.colorFloatsToInt(0.24f * clrMod, 0.58f * clrMod, 0.71f * clrMod);
				else{
					float[] colors = MathUtilities.colorIntToFloats(color);
					for (int c = 0; c < colors.length; ++c)
						colors[c] = colors[c] * clrMod;
					finalColor = MathUtilities.colorFloatsToInt(colors[0], colors[1], colors[2]);
				}
				effect.setParticleScale(1.2f);
				effect.setRGBColorI(finalColor);
				effect.AddParticleController(new ParticleFadeOut(effect, 1, false).setFadeSpeed(0.05f).setKillParticleOnFinish(true));
				effect.AddParticleController(
						new ParticleLeaveParticleTrail(effect, "sparkle2", false, 15, 1, false)
								.addControllerToParticleList(new ParticleChangeSize(effect, 1.2f, 0.01f, 15, 1, false))
								.setParticleRGB_I(finalColor)
								.setChildAffectedByGravity()
								.addRandomOffset(0.2f, 0.2f, 0.2f)
				);
			}
		}

//		Minecraft.getMinecraft().theWorld.playSound(x, y, z, "arsmagica2:spell.special.starfall", 2.0f, 1.0f, false);
	}
//
	private void handleSyncWorldName(byte[] data){
		AMDataReader rdr = new AMDataReader(data, false);
		String worldName = rdr.getString();
		ClientTickHandler.worldName = worldName;
	}
//
//
//	private void handleSetMagicicansWorkbenchRecipe(EntityPlayer player){
//		if (player.openContainer != null && player.openContainer instanceof ContainerMagiciansWorkbench){
//			((ContainerMagiciansWorkbench)player.openContainer).updateCraftingMatrices();
//			((ContainerMagiciansWorkbench)player.openContainer).onCraftMatrixChanged(((ContainerMagiciansWorkbench)player.openContainer).getWorkbench());
//		}
//	}
//
//	private void handleNBTDump(EntityPlayer player){
//		ItemStack stack = player.getCurrentEquippedItem();
//		if (stack == null || !stack.hasTagCompound()) return;
//		NBTTagCompound compound = stack.writeToNBT(new NBTTagCompound());
//
//		String fileName = "NBTDump_" + System.currentTimeMillis() + ".dat";
//
//		File file = new File(fileName);
//
//		try{
//			CompressedStreamTools.write(compound, file);
//		}catch (IOException e){
//			e.printStackTrace();
//			player.addChatMessage(new ChatComponentText("An error occurred while attempting to write the NBT, check the console for more information."));
//			return;
//		}
//
//		player.addChatMessage(new ChatComponentText("NBT Saved to " + file.getAbsolutePath()));
//	}
//
	private void handlePlayerLoginData(byte[] data, EntityPlayer player){
		AMDataReader rdr = new AMDataReader(data, false);
		int skillTreeLock = rdr.getInt();
		ArsMagica2.config.setSkillTreeSecondaryTierCap(skillTreeLock);
		int[] disabledSkills = rdr.getIntArray();
		double manaCap = rdr.getDouble();

		ArsMagica2.config.setManaCap(manaCap);

		LogHelper.info("Received player login packet.");
		LogHelper.debug("Secondary tree cap: %d", skillTreeLock);
		LogHelper.debug("Disabled skills: %d", disabledSkills.length);
		LogHelper.debug("Mana cap: %.2f", manaCap);
		
		ArsMagica2.disabledSkills.disableAllSkillsIn(disabledSkills);
	}

	private void handleEntityActionUpdate(byte[] data, EntityPlayer player){
		AMDataReader rdr = new AMDataReader(data, false);
		int entityID = rdr.getInt();
		int actionOrdinal = rdr.getInt();

		Entity ent = player.worldObj.getEntityByID(entityID);
		if (ent == null || ent.isDead || !(ent instanceof IArsMagicaBoss)) return;
		((IArsMagicaBoss)ent).setCurrentAction(BossActions.values()[actionOrdinal]);
	}
//
//	private void openUICustomization(){
//		Minecraft.getMinecraft().displayGuiScreen(new GuiHudCustomization());
//	}
//
//	private void handleSyncSpellKnowledge(byte[] data){
//		AMDataReader rdr = new AMDataReader(data, false);
//		byte sub = rdr.getByte();
//		int entityID = rdr.getInt();
//
//		EntityLivingBase ent = AMCore.proxy.getEntityByID(entityID);
//		if (ent == null || !(ent instanceof EntityPlayer)) return;
//
//		if (!SkillData.For((EntityPlayer)ent).handlePacketData(data)){
//			LogHelper.error("Critical Error Handling Spell Knowledge Sync Packet!");
//		}
//	}
//
	private void handleSyncAffinity(byte[] data, EntityPlayer player){
		AMDataReader rdr = new AMDataReader(data, false);
		int entityID = rdr.getInt();

		Entity ent = player.getEntityWorld().getEntityByID(entityID);
		if (ent == null || !(ent instanceof EntityPlayer)) return;

		AffinityData.For((EntityLivingBase) ent).handleUpdatePacket(rdr.getRemainingBytes());
	}
	
	private void handleCompendiumUpdate(byte[] data, EntityPlayer player){
		AMDataReader rdr = new AMDataReader(data, false);
		int entityID = rdr.getInt();

		Entity ent = player.getEntityWorld().getEntityByID(entityID);
		if (ent == null || !(ent instanceof EntityPlayer)) return;

		ArcaneCompendium.For((EntityPlayer) ent).handleUpdatePacket(rdr.getRemainingBytes());
	}
	
	private void handleSkillDataUpdate(byte[] data, EntityPlayer player){
		AMDataReader rdr = new AMDataReader(data, false);
		int entityID = rdr.getInt();

		Entity ent = player.getEntityWorld().getEntityByID(entityID);
		if (ent == null || !(ent instanceof EntityPlayer)) return;

		SkillData.For((EntityLivingBase) ent).handleUpdatePacket(rdr.getRemainingBytes());
	}
//
//	private void handleSyncAir(byte[] data){
//		AMDataReader rdr = new AMDataReader(data, false);
//
//		int entityID = rdr.getInt();
//		int air = rdr.getInt();
//
//		EntityLivingBase ent = AMCore.proxy.getEntityByID(entityID);
//		if (ent == null) return;
//
//		ent.setAir(air);
//	}
//
//	private void handleRequestBetaParticles(byte[] data){
//		AMDataReader rdr = new AMDataReader(data, false);
//
//		int entityID = rdr.getInt();
//		byte[] expropData = rdr.getRemainingBytes();
//
//		EntityLivingBase ent = AMCore.proxy.getEntityByID(entityID);
//		if (ent == null) return;
//
//		ExtendedProperties.For(ent).readAuraData(expropData);
//	}
//
//	private void handleKeystoneGuiOpen(byte[] data){
//		AMDataReader rdr = new AMDataReader(data, false);
//		int x = rdr.getInt();
//		int y = rdr.getInt();
//		int z = rdr.getInt();
//
//		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
//
//		FMLNetworkHandler.openGui(player, AMCore.instance, ArsMagicaGuiIdList.GUI_KEYSTONE_CHEST, Minecraft.getMinecraft().theWorld, x, y, z);
//	}
//
//	private void handlePotionReplace(byte[] data){
//		AMDataReader rdr = new AMDataReader(data, false);
//		int id = rdr.getInt();
//		int duration = rdr.getInt();
//		int amplifier = rdr.getInt();
//
//		EntityPlayer localPlayer = Minecraft.getMinecraft().thePlayer;
//
//		PotionEffect pe = BuffList.buffEffectFromPotionID(id, duration, amplifier);
//		if (pe != null){
//			localPlayer.removePotionEffect(id);
//			localPlayer.addPotionEffect(pe);
//		}
//	}
//
//	private void handleSyncBetaParticles(byte[] data){
//		EntityPlayer localPlayer = Minecraft.getMinecraft().thePlayer;
//		if (!AMCore.proxy.playerTracker.hasAA(localPlayer))
//			return;
//
//		ExtendedProperties e = ExtendedProperties.For(localPlayer);
//
//		AMDataWriter writer = new AMDataWriter();
//		writer.add(e.getAuraIndex());
//		writer.add(e.getAuraBehaviour());
//		writer.add(e.getAuraScale());
//		writer.add(e.getAuraAlpha());
//		writer.add(e.getAuraColorRandomize());
//		writer.add(e.getAuraColorDefault());
//		writer.add(e.getAuraColor());
//		writer.add(e.getAuraDelay());
//		writer.add(e.getAuraQuantity());
//		writer.add(e.getAuraSpeed());
//
//		AMNetHandler.INSTANCE.sendPacketToServer(AMPacketIDs.SYNC_BETA_PARTICLES, writer.generate());
//	}
//
	private void handleSyncProps(byte[] data, EntityPlayer player){
		AMDataReader rdr = new AMDataReader(data, false);
		int entityID = rdr.getInt();

		Entity ent = player.getEntityWorld().getEntityByID(entityID);
		if (ent == null || !(ent instanceof EntityLivingBase)) return;
				
		EntityExtension.For((EntityLivingBase)ent).handleUpdatePacket(rdr.getRemainingBytes());
	}
//
//	private void handleRemoveBuffEffect(byte[] data){
//		AMDataReader rdr = new AMDataReader(data, false);
//		int buffID = rdr.getInt();
//		EntityClientPlayerMP localPlayer = Minecraft.getMinecraft().thePlayer;
//		if (buffID == BuffList.temporalAnchor.id){
//			localPlayer.prevTimeInPortal = 1f;
//			localPlayer.timeInPortal = 1f;
//		}else if (buffID == BuffList.flight.id || buffID == BuffList.levitation.id){
//			localPlayer.capabilities.allowFlying = false;
//			localPlayer.capabilities.isFlying = false;
//			localPlayer.fallDistance = 0f;
//		}
//		localPlayer.removePotionEffect(buffID);
//	}
//
	private void handleFlashArmor(byte[] data){
		AMDataReader rdr = new AMDataReader(data, false);
		AMGuiHelper.instance.blackoutArmorPiece(rdr.getInt(), rdr.getInt());
	}
//
//	private void handleVelocityAdd(byte[] data){
//		AMDataReader rdr = new AMDataReader(data, false);
//		int entityID = rdr.getInt();
//		double velX = rdr.getDouble();
//		double velY = rdr.getDouble();
//		double velZ = rdr.getDouble();
//
//		Entity ent = AMCore.proxy.getEntityByID(entityID);
//		EntityLivingBase localPlayer = Minecraft.getMinecraft().thePlayer;
//		//this is only really required for the local player, as other entities seem to work across the network.
//		if (ent.getEntityId() != localPlayer.getEntityId()){
//			return;
//		}
//		if (localPlayer.motionY > 0) return;
//
//		localPlayer.addVelocity(velX, velY, velZ);
//	}
//
//	private void handleMagicLevelUpResponse(byte[] data){
//		AMDataReader rdr = new AMDataReader(data, false);
//		boolean success = rdr.getBoolean();
//		int newXPLevel = rdr.getInt();
//		if (success){
//			EntityPlayer player = Minecraft.getMinecraft().thePlayer;
//			player.experienceLevel = newXPLevel;
//			ExtendedProperties.For(player).setMagicLevelWithMana(ExtendedProperties.For(player).getMagicLevel() + 1);
//		}
//	}
//
//	private void handleSpellCast(byte[] data){
//		AMDataReader rdr = new AMDataReader(data, false);
//		World world = Minecraft.getMinecraft().theWorld;
//
//		ItemStack stack = rdr.getItemStack();
//		int casterID = rdr.getInt();
//		int targetID = -1;
//		if (rdr.getBoolean())
//			targetID = rdr.getInt();
//		double x = rdr.getDouble();
//		double y = rdr.getDouble();
//		double z = rdr.getDouble();
//		int side = rdr.getInt();
//		int ticksUsed = rdr.getInt();
//
//		Entity caster = world.getEntityByID(casterID);
//
//		if (caster == null || !(caster instanceof EntityLivingBase))
//			return;
//
//		Entity target = null;
//
//		if (targetID != -1){
//			target = world.getEntityByID(targetID);
//
//			if (target == null || !(target instanceof EntityLivingBase))
//				return;
//		}
//
//		SpellHelper.instance.applyStackStage(stack, (EntityLivingBase)caster, (EntityLivingBase)target, x, y, z, side, world, false, false, ticksUsed);
//	}
//
//	public WorldServer[] getWorldServers(){
//		return FMLServerHandler.instance().getServer().worldServers;
//	}
//
//	public EntityLivingBase getEntityByID(int entityID){
//		return AMCore.proxy.getEntityByID(entityID);
//	}
}

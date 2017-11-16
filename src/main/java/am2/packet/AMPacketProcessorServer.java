package am2.packet;

import am2.LogHelper;
import am2.api.math.AMVector3;
import am2.api.power.IPowerNode;
import am2.blocks.tileentity.TileEntityArmorImbuer;
import am2.blocks.tileentity.TileEntityInscriptionTable;
import am2.blocks.tileentity.TileEntityMagiciansWorkbench;
import am2.blocks.tileentity.TileEntityParticleEmitter;
import am2.container.ContainerKeystone;
import am2.container.ContainerMagiciansWorkbench;
import am2.container.ContainerSpellCustomization;
import am2.defs.ItemDefs;
import am2.extensions.AffinityData;
import am2.extensions.EntityExtension;
import am2.extensions.SkillData;
import am2.items.ItemSpellBook;
import am2.lore.ArcaneCompendium;
import am2.power.PowerNodeRegistry;
import am2.utils.SpellUtils;
import io.netty.buffer.ByteBufInputStream;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerCustomPacketEvent;
import net.minecraftforge.fml.relauncher.Side;

@SuppressWarnings("deprecation")
public class AMPacketProcessorServer{

	@SubscribeEvent
	public void onServerPacketData(ServerCustomPacketEvent event){
		ByteBufInputStream bbis = new ByteBufInputStream(event.getPacket().payload());
		byte packetID = -1;
		try{
			if (event.getPacket().getTarget() != Side.SERVER){
				return;
			}

			//constant details all packets share:  ID, player, and remaining data
			packetID = bbis.readByte();
			NetHandlerPlayServer srv = (NetHandlerPlayServer)event.getPacket().handler();
			EntityPlayerMP player = srv.playerEntity;
			byte[] remaining = new byte[bbis.available()];
			bbis.readFully(remaining);
			switch (packetID){
			case AMPacketIDs.SPELL_SHAPE_GROUP_CHANGE:
				handleCastingModeChange(remaining, (EntityPlayerMP)player);
				break;
//			case AMPacketIDs.MAGIC_LEVEL_UP:
//				handleMagicLevelUp(remaining, (EntityPlayerMP)player);
//				break;
//			case AMPacketIDs.SYNC_BETA_PARTICLES:
//				handleSyncBetaParticles(remaining, (EntityPlayerMP)player);
//				break;
//			case AMPacketIDs.POSSIBLE_CLIENT_EXPROP_DESYNC:
//				handlePossibleClientExpropDesync(remaining);
//				break;
//			case AMPacketIDs.REQUEST_BETA_PARTICLES:
//				handleRequestBetaParticles(remaining, (EntityPlayerMP)player);
//				break;
			case AMPacketIDs.SPELL_CUSTOMIZE:
				handleSpellCustomize(remaining, (EntityPlayerMP)player);
				break;
			case AMPacketIDs.SPELLBOOK_CHANGE_ACTIVE_SLOT:
				handleSpellBookChangeActiveSlot(remaining, (EntityPlayerMP)player);
				break;
//			case AMPacketIDs.SYNC_SPELL_KNOWLEDGE:
//				handleSyncSpellKnowledge(remaining, (EntityPlayerMP)player);
//				break;
			case AMPacketIDs.DECO_BLOCK_UPDATE:
				handleDecoBlockUpdate(remaining, (EntityPlayerMP)player);
				break;
			case AMPacketIDs.INSCRIPTION_TABLE_UPDATE:
				handleInscriptionTableUpdate(remaining, (EntityPlayerMP)player);
				break;
			case AMPacketIDs.TK_DISTANCE_SYNC:
				float TK_Distance = new AMDataReader(remaining, false).getFloat();
				EntityExtension.For((EntityPlayerMP)player).setTKDistance(TK_Distance);
				break;
			case AMPacketIDs.SAVE_KEYSTONE_COMBO:
				handleSaveKeystoneCombo(remaining, (EntityPlayerMP)player);
				break;
			case AMPacketIDs.SET_KEYSTONE_COMBO:
				handleSetKeystoneCombo(remaining, (EntityPlayerMP)player);
				break;
			case AMPacketIDs.SET_MAG_WORK_REC:
				handleSetMagiciansWorkbenchRecipe(remaining, (EntityPlayerMP)player);
				break;
//			case AMPacketIDs.RUNE_BAG_GUI_OPEN:
//				handleRuneBagGUIOpen(remaining, (EntityPlayerMP)player);
//				break;
			case AMPacketIDs.M_BENCH_LOCK_RECIPE:
				handleMBenchLockRecipe(remaining, (EntityPlayerMP)player);
				break;
			case AMPacketIDs.IMBUE_ARMOR:
				handleImbueArmor(remaining, (EntityPlayerMP)player);
				break;
			case AMPacketIDs.REQUEST_PWR_PATHS:
				handlePowerPathSync(remaining, (EntityPlayerMP)player);
				break;
//			case AMPacketIDs.SYNC_EXTENDED_PROPS:
//				handleExpropOperation(remaining, (EntityPlayerMP)player);
//				break;
//			case AMPacketIDs.AFFINITY_ACTIVATE:
//				handleAffinityActivate(remaining, player);
//				break;
			case AMPacketIDs.PLAYER_FLIP:
				handlePlayerFlip(remaining, player);
				break;
			case AMPacketIDs.UNLOCK_OCCULUS_ENTRY:
				handleOcculusUnlock(remaining, player);
				break;
			case AMPacketIDs.TOGGLE_ABILITY:
				handleAbilityToggle(remaining, player);
				break;
			}
		}catch (Throwable t){
			LogHelper.error("Server Packet Failed to Handle!");
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

	private void handleAbilityToggle(byte[] remaining, EntityPlayerMP player) {
		AMDataReader reader = new AMDataReader(remaining, false);
		String str = reader.getString();
		boolean newState = !AffinityData.For(player).getAbilityBoolean(str);
		String text = String.format(I18n.translateToLocal("am2.chat.activation"), I18n.translateToLocal("am2.chat.ability_" + str), I18n.translateToLocal(newState ? "am2.chat.enabled" : "am2.chat.disabled"));
		player.addChatComponentMessage(new TextComponentString(text));
		AffinityData.For(player).addAbilityBoolean(str, newState);
	}

	private void handleOcculusUnlock(byte[] remaining, EntityPlayerMP player) {
		String str = new AMDataReader(remaining, false).getString();
		SkillData.For(player).unlockSkill(str);
		ArcaneCompendium.For(player).unlockEntry(str);
	}

	private void handlePlayerFlip(byte[] remaining, EntityPlayerMP player) {
		EntityExtension.For(player).setInverted(new AMDataReader(remaining, false).getBoolean());
	}

//	private void handleAffinityActivate(byte[] data, EntityPlayerMP entity){
//		AffinityData.For(entity).onAffinityAbility();
//	}
//
//	private void handleExpropOperation(byte[] data, EntityPlayerMP player){
//		ExtendedProperties.For(player).performRemoteOp(new AMDataReader(data, false).getInt());
//	}
//
	private void handlePowerPathSync(byte[] data, EntityPlayerMP player){
		AMDataReader rdr = new AMDataReader(data, false);
		byte nom = rdr.getByte();
		if (nom == 1){
			AMVector3 loc = new AMVector3(rdr.getFloat(), rdr.getFloat(), rdr.getFloat());
			TileEntity te = player.worldObj.getTileEntity(loc.toBlockPos());
			if (te != null && te instanceof IPowerNode){
				AMNetHandler.INSTANCE.sendPowerResponseToClient(PowerNodeRegistry.For(player.worldObj).getDataCompoundForNode((IPowerNode<?>)te), player, te);
			}
		}
	}

	private void handleImbueArmor(byte[] data, EntityPlayerMP player){
		AMDataReader rdr = new AMDataReader(data, false);
		TileEntity te = player.worldObj.getTileEntity(new BlockPos (rdr.getInt(), rdr.getInt(), rdr.getInt()));
		if (te != null && te instanceof TileEntityArmorImbuer){
			((TileEntityArmorImbuer)te).imbueCurrentArmor(new ResourceLocation(rdr.getString()));
		}
	}

	private void handleMBenchLockRecipe(byte[] data, EntityPlayerMP player){
		AMDataReader rdr = new AMDataReader(data, false);
		int x = rdr.getInt();
		int y = rdr.getInt();
		int z = rdr.getInt();

		TileEntity te = player.worldObj.getTileEntity(new BlockPos(x, y, z));
		if (te != null && te instanceof TileEntityMagiciansWorkbench){
			((TileEntityMagiciansWorkbench)te).setRecipeLocked(rdr.getInt(), rdr.getBoolean());
			te.getWorld().markAndNotifyBlock(te.getPos(), te.getWorld().getChunkFromBlockCoords(te.getPos()), te.getWorld().getBlockState(te.getPos()), te.getWorld().getBlockState(te.getPos()), 2);
		}
	}
//
//	private void handleRuneBagGUIOpen(byte[] data, EntityPlayerMP player){
//		AMDataReader rdr = new AMDataReader(data, false);
//		int entityID = rdr.getInt();
//
//		if (player == null){
//			return;
//		}
//
//		//open the GUI
//		player.openGui(AMCore.instance, ArsMagicaGuiIdList.GUI_RUNE_BAG, player.worldObj, (int)player.posX, (int)player.posY, (int)player.posZ);
//	}
//
	private void handleSetMagiciansWorkbenchRecipe(byte[] data, EntityPlayerMP player){
		if (player.openContainer != null && player.openContainer instanceof ContainerMagiciansWorkbench){
			((ContainerMagiciansWorkbench)player.openContainer).moveRecipeToCraftingGrid(new AMDataReader(data, false).getInt());
		}
	}

	private void handleSetKeystoneCombo(byte[] data, EntityPlayerMP player){
		if (player.openContainer instanceof ContainerKeystone){
			AMDataReader rdr = new AMDataReader(data, false);
			((ContainerKeystone)player.openContainer).setInventoryToCombination(rdr.getInt());
		}
	}

	private void handleSaveKeystoneCombo(byte[] data, EntityPlayerMP player){
		if (player.openContainer instanceof ContainerKeystone){
			AMDataReader rdr = new AMDataReader(data, false);
			boolean add = rdr.getBoolean();
			String name = rdr.getString();
			int[] metas = new int[]{rdr.getInt(), rdr.getInt(), rdr.getInt()};
			if (add)
				ItemDefs.keystone.addCombination(((ContainerKeystone)player.openContainer).getKeystoneStack(), name, metas);
			else
				ItemDefs.keystone.removeCombination(((ContainerKeystone)player.openContainer).getKeystoneStack(), name);
		}
	}

	private void handleInscriptionTableUpdate(byte[] data, EntityPlayerMP player){
		World world = player.worldObj;
		AMDataReader rdr = new AMDataReader(data, false);
		TileEntity te = world.getTileEntity(new BlockPos (rdr.getInt(), rdr.getInt(), rdr.getInt()));
		if (te == null || !(te instanceof TileEntityInscriptionTable)) return;
		((TileEntityInscriptionTable)te).HandleUpdatePacket(rdr.getRemainingBytes());

		world.markAndNotifyBlock(te.getPos(), te.getWorld().getChunkFromBlockCoords(te.getPos()), te.getWorld().getBlockState(te.getPos()), te.getWorld().getBlockState(te.getPos()), 3);
	}

	private void handleDecoBlockUpdate(byte[] data, EntityPlayerMP player){
		World world = player.worldObj;
		AMDataReader rdr = new AMDataReader(data, false);
		TileEntity te = world.getTileEntity(new BlockPos (rdr.getInt(), rdr.getInt(), rdr.getInt()));
		if (te == null || !(te instanceof TileEntityParticleEmitter)) return;
		((TileEntityParticleEmitter)te).readFromNBT(rdr.getNBTTagCompound());

		world.markAndNotifyBlock(te.getPos(), world.getChunkFromBlockCoords(te.getPos()), world.getBlockState(te.getPos()), world.getBlockState(te.getPos()), 2);
	}

//	private void handleSyncSpellKnowledge(byte[] data, EntityPlayerMP player){
//		SkillData.For(player).handlePacketData(data);
//	}

	private void handleSpellBookChangeActiveSlot(byte[] data, EntityPlayerMP player){
		AMDataReader rdr = new AMDataReader(data, false);
		byte subID = rdr.getByte();
		rdr.getInt();
		int inventorySlot = rdr.getInt();

		ItemStack stack = player.inventory.getStackInSlot(inventorySlot);
		if (stack == null || !(stack.getItem() instanceof ItemSpellBook)) return;

		if (subID == ItemSpellBook.ID_NEXT_SPELL)
			ItemDefs.spellBook.SetNextSlot(stack);
		else if (subID == ItemSpellBook.ID_PREV_SPELL)
			ItemDefs.spellBook.SetPrevSlot(stack);
		else
			return;
	}

	private void handleSpellCustomize(byte[] data, EntityPlayerMP player){
		AMDataReader rdr = new AMDataReader(data, false);

		if (player == null){
			return;
		}

		int IIconIndex = rdr.getInt();
		String name = rdr.getString();

		if (player.openContainer instanceof ContainerSpellCustomization){
			((ContainerSpellCustomization)player.openContainer).setNameAndIndex(name, IIconIndex);
		}
	}

//	private void handleRequestBetaParticles(byte[] data, EntityPlayerMP player){
//		AMDataReader rdr = new AMDataReader(data, false);
//		int requesterID = rdr.getInt();
//		int entityID = rdr.getInt();
//		EntityLivingBase entity = getEntityByID(entityID);
//
//		if (player == null || entity == null || !(entity instanceof EntityPlayer)) return;
//
//		if (!ArsMagica2.proxy.playerTracker.hasAA((EntityPlayer)entity)) return;
//
//		byte[] expropData = EntityExtension.For(entity).getAuraData();
//
//		AMDataWriter writer = new AMDataWriter();
//		writer.add(entity.getEntityId());
//		writer.add(expropData);
//
//		AMNetHandler.INSTANCE.sendPacketToClientPlayer(player, AMPacketIDs.REQUEST_BETA_PARTICLES, writer.generate());
//	}
//
//	private void handlePossibleClientExpropDesync(byte[] data){
//		AMDataReader rdr = new AMDataReader(data, false);
//		int entityID = rdr.getInt();
//
//		EntityLivingBase e = getEntityByID(entityID);
//
//		if (e != null && e instanceof EntityPlayer){
//			ExtendedProperties props = ExtendedProperties.For(e);
//			if (!props.detectPossibleDesync()){
//				props.setFullSync();
//				props.forceSync();
//			}
//		}
//	}
//
//	private void handleSyncBetaParticles(byte[] data, EntityPlayerMP player){
//		AMDataReader rdr = new AMDataReader(data, false);
//
//		if (player == null || !ArsMagica2.proxy.playerTracker.hasAA(player)){
//			return;
//		}
//
//		int index = rdr.getInt();
//		int behaviour = rdr.getInt();
//		float scale = rdr.getFloat();
//		float alpha = rdr.getFloat();
//		boolean randomColor = rdr.getBoolean();
//		boolean defaultColor = rdr.getBoolean();
//		int color = rdr.getInt();
//		int delay = rdr.getInt();
//		int quantity = rdr.getInt();
//		float speed = rdr.getFloat();
//
//		EntityExtension.For(player).updateAuraData(index, behaviour, scale, alpha, randomColor, defaultColor, color, delay, quantity, speed);
//	}
//
	private void handleCastingModeChange(byte[] data, EntityPlayerMP player){
		AMDataReader rdr = new AMDataReader(data, false);
		int newShapeGroupOrdinal = rdr.getInt();
		
		@SuppressWarnings("unused") int index = rdr.getInt();

		ItemStack stack = player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
		if (stack != null){
			if (stack.getItem() == ItemDefs.spell){
				SpellUtils.setShapeGroup(stack, newShapeGroupOrdinal);
			}else if (stack.getItem() == ItemDefs.spellBook || stack.getItem() == ItemDefs.arcaneSpellbook){
				ItemStack spellStack = ((ItemSpellBook)stack.getItem()).GetActiveItemStack(stack);
				SpellUtils.setShapeGroup(spellStack, newShapeGroupOrdinal);
				((ItemSpellBook)stack.getItem()).replaceAciveItemStack(stack, spellStack);
			}
		}
	}
//
//	private void handleMagicLevelUp(byte[] data, EntityPlayerMP player){
//		/*AMDataReader reader = new AMDataReader(data, false);
//		int entityID = reader.getInt();
//		Entity ent = getEntityByID(entityID);
//
//		if (ent == null || !(ent instanceof EntityLiving)) return;
//
//		if (AMCore.proxy.IncreaseEntityMagicLevel((EntityLiving)ent, ent.worldObj)){
//			if (ent instanceof EntityPlayerMP){
//				EntityPlayerMP player = (EntityPlayerMP)ent;
//				AMDataWriter writer = new AMDataWriter();
//				writer.add(true);
//				writer.add(player.experienceLevel);
//				Packet pkt = createArsMagicaClientPacket(AMPacketIDs.MAGIC_LEVEL_UP, writer.generate());
//				PacketDispatcher.sendPacketToPlayer(pkt, (Player) player);
//			}
//		}*/
//
//		//TODO
//	}
//
//	public WorldServer[] getWorldServers(){
//		return FMLServerHandler.instance().getServer().worldServers;
//	}
//
	public EntityLivingBase getEntityByID(int entityID){
		for (EntityLivingBase ent : FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld().<EntityLivingBase>getEntities(EntityLivingBase.class, EntitySelectors.NOT_SPECTATING)) {
			if (ent.getEntityId() == entityID)
				return ent;
		}
		return null;
	}
}

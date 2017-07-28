package am2.packet;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageCapabilities implements IMessage, IMessageHandler<MessageCapabilities, IMessage> {
	
	public EntityPlayer player;
	public boolean state;
	public int id;
	
	
	/**
	 * 
	 * @param player : The entity player to modify
	 * @param id     : the id of the capability (0 = isFlying, 1 = allowFlying)
	 * @param state  : true / false
	 */
	public MessageCapabilities(EntityPlayer player, int id, boolean state) {
		this.player = player;
		this.id = id;
		this.state = state;
	}
	
	public MessageCapabilities() {
		
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		String out = ByteBufUtils.readUTF8String(buf);
		player = Minecraft.getMinecraft().world.getPlayerEntityByUUID(UUID.fromString(out.split("\0")[0]));
		id = Integer.valueOf(out.split("\0")[1]);
		state = Boolean.valueOf(out.split("\0")[2]);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		String in = player.getUniqueID().toString() + "\0" + id + "\0" + state;
		ByteBufUtils.writeUTF8String(buf, in);
	}

	@Override
	public IMessage onMessage(final MessageCapabilities message, final MessageContext ctx) {
		((WorldServer)ctx.getServerHandler().player.world).addScheduledTask(new Runnable () {
			@Override
			public void run() {
				if (message.player == null)
					return;
				if (message.id == 0)
					message.player.capabilities.isFlying = message.state;
				else if (message.id == 1)
					message.player.capabilities.allowFlying = message.state;
			}
		});
		return null;
	}
}
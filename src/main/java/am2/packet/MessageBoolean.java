package am2.packet;

import am2.extensions.AffinityData;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageBoolean implements IMessage {
	
	private boolean bool;
	private String id;
	
	public MessageBoolean() {
	}
	
	public MessageBoolean(String id, boolean bool) {
		this.id = id;
		this.bool = bool;
	}
	
	
	@Override
	public void fromBytes(ByteBuf buf) {
		id = ByteBufUtils.readUTF8String(buf);
		bool = buf.readBoolean();
	}
	
	
	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, id);
		buf.writeBoolean(bool);
	}
	
	public static class IceBridgeHandler implements IMessageHandler<MessageBoolean, IMessage> {
		public IceBridgeHandler() {
			
		}
		
		
		@Override
		public IMessage onMessage(final MessageBoolean message, final MessageContext ctx) {
			((WorldServer)ctx.getServerHandler().player.world).addScheduledTask(new Runnable () {
			//Minecraft.getMinecraft().addScheduledTask(new Runnable() {
				@Override
				public void run() {
					EntityPlayer p = ctx.getServerHandler().player;
					if (p == null)
						return;
					AffinityData.For(p).addAbilityBoolean(message.id, message.bool);
				}
			});
			return null;
		}
	}

}

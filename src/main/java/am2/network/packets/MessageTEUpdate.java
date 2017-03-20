package am2.network.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by Growlith1223 on 3/4/2017.
 */
public class MessageTEUpdate implements IMessage {
    public NBTTagCompound tag = new NBTTagCompound();

    public MessageTEUpdate(){

    }

    public MessageTEUpdate(NBTTagCompound val){
        this.tag = val;
    }
    @Override
    public void fromBytes(ByteBuf byteBuf) {
        this.tag = ByteBufUtils.readTag(byteBuf);
    }

    @Override
    public void toBytes(ByteBuf byteBuf) {
        ByteBufUtils.writeTag(byteBuf, this.tag);
    }

    public static class MessageHolder implements IMessageHandler<MessageTEUpdate, IMessage>{

        @SideOnly(Side.CLIENT)
        @Override
        public IMessage onMessage(MessageTEUpdate messageTEUpdate, MessageContext messageContext) {
            Minecraft.getMinecraft().addScheduledTask(()-> {
                NBTTagList list = messageTEUpdate.tag.getTagList("data", Constants.NBT.TAG_COMPOUND);
                for (int i = 0; i < list.tagCount(); i++){
                    NBTTagCompound tag = list.getCompoundTagAt(i);
                    TileEntity te = Minecraft.getMinecraft().thePlayer.getEntityWorld().getTileEntity(new BlockPos(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z")));
                    if( te != null) {
                        te.readFromNBT(tag);
                        te.markDirty();
                    }
                }
            });
            return null;
        }
    }
}

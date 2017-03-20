package am2.network;

import am2.ArsMagica2;
import am2.network.packets.MessageTEUpdate;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {
    public static SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(ArsMagica2.MODID);

    private static int id = 0;

    public static void registerMessages(){
        INSTANCE.registerMessage(MessageTEUpdate.MessageHolder.class, MessageTEUpdate.class, id++, Side.CLIENT);
    }
}

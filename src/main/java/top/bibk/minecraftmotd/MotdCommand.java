package top.bibk.minecraftmotd;

import net.mamoe.mirai.console.command.MemberCommandSender;
import net.mamoe.mirai.console.command.SimpleCommand;
import net.mamoe.mirai.console.command.descriptor.CommandArgumentContext;
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin;

import java.io.IOException;
import java.net.*;

public class MotdCommand extends SimpleCommand {
    public MotdCommand(JvmPlugin plugin) {
        super(plugin, "motd", new String[]{"Motd"}, "Motd", plugin.getParentPermission(), CommandArgumentContext.EMPTY);
    }
    // From https://stackoverflow.com/questions/140131/convert-a-string-representation-of-a-hex-dump-to-a-byte-array-using-java
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    @Handler // 标记这是指令处理器，方法名随意
    public void handle(MemberCommandSender sender, String address, int port) throws IOException { // 后两个参数会被作为指令参数要求
        DatagramSocket datagramSocket = new DatagramSocket();
        datagramSocket.connect(InetAddress.getByName(address), port);
        byte[] data = hexStringToByteArray("01010000000000000000ffff00fefefefefdfdfdfd12345678a1b6ac63b81ca9d3");
        DatagramPacket packet = new DatagramPacket(data, data.length);
        datagramSocket.send(packet);
        byte[] buffer = new byte[1024];
        packet = new DatagramPacket(buffer, buffer.length);
        try {
            datagramSocket.setSoTimeout(1000);
            datagramSocket.receive(packet);
        } catch (Exception e) {
            sender.getGroup().sendMessage("服务器不在线");
            datagramSocket.close();
            return;
        }
        String result = new String(packet.getData(), packet.getOffset(), packet.getLength());
        datagramSocket.disconnect();
        String[] result1 = result.split(";");
        if (result1[0].contains("MCPE")) {
            MinecraftMotd minecraftMotd = new MinecraftMotd();
            minecraftMotd.motd = result1[1];
            minecraftMotd.protocol = result1[2];
            minecraftMotd.version_name = result1[3];
            minecraftMotd.player_count = result1[4];
            minecraftMotd.max_players = result1[5];
            minecraftMotd.unique_id = result1[6];
            if (result1.length > 7) {// Nukkit
                minecraftMotd.sub_motd = result1[7];
                minecraftMotd.game_mode = result1[8];
                minecraftMotd.game_mode_num = result1[9];
                if (result1.length > 10) {// Bedrock Dedicated Server or Geyser
                    minecraftMotd.port_v4 = result1[10];
                    minecraftMotd.port_v6 = result1[11];
                }
            }
            String message = "Motd: " + minecraftMotd.motd + "\n协议版本: "+ minecraftMotd.protocol + "\n版本: " + minecraftMotd.version_name + "\n在线: " + minecraftMotd.player_count + "/" + minecraftMotd.max_players + "\nUniqueID: " + minecraftMotd.unique_id;
            switch (result1.length) {
                case 7:
                    minecraftMotd.type = "PocketMine";
                    break;
                case 10:
                    message = message.concat("\n存档名: " + minecraftMotd.sub_motd + "\n游戏模式: " + minecraftMotd.game_mode);
                    minecraftMotd.type = "Nukkit";
                    break;
                case 12:
                    message = message.concat("\n监听端口: " + minecraftMotd.port_v4 + "/" + minecraftMotd.port_v6);
                    if (minecraftMotd.port_v6.equals("-1")) {
                        minecraftMotd.type = "Geyser";
                    } else {
                        minecraftMotd.type = "Bedrock Dedicated Server";
                    }
            }
            sender.getGroup().sendMessage(message + "\n服务端: " + minecraftMotd.type);
        } else {
            sender.getGroup().sendMessage("不是Minecraft服务器");
        }
    }
}


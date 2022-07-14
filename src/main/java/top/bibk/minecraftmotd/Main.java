package top.bibk.minecraftmotd;

import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.permission.AbstractPermitteeId;
import net.mamoe.mirai.console.permission.PermissionService;
import net.mamoe.mirai.console.permission.Permittee;
import net.mamoe.mirai.console.permission.PermitteeId;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;

public final class Main extends JavaPlugin {
    public static final top.bibk.minecraftmotd.Main INSTANCE = new top.bibk.minecraftmotd.Main();
    private Main() {
        super(new JvmPluginDescriptionBuilder("top.bibk.MinecraftMotd", "0.1.2")
                .name("mirai-console-minecraft-motd")
                .info("Query Minecraft Bedrock Edition Motd by mirai-console")
                .author("ShrBox")
                .build());
    }

    @Override
    public void onEnable() {
        getLogger().info("Plugin loaded");
        CommandManager.INSTANCE.registerCommand(new MotdCommand(this), false);
        //PermissionService.permit(AbstractPermitteeId.AnyContact.INSTANCE, this.permissionId("command.motd"));
    }
}

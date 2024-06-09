package net.outfluencer.serverlinksplus;

import lombok.SneakyThrows;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.JsonConfiguration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Either;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.ServerLinks;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public final class ServerLinksPlus extends Plugin implements Listener {

    private ServerLinks serverLinks;
    private Method sendPacketQueuedMethod;

    @Override
    @SneakyThrows
    public void onLoad() {
        sendPacketQueuedMethod = Class.forName("net.md_5.bungee.UserConnection").getDeclaredMethod("sendPacketQueued", DefinedPacket.class);

        getDataFolder().mkdirs();
        File file = new File(getDataFolder(), "config.json");
        ConfigurationProvider provider = ConfigurationProvider.getProvider(JsonConfiguration.class);
        Configuration configuration = null;

        try {
            configuration = provider.load(file);
        } catch (IOException e) {
            Map<String, String> defaultLinks = new TreeMap<>();
            defaultLinks.put("§aWebsite", "https://example.com");
            defaultLinks.put("§5Discord", "https://discord.gg/invite/example");
            configuration = new Configuration();
            configuration.set("server-links", defaultLinks);
            provider.save(configuration, file);
        }

        Configuration config = ((Configuration) configuration.get("server-links"));
        ServerLinks.Link[] links = config.getKeys().stream().map(key -> new ServerLinks.Link(Either.right(TextComponent.fromLegacy(key)), config.getString(key))).toArray(ServerLinks.Link[]::new);
        serverLinks = new ServerLinks(links);
    }

    @Override
    public void onEnable() {
        getProxy().getPluginManager().registerListener(this, this);
        getProxy().getPluginManager().registerCommand(this, new ServerLinksPlusCommand());
    }

    @EventHandler
    public void postLoginEvent(PostLoginEvent event) throws Exception {
        if (event.getPlayer().getPendingConnection().getVersion() >= ProtocolConstants.MINECRAFT_1_21) {
            sendPacketQueuedMethod.invoke(event.getPlayer(), serverLinks);
        }
    }

    public class ServerLinksPlusCommand extends Command implements TabExecutor {

        public ServerLinksPlusCommand() {
            super("serverlinksplus", "serverlinksplus.command", "slp");
        }

        @Override
        public void execute(CommandSender sender, String[] args) {
            if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
                sender.sendMessage(TextComponent.fromLegacy("§cUsage: /serverlinksplus reload"));
                return;
            }
            onLoad();
            sender.sendMessage(ChatColor.GREEN + "The configuration has been reloaded.");
        }

        @Override
        public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
            if (args.length == 0) {
                return Arrays.asList("reload");
            }
            return Collections.emptyList();
        }
    }

}

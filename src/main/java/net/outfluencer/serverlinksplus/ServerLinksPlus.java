package net.outfluencer.serverlinksplus;

import lombok.Getter;
import lombok.SneakyThrows;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.JsonConfiguration;
import net.md_5.bungee.protocol.Either;
import net.md_5.bungee.protocol.packet.ServerLinks;
import net.outfluencer.serverlinksplus.command.ServerLinksPlusCommand;
import net.outfluencer.serverlinksplus.listeners.LinkListener;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

@Getter
public final class ServerLinksPlus extends Plugin implements Listener {

    private ServerLinks serverLinks;

    @Override
    @SneakyThrows
    public void onLoad() {
        getDataFolder().mkdirs();
        File file = new File(getDataFolder(), "config.json");
        ConfigurationProvider provider = ConfigurationProvider.getProvider(JsonConfiguration.class);
        Configuration configuration = null;
        try {
            configuration = provider.load(file);
        } catch (IOException e) {
            Map<String, String> defaultLinks = new TreeMap<>();
            // Built-in
            // report bug will also shown in error screens
            // ServerLinks.LinkType.REPORT_BUG
            for (ServerLinks.LinkType value : ServerLinks.LinkType.values()) {
                defaultLinks.put(value.toString(), "https://" + value.name().toLowerCase() + ".com");
            }
            // custom
            defaultLinks.put("§aWebsite", "https://example.com");
            defaultLinks.put("§5Discord", "https://discord.gg/invite/example");

            configuration = new Configuration();
            configuration.set("server-links", defaultLinks);
            provider.save(configuration, file);
        }


        Configuration config = ((Configuration) configuration.get("server-links"));
        ServerLinks.Link[] links = config.getKeys().stream().map(key -> {

            ServerLinks.LinkType buildIn = null;
            try {
                buildIn = ServerLinks.LinkType.valueOf(key);
            } catch (IllegalArgumentException ignored) {
            }
            if (buildIn == null) {
                return new ServerLinks.Link(Either.right(TextComponent.fromLegacy(key)), config.getString(key));
            } else {
                return new ServerLinks.Link(Either.left(buildIn), config.getString(key));
            }
        }).toArray(ServerLinks.Link[]::new);
        serverLinks = new ServerLinks(links);

        int custom = Stream.of(links).filter(link -> link.getType().isRight()).mapToInt(link -> 1).sum();
        int buildIn = links.length - custom;
        getLogger().info("Loaded " + buildIn + " built-in links and " + custom + " custom links.");
    }

    @Override
    public void onEnable() {
        getProxy().getPluginManager().registerListener(this, new LinkListener(this));
        getProxy().getPluginManager().registerCommand(this, new ServerLinksPlusCommand(this));
    }

}

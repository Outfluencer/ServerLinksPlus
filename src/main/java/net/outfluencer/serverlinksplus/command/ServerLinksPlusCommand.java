package net.outfluencer.serverlinksplus.command;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.outfluencer.serverlinksplus.ServerLinksPlus;

import java.util.Collections;

public class ServerLinksPlusCommand extends Command implements TabExecutor {

    private final ServerLinksPlus plugin;

    public ServerLinksPlusCommand(ServerLinksPlus plugin) {
        super("serverlinksplus", "serverlinksplus.command", "slp");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage("§cUsage: /serverlinksplus reload");
            return;
        }
        plugin.onLoad();
        sender.sendMessage(ChatColor.GREEN + "The configuration has been reloaded.");
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        return args.length == 1 ? Collections.singletonList("reload") : Collections.emptyList();
    }
}
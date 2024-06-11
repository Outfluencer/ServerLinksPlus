package net.outfluencer.serverlinksplus.listeners;

import lombok.Data;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.outfluencer.serverlinksplus.ServerLinksPlus;

@Data
public class LinkListener implements Listener {

    private final ServerLinksPlus plugin;

    @EventHandler
    public void onServerSwitchedFirstTime(ServerSwitchEvent event) {
        PendingConnection con = event.getPlayer().getPendingConnection();
        if (event.getFrom() == null && con.getVersion() >= ProtocolConstants.MINECRAFT_1_21) {
            con.unsafe().sendPacket(plugin.getServerLinks());
        }
    }

}

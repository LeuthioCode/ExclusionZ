package fr.leuthio.exclusionz.listener;

import fr.leuthio.exclusionz.ExclusionZPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

public final class MotdListener implements Listener {

    private final ExclusionZPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public MotdListener(ExclusionZPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onServerPing(ServerListPingEvent event) {
        boolean maintenance = plugin.getMaintenanceManager().isEnabled();

        String line1Key = maintenance ? "maintenance.motd.line1" : "motd.line1";
        String line2Key = maintenance ? "maintenance.motd.line2" : "motd.line2";

        String rawLine1 = plugin.getConfig().getString(line1Key, "");
        String rawLine2 = plugin.getConfig().getString(line2Key, "");

        Component motd = miniMessage.deserialize(rawLine1)
                .append(Component.newline())
                .append(miniMessage.deserialize(rawLine2));

        event.motd(motd);
    }
}

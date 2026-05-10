package fr.leuthio.exclusionz.listener;

import fr.leuthio.exclusionz.ExclusionZPlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public final class MaintenanceListener implements Listener {

    private final ExclusionZPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public MaintenanceListener(ExclusionZPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (!plugin.getMaintenanceManager().isEnabled()) return;

        // Les ops et ceux avec la permission bypass passent
        if (event.getPlayer().isOp()
                || event.getPlayer().hasPermission("exclusionz.maintenance.bypass")) {
            return;
        }

        String raw = plugin.getConfig().getString(
                "maintenance.kick-message",
                "<red><bold>Serveur en maintenance</bold></red>\n<yellow>Revenez plus tard !"
        );

        event.disallow(
                PlayerLoginEvent.Result.KICK_OTHER,
                miniMessage.deserialize(raw)
        );
    }
}

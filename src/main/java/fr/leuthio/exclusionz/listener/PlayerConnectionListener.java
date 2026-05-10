package fr.leuthio.exclusionz.listener;

import fr.leuthio.exclusionz.ExclusionZPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerConnectionListener implements Listener {

    private final ExclusionZPlugin plugin;

    public PlayerConnectionListener(ExclusionZPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.joinMessage(null);

        if (plugin.getConfig().getBoolean("update-checker.notify-on-join", true)
                && plugin.getUpdateChecker() != null
                && plugin.getUpdateChecker().isUpdateAvailable()
                && event.getPlayer().hasPermission("exclusionz.admin")) {

            event.getPlayer().sendMessage(
                    plugin.prefix()
                            .append(Component.text("Mise a jour disponible : v"
                                    + plugin.getUpdateChecker().getLatestVersion()
                                    + " — Redemarrez le serveur pour l'appliquer.", NamedTextColor.YELLOW))
            );
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.quitMessage(null);
    }
}

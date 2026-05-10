package fr.leuthio.exclusionz.maintenance;

import fr.leuthio.exclusionz.ExclusionZPlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class MaintenanceManager {

    private final ExclusionZPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    private boolean enabled;

    public MaintenanceManager(ExclusionZPlugin plugin) {
        this.plugin = plugin;
        this.enabled = plugin.getConfig().getBoolean("maintenance.enabled", false);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;

        // Persiste l'état dans config.yml
        plugin.getConfig().set("maintenance.enabled", enabled);
        plugin.saveConfig();

        if (enabled) {
            kickNonAuthorizedPlayers();
        }
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    /**
     * Kick tous les joueurs qui n'ont pas la permission de bypass ou qui ne sont pas OP.
     */
    private void kickNonAuthorizedPlayers() {
        String rawMessage = plugin.getConfig().getString(
                "maintenance.kick-broadcast",
                "<red>Le serveur passe en maintenance. Vous avez été déconnecté."
        );

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!canBypass(player)) {
                player.kick(miniMessage.deserialize(rawMessage));
            }
        }
    }

    /**
     * Retourne true si le joueur peut se connecter malgré la maintenance.
     */
    public boolean canBypass(Player player) {
        return player.isOp() || player.hasPermission("exclusionz.maintenance.bypass");
    }
}

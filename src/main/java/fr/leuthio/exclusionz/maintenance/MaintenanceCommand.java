package fr.leuthio.exclusionz.maintenance;

import fr.leuthio.exclusionz.ExclusionZPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class MaintenanceCommand implements CommandExecutor, TabCompleter {

    private final ExclusionZPlugin plugin;

    public MaintenanceCommand(ExclusionZPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {

        MaintenanceManager manager = plugin.getMaintenanceManager();

        // /maintenance sans argument → toggle
        if (args.length == 0) {
            manager.toggle();
            sendFeedback(sender, manager.isEnabled());
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "on" -> {
                if (manager.isEnabled()) {
                    sender.sendMessage(plugin.prefix()
                            .append(Component.text("La maintenance est déjà active.", NamedTextColor.RED)));
                    return true;
                }
                manager.setEnabled(true);
                sendFeedback(sender, true);
            }
            case "off" -> {
                if (!manager.isEnabled()) {
                    sender.sendMessage(plugin.prefix()
                            .append(Component.text("La maintenance est déjà désactivée.", NamedTextColor.GREEN)));
                    return true;
                }
                manager.setEnabled(false);
                sendFeedback(sender, false);
            }
            case "status" -> {
                boolean active = manager.isEnabled();
                sender.sendMessage(
                        plugin.prefix()
                                .append(Component.text("Maintenance : ", NamedTextColor.WHITE))
                                .append(active
                                        ? Component.text("ACTIVE", NamedTextColor.RED)
                                        : Component.text("DÉSACTIVÉE", NamedTextColor.GREEN))
                );
            }
            default -> {
                sender.sendMessage(plugin.prefix()
                        .append(Component.text("Usage : /maintenance [on|off|status]", NamedTextColor.RED)));
            }
        }

        return true;
    }

    private void sendFeedback(CommandSender sender, boolean enabled) {
        if (enabled) {
            sender.sendMessage(plugin.prefix()
                    .append(Component.text("Maintenance activée. Les joueurs non autorisés ont été expulsés.", NamedTextColor.RED)));
        } else {
            sender.sendMessage(plugin.prefix()
                    .append(Component.text("Maintenance désactivée. Le serveur est de nouveau accessible.", NamedTextColor.GREEN)));
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender,
                                      @NotNull Command command,
                                      @NotNull String label,
                                      @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("on", "off", "status").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}

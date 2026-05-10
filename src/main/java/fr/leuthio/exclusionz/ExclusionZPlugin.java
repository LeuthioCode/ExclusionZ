package fr.leuthio.exclusionz;

import fr.leuthio.exclusionz.listener.MaintenanceListener;
import fr.leuthio.exclusionz.listener.MotdListener;
import fr.leuthio.exclusionz.listener.PlayerConnectionListener;
import fr.leuthio.exclusionz.maintenance.MaintenanceCommand;
import fr.leuthio.exclusionz.maintenance.MaintenanceManager;
import fr.leuthio.exclusionz.update.UpdateChecker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;

public final class ExclusionZPlugin extends JavaPlugin {

    private static ExclusionZPlugin instance;
    private UpdateChecker updateChecker;
    private MaintenanceManager maintenanceManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        maintenanceManager = new MaintenanceManager(this);

        updateChecker = new UpdateChecker(this, getFile());
        if (getConfig().getBoolean("update-checker.enabled", true)) {
            updateChecker.check();
        }

        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
        getServer().getPluginManager().registerEvents(new MotdListener(this), this);
        getServer().getPluginManager().registerEvents(new MaintenanceListener(this), this);

        MaintenanceCommand maintenanceCommand = new MaintenanceCommand(this);
        getCommand("maintenance").setExecutor(maintenanceCommand);
        getCommand("maintenance").setTabCompleter(maintenanceCommand);

        getLogger().info("ExclusionZ active.");
    }

    @Override
    public void onDisable() {
        getLogger().info("ExclusionZ desactive.");
    }

    public static ExclusionZPlugin getInstance() {
        return instance;
    }

    public UpdateChecker getUpdateChecker() {
        return updateChecker;
    }

    public MaintenanceManager getMaintenanceManager() {
        return maintenanceManager;
    }

    /**
     * Retourne le prefix du plugin désérialisé depuis config.yml.
     */
    public Component prefix() {
        String raw = getConfig().getString("prefix", "<gold>[ExclusionZ]</gold> ");
        return MiniMessage.miniMessage().deserialize(raw);
    }
}

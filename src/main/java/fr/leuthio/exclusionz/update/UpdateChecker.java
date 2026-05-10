package fr.leuthio.exclusionz.update;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.leuthio.exclusionz.ExclusionZPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;

public final class UpdateChecker {

    private final ExclusionZPlugin plugin;
    private final String githubRepo;
    private final boolean autoDownload;
    private final File currentJarFile;

    private volatile boolean updateAvailable = false;
    private volatile String latestVersion = null;

    public UpdateChecker(ExclusionZPlugin plugin, File currentJarFile) {
        this.plugin = plugin;
        this.currentJarFile = currentJarFile;
        this.githubRepo = plugin.getConfig().getString("update-checker.github-repo", "");
        this.autoDownload = plugin.getConfig().getBoolean("update-checker.auto-download", true);
    }

    public void check() {
        if (githubRepo == null || githubRepo.isBlank()) {
            plugin.getLogger().warning("[UpdateChecker] Aucun depot GitHub configure dans config.yml (update-checker.github-repo).");
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(10))
                        .build();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.github.com/repos/" + githubRepo + "/releases/latest"))
                        .header("Accept", "application/vnd.github+json")
                        .header("User-Agent", "ExclusionZ-UpdateChecker")
                        .timeout(Duration.ofSeconds(10))
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    plugin.getLogger().warning("[UpdateChecker] Impossible de contacter l'API GitHub (HTTP " + response.statusCode() + ").");
                    return;
                }

                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                String remoteTag = json.get("tag_name").getAsString().replaceFirst("^v", "");

                this.latestVersion = remoteTag;

                String currentVersion = plugin.getDescription().getVersion().replace("-SNAPSHOT", "");

                if (isNewerVersion(remoteTag, currentVersion)) {
                    this.updateAvailable = true;
                    plugin.getLogger().info("[UpdateChecker] Mise a jour disponible : v" + remoteTag + " (actuelle : v" + currentVersion + ")");

                    if (autoDownload) {
                        JsonArray assets = json.getAsJsonArray("assets");
                        boolean found = false;
                        for (int i = 0; i < assets.size(); i++) {
                            JsonObject asset = assets.get(i).getAsJsonObject();
                            if (asset.get("name").getAsString().endsWith(".jar")) {
                                String downloadUrl = asset.get("browser_download_url").getAsString();
                                downloadUpdate(downloadUrl, remoteTag);
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            plugin.getLogger().warning("[UpdateChecker] Aucun fichier .jar trouve dans les assets de la release v" + remoteTag + ".");
                        }
                    } else {
                        plugin.getLogger().info("[UpdateChecker] Telechargement automatique desactive. Mettez a jour manuellement.");
                    }
                } else {
                    plugin.getLogger().info("[UpdateChecker] Le plugin est a jour (v" + currentVersion + ").");
                }

            } catch (Exception e) {
                plugin.getLogger().warning("[UpdateChecker] Erreur lors de la verification des mises a jour : " + e.getMessage());
            }
        });
    }

    private void downloadUpdate(String downloadUrl, String remoteVersion) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(30))
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(downloadUrl))
                    .header("User-Agent", "ExclusionZ-UpdateChecker")
                    .timeout(Duration.ofSeconds(60))
                    .GET()
                    .build();

            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() != 200) {
                plugin.getLogger().warning("[UpdateChecker] Echec du telechargement (HTTP " + response.statusCode() + ").");
                return;
            }

            // Dossier plugins/update/ — Paper remplace automatiquement au redemarrage
            Path updateFolder = currentJarFile.getParentFile().toPath().resolve("update");
            Files.createDirectories(updateFolder);
            Path destination = updateFolder.resolve(currentJarFile.getName());

            Files.copy(response.body(), destination, StandardCopyOption.REPLACE_EXISTING);

            plugin.getLogger().info("[UpdateChecker] v" + remoteVersion + " telecharge dans plugins/update/ — Redemarrez le serveur pour appliquer la mise a jour.");

            // Notifier les admins en ligne sur le thread principal
            Bukkit.getScheduler().runTask(plugin, () ->
                    Bukkit.getOnlinePlayers().stream()
                            .filter(p -> p.hasPermission("exclusionz.admin"))
                            .forEach(p -> p.sendMessage(
                                    plugin.prefix()
                                            .append(Component.text("Mise a jour v" + remoteVersion + " telecharge. Redemarrez le serveur !", NamedTextColor.YELLOW))
                            ))
            );

        } catch (Exception e) {
            plugin.getLogger().warning("[UpdateChecker] Erreur lors du telechargement : " + e.getMessage());
        }
    }

    private boolean isNewerVersion(String remote, String current) {
        int[] r = parseVersion(remote);
        int[] c = parseVersion(current);
        for (int i = 0; i < Math.min(r.length, c.length); i++) {
            if (r[i] > c[i]) return true;
            if (r[i] < c[i]) return false;
        }
        return r.length > c.length;
    }

    private int[] parseVersion(String version) {
        String[] parts = version.split("\\.");
        int[] nums = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                nums[i] = Integer.parseInt(parts[i]);
            } catch (NumberFormatException e) {
                nums[i] = 0;
            }
        }
        return nums;
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public String getLatestVersion() {
        return latestVersion;
    }
}

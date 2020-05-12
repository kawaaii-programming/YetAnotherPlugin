package moe.kawaaii.yap;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

import java.io.File;
import java.io.IOException;

import moe.kawaaii.yap.listeners.OnAdvancement;
import moe.kawaaii.yap.listeners.OnPlayerConnection;

public class yap extends JavaPlugin implements Listener {
  public static FileConfiguration config;
  public static FileConfiguration advancements;
  public static DiscordApi discord = null;
  public static Metrics metrics;

  @Override
  public void onLoad() {
    getLogger().info("Loading up config files...");
    File configFile = new File(this.getDataFolder(), "config.yml");
    File advancementsFile = new File(this.getDataFolder(), "advancements.yml");

    if (!configFile.exists()) {
      configFile.getParentFile().mkdirs();
      this.saveResource("config.yml", false);
    }

    if (!advancementsFile.exists()) {
      advancementsFile.getParentFile().mkdirs();
      this.saveResource("advancements.yml", false);
    }

    config = new YamlConfiguration();
    try {
      config.load(configFile);
    } catch (IOException | InvalidConfigurationException e) {
      e.printStackTrace();
    }

    advancements = new YamlConfiguration();
    try {
      advancements.load(advancementsFile);
    } catch (IOException | InvalidConfigurationException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onEnable() {
    PluginManager pluginManager = getServer().getPluginManager();

    if (config.getString("config.discord.token", "").isEmpty()) {
      getLogger().warning("No Discord Token provided, disabling now!");
      pluginManager.disablePlugin(this);
      return;
    }

    getLogger().info("Connecting to Discord API...");
    try {
      discord = new DiscordApiBuilder().setToken(config.getString("config.discord.token")).login().join();
    } catch (Exception e) {
      e.printStackTrace();
      pluginManager.disablePlugin(this);
    }

    getLogger().info("Hooking bStats...");
    metrics = new Metrics(this);

    getLogger().info("Hooking EssentialsX...");
    if (Bukkit.getPluginManager().getPlugin("Essentials") != null) {
      Bukkit.getPluginManager().registerEvents(this, this);
    } else {
      getLogger().warning("EssentialsX was not found, disabling now!");
      getServer().getPluginManager().disablePlugin(this);
    }

    getLogger().info("Registering Enabled Modules...");
    if (config.getBoolean("config.modules.advancements.enabled", false)) {
      pluginManager.registerEvents(new OnAdvancement(), this);
    }

    if (config.getBoolean("config.modules.connections.enabled", false)) {
      pluginManager.registerEvents(new OnPlayerConnection(), this);
    }
  }

  @Override
  public void onDisable() {
    try {
      discord.disconnect();
    } catch (Exception e) {
      //
    }
  }
}

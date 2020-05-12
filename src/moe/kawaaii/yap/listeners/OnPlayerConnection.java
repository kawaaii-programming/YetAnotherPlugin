package moe.kawaaii.yap.listeners;

import org.apache.commons.lang.text.StrSubstitutor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.entity.Player;
import me.clip.placeholderapi.PlaceholderAPI;
import org.javacord.api.entity.channel.TextChannel;

import java.util.*;

import static moe.kawaaii.yap.yap.config;
import static moe.kawaaii.yap.yap.discord;
import static org.bukkit.Bukkit.getLogger;

public class OnPlayerConnection implements Listener {
  private final List<TextChannel> connectChannels = new ArrayList<>();
  private final List<TextChannel> disconnectChannels = new ArrayList<>();

  public OnPlayerConnection() {
    if (config.getBoolean("config.modules.connections.connect.enabled", false)) {
      config.getStringList("config.modules.connections.connect.channels").forEach(channel -> {
        if (channel.isEmpty()) return;
        Optional<TextChannel> fetchedChannel = discord.getTextChannelById(channel);
        if (fetchedChannel.isPresent()) connectChannels.add(fetchedChannel.get());
      });
    }

    if (config.getBoolean("config.modules.connections.disconnect.enabled", false)) {
      config.getStringList("config.modules.connections.disconnect.channels").forEach(channel -> {
        if (channel.isEmpty()) return;
        Optional<TextChannel> fetchedChannel = discord.getTextChannelById(channel);
        if (fetchedChannel.isPresent()) disconnectChannels.add(fetchedChannel.get());
      });
    }
  }

  private String parsePlaceholders(String text, Player player) {
    String parsed = PlaceholderAPI.setBracketPlaceholders(player, text);

    Map<String, String> v = new HashMap<>();
    v.put("username", player.getName());
    v.put("user_id", player.getUniqueId().toString());

    StrSubstitutor sub = new StrSubstitutor(v, "{", "}");
    return sub.replace(parsed);
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent e) {
    getLogger().info("Connected");
    if (!config.getBoolean("config.modules.connections.connect.enabled", false)) return;

    String rawMessage = config.getString("config.modules.connections.connect.format", "\\uD83D\uDFE2 | **{username}** entered the server, have fun!");
    String message = parsePlaceholders(rawMessage, e.getPlayer());

    connectChannels.forEach(channel -> {
      channel.sendMessage(message);
    });
  }

  @EventHandler
  public void onPlayerLeave(PlayerQuitEvent e) {
    getLogger().info("Disconnected");
    if (!config.getBoolean("config.modules.connections.disconnect.enabled", false)) return;

    String rawMessage = config.getString("config.modules.connections.disconnect.format", "\\uD83D\uDD34 | **{username}** left the server, come again!");
    String message = parsePlaceholders(rawMessage, e.getPlayer());

    disconnectChannels.forEach(channel -> {
      channel.sendMessage(message);
    });
  }
}

package moe.kawaaii.yap.listeners;

import org.apache.commons.lang.text.StrSubstitutor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.javacord.api.entity.channel.TextChannel;

import java.util.*;

import static moe.kawaaii.yap.yap.config;
import static moe.kawaaii.yap.yap.advancements;
import static moe.kawaaii.yap.yap.discord;
import static org.bukkit.Bukkit.getLogger;

public class OnAdvancement implements Listener {
  private final List<TextChannel> channels = new ArrayList<>();

  public OnAdvancement() {
    config.getStringList("config.modules.advancements.channels").forEach(channel -> {
      Optional<TextChannel> fetchedChannel = discord.getTextChannelById(channel);
      if (fetchedChannel.isPresent()) channels.add(fetchedChannel.get());
    });
  }

  @EventHandler
  public void onAdvancement(PlayerAdvancementDoneEvent e) {
    String advRaw = e.getAdvancement().getKey().getKey();
    if (e.getAdvancement() == null || advRaw.contains("recipe/") || e.getPlayer() == null) return;

    if (!advancements.contains(advRaw)) return;
    String advName = advancements.getString(advRaw + ".name", null);
    String advText = advancements.getString(advRaw + ".message", null);
    if (advName == null || advText == null) return;

    Map<String, String> values = new HashMap<>();
    values.put("username", e.getPlayer().getName());
    values.put("advancement", advName);

    StrSubstitutor sub = new StrSubstitutor(values, "{", "}");
    String parsedMsg = sub.replace(advText);

    getLogger().info(parsedMsg);

    channels.forEach(channel -> {
      channel.sendMessage(parsedMsg);
    });

  }

}

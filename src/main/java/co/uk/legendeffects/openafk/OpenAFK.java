package co.uk.legendeffects.openafk;

import co.uk.legendeffects.openafk.commands.AFKComand;
import co.uk.legendeffects.openafk.commands.AFKPlayersCommand;
import co.uk.legendeffects.openafk.commands.IsAFKCommand;
import co.uk.legendeffects.openafk.commands.OpenAFKCommand;
import co.uk.legendeffects.openafk.detection.FishingDetection;
import co.uk.legendeffects.openafk.events.PlayerAfkEvent;
import co.uk.legendeffects.openafk.events.PlayerReturnEvent;
import co.uk.legendeffects.openafk.handlers.PlayerConnect;
import co.uk.legendeffects.openafk.handlers.PlayerDisconnect;
import co.uk.legendeffects.openafk.script.*;
import co.uk.legendeffects.openafk.script.actions.*;
import co.uk.legendeffects.openafk.util.CheckTask;
import co.uk.legendeffects.openafk.util.ConfigWrapper;
import co.uk.legendeffects.openafk.util.DataHandler;
import co.uk.legendeffects.openafk.util.PAPIHook;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class OpenAFK extends JavaPlugin {

    private static OpenAFK instance;

    private ConfigWrapper config;
    private ConfigWrapper data;
    private DataHandler playerData;

    private ActionParser actionParser;

    private final Set<Player> afkPlayers = new HashSet<>();
    private final Set<Player> exemptPlayers = new HashSet<>();
    private final HashMap<Player, Location> lastLocations = new HashMap<>();
    private final HashMap<Player, Integer> checkAmounts = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;

        this.config = new ConfigWrapper(this, "config.yml");
        this.data = new ConfigWrapper(this, "data.yml");
        this.playerData = new DataHandler(this);

        this.actionParser = new ActionParser();
        actionParser.registerAction(new AfkAreaAction(this));
        actionParser.registerAction(new ActionBarAction(this));
        actionParser.registerAction(new BroadcastAction());
        actionParser.registerAction(new MessageAction());
        actionParser.registerAction(new TitleAction());

        actionParser.registerScript("onAfk", getConfig().getStringList("scripts.onAfk"));
        actionParser.registerScript("onReturn", getConfig().getStringList("scripts.onReturn"));
        actionParser.registerScript("onAfkCMD", getConfig().getStringList("scripts.onAfkCMD"));
        actionParser.registerScript("onReturnCMD", getConfig().getStringList("scripts.onReturnCMD"));

        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(new PlayerConnect(this), this);
        manager.registerEvents(new PlayerDisconnect(this), this);

        if(config.getRaw().getBoolean("detection.fishing.enabled")) {
            manager.registerEvents(new FishingDetection(this), this);
        }

        if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PAPIHook(this).register();
        }

        getCommand("openafk").setExecutor(new OpenAFKCommand(this));
        getCommand("isafk").setExecutor(new IsAFKCommand(this));
        getCommand("afkplayers").setExecutor(new AFKPlayersCommand(this));
        if(this.getConfig().getBoolean("enableAfkCommand")) {
            getCommand("afk").setExecutor(new AFKComand(this));
        }

        getServer().getOnlinePlayers().forEach(player -> {
            if(playerData.playerHasData(player)) {
                playerData.getPlayer(player);
                afkPlayers.add(player);
            }
        });

        new CheckTask(this).runTaskTimer(this, 0L, this.getConfig().getLong("checkInterval", 20L));
    }

    public void makePlayerAfk(Player player, ActionType type, String script) {
        if(!afkPlayers.contains(player)) {
            PlayerAfkEvent event = new PlayerAfkEvent(player);
            Bukkit.getPluginManager().callEvent(event);

            if(!event.isCancelled()) {
                afkPlayers.add(player);
                actionParser.parse(player, type, script);
            }
        }
    }

    public void makePlayerReturn(Player player, ActionType type, String script) {
        if(afkPlayers.contains(player)) {
            PlayerReturnEvent event = new PlayerReturnEvent(player);
            Bukkit.getPluginManager().callEvent(event);

            if(!event.isCancelled()) {
                afkPlayers.remove(player);
                checkAmounts.remove(player);

                actionParser.parse(player, type, script);
            }
        }
    }

    public static String parse(final Player player, final String s) {
        // This is an easter egg for whenever "perotin" is online. He insulted me okay Kappa.
        if(Bukkit.getPlayer(UUID.fromString("9d311c0a-e4cd-4bc6-aec5-a79f3381d19e")) != null) {
            getInstance().config.getRaw().set("messages.prefix", "&4[&cFrickOffPerotin&4] &7");
        }

        if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return PlaceholderAPI.setPlaceholders(player, s);
        }
        FileConfiguration config = getInstance().getConfig();

        return ChatColor.translateAlternateColorCodes('&', s.replaceAll("%openafk_prefix%", config.getString("messages.prefix")).replaceAll("%player_name%", player.getName()));
    }
    public static String parse(final String s) {
        FileConfiguration config = getInstance().getConfig();
        return ChatColor.translateAlternateColorCodes('&', s.replaceAll("%openafk_prefix%", config.getString("messages.prefix")));
    }

    public static OpenAFK getInstance() { return instance; }

    public FileConfiguration getConfig() { return config.getRaw(); }
    public ConfigWrapper getData() { return data; }
    public DataHandler getPlayerData() { return playerData; }
    public ActionParser getActionParser() { return actionParser; }

    public Integer getCheckAmount(Player player) { return checkAmounts.get(player); }
    public void setCheckAmount(Player player, int value) { checkAmounts.put(player, value); }

    public boolean isAfkPlayer(Player player) { return afkPlayers.contains(player); }
    public Set<Player> getAfkPlayers() { return new HashSet<>(afkPlayers); }
    public void removeAfkPlayerFromCache(Player player) { afkPlayers.remove(player); }

    public void addExemptPlayer(Player player) { exemptPlayers.add(player); }
    public void removeExemptPlayer(Player player) { exemptPlayers.remove(player); }
    public boolean isExempt(Player player) { return exemptPlayers.contains(player); }

    public void setLastLocation(Player player, Location newLocation) { this.lastLocations.put(player, newLocation); }
    public Location getLastLocation(Player player) { return lastLocations.get(player); }

}

package co.uk.legendeffects.openafk;

import co.uk.legendeffects.openafk.commands.OpenAFKCommand;
import co.uk.legendeffects.openafk.handlers.AfkEvent;
import co.uk.legendeffects.openafk.handlers.PlayerConnect;
import co.uk.legendeffects.openafk.handlers.PlayerDisconnect;
import co.uk.legendeffects.openafk.handlers.ReturnEvent;
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class OpenAFK extends JavaPlugin {

    private static OpenAFK instance;

    private ConfigWrapper config;
    private ConfigWrapper data;
    private DataHandler playerData;

    public Set<Player> afkPlayers = new HashSet<>();
    public HashMap<Player, Location> lastLocations = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;

        this.config = new ConfigWrapper(this, "config.yml");
        this.data = new ConfigWrapper(this, "data.yml");
        this.playerData = new DataHandler(this);

        PlaceholderAPI.registerExpansion(new PAPIHook(this));

        PluginManager manager = getServer().getPluginManager();

        manager.registerEvents(new AfkEvent(this), this);
        manager.registerEvents(new ReturnEvent(this), this);
        manager.registerEvents(new PlayerConnect(this), this);
        manager.registerEvents(new PlayerDisconnect(this), this);

        getCommand("openafk").setExecutor(new OpenAFKCommand(this));

        new CheckTask(this).runTaskTimer(this, 0L, this.getConfig().getLong("checkInterval"));
    }

    public static String parse(final Player player, final String s) {
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            return PlaceholderAPI.setPlaceholders(player, s);
        }
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    static OpenAFK getPlugin() {
        return instance;
    }


    public FileConfiguration getConfig() {
        return config.getRaw();
    }
    public ConfigWrapper getData() { return data; }

    public DataHandler getPlayerData() {
        return playerData;
    }

}

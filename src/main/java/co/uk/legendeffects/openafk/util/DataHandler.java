package co.uk.legendeffects.openafk.util;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class DataHandler {
    private JavaPlugin plugin;
    private HashMap<Player, FileConfiguration> attributedData = new HashMap<>();

    public DataHandler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private FileConfiguration loadPlayerData(Player player) {
        File file = new File(plugin.getDataFolder(), "players" + File.separator + player.getUniqueId().toString() + ".yml");

        if(!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        FileConfiguration data = YamlConfiguration.loadConfiguration(file);
        attributedData.put(player, data);

        return data;
    }

    public void savePlayer(Player player) {
        try {
            attributedData.get(player).save(new File(plugin.getDataFolder(), "players"+File.separator+player.getUniqueId().toString()+".yml"));
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getPlayer(Player player) {
        if(attributedData.containsKey(player)) {
            return attributedData.get(player);
        }
        return this.loadPlayerData(player);
    }

    public boolean deletePlayer(Player player) {
        return new File(plugin.getDataFolder(), "players"+File.separator+player.getUniqueId().toString()+".yml").delete();
    }
}

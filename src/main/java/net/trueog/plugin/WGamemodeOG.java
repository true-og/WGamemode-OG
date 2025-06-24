/*
   WGamemode 3, an automatic gamemode switching plugin for Spigot 1.19
   Updated for https://true-og.net by NotAlexNoyle
   Copyright (C) 2015 Nicholas Narsing <soren121@sorenstudios.com>

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published
   by the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package net.trueog.plugin;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class WGamemodeOG extends JavaPlugin {

    // Declare plugin instance.
    private static WGamemodeOG plugin;

    public Map<Player, GameMode> playersChanged;

    public WorldGuardPlugin getWorldGuard() {

        Plugin worldGuard = getServer().getPluginManager().getPlugin("WorldGuard");
        if (worldGuard instanceof WorldGuardPlugin) {

            return (WorldGuardPlugin) worldGuard;

        } else {

            return null;
        }
    }

    private void validateGamemodes() {

        Map<String, Object> regions =
                getConfig().getConfigurationSection("regions").getValues(false);
        for (Map.Entry<String, Object> entry : regions.entrySet()) {

            // Purposely throw fatal exception if gamemode does not exist.
            try {

                GameMode.valueOf(entry.getValue().toString().toUpperCase());

            } catch (IllegalArgumentException | NullPointerException error) {

                throw new IllegalArgumentException(
                        "Invalid gamemode specified in config.yml for region '" + entry.getKey() + "'");
            }
        }
    }

    public void onEnable() {

        // Set plugin instance.
        plugin = this;

        saveDefaultConfig();
        validateGamemodes();

        this.playersChanged = new HashMap<Player, GameMode>((int) (getServer().getMaxPlayers() / 0.75f + 1));

        // Initialize event listeners.
        getServer().getPluginManager().registerEvents(new GamemodeListener(this), this);
        getCommand("wgadd").setExecutor(new AddRegion(this));
        getCommand("wgremove").setExecutor(new RemoveRegion(this));

        getLogger().info("Loaded successfully!");
    }

    public void onDisable() {

        // Return all players to their original game modes to avoid potential issues.
        for (Map.Entry<Player, GameMode> entry : this.playersChanged.entrySet()) {

            Player player = entry.getKey();
            if (player.getGameMode() != entry.getValue()) {

                player.setGameMode(entry.getValue());
            }
        }

        getLogger().info("Player gamemodes returned to original values");
    }

    public String currentRegion(Player player) {

        // Fetch the player's current regions.
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        Location location = BukkitAdapter.adapt(player.getLocation());
        ApplicableRegionSet set = query.getApplicableRegions(location);
        List<String> playerRegions = new ArrayList<>();
        for (ProtectedRegion region : set.getRegions()) {

            playerRegions.add(region.getId());
        }

        Set<String> managedRegions =
                getConfig().getConfigurationSection("regions").getKeys(false);

        // Diff the two region lists.
        playerRegions.retainAll(managedRegions);

        // Use first result.
        if (playerRegions.size() > 0) {

            return playerRegions.get(0);

        } else {

            // If the player is not in any WGamemode region, return null.
            return null;
        }
    }

    // Constructor so that the main class (this) can be referenced from other classes.
    public static WGamemodeOG getPlugin() {

        // Pass instance of main.
        return plugin;
    }
}

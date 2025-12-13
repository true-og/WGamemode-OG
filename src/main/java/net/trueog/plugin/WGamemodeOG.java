/*
   WGamemode 3, an automatic gamemode switching plugin for Spigot
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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;

import net.trueog.utilitiesog.UtilitiesOG;

public class WGamemodeOG extends JavaPlugin {

    // Declare plugin instance.
    private static WGamemodeOG plugin;

    public Map<UUID, PlayerState> playersChanged;

    private Map<String, GameMode> regionRules;

    public record PlayerState(GameMode originalGamemode, String regionId, GameMode appliedGamemode) {
    }

    public record RegionRule(String regionId, GameMode gamemode, int priority) {
    }

    public WorldGuardPlugin getWorldGuard() {

        final Plugin worldGuard = getServer().getPluginManager().getPlugin("WorldGuard");
        if (worldGuard instanceof WorldGuardPlugin) {

            return (WorldGuardPlugin) worldGuard;

        } else {

            return null;

        }

    }

    private String normalizeRegionId(String regionId) {

        if (regionId == null) {

            return null;

        }

        final String id = StringUtils.lowerCase(regionId, Locale.ROOT);
        if ("__global__".equals(id)) {

            return "__global__";

        }

        return id;

    }

    private void validateGamemodes() {

        if (getConfig().getConfigurationSection("regions") == null) {

            throw new IllegalArgumentException("Missing 'regions' section in config.yml");

        }

        final Map<String, Object> regions = getConfig().getConfigurationSection("regions").getValues(false);
        regions.entrySet().forEach(entry -> {

            try {

                GameMode.valueOf(StringUtils.upperCase(entry.getValue().toString()));

            } catch (IllegalArgumentException | NullPointerException error) {

                throw new IllegalArgumentException(
                        "Invalid gamemode specified in config.yml for region '" + entry.getKey() + "'");

            }

        });

    }

    private void loadRegionRules() {

        final Map<String, GameMode> rules = new HashMap<>();
        final Map<String, Object> regions = getConfig().getConfigurationSection("regions").getValues(false);

        regions.entrySet().forEach(entry -> {

            final String id = normalizeRegionId(entry.getKey());
            final GameMode gamemode = GameMode.valueOf(StringUtils.upperCase(entry.getValue().toString()));
            rules.put(id, gamemode);

        });

        this.regionRules = rules;

    }

    public void reloadRegionRules() {

        reloadConfig();
        validateGamemodes();
        loadRegionRules();

    }

    public RegionRule currentRegionRule(Player player) {

        final RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        final RegionQuery query = container.createQuery();
        final Location location = BukkitAdapter.adapt(player.getLocation());
        final ApplicableRegionSet set = query.getApplicableRegions(location);

        RegionRule best = null;

        for (ProtectedRegion region : set.getRegions()) {

            final String id = normalizeRegionId(region.getId());
            final GameMode gamemode = this.regionRules.get(id);
            if (gamemode == null) {

                continue;

            }

            final int priority = region.getPriority();
            if (best == null) {

                best = new RegionRule(id, gamemode, priority);
                continue;

            }

            if (priority > best.priority()) {

                best = new RegionRule(id, gamemode, priority);
                continue;

            }

            if (priority == best.priority() && id.compareTo(best.regionId()) < 0) {

                best = new RegionRule(id, gamemode, priority);

            }

        }

        return best;

    }

    public void refreshPlayer(Player player, boolean announce) {

        if (player == null || !player.isOnline()) {

            return;

        }

        final boolean announceEnabled = announce && getConfig().getBoolean("announceGamemodeChange");
        final UUID uuid = player.getUniqueId();
        final PlayerState state = this.playersChanged.get(uuid);
        final RegionRule rule = currentRegionRule(player);

        if (rule == null) {

            if (state == null) {

                return;

            }

            if (announceEnabled) {

                UtilitiesOG.trueogMessage(player, ("&eYou are now leaving... &2&l"
                        + StringUtils.lowerCase(state.appliedGamemode().name()) + "."));

            }

            if (player.getGameMode() != state.originalGamemode()) {

                player.setGameMode(state.originalGamemode());

            }

            this.playersChanged.remove(uuid);
            return;

        }

        final GameMode desired = rule.gamemode();

        if (state == null) {

            if (player.getGameMode() == desired) {

                return;

            }

            this.playersChanged.put(uuid, new PlayerState(player.getGameMode(), rule.regionId(), desired));
            player.setGameMode(desired);

            if (announceEnabled) {

                UtilitiesOG.trueogMessage(player,
                        ("&eYou are now entering... &2&l" + StringUtils.lowerCase(desired.name()) + "."));

            }

            return;

        }

        if (player.getGameMode() != desired) {

            player.setGameMode(desired);

            if (announceEnabled) {

                UtilitiesOG.trueogMessage(player,
                        ("&eYou are now entering... &2&l" + StringUtils.lowerCase(desired.name()) + "."));

            }

        }

        if (desired == state.originalGamemode()) {

            this.playersChanged.remove(uuid);
            return;

        }

        if (!state.regionId().equals(rule.regionId()) || state.appliedGamemode() != desired) {

            this.playersChanged.put(uuid, new PlayerState(state.originalGamemode(), rule.regionId(), desired));

        }

    }

    public void restorePlayer(Player player) {

        if (player == null) {

            return;

        }

        final UUID uuid = player.getUniqueId();
        final PlayerState state = this.playersChanged.remove(uuid);
        if (state == null) {

            return;

        }

        if (player.getGameMode() != state.originalGamemode()) {

            player.setGameMode(state.originalGamemode());

        }

    }

    public boolean isPlayerOverridden(Player player) {

        if (player == null) {

            return false;

        }

        return this.playersChanged.containsKey(player.getUniqueId());

    }

    @Override
    public void onEnable() {

        // Set plugin instance.
        plugin = this;

        saveDefaultConfig();
        validateGamemodes();
        loadRegionRules();

        this.playersChanged = new HashMap<>((int) (getServer().getMaxPlayers() / 0.75f + 1));

        // Initialize event listeners.
        getServer().getPluginManager().registerEvents(new GamemodeListener(this), this);
        getCommand("wgadd").setExecutor(new AddRegion());
        getCommand("wgremove").setExecutor(new RemoveRegion());

        getServer().getOnlinePlayers()
                .forEach(p -> getServer().getScheduler().runTask(this, () -> refreshPlayer(p, true)));

        getLogger().info("Loaded successfully!");

    }

    @Override
    public void onDisable() {

        // Return all players to their original game modes to avoid potential issues.
        this.playersChanged.entrySet().forEach(entry -> {

            final Player player = getServer().getPlayer(entry.getKey());
            if (player == null) {

                return;

            }

            final GameMode original = entry.getValue().originalGamemode();
            if (player.getGameMode() != original) {

                player.setGameMode(original);

            }

        });

        this.playersChanged.clear();

        getLogger().info("Player gamemodes returned to original values");

    }

    public String currentRegion(Player player) {

        final RegionRule rule = currentRegionRule(player);
        return rule == null ? null : rule.regionId();

    }

    public Set<String> managedRegions() {

        return this.regionRules.keySet();

    }

    // Constructor so that the main class (this) can be referenced from other
    // classes.
    public static WGamemodeOG getPlugin() {

        // Pass instance of main.
        return plugin;

    }

    // Return plugin prefix.
    public static String getPrefix() {

        return "[WGamemode-OG]";

    }

}
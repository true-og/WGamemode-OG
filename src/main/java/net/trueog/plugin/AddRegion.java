/*
   WGamemode 3, an automatic gamemode switching plugin for Spigot 1.18
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

import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class AddRegion implements CommandExecutor {

    private WGamemodeOG plugin;

    public AddRegion(WGamemodeOG instance) {

        this.plugin = instance;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // If the argument length is correct, do this...
        if (args.length >= 2) {

            String regionName = args[0];
            String regionGamemode = args[1];

            // Verify that the gamemode given is valid.
            // (why does Java not have an Enum.contains method...?)
            boolean gamemodeValid = false;
            for (GameMode gm : GameMode.values()) {

                if (gm.name().equals(regionGamemode.toUpperCase())) {

                    gamemodeValid = true;

                    break;
                }
            }

            // Add region to the config file & save.
            ConfigurationSection regions = this.plugin.getConfig().getConfigurationSection("regions");
            if (regions != null && gamemodeValid) {

                regions.set(regionName, regionGamemode);

                this.plugin.saveConfig();

                if (sender instanceof Player) {

                    Utils.trueogMessage(
                            (Player) sender, "&aAdded automatic gamemode rule for region &e\"" + regionName + "\"&a.");

                } else {

                    WGamemodeOG.getPlugin()
                            .getLogger()
                            .info("Added automatic gamemode rule for region: \"" + regionName + "\".");
                }

            } else if (!gamemodeValid) {

                if (sender instanceof Player) {

                    Utils.trueogMessage((Player) sender, ("&cERROR: Invalid gamemode! &6Try again."));

                } else {

                    WGamemodeOG.getPlugin().getLogger().info("ERROR: Invalid gamemode! Try again.");
                }

            } else {

                // Returning false means the command failed unexpectedly.
                return false;
            }
        }

        return true;
    }
}

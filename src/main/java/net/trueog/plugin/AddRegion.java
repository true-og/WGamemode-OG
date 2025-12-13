/*
   WGamemode 3, an automatic gamemode switching plugin for Spigot
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

// Updated for https://true-og.net by NotAlexNoyle
package net.trueog.plugin;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import net.trueog.utilitiesog.UtilitiesOG;

public class AddRegion implements CommandExecutor {

    private String normalizeRegionId(String regionId) {

        if (regionId == null) {

            return null;

        }

        final String id = StringUtils.lowerCase(regionId);
        if ("__global__".equals(id)) {

            return "__global__";

        }

        return id;

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // If the argument length is correct, do this...
        if (args.length < 2) {

            return false;

        }

        final String regionName = normalizeRegionId(args[0]);
        final String regionGamemode = args[1];
        boolean gamemodeValid = false;
        for (GameMode gm : GameMode.values()) {

            if (gm.name().equals(StringUtils.upperCase(regionGamemode))) {

                gamemodeValid = true;
                break;

            }

        }

        final ConfigurationSection regions = WGamemodeOG.getPlugin().getConfig().getConfigurationSection("regions");
        if (regions != null && gamemodeValid) {

            regions.set(regionName, StringUtils.lowerCase(regionGamemode));
            WGamemodeOG.getPlugin().saveConfig();
            WGamemodeOG.getPlugin().reloadRegionRules();

            if (sender instanceof Player) {

                UtilitiesOG.trueogMessage((Player) sender,
                        "&aAdded automatic gamemode rule for region &e\"" + regionName + "\"&a.");

            } else {

                WGamemodeOG.getPlugin().getLogger()
                        .info("Added automatic gamemode rule for region: \"" + regionName + "\".");

            }

        } else if (!gamemodeValid) {

            if (sender instanceof Player) {

                UtilitiesOG.trueogMessage((Player) sender, ("&cERROR: Invalid gamemode! &6Try again."));

            } else {

                UtilitiesOG.logToConsole(WGamemodeOG.getPrefix(), "ERROR: Invalid gamemode! Try again.");

            }

        } else {

            return false;

        }

        return true;

    }

}
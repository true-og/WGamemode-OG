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

import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import net.trueog.utilitiesog.UtilitiesOG;

public class RemoveRegion implements CommandExecutor {

    private String findKeyIgnoreCase(ConfigurationSection section, String input) {

        if (section == null || input == null) {

            return null;

        }

        return section.getKeys(false).stream().filter(key -> StringUtils.equalsIgnoreCase(key, input)).findFirst()
                .orElse(null);

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // If the argument length is correct, do this...
        if (args.length < 1) {

            return false;

        }

        final String regionName = args[0];
        final ConfigurationSection regions = WGamemodeOG.getPlugin().getConfig().getConfigurationSection("regions");
        if (regions == null) {

            return false;

        }

        final String key = findKeyIgnoreCase(regions, regionName);
        if (key != null && regions.isSet(key)) {

            regions.set(key, null);

            WGamemodeOG.getPlugin().saveConfig();
            WGamemodeOG.getPlugin().reloadRegionRules();

            if (sender instanceof Player) {

                UtilitiesOG.trueogMessage((Player) sender,
                        "&aRemoved automatic gamemode rule for region &e\"" + key + "\"&a.");

            } else {

                WGamemodeOG.getPlugin().getLogger().info("Removed automatic gamemode rule for region \"" + key + "\".");

            }

        } else if (key == null) {

            if (sender instanceof Player) {

                UtilitiesOG.trueogMessage((Player) sender,
                        "&cERROR: The region \"&e" + regionName + "\" &cis not managed by WGamemode.");

            } else {

                WGamemodeOG.getPlugin().getLogger()
                        .info("ERROR: The region \"" + regionName + "\" is not managed by WGamemode.");

            }

        } else {

            return false;

        }

        return true;

    }

}
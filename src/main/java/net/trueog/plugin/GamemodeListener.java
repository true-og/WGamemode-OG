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

import java.util.ArrayList;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class GamemodeListener implements Listener {

	private WGamemodeOG plugin;
	private ArrayList<Player> enteredRegion = new ArrayList<Player>();

	public GamemodeListener(WGamemodeOG instance) {

		this.plugin = instance;

	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {

		Player player = event.getPlayer();

		// Check if the player is currently in a region we're managing.
		String currentRegion = this.plugin.currentRegion(player);
		if (currentRegion != null) {

			GameMode regionGamemode = GameMode.valueOf(this.plugin.getConfig().getConfigurationSection("regions").getString(currentRegion).toUpperCase());

			// If their gamemode doesn't match the region's gamemode, change it!
			if (player.getGameMode() != regionGamemode) {

				// Add the player to the list of mutated players.
				if(! this.enteredRegion.contains(player)) {

					this.plugin.playersChanged.put(player, player.getGameMode());

				}

				player.setGameMode(regionGamemode);

				if(this.plugin.getConfig().getBoolean("announceGamemodeChange")) {

					Utils.trueogMessage(player, ("&eYou are now entering... &2&l" + regionGamemode.name().toLowerCase() + "."));

				}

			}

			// Mark this player as having entered a managed region.
			this.enteredRegion.add(player);

		}
		// If the user isn't in a region we manage, see if we've updated their status yet.
		else if (this.plugin.playersChanged.containsKey(player)) {

			if (this.plugin.getConfig().getBoolean("announceGamemodeChange")) {

				Utils.trueogMessage(player, ("&eYou are now leaving... &2&l" + player.getGameMode().name().toLowerCase() + "."));

			}

			// We haven't updated the player's status, so do that now.
			player.setGameMode(this.plugin.playersChanged.get(player));

			this.plugin.playersChanged.remove(player);
			this.enteredRegion.remove(player);

		}

	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {

		Player player = event.getPlayer();
		if (this.plugin.playersChanged.containsKey(player)) {

			player.setGameMode(this.plugin.playersChanged.get(player));

			this.plugin.playersChanged.remove(player);

		}

	}

	@EventHandler
	public void onPlayerKick(PlayerKickEvent event) {

		Player player = event.getPlayer();
		if (this.plugin.playersChanged.containsKey(player)) {

			player.setGameMode(this.plugin.playersChanged.get(player));

			this.plugin.playersChanged.remove(player);

		}

	}

	@EventHandler
	public void onPlayerItemDrop(PlayerDropItemEvent event) {

		Player player = event.getPlayer();
		if (this.plugin.getConfig().getBoolean("stopItemDrop") && this.plugin.playersChanged.containsKey(player)) {

			event.setCancelled(true);

		}

	}

}
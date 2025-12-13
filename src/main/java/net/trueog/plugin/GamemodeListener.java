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

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class GamemodeListener implements Listener {

    private final WGamemodeOG plugin;

    public GamemodeListener(WGamemodeOG instance) {

        this.plugin = instance;

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        final Player player = event.getPlayer();
        this.plugin.getServer().getScheduler().runTask(this.plugin, () -> this.plugin.refreshPlayer(player, true));

    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {

        final Player player = event.getPlayer();
        this.plugin.getServer().getScheduler().runTask(this.plugin, () -> this.plugin.refreshPlayer(player, true));

    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {

        final Player player = event.getPlayer();
        this.plugin.refreshPlayer(player, true);

    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {

        final Player player = event.getPlayer();
        this.plugin.getServer().getScheduler().runTask(this.plugin, () -> this.plugin.refreshPlayer(player, true));

    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {

        if (event.getTo() == null) {

            return;

        }

        if (event.getFrom().getWorld() == event.getTo().getWorld()
                && event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ())
        {

            return;

        }

        this.plugin.refreshPlayer(event.getPlayer(), true);

    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {

        this.plugin.restorePlayer(event.getPlayer());

    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {

        this.plugin.restorePlayer(event.getPlayer());

    }

    @EventHandler
    public void onPlayerItemDrop(PlayerDropItemEvent event) {

        final Player player = event.getPlayer();
        if (this.plugin.getConfig().getBoolean("stopItemDrop") && this.plugin.isPlayerOverridden(player)) {

            event.setCancelled(true);

        }

    }

}
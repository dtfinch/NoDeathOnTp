/*
 NoDeathOnTp: Attempt to resolving the death bug after a teleportation
 Copyright (C) 2013  Michel Blanchet

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package me.tabinol.nodeathontp;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.java.JavaPlugin;

public class NoDeathOnTp extends JavaPlugin implements Listener {

    private long maxTimeCancelDamage;
    private long minTimeAfterCancel;
    private String msgPlayerLocationChange;
    private boolean isDebug;
    private HashMap<Player, PlayerTpData> tpList = new HashMap<Player, PlayerTpData>();

    @Override
    public void onEnable() {

        //For mcstats source : https://github.com/Hidendra/Plugin-Metrics/wiki/Usage
        try {
            MetricsLite metrics = new MetricsLite(this);
            metrics.start();
        } catch (IOException e) {
            // Failed to submit the stats :-(
        }
        
        this.saveDefaultConfig();
        maxTimeCancelDamage = this.getConfig().getLong("General.MaxTimeCancelDamage");
        minTimeAfterCancel = this.getConfig().getLong("General.MinTimeAfterCancel");
        isDebug = this.getConfig().getBoolean("General.Debug");
        msgPlayerLocationChange = this.getConfig().getString("Messages.PlayerLocationChange");

        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {

        tpList.remove(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {

        updateTpList(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {

        doLog("Teleport Player: " + event.getPlayer().getName() + " TeleportCause: " + event.getCause());
        if (event.getCause() == TeleportCause.UNKNOWN || event.getPlayer().isInsideVehicle()) {
            return;
        }
        if (event.getCause() == TeleportCause.ENDER_PEARL) {
            tpList.put(event.getPlayer(), null);
        } else {
            updateTpList(event.getPlayer());
        }
    }

    private void updateTpList(Player player) {

        tpList.put(player, new PlayerTpData(Calendar.getInstance(), player.getLocation().getBlockX(), player.getLocation().getBlockZ()));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {

        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            PlayerTpData tpData;
            int posx = player.getLocation().getBlockX();
            int posz = player.getLocation().getBlockZ();
            Long nowLong = Calendar.getInstance().getTimeInMillis();
            if ((tpData = tpList.get(player)) != null
                    && tpData.getLastTpTime().getTimeInMillis() + maxTimeCancelDamage > nowLong
                    && nowLong > tpData.getLastReLocation() + minTimeAfterCancel
                    && isInInterval(tpData.getLastX(), posx - 3, posx + 3)
                    && isInInterval(tpData.getLastZ(), posz - 3, posz + 3)) {
                if (event.getCause() == DamageCause.FALL) {
                    doLog("On time, Detect Fall damage Player: " + player.getName());
                    event.setCancelled(true);
                    tpData.setLastReLocationToNow(nowLong);
                    notifyNoDeath(player);
                } else if (event.getCause() == DamageCause.SUFFOCATION) {
                    doLog("On time, Detect Suffocation damage Player: " + player.getName());
                    event.setCancelled(true);
                    tpData.setLastReLocationToNow(nowLong);
                    reLocation(player);
                    notifyNoDeath(player);
                }
            }
        }
    }

    private void reLocation(Player player) {

        Location loc = player.getLocation();

        do {
            loc.add(0, 1, 0);
        } while (loc.getBlockY() < loc.getWorld().getMaxHeight()
                && !(loc.getBlock().getType() != Material.LAVA
                && loc.getBlock().getRelative(BlockFace.UP).getType() == Material.AIR
                && loc.getBlock().getRelative(BlockFace.UP, 2).getType() == Material.AIR));

        player.teleport(loc.add(0, 1, 0));
    }

    private void notifyNoDeath(Player tpPlayer) {

        this.getLogger().log(Level.INFO, msgPlayerLocationChange
                + tpPlayer.getPlayerListName());
        for (Player players : this.getServer().getOnlinePlayers()) {
            if (players.hasPermission("nodeathontp.notify")) {
                players.sendMessage(ChatColor.GREEN + "[NoDeathOnTp] "
                        + ChatColor.DARK_GRAY + msgPlayerLocationChange
                        + tpPlayer.getPlayerListName());
            }
        }
    }

    private void doLog(String msg) {

        if (isDebug) {
            this.getLogger().log(Level.INFO, msg);
        }
    }

    private static boolean isInInterval(int nbSource, int nb1, int nb2) {

        return nbSource >= nb1 && nbSource <= nb2;
    }
}

package net.pl3x.pl3xcraft.listener;

import net.pl3x.pl3xcraft.commands.CmdBack;
import net.pl3x.pl3xcraft.configuration.Config;
import net.pl3x.pl3xcraft.configuration.Lang;
import net.pl3x.pl3xcraft.configuration.PlayerConfig;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashSet;
import java.util.Set;

public class PlayerListener implements Listener {
    private final Set<PlayerTeleportEvent.TeleportCause> backAllowCauses = new HashSet<>();

    public PlayerListener() {
        backAllowCauses.add(PlayerTeleportEvent.TeleportCause.PLUGIN);
        backAllowCauses.add(PlayerTeleportEvent.TeleportCause.COMMAND);
        backAllowCauses.add(PlayerTeleportEvent.TeleportCause.UNKNOWN);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void backOnDeath(PlayerDeathEvent event) {
        if (!Config.BACK_ON_DEATH) {
            return;
        }

        Player player = event.getEntity();
        if (!player.hasPermission("command.back")) {
            return;
        }

        CmdBack.setPreviousLocation(player, player.getLocation());
        Lang.send(player, Lang.BACK_DEATH_HINT);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void backOnTeleport(PlayerTeleportEvent event) {
        if (!backAllowCauses.contains(event.getCause())) {
            return; // cause not allowed
        }

        if (!event.getPlayer().hasPermission("command.back")) {
            return;
        }

        Location to = event.getTo();
        Location from = event.getFrom();

        // only save location if teleporting more than 5 blocks
        if (!to.getWorld().equals(from.getWorld()) || to.distanceSquared(from) > 25) {
            CmdBack.setPreviousLocation(event.getPlayer(), event.getFrom());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        CmdBack.setPreviousLocation(event.getPlayer(), null);
        PlayerConfig.remove(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerSleep(PlayerBedEnterEvent event) {
        event.getPlayer().setBedSpawnLocation(event.getBed().getLocation());
        Lang.send(event.getPlayer(), Lang.BED_SPAWN_SET);
    }
}

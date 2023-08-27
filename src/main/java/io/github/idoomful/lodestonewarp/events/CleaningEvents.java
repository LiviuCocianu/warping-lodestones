package io.github.idoomful.lodestonewarp.events;

import io.github.idoomful.bukkitutils.statics.Events;
import io.github.idoomful.lodestonewarp.DMain;
import io.github.idoomful.lodestonewarp.data.Cache;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class CleaningEvents {
    public CleaningEvents(DMain main) {
        final Cache cache = main.getCache();

        Events.listen(main, InventoryCloseEvent.class, e -> {
            final Player player = (Player) e.getPlayer();
            final UUID uuid = player.getUniqueId();

            Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
                if(player.getOpenInventory().getType() == InventoryType.CRAFTING
                        || player.getOpenInventory().getType() == InventoryType.CREATIVE) {
                    if(!main.getCache().getLodestonePermissionSetup().containsKey(uuid))
                        cache.getInventories().remove(uuid);
                }
            }, 10);
        });

        Events.listen(main, PlayerQuitEvent.class, e -> {
            final UUID uuid = e.getPlayer().getUniqueId();

            cache.getInventories().remove(uuid);
            cache.getLinker().remove(uuid);
            cache.getSafetyList().remove(uuid);
            cache.getLodestonePermissionSetup().remove(uuid);
        });
    }
}

package io.github.idoomful.lodestonewarp.events;

import io.github.bananapuncher714.nbteditor.NBTEditor;
import io.github.idoomful.bukkitutils.json.JsonLocation;
import io.github.idoomful.bukkitutils.statics.Events;
import io.github.idoomful.bukkitutils.statics.ItemBuilder;
import io.github.idoomful.bukkitutils.statics.ItemUtils;
import io.github.idoomful.lodestonewarp.DMain;
import io.github.idoomful.lodestonewarp.configuration.MessagesYML;
import io.github.idoomful.lodestonewarp.data.WarpingLodestone;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class InstallUninstallEvents {
    public InstallUninstallEvents(DMain main) {
        Events.listen(main, BlockPlaceEvent.class, e -> {
            ItemStack item = e.getItemInHand();

            if(NBTEditor.contains(item, "WarpingLodestone")) {
                final Player player = e.getPlayer();

                WarpingLodestone lodestone = new WarpingLodestone(player.getUniqueId(), new JsonLocation(e.getBlock().getLocation()));
                lodestone.add();

                player.sendMessage(MessagesYML.PLACED_LODESTONE.withPrefix(player));
            }
        });

        Events.listen(main, BlockBreakEvent.class, e -> {
            final Location breakLoc = e.getBlock().getLocation();
            final Player player = e.getPlayer();

            if(!WarpingLodestone.isLodestone(breakLoc)) return;

            if(!WarpingLodestone.ownsLodestone(player, breakLoc)) {
                player.sendMessage(MessagesYML.Errors.NOT_OWNS.withPrefix(player));
                e.setCancelled(true);
                return;
            }

            ItemStack inHand = ItemUtils.getItemInHand(player);
            if(inHand != null) {
                if(inHand.getType().toString().contains("SWORD") && player.getGameMode() == GameMode.CREATIVE) return;
            }

            List<WarpingLodestone> lodestones = main.getCache().getLodestones();

            for(WarpingLodestone lodestone : lodestones) {
                final Location databaseLoc = lodestone.getLocation().getLocation();

                if(breakLoc.equals(databaseLoc)) {
                    if(player.getGameMode() != GameMode.CREATIVE) {
                        e.setDropItems(false);

                        String pattern = main.getConfigs().getFile("settings").getString("recipes.warping_lodestone.result");

                        if(pattern == null) {
                            player.sendMessage(MessagesYML.Errors.GIVE_ERROR.withPrefix(player));
                            return;
                        }

                        ItemStack item = ItemBuilder.build(pattern);
                        breakLoc.getWorld().dropItem(breakLoc, item);
                    }

                    lodestone.remove();
                    player.sendMessage(MessagesYML.BROKE_LODESTONE.withPrefix(player));
                    break;
                }
            }
        });
    }
}

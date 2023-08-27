package io.github.idoomful.lodestonewarp.events;

import io.github.idoomful.bukkitutils.json.JsonLocation;
import io.github.idoomful.bukkitutils.statics.BeautifyUtils;
import io.github.idoomful.bukkitutils.statics.Events;
import io.github.idoomful.lodestonewarp.DMain;
import io.github.idoomful.lodestonewarp.configuration.MessagesYML;
import io.github.idoomful.lodestonewarp.configuration.SettingsYML;
import io.github.idoomful.lodestonewarp.data.Cache;
import io.github.idoomful.lodestonewarp.data.LodestoneWarp;
import io.github.idoomful.lodestonewarp.data.WarpingLodestone;
import io.github.idoomful.lodestonewarp.gui.LodestoneMainUI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Objects;
import java.util.UUID;

public class InteractionEvents {
    public InteractionEvents(DMain main) {
        final Cache cache = main.getCache();

        Events.listen(main, PlayerInteractEvent.class, e -> {
            if(e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
            if(e.getHand() == EquipmentSlot.OFF_HAND) return;

            Location interactLoc = Objects.requireNonNull(e.getClickedBlock()).getLocation();
            Player player = e.getPlayer();
            UUID uuid = player.getUniqueId();

            if(!WarpingLodestone.isLodestone(interactLoc)) return;

            e.setCancelled(true);

            if(!cache.getLinker().containsKey(uuid)) {
                WarpingLodestone lodestone = cache.getLodestoneAt(interactLoc);

                cache.getInventories().put(uuid, new LodestoneMainUI(player, lodestone));
                cache.getInventories().get(uuid).openInventory();
                BeautifyUtils.playSoundSimple(main, player, SettingsYML.Options.MENU_NAVIGATE_SOUND.get(String.class));
            }
            // If in linking setup and no destination lodestone has been set
            else if(cache.getLinker().get(uuid).getDestionation() == null) {
                WarpingLodestone originLodestone = cache.getLinker().get(uuid).getOrigin();

                // Regular lodestone linking
                if(originLodestone != null) {
                    if(originLodestone.getLocation().getLocation().toString().equals(interactLoc.toString())) {
                        player.sendMessage(MessagesYML.Errors.SAME_LODESTONE.withPrefix(player));
                        return;
                    }

                    final WarpingLodestone clickedLodestone = cache.getLodestoneAt(interactLoc);
                    final OfflinePlayer clickedOwner = Bukkit.getOfflinePlayer(clickedLodestone.getOwner());

                    final JsonLocation clickedJsonLoc = clickedLodestone.getWarpLocation();

                    if(
                            originLodestone.getPublicWarps().stream()
                                    .anyMatch(warp -> warp.getWarpTo().toString().equals(clickedJsonLoc.toString()))
                                    || originLodestone.getPrivateWarps().stream()
                                    .anyMatch(warp -> warp.getWarpTo().toString().equals(clickedJsonLoc.toString()))
                    ) {
                        player.sendMessage(MessagesYML.Errors.ALREADY_LINKED.withPrefix(player));
                        return;
                    }

                    if(!clickedLodestone.getWhitelist().contains(player.getUniqueId())
                            && !clickedLodestone.isPublic()
                            && !clickedLodestone.getOwner().toString().equals(uuid.toString())
                            && !player.hasPermission("lodestonewarp.bypass")
                    ) {
                        player.sendMessage(MessagesYML.Errors.PRIVATE_LODESTONE.withPrefix(player)
                                .replace("$player$", Objects.requireNonNull(clickedOwner.getName()))
                        );
                        return;
                    }

                    cache.getLinker().get(uuid).setDestionation(clickedLodestone);
                }
                // Global warp linking
                else {
                    for(LodestoneWarp globalWarp : cache.getGlobalWarps()) {
                        if(globalWarp.getWarpTo().getLocation().toString().equals(interactLoc.clone().add(0.5, 1, 0.5).toString())) {
                            player.sendMessage(MessagesYML.Errors.GLOBAL_WARP_EXISTS.withPrefix(player)
                                    .replace("$name$", globalWarp.getName()));
                            return;
                        }
                    }

                    WarpingLodestone lodestone = cache.getLodestoneAt(interactLoc);
                    cache.getLinker().get(uuid).setOrigin(lodestone);
                    cache.getLinker().get(uuid).toggleGlobal(true);
                }

                String supportColors = SettingsYML.WarpName.COLORS.get(Boolean.class)
                        ? MessagesYML.Words.COLORS.color(player)
                        : MessagesYML.Words.NO_COLORS.color(player);

                player.sendMessage(MessagesYML.REQUEST_NAME.withPrefix(player).replace("$supportColors$", supportColors));
            }
        });
    }
}

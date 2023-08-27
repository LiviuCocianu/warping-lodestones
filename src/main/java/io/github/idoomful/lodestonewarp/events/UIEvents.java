package io.github.idoomful.lodestonewarp.events;

import io.github.bananapuncher714.nbteditor.NBTEditor;
import io.github.idoomful.bukkitutils.statics.BeautifyUtils;
import io.github.idoomful.bukkitutils.statics.Events;
import io.github.idoomful.lodestonewarp.DMain;
import io.github.idoomful.lodestonewarp.configuration.MessagesYML;
import io.github.idoomful.lodestonewarp.configuration.SettingsYML;
import io.github.idoomful.lodestonewarp.data.Cache;
import io.github.idoomful.lodestonewarp.data.LodestoneWarp;
import io.github.idoomful.lodestonewarp.data.WarpingLodestone;
import io.github.idoomful.lodestonewarp.gui.LodestoneMainUI;
import io.github.idoomful.lodestonewarp.gui.LodestoneSettingsUI;
import io.github.idoomful.lodestonewarp.gui.LodestoneWarpsUI;
import io.github.idoomful.lodestonewarp.objects.LodestoneLink;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class UIEvents {
    public UIEvents(DMain main) {
        final Cache cache = main.getCache();

        Events.listen(main, InventoryClickEvent.class, e -> {
            if(e.getClickedInventory() == null) return;
            if(e.getCurrentItem() == null) return;

            Player player = (Player) e.getWhoClicked();
            UUID uuid = player.getUniqueId();
            ItemStack clicked = e.getCurrentItem();

            if(!cache.hasInventory(player)) return;

            e.setCancelled(true);

            if(cache.getInventories().get(uuid) instanceof LodestoneMainUI) {
                if(NBTEditor.contains(clicked, "UIButtonID")) {
                    LodestoneMainUI ui = (LodestoneMainUI) cache.getInventories().get(uuid);
                    String value = NBTEditor.getString(clicked, "UIButtonID");

                    if (value.equalsIgnoreCase("link")) {
                        if(cache.getLinker().containsKey(player.getUniqueId())) {
                            player.closeInventory();
                            player.sendMessage(MessagesYML.Errors.IS_LINKING.withPrefix(player));
                            return;
                        }

                        if(!WarpingLodestone.ownsLodestone(player, ui.getLodestone().getLocation().getLocation())) {
                            player.closeInventory();
                            player.sendMessage(MessagesYML.Errors.NOT_OWNS.withPrefix(player));
                            return;
                        }

                        player.closeInventory();
                        cache.getLinker().put(player.getUniqueId(), new LodestoneLink(ui.getLodestone()));
                        player.sendMessage(MessagesYML.LINKING.withPrefix(player));
                    } else if(value.equalsIgnoreCase("settings")) {
                        if(!WarpingLodestone.ownsLodestone(player, ui.getLodestone().getLocation().getLocation())) {
                            player.closeInventory();
                            player.sendMessage(MessagesYML.Errors.NOT_OWNS.withPrefix(player));
                            return;
                        }

                        cache.getInventories().put(uuid, new LodestoneSettingsUI(player, ui.getLodestone()));
                        cache.getInventories().get(uuid).openInventory();
                        BeautifyUtils.playSoundSimple(main, player, SettingsYML.Options.MENU_NAVIGATE_SOUND.get(String.class));
                    }
                    // For public, private and global
                    else {
                        cache.getInventories().put(uuid, new LodestoneWarpsUI(player,
                                ui.getLodestone(),
                                LodestoneWarp.Scope.valueOf(value.toUpperCase()))
                        );
                        cache.getInventories().get(uuid).openInventory();
                        BeautifyUtils.playSoundSimple(main, player, SettingsYML.Options.MENU_NAVIGATE_SOUND.get(String.class));
                    }
                }
            } else if(cache.getInventories().get(uuid) instanceof LodestoneWarpsUI) {
                if(NBTEditor.contains(clicked, "UIButtonID")) {
                    LodestoneWarpsUI ui = (LodestoneWarpsUI) cache.getInventories().get(uuid);
                    String value = NBTEditor.getString(clicked, "UIButtonID");

                    if(value.equalsIgnoreCase("warp")) {
                        WarpingLodestone lodestone = ui.getLodestone();

                        String warpCoord = NBTEditor.getString(clicked, "WarpTo").replace(" ", "_");

                        String warpToName = warpCoord.split(",")[0].replace("_", " ");
                        Location to = new Location(
                                Bukkit.getWorld(warpCoord.split(",")[1]),
                                Double.parseDouble(warpCoord.split(",")[2]),
                                Double.parseDouble(warpCoord.split(",")[3]),
                                Double.parseDouble(warpCoord.split(",")[4])
                        );

                        List<LodestoneWarp> warps = ui.getScope() == LodestoneWarp.Scope.PUBLIC
                                ? lodestone.getPublicWarps()
                                : ui.getScope() == LodestoneWarp.Scope.PRIVATE
                                ? lodestone.getPrivateWarps()
                                : cache.getGlobalWarps();

                        for(LodestoneWarp warp : warps) {
                            if(warp.getWarpTo().getLocation().toString().equals(to.toString())
                                    && warp.getName().equals(warpToName)) {
                                if(e.getClick() == ClickType.MIDDLE) {
                                    if(ui.getScope() == LodestoneWarp.Scope.GLOBAL
                                            && !player.hasPermission("lodestonewarp.command.adminwarp")) return;

                                    if(!WarpingLodestone.ownsLodestone(player, lodestone.getLocation().getLocation())) {
                                        player.closeInventory();
                                        player.sendMessage(MessagesYML.Errors.NOT_OWNS.withPrefix(player));
                                        return;
                                    }

                                    if(!cache.getSafetyList().contains(uuid)) {
                                        player.closeInventory();
                                        player.sendMessage(MessagesYML.ASK_DELETE.withPrefix(player).replace("$name$", warp.getName()));
                                        cache.getSafetyList().add(uuid);
                                        return;
                                    }

                                    WarpingLodestone destLode = main.getCache().getLodestoneAt(warp.getWarpTo().getLocation().clone().subtract(0.5, 1, 0.5));

                                    lodestone.warpAction(warp, WarpingLodestone.Action.REMOVE, ui.getScope());
                                    if(SettingsYML.Options.MUTUAL_LINK.get(Boolean.class)) {
                                        for(LodestoneWarp destWarp : destLode.getPublicWarps()) {
                                            if(warp.getName().equals(destWarp.getName())) {
                                                destLode.warpAction(destWarp, WarpingLodestone.Action.REMOVE, LodestoneWarp.Scope.PUBLIC);
                                                break;
                                            }
                                        }

                                        for(LodestoneWarp destWarp : destLode.getPrivateWarps()) {
                                            if(warp.getName().equals(destWarp.getName())) {
                                                destLode.warpAction(destWarp, WarpingLodestone.Action.REMOVE, LodestoneWarp.Scope.PRIVATE);
                                                break;
                                            }
                                        }
                                    }

                                    player.closeInventory();
                                    player.sendMessage(MessagesYML.WARP_DELETED.withPrefix(player).replace("$name$", warp.getName()));
                                    cache.getSafetyList().remove(uuid);
                                    return;
                                }

                                if(!lodestone.isOnLodestone(player)) {
                                    player.closeInventory();
                                    player.sendMessage(MessagesYML.Errors.NOT_STANDING.withPrefix(player));
                                    return;
                                }

                                if(lodestone.isWarping(player)) return;

                                player.closeInventory();

                                Location modified = warp.getWarpTo().getLocation().subtract(0.5, 1, 0.5);
                                WarpingLodestone dest = cache.getLodestoneAt(modified);

                                if(dest == null) {
                                    player.sendMessage(MessagesYML.Errors.NOT_AVAILABLE.withPrefix(player));
                                    return;
                                }

                                if(!warp.isSafe() && !cache.getSafetyList().contains(uuid)) {
                                    cache.getSafetyList().add(uuid);
                                    player.sendMessage(MessagesYML.UNSAFE_WARP.withPrefix(player));
                                    return;
                                }

                                lodestone.warp(player, warp.getName(), dest);
                                cache.getSafetyList().remove(uuid);
                                break;
                            }
                        }
                    }
                }
            } else if(cache.getInventories().get(uuid) instanceof LodestoneSettingsUI) {

                if(NBTEditor.contains(clicked, "UIButtonID")) {
                    LodestoneSettingsUI ui = (LodestoneSettingsUI) cache.getInventories().get(uuid);
                    String value = NBTEditor.getString(clicked, "UIButtonID");

                    if(value.equalsIgnoreCase("status")) {
                        ui.getLodestone().setStatus(!ui.getLodestone().isPublic());
                        player.closeInventory();

                        cache.getInventories().put(uuid, new LodestoneSettingsUI(player, ui.getLodestone()));
                        cache.getInventories().get(uuid).openInventory();

                        String status = ui.getLodestone().isPublic()
                                ? MessagesYML.Words.PUBLIC.color(player)
                                : MessagesYML.Words.PRIVATE.color(player);

                        BeautifyUtils.playSoundSimple(main, player, SettingsYML.Options.SETTINGS_ICON_SOUND.get(String.class));
                        player.sendMessage(MessagesYML.UPDATE_STATUS.withPrefix(player).replace("$status$", status));
                    } else if(value.equalsIgnoreCase("perm")) {
                        if(ui.getLodestone().isPublic()) {
                            player.closeInventory();
                            player.sendMessage(MessagesYML.Errors.IS_PUBLIC.withPrefix(player));
                            return;
                        }

                        BeautifyUtils.playSoundSimple(main, player, SettingsYML.Options.SETTINGS_ICON_SOUND.get(String.class));
                        cache.getLodestonePermissionSetup().put(uuid, new LodestoneLink(ui.getLodestone()));
                        player.closeInventory();
                        MessagesYML.Lists.PERMISSION_SETUP.getStringList(player).forEach(player::sendMessage);
                    }
                }
            }
        });
    }
}

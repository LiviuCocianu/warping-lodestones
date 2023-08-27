package io.github.idoomful.lodestonewarp.events;

import io.github.idoomful.bukkitutils.statics.BeautifyUtils;
import io.github.idoomful.bukkitutils.statics.Events;
import io.github.idoomful.bukkitutils.statics.TextUtils;
import io.github.idoomful.lodestonewarp.DMain;
import io.github.idoomful.lodestonewarp.configuration.MessagesYML;
import io.github.idoomful.lodestonewarp.configuration.SettingsYML;
import io.github.idoomful.lodestonewarp.data.Cache;
import io.github.idoomful.lodestonewarp.data.LodestoneWarp;
import io.github.idoomful.lodestonewarp.data.WarpingLodestone;
import io.github.idoomful.lodestonewarp.gui.LodestoneSettingsUI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ChatEvents {
    private final List<UUID> givenTip = new ArrayList<>();

    public ChatEvents(DMain main) {
        final Cache cache = main.getCache();

        Events.listen(main, AsyncPlayerChatEvent.class, e -> {
            final Player player = e.getPlayer();
            final UUID uuid = player.getUniqueId();
            String message = e.getMessage();

            if(cache.getLinker().containsKey(uuid)) {
                if(message.matches("(?i).*cancel.*")) {
                    e.setCancelled(true);
                    cache.getLinker().remove(uuid);
                    player.sendMessage(MessagesYML.LINK_CANCEL.withPrefix(player));
                } else {
                    String name;

                    for(String word : SettingsYML.WarpName.BLACKLIST.getList()) {
                        if(message.matches(word)) {
                            e.setCancelled(true);
                            player.sendMessage(MessagesYML.Errors.BANNED_NAME.withPrefix(player));
                            return;
                        }
                    }

                    if(SettingsYML.WarpName.COLORS.get(Boolean.class)) {
                        // Apply hex colors as well as simple colors
                        if(SettingsYML.WarpName.HEX_COLORS.get(Boolean.class)) name = TextUtils.color(message);
                        // Only apply simple colors
                        else name = TextUtils.stripHex(message, false);
                    } else {
                        // Strip all kinds of colors
                        name = TextUtils.stripHex(message, true);
                    }

                    if(!cache.getLinker().get(uuid).isGlobal()) {
                        WarpingLodestone origin = cache.getLinker().get(uuid).getOrigin();
                        WarpingLodestone destination = cache.getLinker().get(uuid).getDestionation();

                        if(destination == null) return;

                        e.setCancelled(true);
                        origin.link(destination, name, destination.isPublic() ? LodestoneWarp.Scope.PUBLIC : LodestoneWarp.Scope.PRIVATE);
                        player.sendMessage(MessagesYML.LINKED.withPrefix(player)
                                .replace("$player$", Objects.requireNonNull(Bukkit.getOfflinePlayer(destination.getOwner()).getName()))
                                .replace("$name$", name)
                        );
                    } else {
                        e.setCancelled(true);
                        LodestoneWarp globalWarp = new LodestoneWarp(uuid, name, cache.getLinker().get(uuid).getOrigin().getWarpLocation());

                        try {
                            main.getSQLite().execute("INSERT INTO warps VALUES (?,?,?)",
                                    uuid.toString(), name,
                                    globalWarp.getWarpTo().toJSON()
                            );
                        } catch (SQLException ex) {
                            throw new RuntimeException(ex);
                        }

                        cache.getGlobalWarps().add(globalWarp);

                        player.sendMessage(MessagesYML.GLOBAL_WARP_CREATED.withPrefix(player).replace("$name$", name));
                        if(!givenTip.contains(uuid)) {
                            player.sendMessage(MessagesYML.GLOBAL_WARP_TIP.withPrefix(player));
                            givenTip.add(uuid);
                        }
                    }

                    cache.getLinker().remove(uuid);
                }
            } else if(cache.getLodestonePermissionSetup().containsKey(uuid)) {
                if(message.equalsIgnoreCase("-exit")) {
                    e.setCancelled(true);
                    if(cache.getLodestonePermissionSetup().containsKey(uuid)) {
                        Bukkit.getScheduler().runTask(main, () -> {
                            cache.getInventories().put(uuid, new LodestoneSettingsUI(player, cache.getLodestonePermissionSetup().get(uuid).getOrigin()));
                            cache.getInventories().get(uuid).openInventory();
                            BeautifyUtils.playSoundSimple(main, player, SettingsYML.Options.MENU_NAVIGATE_SOUND.get(String.class));

                            cache.getLodestonePermissionSetup().remove(uuid);
                            player.sendMessage(MessagesYML.LEFT_SETUP.withPrefix(player));
                        });
                    }
                } else if(message.startsWith("-add")) {
                    e.setCancelled(true);
                    String name = message.split(" ")[1];
                    if(!message.contains(" ")) return;

                    OfflinePlayer target = Bukkit.getOfflinePlayer(name);
                    UUID targetUUID = target.getUniqueId();

                    WarpingLodestone lodestone = cache.getLodestonePermissionSetup().get(uuid).getOrigin();

                    if(targetUUID.toString().equals(lodestone.getOwner().toString())) {
                        player.sendMessage(MessagesYML.Errors.CANNOT_ADD_OWNER.withPrefix(player));
                        return;
                    }

                    if(lodestone.getWhitelist().contains(targetUUID)) {
                        player.sendMessage(MessagesYML.Errors.ALREADY_ADDED.withPrefix(player)
                                .replace("$name$", name));
                        return;
                    }

                    lodestone.whitelistAction(targetUUID.toString(), WarpingLodestone.Action.ADD);

                    player.sendMessage(MessagesYML.ADDED_PERM.withPrefix(player).replace("$name$", name));
                } else if(message.startsWith("-remove")) {
                    e.setCancelled(true);
                    String name = message.split(" ")[1];
                    if(!message.contains(" ")) return;

                    OfflinePlayer target = Bukkit.getOfflinePlayer(name);
                    UUID targetUUID = target.getUniqueId();

                    WarpingLodestone lodestone = cache.getLodestonePermissionSetup().get(uuid).getOrigin();

                    if(!lodestone.getWhitelist().contains(targetUUID)) {
                        player.sendMessage(MessagesYML.Errors.NOT_ADDED.withPrefix(player).replace("$name$", name));
                        return;
                    }

                    lodestone.whitelistAction(targetUUID.toString(), WarpingLodestone.Action.REMOVE);

                    player.sendMessage(MessagesYML.REMOVED_PERM.withPrefix(player).replace("$name$", name));
                } else if(message.equalsIgnoreCase("-list")) {
                    e.setCancelled(true);
                    WarpingLodestone lodestone = cache.getLodestonePermissionSetup().get(uuid).getOrigin();
                    if(lodestone.getWhitelist().isEmpty()) {
                        player.sendMessage(MessagesYML.LIST_EMPTY.withPrefix(player));
                        return;
                    }

                    StringBuilder sb = new StringBuilder();

                    for(int i = 0; i < lodestone.getWhitelist().size(); i++) {
                        String targetName = Bukkit.getOfflinePlayer(lodestone.getWhitelist().get(i)).getName();
                        sb.append(targetName);
                        if(i < lodestone.getWhitelist().size() - 1) sb.append(", ");
                    }

                    player.sendMessage(MessagesYML.LIST_FORMAT.withPrefix(player).replace("$list$", sb.toString()));
                }
            }
        });
    }
}

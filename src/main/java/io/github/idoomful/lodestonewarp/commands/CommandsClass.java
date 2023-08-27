package io.github.idoomful.lodestonewarp.commands;

import io.github.idoomful.bukkitutils.command.CommandSettings;
import io.github.idoomful.bukkitutils.command.ModularCommand;
import io.github.idoomful.bukkitutils.json.JsonLocation;
import io.github.idoomful.bukkitutils.statics.ItemBuilder;
import io.github.idoomful.bukkitutils.statics.TextUtils;
import io.github.idoomful.bukkitutils.statics.Utils;
import io.github.idoomful.lodestonewarp.DMain;
import io.github.idoomful.lodestonewarp.configuration.MessagesYML;
import io.github.idoomful.lodestonewarp.configuration.SettingsYML;
import io.github.idoomful.lodestonewarp.data.WarpingLodestone;
import io.github.idoomful.lodestonewarp.objects.LodestoneLink;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CommandsClass {
    public CommandsClass(DMain plugin) {
        final String pluginName = plugin.getDescription().getName();
        final String pluginNameLower = pluginName.toLowerCase();

        // Command initialization
        final CommandSettings settings = new CommandSettings(pluginNameLower)
                .setPermissionMessage(MessagesYML.Errors.NO_PERMISSION.withPrefix(null))
                .setAliases(Utils.Array.of("lodewarp", "lwarp"));

        new ModularCommand(plugin, settings, (sender, args) -> {
            Player arg = sender instanceof Player ? (Player) sender : null;

            if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
                MessagesYML.Lists.HELP.getStringList(arg).forEach(sender::sendMessage);
                return;
            }

            switch(args[0]) {
                case "adminwarp":
                    if(sender.hasPermission(pluginNameLower + ".command.adminwarp")) {
                        if(sender instanceof Player) {
                            Player player = (Player) sender;

                            if(plugin.getCache().getLinker().containsKey(player.getUniqueId())) {
                                player.sendMessage(MessagesYML.Errors.IS_LINKING.withPrefix(player));
                                return;
                            }

                            plugin.getCache().getLinker().put(player.getUniqueId(), new LodestoneLink(null));
                            player.sendMessage(MessagesYML.ADMIN_WARP_SETUP.withPrefix(player));
                        } else {
                            sender.sendMessage(MessagesYML.Errors.NOT_PLAYER.withPrefix(null));
                        }
                    } else {
                        sender.sendMessage(MessagesYML.Errors.NO_PERMISSION.withPrefix(arg));
                    }
                    break;
                case "fixglitched":
                    if(sender.hasPermission(pluginNameLower + ".command.fixglitched")) {
                        List<WarpingLodestone> lodestones = plugin.getCache().getLodestones();
                        try {
                            List<JsonLocation> lodestonesD = plugin.getSQLite()
                                    .queryMore("SELECT locations FROM players");
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }

                        String pattern = plugin.getConfigs().getFile("settings").getString("recipes.warping_lodestone.result");
                        Material check = Material.LODESTONE;

                        if(pattern == null) {
                            sender.sendMessage(MessagesYML.Errors.GIVE_ERROR.withPrefix(arg));
                            return;
                        } else check = ItemBuilder.build(pattern).getType();

                        List<WarpingLodestone> glitched = new ArrayList<>();

                        for(WarpingLodestone lodestone : new ArrayList<>(lodestones)) {
                            Location loc = lodestone.getLocation().getLocation();
                            if(loc.getBlock().getType() != check) {
                                glitched.add(lodestone);
                            }
                        }

                        for(WarpingLodestone glitch : glitched) glitch.remove();

                        if(glitched.size() == 0) sender.sendMessage(TextUtils.color("&aDidn't find any glitched lodestone in the database and cache"));
                        else sender.sendMessage(TextUtils.color("&aFound and fixed " + glitched.size() + " glitched lodestone(s)!"));
                    } else {
                        sender.sendMessage(MessagesYML.Errors.NO_PERMISSION.withPrefix(arg));
                    }
                    break;
                case "debugdatabase":
                    if(sender.hasPermission(pluginNameLower + ".command.debugdatabase")) {
                        if(sender instanceof Player) {
                            Player player = (Player) sender;
                            List<Location> lodestonesC = plugin.getCache().getLodestonesFor(player.getUniqueId());
                            String locationsJson = null;
                            try {
                                locationsJson = plugin.getSQLite()
                                        .queryOne("SELECT locations FROM players WHERE uuid=?", player.getUniqueId().toString());
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                            if(locationsJson == null) return;
                            List<JsonLocation> lodestonesD = JsonLocation.fromJSONList(locationsJson);

                            player.sendMessage("");
                            if(lodestonesC.size() != lodestonesD.size()) {
                                player.sendMessage(TextUtils.color("  &cCache and database are not in sync!"));
                                player.sendMessage(TextUtils.color("  &cContact the developer with a screenshot of this report"));

                                if(lodestonesC.size() < lodestonesD.size()) {
                                    player.sendMessage(TextUtils.color("  &c&lSome lodestones don't register in the cache"));
                                } else {
                                    player.sendMessage(TextUtils.color("  &c&lSome lodestones don't register in the database"));
                                }
                            } else {
                                player.sendMessage(TextUtils.color("  &aCache and database are in sync"));
                            }
                            player.sendMessage("");
                            player.sendMessage(TextUtils.color("  &nYour lodestones in the cache:"));
                            lodestonesC.forEach(loc -> player.sendMessage(TextUtils.color("  * " + new JsonLocation(loc).toString())));
                            player.sendMessage("");
                            player.sendMessage(TextUtils.color("  &nYour lodestones in the database:"));
                            lodestonesD.forEach(loc -> player.sendMessage(TextUtils.color("  * " + loc.toString())));
                            player.sendMessage("");
                        }
                    } else {
                        sender.sendMessage(MessagesYML.Errors.NO_PERMISSION.withPrefix(arg));
                    }
                    break;
                case "give":
                    // /lwarp give <player> <amount>
                    if(sender.hasPermission(pluginNameLower + ".command.give")) {
                        if(args.length < 2) {
                            MessagesYML.Lists.HELP.getStringList(arg).forEach(sender::sendMessage);
                            return;
                        }

                        Player target = Bukkit.getPlayer(args[1]);

                        if(target == null) {
                            sender.sendMessage(MessagesYML.Errors.PLAYER_NOT_VALID.withPrefix(arg).replace("$player$", args[1]));
                            return;
                        }

                        try {
                            int amount = 1;
                            if (args.length == 3) {
                                amount = Math.max(1, Math.min(Integer.parseInt(args[2]), 64));
                            }

                            String pattern = plugin.getConfigs().getFile("settings").getString("recipes.warping_lodestone.result");

                            if(pattern == null) {
                                sender.sendMessage(MessagesYML.Errors.GIVE_ERROR.withPrefix(arg));
                                return;
                            }

                            ItemStack item = ItemBuilder.build(pattern);
                            item.setAmount(amount);

                            target.getInventory().addItem(item);

                            sender.sendMessage(MessagesYML.GAVE_LODESTONE.withPrefix(arg)
                                    .replace("$amount$", amount + "")
                                    .replace("$player$", args[1])
                            );
                        } catch (NumberFormatException ne) {
                            sender.sendMessage(MessagesYML.Errors.NAN.withPrefix(arg).replace("$arg$", args[2]));
                        }
                    } else {
                        sender.sendMessage(MessagesYML.Errors.NO_PERMISSION.withPrefix(arg));
                    }
                    break;
                case "reload":
                    if(sender.hasPermission(pluginNameLower + ".command.reload")) {
                        plugin.getConfigs().reloadConfigs();
                        if(plugin.getConfigs().fileExists("messages")) MessagesYML.RELOAD.reload();
                        if(plugin.getConfigs().fileExists("settings")) SettingsYML._OPTIONS.reload();
                        plugin.getRecipeRegistrant().reload();
                        plugin.getEvents().restartIdleParticles(plugin);

                        sender.sendMessage(MessagesYML.RELOAD.withPrefix(arg));
                    } else {
                        sender.sendMessage(MessagesYML.Errors.NO_PERMISSION.withPrefix(arg));
                    }
                    break;
                case "version":
                case "ver":
                case "v":
                    if(sender.hasPermission(pluginNameLower + ".command.version")) {
                        sender.sendMessage(TextUtils.color("&8&l»&f " + pluginName + " version: &b" + plugin.getVersion()));
                    } else {
                        sender.sendMessage(MessagesYML.Errors.NO_PERMISSION.withPrefix(arg));
                    }
                    break;
                default:
                    MessagesYML.Lists.HELP.getStringList(arg).forEach(sender::sendMessage);
            }
        });
    }
}
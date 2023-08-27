package io.github.idoomful.lodestonewarp.configuration;

import io.github.idoomful.lodestonewarp.DMain;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import io.github.idoomful.bukkitutils.statics.TextUtils;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public enum MessagesYML {
    PREFIX("prefix"),
    RELOAD("reload"),
    GAVE_LODESTONE("gave-lodestone"),
    PLACED_LODESTONE("placed-lodestone"),
    BROKE_LODESTONE("broke-lodestone"),
    LINKING("linking"),
    REQUEST_NAME("request-name"),
    LINK_CANCEL("link-cancel"),
    LINKED("linked"),
    WARPING("warping"),
    WARP_FAILED("warp-failed"),
    UNSAFE_WARP("unsafe-warp"),
    UPDATE_STATUS("update-status"),
    LEFT_SETUP("left-setup"),
    ADDED_PERM("added-perm"),
    REMOVED_PERM("removed-perm"),
    LIST_EMPTY("list-empty"),
    LIST_FORMAT("list-format"),
    ASK_DELETE("ask-delete"),
    WARP_DELETED("warp-deleted"),
    ADMIN_WARP_SETUP("admin-warp-setup"),
    GLOBAL_WARP_CREATED("global-warp-created"),
    GLOBAL_WARP_TIP("global-warp-tip");

    String output;
    FileConfiguration messages;

    MessagesYML(String output) {
        messages = DMain.getInstance().getConfigs().getFile("messages");
        this.output = "messages." + output;
    }

    public String withPrefix(Player player) {
        String text = MessagesYML.PREFIX.color(player) + messages.getString(output);
        return TextUtils.placeholder(player, text);
    }

    public String color(Player player) {
        String text = messages.getString(output);
        return TextUtils.placeholder(player, text);
    }

    public void reload() {
        messages = DMain.getInstance().getConfigs().getFile("messages");
    }

    public enum Errors {
        NO_PERMISSION("no-permission"),
        PLAYER_NOT_VALID("player-not-valid"),
        NAN("nan"),
        GIVE_ERROR("give-error"),
        NOT_OWNS("not-owns"),
        SAME_LODESTONE("same-lodestone"),
        ALREADY_LINKED("already-linked"),
        NOT_STANDING("not-standing"),
        NOT_AVAILABLE("not-available"),
        NOT_ADDED("not-added"),
        ALREADY_ADDED("already-added"),
        CANNOT_ADD_OWNER("cannot-add-owner"),
        IS_PUBLIC("is-public"),
        PRIVATE_LODESTONE("private-lodestone"),
        NOT_PLAYER("not-player"),
        GLOBAL_WARP_EXISTS("global-warp-exists"),
        IS_LINKING("is-linking"),
        BANNED_NAME("banned-name");

        String output;
        FileConfiguration messages;

        Errors(String output) {
            messages = MessagesYML.PREFIX.messages;
            this.output = "errors." + output;
        }

        public String withPrefix(Player player) {
            String text = MessagesYML.PREFIX.color(player) + messages.getString(output);
            return TextUtils.placeholder(player, text);
        }
    }

    public enum Words {
        YES("yes-word"),
        NO("no-word"),
        PUBLIC("public"),
        PRIVATE("private"),
        COLORS("colors"),
        NO_COLORS("no-colors");

        String output;
        FileConfiguration messages;

        Words(String output) {
            messages = MessagesYML.PREFIX.messages;
            this.output = "words." + output;
        }

        public String color(Player player) {
            return TextUtils.placeholder(player, messages.getString(output));
        }
    }

    public enum Lists {
        HELP("help"),
        PERMISSION_SETUP("permission-setup");

        String output;
        FileConfiguration messages;

        Lists(String output) {
            messages = MessagesYML.PREFIX.messages;
            this.output = "lists." + output;
        }

        public List<String> getStringList(Player player) {
            return TextUtils.placeholder(player, messages.getStringList(output));
        }
    }

    public enum Events {
        PLACEHOLDERS("");

        String output;
        FileConfiguration messages;

        Events(String output) {
            messages = MessagesYML.PREFIX.messages;
            this.output = "events." + output;
        }

        public ArrayList<String> getPlaceholders() {
            return new ArrayList<>(messages.getConfigurationSection("events").getKeys(false));
        }

        public String getReplacement(String placeholder) {
            return messages.getString("events." + placeholder + ".replacement");
        }

        public String getClickType(String placeholder) {
            return messages.getString("events." + placeholder + ".click-event.type");
        }

        public String getClickText(String placeholder) {
            return messages.getString("events." + placeholder + ".click-event.text");
        }

        public String getHoverType(String placeholder) {
            return messages.getString("events." + placeholder + ".hover-event.type");
        }

        public String getHoverText(String placeholder) {
            return messages.getString("events." + placeholder + ".hover-event.text");
        }

        public boolean hasClickEvent(String placeholder) {
            return messages.getConfigurationSection("events." + placeholder + ".click-event") != null;
        }

        public boolean hasHoverEvent(String placeholder) {
            return messages.getConfigurationSection("events." + placeholder + ".hover-event") != null;
        }
    }
}

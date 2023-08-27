package io.github.idoomful.lodestonewarp.configuration;

import io.github.idoomful.lodestonewarp.DMain;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;
import java.util.List;

public enum SettingsYML {
    _OPTIONS("");

    String path;
    FileConfiguration settings;

    SettingsYML(String output) {
        settings = DMain.getInstance().getConfigs().getFile("settings");
        this.path = output;
    }

    public void reload() {
        settings = DMain.getInstance().getConfigs().getFile("settings");
    }

    public enum Options {
        /** String */ IDLE_PARTICLES_PUBLIC("idle-particles.public"),
        /** String */ IDLE_PARTICLES_PRIVATE("idle-particles.private"),
        /** String */ IDLE_PARTICLES_GLOBAL("idle-particles.global"),
        /** Integer */ IDLE_PARTICLES_EVERY("idle-particles-every"),
        /** String */ PAGE_TURN_SOUND("page-turn-sound"),
        /** String */ MENU_NAVIGATE_SOUND("menu-navigate-sound"),
        /** String */ SETTINGS_ICON_SOUND("settings-icon-sound"),
        /** Boolean */ WARP_ENTITIES("warp-entities"),
        /** Double */ WARP_ENTITIES_RADIUS("warp-entities-radius"),
        /** Boolean */ MUTUAL_LINK("mutual-link");

        public String path;
        public FileConfiguration settings;

        Options(String path) {
            this.path = "options." + path;
            this.settings = SettingsYML._OPTIONS.settings;
        }

        public <T> T get(Class<T> type) {
            return settings.getObject(path, type);
        }
        public double getDouble() {
            return settings.getDouble(path);
        }
    }

    public enum WarpName {
        /** Boolean */ COLORS("colors"),
        /** Boolean */ HEX_COLORS("hex-colors"),
        /** String List */ BLACKLIST("blacklist");

        public String path;
        public FileConfiguration settings;

        WarpName(String path) {
            this.path = "warp-name." + path;
            this.settings = SettingsYML._OPTIONS.settings;
        }

        public <T> T get(Class<T> type) {
            return settings.getObject(path, type);
        }
        public List<String> getList() {
            return settings.getStringList(path);
        }
    }

    public enum WarpingEffects {
        /** String */ PARTICLE("particle"),
        /** Integer */ AMOUNT("amount"),
        /** Integer */ REPEAT("repeat"),
        /** Integer */ CYCLE("cycle"),
        /** Double */ MULTIPLIER("multiplier"),
        /** Double */ Y_MULTIPLIER("y-multiplier"),
        /** Double */ Y_OFFSET("y-offset"),
        /** Integer */ TIMING("timing"),
        /** String */ CHARGING_SOUND("charging-sound"),
        /** String */ WARP_SOUND("warp-sound"),
        /** String */ WARP_FAIL_SOUND("warp-fail-sound"),
        /** String */ WARP_FAIL_PARTICLE("warp-fail-particle"),
        /** String */ WARP_PARTICLE("warp-particle");

        public String path;
        public FileConfiguration settings;

        WarpingEffects(String path) {
            this.path = "warping-effects." + path;
            this.settings = SettingsYML._OPTIONS.settings;
        }

        public <T> T get(Class<T> type) {
            return settings.getObject(path, type);
        }
        public double getDouble() {
            return settings.getDouble(path);
        }
    }

    public enum WarpsList {
        /** String */ PUBLIC_TITLE("public-title"),
        /** String */ PRIVATE_TITLE("private-title"),
        /** String */ GLOBAL_TITLE("global-title"),
        /** Integer */ ROWS("rows"),
        /** Integer */ ITEMS_PER_PAGE("items-per-page"),
        /** Integer[] */ SKIPPING_POINTS("skipping-points"),
        /** Integer[] */ NEXT_PAGE_BUTTON_SLOTS("next-page-button-slots"),
        /** Integer[] */ PREVIOUS_PAGE_BUTTON_SLOTS("previous-page-button-slots"),
        /** String */ NEXT_PAGE_BUTTON("next-page-button"),
        /** String */ PREVIOUS_PAGE_BUTTON("previous-page-button"),
        /** String */ EMPTY_BUTTON("empty-button"),
        /** String */ WARP_ITEM("warp-item"),
        /** String */ GLOBAL_WARP_ITEM("global-warp-item"),
        /** String */ ADMIN_GLOBAL_WARP_ITEM("admin-global-warp-item");

        public String path;
        public FileConfiguration settings;

        WarpsList(String path) {
            this.path = "inventories.warps-list." + path;
            this.settings = SettingsYML._OPTIONS.settings;
        }

        public <T> T get(Class<T> type) {
            return settings.getObject(path, type);
        }
        public int[] getIntList() {
            return settings.getIntegerList(path).stream().mapToInt(i -> i).toArray();
        }
    }
}

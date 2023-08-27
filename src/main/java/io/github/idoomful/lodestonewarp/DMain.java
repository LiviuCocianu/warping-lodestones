package io.github.idoomful.lodestonewarp;

import io.github.idoomful.bukkitutils.object.ConfigManager;
import io.github.idoomful.bukkitutils.object.RecipeRegistrant;
import io.github.idoomful.bukkitutils.object.SQLiteDatabase;
import io.github.idoomful.lodestonewarp.commands.CommandsClass;
import io.github.idoomful.lodestonewarp.data.Cache;
import io.github.idoomful.lodestonewarp.events.EventsClass;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.sql.SQLException;

public final class DMain extends JavaPlugin {
    private final String version = getDescription().getVersion();

    private static DMain plugin;
    private ConfigManager<DMain> cm;
    private EventsClass events;
    private Cache cache;
    private RecipeRegistrant rr;
    private SQLiteDatabase sqlite;

    @Override
    public void onEnable() {
        plugin = this;
        registerSQLite();
        registerMechanics();
    }

    @Override
    public void onDisable() {
        if(cache != null) {
            cache.getPlayersMap().clear();
            cache.getLodestones().clear();
            cache.getGlobalWarps().clear();
        }
    }

    private void registerMechanics() {
        cm = new ConfigManager<>(this);
        cm.addConfigurationFile("messages").addConfigurationFile("settings");
        cache = new Cache(this);

        new CommandsClass(this);
        events = new EventsClass(this);
        rr = new RecipeRegistrant(this, cm.getFile("settings"));
    }

    private void registerSQLite() {
        boolean madeDir = getDataFolder().mkdirs();

        if(madeDir || getDataFolder().exists()) {
            sqlite = new SQLiteDatabase(this);

            try {
                sqlite.setupConnection("data");

                if(sqlite.isConnectionActive()) {
                    sqlite.setupTable("players", "uuid VARCHAR(36), locations JSON");
                    sqlite.setupTable("lodestones", "owner VARCHAR(36), location JSON, isPublic BOOL, whitelist JSON, publicWarps JSON, privateWarps JSON");
                    sqlite.setupTable("warps", "owner VARCHAR(36), name TINYTEXT, location JSON");
                } else {
                    getLogger().warning("Couldn't connect to SQLite database..");
                }
            } catch (SQLException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static DMain getInstance() {
        return plugin;
    }
    public String getVersion() {
        return version;
    }
    public ConfigManager<DMain> getConfigs() {
        return cm;
    }
    public EventsClass getEvents() {
        return events;
    }
    public Cache getCache() {
        return cache;
    }
    public RecipeRegistrant getRecipeRegistrant() {
        return rr;
    }

    public SQLiteDatabase getSQLite() {
        return sqlite;
    }
}

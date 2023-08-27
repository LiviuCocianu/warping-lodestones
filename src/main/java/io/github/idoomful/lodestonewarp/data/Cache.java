package io.github.idoomful.lodestonewarp.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.idoomful.bukkitutils.json.JSONable;
import io.github.idoomful.bukkitutils.json.JsonLocation;
import io.github.idoomful.bukkitutils.statics.TextUtils;
import io.github.idoomful.lodestonewarp.DMain;
import io.github.idoomful.lodestonewarp.gui.MyGUI;
import io.github.idoomful.lodestonewarp.objects.LodestoneLink;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class Cache {
    private final DMain main;

    private final List<WarpingLodestone> lodestones = new ArrayList<>();
    private final HashMap<UUID, List<Location>> players = new HashMap<>();
    private final List<LodestoneWarp> globalWarps = new ArrayList<>();

    private final HashMap<UUID, MyGUI> inventories = new HashMap<>();
    private final HashMap<UUID, LodestoneLink> linker = new HashMap<>();
    private final HashMap<UUID, LodestoneLink> lodestonePermissionSetup = new HashMap<>();
    private final List<UUID> safetyList = new ArrayList<>();

    public Cache(DMain main) {
        this.main = main;

        setupPlayers();
        setupLodestones();
        setupGlobalWarps();
    }

    public List<WarpingLodestone> getLodestones() {
        return lodestones;
    }

    public WarpingLodestone getLodestoneAt(Location loc) {
        return getLodestoneAt(new JsonLocation(loc));
    }

    public WarpingLodestone getLodestoneAt(JsonLocation loc) {
        for(WarpingLodestone lodestone : lodestones) {
            if(lodestone.getLocation().toString().equals(loc.toString())) return lodestone;
        }

        return null;
    }

    public HashMap<UUID, List<Location>> getPlayersMap() {
        return players;
    }

    public List<Location> getLodestonesFor(UUID player) {
        if(!players.containsKey(player)) players.put(player, new ArrayList<>());
        return players.get(player);
    }

    public List<LodestoneWarp> getGlobalWarps() {
        return globalWarps;
    }

    public HashMap<UUID, MyGUI> getInventories() {
        return inventories;
    }

    /**
     * Check if player has one of the plugin's UIs opened
     */
    public boolean hasInventory(Player player) {
        return inventories.containsKey(player.getUniqueId());
    }

    public HashMap<UUID, LodestoneLink> getLinker() {
        return linker;
    }

    public HashMap<UUID, LodestoneLink> getLodestonePermissionSetup() {
        return lodestonePermissionSetup;
    }

    public List<UUID> getSafetyList() {
        return safetyList;
    }

    private void setupLodestones() {
        try {
            List<String> queriedUUIDs = null;

            queriedUUIDs = main.getSQLite().queryMore("SELECT owner FROM `lodestones`");
            List<String> queriedLocsJson = main.getSQLite().queryMore("SELECT location FROM `lodestones`");
            List<Integer> queriedStatus = main.getSQLite().queryMore("SELECT isPublic FROM `lodestones`");
            List<String> queriedWLJson = main.getSQLite().queryMore("SELECT whitelist FROM `lodestones`");
            List<String> queriedPuWJson = main.getSQLite().queryMore("SELECT publicWarps FROM `lodestones`");
            List<String> queriedPrWJson = main.getSQLite().queryMore("SELECT privateWarps FROM `lodestones`");

            if(queriedUUIDs.isEmpty()) return;

            ArrayList<JsonLocation> locations = new ArrayList<>();
            queriedLocsJson.forEach(locationJson -> {
                locations.add((JsonLocation) JSONable.fromJSON(locationJson, JsonLocation.class));
            });

            ArrayList<ArrayList<UUID>> allWhitelists = new ArrayList<>();
            for(int i = 0; i < queriedPuWJson.size(); i++) {
                ArrayList<String> whitelist = new Gson().fromJson(queriedWLJson.get(i), new TypeToken<ArrayList<String>>(){}.getType());
                allWhitelists.add(new ArrayList<>(whitelist.stream().map(UUID::fromString).collect(Collectors.toList())));
            }

            ArrayList<ArrayList<LodestoneWarp>> allPublicWarps = new ArrayList<>();
            for(int i = 0; i < queriedPuWJson.size(); i++) {
                ArrayList<LodestoneWarp> warpLocations = JsonLodestoneWarp.fromJsonList(queriedPuWJson.get(i));
                allPublicWarps.add(warpLocations);
            }

            ArrayList<ArrayList<LodestoneWarp>> allPrivateWarps = new ArrayList<>();
            for(int i = 0; i < queriedPrWJson.size(); i++) {
                ArrayList<LodestoneWarp> warpLocations = JsonLodestoneWarp.fromJsonList(queriedPrWJson.get(i));
                allPrivateWarps.add(warpLocations);
            }

            for(int i = 0; i < queriedUUIDs.size(); i++) {
                lodestones.add(
                        new WarpingLodestone(
                                UUID.fromString(queriedUUIDs.get(i)),
                                locations.get(i),
                                queriedStatus.get(i) > 0,
                                allWhitelists.get(i),
                                allPublicWarps.get(i),
                                allPrivateWarps.get(i)
                        )
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void setupPlayers() {
        try {
            List<String> queriedUUIDJson = null;

            queriedUUIDJson = main.getSQLite().queryMore("SELECT uuid FROM `players`");
            List<String> queriedLocsJson = main.getSQLite().queryMore("SELECT locations FROM `players`");

            if(queriedUUIDJson.isEmpty()) return;

            ArrayList<ArrayList<Location>> locations = new ArrayList<>();
            queriedLocsJson.forEach(locationsJson -> {
                ArrayList<Location> processed = new ArrayList<>();
                JsonLocation.fromJSONList(locationsJson).forEach(jsonLoc -> processed.add(jsonLoc.getLocation()));
                locations.add(processed);
            });

            for(int i = 0; i < queriedUUIDJson.size(); i++) {
                players.put(UUID.fromString(queriedUUIDJson.get(i)), locations.get(i));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void setupGlobalWarps() {
        try {
            List<String> queriedUUIDs = null;

            queriedUUIDs = main.getSQLite().queryMore("SELECT owner FROM `warps`");
            List<String> queriedNames = main.getSQLite().queryMore("SELECT name FROM `warps`");
            List<String> queriedLocsJson = main.getSQLite().queryMore("SELECT location FROM `warps`");

            if(queriedUUIDs.isEmpty()) return;

            ArrayList<JsonLocation> locations = new ArrayList<>();
            queriedLocsJson.forEach(locationJson -> {
                locations.add((JsonLocation) JSONable.fromJSON(locationJson, JsonLocation.class));
            });

            for(int i = 0; i < queriedUUIDs.size(); i++) {
                globalWarps.add(new LodestoneWarp(UUID.fromString(queriedUUIDs.get(i)),
                        TextUtils.color(queriedNames.get(i)),
                        new JsonLocation(locations.get(i).getLocation())
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

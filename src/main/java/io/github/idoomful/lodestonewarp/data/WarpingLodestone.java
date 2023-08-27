package io.github.idoomful.lodestonewarp.data;

import com.google.common.util.concurrent.AtomicDouble;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.idoomful.bukkitutils.json.JsonLocation;
import io.github.idoomful.bukkitutils.statics.BeautifyUtils;
import io.github.idoomful.bukkitutils.statics.TextUtils;
import io.github.idoomful.lodestonewarp.DMain;
import io.github.idoomful.lodestonewarp.configuration.MessagesYML;
import io.github.idoomful.lodestonewarp.configuration.SettingsYML;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class WarpingLodestone {
    public enum Action {
        ADD, REMOVE
    }

    private final UUID owner;
    private final JsonLocation location;
    private boolean isPublic;
    private final List<UUID> whitelist;
    private final List<LodestoneWarp> publicWarps;
    private final List<LodestoneWarp> privateWarps;

    private final List<UUID> isWarping = new ArrayList<>();

    public WarpingLodestone(UUID owner, JsonLocation location) {
        this.owner = owner;
        this.location = location;
        this.isPublic = true;
        this.whitelist = new ArrayList<>();
        this.publicWarps = new ArrayList<>();
        this.privateWarps = new ArrayList<>();
    }

    public WarpingLodestone(
            UUID owner,
            JsonLocation location,
            boolean isPublic,
            List<UUID> whitelist,
            List<LodestoneWarp> publicWarps,
            List<LodestoneWarp> privateWarps
    ) {
        this.owner = owner;
        this.location = location;
        this.isPublic = isPublic;
        this.whitelist = whitelist;
        this.publicWarps = publicWarps;
        this.privateWarps = privateWarps;
    }

    public UUID getOwner() {
        return owner;
    }
    public JsonLocation getLocation() {
        return location;
    }
    public boolean isPublic() {
        return isPublic;
    }
    public List<UUID> getWhitelist() {
        return whitelist;
    }
    public List<LodestoneWarp> getPublicWarps() {
        return publicWarps;
    }
    public List<LodestoneWarp> getPrivateWarps() {
        return privateWarps;
    }

    public boolean isGlobal() {
        for(LodestoneWarp globalWarp : DMain.getInstance().getCache().getGlobalWarps()) {
            if(getWarpLocation().toString().equals(globalWarp.getWarpTo().toString())) return true;
        }

        return false;
    }

    /**
     * Checks if the specified player is currently warping
     * with this lodestone.
     */
    public boolean isWarping(Player player) {
        return isWarping.contains(player.getUniqueId());
    }

    /**
     * Gets a list of all players who are currently warping
     * with this lodestone.
     */
    public List<UUID> getWhoIsWarping() {
        return isWarping;
    }

    public void setStatus(boolean isPublic) {
        try {
            String exist = DMain.getInstance().getSQLite().queryOne("SELECT publicWarps FROM lodestones WHERE location=?", getLocation().toJSON());
            final Gson gson = new Gson();

            // Update the database
            if(exist == null) {
                DMain.getInstance().getSQLite().execute("INSERT INTO lodestones VALUES (?,?,?,?,?,?)",
                        owner.toString(),
                        getLocation().toJSON(),
                        isPublic,
                        gson.toJson(new ArrayList<>()),
                        gson.toJson(new ArrayList<>()),
                        gson.toJson(new ArrayList<>())
                );
            } else {
                DMain.getInstance().getSQLite().execute("UPDATE lodestones SET isPublic=? WHERE location=?",
                        isPublic,
                        getLocation().toJSON()
                );
            }

            // Update the cache
            DMain.getInstance().getCache().getLodestones().removeIf(wl -> wl.getLocation().toString().equals(getLocation().toString()));
            this.isPublic = isPublic;
            DMain.getInstance().getCache().getLodestones().add(this);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Add this lodestone to the database and cache it for later use
     */
    public void add() {
        try {
            final Gson gson = new Gson();

            // Add it to the lodestones table
            DMain.getInstance().getSQLite().execute("INSERT INTO lodestones VALUES (?,?,?,?,?,?)",
                    owner.toString(),
                    getLocation().toJSON(),
                    true,
                    gson.toJson(new ArrayList<>()),
                    gson.toJson(new ArrayList<>()),
                    gson.toJson(new ArrayList<>())
            );

            // Add its location to the list of lodestones the player owns
            String locationsJson = DMain.getInstance().getSQLite().queryOne("SELECT locations FROM players WHERE uuid=?", owner.toString());
            ArrayList<JsonLocation> locations;

            if(locationsJson == null) locations = new ArrayList<>();
            else locations = JsonLocation.fromJSONList(locationsJson);

            locations.add(getLocation());

            if(locationsJson == null) {
                DMain.getInstance().getSQLite().execute("INSERT INTO players VALUES (?,?)",
                        owner.toString(),
                        JsonLocation.toJsonList(locations)
                );
            } else {
                DMain.getInstance().getSQLite().execute("UPDATE players SET locations=? WHERE uuid=?",
                        JsonLocation.toJsonList(locations),
                        owner.toString()
                );
            }

            // Cache it as well
            DMain.getInstance().getCache().getLodestones().add(this);
            DMain.getInstance().getCache().getLodestonesFor(owner).add(getLocation().getLocation());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Remove this lodestone from the database and cache
     */
    public void remove() {
        try {
            // Get locations from database
            String playerLocationsJson = DMain.getInstance().getSQLite().queryOne("SELECT locations FROM players WHERE uuid=?",
                    owner.toString());
            ArrayList<JsonLocation> playerLocations = new ArrayList<>();

            // If there's no list, insert an empty one for later use
            if(playerLocationsJson == null) {
                DMain.getInstance().getSQLite().execute("INSERT INTO players VALUES (?,?)",
                        owner.toString(),
                        JsonLocation.toJsonList(playerLocations)
                );
            }
            // .. otherwise, convert from JSON, remove the location and update with the new list in the database
            else {
                playerLocations = JsonLocation.fromJSONList(playerLocationsJson);
                playerLocations.removeIf(jsonLocPlayer -> getLocation().toString().equals(jsonLocPlayer.toString()));

                DMain.getInstance().getSQLite().execute("UPDATE players SET locations=? WHERE uuid=?",
                        JsonLocation.toJsonList(playerLocations),
                        owner.toString()
                );
            }

            // Remove from lodestones table as well
            DMain.getInstance().getSQLite().execute("DELETE FROM lodestones WHERE location=?", getLocation().toJSON());

            // .. and from cache
            DMain.getInstance().getCache().getLodestones().removeIf(wl -> wl.getLocation().toString().equals(getLocation().toString()));
            DMain.getInstance().getCache().getLodestonesFor(owner).removeIf(wlLoc -> wlLoc.equals(getLocation().getLocation()));

            // Remove any warps to this lodestone from all lodestones
            for(WarpingLodestone lodestone : new ArrayList<>(DMain.getInstance().getCache().getLodestones())) {
                for(LodestoneWarp publicWarp : new ArrayList<>(lodestone.getPublicWarps())) {
                    if(publicWarp.getWarpTo().toString().equals(getWarpLocation().toString())) {
                        lodestone.warpAction(publicWarp, Action.REMOVE, LodestoneWarp.Scope.PUBLIC);
                    }
                }

                for(LodestoneWarp privateWarp : new ArrayList<>(lodestone.getPrivateWarps())) {
                    if(privateWarp.getWarpTo().toString().equals(getWarpLocation().toString())) {
                        lodestone.warpAction(privateWarp, Action.REMOVE, LodestoneWarp.Scope.PRIVATE);
                    }
                }

                for(LodestoneWarp globalWarp : new ArrayList<>(DMain.getInstance().getCache().getGlobalWarps())) {
                    if(globalWarp.getWarpTo().toString().equals(getWarpLocation().toString())) {
                        lodestone.warpAction(globalWarp, Action.REMOVE, LodestoneWarp.Scope.GLOBAL);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a LodestoneWarp object for the given name
     * @param name The name of the warp
     */
    public LodestoneWarp getWarp(String name) {
        return new LodestoneWarp(owner, name, getWarpLocation());
    }

    /**
     * Get the location where the player will be teleported to when warping to this lodestone
     */
    public JsonLocation getWarpLocation() {
        return new JsonLocation(getLocation().getLocation().clone().add(0.5, 1, 0.5));
    }

    /**
     * Perform an action to this lodestone's warp lists
     * @param warp The warp to add
     * @param action Whether to add or to remove
     * @param scope The scope of the warp
     */
    public void warpAction(LodestoneWarp warp, Action action, LodestoneWarp.Scope scope) {
        try {
            if(scope == LodestoneWarp.Scope.GLOBAL) {
                DMain.getInstance().getSQLite().execute("DELETE FROM warps WHERE location=?", warp.getWarpTo().toJSON());
                DMain.getInstance().getCache().getGlobalWarps().removeIf(wa -> wa.getWarpTo().toString().equals(warp.getWarpTo().toString()));
                return;
            }

            Gson gson = new Gson();

            // Update the database content
            String warpsJson = DMain.getInstance().getSQLite().queryOne("SELECT " + scope.name().toLowerCase() + "Warps FROM lodestones WHERE location=?",
                    getLocation().toJSON());
            List<JsonLodestoneWarp> warps;

            if(warpsJson == null) {
                warps = new ArrayList<>();
                warps.add(new JsonLodestoneWarp(warp));

                DMain.getInstance().getSQLite().execute("INSERT INTO lodestones VALUES (?,?,?,?,?,?)",
                        owner.toString(),
                        getLocation().toJSON(),
                        true,
                        gson.toJson(new ArrayList<>()),
                        scope == LodestoneWarp.Scope.PUBLIC ? JsonLodestoneWarp.toJsonListStore(warps) : gson.toJson(new ArrayList<>()),
                        scope == LodestoneWarp.Scope.PRIVATE ? JsonLodestoneWarp.toJsonListStore(warps) : gson.toJson(new ArrayList<>())
                );
            } else {
                warps = JsonLodestoneWarp.fromJsonList(warpsJson).stream().map(JsonLodestoneWarp::new).collect(Collectors.toList());
                if(action == Action.ADD) warps.add(new JsonLodestoneWarp(warp));
                else if(action == Action.REMOVE) warps.removeIf(warpLoc ->
                        warpLoc.getWarpTo().toString().equals(new JsonLodestoneWarp(warp).getWarpTo().toString())
                );

                DMain.getInstance().getSQLite().execute("UPDATE lodestones SET " + scope.name().toLowerCase() + "Warps=? WHERE location=?",
                        JsonLodestoneWarp.toJsonListStore(warps),
                        getLocation().toJSON()
                );
            }

            // Update the cache
            DMain.getInstance().getCache().getLodestones().removeIf(wl -> wl.getLocation().toString().equals(getLocation().toString()));

            if(action == Action.ADD) {
                if(scope == LodestoneWarp.Scope.PUBLIC) getPublicWarps().add(warp);
                else if(scope == LodestoneWarp.Scope.PRIVATE) getPrivateWarps().add(warp);
            } else if(action == Action.REMOVE) {
                if(scope == LodestoneWarp.Scope.PUBLIC) getPublicWarps().removeIf(warpLoc -> warpLoc.toString().equals(warp.toString()));
                else if(scope == LodestoneWarp.Scope.PRIVATE) getPrivateWarps().removeIf(warpLoc -> warpLoc.toString().equals(warp.toString()));
            }

            DMain.getInstance().getCache().getLodestones().add(this);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void whitelistAction(String uuid, Action action) {
        try {
            Gson gson = new Gson();

            // Update the database content
            String whitelistJson = DMain.getInstance().getSQLite().queryOne("SELECT whitelist FROM lodestones WHERE location=?",
                    getLocation().toJSON());
            List<String> whitelist;

            if(whitelistJson == null) {
                DMain.getInstance().getSQLite().execute("INSERT INTO lodestones VALUES (?,?,?,?,?,?)",
                        owner.toString(),
                        getLocation().toJSON(),
                        true,
                        gson.toJson(new ArrayList<>()),
                        gson.toJson(new ArrayList<>()),
                        gson.toJson(new ArrayList<>())
                );
            } else {
                whitelist = gson.fromJson(whitelistJson, new TypeToken<ArrayList<String>>(){}.getType());

                if(action == Action.ADD) whitelist.add(uuid);
                else if(action == Action.REMOVE) whitelist.removeIf(uuidDB -> uuidDB.equals(uuid));

                DMain.getInstance().getSQLite().execute("UPDATE lodestones SET whitelist=? WHERE location=?",
                        gson.toJson(whitelist),
                        getLocation().toJSON()
                );
            }

            // Update the cache
            DMain.getInstance().getCache().getLodestones().removeIf(wl -> wl.getLocation().toString().equals(getLocation().toString()));

            if(action == Action.ADD) {
                getWhitelist().add(UUID.fromString(uuid));
            } else if(action == Action.REMOVE) {
                getWhitelist().removeIf(uuidC -> uuidC.toString().equals(uuid));
            }

            DMain.getInstance().getCache().getLodestones().add(this);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Link this lodestone to another lodestone
     *
     * @param lodestone The lodestone to link with
     * @param warpName The name of the warp
     * @param scope The scope of the warp
     */
    public void link(WarpingLodestone lodestone, String warpName, LodestoneWarp.Scope scope) {
        if(scope == LodestoneWarp.Scope.GLOBAL) return;
        warpAction(lodestone.getWarp(warpName), Action.ADD, scope);

        if(SettingsYML.Options.MUTUAL_LINK.get(Boolean.class)) {
            lodestone.warpAction(getWarp(warpName), Action.ADD, isPublic() ? LodestoneWarp.Scope.PUBLIC : LodestoneWarp.Scope.PRIVATE);
        }
    }

    /**
     * Warps a player from this lodestone to one of the available warps
     * @param player The player to warp
     * @param to The lodestone to warp to
     */
    public void warp(Player player, String name, WarpingLodestone to) {
        final int repeat = SettingsYML.WarpingEffects.REPEAT.get(Integer.class);
        final int timing = SettingsYML.WarpingEffects.TIMING.get(Integer.class);
        final int cycle = SettingsYML.WarpingEffects.CYCLE.get(Integer.class);

        final double yoffset = SettingsYML.WarpingEffects.Y_OFFSET.getDouble();
        final double ymult = SettingsYML.WarpingEffects.Y_MULTIPLIER.getDouble();

        String[] soundArgs = SettingsYML.WarpingEffects.CHARGING_SOUND.get(String.class).split(" ");
        AtomicDouble incrPitch = new AtomicDouble(Double.parseDouble(soundArgs[2]));
        double mult = 1.0 / (repeat * cycle);

        AtomicInteger count = new AtomicInteger();
        AtomicInteger ID = new AtomicInteger();

        isWarping.add(player.getUniqueId());
        player.sendMessage(MessagesYML.WARPING.withPrefix(player)
                .replace("$name$", TextUtils.color(name))
                .replace("$seconds$", ((int) Math.ceil((timing * repeat * cycle) / 20.0)) + "")
        );

        ID.set(Bukkit.getScheduler().scheduleSyncRepeatingTask(DMain.getInstance(), () -> {
            if(count.get() == cycle) {
                BeautifyUtils.playSoundRadiusSimple(DMain.getInstance(),
                        getLocation().getLocation(),
                        SettingsYML.WarpingEffects.WARP_SOUND.get(String.class), 8
                );

                BeautifyUtils.displayTexturedParticle(
                        getWarpLocation().getLocation(),
                        Bukkit.getOnlinePlayers(),
                        SettingsYML.WarpingEffects.WARP_PARTICLE.get(String.class)
                );

                Location modified = to.getWarpLocation().getLocation();
                modified.setYaw(player.getLocation().getYaw());
                modified.setPitch(player.getLocation().getPitch());

                if(SettingsYML.Options.WARP_ENTITIES.get(Boolean.class)) {
                    double r = Math.max(0, SettingsYML.Options.WARP_ENTITIES_RADIUS.getDouble() - 0.5);
                    getWarpLocation().getLocation().getWorld().getNearbyEntities(getWarpLocation().getLocation(), r, r, r, en ->
                            !(en instanceof Player) && en instanceof LivingEntity && !(en instanceof ArmorStand)
                    ).forEach(len -> len.teleport(modified));
                }

                player.teleport(modified);
                isWarping.remove(player.getUniqueId());

                BeautifyUtils.displayTexturedParticle(
                        to.getWarpLocation().getLocation(),
                        Bukkit.getOnlinePlayers(),
                        SettingsYML.WarpingEffects.WARP_PARTICLE.get(String.class)
                );

                Bukkit.getScheduler().cancelTask(ID.get());
            } else {
                if(failedWarpCheck(player, ID.get())) return;

                for(int i = 0; i < repeat; i++) {
                    int finalI = i;

                    Bukkit.getScheduler().scheduleSyncDelayedTask(DMain.getInstance(), () -> {
                        StringBuilder pattern = new StringBuilder();
                        for(int j = 0; j < soundArgs.length; j++) {
                            if(j == 2) pattern.append(incrPitch.get());
                            else pattern.append(soundArgs[j]);

                            if(j < soundArgs.length - 1) pattern.append(" ");
                        }

                        BeautifyUtils.playSoundRadiusSimple(DMain.getInstance(),
                                getLocation().getLocation(),
                                pattern.toString(), 8
                        );

                        incrPitch.addAndGet(mult);

                        displayCircle(
                                finalI * SettingsYML.WarpingEffects.MULTIPLIER.getDouble(),
                                (getLocation().getY() + yoffset) + (finalI * ymult),
                                SettingsYML.WarpingEffects.AMOUNT.get(Integer.class)
                        );
                    }, timing * i);
                }

                count.incrementAndGet();
            }
        }, 0, repeat * timing));
    }

    public boolean isOnLodestone(Player player) {
        Location belowPlayer = player.getLocation().getBlock().getLocation().subtract(0, 1, 0);

        return getLocation().getLocation().toString().equals(belowPlayer.toString())
                && belowPlayer.getBlock().getType() == Material.LODESTONE;
    }

    private boolean failedWarpCheck(Player player, int scheduleID) {
        if(!isOnLodestone(player)) {
            BeautifyUtils.playSoundRadiusSimple(DMain.getInstance(),
                    getLocation().getLocation(),
                    SettingsYML.WarpingEffects.WARP_FAIL_SOUND.get(String.class), 8
            );

            BeautifyUtils.displayTexturedParticle(
                    getWarpLocation().getLocation(),
                    Bukkit.getOnlinePlayers(),
                    SettingsYML.WarpingEffects.WARP_FAIL_PARTICLE.get(String.class)
            );

            isWarping.remove(player.getUniqueId());
            player.sendMessage(MessagesYML.WARP_FAILED.withPrefix(player));
            Bukkit.getScheduler().cancelTask(scheduleID);
            return true;
        }
        return false;
    }

    private void displayCircle(double radius, double y, int amount) {
        Location center = getWarpLocation().getLocation();

        double increment = (2 * Math.PI) / amount;
        ArrayList<Location> locations = new ArrayList<>();

        for(int i = 0; i < amount; i++) {
            double angle = i * increment;
            double x = center.getX() + (radius * Math.cos(angle));
            double z = center.getZ() + (radius * Math.sin(angle));
            locations.add(new Location(Bukkit.getWorld(getLocation().getWorld()), x, y, z));
        }

        for(Location loc : locations) {
            BeautifyUtils.displayTexturedParticle(loc, Bukkit.getOnlinePlayers(), SettingsYML.WarpingEffects.PARTICLE.get(String.class));
        }
    }

    public static boolean isLodestone(Location loc) {
        List<WarpingLodestone> lodestones = DMain.getInstance().getCache().getLodestones();

        for (WarpingLodestone lodestone : lodestones) {
            JsonLocation jsonLoc = lodestone.getLocation();
            if (loc.equals(jsonLoc.getLocation())) return true;
        }

        return false;
    }

    public static boolean ownsLodestone(Player player, Location loc) {
        List<WarpingLodestone> lodestones = DMain.getInstance().getCache().getLodestones();

        if(player.hasPermission("lodestonewarp.bypass")) return true;

        for (WarpingLodestone lodestone : lodestones) {
            UUID owner = lodestone.getOwner();

            if (isLodestone(loc)
                    && lodestone.getLocation().getLocation().toString().equals(loc.toString())
                    && player.getUniqueId().toString().equals(owner.toString())
            ) return true;
        }

        return false;
    }
}

package io.github.idoomful.lodestonewarp.data;

import com.google.gson.Gson;
import io.github.idoomful.bukkitutils.json.JsonLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.UUID;

public class LodestoneWarp {
    public enum Scope {
        PUBLIC, PRIVATE, GLOBAL
    }

    private final UUID owner;
    private final String name;
    private final JsonLocation warpTo;

    public LodestoneWarp(UUID owner, String name, JsonLocation warpTo) {
        this.owner = owner;
        this.name = name;
        this.warpTo = warpTo;
    }

    public UUID getOwner() {
        return owner;
    }
    public String getName() {
        return name;
    }
    public JsonLocation getWarpTo() {
        return warpTo;
    }

    public boolean isSafe() {
        final World w = Bukkit.getWorld(warpTo.getWorld());
        final Location loc = warpTo.getLocation().clone();

        boolean solid = w.getBlockAt(loc).getType().isOccluding() || w.getBlockAt(loc.add(0, 1, 0)).getType().isOccluding();
        loc.subtract(0, 1, 0);

        return !solid && !hasBlock(w, loc.clone(), Material.LAVA) && !hasBlock(w, loc.clone(), Material.FIRE);
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    private boolean hasBlock(World world, Location loc, Material type) {
        return world.getBlockAt(loc).getType().toString().equals(type.toString())
                || world.getBlockAt(loc.add(0, 1, 0)).getType().toString().equals(type.toString());
    }
}

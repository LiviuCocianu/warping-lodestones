package io.github.idoomful.lodestonewarp.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.idoomful.bukkitutils.json.JSONable;
import io.github.idoomful.bukkitutils.json.JsonLocation;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class JsonLodestoneWarp {
    public enum Scope {
        PUBLIC, PRIVATE, GLOBAL
    }

    private final UUID owner;
    private final String name;
    private final String warpToJson;

    public JsonLodestoneWarp(UUID owner, String name, String warpTo) {
        this.owner = owner;
        this.name = name;
        this.warpToJson = warpTo;
    }

    public JsonLodestoneWarp(LodestoneWarp warp) {
        this.owner = warp.getOwner();
        this.name = warp.getName();
        this.warpToJson = warp.getWarpTo().toJSON();
    }

    public UUID getOwner() {
        return owner;
    }
    public String getName() {
        return name;
    }
    public JsonLocation getWarpTo() {
        return (JsonLocation) JSONable.fromJSON(warpToJson, JsonLocation.class);
    }

    public static LodestoneWarp fromJson(String input) {
        Gson gson = new Gson();
        JsonLodestoneWarp jsonWarp = gson.fromJson(input, JsonLodestoneWarp.class);
        return new LodestoneWarp(jsonWarp.getOwner(), jsonWarp.getName(), jsonWarp.getWarpTo());
    }

    public static ArrayList<LodestoneWarp> fromJsonList(String input) {
        Gson gson = new Gson();
        ArrayList<JsonLodestoneWarp> process = new ArrayList<>();
        ArrayList<String> jsonList = gson.fromJson(input, new TypeToken<ArrayList<String>>(){}.getType());
        jsonList.forEach(pos -> process.add(gson.fromJson(pos, JsonLodestoneWarp.class)));

        ArrayList<LodestoneWarp> output = new ArrayList<>();
        process.forEach(json -> output.add(new LodestoneWarp(json.getOwner(), json.getName(), json.getWarpTo())));

        return output;
    }

    public static String toJsonList(Collection<LodestoneWarp> input) {
        ArrayList<String> output = new ArrayList<>();
        input.forEach(obj -> output.add(obj.toJson()));
        return new Gson().toJson(output);
    }

    public static String toJsonListStore(Collection<JsonLodestoneWarp> input) {
        ArrayList<String> output = new ArrayList<>();
        input.forEach(obj -> output.add(new Gson().toJson(obj)));
        return new Gson().toJson(output);
    }
}

package io.github.idoomful.lodestonewarp.gui;

import io.github.idoomful.bukkitutils.object.InventoryBuilder;
import io.github.idoomful.bukkitutils.statics.TextUtils;
import io.github.idoomful.lodestonewarp.DMain;
import io.github.idoomful.lodestonewarp.data.WarpingLodestone;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.Objects;

public class LodestoneMainUI implements MyGUI {
    private Inventory inventory;
    private final Player player;
    private final WarpingLodestone lodestone;

    public LodestoneMainUI(Player player, WarpingLodestone lodestone) {
        this.player = player;
        this.lodestone = lodestone;

        OfflinePlayer owner = Bukkit.getOfflinePlayer(lodestone.getOwner());

        List<String> layout = DMain.getInstance().getConfigs().getFile("settings").getStringList("inventories.main.layout");
        String own = lodestone.getOwner().toString().equals(player.getUniqueId().toString()) ? "own-" : "";
        String title = DMain.getInstance().getConfigs().getFile("settings").getString("inventories.main." + own + "title");
        String titleColor = TextUtils.color(title == null ? "null" : title);

        inventory = Bukkit.createInventory(player, layout.size() * 9,
                titleColor
                        .replace("$owner$", Objects.requireNonNull(owner.getName()))
                        .replace("$x", ((int) Math.floor(lodestone.getLocation().getX())) + "")
                        .replace("$y", ((int) Math.floor(lodestone.getLocation().getY())) + "")
                        .replace("$z", ((int) Math.floor(lodestone.getLocation().getZ())) + "")
        );

        InventoryBuilder builder = new InventoryBuilder(inventory, DMain.getInstance().getConfigs().getFile("settings"), "main");
        builder.build(player, null);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public WarpingLodestone getLodestone() {
        return lodestone;
    }

    @Override
    public void openInventory() {
        player.openInventory(inventory);
    }
}

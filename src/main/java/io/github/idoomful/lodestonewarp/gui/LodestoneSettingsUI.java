package io.github.idoomful.lodestonewarp.gui;

import io.github.idoomful.bukkitutils.object.InventoryBuilder;
import io.github.idoomful.bukkitutils.statics.TextUtils;
import io.github.idoomful.lodestonewarp.DMain;
import io.github.idoomful.lodestonewarp.configuration.MessagesYML;
import io.github.idoomful.lodestonewarp.data.WarpingLodestone;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

public class LodestoneSettingsUI implements MyGUI {
    private final Inventory inventory;
    private final Player player;
    private final WarpingLodestone lodestone;

    public LodestoneSettingsUI(Player player, WarpingLodestone lodestone) {
        this.player = player;
        this.lodestone = lodestone;

        List<String> layout = DMain.getInstance().getConfigs().getFile("settings").getStringList("inventories.settings.layout");
        String title = DMain.getInstance().getConfigs().getFile("settings").getString("inventories.settings.title");
        String titleColor = TextUtils.color(title == null ? "null" : title);
        inventory = Bukkit.createInventory(player, layout.size() * 9, titleColor);

        String status = lodestone.isPublic()
                ? MessagesYML.Words.PUBLIC.color(player)
                : MessagesYML.Words.PRIVATE.color(player);

        InventoryBuilder builder = new InventoryBuilder(inventory, DMain.getInstance().getConfigs().getFile("settings"), "settings");
        builder.build(player, null);

        List<ItemStack> items = builder.getAddedItems().stream().peek(item -> {
            if(item.hasItemMeta()) {
                ItemMeta im = item.getItemMeta();
                assert im != null;
                if(im.hasDisplayName()) im.setDisplayName(im.getDisplayName().replace("$status$", status));
                item.setItemMeta(im);
            }
        }).collect(Collectors.toList());

        builder.overrideAddedItems(items);
    }

    public WarpingLodestone getLodestone() {
        return lodestone;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public void openInventory() {
        player.openInventory(inventory);
    }
}

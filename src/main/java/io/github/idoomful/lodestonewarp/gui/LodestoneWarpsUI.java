package io.github.idoomful.lodestonewarp.gui;

import io.github.idoomful.bukkitutils.json.JsonLocation;
import io.github.idoomful.bukkitutils.object.Paginable;
import io.github.idoomful.bukkitutils.statics.BeautifyUtils;
import io.github.idoomful.bukkitutils.statics.ItemBuilder;
import io.github.idoomful.bukkitutils.statics.TextUtils;
import io.github.idoomful.lodestonewarp.DMain;
import io.github.idoomful.lodestonewarp.configuration.MessagesYML;
import io.github.idoomful.lodestonewarp.configuration.SettingsYML;
import io.github.idoomful.lodestonewarp.data.LodestoneWarp;
import io.github.idoomful.lodestonewarp.data.WarpingLodestone;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LodestoneWarpsUI extends Paginable implements MyGUI {
    private final Inventory inventory;
    private final Player player;
    private final WarpingLodestone lodestone;
    private final LodestoneWarp.Scope scope;

    public LodestoneWarpsUI(Player player, WarpingLodestone lodestone, LodestoneWarp.Scope scope) {
        this.player = player;
        this.lodestone = lodestone;
        this.scope = scope;

        String title = scope == LodestoneWarp.Scope.PUBLIC
                ? SettingsYML.WarpsList.PUBLIC_TITLE.get(String.class)
                : scope == LodestoneWarp.Scope.PRIVATE
                ? SettingsYML.WarpsList.PRIVATE_TITLE.get(String.class)
                : SettingsYML.WarpsList.GLOBAL_TITLE.get(String.class);

        this.inventory = Bukkit.createInventory(player, SettingsYML.WarpsList.ROWS.get(Integer.class) * 9, TextUtils.placeholder(player, title));
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public void openInventory() {
        createFirstPage();
        player.openInventory(inventory);
    }

    public WarpingLodestone getLodestone() {
        return lodestone;
    }

    public LodestoneWarp.Scope getScope() {
        return scope;
    }

    @Override
    protected List<ItemStack> bodyList() {
        List<ItemStack> output = new ArrayList<>();

        List<LodestoneWarp> warps = scope == LodestoneWarp.Scope.PUBLIC
                ? lodestone.getPublicWarps()
                : scope == LodestoneWarp.Scope.PRIVATE
                ? lodestone.getPrivateWarps()
                : DMain.getInstance().getCache().getGlobalWarps();

        String iconModel = scope == LodestoneWarp.Scope.GLOBAL
                ? player.hasPermission("lodestonewarp.command.adminwarp")
                    ? SettingsYML.WarpsList.ADMIN_GLOBAL_WARP_ITEM.get(String.class)
                    : SettingsYML.WarpsList.GLOBAL_WARP_ITEM.get(String.class)
                : SettingsYML.WarpsList.WARP_ITEM.get(String.class);

        for(LodestoneWarp warp : warps) {
            ItemStack icon = ItemBuilder.build(iconModel
                    .replace("$loc$",
                            TextUtils.color(warp.getName().replace(" ", "_"))
                                    + "," + warp.getWarpTo().getWorld()
                                    + "," + warp.getWarpTo().getX()
                                    + "," + warp.getWarpTo().getY()
                                    + "," + warp.getWarpTo().getZ()
                    )
                    .replace("$name$", TextUtils.color(warp.getName()).replace(" ", "_"))
                    .replace("$owner$", Objects.requireNonNull(Bukkit.getOfflinePlayer(warp.getOwner()).getName()))
                    .replace("$coord$", warp.getWarpTo().toStringRounded().replace(" ", "_"))
                    .replace("$safe$", warp.isSafe()
                            ? MessagesYML.Words.YES.color(player)
                            : MessagesYML.Words.NO.color(player)
                    )
            );

            output.add(icon);
        }

        return output;
    }

    @Override
    protected int[] skippingPoints() {
        return SettingsYML.WarpsList.SKIPPING_POINTS.getIntList();
    }

    @Override
    protected int itemsPerPage() {
        return SettingsYML.WarpsList.ITEMS_PER_PAGE.get(Integer.class);
    }

    @Override
    protected ItemStack createNextButton() {
        return ItemBuilder.build(SettingsYML.WarpsList.NEXT_PAGE_BUTTON.get(String.class));
    }

    @Override
    protected ItemStack createPreviousButton() {
        return ItemBuilder.build(SettingsYML.WarpsList.PREVIOUS_PAGE_BUTTON.get(String.class));
    }

    @Override
    protected ItemStack createItemBeforeReplacement() {
        return ItemBuilder.build(SettingsYML.WarpsList.EMPTY_BUTTON.get(String.class));
    }

    @Override
    protected int[] nextButtonSlots() {
        return SettingsYML.WarpsList.NEXT_PAGE_BUTTON_SLOTS.getIntList();
    }

    @Override
    protected int[] previousButtonSlots() {
        return SettingsYML.WarpsList.PREVIOUS_PAGE_BUTTON_SLOTS.getIntList();
    }

    @Override
    public void nextPage() {
        inventory.clear();
        refreshPage();
        super.nextPage();

        BeautifyUtils.playSoundSimple(DMain.getInstance(), player, SettingsYML.Options.PAGE_TURN_SOUND.get(String.class));
    }

    @Override
    public void previousPage() {
        inventory.clear();
        refreshPage();
        super.previousPage();

        BeautifyUtils.playSoundSimple(DMain.getInstance(), player, SettingsYML.Options.PAGE_TURN_SOUND.get(String.class));
    }
}

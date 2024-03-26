package com.promcteam.divinity.modules.command;

import com.promcteam.codex.CodexEngine;
import com.promcteam.codex.items.ItemType;
import com.promcteam.codex.items.exception.MissingItemException;
import com.promcteam.codex.items.exception.MissingProviderException;
import com.promcteam.codex.util.ItemUT;
import com.promcteam.codex.util.random.Rnd;
import com.promcteam.divinity.Perms;
import com.promcteam.divinity.api.DivinityAPI;
import com.promcteam.divinity.modules.api.QModuleDrop;
import com.promcteam.divinity.modules.list.itemgenerator.ItemGeneratorManager;
import com.promcteam.divinity.modules.list.itemgenerator.ItemGeneratorManager.GeneratorItem;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MGetCmd extends MCmd<QModuleDrop<?>> {

    public MGetCmd(@NotNull QModuleDrop<?> module) {
        super(module, new String[]{"get"}, Perms.ADMIN);
    }

    @Override
    @NotNull
    public String usage() {
        return plugin.lang().Module_Cmd_Get_Usage.getMsg();
    }

    @Override
    @NotNull
    public String description() {
        return plugin.lang().Module_Cmd_Get_Desc.getMsg();
    }

    @Override
    public boolean playersOnly() {
        return true;
    }

    @Override
    @NotNull
    public List<String> getTab(@NotNull Player player, int i, @NotNull String[] args) {
        if (i == 1) {
            return this.module.getItemIds();
        }
        if (i == 2) {
            return Arrays.asList("[level]", "-1", "1:5", "1");
        }
        if (i == 3) {
            return Arrays.asList("1", "10");
        }

        // Support for material argument for ItemGenerator
        if (i == 4 && this.module instanceof ItemGeneratorManager) {
            ItemGeneratorManager itemGeneratorManager = (ItemGeneratorManager) this.module;
            GeneratorItem        generatorItem        = itemGeneratorManager.getItemById(args[1]);
            if (generatorItem != null) {
                List<String> list = generatorItem.getMaterialsList().stream()
                        .map(ItemType::getNamespacedID).collect(Collectors.toList());
                return list;
            }
        }
        return super.getTab(player, i, args);
    }

    @Override
    public void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        String id = QModuleDrop.RANDOM_ID;
        if (args.length >= 2) {
            id = args[1];
        }

        int amount = 1;
        int lMin   = -1;
        int lMax   = -1;

        if (args.length >= 3) {
            if (args[2].contains(":")) {
                String[] split = args[2].split(":");
                lMin = this.getNumI(sender, split[0], -1, true);
                lMax = this.getNumI(sender, split[1], -1, true);
            } else {
                lMin = lMax = this.getNumI(sender, args[2], -1, true);
            }
        }
        if (args.length >= 4) {
            amount = this.getNumI(sender, args[3], 1);
        }

        Player    p    = (Player) sender;
        ItemStack item = null;

        ItemType material;
        try {
            material = args.length >= 5 ? CodexEngine.get().getItemManager().getItemType(args[4].toUpperCase()) : null;
        } catch (MissingProviderException | MissingItemException e) {
            material = null;
        }
        ItemGeneratorManager itemGenerator =
                this.module instanceof ItemGeneratorManager ? (ItemGeneratorManager) this.module : null;
        GeneratorItem generatorItem = itemGenerator != null ? itemGenerator.getItemById(id) : null;

        Map<String, Integer> addedItems = new HashMap<>();

        for (int i = 0; i < amount; i++) {
            int iLevel = Rnd.get(lMin, lMax);

            if (material != null && generatorItem != null) {
                item = generatorItem.create(iLevel, -1, material);
            } else {
                item = DivinityAPI.getItemByModule(this.module, id, iLevel, -1, -1);
            }
            if (item == null) continue;

            ItemUT.addItem(p, item);

            String name = ItemUT.getItemName(item);
            addedItems.put(name, addedItems.containsKey(name) ? addedItems.get(name) + 1 : 1);
        }

        for (String name : addedItems.keySet()) {
            plugin.lang().Module_Cmd_Get_Done
                    .replace("%item%", name)
                    .replace("%amount%", String.valueOf(addedItems.get(name)))
                    .send(sender);
        }

    }
}

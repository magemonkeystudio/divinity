package su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.materials;

import mc.promcteam.engine.NexEngine;
import mc.promcteam.engine.items.exception.MissingItemException;
import mc.promcteam.engine.items.exception.MissingProviderException;
import mc.promcteam.engine.manager.api.menu.Slot;
import mc.promcteam.engine.utils.StringUT;
import mc.promcteam.engine.utils.constants.JStrings;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.quantumrpg.config.Config;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.AbstractEditorGUI;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.EditorGUI;
import su.nightexpress.quantumrpg.types.ItemGroup;
import su.nightexpress.quantumrpg.types.ItemSubType;

public class MainMaterialsGUI extends AbstractEditorGUI {
    public MainMaterialsGUI(Player player, ItemGeneratorReference itemGenerator) {
        super(player, 1, "[&d" + itemGenerator.getId() + "&r] editor/" + EditorGUI.ItemType.MATERIALS.getTitle(), itemGenerator);
    }

    @Override
    public void setContents() {
        boolean reversed = this.itemGenerator.getHandle().isMaterialReversed();
        setSlot(0, new Slot(createItem(reversed
                        ? Material.STRUCTURE_VOID
                        : Material.BARRIER,
                "&eIs whitelist/reversed",
                "&bCurrent: &a" + reversed,
                "&6Left-Click: &eToggle")) {
            @Override
            public void onLeftClick() {
                itemGenerator.getConfig().set(ItemType.REVERSE.getPath(), !reversed);
                saveAndReopen();
            }
        });
        setSlot(1, new Slot(createItem(Material.BOOK,
                "&e" + (reversed
                        ? "Whitelist"
                        : "Blacklist"), StringUT.replace(CURRENT_PLACEHOLDER, itemGenerator.getConfig().getStringList(ItemType.LIST.getPath()),
                        "&bCurrent:",
                        "&a%current%",
                        "&6Left-Click: &eModify"
                ))) {
            @Override
            public void onLeftClick() {
                openSubMenu(new MaterialListGUI(player, itemGenerator));
            }
        });
        setSlot(2, new Slot(createItem(Material.END_CRYSTAL,
                "&eModel Data",
                "&6Left-Click: &eModify")) {
            @Override
            public void onLeftClick() {
                openSubMenu(new MainModelDataGUI(player, itemGenerator));
            }
        });
        setSlot(3, new Slot(createItem(Material.OAK_SIGN,
                "&eStat Modifiers",
                "&6Left-Click: &eModify")) {
            @Override
            public void onLeftClick() {
                openSubMenu(new MainStatModifiersGUI(player, itemGenerator));
            }
        });
    }

    static ItemStack getMaterial(String string) {
        try {
            return NexEngine.get().getItemManager().getItemType(string).create();
        } catch (MissingProviderException | MissingItemException ignored) {}

        try {
            return new ItemStack(Material.valueOf(string.toUpperCase()));
        } catch (IllegalArgumentException ignored) {}

        String[] split = string.toUpperCase().split('\\'+JStrings.MASK_ANY, 2);
        if (split.length == 2) { // We have a wildcard
            for (mc.promcteam.engine.items.ItemType material : Config.getAllRegisteredMaterials()) {
                String materialName = material.getNamespacedID().toUpperCase();
                if (split[0].isEmpty() && materialName.endsWith(split[1])
                || split[1].isEmpty() && materialName.startsWith(split[0])) return material.create();
            }
        }
        return new ItemStack(Material.STONE);
    }

    static ItemStack getMaterialGroup(String materialGroup) {
        try {
            return NexEngine.get().getItemManager().getItemType(materialGroup).create();
        } catch (MissingProviderException | MissingItemException ignored) {}

        ItemSubType subType = Config.getSubTypeById(materialGroup);
        if (subType != null) {
            return getMaterial(subType.getMaterials().stream().findAny().orElse("STONE"));
        }

        try {
            ItemGroup itemGroup = ItemGroup.valueOf(materialGroup.toUpperCase());
            return getMaterial(itemGroup.getMaterials().stream().findAny().orElse("STONE"));
        } catch (IllegalArgumentException ignored) {}

        return getMaterial(materialGroup.toUpperCase());
    }

    public enum ItemType {
        REVERSE("reverse"),
        LIST("black-list"),
        MODEL_DATA("model-data"),
        STAT_MODIFIERS("stat-modifiers"),
        ;

        private final String path;

        ItemType(String path) {
            this.path = "generator.materials." + path;
        }

        public String getPath() {return path;}
    }
}

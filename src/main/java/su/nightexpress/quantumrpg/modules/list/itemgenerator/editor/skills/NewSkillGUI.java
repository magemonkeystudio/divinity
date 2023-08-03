package su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.skills;

import mc.promcteam.engine.config.api.JYML;
import mc.promcteam.engine.manager.api.gui.ContentType;
import mc.promcteam.engine.manager.api.gui.GuiClick;
import mc.promcteam.engine.manager.api.gui.GuiItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.hooks.EHook;
import su.nightexpress.quantumrpg.hooks.external.SkillAPIHK;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.ItemGeneratorManager;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.AbstractEditorGUI;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.EditorGUI;

import java.util.List;

public class NewSkillGUI extends AbstractEditorGUI {
    private final List<String> list;

    public NewSkillGUI(@NotNull ItemGeneratorManager itemGeneratorManager, ItemGeneratorManager.GeneratorItem itemGenerator, List<String> missingSkills) {
        super(itemGeneratorManager, itemGenerator, 54);
        setTitle("[&d"+itemGenerator.getId()+"&r] editor/"+EditorGUI.ItemType.SKILLS.getTitle());
        this.list = missingSkills;
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inventory, int page) {
        int totalPages = Math.max((int) Math.ceil(this.list.size()*1.0/42), 1);
        final int currentPage = page < 1 ? totalPages : Math.min(page, totalPages);
        this.setUserPage(player, currentPage, totalPages);
        GuiClick guiClick = (player1, type, clickEvent) -> {
            this.setUserPage(player1, currentPage, totalPages);
            if (type == null) { return; }
            Class<?> clazz = type.getClass();
            if (clazz.equals(ContentType.class)) {
                ContentType type2 = (ContentType) type;
                switch (type2) {
                    case RETURN:
                        new SkillListGUI(itemGeneratorManager, itemGenerator).open(player1, 1);
                        break;
                    case EXIT: {
                        player1.closeInventory();
                        break;
                    }
                    case NEXT: {
                        saveAndReopen(currentPage+1);
                        break;
                    }
                    case BACK: {
                        saveAndReopen(currentPage-1);
                        break;
                    }
                }
                return;
            }
            if (type.equals(ItemType.NEW)) {
                GuiItem guiItem = this.getButton(player, clickEvent.getSlot());
                if (guiItem == null) { return; }
                String id = guiItem.getId();
                String path = EditorGUI.ItemType.SKILLS.getPath()+".list."+id;
                JYML cfg = this.itemGenerator.getConfig();
                cfg.set(SkillGUI.ItemType.CHANCE.getPath(path), 0);
                cfg.set(SkillGUI.ItemType.MIN.getPath(path), 1);
                cfg.set(SkillGUI.ItemType.MAX.getPath(path), 1);
                cfg.set(SkillGUI.ItemType.LORE.getPath(path), List.of("&b"+id+" &7Lvl. &f%level%"));
                saveAndReopen();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        new SkillListGUI(NewSkillGUI.this.itemGeneratorManager, NewSkillGUI.this.itemGenerator).open(player1, 1);
                    }
                }.runTask(plugin);
            }
        };
        SkillAPIHK skillAPIHK = (SkillAPIHK) QuantumRPG.getInstance().getHook(EHook.SKILL_API);
        for (int enchantmentIndex = (currentPage-1)*42, last = Math.min(this.list.size(), enchantmentIndex+42), invIndex = 1;
             enchantmentIndex < last; enchantmentIndex++, invIndex++) {
            if ((invIndex)%9 == 8) { invIndex += 2; }
            String key = this.list.get(enchantmentIndex);
            GuiItem guiItem = this.createButton(key, ItemType.NEW, Material.JACK_O_LANTERN,
                    "&e"+key, List.of("&6Left-Click: &eAdd"), invIndex, guiClick);
            if (skillAPIHK != null) {
                ItemStack indicator = skillAPIHK.getSkillIndicator(key);
                if (indicator != null) {
                    ItemStack itemStack = guiItem.getItemRaw();
                    itemStack.setType(indicator.getType());
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    if (itemMeta != null) {
                        ItemMeta indicatorMeta = indicator.getItemMeta();
                        if (indicatorMeta != null && indicatorMeta.hasCustomModelData()) {
                            itemMeta.setCustomModelData(indicatorMeta.getCustomModelData());
                            itemStack.setItemMeta(itemMeta);
                        }
                    }
                    guiItem.setItem(itemStack);
                }
            }
            this.addButton(guiItem);
        }
        this.addButton(this.createButton("prev-page", ContentType.BACK, Material.ENDER_PEARL, "&dPrevious Page", List.of(), 0, guiClick));
        this.addButton(this.createButton("next-page", ContentType.NEXT, Material.ENDER_PEARL, "&dNext Page", List.of(), 8, guiClick));
        this.addButton(this.createButton("return", ContentType.RETURN, Material.BARRIER, "&c&lReturn", List.of(), 53, guiClick));
    }
}

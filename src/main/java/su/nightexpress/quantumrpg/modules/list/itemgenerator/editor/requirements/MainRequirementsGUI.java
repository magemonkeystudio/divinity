package su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.requirements;

import mc.promcteam.engine.manager.api.gui.ContentType;
import mc.promcteam.engine.manager.api.gui.GuiClick;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.ItemGeneratorManager;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.AbstractEditorGUI;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.EditorGUI;

import java.util.List;

public class MainRequirementsGUI extends AbstractEditorGUI {

    public MainRequirementsGUI(@NotNull ItemGeneratorManager itemGeneratorManager, ItemGeneratorManager.GeneratorItem itemGenerator) {
        super(itemGeneratorManager, itemGenerator, 18);
        setTitle("[&d"+itemGenerator.getId()+"&r] editor/"+EditorGUI.ItemType.REQUIREMENTS.getTitle());
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inventory, int page) {
        GuiClick guiClick = (player1, type, clickEvent) -> {
            if (type == null) { return; }
            Class<?> clazz = type.getClass();
            if (clazz.equals(ContentType.class)) {
                ContentType type2 = (ContentType) type;
                switch (type2) {
                    case RETURN: {
                        new EditorGUI(itemGeneratorManager, itemGenerator).open(player1, 1);
                        break;
                    }
                    case EXIT: {
                        player1.closeInventory();
                        break;
                    }
                }
                return;
            }

            if (clazz.equals(ItemType.class)) {
                ItemType type2 = (ItemType) type;
                switch (type2) {
                    case LEVEL: {
                        new RequirementsGUI(itemGeneratorManager, itemGenerator, MainRequirementsGUI.ItemType.LEVEL.getPath(), Material.EXPERIENCE_BOTTLE).open(player1, 1);
                        break;
                    }
                    case CLASS: {
                        new RequirementsGUI(itemGeneratorManager, itemGenerator, MainRequirementsGUI.ItemType.CLASS.getPath(), Material.BOW).open(player1, 1);
                        break;
                    }
                    case BANNED_CLASS: {
                        new RequirementsGUI(itemGeneratorManager, itemGenerator, MainRequirementsGUI.ItemType.BANNED_CLASS.getPath(), Material.BOW).open(player1, 1);
                        break;
                    }
                    case MCMMO_SKILL: {
                        new RequirementsGUI(itemGeneratorManager, itemGenerator, MainRequirementsGUI.ItemType.MCMMO_SKILL.getPath(), Material.DIAMOND_SWORD).open(player1, 1);
                        break;
                    }
                    case JOBS_JOB: {
                        new RequirementsGUI(itemGeneratorManager, itemGenerator, MainRequirementsGUI.ItemType.JOBS_JOB.getPath(), Material.IRON_PICKAXE).open(player1, 1);
                        break;
                    }
                }
            }
        };

        this.addButton(this.createButton("level", ItemType.LEVEL, Material.EXPERIENCE_BOTTLE,
                                         "&eLevel requirements", List.of(
                                                "&6Left-Click: &eModify"), 2, guiClick));
        this.addButton(this.createButton("class", ItemType.CLASS, Material.BOW,
                                         "&eClass requirements", List.of(
                                                "&6Left-Click: &eModify"), 4, guiClick));
        this.addButton(this.createButton("banned-class", ItemType.BANNED_CLASS, Material.BARRIER,
                                         "&eBanned Class requirements", List.of(
                                                "&6Left-Click: &eModify"), 6, guiClick));
        this.addButton(this.createButton("mcmmo-skill", ItemType.MCMMO_SKILL, Material.DIAMOND_SWORD,
                "&emcMMO Skill", List.of(
                        "&6Left-Click: &eModify"), 11, guiClick));
        this.addButton(this.createButton("jobs-job", ItemType.JOBS_JOB, Material.IRON_PICKAXE,
                "&eJob", List.of(
                        "&6Left-Click: &eModify"), 13, guiClick));
        this.addButton(this.createButton("aurelium-skill", ItemType.AURELIUM_SKILL, Material.PAPER,
                "&eAureliumSkills Skill", List.of(
                        "&6Left-Click: &eModify"), 15, guiClick));
        this.addButton(this.createButton("aurelium-stat", ItemType.AURELIUM_STAT, Material.RED_DYE,
                "&eAureliumSkills Stat", List.of(
                        "&6Left-Click: &eModify"), 17, guiClick));
        this.addButton(this.createButton("exit", ContentType.RETURN, Material.BARRIER, "&c&lReturn", List.of(), 8, guiClick));
    }

    public enum ItemType {
        LEVEL("level"),
        CLASS("class"),
        BANNED_CLASS("banned-class"),
        MCMMO_SKILL("mcmmo-skill"),
        JOBS_JOB("jobs-job"),
        AURELIUM_SKILL("aurelium-skill"),
        AURELIUM_STAT("aurelium-stat");

        private final String path;

        ItemType(String path) { this.path = "generator.user-requirements-by-level."+path; }

        public String getPath() { return path; }
    }
}

package su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.requirements;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.ItemGeneratorManager;

public class LevelRequirementsGUI extends RequirementsGUI{
    public LevelRequirementsGUI(@NotNull ItemGeneratorManager itemGeneratorManager, ItemGeneratorManager.GeneratorItem itemGenerator) {
        super(itemGeneratorManager, itemGenerator);
    }

    @Override
    protected String getPath() { return MainRequirementsGUI.ItemType.LEVEL.getPath(); }

    @Override
    protected Material getButtonMaterial() { return Material.EXPERIENCE_BOTTLE; }
}

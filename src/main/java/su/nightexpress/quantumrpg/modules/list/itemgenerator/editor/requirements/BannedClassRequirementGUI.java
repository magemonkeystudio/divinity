package su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.requirements;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.ItemGeneratorManager;

public class BannedClassRequirementGUI extends RequirementsGUI {
    public BannedClassRequirementGUI(@NotNull ItemGeneratorManager itemGeneratorManager, ItemGeneratorManager.GeneratorItem itemGenerator) {
        super(itemGeneratorManager, itemGenerator);
    }

    @Override
    protected String getPath() { return MainRequirementsGUI.ItemType.BANNED_CLASS.getPath(); }

    @Override
    protected Material getButtonMaterial() { return Material.BOW; }
}

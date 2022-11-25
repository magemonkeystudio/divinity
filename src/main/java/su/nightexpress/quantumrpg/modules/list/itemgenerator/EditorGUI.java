package su.nightexpress.quantumrpg.modules.list.itemgenerator;

import mc.promcteam.engine.config.api.JYML;
import mc.promcteam.engine.manager.api.gui.NGUI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.QuantumRPG;

public class EditorGUI extends NGUI<QuantumRPG> {
    private ItemGeneratorManager.GeneratorItem itemGenerator;

    EditorGUI(@NotNull ItemGeneratorManager itemGeneratorManager, @NotNull JYML cfg, ItemGeneratorManager.GeneratorItem itemGenerator) {
        super(itemGeneratorManager.plugin, cfg, "editor-gui.");
        this.itemGenerator = itemGenerator;
        this.setTitle(this.getTitle().replace("%id%", itemGenerator.getId()));
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inventory, int i) {

    }

    @Override
    protected boolean ignoreNullClick() {
        return false;
    }

    @Override
    protected boolean cancelClick(int i) {
        return false;
    }

    @Override
    protected boolean cancelPlayerClick() {
        return false;
    }
}

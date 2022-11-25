package su.nightexpress.quantumrpg.modules.list.itemgenerator.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.Perms;
import su.nightexpress.quantumrpg.modules.api.QModuleDrop;
import su.nightexpress.quantumrpg.modules.command.MCmd;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.ItemGeneratorManager;

import java.util.List;

public class EditorCommand extends MCmd<ItemGeneratorManager> {

    public EditorCommand(@NotNull ItemGeneratorManager module) {
        super(module, new String[] {"edit"}, Perms.ADMIN);
    }

    @NotNull
    @Override
    public String usage() { return "<item generator id>"; }

    @NotNull
    @Override
    public String description() { return plugin.lang().ItemGenerator_Cmd_Editor_Desc.getMsg(); }

    @Override
    public boolean playersOnly() { return true; }

    @Override
    @NotNull
    public List<String> getTab(@NotNull Player player, int i, @NotNull String[] args) {
        if (i == 1) {
            List<String> ids = this.module.getItemIds();
            ids.remove(QModuleDrop.RANDOM_ID);
            return ids;
        }
        return super.getTab(player, i, args);
    }

    @Override
    protected void perform(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String[] strings) {
        if (strings.length != 2) {
            this.printUsage(commandSender);
            return;
        }
        module.openEditor(strings[1], (Player) commandSender);
    }
}

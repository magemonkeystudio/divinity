package com.promcteam.divinity.modules.list.itemgenerator.command;

import com.promcteam.divinity.Perms;
import com.promcteam.divinity.modules.api.QModuleDrop;
import com.promcteam.divinity.modules.command.MCmd;
import com.promcteam.divinity.modules.list.itemgenerator.ItemGeneratorManager;
import com.promcteam.divinity.modules.list.itemgenerator.editor.AbstractEditorGUI;
import com.promcteam.divinity.modules.list.itemgenerator.editor.EditorGUI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EditCommand extends MCmd<ItemGeneratorManager> {

    public EditCommand(@NotNull ItemGeneratorManager module) {
        super(module, new String[]{"edit"}, Perms.ADMIN);
    }

    @NotNull
    @Override
    public String usage() {return "<id>";}

    @NotNull
    @Override
    public String description() {return plugin.lang().ItemGenerator_Cmd_Editor_Desc.getMsg();}

    @Override
    public boolean playersOnly() {return true;}

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
        ItemGeneratorManager.GeneratorItem itemGenerator = module.getItemById(strings[1]);
        if (itemGenerator == null) {
            plugin.lang().ItemGenerator_Cmd_Editor_Error_InvalidItem.send(commandSender);
            return;
        }
        try {
            new EditorGUI((Player) commandSender, new AbstractEditorGUI.ItemGeneratorReference(itemGenerator)).open(1);
        } catch (IllegalStateException e) {
            plugin.lang().ItemGenerator_Cmd_Editor_Error_AlreadyOpen.replace("%player%", commandSender.getName())
                    .send(commandSender);
        }
    }
}

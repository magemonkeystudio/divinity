package su.nightexpress.quantumrpg.cmds;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.cmds.list.*;
import su.nightexpress.quantumrpg.utils.Utils;

import java.util.*;

public class MainCommand implements CommandExecutor, TabExecutor {
    private Map<String, ICmd> commands = new HashMap<>();

    private HelpCommand help;

    public MainCommand(QuantumRPG plugin) {
        this.help = new HelpCommand();
        this.commands.put("set", new SetCommand(plugin));
        this.commands.put("modify", new ModifyCommand());
        this.commands.put("reload", new ReloadCommand(plugin));
        this.commands.put("info", new InfoCommand(plugin));
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        HelpCommand helpCommand = this.help;
        ICmd iCmd = helpCommand;
        if (args.length > 0 && this.commands.containsKey(args[0]))
            iCmd = this.commands.get(args[0]);
        iCmd.execute(sender, label, args);
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof org.bukkit.entity.Player))
            return null;
        if (args.length == 1) {
            List<String> sugg = new ArrayList<>(this.commands.keySet());
            return Utils.getSugg(args[0], sugg);
        }
        return Collections.emptyList();
    }
}

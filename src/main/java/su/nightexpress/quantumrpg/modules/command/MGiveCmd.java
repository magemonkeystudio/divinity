package su.nightexpress.quantumrpg.modules.command;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.PlayerUT;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.quantumrpg.Perms;
import su.nightexpress.quantumrpg.api.QuantumAPI;
import su.nightexpress.quantumrpg.modules.api.QModuleDrop;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.ItemGeneratorManager;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.ItemGeneratorManager.GeneratorItem;

public class MGiveCmd extends MCmd<QModuleDrop<?>> {
	
	public MGiveCmd(@NotNull QModuleDrop<?> m) {
		super(m, new String[] {"give"}, Perms.ADMIN);
	}

	@Override
	@NotNull
	public String usage() {
		return plugin.lang().Module_Cmd_Give_Usage.getMsg();
	}
	
	@Override
	@NotNull
	public String description() {
		return plugin.lang().Module_Cmd_Give_Desc.getMsg();
	}

	@Override
	public boolean playersOnly() {
		return false;
	}

	@Override
	@NotNull
	public List<String> getTab(@NotNull Player player, int i, @NotNull String[] args) {
		if (i == 1) {
			return PlayerUT.getPlayerNames();
		}
		if (i == 2) {
			return module.getItemIds();
		}
		if (i == 3) {
			return Arrays.asList("[level]", "-1", "1:5", "1");
		}
		if (i == 4) {
			return Arrays.asList("1", "10");
		}
		
		// Support for material argument for ItemGenerator
		if (i == 5 && this.module instanceof ItemGeneratorManager) {
			ItemGeneratorManager itemGeneratorManager = (ItemGeneratorManager) this.module;
			GeneratorItem generatorItem = itemGeneratorManager.getItemById(args[2]);
			if (generatorItem != null) {
				List<String> list = generatorItem.getMaterialsList().stream()
						.map(Material::name).collect(Collectors.toList());
				return list;
			}
		}
		return super.getTab(player, i, args);
	}

	@Override
	public void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
		if (args.length < 2) {
			this.printUsage(sender);
			return;
		}
		
		Player p = plugin.getServer().getPlayer(args[1]);
		if (p == null) {
			this.errPlayer(sender);
			return;
		}
		
		String id = QModuleDrop.RANDOM_ID;
		if (args.length >= 3) {
			id = args[2];
		}
		
		int lMin = -1;
		int lMax = -1;
		int amount = 1;
		
		if (args.length >= 4) {
			if (args[3].contains(":")) {
				String[] split = args[3].split(":");
				lMin = this.getNumI(sender, split[0], -1, true);
				lMax = this.getNumI(sender, split[1], -1, true);
			}
			else {
				lMin = lMax = this.getNumI(sender, args[3], -1, true);
			}
		}
		
		if (args.length == 5) {
			amount = this.getNumI(sender, args[4], 1);
		}
		
		ItemStack item = null;
		
		Material material = args.length >= 6 ? Material.getMaterial(args[5].toUpperCase()) : null;
		ItemGeneratorManager itemGenerator = this.module instanceof ItemGeneratorManager ? (ItemGeneratorManager) this.module : null;
		GeneratorItem generatorItem = itemGenerator != null ? itemGenerator.getItemById(id) : null;
		
		for (int i = 0; i < amount; i++) {
			int iLevel = Rnd.get(lMin, lMax);
			
			if (material != null && generatorItem != null) {
				item = generatorItem.create(iLevel, -1, material);
			}
			else {
				item = QuantumAPI.getItemByModule(this.module, id, iLevel, -1, -1);
			}
			if (item == null) continue;
			ItemUT.addItem(p, item);
			
			plugin.lang().Module_Cmd_Give_Done
					.replace("%player%", p.getName())
					.replace("%item%", ItemUT.getItemName(item))
					.replace("%amount%", String.valueOf(item.getAmount()))
					.send(sender);
		}
	}
}

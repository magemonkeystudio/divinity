package studio.magemonkey.divinity.modules.command;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import studio.magemonkey.divinity.utils.LoreUT;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.CodexEngine;
import studio.magemonkey.codex.api.items.ItemType;
import studio.magemonkey.codex.api.items.exception.MissingItemException;
import studio.magemonkey.codex.api.items.exception.MissingProviderException;
import studio.magemonkey.codex.util.random.Rnd;
import studio.magemonkey.divinity.Perms;
import studio.magemonkey.divinity.api.DivinityAPI;
import studio.magemonkey.divinity.modules.api.QModuleDrop;
import studio.magemonkey.divinity.modules.list.itemgenerator.ItemGeneratorManager;
import studio.magemonkey.divinity.modules.list.itemgenerator.ItemGeneratorManager.GeneratorItem;
import studio.magemonkey.divinity.stats.EntityStats;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MMobEquipCmd extends MCmd<QModuleDrop<?>> {

    public MMobEquipCmd(@NotNull QModuleDrop<?> m) {
        super(m, new String[]{"mobequip"}, Perms.ADMIN);
    }

    @Override
    @NotNull
    public String usage() {
        return "/[module] mobequip [uuid] <slot> <id> <level> [amount] [material] [-noenchants]";
    }

    @Override
    @NotNull
    public String description() {
        return "Equip a generated item on a mob. With [uuid]: targets by UUID (usable from console/MythicMobs). Without: targets the mob you are looking at (player only).";
    }

    @Override
    public boolean playersOnly() {
        return false;
    }

    @Override
    @NotNull
    public List<String> getTab(@NotNull Player player, int i, @NotNull String[] args) {
        if (i == 1) {
            return Arrays.asList("head", "chest", "legs", "feet", "hand", "offhand");
        }
        if (i == 2) {
            return module.getItemIds();
        }
        if (i == 3) {
            return Arrays.asList("[level]", "-1", "1");
        }
        if (i == 4) {
            return Arrays.asList("1");
        }
        if (i == 5) {
            List<String> list = new java.util.ArrayList<>();
            if (this.module instanceof ItemGeneratorManager) {
                ItemGeneratorManager igm = (ItemGeneratorManager) this.module;
                GeneratorItem gi = igm.getItemById(args[2]);
                if (gi != null) {
                    list.addAll(gi.getMaterialsList().stream()
                            .map(ItemType::getNamespacedID).collect(Collectors.toList()));
                }
            }
            return list;
        }
        if (i == 6) {
            return Arrays.asList("-noenchants");
        }
        return super.getTab(player, i, args);
    }

    @Override
    public void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (args.length < 4) {
            this.printUsage(sender);
            return;
        }

        // Detect UUID mode: args[1] looks like a UUID → console/MythicMobs targeting
        // Otherwise: ray-trace mode (requires Player sender)
        LivingEntity mob;
        int argOffset; // index where <slot> starts

        UUID targetUuid = tryParseUUID(args[1]);
        if (targetUuid != null) {
            // UUID mode — works from console, MythicMobs, or player
            if (args.length < 5) {
                this.printUsage(sender);
                return;
            }
            Entity entity = Bukkit.getEntity(targetUuid);
            if (!(entity instanceof LivingEntity)) {
                sender.sendMessage("§cNo living entity found with UUID §f" + targetUuid + "§c.");
                return;
            }
            mob = (LivingEntity) entity;
            argOffset = 2; // slot = args[2], id = args[3], level = args[4], ...
        } else {
            // Ray-trace mode — player only
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cProvide a target UUID when running this command from console/MythicMobs.");
                sender.sendMessage("§cUsage: " + this.usage());
                return;
            }
            Player player = (Player) sender;
            RayTraceResult result = player.getWorld().rayTraceEntities(
                    player.getEyeLocation(),
                    player.getEyeLocation().getDirection(),
                    10.0,
                    entity -> entity instanceof LivingEntity && !entity.equals(player)
            );
            if (result == null || !(result.getHitEntity() instanceof LivingEntity)) {
                sender.sendMessage("§cNo mob found in your line of sight (max 10 blocks).");
                return;
            }
            mob = (LivingEntity) result.getHitEntity();
            argOffset = 1; // slot = args[1], id = args[2], level = args[3], ...
        }

        EntityEquipment equip = mob.getEquipment();
        if (equip == null) {
            sender.sendMessage("§cThis entity does not support equipment.");
            return;
        }

        String  slotArg    = args[argOffset].toLowerCase();
        String  id         = args[argOffset + 1];
        int     level      = this.getNumI(sender, args[argOffset + 2], -1, true);
        boolean noEnchants = Arrays.stream(args).anyMatch(a -> a.equalsIgnoreCase("-noenchants"));

        int amount = 1;
        int amountArgIdx = argOffset + 3;
        if (args.length > amountArgIdx && !args[amountArgIdx].equalsIgnoreCase("-noenchants")) {
            amount = this.getNumI(sender, args[amountArgIdx], 1);
        }

        // Find material arg (after amount, skipping -noenchants)
        ItemType material = null;
        for (int j = argOffset + 4; j < args.length; j++) {
            if (args[j].equalsIgnoreCase("-noenchants")) continue;
            try {
                material = CodexEngine.get().getItemManager().getItemType(args[j]);
                break;
            } catch (MissingProviderException | MissingItemException ignored) {}
        }

        ItemGeneratorManager igm = this.module instanceof ItemGeneratorManager
                ? (ItemGeneratorManager) this.module : null;
        GeneratorItem generatorItem = igm != null ? igm.getItemById(id) : null;

        for (int i = 0; i < amount; i++) {
            int iLevel = (level == -1) ? Rnd.get(1, 100) : level;
            ItemStack item;
            if (material != null && generatorItem != null) {
                item = generatorItem.create(iLevel, -1, material);
            } else {
                item = DivinityAPI.getItemByModule(this.module, id, iLevel, -1, -1);
            }
            if (item == null) {
                sender.sendMessage("§cFailed to generate item '" + id + "'.");
                return;
            }
            if (noEnchants) LoreUT.removeEnchants(item);

            switch (slotArg) {
                case "head":
                case "helmet":
                    equip.setHelmet(item);
                    break;
                case "chest":
                case "chestplate":
                    equip.setChestplate(item);
                    break;
                case "legs":
                case "leggings":
                    equip.setLeggings(item);
                    break;
                case "feet":
                case "boots":
                    equip.setBoots(item);
                    break;
                case "hand":
                case "mainhand":
                    equip.setItemInMainHand(item);
                    break;
                case "offhand":
                    equip.setItemInOffHand(item);
                    break;
                default:
                    sender.sendMessage("§cUnknown slot '" + slotArg + "'. Use: head, chest, legs, feet, hand, offhand.");
                    return;
            }
        }

        EntityStats.get(mob).updateInventory();

        sender.sendMessage("§aEquipped §f" + id + "§a (lv §f" + level + "§a) on §f"
                + (mob.getCustomName() != null ? mob.getCustomName() : mob.getType().name())
                + "§a in slot §f" + slotArg + "§a.");
    }

    private static UUID tryParseUUID(String s) {
        try {
            return UUID.fromString(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

package su.nightexpress.quantumrpg.hooks.external;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.endlesscode.rpginventory.api.InventoryAPI;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.api.ItemAPI;
import su.nightexpress.quantumrpg.config.Config;
import su.nightexpress.quantumrpg.hooks.EHook;
import su.nightexpress.quantumrpg.hooks.Hook;
import su.nightexpress.quantumrpg.utils.ItemUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RPGInvHook extends Hook {
    public RPGInvHook(EHook type, QuantumRPG plugin) {
        super(type, plugin);
    }

    public void setup() {
    }

    public void shutdown() {
    }

    public ItemStack[] getEquip(Player p) {
        List<ItemStack> list1 = new ArrayList<>();
        List<ItemStack> list2 = new ArrayList<>();
        list1.addAll(InventoryAPI.getActiveItems(p));
        list1.addAll(InventoryAPI.getPassiveItems(p));
        if (p.getEquipment().getItemInOffHand() != null && (
                Config.allowAttributesToOffHand() || p.getEquipment().getItemInOffHand().getType() == Material.SHIELD))
            list1.add(p.getEquipment().getItemInOffHand());
        if (p.getEquipment().getItemInMainHand() != null && !ItemUtils.isArmor(p.getEquipment().getItemInMainHand()))
            list1.add(p.getEquipment().getItemInMainHand());
        list1.addAll(Arrays.asList(p.getInventory().getArmorContents()));
        for (ItemStack iii : list1) {
            if (ItemAPI.getDurabilityMinOrMax(iii, 0) == 0)
                continue;
            list2.add(iii);
        }
        return list2.<ItemStack>toArray(new ItemStack[list2.size()]);
    }
}

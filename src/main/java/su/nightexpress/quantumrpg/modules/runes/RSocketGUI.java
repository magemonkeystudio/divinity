package su.nightexpress.quantumrpg.modules.runes;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.quantumrpg.config.Lang;
import su.nightexpress.quantumrpg.gui.ContentType;
import su.nightexpress.quantumrpg.gui.GUIItem;
import su.nightexpress.quantumrpg.gui.SocketGUI;
import su.nightexpress.quantumrpg.modules.QModuleSocket;
import su.nightexpress.quantumrpg.types.DestroyType;
import su.nightexpress.quantumrpg.utils.Utils;

import java.util.LinkedHashMap;

public class RSocketGUI extends SocketGUI {
    public RSocketGUI(QModuleSocket m, String title, int size, LinkedHashMap<String, GUIItem> items, int item_slot, int source_slot, int result_slot) {
        super(m, title, size, items, item_slot, source_slot, result_slot);
    }

    public boolean click(Player p, ItemStack item, ContentType type, int slot, InventoryClickEvent e) {
        Inventory inv = e.getInventory();
        RuneManager m = (RuneManager) this.m;
        if (type == ContentType.ACCEPT) {
            ItemStack target = new ItemStack(inv.getItem(getItemSlot()));
            ItemStack rune = new ItemStack(inv.getItem(getSourceSlot()));
            ItemStack result = new ItemStack(inv.getItem(getResultSlot()));
            inv.setItem(getItemSlot(), null);
            inv.setItem(getSourceSlot(), null);
            int random = Utils.randInt(0, 100);
            int chance = m.getSocketRate(rune);
            if (chance < random) {
                int i;
                DestroyType dt = m.getSettings().getDestroyType();
                switch (dt) {
                    case ITEM:
                        m.out(p, Lang.Runes_Enchanting_Failure_Item.toMsg());
                        break;
                    case SOURCE:
                        inv.setItem(getItemSlot(), target);
                        m.out(p, Lang.Runes_Enchanting_Failure_Source.toMsg());
                        break;
                    case CLEAR:
                        for (i = 0; i < m.getItemRunesAmount(target); i++)
                            target = m.extractSocket(target, 0);
                        Utils.addItem(p, target);
                        m.out(p, Lang.Runes_Enchanting_Failure_Clear.toMsg());
                        break;
                    case BOTH:
                    default:
                        m.out(p, Lang.Runes_Enchanting_Failure_Both.toMsg());
                        break;
                }
                p.closeInventory();
                m.getSettings().playSound(p, false);
                m.getSettings().playEffect(p, false);
                return false;
            }
            Utils.addItem(p, result);
            m.out(p, Lang.Runes_Enchanting_Success.toMsg());
            p.closeInventory();
            m.getSettings().playSound(p, true);
            m.getSettings().playEffect(p, true);
            return false;
        }
        if (type == ContentType.EXIT) {
            m.out(p, Lang.Runes_Enchanting_Cancel.toMsg());
            p.closeInventory();
        }
        return false;
    }
}

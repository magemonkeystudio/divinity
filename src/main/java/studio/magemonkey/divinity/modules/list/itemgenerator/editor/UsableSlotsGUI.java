package studio.magemonkey.divinity.modules.list.itemgenerator.editor;

import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.entity.Player;
import studio.magemonkey.codex.manager.api.menu.Slot;

import java.util.ArrayList;
import java.util.List;

public class UsableSlotsGUI extends AbstractEditorGUI {

    private static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET,
            EquipmentSlot.HAND,
            EquipmentSlot.OFF_HAND
    };

    public UsableSlotsGUI(Player player, ItemGeneratorReference itemGenerator) {
        super(player,
                1,
                "Editor/" + EditorGUI.ItemType.USABLE_SLOTS.getTitle(),
                itemGenerator);
    }

    @Override
    public void setContents() {
        List<String> configuredSlots = itemGenerator.getConfig()
                .getStringList(EditorGUI.ItemType.USABLE_SLOTS.getPath());

        int i = 0;
        for (EquipmentSlot equipmentSlot : SLOTS) {
            String slotName  = equipmentSlot.name();
            boolean isActive = configuredSlots.stream()
                    .anyMatch(s -> s.equalsIgnoreCase(slotName));

            Material icon = isActive ? Material.LIME_DYE : Material.GRAY_DYE;
            String   status = isActive ? "&aEnabled" : "&cDisabled";

            this.setSlot(i, new Slot(createItem(icon,
                    "&e" + slotName,
                    "&bStatus: " + status,
                    "&6Left-Click: &eToggle")) {
                @Override
                public void onLeftClick() {
                    List<String> current = new ArrayList<>(itemGenerator.getConfig()
                            .getStringList(EditorGUI.ItemType.USABLE_SLOTS.getPath()));
                    boolean found = current.removeIf(s -> s.equalsIgnoreCase(slotName));
                    if (!found) {
                        current.add(slotName);
                    }
                    if (current.isEmpty()) {
                        itemGenerator.getConfig().remove(EditorGUI.ItemType.USABLE_SLOTS.getPath());
                    } else {
                        itemGenerator.getConfig().set(EditorGUI.ItemType.USABLE_SLOTS.getPath(), current);
                    }
                    saveAndReopen();
                }
            });
            i++;
        }
    }
}

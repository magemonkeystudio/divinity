package studio.magemonkey.divinity.modules.list.itemgenerator.generators;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.magemonkey.codex.config.api.JYML;
import studio.magemonkey.codex.manager.api.ClickType;
import studio.magemonkey.codex.util.DataUT;
import studio.magemonkey.codex.util.ItemUT;
import studio.magemonkey.codex.util.StringUT;
import studio.magemonkey.codex.util.random.Rnd;
import studio.magemonkey.divinity.Divinity;
import studio.magemonkey.divinity.modules.list.itemgenerator.ItemGeneratorManager;
import studio.magemonkey.divinity.modules.list.itemgenerator.ItemGeneratorManager.GeneratorItem;
import studio.magemonkey.divinity.modules.list.itemgenerator.api.AbstractAttributeGenerator;
import studio.magemonkey.divinity.utils.LoreUT;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class AbilityGenerator extends AbstractAttributeGenerator {
    public static NamespacedKey                         LEGACY_KEY;
    public static NamespacedKey                         ABILITY_KEY;
    public static NamespacedKey                         SKILL_LORE_KEY;
    private final Map<AbilityGenerator.Ability, Double> abilities;

    public AbilityGenerator(@NotNull Divinity plugin,
                            @NotNull GeneratorItem generatorItem,
                            @NotNull String placeholder) {
        super(plugin, generatorItem, placeholder);
        AbilityGenerator.LEGACY_KEY = NamespacedKey.fromString("prorpgitems:skills");
        AbilityGenerator.ABILITY_KEY = NamespacedKey.fromString("skills", plugin);
        AbilityGenerator.SKILL_LORE_KEY = NamespacedKey.fromString("skill-lore", plugin);

        JYML   cfg  = this.generatorItem.getConfig();
        String path = "generator.skills.";

        this.minAmount = cfg.getInt(path + "minimum");
        this.maxAmount = Math.min(cfg.getInt(path + "maximum"), ClickType.values().length);

        this.abilities = new HashMap<>();
        for (String abilityId : cfg.getSection(path + "list")) {
            String path2 = path + "list." + abilityId + ".";

            double chance = cfg.getDouble(path2 + "chance");
            if (chance <= 0) {
                continue;
            }

            AbilityGenerator.Ability ability = new Ability(abilityId,
                    cfg.getInt(path2 + "min-level", 1),
                    cfg.getInt(path2 + "max-level", 1),
                    cfg.getStringList(path2 + "lore-format"));
            this.abilities.put(ability, chance);
        }
    }

    public static void updateNamespace(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        String[] abilityArray = DataUT.getStringArrayData(item, LEGACY_KEY);
        if (abilityArray == null) return;

        // Replace the legacy keyed ability with the new one
        DataUT.setData(item, ABILITY_KEY, abilityArray);
    }

    @Nullable
    public Ability getAbility(String id) {
        return this.abilities.keySet()
                .stream()
                .filter(ability -> ability.getId().equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void generate(@NotNull ItemStack item, int itemLevel) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }

        int min = this.getMinAmount();
        int max = this.getMaxAmount();

        if (max == 0 || this.abilities.isEmpty()) {
            LoreUT.replacePlaceholder(item, placeholder, null);
            return;
        }

        Map<AbilityGenerator.Ability, Double> abilityMap = new HashMap<>(this.abilities);

        // Min: -1, Max: 5
        // Roll: <=5
        //
        // Min: -1, Max: -1
        // Roll: Unlimited
        //
        // Min: 3, Max: -1
        // Roll: >3
        //
        // Min: 2, Max: 5
        // Roll: (2, 5);
        boolean isMaxUnlimited = (max < 0);
        boolean isMinUnlimited = (min < 0);
        int     rollMax        = isMaxUnlimited ? Integer.MAX_VALUE - 1 : max;
        int     rollMin        = isMinUnlimited ? Rnd.get(rollMax + 1) : min;
        int     roll           = Rnd.get((isMaxUnlimited ? rollMax : rollMin), rollMax);

        // If get stats number is 0
        // Remove all stat placeholders
        if (roll <= 0) {
            LoreUT.replacePlaceholder(item, this.placeholder, null);
            return;
        }

        Map<AbilityGenerator.Ability, Integer> abilityAdd = new HashMap<>();

        for (int count = 0; count < roll; count++) {
            if (abilityMap.isEmpty()) {
                break;
            }

            AbilityGenerator.Ability ability = Rnd.getRandomItem(abilityMap, true);
            if (ability == null) {
                break;
            }

            // Minimal stats are added, so we can process chances
            if (count >= rollMin) {
                // If stats are not limited, then we will check
                // for a chance to apply on item manually.
                double chance = abilityMap.get(ability);
                if (Rnd.get(true) > chance) {
                    abilityMap.remove(ability);
                    continue;
                }
            }

            // Add ability lore to the item
            int level = ability.getRndLevel();
            abilityAdd.put(ability, level);
            abilityMap.remove(ability);
        }

        item.setItemMeta(meta);

        int      i            = 0;
        String[] abilityArray = new String[abilityAdd.size()];
        for (Map.Entry<AbilityGenerator.Ability, Integer> entry : abilityAdd.entrySet()) {
            abilityArray[i] = entry.getKey().getId() + ':' + entry.getValue();
            i++;
        }
        DataUT.setData(item, ABILITY_KEY, abilityArray);

        updateLore(item);
    }

    public static Map<String, Integer> getAbilities(ItemStack item) {
        Map<String, Integer> map = new HashMap<>();
        if (item == null) {
            return map;
        }
        String[] stringAbilities = DataUT.getStringArrayData(item, AbilityGenerator.ABILITY_KEY);
        if (stringAbilities == null) {
            return map;
        }
        for (String stringAbility : stringAbilities) {
            int i = stringAbility.lastIndexOf(':');
            int level;
            try {
                level = Integer.parseInt(stringAbility.substring(i + 1));
            } catch (NumberFormatException e) {
                continue;
            }
            map.put(stringAbility.substring(0, i), level);
        }
        return map;
    }

    public void updateLore(ItemStack item) {
        Map<String, Integer> abilities = getAbilities(item);
        if (abilities.isEmpty()) return;

        List<Ability> abilityList = this.abilities.keySet().stream()
                .filter(ability -> abilities.containsKey(ability.getId()))
                .collect(Collectors.toList());

        updateLore(item, abilities, abilityList);
    }

    public static void updateLore(ItemStack item, Map<String, Integer> abilities, List<Ability> abilityList) {
        if (abilityList.isEmpty()) return;

        // At this point, we have a list of Abilities, so we just need to get their lore formats and update the item's lore for them

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> lore = meta.getLore();
        if (lore == null) return;

        StringBuilder loreTag     = new StringBuilder();
        String        storedTag   = ItemUT.getLoreTag(item, SKILL_LORE_KEY.getKey());
        String[]      storedLines = storedTag != null ? storedTag.split(LoreUT.TAG_SPLITTER) : new String[]{};
        int           pos         = lore.indexOf(ItemGeneratorManager.PLACE_GEN_ABILITY);

        // If we don't have a placeholder (meaning this is an existing item)
        if (pos < 0) {
            // Delete the old lines, but first we have to find the first stored one
            if (storedLines.length > 0) {
                int firstIndex = -1;
                for (String storedLine : storedLines) {
                    firstIndex++;
                    if (!StringUT.colorOff(storedLine).isEmpty()) break;
                }

                int index = lore.indexOf(storedLines[firstIndex]) - firstIndex;
                if (index >= 0) {
                    pos = index;
                    for (int count = 0; count < storedLines.length; count++) {
                        lore.remove(index);
                    }
                }
            }
            if (pos < 0) return; // Still -1, so we can't add new lines
        } else lore.remove(pos); // Otherwise, we'll remove it so we can add new lines at the position

        for (Ability ability : abilityList) {
            int level = abilities.get(ability.getId());
            for (String format : ability.getLoreFormat()) {
                String loreLine = format.replace("%level%", String.valueOf(level));
                pos = LoreUT.addToLore(lore, pos, loreLine);

                loreTag.append(loreLine).append(LoreUT.TAG_SPLITTER);
            }
        }


        meta.setLore(lore);
        item.setItemMeta(meta);
        if (loreTag.length() > 0) {
            // Remove the last splitter
            loreTag.setLength(loreTag.length() - LoreUT.TAG_SPLITTER.length());
            ItemUT.addLoreTag(item, SKILL_LORE_KEY.getKey(), loreTag.toString());
        }
    }

    public static class Ability {
        private final String       id;
        private final int          minLevel;
        private final int          maxLevel;
        private final List<String> loreFormat;

        public Ability(
                @NotNull String id,
                int minLevel,
                int maxLevel,
                @NotNull List<String> loreFormat
        ) {
            this.id = id.toLowerCase();
            this.minLevel = minLevel;
            this.maxLevel = maxLevel;
            this.loreFormat = StringUT.color(loreFormat);
        }

        @NotNull
        public String getId() {
            return id;
        }

        public int getRndLevel() {return Rnd.get(minLevel, maxLevel);}

        @NotNull
        public List<String> getLoreFormat() {
            return loreFormat;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Ability ability = (Ability) o;
            return id.equals(ability.id);
        }

        @Override
        public int hashCode() {return Objects.hash(id);}
    }
}

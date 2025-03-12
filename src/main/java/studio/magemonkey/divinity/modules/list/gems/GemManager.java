package studio.magemonkey.divinity.modules.list.gems;

import net.citizensnpcs.api.trait.TraitInfo;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.magemonkey.codex.config.api.JYML;
import studio.magemonkey.codex.hooks.external.citizens.CitizensHK;
import studio.magemonkey.codex.util.DataUT;
import studio.magemonkey.codex.util.StringUT;
import studio.magemonkey.divinity.Divinity;
import studio.magemonkey.divinity.modules.EModule;
import studio.magemonkey.divinity.modules.SocketItem;
import studio.magemonkey.divinity.modules.api.socketing.ModuleSocket;
import studio.magemonkey.divinity.modules.list.gems.GemManager.Gem;
import studio.magemonkey.divinity.modules.list.gems.merchant.MerchantTrait;
import studio.magemonkey.divinity.modules.list.itemgenerator.ItemGeneratorManager;
import studio.magemonkey.divinity.modules.list.itemgenerator.generators.AbilityGenerator;
import studio.magemonkey.divinity.stats.bonus.BonusMap;
import studio.magemonkey.divinity.stats.items.ItemStats;

import java.util.*;

public class GemManager extends ModuleSocket<Gem> {

    public GemManager(@NotNull Divinity plugin) {
        super(plugin, Gem.class);
    }

    @Override
    @NotNull
    public String getId() {
        return EModule.GEMS;
    }

    @Override
    @NotNull
    public String version() {
        return "1.76";
    }

    @Override
    public void setup() {

    }

    @Override
    public void shutdown() {

    }

    @Override
    protected void onPostSetup() {
        super.onPostSetup();

        CitizensHK citizensHook = plugin.getCitizens();
        if (citizensHook != null) {
            TraitInfo trait = TraitInfo.create(MerchantTrait.class);
            citizensHook.registerTrait(plugin, trait);
        }
    }

    @Override
    @NotNull
    public ItemStack insertSocket(@NotNull ItemStack item, @NotNull ItemStack src) {
        item = super.insertSocket(item, src);
        Gem gem = this.getModuleItem(src);

        if (gem == null) return item;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        int gemLevel = ItemStats.getLevel(src);
        gem.applyAbilities(item, gemLevel);

        return item;
    }

    @Override
    @NotNull
    public List<ItemStack> extractSocket(@NotNull ItemStack target, @NotNull String socketId, int index) {
        List<ItemStack> items = super.extractSocket(target, socketId, index);
        Gem             gem   = this.getModuleItem(target);

        if (gem == null) return items;

        ItemStack result   = items.get(0);
        int       gemLevel = ItemStats.getLevel(target);
        gem.removeAbilities(result, gemLevel);

        return items;
    }

    // -------------------------------------------------------------------- //
    // CLASSES

    public class Gem extends SocketItem {
        private final TreeMap<Integer, BonusMap>                   bonusMap;
        private final Map<Integer, List<AbilityGenerator.Ability>> abilitiesByLevel;

        public Gem(@NotNull Divinity plugin, @NotNull JYML cfg) {
            super(plugin, cfg, GemManager.this);

            this.bonusMap = new TreeMap<>();
            this.abilitiesByLevel = new HashMap<>();
            for (String sLvl : cfg.getSection("bonuses-by-level")) {
                int lvl = StringUT.getInteger(sLvl, -1);
                if (lvl < 1) {
                    continue;
                }
                String   path = "bonuses-by-level." + sLvl + ".";
                BonusMap bMap = new BonusMap();
                bMap.loadStats(cfg, path + "item-stats");
                bMap.loadDamages(cfg, path + "damage-types");
                bMap.loadDefenses(cfg, path + "defense-types");

                this.bonusMap.put(lvl, bMap);

                ConfigurationSection skillsSection = cfg.getConfigurationSection(path + "skills");
                if (skillsSection != null) {
                    this.loadAbilities(skillsSection, lvl);
                }
            }

        }

        private void loadAbilities(@NotNull ConfigurationSection section, int lvl) {
            List<AbilityGenerator.Ability> abilities =
                    this.abilitiesByLevel.computeIfAbsent(lvl, k -> new ArrayList<>());
            for (String skillId : section.getKeys(false)) {
                String path       = skillId + ".";
                int    skillLevel = section.getInt(path + "level", 1);
                AbilityGenerator.Ability ability = new AbilityGenerator.Ability(
                        skillId,
                        skillLevel,
                        skillLevel,
                        section.getStringList(path + "lore-format")
                );
                abilities.add(ability);
            }
        }

        private void applyAbilities(ItemStack item, int level) {
            List<AbilityGenerator.Ability> abilities =
                    this.abilitiesByLevel.computeIfAbsent(level, k -> new ArrayList<>());
            if (abilities.isEmpty()) return;

            // Start off with our old abilities and then apply the new ones on top
            Map<String, AbilityGenerator.AbilityInfo> itemAbilities = AbilityGenerator.getAbilities(item);
            List<AbilityGenerator.Ability>            abilitiesList = new ArrayList<>();
            // Only add any abilities that aren't already on the item
            for (AbilityGenerator.Ability ability : abilities) {
                if (ability == null) continue;

                String abilityId = ability.getId();
                if (!itemAbilities.containsKey(abilityId)) {
                    itemAbilities.put(ability.getId(),
                            new AbilityGenerator.AbilityInfo(ability.getId(), ability.getRndLevel(), "gem"));
                    abilitiesList.add(ability);
                }
            }

            // Apply the new abilities to the item
            int      i            = 0;
            String[] abilityArray = new String[itemAbilities.size()];
            for (Map.Entry<String, AbilityGenerator.AbilityInfo> entry : itemAbilities.entrySet()) {
                abilityArray[i] = entry.getKey() + ":" + entry.getValue() + ":" + entry.getValue().getSource();
                i++;
            }
            DataUT.setData(item, AbilityGenerator.ABILITY_KEY, abilityArray);
            updateItemLore(item, itemAbilities, abilitiesList);
        }

        /**
         * Removes the abilities from the item that are associated with the gem at the given level.
         *
         * @param item  The item to remove the abilities from.
         * @param level The level of the gem.
         */
        private void removeAbilities(ItemStack item, int level) {
            List<AbilityGenerator.Ability> abilities = this.abilitiesByLevel.get(level);
            if (abilities == null) return;

            Map<String, AbilityGenerator.AbilityInfo> itemAbilities = AbilityGenerator.getAbilities(item);
            if (itemAbilities.isEmpty()) return;

            List<AbilityGenerator.Ability> abilitiesList = new ArrayList<>();
            for (AbilityGenerator.Ability ability : abilities) {
                if (ability == null) continue;

                String abilityId = ability.getId();
                if (itemAbilities.containsKey(abilityId) && itemAbilities.get(abilityId).getSource().equals("gem")) {
                    itemAbilities.remove(abilityId);
                }
            }

            int      i            = 0;
            String[] abilityArray = new String[itemAbilities.size()];
            for (Map.Entry<String, AbilityGenerator.AbilityInfo> entry : itemAbilities.entrySet()) {
                abilityArray[i] =
                        entry.getKey() + ":" + entry.getValue().getLevel() + ":" + entry.getValue().getSource();
                i++;
            }
            DataUT.setData(item, AbilityGenerator.ABILITY_KEY, abilityArray);
            updateItemLore(item, itemAbilities, abilitiesList);
        }

        private void updateItemLore(ItemStack item,
                                    Map<String, AbilityGenerator.AbilityInfo> itemAbilities,
                                    List<AbilityGenerator.Ability> abilitiesList) {
            ItemGeneratorManager.GeneratorItem genItem = Divinity.getInstance().getModuleCache().getTierManager()
                    .getModuleItem(item);
            if (genItem != null) {
                AbilityGenerator generator =
                        new AbilityGenerator(Divinity.getInstance(), genItem, ItemGeneratorManager.PLACE_GEN_ABILITY);
                // Abilities could be from the item or the gem... so we need to get the abilities from the item
                // and then add the gem abilities to the list
                int i = 0;
                for (String s : itemAbilities.keySet()) {
                    AbilityGenerator.Ability ability = generator.getAbility(s);
                    if (ability != null) {
                        abilitiesList.add(i++, ability);
                    }
                }
            }

            AbilityGenerator.updateLore(item, itemAbilities, abilitiesList);
        }

        @Nullable
        public BonusMap getBonusMap(int lvl) {
            Map.Entry<Integer, BonusMap> e = this.bonusMap.floorEntry(lvl);
            if (e == null) return null;

            return e.getValue();
        }

        @Override
        @NotNull
        protected ItemStack build(int lvl, int uses, int suc) {
            ItemStack item = super.build(lvl, uses, suc);

            ItemMeta meta = item.getItemMeta();
            if (meta == null) return item;

            BonusMap bMap = this.getBonusMap(lvl);
            if (bMap == null) return item;

            if (meta.hasDisplayName()) {
                String name = bMap.replacePlaceholders(meta.getDisplayName());
                meta.setDisplayName(name);
            }

            List<String> lore = meta.getLore();
            if (lore != null) {
                lore.replaceAll(bMap::replacePlaceholders);
                meta.setLore(lore);
            }
            item.setItemMeta(meta);

            return item;
        }
    }
}

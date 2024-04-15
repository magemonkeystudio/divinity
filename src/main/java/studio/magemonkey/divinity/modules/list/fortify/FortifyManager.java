package studio.magemonkey.divinity.modules.list.fortify;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.magemonkey.codex.config.api.JYML;
import studio.magemonkey.codex.modules.IModule;
import studio.magemonkey.codex.util.DataUT;
import studio.magemonkey.codex.util.ItemUT;
import studio.magemonkey.codex.util.StringUT;
import studio.magemonkey.codex.util.random.Rnd;
import studio.magemonkey.divinity.Divinity;
import studio.magemonkey.divinity.config.EngineCfg;
import studio.magemonkey.divinity.modules.EModule;
import studio.magemonkey.divinity.modules.LimitedItem;
import studio.magemonkey.divinity.modules.api.QModuleDrop;
import studio.magemonkey.divinity.modules.list.fortify.FortifyManager.FortifyItem;
import studio.magemonkey.divinity.modules.list.fortify.command.FortifyCmd;
import studio.magemonkey.divinity.modules.list.fortify.command.UnfortifyCmd;
import studio.magemonkey.divinity.stats.items.ItemStats;
import studio.magemonkey.divinity.utils.LoreUT;

import java.util.*;

public class FortifyManager extends QModuleDrop<FortifyItem> {

    private static final List<String>        LORE_KEY_FORTIFY = List.of("fortify", "qrpg_fortify");
    private static final List<NamespacedKey> META_KEY_FORTIFY = List.of(
            new NamespacedKey(Divinity.getInstance(), "FORTIFY_PROTECTION"),
            Objects.requireNonNull(NamespacedKey.fromString("quantumrpg:FORTIFY_PROTECTION")),
            Objects.requireNonNull(NamespacedKey.fromString("quantumrpg:QRPG_FORTIFY_PROTECTION")),
            Objects.requireNonNull(NamespacedKey.fromString("quantumrpg:qrpg_fortify_protection")));
    private              boolean             formatNameAsPrefix;
    private              String              formatNameText;
    private              List<String>        formatLoreFormat;

    public FortifyManager(@NotNull Divinity plugin) {
        super(plugin, FortifyItem.class);
    }

    @Override
    @NotNull
    public String getId() {
        return EModule.FORTIFY;
    }

    @Override
    @NotNull
    public String version() {
        return "1.2.0";
    }

    @Override
    public void setup() {
        String path = "format.";
        this.formatNameAsPrefix = cfg.getBoolean(path + "item-name.as-prefix");
        this.formatNameText = StringUT.color(cfg.getString(path + "item-name.format", ""));
        this.formatLoreFormat = StringUT.color(cfg.getStringList(path + "item-lore.format"));

        this.moduleCommand.addSubCommand(new FortifyCmd(this));
        this.moduleCommand.addSubCommand(new UnfortifyCmd(this));
    }

    @Override
    public void shutdown() {
        if (this.formatLoreFormat != null) {
            this.formatLoreFormat.clear();
            this.formatLoreFormat = null;
        }
    }

    public void deformatItemName(@NotNull ItemStack item) {
        if (!this.isFortified(item)) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        String name = this.getNameDeformatted(item, ItemUT.getItemName(item));
        meta.setDisplayName(name);
        item.setItemMeta(meta);
    }

    public void formatItemName(@NotNull ItemStack item) {
        if (!this.isFortified(item)) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        String name = this.getNameFormatted(item, this.getFortifyLevel(item));
        meta.setDisplayName(name);
        item.setItemMeta(meta);
    }

    @NotNull
    public String getNameDeformatted(@NotNull ItemStack item, @NotNull String name) {
        for (String key : LORE_KEY_FORTIFY) {
            String storedName = ItemUT.getNameTag(item, key);
            if (storedName != null) return name.replace(storedName, "");
        }
        return name;
    }

    @NotNull
    public String getNameFormatted(@NotNull ItemStack item, int lvl) {
        String name = ItemUT.getItemName(item);
        if (this.formatNameText.isEmpty()) return name;

        // Add refine lvl to item name
        String format = this.formatNameText.replace("%fortify-level%", String.valueOf(lvl));
        if (this.formatNameAsPrefix) {
            return StringUT.colorFix(new StringBuilder(format).append(name).toString());
        } else {
            return StringUT.colorFix(new StringBuilder(name).append(format).toString());
        }
    }

    @NotNull
    public void fortifyItem(@NotNull ItemStack item, @NotNull FortifyItem stone, int stoneLvl) {
        if (this.isFortified(item)) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        String stoneId = stone.getId();

        String name  = meta.getDisplayName();
        String name2 = this.getNameFormatted(item, stoneLvl);
        meta.setDisplayName(name2);

        String   sep        = EngineCfg.LORE_STYLE_SEPAR_VALUE;
        String   sepColor   = EngineCfg.LORE_STYLE_SEPAR_COLOR;
        String[] sepModules = new String[stone.protectModules.size()];
        int      j          = 0;
        for (String modId : stone.protectModules) {
            IModule<?> mod = plugin.getModuleManager().getModule(modId);
            if (mod != null) {
                sepModules[j] = mod.name();
            }
        }
        String        fModules = LoreUT.getStrSeparated(sepModules, sep, sepColor);
        StringBuilder loreTag  = new StringBuilder();

        if (!this.formatLoreFormat.isEmpty()) {
            int fRate = stone.getFortifyRateByLevel(stoneLvl);

            List<String> lore = new ArrayList<>();
            for (String formatLine : this.formatLoreFormat) {
                if (formatLine.equalsIgnoreCase("%item-lore%")) {
                    List<String> metaLore = meta.getLore();
                    if (metaLore != null) {
                        for (String loreLine : metaLore) {
                            lore.add(loreLine);
                        }
                    }
                    continue;
                }
                String line = StringUT.color(formatLine
                                .replace("%fortify-modules%", fModules)
                                .replace("%fortify-rate%", String.valueOf(fRate)))
                        .replace("%fortify-level%", String.valueOf(stoneLvl));

                lore.add(line);

                if (loreTag.length() > 0) loreTag.append(ItemUT.TAG_SPLITTER);
                loreTag.append(line);
            }
            meta.setLore(lore);
        }

        item.setItemMeta(meta);

        String tagValue = stoneId + ":" + stoneLvl;
        DataUT.setData(item, META_KEY_FORTIFY.get(0), tagValue);

        ItemUT.addLoreTag(item, LORE_KEY_FORTIFY.get(0), loreTag.toString());
        ItemUT.addNameTag(item, LORE_KEY_FORTIFY.get(0), name2.replace(name, ""));
    }

    @NotNull
    public void unfortifyItem(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        String name = this.getNameDeformatted(item, meta.getDisplayName());
        meta.setDisplayName(name);
        item.setItemMeta(meta);

        for (String key : LORE_KEY_FORTIFY) {
            ItemUT.delLore(item, key);
            ItemUT.delNameTag(item, key);
        }
        for (NamespacedKey key : META_KEY_FORTIFY) {
            DataUT.removeData(item, key);
        }
    }

    public boolean tryFortify(@NotNull ItemStack item) {
        FortifyItem stone = this.getFortifyItem(item);
        if (stone == null) return false;

        int lvl = this.getFortifyLevel(item);
        if (lvl < 0) return false;

        return Rnd.get(true) < stone.getFortifyRateByLevel(lvl);
    }

    public boolean canFortify(@NotNull ItemStack item, @NotNull QModuleDrop<?> modType) {
        FortifyItem stone = this.getFortifyItem(item);
        return stone != null && stone.isApplicable(modType);
    }

    public boolean isFortified(@NotNull ItemStack item) {
        return this.getFortifyLevel(item) > 0 && this.getFortifyItem(item) != null;
    }

    @Nullable
    public FortifyItem getFortifyItem(@NotNull ItemStack item) {
        String stoneId = this.getFortifyId(item);
        if (stoneId == null) return null;

        return this.getItemById(stoneId);
    }

    @Nullable
    public String getFortifyId(@NotNull ItemStack item) {
        for (NamespacedKey key : META_KEY_FORTIFY) {
            String data = DataUT.getStringData(item, key);
            if (data != null) return data.split(":")[0];
        }
        return null;
    }

    public int getFortifyLevel(@NotNull ItemStack item) {
        for (NamespacedKey key : META_KEY_FORTIFY) {
            String data = DataUT.getStringData(item, key);
            if (data != null) {
                String[] split = data.split(":");
                return split.length < 2 ? -1 : StringUT.getInteger(split[1], -1);
            }
        }
        return -1;
    }

    @Override
    protected boolean onDragDrop(
            @NotNull Player p,
            @NotNull ItemStack src,
            @NotNull ItemStack target,
            @NotNull FortifyItem fItem,
            @NotNull InventoryClickEvent e) {

        if (this.isFortified(target)) {
            plugin.lang().Fortify_Fortify_Error_Already.send(p);
            return false;
        }

        this.takeChargeClickEvent(p, src, e);

        // Save other items in stack
        // and then return them back to a player
        ItemStack lost = null;
        if (target.getAmount() > 1) {
            lost = new ItemStack(target);
            lost.setAmount(target.getAmount() - 1);
        }

        this.fortifyItem(target, fItem, ItemStats.getLevel(src));
        e.setCurrentItem(target);

        if (lost != null) {
            ItemUT.addItem(p, lost);
        }

        plugin.lang().Fortify_Fortify_Done.send(p);
        return true;
    }


    public class FortifyItem extends LimitedItem {

        private final Set<String>               protectModules;
        private final TreeMap<Integer, Integer> protectChanceMap;

        public FortifyItem(@NotNull Divinity plugin, @NotNull JYML cfg) {
            super(plugin, cfg, FortifyManager.this);

            this.protectModules = new HashSet<>();
            for (String sMod : cfg.getStringList("protection.modules")) {
                IModule<?> mod = plugin.getModuleManager().getModule(sMod);
                if (mod == null || !mod.isLoaded() || !(mod instanceof QModuleDrop<?>)) {
                    error("Invalid module provided in '" + cfg.getFile().getName() + "' !");
                    continue;
                }
                this.protectModules.add(sMod.toLowerCase());
            }

            this.protectChanceMap = new TreeMap<>();
            for (String sLvl : cfg.getSection("protection.chance-by-level")) {
                int lvl = StringUT.getInteger(sLvl, -1);
                if (lvl <= 0) {
                    continue;
                }

                int chance = cfg.getInt("protection.chance-by-level." + sLvl);
                this.protectChanceMap.put(lvl, chance);
            }
        }

        public boolean isApplicable(@NotNull QModuleDrop<?> e) {
            return this.protectModules.contains(e.getId());
        }

        public int getFortifyRateByLevel(int lvl) {
            Map.Entry<Integer, Integer> e = this.protectChanceMap.floorEntry(lvl);
            if (e == null) return 0;

            return e.getValue();
        }

        @Override
        @NotNull
        protected ItemStack build(int lvl, int uses) {
            ItemStack item = super.build(lvl, uses);

            ItemMeta meta = item.getItemMeta();
            if (meta == null) return item;

            int fRate = this.getFortifyRateByLevel(lvl);

            if (meta.hasDisplayName()) {
                meta.setDisplayName(meta.getDisplayName()
                        .replace("%fortify-rate%", String.valueOf(fRate))
                        .replace("%fortify-level%", String.valueOf(lvl)));
            }

            List<String> lore = meta.getLore();
            if (lore != null) {
                lore.replaceAll(str -> str
                        .replace("%fortify-rate%", String.valueOf(fRate))
                        .replace("%fortify-level%", String.valueOf(lvl)));
                meta.setLore(lore);
            }
            item.setItemMeta(meta);

            return item;
        }
    }
}

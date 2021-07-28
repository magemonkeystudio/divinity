package su.nightexpress.quantumrpg.modules.customitems;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.api.DivineItemsAPI;
import su.nightexpress.quantumrpg.api.ItemAPI;
import su.nightexpress.quantumrpg.config.Config;
import su.nightexpress.quantumrpg.config.JYML;
import su.nightexpress.quantumrpg.config.Lang;
import su.nightexpress.quantumrpg.modules.EModule;
import su.nightexpress.quantumrpg.modules.ModuleItem;
import su.nightexpress.quantumrpg.modules.QModuleDrop;
import su.nightexpress.quantumrpg.modules.cmds.MExecutor;
import su.nightexpress.quantumrpg.modules.customitems.events.QuantumPlayerCustomItemUseEvent;
import su.nightexpress.quantumrpg.stats.ItemStat;
import su.nightexpress.quantumrpg.types.*;
import su.nightexpress.quantumrpg.utils.ItemUtils;
import su.nightexpress.quantumrpg.utils.LoreUT;
import su.nightexpress.quantumrpg.utils.NBTUtils;
import su.nightexpress.quantumrpg.utils.Utils;
import su.nightexpress.quantumrpg.utils.logs.LogType;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomItemsManager extends QModuleDrop {
    private HashMap<String, HashMap<String, List<ItemUsageCooldown>>> cds;

    private boolean cancel_interact;

    private boolean cooldown_msg;

    public CustomItemsManager(QuantumRPG plugin, boolean enabled, MExecutor exe) {
        super(plugin, enabled, exe);
    }

    public EModule type() {
        return EModule.CUSTOM_ITEMS;
    }

    public String name() {
        return "Custom Items";
    }

    public String version() {
        return "1.0";
    }

    public boolean isResolvable() {
        return true;
    }

    public void updateCfg() {
    }

    public void setup() {
        this.cds = new HashMap<>();
        setupMain();
    }

    public void shutdown() {
        this.cds = null;
    }

    private void setupMain() {
        this.plugin.getCM().extract("modules/" + getId() + "/items");
        setupItems();
        JYML jYML = this.cfg.getConfig();
        this.cancel_interact = jYML.getBoolean("general.cancel-interact-event");
        this.cooldown_msg = jYML.getBoolean("general.enable-cooldown-message");
    }

    private void setupItems() {
        for (File f : listf(String.valueOf(getPath()) + "/items/")) {
            CustomItem ci = loadFromConfig(f);
            if (ci == null)
                continue;
            this.items.put(ci.getId(), ci);
        }
    }

    private CustomItem loadFromConfig(File f) {
        YamlConfiguration cfg = new YamlConfiguration();
        cfg = YamlConfiguration.loadConfiguration(f);
        String id = f.getName().replace(".yml", "");
        if (id.isEmpty())
            return null;
        HashMap<String, String> enchants = new HashMap<>();
        if (cfg.isConfigurationSection("enchants"))
            for (String en : cfg.getConfigurationSection("enchants").getKeys(false))
                enchants.put(en.toLowerCase(), cfg.getString("enchants." + en));
        String color = cfg.getString("extras.color");
        HashMap<QClickType, ItemUsage> use = new HashMap<>();
        if (cfg.isConfigurationSection("usage"))
            for (String en : cfg.getConfigurationSection("usage").getKeys(false)) {
                QClickType click = null;
                try {
                    click = QClickType.valueOf(en.toUpperCase());
                } catch (IllegalArgumentException ex) {
                    log("Invalid click type '" + en + "' in custom item '" + id + "'!", LogType.WARN);
                    continue;
                }
                String path = "usage." + en + ".";
                int cd = cfg.getInt(String.valueOf(path) + "cooldown");
                List<String> actions = cfg.getStringList(String.valueOf(path) + "actions");
                ItemUsage iu = new ItemUsage(cd, actions);
                use.put(click, iu);
            }
        CustomItem ci = new CustomItem(
                id,
                "",
                (FileConfiguration) cfg,
                type(),

                enchants,
                color,
                use);
        return ci;
    }

    private void cooldownNotify(Player p, ItemStack item, long left) {
        String name = Utils.getItemName(item);
        String time = Utils.getTime(left);
        String msg = Lang.CustomItems_Cooldown.toMsg().replace("%time%", time).replace("%item%", name);
        out((Entity) p, msg);
    }

    private boolean use(Player p, ItemStack item, QClickType type) {
        String id = getItemId(item);
        CustomItem ci = (CustomItem) getItemById(id, CustomItem.class);
        if (ci == null || ci.getUsage(type) == null)
            return false;
        QuantumPlayerCustomItemUseEvent eve = new QuantumPlayerCustomItemUseEvent(item, p, ci, type);
        this.plugin.getPluginManager().callEvent((Event) eve);
        if (eve.isCancelled())
            return true;
        updateCooldown(p);
        if (isOnCooldown(p, item, type)) {
            if (this.cooldown_msg) {
                long left = getCooldownLeft(p, item, type);
                cooldownNotify(p, item, left);
            }
            return true;
        }
        ItemUsage iu = ci.getUsage(type);
        iu.use(p, item);
        setCooldown(p, item, type);
        return true;
    }

    public void updateCooldown(Player p) {
        String u = p.getUniqueId().toString();
        HashMap<String, List<ItemUsageCooldown>> map = null;
        HashMap<String, List<ItemUsageCooldown>> map_new = new HashMap<>();
        if (this.cds.containsKey(u))
            map = this.cds.get(u);
        if (map == null || map.isEmpty())
            return;
        for (Map.Entry<String, List<ItemUsageCooldown>> e : map.entrySet()) {
            String item_id = e.getKey();
            List<ItemUsageCooldown> list = e.getValue();
            for (ItemUsageCooldown i : list) {
                if (i.isExpired())
                    list.remove(i);
            }
            map_new.put(item_id, list);
        }
        this.cds.put(u, map_new);
    }

    public boolean isOnCooldown(Player p, ItemStack item, QClickType type) {
        if (!isItemOfThisModule(item))
            return false;
        String u = p.getUniqueId().toString();
        HashMap<String, List<ItemUsageCooldown>> map = null;
        if (this.cds.containsKey(u))
            map = this.cds.get(u);
        if (map == null)
            map = new HashMap<>();
        String id = getItemId(item);
        List<ItemUsageCooldown> list = map.get(id);
        if (list == null || list.isEmpty())
            return false;
        for (ItemUsageCooldown i : list) {
            if (i.getClickType() == type)
                return !i.isExpired();
        }
        return false;
    }

    public long getCooldownLeft(Player p, ItemStack item, QClickType type) {
        String u = p.getUniqueId().toString();
        HashMap<String, List<ItemUsageCooldown>> map = this.cds.get(u);
        String id = getItemId(item);
        for (ItemUsageCooldown i : map.get(id)) {
            if (i.getClickType() == type)
                return i.getTimeExpire() - System.currentTimeMillis();
        }
        return 0L;
    }

    public void setCooldown(Player p, ItemStack item, QClickType type) {
        if (!isItemOfThisModule(item))
            return;
        String u = p.getUniqueId().toString();
        HashMap<String, List<ItemUsageCooldown>> map = null;
        if (this.cds.containsKey(u))
            map = this.cds.get(u);
        if (map == null)
            map = new HashMap<>();
        String id = getItemId(item);
        CustomItem ci = (CustomItem) getItemById(id, CustomItem.class);
        ItemUsage iu = ci.getUsage(type);
        if (iu == null || iu.getCooldown() <= 0)
            return;
        ItemUsageCooldown ic = new ItemUsageCooldown(id, type, iu.getCooldown());
        List<ItemUsageCooldown> list = map.get(id);
        if (list == null)
            list = new ArrayList<>();
        for (ItemUsageCooldown i : list) {
            if (i.getClickType() == type)
                list.remove(i);
        }
        list.add(ic);
        map.put(id, list);
        this.cds.put(u, map);
    }

    private ItemStack replaceLore(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore())
            return item;
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        int lvl = -1;
        String[] cls = null;
        for (int line = 0; line < lore.size(); line++) {
            ItemStat stat;
            String[] nums;
            double min, max, amount;
            String val, s = lore.get(line);
            if (!s.startsWith("%") && !s.endsWith("%"))
                continue;
            String str = s.replace("%", "");
            String[] split = str.split(":");
            String type = split[0].toUpperCase();
            if (split.length < 2)
                continue;
            String values = split[1];
            String str1;
            switch ((str1 = type).hashCode()) {
                case -1843719309:
                    if (str1.equals("SOCKET")) {
                        try {
                            QSlotType sock = QSlotType.valueOf(values.toUpperCase());
                            if (sock.getModule() != null && sock.getModule().isActive())
                                lore.set(line, sock.getEmpty());
                        } catch (IllegalArgumentException arrayOfString) {
                            String str2;
                        }
                        break;
                    }
                case -1482182054:

                case -1426230006:
                    if (str1.equals("HAND_TYPE")) {
                        try {
                            WpnHand ammo = WpnHand.valueOf(values.toUpperCase());
                            if (ammo.isEnabled() && ItemUtils.isWeapon(item))
                                lore.set(line, ammo.getFormat());
                        } catch (IllegalArgumentException ammo) {
                        }
                        break;
                    }
                case -1289613814:
                    if (str1.equals("DAMAGE_TYPE")) {
                        if (split.length != 4)
                            break;
                        DamageType at = Config.getDamageTypeById(values);
                        if (at == null)
                            break;
                        String[] nums1 = split[2].split("-");
                        String[] nums2 = split[3].split("-");
                        double d1 = -1.0D, max1 = -1.0D, min2 = -1.0D, max2 = -1.0D;
                        try {
                            d1 = Double.parseDouble(nums1[0]);
                            max1 = Double.parseDouble(nums1[1]);
                            min2 = Double.parseDouble(nums2[0]);
                            max2 = Double.parseDouble(nums2[1]);
                        } catch (NumberFormatException | ArrayIndexOutOfBoundsException numberFormatException) {
                        }
                        double amount1 = Utils.round3(Utils.getRandDoubleNega(d1, max1));
                        double amount2 = Utils.round3(Utils.getRandDoubleNega(min2, max2));
                        String str2 = String.valueOf(at.getFormat()) + Math.min(amount1, amount2) + Config.str_dmgSep + at.getValue() + Math.max(amount1, amount2);
                        lore.set(line, str2);
                        break;
                    }
                case 64205144:
                    if (str1.equals("CLASS")) {
                        cls = values.split(",");
                        if (cls.length <= 0)
                            break;
                        String str2 = LoreUT.getStrSeparated(cls);
                        String lvl_str = Config.str_Req_Cls_User_Single.replace("%class%", String.valueOf(str2)).replace("%state%", Lang.Lore_State_false.toMsg());
                        lore.set(line, lvl_str);
                        break;
                    }
                case 72328036:
                    if (str1.equals("LEVEL")) {
                        String[] arrayOfString = values.split("-");
                        int i = -1, j = -1;
                        try {
                            i = Integer.parseInt(arrayOfString[0]);
                            j = Integer.parseInt(arrayOfString[1]);
                        } catch (NumberFormatException | ArrayIndexOutOfBoundsException numberFormatException) {
                        }
                        lvl = Utils.randInt(i, j);
                        String str2 = Config.str_Req_Lvl_User_Single.replace("%lvl%", String.valueOf(lvl)).replace("%state%", Lang.Lore_State_false.toMsg());
                        lore.set(line, str2);
                        break;
                    }
                case 935098873:

                case 1502321163:
                    if (str1.equals("AMMO_TYPE")) {
                        try {
                            AmmoType ammoType = AmmoType.valueOf(values.toUpperCase());
                            if (ammoType.isEnabled() && item.getType() == Material.BOW)
                                lore.set(line, ammoType.getFormat());
                        } catch (IllegalArgumentException illegalArgumentException) {
                        }
                        break;
                    }
                default:
                    try {
                        stat = ItemStat.valueOf(type);
                    } catch (IllegalArgumentException ex) {
                        String[] arrayOfString;
                        break;
                    }
                    nums = split[1].split("-");
                    min = -1.0D;
                    max = -1.0D;
                    try {
                        min = Double.parseDouble(nums[0].replace("!", "-"));
                        max = Double.parseDouble(nums[1].replace("!", "-"));
                    } catch (NumberFormatException | ArrayIndexOutOfBoundsException d) {
                        NumberFormatException numberFormatException;
                    }
                    amount = Utils.round3(Utils.getRandDoubleNega(min, max));
                    val = ItemUtils.getItemStatString(item, stat, amount);
                    if (val.isEmpty())
                        break;
                    lore.set(line, val);
                    break;
            }
            continue;
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        item = ItemUtils.replaceSoul(item);
        item = ItemUtils.replaceUntrade(item);
        item = ItemUtils.replaceEnchants(item);
        item = ItemUtils.replaceSet(item);
        item = NBTUtils.setItemLevel(item, lvl);
        item = NBTUtils.setItemClass(item, cls);
        meta = item.getItemMeta();
        lore = meta.getLore();
        meta.setLore(lore);
        item.setItemMeta(meta);
        item = this.plugin.getNMS().fixNBT(item);
        return item;
    }

    private List<File> listf(String directoryName) {
        File directory = new File(this.plugin.getDataFolder() + directoryName);
        List<File> files = new ArrayList<>();
        File[] fList = directory.listFiles();
        if (fList == null)
            return files;
        byte b;
        int i;
        File[] arrayOfFile1;
        for (i = (arrayOfFile1 = fList).length, b = 0; b < i; ) {
            File file = arrayOfFile1[b];
            if (file.isFile()) {
                files.add(file);
            } else if (file.isDirectory()) {
                files.addAll(listf("/modules/" + getId() + "/" + file.getName() + "/"));
            }
            b++;
        }
        return files;
    }

    @EventHandler
    public void onItemUse(PlayerInteractEvent e) {
        if (e.isCancelled() && !e.getAction().name().contains("AIR"))
            return;
        Player p = e.getPlayer();
        ItemStack item = e.getItem();
        if (!isItemOfThisModule(item))
            return;
        if (!ItemAPI.canUse(item, p))
            return;
        Action a = e.getAction();
        boolean shift = p.isSneaking();
        QClickType type = QClickType.getFromAction(a, shift);
        if (type == null)
            return;
        if (use(p, item, type))
            e.setCancelled(this.cancel_interact);
    }

    public class ItemUsageCooldown {
        private String id;

        private QClickType click;

        private long time;

        public ItemUsageCooldown(String id, QClickType click, int cd) {
            this.id = id;
            this.click = click;
            this.time = System.currentTimeMillis() + 1000L * cd;
        }

        public String getItemId() {
            return this.id;
        }

        public QClickType getClickType() {
            return this.click;
        }

        public long getTimeExpire() {
            return this.time;
        }

        public boolean isExpired() {
            return (System.currentTimeMillis() > this.time);
        }
    }

    public class ItemUsage {
        private int cd;

        private List<String> acts;

        public ItemUsage(int cd, List<String> acts) {
            this.cd = cd;
            this.acts = acts;
        }

        public int getCooldown() {
            return this.cd;
        }

        public List<String> getActions() {
            return this.acts;
        }

        public void use(Player p, ItemStack item) {
            DivineItemsAPI.executeActions((Entity) p, this.acts, item);
        }
    }

    public class CustomItem extends ModuleItem {
        private Map<String, String> enchants;

        private String color;

        private Map<QClickType, CustomItemsManager.ItemUsage> use;

        public CustomItem(String id, String path, FileConfiguration cfg, EModule module, Map<String, String> enchants, String color, Map<QClickType, CustomItemsManager.ItemUsage> use) {
            super(id, path, cfg, CustomItemsManager.this.type());
            this.enchants = enchants;
            this.color = color;
            if (use == null)
                use = new HashMap<>();
            this.use = use;
        }

        public Map<String, String> getEnchants() {
            return this.enchants;
        }

        public String getColor() {
            return this.color;
        }

        public Map<QClickType, CustomItemsManager.ItemUsage> getUse() {
            return this.use;
        }

        public CustomItemsManager.ItemUsage getUsage(QClickType type) {
            return this.use.get(type);
        }

        protected ItemStack build() {
            ItemStack item = super.build();
            if (item.getType() == Material.AIR)
                return item;
            ItemMeta meta = item.getItemMeta();
            for (Map.Entry<String, String> e : this.enchants.entrySet()) {
                String key = e.getKey();
                Enchantment en = Enchantment.getByName(key.toUpperCase());
                if (en == null) {
                    CustomItemsManager.this.log("Invalid enchantment key '" + key + "' for custom item '" + this.id + "'!", LogType.WARN);
                    continue;
                }
                String[] val = ((String) e.getValue()).split(":");
                int min = -1, max = -1;
                try {
                    min = Integer.parseInt(val[0]);
                    max = Integer.parseInt(val[1]);
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException numberFormatException) {
                }
                if (min >= 0 && max >= 0) {
                    int lvl = Utils.randInt(min, max);
                    meta.addEnchant(en, lvl, true);
                }
            }
            item.setItemMeta(meta);
            if (item.getType().name().startsWith("LEATHER_") && this.color != null && !this.color.isEmpty()) {
                LeatherArmorMeta lm = (LeatherArmorMeta) item.getItemMeta();
                Color c = Color.WHITE;
                String[] s1 = this.color.split(",");
                int r = -1;
                int g = -1;
                int b = -1;
                try {
                    r = Integer.parseInt(s1[0]);
                    g = Integer.parseInt(s1[1]);
                    b = Integer.parseInt(s1[2]);
                } catch (NumberFormatException numberFormatException) {
                }
                if (r >= 0 && g >= 0 && b >= 0) {
                    c = Color.fromRGB(r, g, b);
                    lm.setColor(c);
                    item.setItemMeta((ItemMeta) lm);
                }
            }
            return CustomItemsManager.this.replaceLore(item);
        }
    }
}

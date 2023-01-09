package su.nightexpress.quantumrpg.stats.items;

import mc.promcteam.engine.api.meta.NBTAttribute;
import mc.promcteam.engine.modules.IModule;
import mc.promcteam.engine.utils.DataUT;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.api.QModuleDrop;
import su.nightexpress.quantumrpg.stats.items.api.DuplicableItemLoreStat;
import su.nightexpress.quantumrpg.stats.items.api.ItemLoreStat;
import su.nightexpress.quantumrpg.stats.items.attributes.*;
import su.nightexpress.quantumrpg.stats.items.attributes.SocketAttribute.Type;
import su.nightexpress.quantumrpg.stats.items.attributes.api.AbstractStat;
import su.nightexpress.quantumrpg.stats.items.attributes.api.DoubleStat;
import su.nightexpress.quantumrpg.stats.items.attributes.stats.SimpleStat;
import su.nightexpress.quantumrpg.utils.ItemUtils;

import java.util.*;

public class ItemStats {

    private static final Map<String, DamageAttribute>            DAMAGES          = new LinkedHashMap<>();
    private static final Map<String, DefenseAttribute>           DEFENSES         = new LinkedHashMap<>();
    private static final Map<AbstractStat.Type, AbstractStat<?>> STATS            = new HashMap<>();
    private static final Map<AmmoAttribute.Type, AmmoAttribute>  AMMO             = new HashMap<>();
    private static final Map<HandAttribute.Type, HandAttribute>  HANDS            = new HashMap<>();
    private static final Map<Type, Map<String, SocketAttribute>> SOCKETS          = new HashMap<>();
    private static final Map<String, ItemLoreStat<?>>            ATTRIBUTES       = new HashMap<>();
    private static final Map<String, DuplicableItemLoreStat<?>>  MULTI_ATTRIBUTES = new HashMap<>();
    private static final QuantumRPG                              plugin           = QuantumRPG.getInstance();
    private static final NamespacedKey                           KEY_ID           = new NamespacedKey(plugin, ItemTags.TAG_ITEM_ID);
    private static final NamespacedKey                           KEY_MODULE       = new NamespacedKey(plugin, ItemTags.TAG_ITEM_MODULE);
    private static final NamespacedKey                           KEY_LEVEL        = new NamespacedKey(plugin, ItemTags.TAG_ITEM_LEVEL);
    private static final NamespacedKey                           KEY_SOCKET       = new NamespacedKey(plugin, ItemTags.TAG_ITEM_SOCKET_RATE);
    private static final NamespacedKey                           KEY_ID2          = NamespacedKey.fromString("quantumrpg:" + ItemTags.TAG_ITEM_ID.toLowerCase());
    private static final NamespacedKey                           KEY_MODULE2      = NamespacedKey.fromString("quantumrpg:" + ItemTags.TAG_ITEM_MODULE.toLowerCase());
    private static final NamespacedKey                           KEY_LEVEL2       = NamespacedKey.fromString("quantumrpg:" + ItemTags.TAG_ITEM_LEVEL.toLowerCase());
    private static final NamespacedKey                           KEY_SOCKET2      = NamespacedKey.fromString("quantumrpg:" + ItemTags.TAG_ITEM_SOCKET_RATE.toLowerCase());
    private static       DamageAttribute                         DAMAGE_DEFAULT;
    private static       DefenseAttribute                        DEFENSE_DEFAULT;


    // TODO Register logs

    public static void clear() {
        DAMAGES.clear();
        DEFENSES.clear();
        STATS.clear();
        AMMO.clear();
        HANDS.clear();
        SOCKETS.clear();
        ATTRIBUTES.clear();
        MULTI_ATTRIBUTES.clear();
        DAMAGE_DEFAULT = null;
        DEFENSE_DEFAULT = null;
    }

    public static void registerDamage(@NotNull DamageAttribute dmg) {
        DAMAGES.put(dmg.getId(), dmg);
        if (DAMAGE_DEFAULT == null || dmg.getPriority() >= DAMAGE_DEFAULT.getPriority()) {
            DAMAGE_DEFAULT = dmg;
        }
        // Put default damage at the end of list every time when new damage is added.
        else {
            DAMAGES.remove(DAMAGE_DEFAULT.getId());
            DAMAGES.put(DAMAGE_DEFAULT.getId(), DAMAGE_DEFAULT);
        }
        ItemStats.updateDefenseByDefault();
    }

    public static void registerDefense(@NotNull DefenseAttribute def) {
        DEFENSES.put(def.getId(), def);
        ItemStats.updateDefenseByDefault();
    }

    public static void registerStat(@NotNull AbstractStat<?> stat) {
        if (stat.getCapability() == 0) return; // TODO Log

        STATS.put(stat.getType(), stat);
    }

    public static void registerAmmo(@NotNull AmmoAttribute ammo) {
        AMMO.put(ammo.getType(), ammo);
    }


    public static void registerHand(@NotNull HandAttribute hand) {
        HANDS.put(hand.getType(), hand);
    }

    private static void updateDefenseByDefault() {
        if (DAMAGES.isEmpty()) return;

        for (DamageAttribute dmg : ItemStats.getDamages()) {
            Optional<DefenseAttribute> opt = ItemStats.getDefenses().stream()
                    .filter(def -> def.isBlockable(dmg))
                    .sorted((def1, def2) -> def2.getPriority() - def1.getPriority())
                    .findFirst();

            if (opt.isPresent()) {
                dmg.setAttachedDefense(opt.get());
                //System.out.println("Attached def: " + opt.get().getId() + " to " + dmg.getId() + " dmg");
            }
        }

        DEFENSE_DEFAULT = ItemStats.getDamageByDefault().getAttachedDefense();
    }

    @NotNull
    public static Collection<DamageAttribute> getDamages() {
        return DAMAGES.values();
    }

    @Nullable
    public static DamageAttribute getDamageById(@NotNull String id) {
        return DAMAGES.get(id.toLowerCase());
    }

    @Nullable
    public static DamageAttribute getDamageByCause(@NotNull DamageCause cause) {
        Optional<DamageAttribute> opt = ItemStats.getDamages().stream()
                .filter(dmg -> dmg.isAttached(cause))
                .sorted((dmg1, dmg2) -> {
                    return dmg2.getPriority() - dmg1.getPriority();
                }).findFirst();

        return opt.isPresent() ? opt.get() : null;
    }

    @NotNull
    public static DamageAttribute getDamageByDefault() {
        return DAMAGE_DEFAULT;
    }

    public static boolean hasDamage(@NotNull ItemStack item) {
        return ItemStats.getDamages().stream().anyMatch(dmg -> ItemStats.hasDamage(item, dmg));
    }

    public static boolean hasDamage(@NotNull ItemStack item, @NotNull String id) {
        DamageAttribute dmgType = getDamageById(id);
        if (dmgType == null) return false;

        return ItemStats.hasDamage(item, dmgType);
    }

    public static boolean hasDamage(@NotNull ItemStack item, @NotNull DamageAttribute dmgType) {
        double[] arr = dmgType.getRaw(item);
        return arr != null && arr.length == 2 && arr[0] > 0 && arr[1] > 0;
    }

    public static double getDamageMinOrMax(@NotNull ItemStack item, @NotNull String id, int index) {
        DamageAttribute dmgType = getDamageById(id);
        if (dmgType == null) return 0D;

        return dmgType.getMinOrMax(item, index);
    }

    public static double getDamage(@NotNull ItemStack item, @NotNull String id) {
        DamageAttribute dmgType = getDamageById(id);
        if (dmgType == null) return 0D;

        return dmgType.get(item);
    }

    @NotNull
    public static Collection<DefenseAttribute> getDefenses() {
        return DEFENSES.values();
    }

    @Nullable
    public static DefenseAttribute getDefenseById(@NotNull String id) {
        return DEFENSES.get(id.toLowerCase());
    }

    public static boolean hasDefense(@NotNull ItemStack item, @NotNull String id) {
        DefenseAttribute defType = getDefenseById(id);
        if (defType == null) return false;

        return ItemStats.hasDefense(item, defType);
    }

    public static boolean hasDefense(@NotNull ItemStack item, @NotNull DefenseAttribute defType) {
        Double d = defType.getRaw(item);
        return d != null && d != 0D;
    }

    @Nullable
    public static DefenseAttribute getDefenseByDefault() {
        return DEFENSE_DEFAULT;
    }

    public static double getDefense(@NotNull ItemStack item, @NotNull String id) {
        DefenseAttribute defType = getDefenseById(id);
        if (defType == null) return 0D;

        return defType.get(item);
    }

    @NotNull
    public static Collection<AbstractStat<?>> getStats() {
        return STATS.values();
    }

    @Nullable
    public static AbstractStat<?> getStat(@NotNull AbstractStat.Type type) {
        return STATS.get(type);
    }

    public static double getStat(@NotNull ItemStack item, @NotNull AbstractStat.Type type) {
        AbstractStat<?> stat = getStat(type);
        if (stat == null) return 0D;

        return stat.get(item);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Nullable
    public static <T extends AbstractStat<?>> T getStat(@NotNull Class<T> clazz) {
        for (AbstractStat<?> stat : ItemStats.getStats()) {
            Class<? extends AbstractStat> clazz2 = stat.getClass();
            if (clazz.isAssignableFrom(clazz2)) {
                return (T) stat;
            }
        }
        return null;
    }

    public static boolean hasStat(@NotNull ItemStack item, @NotNull AbstractStat.Type type) {
        AbstractStat<?> stat = ItemStats.getStat(type);
        if (stat == null) return false;

        if (stat instanceof SimpleStat) {
            SimpleStat rs = (SimpleStat) stat;
            Double     d  = rs.getRaw(item);
            return d != null && d != 0D;
        }
        if (stat instanceof DoubleStat) {
            DoubleStat rs  = (DoubleStat) stat;
            double[]   arr = rs.getRaw(item);
            return arr != null && arr.length == 2;// && arr[0] != 0D;
        }

        return stat.getRaw(item) != null;
    }

    // ----------------------------------------------------------------- //

    @NotNull
    public static void updateVanillaAttributes(@NotNull ItemStack item) {
        double hp    = getStat(item, AbstractStat.Type.MAX_HEALTH);
        double speed = getStat(item, AbstractStat.Type.ATTACK_SPEED);
        double move  = getStat(item, AbstractStat.Type.MOVEMENT_SPEED);

        addAttribute(item, NBTAttribute.MAX_HEALTH, hp);
        addAttribute(item, NBTAttribute.MOVEMENT_SPEED, move);
        addAttribute(item, NBTAttribute.ATTACK_SPEED, speed);

//        if (ItemUtils.isWeapon(item)) {
        double vanilla = DamageAttribute.getVanillaDamage(item);
        if (vanilla > 1)
            addAttribute(item, NBTAttribute.ATTACK_DAMAGE, vanilla - 1); // -1 because it adds instead of set
//        }
        if (ItemUtils.isArmor(item)) {
            addAttribute(item, NBTAttribute.ARMOR, DefenseAttribute.getVanillaArmor(item));
            addAttribute(item, NBTAttribute.ARMOR_TOUGHNESS, DefenseAttribute.getVanillaToughness(item));
        }
        ItemMeta im = item.getItemMeta();
        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(im);
    }

    @NotNull
    private static void addAttribute(@NotNull ItemStack item, @NotNull NBTAttribute att, double value) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        // Remove attribute before update with new value
        meta.removeAttributeModifier(att.getAttribute());
        item.setItemMeta(meta);

        // Bows not needed damage tag at all.
        if (att == NBTAttribute.ATTACK_DAMAGE && ItemUtils.isBow(item)) {
            return;
        }

        // Do not add attribute with zero value
        // except the weapons, they will get vanilla speed.
        if (value == 0) {
            if (!(att == NBTAttribute.ATTACK_SPEED && ItemUtils.isWeapon(item))) {
                return;
            }
        }

        // Fine values
        if (att == NBTAttribute.MOVEMENT_SPEED) {
            value = 0.1 * (1D + value / 100D) - 0.1;
        } else if (att == NBTAttribute.ATTACK_SPEED) {
            value /= 100D;
            double baseSpeed = getStat(item, AbstractStat.Type.BASE_ATTACK_SPEED) + AbstractStat.getDefaultAttackSpeed(item);
            double extra     = baseSpeed * value;
            value            = baseSpeed + extra - 4;
        }

        for (EquipmentSlot slot : ItemUtils.getItemSlots(item)) {
            if (slot == EquipmentSlot.OFF_HAND
                    && (att == NBTAttribute.ATTACK_DAMAGE || att == NBTAttribute.ATTACK_SPEED)) continue;

            AttributeModifier am = new AttributeModifier(
                    att.getUUID(slot),
                    att.getNmsName(),
                    value,
                    Operation.ADD_NUMBER,
                    slot);

            meta.removeAttributeModifier(att.getAttribute(), am); // Avoid dupe and error
            meta.addAttributeModifier(att.getAttribute(), am);
        }
        item.setItemMeta(meta);
    }

    @NotNull
    public static Collection<AmmoAttribute> getAmmos() {
        return AMMO.values();
    }

    @Nullable
    public static AmmoAttribute getAmmo(@NotNull AmmoAttribute.Type type) {
        return AMMO.get(type);
    }

    @Nullable
    public static AmmoAttribute getAmmo(@NotNull ItemStack item) {
        for (AmmoAttribute ammo : getAmmos()) {
            String value = ammo.getRaw(item);
            if (value != null) {
                return ammo;
            }
        }
        return null;
    }

    // ================================================================================== //


    @NotNull
    public static Collection<HandAttribute> getHands() {
        return HANDS.values();
    }

    @Nullable
    public static HandAttribute getHand(@NotNull HandAttribute.Type type) {
        return HANDS.get(type);
    }

    @Nullable
    public static HandAttribute getHand(@NotNull ItemStack item) {
        for (HandAttribute hand : getHands()) {
            String value = hand.getRaw(item);
            if (value != null) {
                return hand;
            }
        }
        return null;
    }

    public static void registerAttribute(@NotNull ItemLoreStat<?> att) {
        ATTRIBUTES.put(att.getId(), att);
    }

    @NotNull
    public static Collection<ItemLoreStat<?>> getAttributes() {
        return ATTRIBUTES.values();
    }

    @Nullable
    public static ItemLoreStat<?> getAttribute(@NotNull String id) {
        return ATTRIBUTES.get(id.toLowerCase());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Nullable
    public static <T extends ItemLoreStat<?>> T getAttribute(@NotNull Class<T> clazz) {
        for (ItemLoreStat<?> stat : ItemStats.getAttributes()) {
            Class<? extends ItemLoreStat> clazz2 = stat.getClass();
            if (clazz.isAssignableFrom(clazz2)) {
                return (T) stat;
            }
        }
        return null;
    }

    // ====================================================== //

    public static void registerSocket(@NotNull SocketAttribute socket) {
        SocketAttribute.Type         type = socket.getType();
        Map<String, SocketAttribute> map  = SOCKETS.get(type);
        if (map == null) map = new HashMap<>();

        map.put(socket.getId(), socket);
        SOCKETS.put(type, map);
    }

    @Nullable
    public static SocketAttribute getSocket(@NotNull SocketAttribute.Type type, @NotNull String id) {
        Map<String, SocketAttribute> map = SOCKETS.get(type);
        if (map == null) return null;

        return map.get(id.toLowerCase());
    }

    @NotNull
    public static Collection<SocketAttribute> getSockets(@NotNull SocketAttribute.Type type) {
        Map<String, SocketAttribute> map = SOCKETS.get(type);
        if (map == null) return Collections.emptySet();

        return map.values();
    }


    public static void setId(@NotNull ItemStack item, @NotNull String id) {
        DataUT.setData(item, KEY_ID, id);
    }

    @Nullable
    public static String getId(@NotNull ItemStack item) {
        String data = DataUT.getStringData(item, KEY_ID2);
        return data != null ? data : DataUT.getStringData(item, KEY_ID);
    }

    @NotNull
    public static void setLevel(@NotNull ItemStack item, int lvl) {
        if (lvl < 1) {
            DataUT.removeData(item, KEY_LEVEL);
            DataUT.removeData(item, KEY_LEVEL2);
            return;
        }
        DataUT.setData(item, KEY_LEVEL, lvl);
    }

    public static int getLevel(@NotNull ItemStack item) {
        int data = DataUT.getIntData(item, KEY_LEVEL2);
        return data != 0 ? data : DataUT.getIntData(item, KEY_LEVEL);
    }

    // ======================================================== //

    public static void setModule(@NotNull ItemStack item, @NotNull String mod) {
        DataUT.setData(item, KEY_MODULE, mod);
    }

    @Nullable
    public static QModuleDrop<?> getModule(@NotNull ItemStack item) {
        String data = DataUT.getStringData(item, KEY_MODULE2);
        if (data == null) data = DataUT.getStringData(item, KEY_MODULE);
        if (data == null) return null;

        IModule<?> mod = plugin.getModuleManager().getModule(data);
        if (mod instanceof QModuleDrop<?>) {
            return (QModuleDrop<?>) mod;
        }
        return null;
    }

    public static void setSocketRate(@NotNull ItemStack item, int rate) {
        DataUT.setData(item, KEY_SOCKET, rate);
    }

    public static int getSocketRate(@NotNull ItemStack item) {
        int data = DataUT.getIntData(item, KEY_SOCKET2);
        return data != 0 ? data : DataUT.getIntData(item, KEY_SOCKET);
    }
}

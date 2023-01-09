package su.nightexpress.quantumrpg.config;

import mc.promcteam.engine.config.api.IConfigTemplate;
import mc.promcteam.engine.config.api.JYML;
import mc.promcteam.engine.utils.StringUT;
import mc.promcteam.engine.utils.actions.ActionManipulator;
import mc.promcteam.engine.utils.constants.JStrings;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.stats.items.ItemStats;
import su.nightexpress.quantumrpg.stats.items.attributes.*;
import su.nightexpress.quantumrpg.stats.items.attributes.api.AbstractStat;
import su.nightexpress.quantumrpg.stats.items.attributes.stats.BleedStat;
import su.nightexpress.quantumrpg.stats.items.attributes.stats.DurabilityStat;
import su.nightexpress.quantumrpg.stats.items.attributes.stats.SimpleStat;
import su.nightexpress.quantumrpg.stats.tiers.Tier;
import su.nightexpress.quantumrpg.types.ItemGroup;
import su.nightexpress.quantumrpg.types.ItemSubType;

import java.util.*;

public class Config extends IConfigTemplate {

    public Config(@NotNull QuantumRPG plugin) {
        super(plugin);
    }

    private static Map<String, Tier>        TIERS_MAP;
    private static Map<String, ItemSubType> ITEM_SUB_TYPES;

    @Override
    public void load() {
    	String path = "tiers.";
    	Config.TIERS_MAP = new LinkedHashMap<>();
    	for (String tierId : cfg.getSection("tiers")) {
    		String path2 = "tiers." + tierId + ".";
    		String tierColor = cfg.getString(path2 + "color", "&f");
    		String tierName = cfg.getString(path2 + "name", tierId);
    		
    		Tier tier = new Tier(tierId, tierName, tierColor);
    		TIERS_MAP.put(tier.getId(), tier);
    	}
    	
    	
    	for (ItemGroup itemGroup : ItemGroup.values()) {
    		path = "item-groups." + itemGroup.name() + ".";
    		
    		itemGroup.setName(cfg.getString(path + "name", itemGroup.name()));
    		itemGroup.setMaterials(cfg.getStringSet(path + "materials"));
    	}
    	
    	
    	ITEM_SUB_TYPES = new HashMap<>();
    	for (String typeId : cfg.getSection("item-sub-types")) {
    		path = "item-sub-types." + typeId + ".";
    		String name = cfg.getString(path + "name", typeId);
    		
    		ItemSubType ist = new ItemSubType(typeId, name, cfg.getStringSet(path + "materials"));
    		ITEM_SUB_TYPES.put(ist.getId(), ist);
    	}
    }

    private void setupSockets() {
        JYML cfg = JYML.loadOrExtract(plugin, "/item_stats/sockets.yml");

        for (SocketAttribute.Type type : SocketAttribute.Type.values()) {
            String path = type.name() + ".";

            for (String catId : cfg.getSection(path + "categories")) {
                String path2             = path + "categories." + catId + ".";
                String catTierId         = cfg.getString(path2 + "tier", JStrings.DEFAULT);
                String catName           = cfg.getString(path2 + "name", catId);
                String catFormatMain     = cfg.getString(path2 + "format.main", "%value%");
                String catFormatValEmpty = cfg.getString(path2 + "format.value.empty", "%TIER_COLOR%□ <%name%>");
                String catFormatValFill  = cfg.getString(path2 + "format.value.filled", "%TIER_COLOR%▣ &7%value%");

                Tier catTier = Config.getTier(catTierId);
                if (catTier == null) {
                    plugin.error("Invalid tier '" + catTierId + "' for Socket Attribute '" + catId + "' !");
                    continue;
                }

                SocketAttribute socket = new SocketAttribute(
                        type,

                        catId,
                        catName,
                        catFormatMain,

                        catTier,

                        catFormatValEmpty,
                        catFormatValFill);

                ItemStats.registerSocket(socket);
            }
        }
    }

    @Nullable
    public static Tier getTier(@NotNull String id) {
        return TIERS_MAP.get(id.toLowerCase());
    }

    @NotNull
    public static Collection<Tier> getTiers() {
        return TIERS_MAP.values();
    }

    @NotNull
    public static Set<String> getSubTypeIds() {
        return new HashSet<>(ITEM_SUB_TYPES.keySet());
    }

    @Nullable
    public static ItemSubType getSubTypeById(@NotNull String id) {
        return ITEM_SUB_TYPES.get(id.toLowerCase());
    }

    @Nullable
    public static ItemSubType getItemSubType(@NotNull ItemStack item) {
        return getItemSubType(item.getType());
    }

    @Nullable
    public static ItemSubType getItemSubType(@NotNull Material material) {
        return getItemSubType(material.name());
    }

    @Nullable
    public static ItemSubType getItemSubType(@NotNull String mat) {
        Optional<ItemSubType> opt = ITEM_SUB_TYPES.values().stream().filter(type -> type.isItemOfThis(mat))
                .findFirst();
        return opt.isPresent() ? opt.get() : null;
    }

    @NotNull
    public static Set<Material> getAllRegisteredMaterials() {
        Set<Material> set = new HashSet<>();

        for (ItemGroup group : ItemGroup.values()) {
            for (String materialName : group.getMaterials()) {
                Material material = Material.getMaterial(materialName.toUpperCase());
                if (material != null) {
                    set.add(material);
                }
            }
        }

        return set;
    }
}

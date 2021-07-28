//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package su.nightexpress.quantumrpg.modules.tiers.resources;

import org.bukkit.Material;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.config.Config;
import su.nightexpress.quantumrpg.types.ItemGroup;
import su.nightexpress.quantumrpg.types.ItemSubType;
import su.nightexpress.quantumrpg.utils.Files;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

public class Resources {
    private static final QuantumRPG plugin;
    private static final HashMap<String, Res> res;
    private static final HashMap<String, Res> res_half;
    private static final Set<String> mats;
    private static final Set<Material> mats2;

    static {
        plugin = QuantumRPG.instance;
        res = new HashMap();
        res_half = new HashMap();
        mats = new HashSet();
        mats2 = new HashSet();
    }

    public Resources() {
    }

    public static void setup() {
        ItemGroup[] var3;
        int var2 = (var3 = ItemGroup.values()).length;

        for (int var1 = 0; var1 < var2; ++var1) {
            ItemGroup ig = var3[var1];
            mats.addAll(ig.getMaterials());
        }

        Iterator var5 = mats.iterator();

        while (var5.hasNext()) {
            String s = (String) var5.next();
            Material m = Material.getMaterial(s.toUpperCase());
            if (m != null) {
                mats2.add(m);
            }
        }

        setupMissingConfigs(ResourceType.PREFIX);
        setupMissingConfigs(ResourceType.SUFFIX);
        setupResourcesByTypes();
    }

    public static void setupMissingConfigs(ResourceType rt) {
        String type = rt.name().toLowerCase() + "es";
        Iterator var3 = Files.getFilesFolder("tiers/tiers").iterator();

        String m;
        File f;
        while (var3.hasNext()) {
            m = (String) var3.next();
            f = new File(plugin.getDataFolder() + "/modules/tiers/resources/names/" + type + "/tiers/", m.toLowerCase().replace(".yml", "") + ".txt");
            if (!f.exists()) {
                if (plugin.getResource("modules/tiers/resources/names/" + type + "/tiers/" + m.toLowerCase().replace(".yml", "") + ".txt") != null) {
                    plugin.saveResource("modules/tiers/resources/names/" + type + "/tiers/" + m.toLowerCase().replace(".yml", "") + ".txt", false);
                } else {
                    Files.create(f);
                }
            }
        }

        var3 = mats.iterator();

        while (var3.hasNext()) {
            m = (String) var3.next();
            f = new File(plugin.getDataFolder() + "/modules/tiers/resources/names/" + type + "/materials/", m.toLowerCase() + ".txt");
            if (!f.exists()) {
                if (plugin.getResource("modules/tiers/resources/names/" + type + "/materials/" + m.toLowerCase() + ".txt") != null) {
                    plugin.saveResource("modules/tiers/resources/names/" + type + "/materials/" + m.toLowerCase() + ".txt", false);
                } else {
                    Files.create(f);
                }
            }
        }

        var3 = Config.getSubTypeIds().iterator();

        while (var3.hasNext()) {
            m = (String) var3.next();
            f = new File(plugin.getDataFolder() + "/modules/tiers/resources/names/" + type + "/types/", m.toLowerCase() + ".txt");
            if (!f.exists()) {
                Files.create(f);
            }
        }

    }

    public static List<String> getSource(ResourceType type, ResourceSubType sub, String file) {
        List<String> list = new ArrayList();
        String t1 = type.name().toLowerCase() + "es";
        String t2 = sub.name().toLowerCase() + "s";
        String path = plugin.getDataFolder() + "/modules/tiers/resources/names/" + t1 + "/" + t2 + "/" + file + ".txt";

        try {
            Throwable var7 = null;

            try {
                BufferedReader br = new BufferedReader(new FileReader(path));

                String sCurrentLine;
                try {
                    while ((sCurrentLine = br.readLine()) != null) {
                        list.add(sCurrentLine);
                    }
                } finally {
                    if (br != null) {
                        br.close();
                    }

                }
            } catch (Throwable var18) {
                if (var7 == null) {
                    var7 = var18;
                } else if (var7 != var18) {
                    var7.addSuppressed(var18);
                }

                throw var7;
            }
        } catch (Throwable var19) {
            var19.printStackTrace();
        }

        return list;
    }

    public static void setupResourcesByTypes() {
        Iterator var1 = mats.iterator();

        String sub;
        List<String> suffix;
        while (var1.hasNext()) {
            sub = (String) var1.next();
            String type = sub.toLowerCase();
            List<String> prefix = getSource(ResourceType.PREFIX, ResourceSubType.MATERIAL, type);
            suffix = getSource(ResourceType.SUFFIX, ResourceSubType.MATERIAL, type);
            Res r = new Res(type, prefix, suffix);
            res.put(type, r);
        }

        var1 = Config.getSubTypeIds().iterator();

        while (var1.hasNext()) {
            sub = (String) var1.next();
            List<String> prefix = getSource(ResourceType.PREFIX, ResourceSubType.TYPE, sub);
            suffix = getSource(ResourceType.SUFFIX, ResourceSubType.TYPE, sub);
            Res r = new Res(sub, prefix, suffix);
            res_half.put(sub, r);
        }

    }

    public static List<String> getSourceByMaterial(ResourceType rt, String type) {
        List<String> list = new ArrayList();
        type = type.toLowerCase();
        if (res.containsKey(type)) {
            return rt == ResourceType.PREFIX ? res.get(type).getPrefixes() : res.get(type).getSuffixes();
        } else {
            return list;
        }
    }

    public static List<String> getSourceBySubType(ResourceType rt, String type) {
        List<String> list = new ArrayList();
        type = type.toLowerCase();
        ItemSubType ist = Config.getItemSubType(type);
        if (ist == null) {
            return list;
        } else {
            String id = ist.getId();
            if (res_half.containsKey(id)) {
                return rt == ResourceType.PREFIX ? res_half.get(id).getPrefixes() : res_half.get(id).getSuffixes();
            } else {
                return list;
            }
        }
    }

    public static Set<Material> getAllMaterials() {
        return mats2;
    }

    public static void clear() {
        res.clear();
        res_half.clear();
        mats.clear();
    }
}

package su.nightexpress.quantumrpg.nms;

import org.bukkit.Bukkit;

public class VersionUtils {
    private static Version version;

    private NMS nms;

    public VersionUtils() {
        version = Version.getCurrent();
    }

    public static Version get() {
        return version;
    }

    public boolean setup() {
        if (setNMS()) {
            Bukkit.getConsoleSender().sendMessage("§7> §fServer version: §a" + version.getVersion() + " / OK!");
            return true;
        }
        Bukkit.getConsoleSender().sendMessage("§7> §fServer version: " + version.getVersion() + ". §cUnsupported! Disabling...");
        return false;
    }

    public boolean setNMS() {
        String str = version.getVersion();
        if (str.equals("v1_9_R2")) {
            this.nms = new V1_9_R1();
        } else if (str.equals("v1_10_R1")) {
            this.nms = new V1_10_R1();
        } else if (str.equals("v1_11_R1")) {
            this.nms = new V1_11_R1();
        } else if (str.equals("v1_12_R1")) {
            this.nms = new V1_12_R1();
        }
        return (this.nms != null);
    }

    public NMS getNMS() {
        return this.nms;
    }

    public Class<?> getNmsClass(String nmsClassName) throws ClassNotFoundException {
        return Class.forName("net.minecraft.server." + version.getVersion() + "." + nmsClassName);
    }

    public enum Version {
        v1_9_R2("v1_9_R2", 10),
        v1_10_R1("v1_10_R1", 11),
        v1_11_R1("v1_11_R1", 12),
        v1_12_R1("v1_12_R1", 13);

        private int n;

        private String s;

        Version(String s, int n) {
            this.n = n;
            this.s = s;
        }

        public static Version getCurrent() {
            String[] split = Bukkit.getServer().getClass().getPackage().getName().split("\\.");
            String s = split[split.length - 1];
            Version[] values;
            for (int length = (values = values()).length, i = 0; i < length; i++) {
                Version version = values[i];
                if (version.name().equalsIgnoreCase(s))
                    return version;
            }
            return null;
        }

        public String getVersion() {
            return this.s;
        }

        public int getValue() {
            return this.n;
        }

        public boolean isLower(Version version) {
            return (getValue() < version.getValue());
        }

        public boolean isHigher(Version version) {
            return (getValue() > version.getValue());
        }
    }
}

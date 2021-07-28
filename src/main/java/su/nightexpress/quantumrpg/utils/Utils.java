package su.nightexpress.quantumrpg.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.StringUtil;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.config.Lang;
import su.nightexpress.quantumrpg.modules.QModule;
import su.nightexpress.quantumrpg.utils.logs.LogType;
import su.nightexpress.quantumrpg.utils.logs.LogUtil;

import java.lang.reflect.Field;
import java.util.*;

public class Utils {
    private static final QuantumRPG plugin = QuantumRPG.getInstance();
    public static Random r = new Random();

    public static String getItemName(ItemStack item) {
        String name;
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            name = item.getItemMeta().getDisplayName();
        } else {
            name = plugin.getCM().getDefaultItemName(item);
        }
        return name;
    }

    public static List<String> getSugg(String arg, List<String> source) {
        if (source == null)
            return null;
        List<String> ret = new ArrayList<>();
        List<String> sugg = new ArrayList<>(source);
        StringUtil.copyPartialMatches(arg, sugg, ret);
        Collections.sort(ret);
        return ret;
    }

    public static String getModuleStatus(QModule m) {
        if (m.isActive())
            return "§aActive!";
        return "§cDisabled.";
    }

    public static void addItem(Player p, ItemStack item) {
        if (p.getInventory().firstEmpty() == -1) {
            p.getWorld().dropItem(p.getLocation(), item);
        } else {
            p.getInventory().addItem(item);
        }
    }

    public static ItemStack buildItem(String s) {
        String[] mat = s.split(":");
        Material m = Material.getMaterial(mat[0]);
        if (m == null)
            return null;
        int data = 0;
        if (mat.length >= 2)
            data = Integer.parseInt(mat[1]);
        int amount = 1;
        if (mat.length == 3)
            amount = Integer.parseInt(mat[2]);
        ItemStack item = new ItemStack(m, amount, (short) data);
        return item.clone();
    }

    public static boolean checkEnchantConflict(ItemStack item, Enchantment ee) {
        for (Enchantment e2 : item.getEnchantments().keySet()) {
            if (ee.conflictsWith(e2))
                return false;
        }
        return true;
    }

    public static double round3(double value) {
        int i = (int) (value * 100.0D);
        double d = value * 100.0D;
        if (d - i >= 0.5D)
            i++;
        d = i;
        return d / 100.0D;
    }

    public static int randInt(int min, int max) {
        int min1 = min;
        int max1 = max;
        min = Math.min(min1, max1);
        max = Math.max(min1, max1);
        return r.nextInt(max - min + 1) + min;
    }

    public static String getEntityName(Entity e) {
        String name;
        if (e instanceof Projectile) {
            Projectile pp = (Projectile) e;
            if (pp.getShooter() != null && pp.getShooter() instanceof LivingEntity) {
                e = (LivingEntity) pp.getShooter();
            }
        }

        if (e instanceof Player) {
            name = e.getName();
        } else if (e instanceof LivingEntity) {
            if (e.getCustomName() != null) {
                name = e.getCustomName();
            } else {
                name = Lang.getCustom("EntityNames." + e.getType().name());
            }
        } else {
            name = "Unknown Object";
        }

        return name;
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });
        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list)
            result.put(entry.getKey(), entry.getValue());
        return result;
    }

    public static <T> List<List<T>> split(List<T> list, int targetSize) {
        List<List<T>> lists = new ArrayList<>();
        for (int i = 0; i < list.size(); i += targetSize)
            lists.add(list.subList(i, Math.min(i + targetSize, list.size())));
        return lists;
    }

    public static String IntegerToRomanNumeral(int input) {
        if (input < 1 || input > 3999)
            return "Invalid Roman Number Value";
        StringBuilder s = new StringBuilder();
        while (input >= 1000) {
            s.append("M");
            input -= 1000;
        }
        while (input >= 900) {
            s.append("CM");
            input -= 900;
        }
        while (input >= 500) {
            s.append("D");
            input -= 500;
        }
        while (input >= 400) {
            s.append("CD");
            input -= 400;
        }
        while (input >= 100) {
            s.append("C");
            input -= 100;
        }
        while (input >= 90) {
            s.append("XC");
            input -= 90;
        }
        while (input >= 50) {
            s.append("L");
            input -= 50;
        }
        while (input >= 40) {
            s.append("XL");
            input -= 40;
        }
        while (input >= 10) {
            s.append("X");
            input -= 10;
        }
        while (input >= 9) {
            s.append("IX");
            input -= 9;
        }
        while (input >= 5) {
            s.append("V");
            input -= 5;
        }
        while (input >= 4) {
            s.append("IV");
            input -= 4;
        }
        while (input >= 1) {
            s.append("I");
            input--;
        }
        return s.toString();
    }

    public static int romanToDecimal(String romanNumber) {
        int decimal = 0;
        int lastNumber = 0;
        String romanNumeral = romanNumber.toUpperCase();
        for (int x = romanNumeral.length() - 1; x >= 0; x--) {
            char convertToDecimal = romanNumeral.charAt(x);
            switch (convertToDecimal) {
                case 'M':
                    decimal = processDecimal(1000, lastNumber, decimal);
                    lastNumber = 1000;
                    break;
                case 'D':
                    decimal = processDecimal(500, lastNumber, decimal);
                    lastNumber = 500;
                    break;
                case 'C':
                    decimal = processDecimal(100, lastNumber, decimal);
                    lastNumber = 100;
                    break;
                case 'L':
                    decimal = processDecimal(50, lastNumber, decimal);
                    lastNumber = 50;
                    break;
                case 'X':
                    decimal = processDecimal(10, lastNumber, decimal);
                    lastNumber = 10;
                    break;
                case 'V':
                    decimal = processDecimal(5, lastNumber, decimal);
                    lastNumber = 5;
                    break;
                case 'I':
                    decimal = processDecimal(1, lastNumber, decimal);
                    lastNumber = 1;
                    break;
            }
        }
        return decimal;
    }

    public static String getEnums(Class<?> c, String c1, String c2) {
        String s = "";
        c1 = ChatColor.translateAlternateColorCodes('&', c1);
        c2 = ChatColor.translateAlternateColorCodes('&', c2);
        if (c.isEnum()) {
            Object[] var7;
            int var6 = (var7 = c.getEnumConstants()).length;

            for (int var5 = 0; var5 < var6; ++var5) {
                Object o = var7[var5];
                s = s + c1 + o.toString() + c2 + ", ";
            }

            if (s.length() > 4) {
                s = s.substring(0, s.length() - 4);
            }
        }

        return s;
    }

    public static List<String> getEnumsList(Class<?> c) {
        List<String> list = new ArrayList();
        if (c.isEnum()) {
            Object[] var5;
            int var4 = (var5 = c.getEnumConstants()).length;

            for (int var3 = 0; var3 < var4; ++var3) {
                Object o = var5[var3];
                list.add(o.toString());
            }
        }

        return list;
    }

    public static List<String> getWorldNames() {
        List<String> list = new ArrayList<>();
        for (World w : plugin.getServer().getWorlds())
            list.add(w.getName());
        return list;
    }

    public static <T> T getRandomItem(Map<T, Double> map, boolean alwaysHundred) {
        if (alwaysHundred) {
            List<T> fix = new ArrayList<>();
            for (Map.Entry<T, Double> e : map.entrySet()) {
                if (e.getValue().doubleValue() >= 100.0D)
                    fix.add(e.getKey());
            }
            if (!fix.isEmpty())
                return fix.get(r.nextInt(fix.size()));
        }
        map = sortByValue(map);
        List<T> items = new ArrayList<>(map.keySet());
        double totalSum = 0.0D;
        for (Iterator<Double> iterator = map.values().iterator(); iterator.hasNext(); ) {
            double d = iterator.next().doubleValue();
            totalSum += d;
        }
        double index = getRandDouble(0.0D, totalSum);
        double sum = 0.0D;
        int i = 0;
        while (sum < index)
            sum += map.get(items.get(i++)).doubleValue();
        return items.get(Math.max(0, i - 1));
    }

    public static int processDecimal(int decimal, int lastNumber, int lastDecimal) {
        if (lastNumber > decimal)
            return lastDecimal - decimal;
        return lastDecimal + decimal;
    }

    public static double getRandDouble(double min, double max) {
        return min + (max - min) * r.nextDouble();
    }

    public static double getRandDoubleNega(double min, double max) {
        double range = max - min;
        double scaled = r.nextDouble() * range;
        double shifted = scaled + min;
        return shifted;
    }

    public static ArrayList<Player> getNearbyEntities(double n, Location location) {
        ArrayList<Player> list = new ArrayList<>();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (location.getWorld().equals(player.getLocation().getWorld()) && location.distance(player.getLocation()) < n)
                list.add(player);
        }
        return list;
    }

    public static boolean check(Location location) {
        return !getNearbyEntities(50.0D, location).isEmpty();
    }

    public static Location getPointOnCircle(Location location, boolean b, double n, double n2, double n3) {
        return (b ? location.clone() : location).add(Math.cos(n) * n2, n3, Math.sin(n) * n2);
    }

    public static Location getPointOnCircle(Location location, double n, double n2, double n3) {
        return getPointOnCircle(location, true, n, n2, n3);
    }

    public static Location getCenter(Location loc) {
        return new Location(loc.getWorld(),
                getRelativeCoord(loc.getBlockX()),
                getRelativeCoord(loc.getBlockY()),
                getRelativeCoord(loc.getBlockZ()));
    }

    private static double getRelativeCoord(int i) {
        double d = i;
        d = (d < 0.0D) ? (d - 0.5D) : (d + 0.5D);
        return d;
    }

    public static double getClose(double input, List<Double> list) {
        if (list.isEmpty())
            return -1.0D;
        double distance = Math.abs(list.get(0).doubleValue() - input);
        int idx = 0;
        for (int c = 1; c < list.size(); c++) {
            double cdistance = Math.abs(list.get(c).doubleValue() - input);
            if (cdistance < distance) {
                idx = c;
                distance = cdistance;
            }
        }
        return list.get(idx).doubleValue();
    }

    public static String getTimeLeft(long l1, long l2) {
        long time = l1 - l2;
        long secs = time / 1000L;
        long mins = time / 1000L / 60L;
        long hours = mins / 60L;
        secs %= 60L;
        mins %= 60L;
        hours %= 24L;
        String tt = "";
        if (hours > 0L)
            tt = tt + hours + " " + Lang.Time_Hour.toMsg();
        if (mins > 0L)
            tt = tt + " " + mins + " " + Lang.Time_Min.toMsg();
        if (tt.isEmpty()) {
            tt = tt + secs + " " + Lang.Time_Sec.toMsg();
        } else {
            tt = tt + " " + secs + " " + Lang.Time_Sec.toMsg();
        }
        return tt;
    }

    public static String getTime(long time) {
        long secs = time / 1000L;
        long mins = time / 1000L / 60L;
        long hours = mins / 60L;
        secs %= 60L;
        mins %= 60L;
        hours %= 24L;
        String tt = hours + Lang.Time_Hour.toMsg() + " " + mins + Lang.Time_Min.toMsg() + " " + secs + Lang.Time_Sec.toMsg();
        return tt.replace("0" + Lang.Time_Hour.toMsg() + " ", "").replace("0" + Lang.Time_Min.toMsg() + " ", "");
    }

    public static BaseComponent[] myHoverText(String text) {
        ComponentBuilder cb = new ComponentBuilder("");
        cb.append(text);
        return cb.create();
    }

    public static Firework spawnRandomFirework(Location location) {
        Firework fw = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();
        int rt = r.nextInt(4) + 1;
        FireworkEffect.Type type = FireworkEffect.Type.BALL;
        if (rt == 1)
            type = FireworkEffect.Type.BALL;
        if (rt == 2)
            type = FireworkEffect.Type.BALL_LARGE;
        if (rt == 3)
            type = FireworkEffect.Type.BURST;
        if (rt == 4)
            type = FireworkEffect.Type.CREEPER;
        if (rt == 5)
            type = FireworkEffect.Type.STAR;
        int r1i = r.nextInt(250) + 1;
        int r2i = r.nextInt(250) + 1;
        Color c1 = Color.fromBGR(r1i, r2i, r1i);
        Color c2 = Color.fromBGR(r2i, r1i, r2i);
        FireworkEffect effect = FireworkEffect.builder().flicker(r.nextBoolean()).withColor(c1).withFade(c2).with(type).trail(r.nextBoolean()).build();
        fwm.addEffect(effect);
        int rp = r.nextInt(2) + 1;
        fwm.setPower(rp);
        fw.setFireworkMeta(fwm);
        return fw;
    }

    public static ItemStack getHashed(ItemStack item, String hash, String id) {
        UUID uuid;
        if (hash == null || hash.isEmpty())
            return item;
        if (id.isEmpty() || id == null) {
            uuid = UUID.randomUUID();
        } else {
            uuid = plugin.getCM().getItemHash(id);
        }
        if (item.getType() == Material.SKULL_ITEM) {
            SkullMeta sm = (SkullMeta) item.getItemMeta();
            GameProfile profile = new GameProfile(uuid, null);
            profile.getProperties().put("textures", new Property("textures", hash));
            Field profileField = null;
            try {
                profileField = sm.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                profileField.set(sm, profile);
            } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e1) {
                e1.printStackTrace();
            }
            item.setItemMeta(sm);
        }
        return item;
    }

    public static String getHashOf(ItemStack item) {
        if (!item.hasItemMeta())
            return "";
        if (item.getType() == Material.SKULL_ITEM) {
            GameProfile profile;
            SkullMeta sm = (SkullMeta) item.getItemMeta();
            Field f = null;
            try {
                f = sm.getClass().getDeclaredField("profile");
                f.setAccessible(true);
                profile = (GameProfile) f.get(sm);
                f.set(sm, profile);
            } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e1) {
                e1.printStackTrace();
                return "";
            }
            if (profile == null)
                return "";
            Collection<Property> pr = profile.getProperties().get("textures");
            for (Property p : pr) {
                if (p.getName().equalsIgnoreCase("textures") || p.getSignature().equalsIgnoreCase("textures"))
                    return p.getValue();
            }
            item.setItemMeta(sm);
        }
        return "";
    }

    public static int transSec(long from) {
        return (int) ((from - System.currentTimeMillis()) / 1000L);
    }

    public static void interactiveList(CommandSender sender, int page, List<String> source, QModule module, String cmd) {
        List<String> list = new ArrayList<>(source);
        int pages = split(list, 10).size();
        if (page > pages)
            page = pages;
        if (pages < 1) {
            list = new ArrayList<>();
        } else {
            list = split(list, 10).get(page - 1);
        }
        sender.sendMessage("§6§m--------§e " + module.name() + " List §6§m---------");
        int i = 10 * (page - 1) + 1;
        for (String s : list) {
            BaseComponent[] bs = null;
            ComponentBuilder append = new ComponentBuilder("");
            append.append("§c" + i + ". §7", ComponentBuilder.FormatRetention.NONE);
            append.append(s);
            append.append("   ", ComponentBuilder.FormatRetention.NONE);
            append.append("§a[Get Item]");
            append.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, myHoverText("§fGive the item into your inventory")));
            append.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + module.cmd() + " get " + s + " " + cmd));
            append.append("  ", ComponentBuilder.FormatRetention.NONE);
            bs = append.create();
            if (sender instanceof Player) {
                Player p = (Player) sender;
                p.spigot().sendMessage(bs);
            } else {
                sender.sendMessage("§c" + i + ". §7" + s);
            }
            i++;
        }
        sender.sendMessage("");
        if (page < pages)
            sender.sendMessage("§eType §6/" + module.cmd() + " list " + (page + 1) + " §eto next page.");
        sender.sendMessage("§6§m------ §e Page §7" + page + "§e of §7" + pages + " §6§m------");
    }

    public static String getDirection(Float n) {
        n = Float.valueOf(n.floatValue() / 90.0F);
        n = Float.valueOf(Math.round(n.floatValue()));
        if (n.floatValue() == -4.0F || n.floatValue() == 0.0F || n.floatValue() == 4.0F)
            return "SOUTH";
        if (n.floatValue() == -1.0F || n.floatValue() == 3.0F)
            return "EAST";
        if (n.floatValue() == -2.0F || n.floatValue() == 2.0F)
            return "NORTH";
        if (n.floatValue() == -3.0F || n.floatValue() == 1.0F)
            return "WEST";
        return "";
    }

    public static String getCardinalDirection(Player player) {
        double rotation = ((player.getLocation().getYaw() - 90.0F) % 360.0F);
        if (rotation < 0.0D)
            rotation += 360.0D;
        if (0.0D <= rotation && rotation < 22.5D)
            return "N";
        if (22.5D <= rotation && rotation < 67.5D)
            return "NE";
        if (67.5D <= rotation && rotation < 112.5D)
            return "E";
        if (112.5D <= rotation && rotation < 157.5D)
            return "SE";
        if (157.5D <= rotation && rotation < 202.5D)
            return "S";
        if (202.5D <= rotation && rotation < 247.5D)
            return "SW";
        if (247.5D <= rotation && rotation < 292.5D)
            return "W";
        if (292.5D <= rotation && rotation < 337.5D)
            return "NW";
        if (337.5D <= rotation && rotation < 360.0D)
            return "N";
        return null;
    }

    public static Color getColorOfChar(String s) {
        Color c = Color.WHITE;
        String str;
        switch ((str = s).hashCode()) {
            case 48:
                if (!str.equals("0"))
                    break;
                c = Color.BLACK;
                break;
            case 49:
                if (!str.equals("1"))
                    break;
                c = Color.NAVY;
                break;
            case 50:
                if (!str.equals("2"))
                    break;
                c = Color.GREEN;
                break;
            case 51:
                if (!str.equals("3"))
                    break;
                c = Color.TEAL;
                break;
            case 52:
                if (!str.equals("4"))
                    break;
                c = Color.MAROON;
                break;
            case 53:
                if (!str.equals("5"))
                    break;
                c = Color.OLIVE;
                break;
            case 54:
                if (!str.equals("6"))
                    break;
                c = Color.ORANGE;
                break;
            case 55:
                if (!str.equals("7"))
                    break;
                c = Color.SILVER;
                break;
            case 56:
                if (!str.equals("8"))
                    break;
                c = Color.GRAY;
                break;
            case 57:
                if (!str.equals("9"))
                    break;
                c = Color.BLUE;
                break;
            case 97:
                if (!str.equals("a"))
                    break;
                c = Color.LIME;
                break;
            case 98:
                if (!str.equals("b"))
                    break;
                c = Color.AQUA;
                break;
            case 99:
                if (!str.equals("c"))
                    break;
                c = Color.RED;
                break;
            case 100:
                if (!str.equals("d"))
                    break;
                c = Color.PURPLE;
                break;
            case 101:
                if (!str.equals("e"))
                    break;
                c = Color.YELLOW;
                break;
            case 102:
                if (!str.equals("f"))
                    break;
                c = Color.WHITE;
                break;
        }
        return c;
    }

    public static Color getColorByName(String s) {
        Color c = Color.WHITE;
        s = s.toUpperCase();
        String str;
        switch ((str = s).hashCode()) {
            case -2027972496:
                if (!str.equals("MAROON"))
                    break;
                c = Color.MAROON;
                break;
            case -1955522002:
                if (!str.equals("ORANGE"))
                    break;
                c = Color.ORANGE;
                break;
            case -1923613764:
                if (!str.equals("PURPLE"))
                    break;
                c = Color.PURPLE;
                break;
            case -1848981747:
                if (!str.equals("SILVER"))
                    break;
                c = Color.SILVER;
                break;
            case -1680910220:
                if (!str.equals("YELLOW"))
                    break;
                c = Color.YELLOW;
                break;
            case 81009:
                if (!str.equals("RED"))
                    break;
                c = Color.RED;
                break;
            case 2016956:
                if (!str.equals("AQUA"))
                    break;
                c = Color.AQUA;
                break;
            case 2041946:
                if (!str.equals("BLUE"))
                    break;
                c = Color.BLUE;
                break;
            case 2196067:
                if (!str.equals("GRAY"))
                    break;
                c = Color.GRAY;
                break;
            case 2336725:
                if (!str.equals("LIME"))
                    break;
                c = Color.LIME;
                break;
            case 2388918:
                if (!str.equals("NAVY"))
                    break;
                c = Color.NAVY;
                break;
            case 2570844:
                if (!str.equals("TEAL"))
                    break;
                c = Color.TEAL;
                break;
            case 63281119:
                if (!str.equals("BLACK"))
                    break;
                c = Color.BLACK;
                break;
            case 68081379:
                if (!str.equals("GREEN"))
                    break;
                c = Color.GREEN;
                break;
            case 75295163:
                if (!str.equals("OLIVE"))
                    break;
                c = Color.OLIVE;
                break;
            case 82564105:
                if (!str.equals("WHITE"))
                    break;
                c = Color.WHITE;
                break;
        }
        return c;
    }

    public static void playEffect(String eff, Location loc, float x, float y, float z, float speed, int a) {
        Particle pe;
        if ((eff.split(":")).length == 2) {
            try {
                pe = Particle.valueOf(eff.split(":")[0].toUpperCase());
            } catch (IllegalArgumentException ex) {
                LogUtil.send("Invalid particle effect '" + eff + "'!", LogType.WARN);
                return;
            }
        } else {
            try {
                pe = Particle.valueOf(eff.toUpperCase());
            } catch (IllegalArgumentException ex) {
                LogUtil.send("Invalid particle effect '" + eff + "'!", LogType.WARN);
                return;
            }
        }
        if (pe == Particle.REDSTONE) {
            Color c = Color.WHITE;
            if ((eff.split(":")).length == 2) {
                String ss = eff.split(":")[1];
                if ((ss.split(",")).length == 3) {
                    String[] s3 = ss.split(",");
                    int r = Integer.parseInt(s3[0]);
                    int g = Integer.parseInt(s3[1]);
                    int b = Integer.parseInt(s3[2]);
                    c = Color.fromRGB(r, g, b);
                } else {
                    c = getColorByName(ss);
                }
            }
            loc.getWorld().spawnParticle(pe, loc, a, x, y, z);
        } else if (pe == Particle.BLOCK_CRACK) {
            Material m = null;
            if ((eff.split(":")).length == 2)
                m = Material.getMaterial(eff.split(":")[1].toUpperCase());
            if (m == null)
                m = Material.STONE;
            loc.getWorld().spawnParticle(pe, loc, a, x, y, z, speed, m);
        } else if (pe == Particle.ITEM_CRACK) {
            Material m = null;
            if ((eff.split(":")).length == 2)
                m = Material.getMaterial(eff.split(":")[1].toUpperCase());
            if (m == null)
                m = Material.STONE;
            loc.getWorld().spawnParticle(pe, loc, a, x, y, z, speed);
        } else {
            loc.getWorld().spawnParticle(pe, loc, a, x, y, z, speed);
        }
    }
}

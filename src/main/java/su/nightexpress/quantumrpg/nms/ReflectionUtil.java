package su.nightexpress.quantumrpg.nms;

import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.netty.channel.Channel;
import mc.promcteam.engine.utils.Reflex;
import mc.promcteam.engine.utils.constants.JNumbers;
import mc.promcteam.engine.utils.random.Rnd;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Random;
import java.util.UUID;

public class ReflectionUtil {

    public static final String VERSION = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
    public static final int MINOR_VERSION = Integer.parseInt(VERSION.split("_")[1]);

    protected static Object newNBTTagCompound() {
        try {
            Class<?> nbtTagClass = getNMSClass("NBTTagCompound");
            return nbtTagClass.getConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected static Object newNBTTagList() {
        try {
            Class<?> nbtTagClass = getNMSClass("NBTTagList");
            return nbtTagClass.getConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    protected static Object newNBTTagString(String str) {
        try {
            Class<?> nbtTagClass = getNMSClass("NBTTagString");
            return nbtTagClass.getConstructor(String.class).newInstance(str);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    protected static Object newNBTTagDouble(double dub) {
        try {
            Class<?> nbtTagClass = getNMSClass("NBTTagDouble");
            return nbtTagClass.getConstructor(Double.class).newInstance(dub);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    protected static Object newNBTTagInt(int i) {
        try {
            Class<?> nbtTagClass = getNMSClass("NBTTagInt");
            return nbtTagClass.getConstructor(Integer.class).newInstance(i);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    protected static Object getNMSCopy(ItemStack item) {
        try {
            Class<?> craftItemClass = getCraftClass("inventory.CraftItemStack");
            Method asNMSCopy = Reflex.getMethod(craftItemClass, "asNMSCopy", ItemStack.class);

            return Reflex.invokeMethod(asNMSCopy, null, item);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    protected static ItemStack toBukkitCopy(Object nmsItem) {
        try {
            Class<?> craftItem = getCraftClass("inventory.CraftItemStack");
            Method asBukkitCopy = Reflex.getMethod(craftItem, "asBukkitCopy", getNMSClass("ItemStack"));
            if (asBukkitCopy == null) return null;

            return (ItemStack) Reflex.invokeMethod(asBukkitCopy, null, nmsItem);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    protected static Object save(Object nmsItem, Object nbtCompound) {
        try {
            Method save = Reflex.getMethod(nmsItem.getClass(), "save", nbtCompound.getClass());

            return Reflex.invokeMethod(save, nmsItem, nbtCompound);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Class<?> getNMSClass(String nmsClassString) throws ClassNotFoundException {
        String version = VERSION + ".";
        String name = "net.minecraft.server." + version + nmsClassString;
        Class<?> nmsClass = Class.forName(name);
        return nmsClass;
    }

    public static Class<?> getCraftClass(String craftClassString) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        String name = "org.bukkit.craftbukkit." + version + craftClassString;
        Class<?> craftClass = Class.forName(name);
        return craftClass;
    }

    public static Object getConnection(Player player) {
        try {
            Class craftPlayerClass = getCraftClass("entity.CraftPlayer");

            Method getHandle = Reflex.getMethod(craftPlayerClass, "getHandle");
            Object nmsPlayer = Reflex.invokeMethod(getHandle, getCraftPlayer(player));

            Object con = Reflex.getFieldValue(nmsPlayer, "playerConnection");
            return con;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Object getCraftPlayer(Player p) {
        try {
            Class<?> craftClass = getCraftClass("entity.CraftPlayer");
            return craftClass.cast(p);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Object getEntity(Object craftEntity) {
        try {
            Class<?> craftClass = getNMSClass("Entity");

            Method getHandle = Reflex.getMethod(craftClass, "getHandle");

            return craftClass.cast(getHandle.invoke(craftEntity));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Channel getChannel(Player p) {
        try {
            Object conn = getConnection(p);
            Object manager = Reflex.getFieldValue(conn, "networkManager");
            Channel channel = (Channel) Reflex.getFieldValue(manager, "channel");

            return channel;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void sendPackets(Player p, Collection<Object> packets) {
        for (Object packet : packets) {
            sendPacket(p, packet);
        }
    }

    public static void sendPacket(Player p, Object packet) {
        Object conn = getConnection(p);
        Class<?> packetClass = null;
        try {
            packetClass = getNMSClass("Packet");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Method sendMethod = Reflex.getMethod(conn.getClass(), "sendPacket", packetClass);
        Reflex.invokeMethod(sendMethod, conn, packet);
    }

    public static void sendAttackPacket(Player p, int id) {
        try {
            Object craftPlayer = getCraftPlayer(p);
            Object entity = getEntity(craftPlayer);

            Class<?> packetClass = getNMSClass("PacketPlayOutAnimation");
            Constructor ctor = Reflex.getConstructor(packetClass, entity.getClass(), int.class);
            Object packet = Reflex.invokeConstructor(ctor, entity, id);

            sendPacket(p, packet);
        } catch (ClassNotFoundException e) {
            System.err.println("Could not send attack packet.");
            e.printStackTrace();
        }
    }

    public static void openChestAnimation(Block chest, boolean open) {
        if (chest.getState() instanceof Chest) {
            Location lo = chest.getLocation();
            World bWorld = lo.getWorld();
            if (bWorld == null) return;

            try {
                Class<?> worldClass = getNMSClass("World");
                Class<?> craftWorld = getCraftClass("CraftWorld");
                Class<?> blockClass = getNMSClass("Block");
                Class<?> blockPosClass = getNMSClass("BlockPosition");

                Object nmsWorld = worldClass.cast(bWorld);
                Method getHandle = Reflex.getMethod(nmsWorld.getClass(), "getHandle");

                Object world = craftWorld.cast(Reflex.invokeMethod(getHandle, nmsWorld));
                Method playBlockAction = Reflex.getMethod(craftWorld, "playBlockAction", blockPosClass, blockClass, int.class, int.class);

                Constructor ctor = Reflex.getConstructor(blockPosClass, double.class, double.class, double.class);
                Object position = Reflex.invokeConstructor(ctor, lo.getX(), lo.getY(), lo.getZ());

                Method getType = Reflex.getMethod(world.getClass(), "getType", blockPosClass);
                Class<?> blockData = getNMSClass("IBlockData");
                Object data = blockData.cast(Reflex.invokeMethod(getType, world, position));

                Method getBlock = Reflex.getMethod(blockData, "getBlock");

                //TileEntityChest tileChest = (TileEntityChest) world.getTileEntity(position);
                Reflex.invokeMethod(playBlockAction, world, position, getBlock.invoke(data), 1, open ? 1 : 0);
            } catch (Exception e) {
                System.err.println("Problem sending chest animation");
                e.printStackTrace();
            }
        }
    }

    public static String toJSON(@NotNull ItemStack item) {
        try {
            Object nbtCompound = newNBTTagCompound();
            Object nmsItem = getNMSCopy(item);

            nbtCompound = save(nmsItem, nbtCompound);

            Method toString = Reflex.getMethod(nbtCompound.getClass(), "toString");

            String js = (String) Reflex.invokeMethod(toString, nbtCompound);
            if (js.length() > JNumbers.JSON_MAX) {
                ItemStack item2 = new ItemStack(item.getType());
                return toJSON(item2);
            }

            return js;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String toBase64(@NotNull ItemStack item) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutput = new DataOutputStream(outputStream);

            Object nbtTagListItems = newNBTTagList();
            Object nbtTagCompoundItem = newNBTTagCompound();

            Object nmsItem = getNMSCopy(item);

            save(nmsItem, nbtTagCompoundItem);

            Method add = Reflex.getMethod(AbstractList.class, "add", Object.class);
            Reflex.invokeMethod(add, nbtTagListItems, nbtTagCompoundItem);

            Class<?> compressedClass = getNMSClass("NBTCompressedStreamTools");
            Method a = Reflex.getMethod(compressedClass, "a", nbtTagCompoundItem.getClass(), DataOutput.class);

            Reflex.invokeMethod(a, null, nbtTagCompoundItem, dataOutput);

            String str = new BigInteger(1, outputStream.toByteArray()).toString(32);


            return str;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static ItemStack fromBase64(@NotNull String data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(new BigInteger(data, 32).toByteArray());

            Object nbtTagCompoundRoot;
            try {
                Class<?> compressedClass = getNMSClass("NBTCompressedStreamTools");
                Method a = Reflex.getMethod(compressedClass, "a", DataInput.class);

                nbtTagCompoundRoot = Reflex.invokeMethod(a, null, (DataInput) new DataInputStream(inputStream));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }

            Class<?> nmsItemClass = getNMSClass("ItemStack");
            Method a = Reflex.getMethod(nmsItemClass, "a", getNMSClass("NBTTagCompound"));

            Object nmsItem = Reflex.invokeMethod(a, null, nbtTagCompoundRoot);

            Method asBukkitCopy = Reflex.getMethod(getCraftClass("inventory.CraftItemStack"), "asBukkitCopy", nmsItemClass);

            ItemStack item = (ItemStack) Reflex.invokeMethod(asBukkitCopy, null, nmsItem);

            return item;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getNbtString(@NotNull ItemStack item) {
        try {
            Object nmsCopy = getNMSCopy(item);
            Method getOrCreateTag = Reflex.getMethod(nmsCopy.getClass(), "getOrCreateTag");
            Object tag = Reflex.invokeMethod(getOrCreateTag, nmsCopy);
            Method asString = Reflex.getMethod(tag.getClass(), "asString");
            return (String) Reflex.invokeMethod(asString, tag);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static ItemStack damageItem(@NotNull ItemStack item, int amount, @Nullable Player player) {
        //CraftItemStack craftItem = (CraftItemStack) item;
        try {
            Object nmsStack = getNMSCopy(item);

            Object nmsPlayer = player != null ? getEntity(getCraftPlayer(player)) : null;

            Method isDamaged = Reflex.getMethod(nmsStack.getClass(), "isDamaged", int.class, Random.class, getNMSClass("EntityPlayer"));

            Reflex.invokeMethod(isDamaged, nmsStack, amount, Rnd.rnd, nmsPlayer);

            return toBukkitCopy(nmsStack);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Multimap<Object, Object> getAttributes(@NotNull ItemStack itemStack) {
        try {
            Multimap<Object, Object> attMap = null;
            Object nmsItem = getNMSCopy(itemStack);
            Method getItem = Reflex.getMethod(nmsItem.getClass(), "getItem");
            Object item = Reflex.invokeMethod(getItem, nmsItem);


            Class<Enum> enumItemSlotClass = (Class<Enum>) getNMSClass("EnumItemSlot");
//            Class<?> attributeModClass = getNMSClass("AttributeModifier");
            Class<?> itemArmorClass = getNMSClass("ItemArmor");
            Class<?> itemToolClass = getNMSClass("ItemTool");
            Class<?> itemSwordClass = getNMSClass("ItemSword");
            Class<?> itemTridentClass = getNMSClass("ItemTrident");


            if (itemArmorClass.isInstance(item)) {
                Object tool = itemArmorClass.cast(item);
                Method b = Reflex.getMethod(itemArmorClass, "b");
                Object bObj = Reflex.invokeMethod(b, tool);
                Method a = Reflex.getMethod(itemArmorClass, "a", enumItemSlotClass);

                attMap = (Multimap<Object, Object>) Reflex.invokeMethod(a, tool, bObj);
            } else if (itemToolClass.isInstance(item)) {
                Object tool = itemToolClass.cast(item);
                Method a = Reflex.getMethod(itemToolClass, "a", enumItemSlotClass);
                attMap = (Multimap<Object, Object>) Reflex.invokeMethod(a, tool, Enum.valueOf(enumItemSlotClass, "MAINHAND"));
            } else if (itemSwordClass.isInstance(item)) {
                Object tool = itemSwordClass.cast(item);
                Method a = Reflex.getMethod(itemSwordClass, "a", enumItemSlotClass);
                attMap = (Multimap<Object, Object>) Reflex.invokeMethod(a, tool, Enum.valueOf(enumItemSlotClass, "MAINHAND"));
            } else if (itemTridentClass.isInstance(item)) {
                Object tool = itemTridentClass.cast(item);
                Method a = Reflex.getMethod(itemTridentClass, "a", enumItemSlotClass);
                attMap = (Multimap<Object, Object>) Reflex.invokeMethod(a, tool, Enum.valueOf(enumItemSlotClass, "MAINHAND"));
            }

            return attMap;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static double getAttributeValue(@NotNull ItemStack item, @NotNull Object attackDamage) {
        try {
            Class<?> attributeModifierClass = getNMSClass("AttributeModifier");
            if (attackDamage.getClass().getSimpleName().equals("IAttribute")) {
                Class<?> iAttributeClass = getNMSClass("IAttribute");
                Multimap<Object, Object> attMap = getAttributes(item);
                if (attMap == null) return 0D;
                Object atkDmg = iAttributeClass.cast(attackDamage);
                Method getName = Reflex.getMethod(iAttributeClass, "getName");

                //Collection<AttributeModifier>
                Collection<Object> att = attMap.get(Reflex.invokeMethod(getName, atkDmg));
                Object mod = attributeModifierClass.cast((att == null || att.isEmpty()) ? 0 : att.stream().findFirst().get());

                Method getAmount = Reflex.getMethod(attributeModifierClass, "getAmount");
                double damage = (double) Reflex.invokeMethod(getAmount, mod);

                return damage;// + 1;
            } else if (attackDamage.getClass().getSimpleName().equals("AttributeBase")) {
                Class<?> attributeBaseClass = getNMSClass("AttributeBase");
                Multimap<Object, Object> attMap = getAttributes(item);
                if (attMap == null) return 0D;

                Collection<Object> att = attMap.get(attributeBaseClass.cast(attackDamage));
                Object mod = attributeModifierClass.cast((att == null || att.isEmpty()) ? 0 : att.stream().findFirst().get());

                Method getAmount = Reflex.getMethod(attributeModifierClass, "getAmount");
                double damage = (double) Reflex.invokeMethod(getAmount, mod);

                return damage;// + 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static double getDefaultDamage(@NotNull ItemStack itemStack) {
        return getAttributeValue(itemStack, getGenericAttribute("ATTACK_DAMAGE"));
    }

    public static double getDefaultSpeed(@NotNull ItemStack itemStack) {
        return getAttributeValue(itemStack, getGenericAttribute("ATTACK_SPEED"));
    }

    public static double getDefaultArmor(@NotNull ItemStack itemStack) {
        return getAttributeValue(itemStack, getGenericAttribute("ARMOR"));
    }

    public static double getDefaultToughness(@NotNull ItemStack itemStack) {
        return getAttributeValue(itemStack, getGenericAttribute("ARMOR_TOUGHNESS"));
    }

    public static Object getGenericAttribute(String field) {
        try {
            Class<?> attributes = getNMSClass("GenericAttributes");
            Object value = Reflex.getField(attributes, field).get(null);

            //AttributeBase or IAttribute
            return value;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean isWeapon(@NotNull ItemStack itemStack) {
        try {
            Object nmsItem = getNMSCopy(itemStack);

            Method getItem = Reflex.getMethod(nmsItem.getClass(), "getItem");

            Object item = Reflex.invokeMethod(getItem, nmsItem);

            Class<?> swordClass = getNMSClass("ItemSword");
            Class<?> axeClass = getNMSClass("ItemAxe");
            Class<?> tridentClass = getNMSClass("ItemTrident");

            return swordClass.isInstance(item) || axeClass.isInstance(item) || tridentClass.isInstance(item);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isTool(@NotNull ItemStack itemStack) {
        try {
            Object nmsItem = getNMSCopy(itemStack);

            Method getItem = Reflex.getMethod(nmsItem.getClass(), "getItem");

            Object item = Reflex.invokeMethod(getItem, nmsItem);

            Class<?> toolClass = getNMSClass("ItemTool");

            return toolClass.isInstance(item);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isArmor(@NotNull ItemStack itemStack) {
        try {
            Object nmsItem = getNMSCopy(itemStack);

            Method getItem = Reflex.getMethod(nmsItem.getClass(), "getItem");

            Object item = Reflex.invokeMethod(getItem, nmsItem);

            Class<?> armorClass = getNMSClass("ItemArmor");

            return armorClass.isInstance(item);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String fixColors(@NotNull String str) {
        try {
            str = str.replace("\n", "%n%"); // CraftChatMessage wipes all lines out.

            Class<?> baseComponentClass = getNMSClass("IChatBaseComponent");
            Class<?> chatMessageClass = getCraftClass("util.CraftChatMessage");

            Method fromComponent = Reflex.getMethod(chatMessageClass, "fromComponent", baseComponentClass);
            Method fromStringOrNull = Reflex.getMethod(chatMessageClass, "fromStringOrNull", String.class);

            Object baseComponent = Reflex.invokeMethod(fromStringOrNull, null, str);
            String singleColor = (String) Reflex.invokeMethod(fromComponent, null, baseComponentClass.cast(baseComponent));
            return singleColor.replace("%n%", "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return str;
    }

    public static float getAttackCooldown(Player p) {
        try {
            Class<?> entityPlayerClass = getNMSClass("EntityPlayer");
            Class entityHumanClass = getNMSClass("EntityHuman");
            Object craftPlayer = getCraftPlayer(p);
            Method getHandle = Reflex.getMethod(craftPlayer.getClass(), "getHandle");

            Object ep = entityPlayerClass.cast(Reflex.invokeMethod(getHandle, craftPlayer));

            if (MINOR_VERSION < 16) {
                Method s = Reflex.getMethod(entityHumanClass, "s", float.class);
                if (s == null)
                    throw new NullPointerException("Could not find a \"s\" method using Reflection.");

                return (float) Reflex.invokeMethod(s, entityHumanClass.cast(ep), 0f);
            } else {
                Method getAttackCooldown = Reflex.getMethod(entityHumanClass, "getAttackCooldown", float.class);
                if (getAttackCooldown == null)
                    throw new NullPointerException("Could not find a \"getAttackCooldown\" method using Reflection.");

                return (float) Reflex.invokeMethod(getAttackCooldown, entityHumanClass.cast(ep), 0f);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static void changeSkull(Block b, String hash) {
        try {
            Class<?> tileSkullClass = getNMSClass("TileEntitySkull");
            Class<?> craftWorldClass = getCraftClass("CraftWorld");
//            Class<?> worldServerClass = getNMSClass("WorldServer");
            Class<?> blockAccessClass = getNMSClass("IBlockAccess");
            Class<?> blockPosClass = getNMSClass("BlockPosition");

            Constructor ctor = Reflex.getConstructor(blockPosClass, int.class, int.class, int.class);

            Method getHandle = Reflex.getMethod(craftWorldClass, "getHandle");
            Method getTileEntity = Reflex.getMethod(blockAccessClass, "getTileEntity", blockPosClass);

            Object bPos = Reflex.invokeConstructor(ctor, b.getX(), b.getY(), b.getZ());

            Object worldServer = Reflex.invokeMethod(getHandle, craftWorldClass.cast(b.getWorld()));
            Object skullTile = tileSkullClass.cast(Reflex.invokeMethod(getTileEntity, worldServer, bPos));


            Method setGameProfile = Reflex.getMethod(tileSkullClass, "setGameProfile", GameProfile.class);

            Reflex.invokeMethod(setGameProfile, skullTile, getNonPlayerProfile(hash));
            b.getState().update(true);
        } catch (Exception e) {
            System.err.println("Could not update skull");
            e.printStackTrace();
        }
    }

    protected static GameProfile getNonPlayerProfile(String hash) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", new String(hash)));
        return profile;
    }

    /**
     * Tries to get a method from the object
     *
     * @param o          object reference
     * @param methodName name of the field to retrieve the value from
     * @return the value of the field or null if not found
     */
    public static Method getMethod(Object o, String methodName, Class<?>... params) {
        try {
            Method method = o.getClass().getMethod(methodName, params);
            if (!method.isAccessible()) method.setAccessible(true);
            return method;
        } catch (Exception ex) { /* Do nothing */ }
        return null;
    }

    @Nullable
    public static Method getMethod(@NotNull Class<?> clazz, @NotNull String fieldName, @NotNull Class<?>... o) {
        try {
            return clazz.getDeclaredMethod(fieldName, o);
        } catch (NoSuchMethodException e) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass == null) {
                return null;
            } else {
                return getMethod(superClass, fieldName);
            }
        }
    }

    @Nullable
    public static Object invokeMethod(@NotNull Method m, @Nullable Object by, @Nullable Object... param) {
        m.setAccessible(true);
        try {
            return m.invoke(by, param);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T extends Enum> T getEnum(Class<?> clazz, String enumName) {
        return (T) Enum.valueOf((Class<T>) clazz, enumName);
    }

}

package su.nightexpress.quantumrpg.nms;

import mc.promcteam.engine.utils.Reflex;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.quantumrpg.api.ItemAPI;
import su.nightexpress.quantumrpg.stats.ItemStat;
import su.nightexpress.quantumrpg.utils.AttUT;
import su.nightexpress.quantumrpg.utils.ItemUtils;
import su.nightexpress.quantumrpg.utils.Utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;

public interface NMS {
    default ItemStack setNBTAttribute(ItemStack paramItemStack, NBTAttribute paramNBTAttribute, double paramDouble) {
        if (paramNBTAttribute == NBTAttribute.attackDamage && ItemUtils.isBow(paramItemStack))
            return paramItemStack;
        if (paramDouble == 0.0D && (
                paramNBTAttribute != NBTAttribute.attackSpeed || !ItemUtils.isWeapon(paramItemStack)))
            return delNBTAttribute(paramItemStack, paramNBTAttribute);

        Object nmsStack = ReflectionUtil.getNMSCopy(paramItemStack);

        Method hasTag = ReflectionUtil.getMethod(nmsStack, "hasTag");
        Method getTag = ReflectionUtil.getMethod(nmsStack, "getTag");

        Object nbt = (Boolean) ReflectionUtil.invokeMethod(hasTag, nmsStack)
                ? ReflectionUtil.invokeMethod(getTag, nmsStack)
                : ReflectionUtil.newNBTTagCompound();


        Method getList = ReflectionUtil.getMethod(nbt, "getList", String.class, int.class);
        Object nbtTags = ReflectionUtil.invokeMethod(getList, nbt, "AttributeModifiers", 10);
        Method get = ReflectionUtil.getMethod(nbtTags, "get", int.class);
        Method remove = ReflectionUtil.getMethod(nbtTags, "remove", int.class);
        Method size = ReflectionUtil.getMethod(nbtTags, "size");

        Method nbtSet = null;
        Method add = null;
        Method set = null;
        Method getString = null;
        try {
            Class compound = ReflectionUtil.getNMSClass("NBTTagCompound");
            getString = ReflectionUtil.getMethod(compound, "getString", String.class);
            set = ReflectionUtil.getMethod(compound, "set", String.class, ReflectionUtil.getNMSClass("NBTBase"));
            add = ReflectionUtil.getMethod(nbtTags, "add", ReflectionUtil.getNMSClass("NBTBase"));
            nbtSet = ReflectionUtil.getMethod(nbt, "set", String.class, ReflectionUtil.getNMSClass("NBTBase"));
        } catch (ClassNotFoundException e) {
            System.err.println("Could not get NBTTagCompound using Reflection.");
            e.printStackTrace();
        }

        if (getString == null) return paramItemStack;

        byte b;
        int i;
        String[] arrayOfString;
        for (i = (arrayOfString = ItemUtils.getAllNBTSlots(paramItemStack)).length, b = 0; b < i; ) {
            String s = arrayOfString[b];
            double amount = paramDouble;
            Object tag = ReflectionUtil.newNBTTagCompound();
            int listSize = (int) ReflectionUtil.invokeMethod(size, nbtTags);
            for (int j = 0; j < listSize; j++) {
                Object nb = ReflectionUtil.invokeMethod(get, nbtTags, j);
                String a_key = (String) ReflectionUtil.invokeMethod(getString, nb, "AttributeName");
                String slot = (String) ReflectionUtil.invokeMethod(getString, nb, "Slot");
                if (a_key.equals("generic." + paramNBTAttribute) && slot.equalsIgnoreCase(s)) {
                    tag = nb;
                    ReflectionUtil.invokeMethod(remove, nbtTags, j);
                    break;
                }
            }
            if (paramNBTAttribute == NBTAttribute.movementSpeed) {
                amount = 0.1D * (1.0D + amount / 100.0D) - 0.1D;
            } else if (paramNBTAttribute == NBTAttribute.attackSpeed) {
                amount /= 1000.0D;
                double a1 = ItemAPI.getDefaultAttackSpeed(paramItemStack) * amount;
                amount = -(ItemAPI.getDefaultAttackSpeed(paramItemStack) - a1);
            }
            int rand = Utils.randInt(1, 333);
            ReflectionUtil.invokeMethod(set, tag, "AttributeName", ReflectionUtil.newNBTTagString("generic." + paramNBTAttribute.att()));
            ReflectionUtil.invokeMethod(set, tag, "Name", ReflectionUtil.newNBTTagString("generic." + paramNBTAttribute.att()));
            ReflectionUtil.invokeMethod(set, tag, "Amount", ReflectionUtil.newNBTTagDouble(amount));
            ReflectionUtil.invokeMethod(set, tag, "Operation", ReflectionUtil.newNBTTagInt(0));
            ReflectionUtil.invokeMethod(set, tag, "UUIDLeast", ReflectionUtil.newNBTTagInt(rand));
            ReflectionUtil.invokeMethod(set, tag, "UUIDMost", ReflectionUtil.newNBTTagInt(rand));
            ReflectionUtil.invokeMethod(set, tag, "Slot", ReflectionUtil.newNBTTagString(s));

            ReflectionUtil.invokeMethod(add, nbtTags, tag);
            b++;
        }

        ReflectionUtil.invokeMethod(nbtSet, nbt, "AttributeModifiers", nbtTags);

        try {
            ReflectionUtil.invokeMethod(
                    ReflectionUtil.getMethod(nmsStack, "setTag", ReflectionUtil.getNMSClass("NBTTagCompound")),
                    nmsStack,
                    nbt
            );
            paramItemStack = ReflectionUtil.toBukkitCopy(nmsStack);
            return paramItemStack;
        } catch (Exception e) {
            System.err.println("Couldn't create Bukkit stack from NMS");
            e.printStackTrace();
            return null;
        }
    }

    default ItemStack delNBTAttribute(ItemStack paramItemStack, NBTAttribute paramNBTAttribute) {
        Object nmsStack = ReflectionUtil.getNMSCopy(paramItemStack);

        Method hasTag = ReflectionUtil.getMethod(nmsStack, "hasTag");
        Method getTag = ReflectionUtil.getMethod(nmsStack, "getTag");

        Object nbt = (Boolean) ReflectionUtil.invokeMethod(hasTag, nmsStack)
                ? ReflectionUtil.invokeMethod(getTag, nmsStack)
                : ReflectionUtil.newNBTTagCompound();

        Method getList = ReflectionUtil.getMethod(nbt, "getList", String.class, int.class);
        Object nbtTags = ReflectionUtil.invokeMethod(getList, nbt, "AttributeModifiers", 10);

        Method get = ReflectionUtil.getMethod(nbtTags, "get", int.class);
        Method remove = ReflectionUtil.getMethod(nbtTags, "remove", int.class);
        Method size = ReflectionUtil.getMethod(nbtTags, "size");

        Method nbtSet = null;
        Method getString = null;
        try {
            Class compound = ReflectionUtil.getNMSClass("NBTTagCompound");
            getString = ReflectionUtil.getMethod(compound, "getString", String.class);
            nbtSet = ReflectionUtil.getMethod(nbt, "set", String.class, ReflectionUtil.getNMSClass("NBTBase"));
        } catch (ClassNotFoundException e) {
            System.err.println("Could not get NBTTagCompound using Reflection.");
            e.printStackTrace();
        }

        byte b;
        int i;
        String[] arrayOfString;
        for (i = (arrayOfString = ItemUtils.getAllNBTSlots(paramItemStack)).length, b = 0; b < i; ) {
            String s = arrayOfString[b];
            int listSize = (int) ReflectionUtil.invokeMethod(size, nbtTags);
            for (int j = 0; j < listSize; j++) {
                Object nb = ReflectionUtil.invokeMethod(get, nbtTags, j);
                String a_key = (String) ReflectionUtil.invokeMethod(getString, nb, "AttributeName");
                String slot = (String) ReflectionUtil.invokeMethod(getString, nb, "Slot");
                if (a_key.equals("generic." + paramNBTAttribute) && slot.equalsIgnoreCase(s)) {
                    ReflectionUtil.invokeMethod(remove, nbtTags, j);
                    break;
                }
            }
            b++;
        }

        ReflectionUtil.invokeMethod(nbtSet, nbt, "AttributeModifiers", nbtTags);
        try {
            ReflectionUtil.invokeMethod(
                    ReflectionUtil.getMethod(nmsStack, "setTag", ReflectionUtil.getNMSClass("NBTTagCompound")),
                    nmsStack,
                    nbt
            );
            paramItemStack = ReflectionUtil.toBukkitCopy(nmsStack);
            return paramItemStack;
        } catch (Exception e) {
            System.err.println("Couldn't create Bukkit stack from NMS");
            e.printStackTrace();
            return null;
        }
    }

    default ItemStack fixNBT(ItemStack item) {
        return fixNBT(null, item);
    }

    default ItemStack fixNBT(Player player, ItemStack item) {
        double hp = AttUT.getItemStatValue(item, ItemStat.MAX_HEALTH, player);
        double speed = AttUT.getItemStatValue(item, ItemStat.ATTACK_SPEED, player);
        double move = AttUT.getItemStatValue(item, ItemStat.MOVEMENT_SPEED, player);
        item = setNBTAttribute(item, NBTAttribute.maxHealth, hp);
        item = setNBTAttribute(item, NBTAttribute.movementSpeed, move);
        if (!ItemUtils.isArmor(item)) {
            item = setNBTAttribute(item, NBTAttribute.attackSpeed, speed);
            item = setNBTAttribute(item, NBTAttribute.attackDamage, ItemAPI.getDefaultDamage(item) - 1.0D);
        } else {
            item = setNBTAttribute(item, NBTAttribute.armor, ItemAPI.getDefaultDefense(item));
            item = setNBTAttribute(item, NBTAttribute.armorToughness, ItemAPI.getDefaultToughness(item));
        }
        return item;
    }

    default void sendActionBar(Player p, String msg) {
        try {
            String s = ChatColor.translateAlternateColorCodes('&', msg);

            Class chatBaseComponent = ReflectionUtil.getNMSClass("IChatBaseComponent");
            Class chatSerializer = ReflectionUtil.getNMSClass("IChatBaseComponent.ChatSerializer");
            Method a = ReflectionUtil.getMethod(chatSerializer, "a", String.class);
            Object icbc = ReflectionUtil.invokeMethod(a, null, "{\"text\": \"" + s + "\"}");
            Class chatType = ReflectionUtil.getNMSClass("ChatMessageType");


            Object bar = ReflectionUtil.getNMSClass("PacketPlayOutChat")
                    .getConstructor(chatBaseComponent, chatType)
                    .newInstance(icbc, ReflectionUtil.getEnum(chatType, "GAME_INFO"));

            ReflectionUtil.sendPacket(p, bar);
        } catch (Exception e) {
            System.err.println("Could not send action bar.");
            e.printStackTrace();
        }
    }

    default void sendTitles(Player player, String titleText, String subtitleText, int fadeIn, int stay, int fadeOut) {
        try {
            Class chatBaseComponent = ReflectionUtil.getNMSClass("IChatBaseComponent");
            Class chatSerializer = ReflectionUtil.getNMSClass("IChatBaseComponent.ChatSerializer");
            Method a = ReflectionUtil.getMethod(chatSerializer, "a", String.class);
            Object chatTitle = ReflectionUtil.invokeMethod(a, null, "{\"text\": \"" + titleText + "\"}");
            Object chatSubTitle = ReflectionUtil.invokeMethod(a, null, "{\"text\": \"" + subtitleText + "\"}");

            Class titleClass = ReflectionUtil.getNMSClass("PacketPlayOutTitle");
            Class enumAction = ReflectionUtil.getNMSClass("PacketPlayOutTitle.EnumTitleAction");
            Constructor ctor1 = titleClass.getConstructor(enumAction, chatBaseComponent);
            Constructor ctor2 = titleClass.getConstructor(int.class, int.class, int.class);
            Object title = ctor1.newInstance(ReflectionUtil.getEnum(enumAction, "TITLE"), chatTitle);
            Object subTitle = ctor1.newInstance(ReflectionUtil.getEnum(enumAction, "SUBTITLE"), chatSubTitle);
            Object length = ctor2.newInstance(fadeIn, stay, fadeOut);

            ReflectionUtil.sendPackets(player, Arrays.asList(title, subTitle, length));
        } catch (Exception e) {
            System.err.println("Could not send titles");
            e.printStackTrace();
        }
    }

    default float getAttackCooldown(Player paramPlayer) {
        Class craftPlayerClass = null;
        try {
            craftPlayerClass = ReflectionUtil.getCraftClass("entity.CraftPlayer");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Method getHandle = Reflex.getMethod(craftPlayerClass, "getHandle");
        Object ep = Reflex.invokeMethod(getHandle, ReflectionUtil.getCraftPlayer(paramPlayer));

        return (float) ReflectionUtil.invokeMethod(ReflectionUtil.getMethod(ep, "o", float.class), ep, 0.0F);
    }
}

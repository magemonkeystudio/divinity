package su.nightexpress.quantumrpg.nms;

import mc.promcteam.engine.utils.Reflex;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

public class V1_12_R1 implements NMS {
//    public org.bukkit.inventory.ItemStack setNBTAttribute(org.bukkit.inventory.ItemStack item, NBTAttribute paramNBTAttribute, double a) {
//        if (paramNBTAttribute == NBTAttribute.attackDamage && ItemUtils.isBow(item))
//            return item;
//        if (a == 0.0D && (
//                paramNBTAttribute != NBTAttribute.attackSpeed || !ItemUtils.isWeapon(item)))
//            return delNBTAttribute(item, paramNBTAttribute);
//        ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
//        NBTTagCompound nbt = nmsStack.hasTag() ? nmsStack.getTag() : new NBTTagCompound();
//        NBTTagList nbtTags = nbt.getList("AttributeModifiers", 10);
//        byte b;
//        int i;
//        String[] arrayOfString;
//        for (i = (arrayOfString = ItemUtils.getAllNBTSlots(item)).length, b = 0; b < i; ) {
//            String s = arrayOfString[b];
//            if (!s.equalsIgnoreCase("offhand") || (
//                    paramNBTAttribute != NBTAttribute.attackSpeed && paramNBTAttribute != NBTAttribute.attackDamage)) {
//                double amount = a;
//                NBTTagCompound tag = new NBTTagCompound();
//                for (int j = 0; j < nbtTags.size(); j++) {
//                    NBTTagCompound nb = nbtTags.get(j);
//                    String a_key = nb.getString("AttributeName");
//                    if (a_key.equals("generic." + paramNBTAttribute) && nb.getString("Slot").equalsIgnoreCase(s)) {
//                        tag = nb;
//                        nbtTags.remove(j);
//                        break;
//                    }
//                }
//                if (paramNBTAttribute == NBTAttribute.movementSpeed) {
//                    amount = 0.1D * (1.0D + amount / 100.0D) - 0.1D;
//                } else if (paramNBTAttribute == NBTAttribute.attackSpeed) {
//                    amount /= 1000.0D;
//                    double a1 = ItemAPI.getDefaultAttackSpeed(item) * amount;
//                    amount = -(ItemAPI.getDefaultAttackSpeed(item) - a1);
//                }
//                int rand = Utils.randInt(1, 333);
//                tag.set("AttributeName", new NBTTagString("generic." + paramNBTAttribute.att()));
//                tag.set("Name", new NBTTagString("generic." + paramNBTAttribute.att()));
//                tag.set("Amount", new NBTTagDouble(amount));
//                tag.set("Operation", new NBTTagInt(0));
//                tag.set("UUIDLeast", new NBTTagInt(rand));
//                tag.set("UUIDMost", new NBTTagInt(rand));
//                tag.set("Slot", new NBTTagString(s));
//                nbtTags.add(tag);
//            }
//            b++;
//        }
//        nbt.set("AttributeModifiers", nbtTags);
//        nmsStack.setTag(nbt);
//        item = CraftItemStack.asBukkitCopy(nmsStack);
//        return item;
//    }
//
//    public org.bukkit.inventory.ItemStack delNBTAttribute(org.bukkit.inventory.ItemStack item, NBTAttribute att) {
//        ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
//        NBTTagCompound nbt = nmsStack.hasTag() ? nmsStack.getTag() : new NBTTagCompound();
//        NBTTagList nbtTags = nbt.getList("AttributeModifiers", 10);
//        byte b;
//        int i;
//        String[] arrayOfString;
//        for (i = (arrayOfString = ItemUtils.getAllNBTSlots(item)).length, b = 0; b < i; ) {
//            String s = arrayOfString[b];
//            for (int j = 0; j < nbtTags.size(); j++) {
//                NBTTagCompound nb = nbtTags.get(j);
//                String a_key = nb.getString("AttributeName");
//                if (a_key.equals("generic." + att) && nb.getString("Slot").equalsIgnoreCase(s)) {
//                    nbtTags.remove(j);
//                    break;
//                }
//            }
//            b++;
//        }
//        nbt.set("AttributeModifiers", nbtTags);
//        nmsStack.setTag(nbt);
//        item = CraftItemStack.asBukkitCopy(nmsStack);
//        return item;
//    }
//
//    public org.bukkit.inventory.ItemStack fixNBT(org.bukkit.inventory.ItemStack item) {
//        return fixNBT(null, item);
//    }
//
//    public org.bukkit.inventory.ItemStack fixNBT(Player player, org.bukkit.inventory.ItemStack item) {
//        double hp = AttUT.getItemStatValue(item, ItemStat.MAX_HEALTH, player);
//        double speed = AttUT.getItemStatValue(item, ItemStat.ATTACK_SPEED, player);
//        double move = AttUT.getItemStatValue(item, ItemStat.MOVEMENT_SPEED, player);
//        item = setNBTAttribute(item, NBTAttribute.maxHealth, hp);
//        item = setNBTAttribute(item, NBTAttribute.movementSpeed, move);
//        if (!ItemUtils.isArmor(item)) {
//            item = setNBTAttribute(item, NBTAttribute.attackSpeed, speed);
//            item = setNBTAttribute(item, NBTAttribute.attackDamage, ItemAPI.getDefaultDamage(item) - 1.0D);
//        } else {
//            item = setNBTAttribute(item, NBTAttribute.armor, ItemAPI.getDefaultDefense(item));
//            item = setNBTAttribute(item, NBTAttribute.armorToughness, ItemAPI.getDefaultToughness(item));
//        }
//        return item;
//    }

    public float getAttackCooldown(Player p) {
        Class craftPlayerClass = null;
        try {
            craftPlayerClass = ReflectionUtil.getCraftClass("entity.CraftPlayer");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Method getHandle = Reflex.getMethod(craftPlayerClass, "getHandle");
        Object ep = Reflex.invokeMethod(getHandle, ReflectionUtil.getCraftPlayer(p));

        return (float) ReflectionUtil.invokeMethod(ReflectionUtil.getMethod(ep, "n", float.class), ep, 0.0F);
    }

//    public void sendActionBar(Player p, String msg) {
//        String s = ChatColor.translateAlternateColorCodes('&', msg);
//        IChatBaseComponent icbc = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + s + "\"}");
//        PacketPlayOutChat bar = new PacketPlayOutChat(icbc, ChatMessageType.GAME_INFO);
//        (((CraftPlayer) p).getHandle()).playerConnection.sendPacket(bar);
//    }
//
//    public void sendTitles(Player player, String titleText, String subtitleText, int fadeIn, int stay, int fadeOut) {
//        IChatBaseComponent chatTitle = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + titleText + "\"}");
//        IChatBaseComponent chatSubTitle = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + subtitleText + "\"}");
//        PacketPlayOutTitle title = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, chatTitle);
//        PacketPlayOutTitle subTitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, chatSubTitle);
//        PacketPlayOutTitle length = new PacketPlayOutTitle(fadeIn, stay, fadeOut);
//        (((CraftPlayer) player).getHandle()).playerConnection.sendPacket(title);
//        (((CraftPlayer) player).getHandle()).playerConnection.sendPacket(subTitle);
//        (((CraftPlayer) player).getHandle()).playerConnection.sendPacket(length);
//    }
}

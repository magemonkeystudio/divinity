package su.nightexpress.quantumrpg.nms;

public class V1_10_R1 implements NMS {
//    public ItemStack setNBTAttribute(ItemStack item, NBTAttribute paramNBTAttribute, double a) {
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
//            double amount = a;
//            NBTTagCompound tag = new NBTTagCompound();
//            for (int j = 0; j < nbtTags.size(); j++) {
//                NBTTagCompound nb = nbtTags.get(j);
//                String a_key = nb.getString("AttributeName");
//                if (a_key.equals("generic." + paramNBTAttribute) && nb.getString("Slot").equalsIgnoreCase(s)) {
//                    tag = nb;
//                    nbtTags.remove(j);
//                    break;
//                }
//            }
//            if (paramNBTAttribute == NBTAttribute.movementSpeed) {
//                amount = 0.1D * (1.0D + amount / 100.0D) - 0.1D;
//            } else if (paramNBTAttribute == NBTAttribute.attackSpeed) {
//                amount /= 1000.0D;
//                double a1 = ItemAPI.getDefaultAttackSpeed(item) * amount;
//                amount = -(ItemAPI.getDefaultAttackSpeed(item) - a1);
//            }
//            int rand = Utils.randInt(1, 333);
//            tag.set("AttributeName", (NBTBase) new NBTTagString("generic." + paramNBTAttribute.att()));
//            tag.set("Name", (NBTBase) new NBTTagString("generic." + paramNBTAttribute.att()));
//            tag.set("Amount", (NBTBase) new NBTTagDouble(amount));
//            tag.set("Operation", (NBTBase) new NBTTagInt(0));
//            tag.set("UUIDLeast", (NBTBase) new NBTTagInt(rand));
//            tag.set("UUIDMost", (NBTBase) new NBTTagInt(rand));
//            tag.set("Slot", (NBTBase) new NBTTagString(s));
//            nbtTags.add((NBTBase) tag);
//            b++;
//        }
//        nbt.set("AttributeModifiers", (NBTBase) nbtTags);
//        nmsStack.setTag(nbt);
//        item = CraftItemStack.asBukkitCopy(nmsStack);
//        return item;
//    }
//
//    public ItemStack delNBTAttribute(ItemStack item, NBTAttribute att) {
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
//        nbt.set("AttributeModifiers", (NBTBase) nbtTags);
//        nmsStack.setTag(nbt);
//        item = CraftItemStack.asBukkitCopy(nmsStack);
//        return item;
//    }
//
//    public ItemStack fixNBT(ItemStack item) {
//        return fixNBT(null, item);
//    }
//
//    public ItemStack fixNBT(Player player, ItemStack item) {
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
//
//    public float getAttackCooldown(Player p) {
//        EntityPlayer ep = ((CraftPlayer) p).getHandle();
//        return ep.o(0.0F);
//    }
//
//    public void sendActionBar(Player p, String msg) {
//        String s = ChatColor.translateAlternateColorCodes('&', msg);
//        IChatBaseComponent icbc = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + s + "\"}");
//        PacketPlayOutChat bar = new PacketPlayOutChat(icbc, (byte) 2);
//        (((CraftPlayer) p).getHandle()).playerConnection.sendPacket((Packet) bar);
//    }
//
//    public void sendTitles(Player player, String titleText, String subtitleText, int fadeIn, int stay, int fadeOut) {
//        IChatBaseComponent chatTitle = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + titleText + "\"}");
//        IChatBaseComponent chatSubTitle = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + subtitleText + "\"}");
//        PacketPlayOutTitle title = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, chatTitle);
//        PacketPlayOutTitle subTitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, chatSubTitle);
//        PacketPlayOutTitle length = new PacketPlayOutTitle(fadeIn, stay, fadeOut);
//        (((CraftPlayer) player).getHandle()).playerConnection.sendPacket((Packet) title);
//        (((CraftPlayer) player).getHandle()).playerConnection.sendPacket((Packet) subTitle);
//        (((CraftPlayer) player).getHandle()).playerConnection.sendPacket((Packet) length);
//    }
}

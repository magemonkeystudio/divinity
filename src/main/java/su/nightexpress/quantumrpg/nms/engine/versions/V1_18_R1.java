package su.nightexpress.quantumrpg.nms.engine.versions;

import su.nightexpress.quantumrpg.nms.engine.PMS;

public class V1_18_R1 implements PMS {

//    @Override
//    public void changeSkull(Block b, String hash) {
//        TileEntitySkull skullTile = (TileEntitySkull) ((CraftWorld) b.getWorld()).getHandle().getTileEntity(new BlockPosition(b.getX(), b.getY(), b.getZ()));
//        skullTile.setGameProfile(ItemUtils.getNonPlayerProfile(hash));
//        b.getState().update(true);
//    }
//
//    @Override
//    public float getAttackCooldown(Player p) {
//        EntityPlayer ep = ((CraftPlayer) p).getHandle();
//        return ep.getAttackCooldown(0);
//    }
//
//    private Multimap<AttributeBase, AttributeModifier> getAttributes(ItemStack itemStack) {
//        Item item = CraftItemStack.asNMSCopy(itemStack).getItem();
//        Multimap<AttributeBase, AttributeModifier> attMap = null;
//
//        if (item instanceof ItemArmor) {
//            ItemArmor tool = (ItemArmor) item;
//            attMap = tool.a(tool.b());
//        } else if (item instanceof ItemTool) {
//            ItemTool tool = (ItemTool) item;
//            attMap = tool.a(EnumItemSlot.a);
//        } else if (item instanceof ItemSword) {
//            ItemSword tool = (ItemSword) item;
//            attMap = tool.a(EnumItemSlot.a);
//        } else if (item instanceof ItemTrident) {
//            ItemTrident tool = (ItemTrident) item;
//            attMap = tool.a(EnumItemSlot.a);
//        }
//
//        return attMap;
//    }
//
//    @Override
//    public double getDefaultDamage(ItemStack itemStack) {
//        if (ItemUtils.isBow(itemStack)) return 10D;
//
//        Multimap<AttributeBase, AttributeModifier> attMap = this.getAttributes(itemStack);
//        if (attMap == null) return 1D;
//
//        Collection<AttributeModifier> att = attMap.get(GenericAttributes.f);
//        double damage = (att == null || att.isEmpty()) ? 0 : att.stream().findFirst().get().getAmount();
//
//        return damage + 1;
//    }
//
//    @Override
//    public double getDefaultSpeed(ItemStack itemStack) {
//        Multimap<AttributeBase, AttributeModifier> attMap = this.getAttributes(itemStack);
//        if (attMap == null) return 0D;
//
//        Collection<AttributeModifier> att = attMap.get(GenericAttributes.h);
//        double speed = (att == null || att.isEmpty()) ? 0D : att.stream().findFirst().get().getAmount();
//
//        return speed; // 4D +
//    }
//
//    @Override
//    public double getDefaultArmor(ItemStack itemStack) {
//        Multimap<AttributeBase, AttributeModifier> attMap = this.getAttributes(itemStack);
//        if (attMap == null) return 0D;
//
//        Collection<AttributeModifier> att = attMap.get(GenericAttributes.i);
//        double speed = (att == null || att.isEmpty()) ? 0D : att.stream().findFirst().get().getAmount();
//
//        return speed;
//    }
//
//    @Override
//    public double getDefaultToughness(ItemStack itemStack) {
//        Multimap<AttributeBase, AttributeModifier> attMap = this.getAttributes(itemStack);
//        if (attMap == null) return 0D;
//
//        Collection<AttributeModifier> att = attMap.get(GenericAttributes.j);
//        double speed = (att == null || att.isEmpty()) ? 0D : att.stream().findFirst().get().getAmount();
//
//        return speed;
//    }
}

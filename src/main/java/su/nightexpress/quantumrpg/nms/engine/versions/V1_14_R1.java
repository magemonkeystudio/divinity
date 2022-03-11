package su.nightexpress.quantumrpg.nms.engine.versions;

import su.nightexpress.quantumrpg.nms.engine.PMS;

/**
 * @deprecated No longer needed as PMS handles versions using the ReflectionUtil
 */
@Deprecated
public class V1_14_R1 extends PMS {
	
//	@Override
//	public void changeSkull(Block b, String hash) {
//	    TileEntitySkull skullTile = (TileEntitySkull)((CraftWorld)b.getWorld()).getHandle().getTileEntity(new BlockPosition(b.getX(), b.getY(), b.getZ()));
//	    skullTile.setGameProfile(ItemUtils.getNonPlayerProfile(hash));
//	    b.getState().update(true);
//	}
//
//	@Override
//	public float getAttackCooldown(@NotNull Player p) {
//		EntityPlayer ep = ((CraftPlayer)p).getHandle();
//		return ep.s(0);
//	}
//
//	@Nullable
//	private Multimap<String, AttributeModifier> getAttributes(@NotNull ItemStack itemStack) {
//		Item item = CraftItemStack.asNMSCopy(itemStack).getItem();
//		Multimap<String, AttributeModifier> attMap = null;
//
//		if (item instanceof ItemArmor) {
//			ItemArmor tool = (ItemArmor) item;
//			attMap = tool.a(tool.b());
//		}
//		else if (item instanceof ItemTool) {
//			ItemTool tool = (ItemTool) item;
//			attMap = tool.a(EnumItemSlot.MAINHAND);
//		}
//		else if (item instanceof ItemSword) {
//			ItemSword tool = (ItemSword) item;
//			attMap = tool.a(EnumItemSlot.MAINHAND);
//		}
//		else if (item instanceof ItemTrident) {
//			ItemTrident tool = (ItemTrident) item;
//			attMap = tool.a(EnumItemSlot.MAINHAND);
//		}
//
//		return attMap;
//	}

//	@Override
//	public double getDefaultDamage(@NotNull ItemStack itemStack) {
//		if (ItemUtils.isBow(itemStack)) return 10D;
//
//		Multimap<String, AttributeModifier> attMap = this.getAttributes(itemStack);
//		if (attMap == null) return 1D;
//
//		Collection<AttributeModifier> att = attMap.get(GenericAttributes.ATTACK_DAMAGE.getName());
//		double damage = (att == null || att.isEmpty()) ? 0 : att.stream().findFirst().get().getAmount();
//
//		return damage + 1;
//	}
//
//	@Override
//	public double getDefaultSpeed(@NotNull ItemStack itemStack) {
//		Multimap<String, AttributeModifier> attMap = this.getAttributes(itemStack);
//		if (attMap == null) return 0D;
//
//		Collection<AttributeModifier> att = attMap.get(GenericAttributes.ATTACK_SPEED.getName());
//		double speed = (att == null || att.isEmpty()) ? 0D : att.stream().findFirst().get().getAmount();
//
//		return speed; // 4D +
//	}
//
//	@Override
//	public double getDefaultArmor(@NotNull ItemStack itemStack) {
//		Multimap<String, AttributeModifier> attMap = this.getAttributes(itemStack);
//		if (attMap == null) return 0D;
//
//		Collection<AttributeModifier> att = attMap.get(GenericAttributes.ARMOR.getName());
//		double speed = (att == null || att.isEmpty()) ? 0D : att.stream().findFirst().get().getAmount();
//
//		return speed;
//	}
//
//	@Override
//	public double getDefaultToughness(@NotNull ItemStack itemStack) {
//		Multimap<String, AttributeModifier> attMap = this.getAttributes(itemStack);
//		if (attMap == null) return 0D;
//
//		Collection<AttributeModifier> att = attMap.get(GenericAttributes.ARMOR_TOUGHNESS.getName());
//		double speed = (att == null || att.isEmpty()) ? 0D : att.stream().findFirst().get().getAmount();
//
//		return speed;
//	}
}

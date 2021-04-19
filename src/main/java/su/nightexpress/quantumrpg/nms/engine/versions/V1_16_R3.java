package su.nightexpress.quantumrpg.nms.engine.versions;

import java.util.Collection;

import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Multimap;

import net.minecraft.server.v1_16_R3.AttributeBase;
import net.minecraft.server.v1_16_R3.AttributeModifier;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.EnumItemSlot;
import net.minecraft.server.v1_16_R3.GenericAttributes;
import net.minecraft.server.v1_16_R3.Item;
import net.minecraft.server.v1_16_R3.ItemArmor;
import net.minecraft.server.v1_16_R3.ItemSword;
import net.minecraft.server.v1_16_R3.ItemTool;
import net.minecraft.server.v1_16_R3.ItemTrident;
import net.minecraft.server.v1_16_R3.TileEntitySkull;
import su.nightexpress.quantumrpg.nms.engine.PMS;
import su.nightexpress.quantumrpg.utils.ItemUtils;

public class V1_16_R3 implements PMS {
	
	@Override
	public void changeSkull(Block b, String hash) {
	    TileEntitySkull skullTile = (TileEntitySkull)((CraftWorld)b.getWorld()).getHandle().getTileEntity(new BlockPosition(b.getX(), b.getY(), b.getZ()));
	    skullTile.setGameProfile(ItemUtils.getNonPlayerProfile(hash));
	    b.getState().update(true);
	}
	
	@Override
	public float getAttackCooldown(@NotNull Player p) {
		EntityPlayer ep = ((CraftPlayer)p).getHandle();
		return ep.getAttackCooldown(0);
		//return ep.r(0);
	}

	@Nullable
	private Multimap<AttributeBase, AttributeModifier> getAttributes(@NotNull ItemStack itemStack) {
		Item item = CraftItemStack.asNMSCopy(itemStack).getItem();
		Multimap<AttributeBase, AttributeModifier> attMap = null;
		
		if (item instanceof ItemArmor) {
			ItemArmor tool = (ItemArmor) item;
			attMap = tool.a(tool.b());
		}
		else if (item instanceof ItemTool) {
			ItemTool tool = (ItemTool) item;
			attMap = tool.a(EnumItemSlot.MAINHAND);
		}
		else if (item instanceof ItemSword) {
			ItemSword tool = (ItemSword) item;
			attMap = tool.a(EnumItemSlot.MAINHAND);
		}
		else if (item instanceof ItemTrident) {
			ItemTrident tool = (ItemTrident) item;
			attMap = tool.a(EnumItemSlot.MAINHAND);
		}
		
		return attMap;
	}
	
	@Override
	public double getDefaultDamage(@NotNull ItemStack itemStack) {
		if (ItemUtils.isBow(itemStack)) return 10D;
		
		Multimap<AttributeBase, AttributeModifier> attMap = this.getAttributes(itemStack);
		if (attMap == null) return 1D;
		
		Collection<AttributeModifier> att = attMap.get(GenericAttributes.ATTACK_DAMAGE);
		double damage = (att == null || att.isEmpty()) ? 0 : att.stream().findFirst().get().getAmount();
		
		return damage + 1;
	}

	@Override
	public double getDefaultSpeed(@NotNull ItemStack itemStack) {
		Multimap<AttributeBase, AttributeModifier> attMap = this.getAttributes(itemStack);
		if (attMap == null) return 0D;
		
		Collection<AttributeModifier> att = attMap.get(GenericAttributes.ATTACK_SPEED);
		double speed = (att == null || att.isEmpty()) ? 0D : att.stream().findFirst().get().getAmount();
		
		return speed; // 4D + 
	}

	@Override
	public double getDefaultArmor(@NotNull ItemStack itemStack) {
		Multimap<AttributeBase, AttributeModifier> attMap = this.getAttributes(itemStack);
		if (attMap == null) return 0D;
		
		Collection<AttributeModifier> att = attMap.get(GenericAttributes.ARMOR);
		double speed = (att == null || att.isEmpty()) ? 0D : att.stream().findFirst().get().getAmount();
		
		return speed;
	}

	@Override
	public double getDefaultToughness(@NotNull ItemStack itemStack) {
		Multimap<AttributeBase, AttributeModifier> attMap = this.getAttributes(itemStack);
		if (attMap == null) return 0D;
		
		Collection<AttributeModifier> att = attMap.get(GenericAttributes.ARMOR_TOUGHNESS);
		double speed = (att == null || att.isEmpty()) ? 0D : att.stream().findFirst().get().getAmount();
		
		return speed;
	}
}

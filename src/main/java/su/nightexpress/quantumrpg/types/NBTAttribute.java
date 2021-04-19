package su.nightexpress.quantumrpg.types;

import java.util.UUID;

import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

public enum NBTAttribute {

	armor(
			"generic.armor", 
			Attribute.GENERIC_ARMOR, 
			"1f1173-9999-3333-5555-99cb0245f9c1"), // '1' at start
	
	armorToughness(
			"generic.armorToughness", 
			Attribute.GENERIC_ARMOR_TOUGHNESS, 
			"1f1173-9999-3333-5555-99cb0245f9c2"),
	
	attackDamage(
			"generic.attackDamage", 
			Attribute.GENERIC_ATTACK_DAMAGE,
			"1f1173-9999-3333-5555-99cb0245f9c3"),
	
	attackSpeed(
			"generic.attackSpeed", 
			Attribute.GENERIC_ATTACK_SPEED,
			"1f1173-9999-3333-5555-99cb0245f9c4"),
	
	movementSpeed(
			"generic.movementSpeed", 
			Attribute.GENERIC_MOVEMENT_SPEED,
			"1f1173-9999-3333-5555-99cb0245f9c5"),
	
	maxHealth(
			"generic.maxHealth", 
			Attribute.GENERIC_MAX_HEALTH,
			"1f1173-9999-3333-5555-99cb0245f9c6"),
	
	knockbackResistance(
			"generic.knockbackResistance", 
			Attribute.GENERIC_KNOCKBACK_RESISTANCE,
			"1f1173-9999-3333-5555-99cb0245f9c7"),
	;
	
	private String nmsName;
	private Attribute att;
	private String uuid;
	
	private NBTAttribute(@NotNull String nmsName, @NotNull Attribute att, @NotNull String uuid) {
		this.nmsName = nmsName;
		this.att = att;
		this.uuid = uuid;
	}
	
	@NotNull
	public String getNmsName() {
		return this.nmsName;
	}
	
	@NotNull
	public Attribute getAttribute() {
		return this.att;
	}
	
	@NotNull
	public UUID getUUID(@NotNull EquipmentSlot slot) {
		return UUID.fromString(slot.ordinal() + this.uuid);
	}
}

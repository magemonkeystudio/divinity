package com.promcteam.divinity.manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class EntityEquipmentSnapshot implements EntityEquipment {
    private final UUID        id;
    private final ItemStack[] armorContents;
    private final ItemStack   mainHand;
    private final ItemStack   offHand;

    public EntityEquipmentSnapshot(@NotNull LivingEntity entity) {
        id = entity.getUniqueId();
        EntityEquipment equipment = entity.getEquipment();
        if (equipment == null) {
            armorContents = new ItemStack[4];
            mainHand = null;
            offHand = null;
            return;
        }
        armorContents = equipment.getArmorContents();
        mainHand = equipment.getItemInMainHand();
        offHand = equipment.getItemInOffHand();
    }

    @Override
    public void setItem(@NotNull EquipmentSlot equipmentSlot, @Nullable ItemStack itemStack) {
        // no-op
    }

    @Override
    public void setItem(@NotNull EquipmentSlot equipmentSlot, @Nullable ItemStack itemStack, boolean b) {
        // no-op
    }

    @NotNull
    @Override
    public ItemStack getItem(@NotNull EquipmentSlot equipmentSlot) {
        switch (equipmentSlot) {
            case HAND:
                return mainHand;
            case OFF_HAND:
                return offHand;
            case FEET:
                return armorContents[0];
            case LEGS:
                return armorContents[1];
            case CHEST:
                return armorContents[2];
            case HEAD:
                return armorContents[3];
            default:
                throw new IllegalArgumentException();
        }
    }

    @NotNull
    @Override
    public ItemStack getItemInMainHand() {
        return mainHand;
    }

    @Override
    public void setItemInMainHand(@Nullable ItemStack itemStack) {
        // no-op
    }

    @Override
    public void setItemInMainHand(@Nullable ItemStack itemStack, boolean b) {
        // no-op
    }

    @NotNull
    @Override
    public ItemStack getItemInOffHand() {
        return offHand;
    }

    @Override
    public void setItemInOffHand(@Nullable ItemStack itemStack) {
        // no-op
    }

    @Override
    public void setItemInOffHand(@Nullable ItemStack itemStack, boolean b) {
        // no-op
    }

    @NotNull
    @Override
    public ItemStack getItemInHand() {
        return mainHand;
    }

    @Override
    public void setItemInHand(@Nullable ItemStack itemStack) {
        // no-op
    }

    @Nullable
    @Override
    public ItemStack getHelmet() {
        return armorContents[3];
    }

    @Override
    public void setHelmet(@Nullable ItemStack itemStack) {
        // no-op
    }

    @Override
    public void setHelmet(@Nullable ItemStack itemStack, boolean b) {
        // no-op
    }

    @Nullable
    @Override
    public ItemStack getChestplate() {
        return armorContents[2];
    }

    @Override
    public void setChestplate(@Nullable ItemStack itemStack) {
        // no-op
    }

    @Override
    public void setChestplate(@Nullable ItemStack itemStack, boolean b) {
        // no-op
    }

    @Nullable
    @Override
    public ItemStack getLeggings() {
        return armorContents[1];
    }

    @Override
    public void setLeggings(@Nullable ItemStack itemStack) {
        // no-op
    }

    @Override
    public void setLeggings(@Nullable ItemStack itemStack, boolean b) {
        // no-op
    }

    @Nullable
    @Override
    public ItemStack getBoots() {
        return armorContents[0];
    }

    @Override
    public void setBoots(@Nullable ItemStack itemStack) {
        // no-op
    }

    @Override
    public void setBoots(@Nullable ItemStack itemStack, boolean b) {
        // no-op
    }

    @NotNull
    @Override
    public ItemStack[] getArmorContents() {
        return armorContents;
    }

    @Override
    public void setArmorContents(@NotNull ItemStack[] itemStacks) {
        // no-op
    }

    @Override
    public void clear() {
        // no-op
    }

    @Override
    public float getItemInHandDropChance() {
        return 0;
    }

    @Override
    public void setItemInHandDropChance(float v) {
        // no-op
    }

    @Override
    public float getItemInMainHandDropChance() {
        return 0;
    }

    @Override
    public void setItemInMainHandDropChance(float v) {
        // no-op
    }

    @Override
    public float getItemInOffHandDropChance() {
        return 0;
    }

    @Override
    public void setItemInOffHandDropChance(float v) {
        // no-op
    }

    @Override
    public float getHelmetDropChance() {
        return 0;
    }

    @Override
    public void setHelmetDropChance(float v) {
        // no-op
    }

    @Override
    public float getChestplateDropChance() {
        return 0;
    }

    @Override
    public void setChestplateDropChance(float v) {
        // no-op
    }

    @Override
    public float getLeggingsDropChance() {
        return 0;
    }

    @Override
    public void setLeggingsDropChance(float v) {
        // no-op
    }

    @Override
    public float getBootsDropChance() {
        return 0;
    }

    @Override
    public void setBootsDropChance(float v) {
        // no-op
    }

    @Nullable
    @Override
    public Entity getHolder() {
        return Bukkit.getEntity(id);
    }
}
